package hu.elte.sbzbxr.phoneconnect.controller;

import hu.elte.sbzbxr.phoneconnect.model.Picture;
import hu.elte.sbzbxr.phoneconnect.model.ServerMainModel;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.NotificationFrame;
import hu.elte.sbzbxr.phoneconnect.view.MainScreenJPG;
import hu.elte.sbzbxr.phoneconnect.view.WelcomeScreen;

import java.awt.*;
import java.io.File;
import java.net.SocketAddress;
import java.util.Objects;

public class Controller {
    private ControllerState state;
    private final ServerMainModel model;
    private WelcomeScreen welcomeScreen;
    private MainScreenJPG mainScreen;

    public Controller() {
        state=ControllerState.WELCOME_DISCONNECTED;
        this.model= new ServerMainModel();
    }

    public void start(){
        SocketAddress serverAddress = model.start();
        if(Objects.isNull(serverAddress)){
            System.err.println("Failed to establish connection");
        }else{
            welcomeScreen.setIpAddress(serverAddress.toString().replace("/",""));
        }
    }

    public void init() {
        welcomeScreen = new WelcomeScreen(this);
        model.setController(this);
    }

    public void connectionEstablished(){
        welcomeScreen.setConnectionLabel(true);
        state=ControllerState.WELCOME_CONNECTED;
    }

    private PictureProvider pictureProvider;
    public void startStreaming(Picture picture){
        mainScreen= new MainScreenJPG(model.getServerAddress());
        mainScreen.setController(this);
        state=ControllerState.STREAM_RUNNING;

        pictureProvider=new PictureProvider();
        pictureProvider.pictureArrived(picture);

        welcomeScreen.dispose();
        mainScreen.initVideoPlayer();
        pictureProvider.askNextPicture(this);
    }

    public void showPicture(Picture picture){
        mainScreen.showPicture(picture.getImg());
        pictureProvider.askNextPicture(this);
    }

    public void segmentArrived(Picture picture) {
        pictureProvider.pictureArrived(picture);
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

        trayIcon.displayMessage(notification.title, notification.text, TrayIcon.MessageType.INFO);
    }

    public void sendFilesToPhone(java.util.List<File> files){
        for (File file : files) {
            model.sendFile(file);
        }
    }

    public void disconnected(){
        switch (state){
            case WELCOME_DISCONNECTED -> {return;}
            case WELCOME_CONNECTED -> welcomeScreen.setConnectionLabel(false);
            case STREAM_RUNNING -> {
                welcomeScreen = new WelcomeScreen(this);
                SocketAddress serverAddress = model.getServerAddress();
                welcomeScreen.setIpAddress(serverAddress.toString().replace("/",""));
                mainScreen.dispose();
            }
        }

        state=ControllerState.WELCOME_DISCONNECTED;
    }
}
