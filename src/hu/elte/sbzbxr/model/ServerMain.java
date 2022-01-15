package hu.elte.sbzbxr.model;

import hu.elte.sbzbxr.model.connection.ConnectionManager;
import hu.elte.sbzbxr.model.connection.MyNetworkProtocolFrame;

import java.io.*;
import java.net.SocketAddress;

public class ServerMain
{
    ConnectionManager connectionManager;
    boolean isRunning=false;

    public ServerMain() {connectionManager=new ConnectionManager();}

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
        isRunning=true;
        System.out.println("Connection established");
        while (isRunning) {
            try {
                MyNetworkProtocolFrame frame = MyNetworkProtocolFrame.inputStreamToFrame(i);
                switch (frame.getType()) {
                    case PROTOCOL_PING -> reactToPingRequest(frame, o);
                    case PROTOCOL_SEGMENT -> reactToSegmentArrivedRequest(frame);
                    default -> throw new RuntimeException("Unhandled type");
                }
            } catch (IOException e) {
                e.printStackTrace();
                stopConnection();
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

    private void reactToSegmentArrivedRequest(MyNetworkProtocolFrame frame){
        String directoryPath = getClass().getProtectionDomain().getCodeSource().getLocation().toString() + "videos";
        directoryPath = directoryPath.substring(6);
        File directory = new File(directoryPath);
        if (! directory.exists()){
            if(directory.mkdir()){
                System.err.println("Videos directory created at: "+directoryPath);
            }
        }
        File outputFile = new File(directoryPath+"/"+frame.getName());

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(frame.getData());
            System.err.println("File saved to " + outputFile.getAbsolutePath());
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
    }
}