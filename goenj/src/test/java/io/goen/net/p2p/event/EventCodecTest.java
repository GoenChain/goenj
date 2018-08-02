package io.goen.net.p2p.event;

import io.goen.net.p2p.P2PMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.net.InetSocketAddress;

import static org.junit.Assert.*;

public class EventCodecTest {
    @Test
    public void encode() throws Exception {


        PingEvent pingEvent = new PingEvent();
        pingEvent.setExpires(1533218340901L+100);
        pingEvent.setFromIp("192.168.1.1");
        pingEvent.setFromPort(30245);
        pingEvent.setRandomHexString("abcdef");
        pingEvent.setToIp("192.168.1.2");
        pingEvent.setToPort(30245);

        P2PMessage p2pMessage = new P2PMessage(InetSocketAddress.createUnresolved(pingEvent.getToIp(),pingEvent.getToPort()),pingEvent);

        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec());
        //output p2pMessge
        assertTrue(channel.writeOutbound(p2pMessage));
        assertTrue(channel.finish());

        DatagramPacket packet = channel.readOutbound();

        ByteBuf buf = packet.content();
        byte[] testEncodeData = new byte[buf.readableBytes()];
        buf.readBytes(testEncodeData);

        PingEvent decodePingEvent = new PingEvent();
        decodePingEvent.parse(testEncodeData);

        assertEquals(decodePingEvent.getToIp(),pingEvent.getToIp());
        assertEquals(decodePingEvent.getToPort(),pingEvent.getToPort());
        assertEquals(decodePingEvent.getFromIp(),pingEvent.getFromIp());
        assertEquals(decodePingEvent.getFromPort(),pingEvent.getFromPort());
        assertEquals(decodePingEvent.getRandomHexString(),pingEvent.getRandomHexString());

    }

    @Test
    public void decode() throws Exception {
        ByteBuf buf = Unpooled.buffer();
        String hexString = "f66a5e9adc47a8934767f1ca6f3da51f5511385b1877159f8e71b5052d7686100101525ec363cd90a84b26ca47bcb04dcb95c90bb88589d6ee9093bd909e9727b9d067ad466132032a5779794e78e9346be5205fca2f8b12ac0a4bd31cd1f57b8da900dd84c0a8010282762584c0a8010182762583abcdef8800000164faef4089";

        buf.writeBytes(Hex.decode(hexString));
        ByteBuf input = buf.duplicate();
        DatagramPacket packet = new DatagramPacket(input,InetSocketAddress.createUnresolved("192.168.1.1",30245));
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec());
        channel.writeInbound(packet);
        channel.finish();
        P2PMessage p2PMessage = channel.readInbound();
    }

    public static void main(String[] args) {
        System.out.print(System.currentTimeMillis());
    }
}