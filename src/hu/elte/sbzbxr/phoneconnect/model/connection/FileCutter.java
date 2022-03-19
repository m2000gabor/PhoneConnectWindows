package hu.elte.sbzbxr.phoneconnect.model.connection;

import hu.elte.sbzbxr.phoneconnect.model.connection.items.BackupFileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.SegmentFrame;

import java.io.*;

//version: 1.4
public class FileCutter {
    private static final int FILE_FRAME_MAX_SIZE=32000;//in bytes
    private final InputStream inputStream;
    private boolean hadClosingPart=false;
    private boolean isEnd=false;
    private FileFrame current;
    private final FrameType fileType;
    private final String filename;
    private final String backupID;

    public FileCutter(File file,FrameType fileType, String backupID){
        this.fileType = fileType;
        InputStream inputStream1;
        this.filename=file.getName();
        this.backupID=backupID;
        try  {
            inputStream1 = new FileInputStream(file);
        } catch (IOException e) {
            inputStream1 =null;
            e.printStackTrace();
        }
        inputStream = inputStream1;
        next();
    }

    public FileFrame current(){
        return current;
    }

    public void next(){
        if(isEnd) return;
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(FILE_FRAME_MAX_SIZE);
            int writtenBytes=0;
            int read = inputStream.read();
            if(read==-1){
                if(hadClosingPart){
                    isEnd = true;
                    inputStream.close();
                }else{
                    current = getEndOfFileFrame();
                    hadClosingPart =true;
                }
            }else{
                while(writtenBytes<FILE_FRAME_MAX_SIZE && read>=0){
                    byteArrayOutputStream.write(read);
                    writtenBytes++;
                    read = inputStream.read();
                }
                if(read>=0){byteArrayOutputStream.write(read);}
                switch (fileType) {
                    case BACKUP_FILE, RESTORE_FILE -> current = new BackupFileFrame(fileType, filename, byteArrayOutputStream.toByteArray(), backupID);
                    case FILE -> current = new FileFrame(fileType, filename, byteArrayOutputStream.toByteArray());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            current=null;
        }
    }

    public boolean isEnd(){
        return isEnd;
    }

    private FileFrame getEndOfFileFrame(){
        return switch (fileType) {
            default -> new FileFrame(fileType, filename, new byte[0]);
            case BACKUP_FILE -> new BackupFileFrame(fileType, filename, new byte[0], backupID);
            case SEGMENT -> new SegmentFrame(filename, new byte[0], backupID);
        };
    }
}

