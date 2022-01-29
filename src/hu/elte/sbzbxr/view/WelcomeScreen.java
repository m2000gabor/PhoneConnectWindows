package hu.elte.sbzbxr.view;

import hu.elte.sbzbxr.controller.Controller;

import javax.swing.*;
import java.awt.*;

public class WelcomeScreen extends JFrame {
    private Controller controller;
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

        connectionLabel =new JLabel();
        northPanel.add(connectionLabel);
        setConnectionLabel(false);

        messageLabel =new JLabel("");
        northPanel.add(messageLabel);

        //UI final moves
        add(northPanel,BorderLayout.NORTH);
        setTitle("PhoneConnect");
        setPreferredSize(new Dimension(300,300));
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        /*
        try {
            Controller.displayTray(null);
        } catch (AWTException e) {
            e.printStackTrace();
        }*/
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
        ipAddressLabel.setText(ipAddress);
    }

    public void setConnectionLabel(boolean b){
        if (b){
            connectionLabel.setText("Connected");
        }else{
            connectionLabel.setText("Not connected");
        }
    }
}
