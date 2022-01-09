package hu.elte.sbzbxr.controller;

import hu.elte.sbzbxr.model.ServerMain;
import hu.elte.sbzbxr.view.WelcomeScreen;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Controller {
    private final ServerMain model;
    private final WelcomeScreen view;

    public Controller(ServerMain model, WelcomeScreen view) {
        this.model = model;
        this.view = view;
    }

    public void start(){
        SocketAddress serverAddress = model.start();
        if(Objects.isNull(serverAddress)){
            System.err.println("Failed to establish connection");
        }else{
            view.setIpAddress(serverAddress.toString());
        }
    }


}
