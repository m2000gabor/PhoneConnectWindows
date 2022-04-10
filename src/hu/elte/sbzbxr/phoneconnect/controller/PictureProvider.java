package hu.elte.sbzbxr.phoneconnect.controller;

import hu.elte.sbzbxr.phoneconnect.model.Picture;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PictureProvider {
    private final ArrayBlockingQueue<Picture> arrived = new ArrayBlockingQueue<Picture>(3);
    private final PictureFunctional callback;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public PictureProvider(PictureFunctional callback) {
        this.callback = callback;
    }


    public void start(){
        if(!isRunning.compareAndSet(false,true)){
            System.err.println("Picture provider is already running!");
        }
        executor.submit(()->{
            while(isRunning.get()){
                try {
                    Picture p = arrived.take();
                    long timestamp_pictureTook = System.currentTimeMillis();
                    p.addTimestamp("pictureTook",timestamp_pictureTook);
                    callback.consume(p);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void pictureArrived(Picture pic){
        if(!isRunning.get()){start();}
        long timestamp_pictureInserted = System.currentTimeMillis();
        pic.addTimestamp("pictureInserted",timestamp_pictureInserted);
        if(!arrived.offer(pic)){
            System.err.println("Too much segment arrived, one is thrown away");
        }
    }


    public void stop() {
        isRunning.set(false);
        arrived.clear();
    }
}
