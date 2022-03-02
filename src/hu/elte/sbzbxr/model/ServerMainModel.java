package hu.elte.sbzbxr.model;

import hu.elte.sbzbxr.controller.Controller;
import hu.elte.sbzbxr.model.connection.ConnectionManager;
import hu.elte.sbzbxr.model.connection.FileCutter;
import hu.elte.sbzbxr.model.connection.SafeOutputStream;
import hu.elte.sbzbxr.model.connection.protocol.FrameType;
import hu.elte.sbzbxr.model.connection.protocol.MyNetworkProtocolFrame;

import java.io.*;
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
                MyNetworkProtocolFrame frame = MyNetworkProtocolFrame.inputStreamToFrame(i);
                switch (frame.getType()) {
                    case PROTOCOL_PING -> reactToPingRequest(frame);
                    case PROTOCOL_SEGMENT -> reactToSegmentArrivedRequest(frame);
                    case PROTOCOL_NOTIFICATION -> reactToNotificationArrived(frame);
                    case PROTOCOL_FILE -> fileCreator.reactToIncomingFileTransfer(frame);
                    default -> throw new RuntimeException("Unhandled type");
                }
            } catch (IOException e) {
                e.printStackTrace();
                stopConnection();
                connectionManager.restartServer();
            }
        }
    }

    private void reactToPingRequest(MyNetworkProtocolFrame frame){
        String receivedMsg = new String(frame.getData());
        System.out.println("Received ping message: "+receivedMsg);
        MyNetworkProtocolFrame answerFrame = new MyNetworkProtocolFrame(FrameType.PROTOCOL_PING,"Hello client!");
        try {
            connectionManager.getOutputStream().write(answerFrame.getAsBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to send the answer to the ping request");
        }
    }

    private void reactToNotificationArrived(MyNetworkProtocolFrame frame){
        SendableNotification notification = new SendableNotification(frame.getData());
        controller.showNotification(notification);
    }

    private void reactToSegmentArrivedRequest(MyNetworkProtocolFrame frame){
        if(SAVE_TO_FILE){saveSegmentToFile(frame);}
        Picture picture=Picture.create(frame.getName(), frame.getData());
        if(!isStreaming){
            controller.startStreaming(picture);
            isStreaming=true;// If this is the first segment, start the streaming
        }
        controller.segmentArrived(picture);
    }

    private void reactToIncomingFileTransfer(MyNetworkProtocolFrame frame){

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

    private void createAndSaveFile(MyNetworkProtocolFrame frame,File directory){
        File outputFile = new File(directory.getAbsolutePath()+"\\"+frame.getName());

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(frame.getData());
            System.out.println("File saved to " + outputFile.getAbsolutePath());
            System.out.println(frame.getDataLength()+" bytes saved");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to save the file");
        }
    }

    private void saveSegmentToFile(MyNetworkProtocolFrame frame){
        File directory = obtainFileTransferDirectory();

        //current timestamped folder
        String timestamp= frame.getName().split("__part")[0];
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

    public void stopConnection(){//todo show it in the ui?
        isRunning=false;
        isStreaming=false;
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
                outputStream.write(fileCutter.current().getAsBytes());
                System.out.println("sent a piece of file");
                fileCutter.next();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}