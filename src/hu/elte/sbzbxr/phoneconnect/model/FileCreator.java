package hu.elte.sbzbxr.phoneconnect.model;

import hu.elte.sbzbxr.phoneconnect.model.connection.MyNetworkProtocolFrame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Optional;

public class FileCreator {
    private final ServerMainModel serverMainModel;
    public String currentFileTransfer = null;
    public FileOutputStream fileOutputStream = null;

    public FileCreator(ServerMainModel serverMainModel) {
        this.serverMainModel = serverMainModel;
    }

    public void reactToIncomingFileTransfer(MyNetworkProtocolFrame frame) {
        try {
            if (currentFileTransfer == null) {//nothing is in progress
                currentFileTransfer = frame.getName();
                File directory = serverMainModel.obtainFileTransferDirectory();
                Optional<FileOutputStream> tmp = createFile(frame.getName(), directory);
                tmp.ifPresent(outputStream -> {
                    fileOutputStream = outputStream;
                    try {
                        fileOutputStream.write(frame.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else if (currentFileTransfer.equals(frame.getName())) {//this frame is a part of an ongoing transfer
                if (frame.getDataLength() == 0) {//end signal
                    currentFileTransfer = null;
                    fileOutputStream.close();
                    fileOutputStream = null;
                    System.out.println("File arrived: " + frame.getName());
                } else {//append to file
                    fileOutputStream.write(frame.getData());
                }
            } else {//other file transfer is in progress
                throw new InvalidParameterException("Another file transfer is in progress. (" + currentFileTransfer + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
}