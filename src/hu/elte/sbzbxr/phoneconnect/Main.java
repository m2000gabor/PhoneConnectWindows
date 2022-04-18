package hu.elte.sbzbxr.phoneconnect;

import hu.elte.sbzbxr.phoneconnect.controller.Controller;

import javax.swing.*;
import java.util.Objects;

public class Main {
    public static final boolean LOG_SEGMENTS=false;
    public static final boolean SAVE_RESIZED_IMG = false;

    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.init();
        controller.start();
    }

    public static void setFancyLookAndFeel(JFrame frame) {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No look and feel");
        }
        try{
            frame.setIconImage(new javax.swing.ImageIcon(Objects.requireNonNull(frame.getClass().getResource("/icon.png"))).getImage());
        }catch (NullPointerException e){
            System.err.println("Cannot set image to app icon");
        }
    }
}
