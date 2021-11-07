package hu.elte.sbzbxr.model;

import hu.elte.sbzbxr.controller.Controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ServerMain
{
    public static final int SERVER_PORT = 5000;


    private SocketAddress serverAddress = new InetSocketAddress(SERVER_PORT);
    private AsynchronousServerSocketChannel listener;
    private Controller controller;

    public void init(){
        try
        {
            // Create an AsynchronousServerSocketChannel that will listen on port 5000
            listener = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(SERVER_PORT));
            serverAddress = listener.getLocalAddress();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void startServer(Controller owner){
        controller = owner;

        // Listen for a new request
        listener.accept( null, new CompletionHandler<AsynchronousSocketChannel,Void>() {

            @Override
            public void completed(AsynchronousSocketChannel ch, Void att)
            {
                // Accept the next connection
                listener.accept( null, this );
                controller.connectionEstablished(ch);
            }

            @Override
            public void failed(Throwable exc, Void att) {
               controller.connectionFailed(exc);
            }
        });
    }

    public ServerMain(){}

    public SocketAddress getServerAddress() {
        return serverAddress;
    }

}