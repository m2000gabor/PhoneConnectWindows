package hu.elte.sbzbxr.view;

import javax.swing.*;
import java.awt.*;

public class WelcomeScreen extends JFrame {

    public WelcomeScreen(){
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No look and feel");
        }


        //window
        //setJMenuBar(menuBar);
        setLayout(new BorderLayout());
        JLabel timerText=new JLabel("Ip:"+ "unknown");
        add(timerText,BorderLayout.NORTH);
        setTitle("PhoneConnect");
        setPreferredSize(new Dimension(300,300));
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }
}
