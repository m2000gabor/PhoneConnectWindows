package hu.elte.sbzbxr.phoneconnect.model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.LinkedList;

public class Picture {
    private final String name;
    private BufferedImage img;
    public final LinkedList<AbstractMap.SimpleEntry<String,String>> timestamps = new LinkedList<>();

    private Picture(String name, BufferedImage img,LinkedList<AbstractMap.SimpleEntry<String,String>> t) {
        this.name = name;
        this.img = img;
        this.timestamps.addAll(t);
    }

    public static Picture create(String name, byte[] data, LinkedList<AbstractMap.SimpleEntry<String,String>> t){
        try{
            return new Picture(name,ImageIO.read(new ByteArrayInputStream(data)), t);
        }catch (IOException e){
            e.printStackTrace();
            System.err.println("Cannot create picture from this byte array");
        }
        return null;
    }

    public String getName() {
        return name;
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
                "name='" + name + '\'' +
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
}
