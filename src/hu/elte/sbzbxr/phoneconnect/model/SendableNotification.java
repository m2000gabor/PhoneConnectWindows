package hu.elte.sbzbxr.phoneconnect.model;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 1.2
 */
public class SendableNotification {
    private final String title;
    private final String text;
    private final String appName;

    public SendableNotification(CharSequence title, CharSequence text, CharSequence appName) {
        this(String.valueOf(title),String.valueOf(text),String.valueOf(appName));
    }

    public SendableNotification(String title, String text, String appName) {
        this.title = title;
        this.text = text;
        this.appName = appName;
    }

    public SendableNotification(byte[] data){
        String str=new String(data);
        String[] arr = str.split(";");
        title = String.valueOf(arr[0]);
        text = String.valueOf(arr[1]);
        appName = String.valueOf(arr[2]);
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getAppName() {
        return appName;
    }

    public byte[] getByteArray(){
        return (title+";"+text+";"+appName).getBytes();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return "Notification:\nApp name: "+getAppName()+"\nTitle: "+getTitle()+"\nText: "+getText()+"\n";
    }
}