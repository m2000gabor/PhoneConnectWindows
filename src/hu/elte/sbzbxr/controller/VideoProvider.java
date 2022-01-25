package hu.elte.sbzbxr.controller;

import hu.elte.sbzbxr.model.VideoNameManager;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;

@Deprecated
public class VideoProvider {
    private final VideoNameManager videoNameManager;
    private final ArrayBlockingQueue<String> arrived = new ArrayBlockingQueue<String>(100);

    public VideoProvider(VideoNameManager videoNameManager) {
        this.videoNameManager = videoNameManager;
    }

    public void askNextVideo(Controller controller){
        /*
        String next = arrived.poll();
        if(next!=null){
            controller.playVideo(next);
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String n= arrived.take();
                        controller.playVideo(n);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
         */
    }

    public void segmentArrived(String path){
        if(!arrived.offer(path)){
            System.err.println("To much segment arrived, one is thrown");
        }
    }
}
