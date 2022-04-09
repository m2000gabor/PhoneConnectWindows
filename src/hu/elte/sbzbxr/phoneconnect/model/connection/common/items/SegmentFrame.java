package hu.elte.sbzbxr.phoneconnect.model.connection.common.items;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedList;

//version 2.0
public class SegmentFrame extends FileFrame{
    public final LinkedList<AbstractMap.SimpleEntry<String,String>> timestamps = new LinkedList<>();

    public SegmentFrame(String filename, Long totalSize, byte[] data, String folderName, Long folderSize) {
        super(FrameType.SEGMENT, filename,totalSize, data, folderName, folderSize);
        System.out.println("Segment created with name: "+ filename);
        if(folderName==null) {folderName="";}
    }

    public SegmentFrame(String filename, byte[] data, String folderName) {
        this(filename, 0L, data, folderName,0L);
    }


    public static SegmentFrame deserialize(FrameType type,InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new SegmentFrame(deserializer.getString(),deserializer.getLong(),deserializer.getByteArray(), deserializer.getString(), deserializer.getLong());
    }

    public void addTimestamp(String label, Long timeInMillis){
        String readableTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(timeInMillis);
        timestamps.add(new AbstractMap.SimpleEntry<>(label,readableTime));
    }

    @Override
    public String toString() {
        return "SegmentFrame{" +
                "filename='" + filename + '\'' +
                ", fileTotalSize=" + fileTotalSize +
                ", data=" + Arrays.toString(data) +
                ", folderName='" + folderName + '\'' +
                ", folderSize=" + folderSize +
                ", type=" + type +
                ", timestamps=\n" + getMapAsString(timestamps) +
                '}';
    }

    private static String getMapAsString(LinkedList<AbstractMap.SimpleEntry<String,String>> timestamps){
        StringBuilder sb = new StringBuilder();
        timestamps.forEach((item)->{
            sb.append("key=").append(item.getKey()).append("; value=").append(item.getValue()).append("\n");
        });
        return sb.toString();
    }
}
