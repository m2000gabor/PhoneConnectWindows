package hu.elte.sbzbxr.phoneconnect.model;

import hu.elte.sbzbxr.phoneconnect.controller.Controller;
import hu.elte.sbzbxr.phoneconnect.model.connection.ConnectionManager;
import hu.elte.sbzbxr.phoneconnect.model.connection.FileCutter;
import hu.elte.sbzbxr.phoneconnect.model.connection.SafeOutputStream;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.*;
import hu.elte.sbzbxr.phoneconnect.model.persistence.FileCreator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;

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
                switch (type) {
                    case INTERNAL_MESSAGE -> reactToInternalMessage(MessageFrame.deserialize(i));
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

    private void reactToInternalMessage(MessageFrame messageFrame){
        MessageType type = MessageType.valueOf(messageFrame.name);
        String receivedMsg = messageFrame.message;
        switch (type){
            default -> throw new IllegalArgumentException("Unknown type of internal message");
            case PING -> pingMessageArrived(receivedMsg);
            case RESTORE -> restoreMessageArrived(receivedMsg);
        }

    }

    private void restoreMessageArrived(String msg){

    }

    private void pingMessageArrived(String receivedMsg){
        System.out.println("Received ping message: "+receivedMsg);
        MessageFrame answerFrame = new MessageFrame(MessageType.PING,"Hello client!");
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
        fileCreator.saveFileFrame(frame);
    }

    private void saveSegmentToFile(FileFrame frame){
        String timestamp = frame.name.split("__part")[0];
        fileCreator.saveSegment(frame,timestamp);
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