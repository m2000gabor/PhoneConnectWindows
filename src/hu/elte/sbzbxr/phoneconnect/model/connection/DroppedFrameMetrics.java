package hu.elte.sbzbxr.phoneconnect.model.connection;

public class DroppedFrameMetrics {
    private String lastPicName ="";
    private int lastID;

    private int droppedFrames;
    private int arrivedFrames;

    public DroppedFrameMetrics(){
        reset();
    }

    public void arrivedPicture(String name){
        String currentName = getNameFromFilename(name);
        int currentId=getIdFromFilename(name);

        if(!currentName.equals(lastPicName)){
            reset();
            lastPicName=currentName;
            lastID=currentId-1;
        }

        arrivedFrames++;
        droppedFrames += Math.abs(currentId-(lastID+1));
        lastID=currentId;
    }

    public void reset() {
       arrivedFrames=0;
       droppedFrames=0;
       lastID=0;
       lastPicName="";
    }

    public int getMetrics() {
        if(droppedFrames+arrivedFrames==0) return 0;
        return ((int) Math.round(100*(double)droppedFrames/(double)(arrivedFrames+droppedFrames)));
    }

    public static String getNameFromFilename(String name){
        String[] arr = name.split("__");
        return arr[0];
    }

    public static int getIdFromFilename(String name){
        String[] arr = name.split("__");
        int r=0;
        try{
            r=Integer.parseInt(arr[1].split("\\.")[0].substring(4));
        }catch (Exception e){
            System.err.println("Couldnt detect segment id");
        }
        return r;
    }

}
