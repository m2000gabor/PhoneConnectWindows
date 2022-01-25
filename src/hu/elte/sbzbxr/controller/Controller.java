package hu.elte.sbzbxr.controller;

import hu.elte.sbzbxr.model.Picture;
import hu.elte.sbzbxr.model.ServerMainModel;
import hu.elte.sbzbxr.view.MainScreenJPG;
import hu.elte.sbzbxr.view.WelcomeScreen;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

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
            welcomeScreen.setIpAddress(serverAddress.toString());

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

}
