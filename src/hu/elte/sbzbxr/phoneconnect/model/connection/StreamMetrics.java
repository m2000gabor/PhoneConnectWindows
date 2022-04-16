package hu.elte.sbzbxr.phoneconnect.model.connection;

public class StreamMetrics {
    private static final int refreshRate = 5000; //in millis
    private final DroppedFrameMetrics currentMetrics;
    private final DroppedFrameMetrics totalMetrics;
    private long lastTime=0;
    private int lastMetric  =-1;

    public StreamMetrics() {
        currentMetrics = new DroppedFrameMetrics();
        totalMetrics = new DroppedFrameMetrics();
    }

    public void arrivedPicture(String name){
        currentMetrics.arrivedPicture(getNameFromFilename(name),getIdFromFilename(name));
        totalMetrics.arrivedPicture(getNameFromFilename(name),getIdFromFilename(name));
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastTime>refreshRate){
            lastMetric = currentMetrics.getMetrics();
            currentMetrics.reset();
            lastTime=System.currentTimeMillis();
        }
    }

    public String getCurrentMetrics(){
        return "Current drop rate (\\5s): "+lastMetric+"%";
    }

    public String getOverallMetrics(){
        return "Overall drop rate: "+ totalMetrics.getMetrics()+"%";
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

    public static int getPercentage(int droppedFrames, int arrivedFrames){
        if(droppedFrames+arrivedFrames==0) return 0;
        return ((int) Math.round(100*(double)droppedFrames/(double)(arrivedFrames+droppedFrames)));
    }

}
