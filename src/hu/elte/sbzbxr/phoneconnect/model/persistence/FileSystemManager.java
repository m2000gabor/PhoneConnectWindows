package hu.elte.sbzbxr.phoneconnect.model.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Create the directories, open the files, list files in directory
 */
public class FileSystemManager {
    private File rootDir;
    private File backupDirHome;
    private File segmentDirHome;
    private File fileTransferDirHome;

    public FileSystemManager(){
        init();
    }

    private void init() {
        rootDir = obtainDirectory(getRootPath());
        backupDirHome = obtainDirectory(getBackupPath(rootDir));
        segmentDirHome = obtainDirectory(getSegmentPath(rootDir));
        fileTransferDirHome = obtainDirectory(getFileTransferPath(rootDir));
    }

    private static File getRootPath(){
        //String directoryPath = getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6)
        return new File(System.getProperty("user.dir") + File.separator + "files");
    }

    private static File getBackupPath(File rootDir){
        return new File( rootDir.getPath() + File.separator + "backups" );
    }

    private static File getSegmentPath(File rootDir){
        return new File( rootDir.getPath() + File.separator + "screenSharing" );
    }

    private static File getFileTransferPath(File rootDir){
        return new File( rootDir.getPath() + File.separator + "arrivedFiles" );
    }

    private static File obtainDirectory(File dir) {
        if (!dir.exists()){
            if(dir.mkdir()){
                System.err.println("Directory created at: "+ dir.getAbsolutePath());
            }
        }
        return dir;
    }

    private static FileOutputStream openOutputStream(File toOpen) throws FileNotFoundException {
        return new FileOutputStream(toOpen);
    }


    public File getFileTransferDirectory() {
        return fileTransferDirHome;
    }

    public File getBackupDirectory() {
        return backupDirHome;
    }

    public File getSegmentDirectory() {
        return segmentDirHome;
    }

    public FileOutputStream createFile_IncomingTransfer(String filename) throws IOException {
        return openOutputStream(new File(getFileTransferDirectory() + File.separator + filename));
    }

    public File createSegmentDirectory(String directoryName) {
        return obtainDirectory(new File(getSegmentDirectory() + File.separator + directoryName));
    }

    public FileOutputStream createFile_SegmentSave(String directoryName, String filename) throws IOException {
        return openOutputStream(new File(getSegmentDirectory() + File.separator + directoryName + File.separator + filename));
    }

    public File createBackup(String directoryName) {
        return obtainDirectory(new File(getBackupDirectory() + File.separator + directoryName));
    }

    public FileOutputStream createFile_Backup(String directory, String filename) throws IOException {
        return openOutputStream(new File(getBackupDirectory() + File.separator + directory + File.separator + filename));
    }

    public ArrayList<String> getBackupFolderNames() {
        File[] fileArr = getBackupDirectory().listFiles();
        if(fileArr == null) fileArr= new File[0];
        return new ArrayList<>(Stream.of(fileArr).map(File::getName).toList());
    }

    public List<File> getFilesOfBackup(String dirName) {
        File[] fileArr = new File(getBackupDirectory().getPath() + File.separator + dirName ).listFiles();
        if(fileArr == null) fileArr= new File[0];
        return List.of(fileArr);
    }

    public File getRootDir() {
        return rootDir;
    }
}
