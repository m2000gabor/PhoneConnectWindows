package hu.elte.sbzbxr.phoneconnect.model.connection.udp;


import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.UdpSegmentFramePart;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpReader {
    private UdpReader(){}

    public static UdpSegmentFramePart readSegmentFramePart(DatagramSocket socket) throws IOException {
        byte[] partBuffer = new byte[70000];
        DatagramPacket packet = new DatagramPacket(partBuffer, partBuffer.length);
        socket.receive(packet);
        return UdpSegmentFramePart.deserialize(packet.getData());
    }
}
