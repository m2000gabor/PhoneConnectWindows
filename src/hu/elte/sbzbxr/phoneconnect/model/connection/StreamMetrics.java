package hu.elte.sbzbxr.phoneconnect.model.connection;

public class StreamMetrics {
    private static final int refreshRate = 3000; //in millis
    private final DroppedFrameMetrics droppedFrameMetrics;
    private long lastTime=0;
    private String lastMetric ="just started";

    public StreamMetrics() {
        droppedFrameMetrics = new DroppedFrameMetrics();
    }

    public void arrivedPicture(String name){
        droppedFrameMetrics.arrivedPicture(name);
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastTime>refreshRate){
            lastMetric = "Dropping rate: "+droppedFrameMetrics.getMetrics()+"%";
            droppedFrameMetrics.reset();
            lastTime=System.currentTimeMillis();
        }
    }

    public String getMetrics(){
        return lastMetric;
    }

}
