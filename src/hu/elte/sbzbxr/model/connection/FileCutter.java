package hu.elte.sbzbxr.model.connection;

import hu.elte.sbzbxr.model.connection.protocol.FrameType;
import hu.elte.sbzbxr.model.connection.protocol.MyNetworkProtocolFrame;

import java.io.*;

//version: 1.1
public class FileCutter {
    private static final int FILE_FRAME_MAX_SIZE=32000;//in bytes
    private final InputStream inputStream;
    private final String path;
    private boolean isClosingPart=false;
    private boolean isEnd=false;
    private MyNetworkProtocolFrame current;

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

    public MyNetworkProtocolFrame current(){return current;}

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
                current = new MyNetworkProtocolFrame(FrameType.PROTOCOL_FILE,path,byteArrayOutputStream.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
            current=null;
        }
    }

    public boolean isEnd(){
        return isEnd;
    }

    private MyNetworkProtocolFrame getEndOfFileFrame(){
        return new MyNetworkProtocolFrame(FrameType.PROTOCOL_FILE,path,new byte[0]);
    }
}

