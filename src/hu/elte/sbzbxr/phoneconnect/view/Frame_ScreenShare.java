package hu.elte.sbzbxr.phoneconnect.view;


import hu.elte.sbzbxr.phoneconnect.controller.Controller;
import hu.elte.sbzbxr.phoneconnect.model.Picture;
import hu.elte.sbzbxr.phoneconnect.model.persistence.FileCreator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Objects;

import static hu.elte.sbzbxr.phoneconnect.Main.LOG_SEGMENTS;
import static hu.elte.sbzbxr.phoneconnect.Main.SAVE_RESIZED_IMG;
import static hu.elte.sbzbxr.phoneconnect.view.Frame_Connected_NoScreenShare.setupDragAndDropSupport;

public class Frame_ScreenShare extends JFrame {
    private final Controller controller;
    JPanel northPanel;
    JPanel centerPanel;
    JLabel ipAddressLabel;
    JLabel connectionLabel;
    JLabel messageLabel;
    JLabel metricsLabel;
    ImageCanvas canvas;


    public Frame_ScreenShare(SocketAddress serverAddress, Controller controller){
        this.controller = controller;
        setFancyLookAndFeel();

        //window
        setLayout(new BorderLayout());

        //UI elements
        northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.PAGE_AXIS));

        ipAddressLabel =new JLabel();
        northPanel.add(ipAddressLabel);

        connectionLabel =new JLabel();
        northPanel.add(connectionLabel);

        messageLabel =new JLabel("No message");
        northPanel.add(messageLabel);

        metricsLabel =new JLabel("No metrics");
        northPanel.add(metricsLabel);

        if(Objects.isNull(serverAddress)){
            ipAddressLabel.setText("Ip: Unknown");
            connectionLabel.setText("Not connected");
        }else{
            ipAddressLabel.setText(("Ip: "+serverAddress));
            connectionLabel.setText("Connected");
        }

        //Video panel
        centerPanel = new JPanel(new BorderLayout());
        canvas = new ImageCanvas();
        centerPanel.add( canvas, BorderLayout.CENTER );


        //UI final moves
        MenuInflater.inflateMenu(this);
        add(northPanel,BorderLayout.NORTH);
        add(centerPanel,BorderLayout.CENTER);
        setTitle("PhoneConnect");
        //setPreferredSize(new Dimension(300,300));
        GraphicsConfiguration gc = getGraphicsConfiguration();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        int height=Toolkit.getDefaultToolkit().getScreenSize().height - screenInsets.bottom;
        setPreferredSize(new Dimension(height/2,height));
        //setPreferredSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize().height/2,Toolkit.getDefaultToolkit().getScreenSize().height));
        pack();
        setupDragAndDropSupport(this, this.controller);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setFancyLookAndFeel() {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No look and feel");
        }
    }

    private final FileCreator fileCreator = new FileCreator();
    public void showPicture(Picture img) {
        canvas.showImage(img);
        long timestamp_afterCanvasShow = System.currentTimeMillis();
        img.addTimestamp("afterCanvasShow", timestamp_afterCanvasShow);
        if(LOG_SEGMENTS) System.out.println(img);
        if(SAVE_RESIZED_IMG){
            //BufferedImage image = ImageCanvas.getScaledBufferedImage(img.getImg(),canvas.getWidth(),canvas.getHeight());
            BufferedImage image = canvas.getCurrentImage().get();
            if(image == null) return;
            fileCreator.saveBufferedImage(image, img.getFilename(),img.getFolderName());
        }
    }


    public void showPicture(BufferedImage img){
        canvas.showImage(img);
    }

    public void showPictureFromFile(String path){
        System.out.println("showPictureFromFile called");
        try {
            BufferedImage img = ImageIO.read(new File(path));
            showPicture(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateMetrics(String currentMetric, String
            overallMetrics){
        metricsLabel.setText(currentMetric +" \t"+overallMetrics);
    }
}
