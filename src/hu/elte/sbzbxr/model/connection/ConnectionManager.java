package hu.elte.sbzbxr.model.connection;

import hu.elte.sbzbxr.model.ServerMainModel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

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
/*
        try {
            InetAddress a =InetAddress.getLocalHost();
            return a;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            String localHost = InetAddress.getLocalHost().getHostAddress();
            System.out.println("LocalHost"+localHost);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
*/
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ip = socket.getLocalAddress().getHostAddress();
            System.out.println("ip: " + ip);
            return InetAddress.getByName(ip);//todo might remove this
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        Enumeration<NetworkInterface> nets = null;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (nets == null) throw new AssertionError();
        for (NetworkInterface netint : Collections.list(nets)){
            for (Iterator<InetAddress> it = netint.getInetAddresses().asIterator(); it.hasNext(); ) {
                InetAddress address = it.next();
                System.out.println(netint.getName() + " "+address);
            }

            if(netint.getName().equals("wlan2")){return Collections.list(netint.getInetAddresses()).get(0);}
        }


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
            serverMainModel.connectionEstablished(client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SafeOutputStream getOutputStream(){
        try {
            if(client == null) return null;
            return new SafeOutputStream(client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
