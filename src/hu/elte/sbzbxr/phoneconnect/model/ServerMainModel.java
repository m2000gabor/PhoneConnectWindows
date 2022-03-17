package hu.elte.sbzbxr.phoneconnect.model;

import hu.elte.sbzbxr.phoneconnect.controller.Controller;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.model.connection.FileCutter;
import hu.elte.sbzbxr.phoneconnect.model.connection.SafeOutputStream;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;

public class ServerMainModel
{
    private static final boolean SAVE_TO_FILE = false;
    private final FileCreator fileCreator = new FileCreator(this);
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
                switch (type) {
                    case PING -> reactToPingRequest(PingFrame.deserialize(i));
                    case SEGMENT -> reactToSegmentArrivedRequest(FileFrame.deserialize(type,i));
                    case NOTIFICATION -> reactToNotificationArrived(NotificationFrame.deserialize(i));
                    case FILE -> reactToIncomingFileTransfer(FileFrame.deserialize(type,i));
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

    private void reactToPingRequest(PingFrame pingFrame){
        String receivedMsg = pingFrame.name;
        System.out.println("Received ping message: "+receivedMsg);
        PingFrame answerFrame = new PingFrame("Hello client!");
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

    private void reactToSegmentArrivedRequest(FileFrame segment) {
        if(SAVE_TO_FILE){saveSegmentToFile(segment);}
        Picture picture=Picture.create(segment.name, segment.getData());
        if(!isStreaming){
            controller.startStreaming(picture);
            isStreaming=true;// If this is the first segment, start the streaming
        }
        controller.segmentArrived(picture);
    }

    private void reactToIncomingFileTransfer(FileFrame frame){
        fileCreator.reactToIncomingFileTransfer(frame);
    }

    File obtainFileTransferDirectory(){
        //Main pictures folder
        String directoryPath = getClass().getProtectionDomain().getCodeSource().getLocation().toString() + "saves";
        directoryPath = directoryPath.substring(6);
        File directory = new File(directoryPath);
        if (! directory.exists()){
            if(directory.mkdir()){
                System.err.println("Saves directory created at: "+directoryPath);
            }
        }
        return directory;
    }

    private void createAndSaveFile(FileFrame frame,File directory){
        File outputFile = new File(directory.getAbsolutePath()+"\\"+frame.name);

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(frame.getData());
            System.out.println("File saved to " + outputFile.getAbsolutePath());
            System.out.println(frame.getDataLength()+" bytes saved");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to save the file");
        }
    }

    private void saveSegmentToFile(FileFrame frame){
        File directory = obtainFileTransferDirectory();

        //current timestamped folder
        String timestamp= frame.name.split("__part")[0];
        String directoryPath = directory.getAbsolutePath()  + "/" + timestamp;
        directory = new File(directoryPath);
        if (! isStreaming){
            if(directory.mkdir()){
                System.err.println("New directory created at: "+directoryPath);
            }
        }

        createAndSaveFile(frame,directory);
    }

    public void connectionFailed(Throwable exc){
        System.err.println(exc.getMessage());
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

    public void sendFile(File file) {
        FileCutter fileCutter = new FileCutter(file);
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
    }
}