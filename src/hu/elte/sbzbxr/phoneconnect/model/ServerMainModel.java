package hu.elte.sbzbxr.phoneconnect.model;

import hu.elte.sbzbxr.phoneconnect.controller.Controller;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.model.connection.FileCutterCreator;
import hu.elte.sbzbxr.phoneconnect.model.connection.SafeOutputStream;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.FileCutter;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.*;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.*;
import hu.elte.sbzbxr.phoneconnect.model.connection.udp.SegmentFramePartBuffer;
import hu.elte.sbzbxr.phoneconnect.model.connection.udp.UdpReader;
import hu.elte.sbzbxr.phoneconnect.model.persistence.FileCreator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerMainModel
{
    private static final boolean SAVE_TO_FILE = false;
    private final FileCreator fileCreator = new FileCreator();
    private Controller controller;
    private ConnectionManager connectionManager;
    AtomicBoolean isTcpRunning = new AtomicBoolean(false);
    AtomicBoolean isUdpRunning = new AtomicBoolean(false);

    public ServerMainModel() {connectionManager=new ConnectionManager();}

    /**
     *
     * @return Address it's listening to
     */
    public SocketAddress start() {
        SocketAddress address =connectionManager.init();
        connectionManager.startServer(this);
        return address;
    }

    public void tcpConnectionEstablished(BufferedInputStream i){
        controller.connectionEstablished();
        isTcpRunning.set(true);
        System.out.println("Connection established");
        while (isTcpRunning.get()) {
            try {
                FrameType type = NetworkFrameCreator.getType(i);
                //System.out.println("Frame arrived with type: " + type);
                switch (type) {
                    case INTERNAL_MESSAGE:  reactToInternalMessage(i); break;
                    case SEGMENT: {
                        long timestamp_beforeDeserialization = System.currentTimeMillis();
                        SegmentFrame segmentFrame = SegmentFrame.deserialize(type,i);
                        long timestamp_afterDeserialization = System.currentTimeMillis();
                        segmentFrame.addTimestamp("beforeDeserialization",timestamp_beforeDeserialization);
                        segmentFrame.addTimestamp("afterDeserialization",timestamp_afterDeserialization);
                        reactToSegmentArrivedRequest(segmentFrame);
                    }break;
                    case NOTIFICATION: reactToNotificationArrived(NotificationFrame.deserialize(i));break;
                    case FILE: reactToIncomingFileTransfer(FileFrame.deserialize(type,i));break;
                    case BACKUP_FILE: reactToIncomingBackup(BackupFileFrame.deserialize(type,i));break;
                    default: throw new RuntimeException("Unhandled type");
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        stopConnection();
        controller.disconnected();
        connectionManager.restartServer();
    }

    public void udpConnectionStart(DatagramSocket socket) {
        SegmentFramePartBuffer buffer = new SegmentFramePartBuffer();
        isUdpRunning.set(true);
        while (isUdpRunning.get()) {
            try {
                UdpSegmentFramePart part = UdpReader.readSegmentFramePart(socket);
                buffer.add(part);
            }catch (IOException exception){
                continue;
            }

            for(SegmentFrame segmentFrame : buffer.pullFinished()){
                reactToSegmentArrivedRequest(segmentFrame);
            }
        }
        socket.close();
        isUdpRunning.set(false);
    }

    public void stopConnection(){
        isTcpRunning.set(false);
        controller.endOfStreaming();
        fileCreator.connectionStopped();
    }

    private void reactToInternalMessage(InputStream inputStream) throws IOException {
        MessageFrame messageFrame = MessageFrame.deserialize(inputStream);
        MessageType type = messageFrame.messageType;
        switch (type){
            default: throw new IllegalArgumentException("Unknown type of internal message");
            case PING: pingMessageArrived(PingMessageFrame.deserialize(inputStream));break;
            case RESTORE_GET_AVAILABLE: restoreGetMessageArrived();break;
            case RESTORE_START_RESTORE: restoreStartMessageArrived(StartRestoreMessageFrame.deserialize(inputStream));break;
            case START_OF_STREAM: controller.startStreaming();break;
            case END_OF_STREAM: controller.endOfStreaming();break;
        }

    }

    private void restoreStartMessageArrived(StartRestoreMessageFrame messageFrame) {
        System.out.println("Start restore message arrived! Restore dir: "+ messageFrame.backupId);
        List<File> filesToRestore = fileCreator.getFileManager().getFilesOfBackup(messageFrame.backupId);
        long folderSize = filesToRestore.stream().map(File::length).reduce(0L,Long::sum);
        if(filesToRestore.isEmpty()){
            System.err.println("There is no backup directory with the name: "+messageFrame.backupId);
        }else{
            sendFiles(filesToRestore,FrameType.RESTORE_FILE, messageFrame.backupId, folderSize);
        }
    }

    private void restoreGetMessageArrived(){
        System.out.println("Received: get backup list message");
        ArrayList<AbstractMap.SimpleImmutableEntry<String,Long>> folders = fileCreator.getFileManager().getBackupFolderNames();
        MessageFrame answerFrame = new RestorePostMessageFrame(folders);
        try {
            connectionManager.getOutputStream().write(answerFrame.serialize().getAsBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to send the answer to the restore request");
        }
    }

    private void pingMessageArrived(PingMessageFrame pingFrame){
        pingFrame.requestArrived();
        try {
            connectionManager.getOutputStream().write(pingFrame.serialize().getAsBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to send the answer to the ping request");
        }
        System.out.println("Received ping message: "+pingFrame.toString());
    }

    private void reactToNotificationArrived(NotificationFrame notificationFrame){
        controller.showNotification(notificationFrame);
    }

    private void reactToSegmentArrivedRequest(SegmentFrame segment) {
        if(SAVE_TO_FILE){saveSegmentToFile(segment);}
        long timestamp_beforePictureCreation = System.currentTimeMillis();
        Picture picture=Picture.create(segment.filename, segment.folderName, segment.getData(),segment.timestamps);
        if(picture==null) return;
        long timestamp_afterPictureCreation = System.currentTimeMillis();
        picture.addTimestamp("beforePictureCreation",timestamp_beforePictureCreation);
        picture.addTimestamp("afterPictureCreation",timestamp_afterPictureCreation);
        controller.segmentArrived(picture);
    }

    private void reactToIncomingFileTransfer(FileFrame frame){
        Runnable r = () -> controller.showNotification(new NotificationFrame( "File arrived", frame.filename + " arrived", null));
        fileCreator.saveFileFrame(frame,r);
    }

    private void reactToIncomingBackup(BackupFileFrame frame){
        fileCreator.saveBackupFrame(frame);
    }

    private void saveSegmentToFile(SegmentFrame frame){
        fileCreator.saveSegment(frame);
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public SocketAddress getServerAddress(){
        try {
            return connectionManager.getServerAddress();
        } catch (IOException e) {
            return null;
        }
    }

    public void sendFiles(List<File> files, FrameType type, String backupID, Long folderSize){
        if(files == null) return;
        new Thread(()->{
            AtomicBoolean errorOccurred= new AtomicBoolean(false);
            files.forEach(file -> {
                FileCutter fileCutter = FileCutterCreator.create(file,type,backupID,folderSize);
                System.out.println("Sending file: "+file.getName());
                SafeOutputStream outputStream = connectionManager.getOutputStream();
                if(outputStream==null){System.err.println("You need to connect first!");
                    errorOccurred.set(true);
                    return;}

                while(!fileCutter.isEnd()){
                    try {
                        outputStream.write(fileCutter.current().serialize().getAsBytes());
                        System.out.println("sent a piece of file");
                        fileCutter.next();
                    } catch (IOException e) {
                        e.printStackTrace();
                        errorOccurred.set(true);
                        break;
                    }
                }
                if(!errorOccurred.get()){
                    System.out.println("File sent");
                }else{
                    System.err.println("Error occurred, file not sent");
                }

            });
            if(!errorOccurred.get()){
                controller.showNotification(new NotificationFrame("Sending completed", "Successfully sent the chosen files", null));
            }else{
                controller.showNotification(new NotificationFrame("Sending failed", "Failed to send the chosen files", null));
            }
        }).start();
    }

}