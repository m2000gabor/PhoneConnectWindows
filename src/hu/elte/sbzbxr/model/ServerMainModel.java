package hu.elte.sbzbxr.model;

import hu.elte.sbzbxr.controller.Controller;
import hu.elte.sbzbxr.model.connection.ConnectionManager;
import hu.elte.sbzbxr.model.connection.MyNetworkProtocolFrame;

import java.io.*;
import java.net.SocketAddress;

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
        MyNetworkProtocolFrame answerFrame = new MyNetworkProtocolFrame(MyNetworkProtocolFrame.FrameType.PROTOCOL_PING,"Hello client!");
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

    @Deprecated
    private void saveSegmentToFile(MyNetworkProtocolFrame frame){
        //Main pictures folder
        String directoryPath = getClass().getProtectionDomain().getCodeSource().getLocation().toString() + "pictures";
        directoryPath = directoryPath.substring(6);
        File directory = new File(directoryPath);
        if (! directory.exists()){
            if(directory.mkdir()){
                System.err.println("Pictures directory created at: "+directoryPath);
            }
        }

        //current timestamped folder
        String timestamp= frame.getName().split("__part")[0];
        directoryPath = directoryPath + "/" + timestamp;
        directory = new File(directoryPath);
        if (! isStreaming){
            if(directory.mkdir()){
                System.err.println("New directory created at: "+directoryPath);
            }
        }

        File outputFile = new File(directoryPath+"\\"+frame.getName());

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(frame.getData());
            System.out.println("File saved to " + outputFile.getAbsolutePath());
            System.out.println(frame.getDataLength()+" bytes saved");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to save the file");
        }
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