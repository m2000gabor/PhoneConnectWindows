package hu.elte.sbzbxr.view;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class ImageCanvas extends Canvas {
    private BufferedImage currentImage;
    private BufferedImage originalImage;

    public ImageCanvas() {
        super();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if(originalImage!=null){
                    currentImage=getScaledBufferedImage(originalImage,getWidth(),getHeight());
                }
            }
        });
    }

    public void showImage(BufferedImage img){
        originalImage=img;
        currentImage=getScaledBufferedImage(originalImage,getWidth(),getHeight());
        repaint();

    }

    @Override
    public void paint(Graphics g) {
        //super.paint(g);
        g.drawImage(currentImage,0,0,null);
    }

    //From: https://stackoverflow.com/questions/4216123/how-to-scale-a-bufferedimage
    public static BufferedImage getScaledBufferedImage(BufferedImage before,double maxWidth, double maxHeight){
        double w = maxWidth/ before.getWidth();
        double h = maxHeight/ before.getHeight();
        double ration = Math.min(w, h);
        BufferedImage after = new BufferedImage((int) Math.round(maxWidth), (int) Math.round(maxHeight), BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        at.scale(ration, ration);
        AffineTransformOp scaleOp =
                new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);//Adjust to other than bilinear
        after = scaleOp.filter(before, after);
        return after;
    }

    public static BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
        Graphics2D g = dimg.createGraphics();
        //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        return dimg;
    }
}
