package hu.elte.sbzbxr.controller;

import hu.elte.sbzbxr.model.ServerMainModel;
import hu.elte.sbzbxr.model.VideoNameManager;
import hu.elte.sbzbxr.view.MainScreen;
import hu.elte.sbzbxr.view.WelcomeScreen;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import javax.swing.*;
import java.io.File;
import java.net.SocketAddress;
import java.util.Objects;

public class Controller {
    private final ServerMainModel model;
    private WelcomeScreen welcomeScreen;
    private MainScreen mainScreen;

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

    private VideoProvider videoProvider;
    public void startStreaming(String folderPath,String firstFileName){
        mainScreen= new MainScreen(model.getServerAddress());
        mainScreen.setController(this);

        videoProvider =new VideoProvider(new VideoNameManager(folderPath,firstFileName));

        welcomeScreen.dispose();
        mainScreen.initVideoPlayer();
        videoProvider.askNextVideo(this);
    }

    private String currentlyPlayedVideoPath;
    public void playVideo(String path){
        currentlyPlayedVideoPath=path;
        mainScreen.playVideo(path);
    }

    public void videoFinished(MediaPlayer mediaPlayer) {
        videoProvider.askNextVideo(this);
        deleteFile(currentlyPlayedVideoPath);
    }

    private void deleteFile(String currentlyPlayedVideoPath) {
        /*
        SwingUtilities.invokeLater( ()->{
                    File file = new File(currentlyPlayedVideoPath);
                    if(file.delete()){
                        System.out.println("File deleted: "+file.getName());
                    }else{
                        System.out.println("Cannot delete file: "+file.getName());
                    }
                }
        );
        */
    }

    public void segmentArrived(File outputFile) {
        videoProvider.segmentArrived(outputFile.getAbsolutePath());
    }
}
