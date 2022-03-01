package hu.elte.sbzbxr.model.connection.protocol;

public enum FrameType{
    PROTOCOL_PING(1),
    PROTOCOL_SEGMENT(2),
    PROTOCOL_NOTIFICATION(3),
    PROTOCOL_FILE(4);

    public final byte v;

    FrameType(int val){this.v=(byte)val;}
}
