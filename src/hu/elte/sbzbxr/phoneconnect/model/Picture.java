package hu.elte.sbzbxr.phoneconnect.model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Picture {
    private final String name;
    private final BufferedImage img;

    private Picture(String name, BufferedImage img) {
        this.name = name;
        this.img = img;
    }

    public static Picture create(String name, byte[] data){
        try{
            return new Picture(name,ImageIO.read(new ByteArrayInputStream(data)));
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
}
