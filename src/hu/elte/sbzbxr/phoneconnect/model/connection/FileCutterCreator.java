package hu.elte.sbzbxr.phoneconnect.model.connection;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.FileCutter;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileCutterCreator {
    public static FileCutter create(File file, FrameType fileType, String backupID, Long folderSize){
        InputStream inputStream1;
        try  {
            inputStream1 = new FileInputStream(file);
        } catch (IOException e) {
            inputStream1 =null;
            e.printStackTrace();
        }
        return new FileCutter(inputStream1,file.getName(), file.length(),fileType,backupID,folderSize);
    }
}
