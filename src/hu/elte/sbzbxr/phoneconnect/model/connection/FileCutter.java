package hu.elte.sbzbxr.phoneconnect.model.connection;

import hu.elte.sbzbxr.phoneconnect.model.connection.items.FileFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.items.FrameType;

import java.io.*;

//version: 1.2
public class FileCutter {
    private static final int FILE_FRAME_MAX_SIZE=32000;//in bytes
    private final InputStream inputStream;
    private final String path;
    private boolean isClosingPart=false;
    private boolean isEnd=false;
    private FileFrame current;

    public FileCutter(File file){
        InputStream inputStream1;
        this.path=file.getName();
        try  {
            inputStream1 = new FileInputStream(file);
        } catch (IOException e) {
            inputStream1 =null;
            e.printStackTrace();
        }
        inputStream = inputStream1;
        next();
    }

    public FileFrame current(){return current;}

    public void next(){
        if(isEnd) return;
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(FILE_FRAME_MAX_SIZE);
            int writtenBytes=0;
            int read = inputStream.read();
            if(read==-1){
                if(isClosingPart){
                    isEnd = true;
                }else{
                    current = getEndOfFileFrame();
                    isClosingPart=true;
                }
            }else{
                while(writtenBytes<FILE_FRAME_MAX_SIZE && read>=0){
                    byteArrayOutputStream.write(read);
                    writtenBytes++;
                    read = inputStream.read();
                }
                if(read>=0){byteArrayOutputStream.write(read);}
                current = new FileFrame(FrameType.FILE,path,byteArrayOutputStream.toByteArray());
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
        return new FileFrame(FrameType.FILE,path,new byte[0]);
    }
}

