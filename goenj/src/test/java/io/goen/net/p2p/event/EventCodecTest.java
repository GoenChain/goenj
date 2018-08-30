package io.goen.net.p2p.event;

import io.goen.core.GoenConfig;
import io.goen.net.p2p.P2PMessage;
import io.goen.util.FastByteComparisons;
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
    public void encodePing() throws Exception {

        //PingEvent
        PingEvent pingEvent = new PingEvent();
        pingEvent.setExpires(1533218340901L + 100);
        pingEvent.setFromIp("192.168.1.1");
        pingEvent.setFromPort(30245);
        pingEvent.setRandomHexString("abcdef");
        pingEvent.setToIp("192.168.1.2");
        pingEvent.setToPort(30245);

        P2PMessage p2pMessage = new P2PMessage(new InetSocketAddress(pingEvent.getToIp(), pingEvent.getToPort()), pingEvent);

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

        assertEquals(decodePingEvent.getToIp(), pingEvent.getToIp());
        assertEquals(decodePingEvent.getToPort(), pingEvent.getToPort());
        assertEquals(decodePingEvent.getFromIp(), pingEvent.getFromIp());
        assertEquals(decodePingEvent.getFromPort(), pingEvent.getFromPort());
        assertEquals(decodePingEvent.getRandomHexString(), pingEvent.getRandomHexString());

    }

    @Test
    public void decodePing() throws Exception {
        ByteBuf buf = Unpooled.buffer();
        String hexString = "f66a5e9adc47a8934767f1ca6f3da51f5511385b1877159f8e71b5052d7686100101525ec363cd90a84b26ca47bcb04dcb95c90bb88589d6ee9093bd909e9727b9d067ad466132032a5779794e78e9346be5205fca2f8b12ac0a4bd31cd1f57b8da900dd84c0a8010282762584c0a8010182762583abcdef8800000164faef4089";

        buf.writeBytes(Hex.decode(hexString));
        ByteBuf input = buf.duplicate();
        DatagramPacket packet = new DatagramPacket(input, new InetSocketAddress("192.168.1.1", 30245));
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec());
        channel.writeInbound(packet);
        channel.finish();
        P2PMessage p2PMessage = channel.readInbound();
        PingEvent pingEvent = (PingEvent) p2PMessage.getEvent();
        assertEquals("192.168.1.2", pingEvent.getToIp());
        assertEquals(30245, pingEvent.getToPort());
        assertEquals("192.168.1.1", pingEvent.getFromIp());
        assertEquals(30245, pingEvent.getFromPort());
        assertEquals("abcdef", pingEvent.getRandomHexString());
    }

    @Test
    public void encodePong() {
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec());
        //PoneEvent

        PongEvent pongEvent = new PongEvent();
        pongEvent.setExpires(1533218340901L + 200);
        pongEvent.setPingHexString("abcdef");
        P2PMessage p2pMessageForPong = new P2PMessage(new InetSocketAddress("192.168.1.1", 30245), pongEvent);

        assertTrue(channel.writeOutbound(p2pMessageForPong));
        assertTrue(channel.finish());

        DatagramPacket packetForPong = channel.readOutbound();

        ByteBuf bufForPong = packetForPong.content();
        byte[] testEncodeDataForPong = new byte[bufForPong.readableBytes()];
        bufForPong.readBytes(testEncodeDataForPong);

        PongEvent decodePongEvent = new PongEvent();
        decodePongEvent.parse(testEncodeDataForPong);

        assertEquals(decodePongEvent.getPingHexString(), pongEvent.getPingHexString());
        assertEquals(decodePongEvent.getExpires(), pongEvent.getExpires());
        //92c7524844ff4ed43eae004f4137c15ca70ed301c762bac7d6ade03a1207b9730102c842ca9db7b2101ce13d88e1e13c00ec45e761b315f04c2dd5e6780d1e1c9e62287b13c547071c8010d88f097d03b4866d419fcac87e877e93fcb512ba964dad00cd83abcdef8800000164faef40ed
    }

    @Test
    public void decodePong() throws Exception {
        ByteBuf buf = Unpooled.buffer();
        String hexString = "92c7524844ff4ed43eae004f4137c15ca70ed301c762bac7d6ade03a1207b9730102c842ca9db7b2101ce13d88e1e13c00ec45e761b315f04c2dd5e6780d1e1c9e62287b13c547071c8010d88f097d03b4866d419fcac87e877e93fcb512ba964dad00cd83abcdef8800000164faef40ed";

        buf.writeBytes(Hex.decode(hexString));
        ByteBuf input = buf.duplicate();
        DatagramPacket packet = new DatagramPacket(input, new InetSocketAddress("192.168.1.1", 30245));
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec());
        channel.writeInbound(packet);
        channel.finish();
        P2PMessage p2PMessage = channel.readInbound();
        PongEvent pongEvent = (PongEvent) p2PMessage.getEvent();
        assertEquals("abcdef", pongEvent.getPingHexString());
        assertEquals(1533218340901L + 200, pongEvent.getExpires());
    }

    @Test
    public void encodeFind() {
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec());

        FindEvent findEvent = new FindEvent();
        findEvent.setExpires(1533218340901L + 200);
        findEvent.setNearDistance(GoenConfig.system.localNodeId());
        P2PMessage p2pMessageForPong = new P2PMessage(new InetSocketAddress("192.168.1.1", 30245), findEvent);

        assertTrue(channel.writeOutbound(p2pMessageForPong));
        assertTrue(channel.finish());

        DatagramPacket packetForPong = channel.readOutbound();

        ByteBuf bufForFind = packetForPong.content();
        byte[] testEncodeDataForFind = new byte[bufForFind.readableBytes()];
        bufForFind.readBytes(testEncodeDataForFind);
        System.out.println(Hex.toHexString(testEncodeDataForFind));

        FindEvent decodeFindEvent = new FindEvent();
        decodeFindEvent.parse(testEncodeDataForFind);

        assertEquals(0,FastByteComparisons.compareTo(findEvent.getNearDistance(),decodeFindEvent.getNearDistance()));
        assertEquals(findEvent.getExpires(), decodeFindEvent.getExpires());
    }

    @Test
    public void decodeFind() throws Exception {
    //fa7640135762b84b065652c6ff70a7f2f78e6b678f4803314145f30d25f4436a01036dac1f660f86def0a7ab6f2a9f6b15c7c2bea9af662fd4ff450f7ce7a451dcb729000753b1cb7980bde840f04e2c8f93f32931b4e03879de4216171874710c8a00eaa0fd5c036c3919c9bf048b59e5edc9e7a940a6b455094d19ea1f96eb0bc152764b8800000164faef40ed
        ByteBuf buf = Unpooled.buffer();
        String hexString = "fa7640135762b84b065652c6ff70a7f2f78e6b678f4803314145f30d25f4436a01036dac1f660f86def0a7ab6f2a9f6b15c7c2bea9af662fd4ff450f7ce7a451dcb729000753b1cb7980bde840f04e2c8f93f32931b4e03879de4216171874710c8a00eaa0fd5c036c3919c9bf048b59e5edc9e7a940a6b455094d19ea1f96eb0bc152764b8800000164faef40ed";

        buf.writeBytes(Hex.decode(hexString));
        ByteBuf input = buf.duplicate();
        DatagramPacket packet = new DatagramPacket(input, new InetSocketAddress("192.168.1.1", 30245));
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec());
        channel.writeInbound(packet);
        channel.finish();
        P2PMessage p2PMessage = channel.readInbound();
        FindEvent findEvent = (FindEvent) p2PMessage.getEvent();
        assertEquals(0,FastByteComparisons.compareTo(GoenConfig.system.localNodeId(), findEvent.getNearDistance()));
        assertEquals(1533218340901L + 200, findEvent.getExpires());
    }

}