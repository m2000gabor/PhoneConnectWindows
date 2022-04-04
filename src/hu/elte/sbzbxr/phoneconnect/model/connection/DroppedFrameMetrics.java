package hu.elte.sbzbxr.phoneconnect.model.connection;

import static hu.elte.sbzbxr.phoneconnect.model.connection.StreamMetrics.getPercentage;

public class DroppedFrameMetrics {
    private String lastPicName ="";
    private int lastID;

    private int droppedFrames;
    private int arrivedFrames;

    public DroppedFrameMetrics(){
        reset();
    }

    public void arrivedPicture(String filename,int fileID){
        if(!filename.equals(lastPicName)){
            reset();
            lastPicName= filename;
            lastID= fileID -1;
        }

        arrivedFrames++;
        droppedFrames += Math.abs(fileID -(lastID+1));
        lastID= fileID;
    }

    public void reset() {
       arrivedFrames=0;
       droppedFrames=0;
       lastID=0;
       lastPicName="";
    }

    public int getMetrics() {
        return getPercentage(droppedFrames,arrivedFrames);
    }

}
