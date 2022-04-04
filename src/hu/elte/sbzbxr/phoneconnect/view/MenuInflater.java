package hu.elte.sbzbxr.phoneconnect.view;

import hu.elte.sbzbxr.phoneconnect.model.persistence.FileSystemManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MenuInflater {

    public static void inflateMenu(JFrame frame){
        JMenuBar menuBar = new JMenuBar();

        ////////////Go to ////////
        JMenu goToMenu = new JMenu("Go to");

        JMenuItem backupItem = new JMenuItem("Backup folder");
        goToMenu.add(backupItem);
        backupItem.addActionListener(e -> openFileExplorerAt(new FileSystemManager().getBackupDirectory().getPath()));

        JMenuItem fileTransfers = new JMenuItem("File transfers");
        goToMenu.add(fileTransfers);
        fileTransfers.addActionListener(e -> openFileExplorerAt(new FileSystemManager().getFileTransferDirectory().getPath()));


        menuBar.add(goToMenu);
        frame.setJMenuBar(menuBar);
    }

    private static void openFileExplorerAt(String path){
        File file = new File (path);
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(file);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Couldn't open a file explorer");
        }
    }
}
