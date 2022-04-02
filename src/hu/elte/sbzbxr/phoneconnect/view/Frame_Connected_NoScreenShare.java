package hu.elte.sbzbxr.phoneconnect.view;

import hu.elte.sbzbxr.phoneconnect.controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.net.SocketAddress;

public class Frame_Connected_NoScreenShare extends JFrame {
    private final Controller controller;
    JPanel northPanel;
    JPanel centerPanel;
    JLabel ipAddressLabel;
    JLabel connectionLabel;

    public Frame_Connected_NoScreenShare(Controller controller, SocketAddress serverAddress){
        this.controller=controller;
        setFancyLookAndFeel();

        //window
        setLayout(new BorderLayout());

        //UI elements
        northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.PAGE_AXIS));

        String ipAddress = getIpAddress(serverAddress);
        ipAddressLabel = new JLabel("IP address and port: " +ipAddress);
        northPanel.add(ipAddressLabel);
        northPanel.add(new JSeparator());

        connectionLabel = new JLabel("Connected");
        northPanel.add(connectionLabel);
        northPanel.add(new JSeparator());

        //Center
        centerPanel = new JPanel(new BorderLayout());
        JLabel functionsLabel = new JLabel("<html>Drag&Drop files here, to send them to your phone.<br>" +
                "You can toggle more functions from your phone.</html>",SwingConstants.CENTER);
        centerPanel.add( functionsLabel, BorderLayout.CENTER );

        //UI final moves
        add(northPanel,BorderLayout.NORTH);
        add(centerPanel,BorderLayout.CENTER);
        setTitle("PhoneConnect");
        setPreferredSize(new Dimension(300,300));
        pack();
        setupDragAndDropSupport(this,controller);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void setupDragAndDropSupport(JFrame frame,Controller controller){
        //Based on: https://stackoverflow.com/questions/811248/how-can-i-use-drag-and-drop-in-swing-to-get-file-path
        frame.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> droppedFiles = (java.util.List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    controller.sendFilesToPhone(droppedFiles);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void setFancyLookAndFeel() {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No look and feel");
        }
    }

    private static String getIpAddress(SocketAddress serverAddress){
        if(serverAddress==null){
            return "Ip: unknown";
        }else{
            return serverAddress.toString().replace("/","");
        }
    }
}
