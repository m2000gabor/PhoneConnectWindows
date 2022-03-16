package hu.elte.sbzbxr.phoneconnect.controller;

import hu.elte.sbzbxr.phoneconnect.model.Picture;

import java.util.concurrent.ArrayBlockingQueue;

public class PictureProvider {
    private final ArrayBlockingQueue<Picture> arrived = new ArrayBlockingQueue<Picture>(100);

    public void askNextPicture(Controller controller){
        Picture next = arrived.poll();
        if(next!=null){
            controller.showPicture(next);
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Picture n= arrived.take();
                        controller.showPicture(n);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void pictureArrived(Picture pic){
        if(!arrived.offer(pic)){
            System.err.println("Too much segment arrived, one is thrown");
        }
    }
}
