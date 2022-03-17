package hu.elte.sbzbxr.phoneconnect.model.persistence;

import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Objects;
import java.util.Optional;

public class FileCreator {
    private final FileManager fileManager;
    public String onGoingFileSaving = null; //filename
    public String onGoingSegmentSaving = null; // folder name
    public FileOutputStream fileOutputStream = null;

    public FileCreator() {
        this.fileManager = new FileManager();
    }

    public void saveSegment(FileFrame frame, String directoryName){
        if(!Objects.equals(onGoingSegmentSaving, directoryName)){onGoingSegmentSaving = fileManager.createSegmentDirectory(directoryName).getName();}
        try (FileOutputStream outputStream = fileManager.createFile_SegmentSave(onGoingSegmentSaving, frame.name)){
            outputStream.write(frame.getData());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void saveFileFrame(FileFrame frame) {
        try {
            if (onGoingFileSaving == null) {//nothing is in progress
                onGoingFileSaving = frame.name;
                fileOutputStream = fileManager.createFile_IncomingTransfer(onGoingFileSaving);
                fileOutputStream.write(frame.getData());
            } else if (onGoingFileSaving.equals(frame.name)) {//this frame is a part of an ongoing transfer
                if (isLastPieceOfFile(frame)) {//end signal
                    onGoingFileSaving = null;
                    fileOutputStream.close();
                    fileOutputStream = null;
                    System.out.println("File arrived: " + frame.name);
                } else {//append to file
                    fileOutputStream.write(frame.getData());
                }
            } else {//other file transfer is in progress
                throw new InvalidParameterException("Another file transfer is in progress. (" + onGoingFileSaving + ")");
            }
        } catch (IOException | InvalidParameterException e) {
            e.printStackTrace();
        }

    }

    private boolean isLastPieceOfFile(FileFrame frame){
        return frame.getDataLength() == 0;
    }

    private Optional<FileOutputStream> createFile(String name , File directory){
        File outputFile = new File(directory.getAbsolutePath()+"\\"+name);
        FileOutputStream outputStream=null;
        try {
            outputStream = new FileOutputStream(outputFile);
            System.out.println("FileOutputStream created: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to create the file");
        }
        return Optional.ofNullable(outputStream);
    }

    public void connectionStopped() {
        try {
            if(fileOutputStream!=null) fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}