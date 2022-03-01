package hu.elte.sbzbxr.model;

import hu.elte.sbzbxr.controller.Controller;
import hu.elte.sbzbxr.model.connection.ConnectionManager;
import hu.elte.sbzbxr.model.connection.protocol.FrameType;
import hu.elte.sbzbxr.model.connection.protocol.MyNetworkProtocolFrame;

import javax.naming.InvalidNameException;
import java.io.*;
import java.net.SocketAddress;
import java.security.InvalidParameterException;
import java.util.Optional;

public class ServerMainModel
{
    private static final boolean SAVE_TO_FILE = false;
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

    public void connectionEstablished(InputStream i, OutputStream o){
        controller.connectionEstablished();
        isRunning=true;
        System.out.println("Connection established");
        while (isRunning) {
            try {
                MyNetworkProtocolFrame frame = MyNetworkProtocolFrame.inputStreamToFrame(i);
                switch (frame.getType()) {
                    case PROTOCOL_PING -> reactToPingRequest(frame, o);
                    case PROTOCOL_SEGMENT -> reactToSegmentArrivedRequest(frame);
                    case PROTOCOL_NOTIFICATION -> reactToNotificationArrived(frame);
                    case PROTOCOL_FILE -> reactToIncomingFileTransfer(frame);
                    default -> throw new RuntimeException("Unhandled type");
                }
            } catch (IOException e) {
                e.printStackTrace();
                stopConnection();
                connectionManager.restartServer();
            }
        }
    }

    private void reactToPingRequest(MyNetworkProtocolFrame frame,OutputStream outputStream){
        String receivedMsg = new String(frame.getData());
        System.out.println("Received ping message: "+receivedMsg);
        MyNetworkProtocolFrame answerFrame = new MyNetworkProtocolFrame(FrameType.PROTOCOL_PING,"Hello client!");
        try {
            outputStream.write(answerFrame.getAsBytes());
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

    private String currentFileTransfer=null;
    private FileOutputStream fileOutputStream=null;
    private void reactToIncomingFileTransfer(MyNetworkProtocolFrame frame){
        if(currentFileTransfer==null){//nothing is in progress
            currentFileTransfer = frame.getName();
            File directory = obtainFileTransferDirectory();
            Optional<FileOutputStream> tmp= createFile(frame.getName(),directory);
            tmp.ifPresent(outputStream -> {
                fileOutputStream = outputStream;
                try {
                    fileOutputStream.write(frame.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }else if(currentFileTransfer.equals(frame.getName())){//this frame is a part of an ongoing transfer
            if(frame.getDataLength()==0){//end signal
                currentFileTransfer=null;
                try {
                    fileOutputStream.close();
                    fileOutputStream=null;
                    System.out.println("File arrived: " + frame.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{//append to file
                try {
                    fileOutputStream.write(frame.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{//other file transfer is in progress
            throw new InvalidParameterException("Another file transfer is in progress. ("+ currentFileTransfer +")");
        }
    }

    private File obtainFileTransferDirectory(){
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

    private Optional<FileOutputStream> createFile(String name , File directory){
        File outputFile = new File(directory.getAbsolutePath()+"\\"+name);
        FileOutputStream outputStream=null;
        try {
            outputStream = new FileOutputStream(outputFile);
            System.out.println("FileOutputStream created: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to create the file");
        }
        return Optional.ofNullable(outputStream);
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
}