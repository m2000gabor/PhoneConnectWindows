package hu.elte.sbzbxr.phoneconnect.model;

import hu.elte.sbzbxr.phoneconnect.controller.Controller;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.model.connection.FileCutterCreator;
import hu.elte.sbzbxr.phoneconnect.model.connection.SafeOutputStream;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.FileCutter;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.*;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message.*;
import hu.elte.sbzbxr.phoneconnect.model.persistence.FileCreator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class ServerMainModel
{
    private static final boolean SAVE_TO_FILE = false;
    private final FileCreator fileCreator = new FileCreator();
    private Controller controller;
    private ConnectionManager connectionManager;
    boolean isRunning=false;
    boolean isStreaming=false;

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

    public void connectionEstablished(InputStream i){
        controller.connectionEstablished();
        isRunning=true;
        System.out.println("Connection established");
        while (isRunning) {
            try {
                FrameType type = NetworkFrameCreator.getType(i);
                //System.out.println("Frame arrived with type: " + type);
                switch (type) {
                    case INTERNAL_MESSAGE -> reactToInternalMessage(i);
                    case SEGMENT -> reactToSegmentArrivedRequest(SegmentFrame.deserialize(type,i));
                    case NOTIFICATION -> reactToNotificationArrived(NotificationFrame.deserialize(i));
                    case FILE -> reactToIncomingFileTransfer(FileFrame.deserialize(type,i));
                    case BACKUP_FILE -> reactToIncomingBackup(BackupFileFrame.deserialize(type,i));
                    default -> throw new RuntimeException("Unhandled type");
                }
            } catch (IOException e) {
                e.printStackTrace();
                stopConnection();
                controller.disconnected();
                connectionManager.restartServer();
                break;
            }
        }
    }

    private void reactToInternalMessage(InputStream inputStream) throws IOException {
        MessageFrame messageFrame = MessageFrame.deserialize(inputStream);
        MessageType type = messageFrame.messageType;
        switch (type){
            default -> throw new IllegalArgumentException("Unknown type of internal message");
            case PING -> pingMessageArrived(PingMessageFrame.deserialize(inputStream).message);
            case RESTORE_GET_AVAILABLE -> restoreGetMessageArrived();
            case RESTORE_START_RESTORE -> restoreStartMessageArrived(StartRestoreMessageFrame.deserialize(inputStream));
            case END_OF_STREAM -> controller.endOfStreaming();
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

    private void pingMessageArrived(String receivedMsg){
        System.out.println("Received ping message: "+receivedMsg);
        MessageFrame answerFrame = new PingMessageFrame("Hello client!");
        try {
            connectionManager.getOutputStream().write(answerFrame.serialize().getAsBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to send the answer to the ping request");
        }
    }

    private void reactToNotificationArrived(NotificationFrame notificationFrame){
        controller.showNotification(notificationFrame);
    }

    private void reactToSegmentArrivedRequest(SegmentFrame segment) {
        if(SAVE_TO_FILE){saveSegmentToFile(segment);}
        Picture picture=Picture.create(segment.filename, segment.getData());
        if(!isStreaming){
            controller.startStreaming(picture);
            isStreaming=true;// If this is the first segment, start the streaming
        }
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

    public void stopConnection(){
        isRunning=false;
        isStreaming=false;
        fileCreator.connectionStopped();
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
            files.forEach(file -> {
                FileCutter fileCutter = FileCutterCreator.create(file,type,backupID,folderSize);
                System.out.println("Sending file: "+file.getName());
                SafeOutputStream outputStream = connectionManager.getOutputStream();
                if(outputStream==null){System.err.println("You need to connect first!"); return;}

                while(!fileCutter.isEnd()){
                    try {
                        outputStream.write(fileCutter.current().serialize().getAsBytes());
                        System.out.println("sent a piece of file");
                        fileCutter.next();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                System.out.println("File sent");
            });
            controller.showNotification(new NotificationFrame("Sending completed", "Successfully sent the chosen files", null));
        }).start();
    }
}