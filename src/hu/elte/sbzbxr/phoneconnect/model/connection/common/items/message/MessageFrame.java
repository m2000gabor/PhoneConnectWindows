package hu.elte.sbzbxr.phoneconnect.model.connection.common.items.message;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.Deserializer;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.FrameType;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.NetworkFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.Serializer;

import java.io.IOException;
import java.io.InputStream;

/**
 * @implNote should be the same for both Windows and Android part
 * @version 4.1
 */
public class MessageFrame extends NetworkFrame {
    public final MessageType messageType;

    public MessageFrame(MessageType messageType) {
        super(FrameType.INTERNAL_MESSAGE);
        this.messageType = messageType;
    }

    @Override
    public Serializer serialize() {
        return super.serialize().addField(messageType.toString());
    }

    public static MessageFrame deserialize(InputStream inputStream) throws IOException {
        Deserializer deserializer = new Deserializer(inputStream);
        return new MessageFrame(MessageType.valueOf(deserializer.getString()));
    }
}
