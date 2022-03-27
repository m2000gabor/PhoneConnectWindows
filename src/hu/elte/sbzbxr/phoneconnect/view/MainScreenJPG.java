package hu.elte.sbzbxr.phoneconnect.view;


import hu.elte.sbzbxr.phoneconnect.controller.Controller;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.Objects;

public class MainScreenJPG extends JFrame {
    private final Controller controller;
    JPanel northPanel;
    JPanel centerPanel;
    JLabel ipAddressLabel;
    JLabel connectionLabel;
    JLabel messageLabel;
    ImageCanvas canvas;


    public MainScreenJPG(SocketAddress serverAddress, Controller controller){
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
        WelcomeScreen.setupDragAndDropSupport(this, this.controller);
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

    public void showPicture(BufferedImage img){
        canvas.showImage(img);
        /*
        img=ImageCanvas.resizeImage(img,canvas.getWidth(),canvas.getHeight());
        Graphics graphics = canvas.getGraphics();
        if (graphics != null) {
            graphics.drawImage(img,0,0,null);
            graphics.dispose();
        }*/
    }

    public void showPictureFromFile(String path){
        System.out.println("showPicture called");
        try {
            BufferedImage img = ImageIO.read(new File(path));
            showPicture(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initVideoPlayer() {
        showPictureFromFile("C:\\Users\\Gabor\\egyetem\\5felev_20_21_osz\\szakdoga\\vidik\\jpgStream_sample\\PhoneC_24_Jan_2022_11_49_06__part24.jpg");//todo change it
    }
}
