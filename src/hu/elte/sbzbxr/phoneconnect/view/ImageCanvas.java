package hu.elte.sbzbxr.phoneconnect.view;

import hu.elte.sbzbxr.phoneconnect.model.Picture;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

public class ImageCanvas extends JComponent {
    private final AtomicReference<BufferedImage> currentImage = new AtomicReference<>(null);
    private final AtomicReference<BufferedImage> originalImage = new AtomicReference<>(null);

    public ImageCanvas() {
        super();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if(originalImage.get() == null){
                    currentImage.set(getScaledBufferedImage(originalImage.get(),getWidth(),getHeight()));
                }
            }
        });
        setOpaque(true);
    }

    public void showImage(Picture pic){
        originalImage.set(pic.getImg());

        long timestamp_beforeScale = System.currentTimeMillis();
        pic.addTimestamp("timestamp_beforeScale", timestamp_beforeScale);
        currentImage.set(getScaledBufferedImage(originalImage.get(),getWidth(),getHeight()));
        long timestamp_afterScale = System.currentTimeMillis();
        pic.addTimestamp("timestamp_afterScale", timestamp_afterScale);

        //repaint();
        RepaintManager rm = RepaintManager.currentManager(this);
        boolean b = rm.isDoubleBufferingEnabled();
        rm.setDoubleBufferingEnabled(false);

        long timestamp_beforePaintImmediately = System.currentTimeMillis();
        pic.addTimestamp("timestamp_beforePaintImmediately", timestamp_beforePaintImmediately);
        paintImmediately(0,0,getWidth(),getHeight());
        long timestamp_afterPaintImmediately = System.currentTimeMillis();
        pic.addTimestamp("timestamp_afterPaintImmediately", timestamp_afterPaintImmediately);

        rm.setDoubleBufferingEnabled(b);
    }

    public void showImage(BufferedImage img){
        originalImage.set(img);
        currentImage.set(getScaledBufferedImage(originalImage.get(),getWidth(),getHeight()));

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics = (Graphics2D) g;

        graphics.drawImage(currentImage.get(),0,0,null);
        Toolkit.getDefaultToolkit().sync();
    }

    //From: https://stackoverflow.com/questions/4216123/how-to-scale-a-bufferedimage
    public static BufferedImage getScaledBufferedImage(BufferedImage before,double maxWidth, double maxHeight){
        if(before==null) return null;
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
