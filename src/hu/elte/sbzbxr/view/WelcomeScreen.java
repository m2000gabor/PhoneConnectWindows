package hu.elte.sbzbxr.view;

import hu.elte.sbzbxr.controller.Controller;
import hu.elte.sbzbxr.model.QrGenerator;

import javax.swing.*;
import java.awt.*;

public class WelcomeScreen extends JFrame {
    private Controller controller;
    ImageCanvas canvas;
    JPanel northPanel;
    JPanel centerPanel;
    JLabel ipAddressLabel;
    JLabel connectionLabel;
    JLabel messageLabel;

    public WelcomeScreen(){
        setFancyLookAndFeel();

        //window
        setLayout(new BorderLayout());

        //UI elements
        northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.PAGE_AXIS));

        ipAddressLabel =new JLabel("Ip:"+ "unknown");
        northPanel.add(ipAddressLabel);

        connectionLabel =new JLabel();
        northPanel.add(connectionLabel);
        setConnectionLabel(false);

        messageLabel =new JLabel("");
        northPanel.add(messageLabel);

        //Center
        centerPanel = new JPanel(new BorderLayout());
        canvas = new ImageCanvas();
        centerPanel.add( canvas, BorderLayout.CENTER );

        //UI final moves
        add(northPanel,BorderLayout.NORTH);
        add(centerPanel,BorderLayout.CENTER);
        setTitle("PhoneConnect");
        setPreferredSize(new Dimension(300,300));
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

    }

    private void setupQrCode(String str){
        canvas.showImage(QrGenerator.getQr(str));
    }

    private void setFancyLookAndFeel() {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No look and feel");
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setIpAddress(String ipAddress){
        ipAddressLabel.setText("IP address and port: " + ipAddress);
        setupQrCode(ipAddress);
    }

    public void setConnectionLabel(boolean b){
        if (b){
            connectionLabel.setText("Connected");
        }else{
            connectionLabel.setText("Not connected");
        }
    }
}
