package hu.elte.sbzbxr.phoneconnect.view;

import hu.elte.sbzbxr.phoneconnect.controller.Controller;
import hu.elte.sbzbxr.phoneconnect.model.QrGenerator;

import javax.swing.*;
import java.awt.*;
import java.net.SocketAddress;

import static hu.elte.sbzbxr.phoneconnect.Main.setFancyLookAndFeel;
import static hu.elte.sbzbxr.phoneconnect.view.MenuInflater.inflateMenu;

public class Frame_NotConnected extends JFrame {
    private final Controller controller;
    ImageCanvas canvas;
    JPanel northPanel;
    JPanel centerPanel;
    JLabel ipAddressLabel;
    JLabel connectionLabel;

    public Frame_NotConnected(Controller controller,SocketAddress serverAddress){
        this.controller=controller;
        setFancyLookAndFeel(this);

        //window
        setLayout(new BorderLayout());

        //UI elements
        northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.PAGE_AXIS));

        String ipAddress = getIpAddress(serverAddress);
        ipAddressLabel = new JLabel("IP address and port: " +ipAddress);
        northPanel.add(ipAddressLabel);
        northPanel.add(new JSeparator());

        connectionLabel =new JLabel("<html>Not connected. Open the PhoneConnect app and scan the QR code or type in the IP address and port shown above.</html>");
        northPanel.add(connectionLabel);
        northPanel.add(new JSeparator());

        //Center
        centerPanel = new JPanel(new BorderLayout());
        canvas = new ImageCanvas();
        centerPanel.add( canvas, BorderLayout.CENTER );

        //UI final moves
        inflateMenu(this);
        add(northPanel,BorderLayout.NORTH);
        add(centerPanel,BorderLayout.CENTER);
        setTitle("PhoneConnect");
        setPreferredSize(new Dimension(300,300));
        pack();
        setupQrCode(ipAddress);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setupQrCode(String str){
        canvas.showImage(QrGenerator.getQr(str));
    }


    private static String getIpAddress(SocketAddress serverAddress){
        if(serverAddress==null){
            return "Ip: unknown";
        }else{
            return serverAddress.toString().replace("/","");
        }
    }
}
