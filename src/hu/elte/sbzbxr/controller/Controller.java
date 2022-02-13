package hu.elte.sbzbxr.controller;

import hu.elte.sbzbxr.model.Picture;
import hu.elte.sbzbxr.model.SendableNotification;
import hu.elte.sbzbxr.model.ServerMainModel;
import hu.elte.sbzbxr.view.MainScreenJPG;
import hu.elte.sbzbxr.view.WelcomeScreen;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import java.awt.*;
import java.net.SocketAddress;
import java.util.Objects;

public class Controller {
    private final ServerMainModel model;
    private WelcomeScreen welcomeScreen;
    private MainScreenJPG mainScreen;

    public Controller(ServerMainModel model, WelcomeScreen view) {
        this.model = model;
        this.welcomeScreen = view;
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
        welcomeScreen.setController(this);
        model.setController(this);
    }

    public void connectionEstablished(){
        welcomeScreen.setConnectionLabel(true);
    }

    private PictureProvider pictureProvider;
    public void startStreaming(Picture picture){
        mainScreen= new MainScreenJPG(model.getServerAddress());
        mainScreen.setController(this);

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

    public void videoFinished(MediaPlayer mediaPlayer) {
        pictureProvider.askNextPicture(this);
    }

    public void segmentArrived(Picture picture) {
        pictureProvider.pictureArrived(picture);
    }

    public void showNotification(SendableNotification notification) {
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

    public static void displayTray(SendableNotification notification) throws AWTException {
        //Obtain only one instance of the SystemTray object
        SystemTray tray = SystemTray.getSystemTray();

        //If the icon is a file
        Image image = Toolkit.getDefaultToolkit().createImage("resources/icon.jpg");
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        //Let the system resize the image if needed
        trayIcon.setImageAutoSize(true);
        //Set tooltip text for the tray icon
        trayIcon.setToolTip("System tray icon demo");
        tray.add(trayIcon);

        trayIcon.displayMessage(notification.getTitle(), notification.getText(), TrayIcon.MessageType.INFO);
    }
}
