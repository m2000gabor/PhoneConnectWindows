package hu.elte.sbzbxr.view;

import javax.swing.*;
import java.awt.*;

public class WelcomeScreen extends JFrame {
    JPanel northPanel;
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

        connectionLabel =new JLabel("Not connected");
        northPanel.add(connectionLabel);

        messageLabel =new JLabel("");
        northPanel.add(messageLabel);

        //UI final moves
        add(northPanel,BorderLayout.NORTH);
        setTitle("PhoneConnect");
        setPreferredSize(new Dimension(300,300));
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
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

    public void setIpAddress(String ipAddress){
        ipAddressLabel.setText(ipAddress);
    }
}
