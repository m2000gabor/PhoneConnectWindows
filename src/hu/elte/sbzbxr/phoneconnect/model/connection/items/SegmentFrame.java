package hu.elte.sbzbxr.phoneconnect.model.connection.items;

import java.io.IOException;
import java.io.InputStream;

public class SegmentFrame extends FileFrame{
    public final String folderName;

    public SegmentFrame(String filename, byte[] data, String folderName) {
        super(FrameType.SEGMENT, filename, data);
        if(folderName==null) {folderName="";}
        this.folderName = folderName;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(folderName);
    }

    public static SegmentFrame deserialize(FrameType type, InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        String filename = deserializer.getString();
        byte[] array=deserializer.getByteArray();
        String folderName= deserializer.getString();
        return new SegmentFrame(filename,array,folderName);
    }
}
