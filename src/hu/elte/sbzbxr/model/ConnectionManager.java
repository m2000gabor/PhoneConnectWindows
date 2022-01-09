package hu.elte.sbzbxr.model;

import java.io.IOException;
import java.net.*;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Collections;
import java.util.Enumeration;

public class ConnectionManager {
    private static final int SERVER_PORT = 5000;

    private AsynchronousServerSocketChannel listener;
    private ServerMain serverMain;

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
            listener = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(getPublicIpAddress(),SERVER_PORT));
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

    public SocketAddress getServerAddress() throws IOException {return listener.getLocalAddress();}

    public void startServer(ServerMain owner){
        serverMain = owner;

        // Listen for a new request
        listener.accept( null, new CompletionHandler<AsynchronousSocketChannel,Void>() {

            @Override
            public void completed(AsynchronousSocketChannel ch, Void att)
            {
                // Accept the next connection
                listener.accept( null, this );
                serverMain.connectionEstablished(ch);
            }

            @Override
            public void failed(Throwable exc, Void att) {
                serverMain.connectionFailed(exc);
            }
        });
    }
}
