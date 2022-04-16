package hu.elte.sbzbxr.phoneconnect.model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.LinkedList;

public class Picture {
    private final String filename;
    private final String folderName;
    private BufferedImage img;
    public final LinkedList<AbstractMap.SimpleEntry<String,String>> timestamps = new LinkedList<>();

    private Picture(String filename, String folderName, BufferedImage img, LinkedList<AbstractMap.SimpleEntry<String, String>> t) {
        this.filename = filename;
        this.img = img;
        this.folderName = folderName;
        this.timestamps.addAll(t);
    }

    public static Picture create(String filename, String folderName, byte[] data, LinkedList<AbstractMap.SimpleEntry<String,String>> timestamps){
        try{
            return new Picture(filename, folderName, ImageIO.read(new ByteArrayInputStream(data)), timestamps);
        }catch (IOException e){
            e.printStackTrace();
            System.err.println("Cannot create picture from this byte array");
        }
        return null;
    }

    public String getFilename() {
        return filename;
    }

    public BufferedImage getImg() {
        return img;
    }

    public void setImg(BufferedImage img) {
        this.img = img;
    }

    public void addTimestamp(String label, Long timeInMillis){
        String readableTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(timeInMillis);
        timestamps.add(new AbstractMap.SimpleEntry<>(label,readableTime));
    }

    private boolean hasBeenPainted=false;
    public void addTimestamp_FirstPainted(){
        if(hasBeenPainted)return;
        hasBeenPainted=true;
        String readableTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(System.currentTimeMillis());
        timestamps.add(new AbstractMap.SimpleEntry<>("firstPainted",readableTime));
    }

    @Override
    public String toString() {
        return "Picture{" +
                "name='" + filename + '\'' +
                ", img=" + img +
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

    public String getFolderName() {
        return folderName;
    }
}
