package hu.elte.sbzbxr.model;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ServerMain
{
    public static final int BUFFER_SIZE = 4096;
    ConnectionManager connectionManager;

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

    //From: https://gist.github.com/teocci/0187ac32dcdbd57d8aaa89342be90f89
    public void connectionEstablished(AsynchronousSocketChannel ch){
        // Greet the client
        ch.write( ByteBuffer.wrap( "Hello, I am Echo Server 2020, let's have an engaging conversation!\n".getBytes() ) );

        // Allocate a byte buffer (4K) to read from the client
        ByteBuffer byteBuffer = ByteBuffer.allocate( BUFFER_SIZE );
        try
        {
            // Read the first line
            int bytesRead = ch.read( byteBuffer ).get( 20, TimeUnit.SECONDS );

            boolean running = true;
            while( bytesRead != -1 && running )
            {
                System.out.println( "bytes read: " + bytesRead );

                // Make sure that we have data to read
                if( byteBuffer.position() > 2 )
                {
                    // Make the buffer ready to read
                    byteBuffer.flip();

                    // Convert the buffer into a line
                    byte[] lineBytes = new byte[ bytesRead ];
                    byteBuffer.get( lineBytes, 0, bytesRead );
                    String line = new String( lineBytes );

                    // Debug
                    System.out.println( "Message: " + line );
                    ///todo send to the view

                    // Echo back to the caller
                    ch.write( ByteBuffer.wrap( line.getBytes() ) );

                    // Make the buffer ready to write
                    byteBuffer.clear();

                    // Read the next line
                    bytesRead = ch.read( byteBuffer ).get( 10, TimeUnit.SECONDS );
                }
                else
                {
                    // An empty line signifies the end of the conversation in our protocol
                    running = false;
                }
            }
        }
        catch (InterruptedException | ExecutionException e)
        {
            e.printStackTrace();
        } catch (TimeoutException e)
        {
            // The user exceeded the 10 second timeout, so close the connection
            ch.write( ByteBuffer.wrap( "Good Bye\n".getBytes() ) );
            System.out.println( "Connection timed out(10 sec), closing connection" );
        }

        System.out.println( "End of conversation" );
        try
        {
            // Close the connection if we need to
            if( ch.isOpen() )
            {
                ch.close();
            }
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }

    public void connectionFailed(Throwable exc){
        System.err.println(exc.getMessage());
    }
}