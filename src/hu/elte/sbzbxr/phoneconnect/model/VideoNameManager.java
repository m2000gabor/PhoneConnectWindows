package hu.elte.sbzbxr.phoneconnect.model;

public class VideoNameManager{
    private final String folderPath;
    private final String firstFileName;

    public VideoNameManager(String folderPath, String firstFileName) {
        this.folderPath = folderPath;
        this.firstFileName = firstFileName;
    }


    public static int getPartNumber(String videoname){
        String c = videoname.split("part")[1].substring(0,1);
        return Integer.parseInt(c);
    }

    public String getVideoName(int partNum){
        int i = getPartNumber(firstFileName);
        return firstFileName.replace("part"+i,"part"+(partNum));
    }

    public String getVideoPath(int partNum){
        return (folderPath+"\\"+getVideoName(partNum)).replace("/","\\");
    }
}
