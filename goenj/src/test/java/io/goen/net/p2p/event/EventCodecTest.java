package io.goen.net.p2p.event;

import io.goen.net.p2p.P2PMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import org.junit.Test;

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

        assertTrue(channel.writeOutbound(p2pMessage));
        assertTrue(channel.finish());

        DatagramPacket packet = channel.readOutbound();

        ByteBuf buf = packet.content();
        byte[] testEncodeData = new byte[buf.readableBytes()];
        buf.readBytes(testEncodeData);
        PingEvent decodePingEvent = new PingEvent();
        decodePingEvent.parse(testEncodeData);
        assertEquals(decodePingEvent.getToIp(),pingEvent.getToIp());

    }

    @Test
    public void decode() throws Exception {
        ByteBuf buf = Unpooled.buffer();

    }

    public static void main(String[] args) {
        System.out.print(System.currentTimeMillis());
    }
}