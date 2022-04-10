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

    public ImageCanvas() {
        super();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //repaint();
            }
        });
        setOpaque(true);
    }

    public void showImage(Picture pic){
        currentImage.set(pic.getImg());

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

    /**
     * Use for static images, like QR code.
     * @param img to show
     */
    public void showImage(BufferedImage img){
        currentImage.set(img);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D graphics = (Graphics2D) g;
        setRenderHints(graphics);

        Image img = currentImage.get();
        if(img==null) return;

        int og_img_width=img.getWidth(null);
        int og_img_height=img.getHeight(null);
        int canvas_width = getWidth();
        int canvas_height = getHeight();
        double x_ratio =  canvas_width/ (double) og_img_width;
        double y_ratio =  canvas_height/ (double) og_img_height;
        double final_ration = Math.min(x_ratio,y_ratio);
        int final_width = (int) Math.round(og_img_width*final_ration);
        int final_height = (int) Math.round(og_img_height*final_ration);

        graphics.drawImage(img,0,0,final_width,final_height,null);
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
                new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);//Adjust to other than bilinear
        after = scaleOp.filter(before, after);
        return after;
    }

    public static BufferedImage getResizedImage(BufferedImage img, int newW, int newH) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage ret = new BufferedImage(newW, newH, img.getType());
        Graphics2D g = ret.createGraphics();
        setRenderHints(g);
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        return ret;
    }

    private static void setRenderHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public AtomicReference<BufferedImage> getCurrentImage() {
        return currentImage;
    }
}
