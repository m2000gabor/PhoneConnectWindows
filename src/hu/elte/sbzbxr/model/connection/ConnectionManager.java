package hu.elte.sbzbxr.model.connection;

import hu.elte.sbzbxr.model.ServerMainModel;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;

public class ConnectionManager {
    private static final int SERVER_PORT = 5000;

    private ServerSocket serverSocket;
    private ServerMainModel serverMainModel;
    private Socket client;

    public ConnectionManager(){}

    /**
     * Start listening on the specified port
     * @return the address, it's listening to
     */
    public SocketAddress init(){
        try
        {
            // Create an AsynchronousServerSocketChannel that will listen on port 5000
            //System.out.println("getPublicIpAddress() = " + getPublicIpAddress().toString());
            serverSocket = new ServerSocket(SERVER_PORT,0,getPublicIpAddress());
            return getServerAddress();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    private InetAddress getPublicIpAddress(){
        Enumeration<NetworkInterface> nets = null;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (nets == null) throw new AssertionError();
        for (NetworkInterface netint : Collections.list(nets))
            if(netint.getName().equals("wlan2")){return Collections.list(netint.getInetAddresses()).get(0);}

        return null;
    }

    public SocketAddress getServerAddress() throws IOException {return serverSocket.getLocalSocketAddress();}

    public void startServer(ServerMainModel owner){
        serverMainModel = owner;
        // Listen for a new request
        new Thread(this::restartServer).start();
    }

    public void restartServer(){
        try {
            if(client!=null){client.close();}
            System.out.println("Waiting for connection");
            client = serverSocket.accept();
            serverMainModel.connectionEstablished(client.getInputStream(), client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
