package hu.elte.sbzbxr.phoneconnect.model.persistence;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.BackupFileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.SegmentFrame;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Objects;

/**
 * Creates a file, write the pieces after each other
 */
public class FileCreator {
    private final FileSystemManager fileSystemManager;
    public String onGoingFileSaving = null; //filename
    public String onGoingSegmentSaving = null; // folder name
    public String onGoingBackupFilename = null; //filename
    public String onGoingBackupDirectory = null;// folder name
    public FileOutputStream fileTransferStream = null;
    public FileOutputStream backupStream = null;

    public FileCreator() {
        this.fileSystemManager = new FileSystemManager();
    }

    public void saveSegment(SegmentFrame frame){
        if(!Objects.equals(onGoingSegmentSaving, frame.folderName) || !Objects.equals(frame.folderName, onGoingSegmentSaving)){
            onGoingSegmentSaving = fileSystemManager.createSegmentDirectory(frame.folderName).getName();
        }
        try (FileOutputStream outputStream = fileSystemManager.createFile_SegmentSave(onGoingSegmentSaving, frame.filename)){
            outputStream.write(frame.getData());
            System.out.println("Saved segment: " + frame.filename);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveBufferedImage(BufferedImage toSave, String filename,String folderName){
        if(!Objects.equals(onGoingSegmentSaving, folderName) || !Objects.equals(folderName, onGoingSegmentSaving)){
            onGoingSegmentSaving = fileSystemManager.createSegmentDirectory(folderName).getName();
        }
        try (FileOutputStream outputStream = fileSystemManager.createFile_SegmentSave(onGoingSegmentSaving, filename)){
            ImageIO.write(toSave, "png", outputStream);
            System.out.println("Saved image: " + filename);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveFileFrame(FileFrame frame, Runnable notificationCallback) {
        try {
            if (onGoingFileSaving == null) {//nothing is in progress
                onGoingFileSaving = frame.filename;
                fileTransferStream = fileSystemManager.createFile_IncomingTransfer(onGoingFileSaving);
                System.out.println("Saving file: " + frame.filename + " started");
                fileTransferStream.write(frame.getData());
            } else if (onGoingFileSaving.equals(frame.filename)) {//this frame is a part of an ongoing transfer
                if (isLastPieceOfFile(frame)) {//end signal
                    onGoingFileSaving = null;
                    fileTransferStream.close();
                    fileTransferStream = null;
                    System.out.println("File fully arrived: " + frame.filename);
                    notificationCallback.run();
                } else {//append to file
                    fileTransferStream.write(frame.getData());
                }
            } else {//other file transfer is in progress
                //throw new InvalidParameterException("Another file transfer is in progress. (" + onGoingFileSaving + ")");
                System.err.println("Another file transfer was in progress. (" + onGoingFileSaving + ") It's stopped.");
                onGoingFileSaving = null;
                fileTransferStream.close();
                fileTransferStream = null;
                saveFileFrame(frame,notificationCallback);
            }
        } catch (IOException | InvalidParameterException e) {
            e.printStackTrace();
            connectionStopped();
        }
    }

    public void saveBackupFrame(BackupFileFrame frame) {
        try {
            if(onGoingBackupDirectory == null || !Objects.equals(frame.folderName, onGoingBackupDirectory)){
                onGoingBackupDirectory = fileSystemManager.createBackup(frame.folderName).getName();
            }

            if (onGoingBackupFilename == null) {//nothing is in progress
                onGoingBackupFilename = frame.filename;
                backupStream = fileSystemManager.createFile_Backup(onGoingBackupDirectory,onGoingBackupFilename);
                System.out.println("Saving file: " + frame.filename + " started");
                backupStream.write(frame.getData());
            } else if (onGoingBackupFilename.equals(frame.filename)) {//this frame is a part of an ongoing transfer
                if (isLastPieceOfFile(frame)) {//end signal
                    endCurrentFile();
                    System.out.println("File fully arrived: " + frame.filename);
                } else {//append to file
                    try {
                        backupStream.write(frame.getData());
                    }catch (IOException e){
                        onGoingBackupFilename=null;
                    }
                }
            } else {//other file transfer is in progress
                endCurrentFile();
                throw new InvalidParameterException("Another file transfer is in progress. Instead of " + onGoingBackupFilename + ", got "+frame.filename);
            }
        } catch (IOException | InvalidParameterException e) {
            e.printStackTrace();
        }
    }

    private void endCurrentFile() throws IOException {
        onGoingBackupFilename = null;
        if(backupStream!=null) backupStream.close();
        backupStream = null;
    }

    private boolean isLastPieceOfFile(FileFrame frame){
        return frame.getDataLength() == 0;
    }

    public void connectionStopped() {
        onGoingFileSaving = null;
        onGoingSegmentSaving = null;
        onGoingBackupFilename = null;
        onGoingBackupDirectory = null;
        try {
            if(fileTransferStream!=null) fileTransferStream.close();
            if(backupStream!=null) backupStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileSystemManager getFileManager() {
        return fileSystemManager;
    }
}