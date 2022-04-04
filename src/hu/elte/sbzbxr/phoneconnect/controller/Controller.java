package hu.elte.sbzbxr.phoneconnect.controller;

import hu.elte.sbzbxr.phoneconnect.model.Picture;
import hu.elte.sbzbxr.phoneconnect.model.ServerMainModel;
import hu.elte.sbzbxr.phoneconnect.model.connection.StreamMetrics;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.NotificationFrame;
import hu.elte.sbzbxr.phoneconnect.view.Frame_Connected_NoScreenShare;
import hu.elte.sbzbxr.phoneconnect.view.Frame_NotConnected;
import hu.elte.sbzbxr.phoneconnect.view.Frame_ScreenShare;

import java.awt.*;
import java.io.File;
import java.net.SocketAddress;
import java.util.Objects;

public class Controller {
    private ControllerState currentState;
    private final ServerMainModel model;
    private final StreamMetrics streamMetrics;

    private Frame_NotConnected frameNotConnected;
    private Frame_Connected_NoScreenShare frameConnectedNoScreenShare;
    private Frame_ScreenShare frameScreenShare;

    public Controller() {
        currentState =ControllerState.WELCOME_DISCONNECTED;
        this.model= new ServerMainModel();
        streamMetrics = new StreamMetrics();
    }

    public void start(){
        SocketAddress serverAddress = model.start();
        if(Objects.isNull(serverAddress)){
            System.err.println("Failed to establish connection");
        }
        frameNotConnected = new Frame_NotConnected(this,serverAddress);
    }

    public void init() {
        model.setController(this);
    }

    public void connectionEstablished(){
        disposeAll();
        frameConnectedNoScreenShare = new Frame_Connected_NoScreenShare(this, model.getServerAddress());
        currentState = ControllerState.WELCOME_CONNECTED;
    }

    private PictureProvider pictureProvider;
    public void startStreaming(Picture picture){
        disposeAll();
        frameScreenShare = new Frame_ScreenShare(model.getServerAddress(),this );
        currentState = ControllerState.STREAM_RUNNING;

        pictureProvider=new PictureProvider();
        pictureProvider.pictureArrived(picture);

        frameScreenShare.initVideoPlayer();
        pictureProvider.askNextPicture(this);
    }

    public void showPicture(Picture picture){
        frameScreenShare.showPicture(picture.getImg());
        pictureProvider.askNextPicture(this);
    }

    public void segmentArrived(Picture picture) {
        pictureProvider.pictureArrived(picture);
        streamMetrics.arrivedPicture(picture.getName());
        frameScreenShare.updateMetrics(streamMetrics.getCurrentMetrics(),streamMetrics.getOverallMetrics());
    }

    public void showNotification(NotificationFrame notification) {
        System.out.println(notification);

        //From: https://stackoverflow.com/questions/34490218/how-to-make-a-windows-notification-in-java
        if (SystemTray.isSupported()) {
            try {
                displayTray(notification);
            } catch (AWTException e) {
                e.printStackTrace();
                System.err.println("Cannot add Tray");
            }
        } else {
            System.err.println("System tray not supported!");
        }
    }

    private TrayIcon trayIcon = null;
    public void displayTray(NotificationFrame notification) throws AWTException {
        if(trayIcon == null){
            //Obtain only one instance of the SystemTray object
            SystemTray tray = SystemTray.getSystemTray();
            //If the icon is a file
            Image image = Toolkit.getDefaultToolkit().createImage("resources/icon.jpg");
            //Alternative (if the icon is on the classpath):
            //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));
            trayIcon = new TrayIcon(image, "Tray Demo");
            //Let the system resize the image if needed
            trayIcon.setImageAutoSize(true);
            //Set tooltip text for the tray icon
            trayIcon.setToolTip("System tray icon demo");
            tray.add(trayIcon);
        }

        if(notification.appName==null){
            trayIcon.displayMessage(notification.title, notification.text, TrayIcon.MessageType.INFO);
        }else{
            trayIcon.displayMessage(notification.appName, notification.title+": "+notification.text, TrayIcon.MessageType.INFO);
        }
    }

    public void sendFilesToPhone(java.util.List<File> files){
        model.sendFiles(files, FrameType.FILE, null, 0L);
    }

    public void disconnected(){
        switch (currentState){
            case WELCOME_DISCONNECTED -> {return;}
            case WELCOME_CONNECTED, STREAM_RUNNING -> {
                disposeAll();
                frameNotConnected = new Frame_NotConnected(this, model.getServerAddress());
            }
        }

        currentState = ControllerState.WELCOME_DISCONNECTED;
    }

    private void disposeAll(){
        if(frameConnectedNoScreenShare!=null) frameConnectedNoScreenShare.dispose();
        if(frameNotConnected!=null) frameNotConnected.dispose();
        if(frameScreenShare!=null) frameScreenShare.dispose();
    }

    public void endOfStreaming() {
        disposeAll();
        frameConnectedNoScreenShare = new Frame_Connected_NoScreenShare(this, model.getServerAddress());
    }
}
