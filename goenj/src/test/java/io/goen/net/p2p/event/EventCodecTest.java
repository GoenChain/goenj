package io.goen.net.p2p.event;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import io.goen.core.GoenConfig;
import io.goen.net.p2p.Node;
import io.goen.net.p2p.P2PMessage;
import io.goen.util.FastByteComparisons;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec(GoenConfig.getSystem().systemKey()));
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
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec(GoenConfig.getSystem().systemKey()));
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
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec(GoenConfig.getSystem().systemKey()));
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
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec(GoenConfig.getSystem().systemKey()));
        channel.writeInbound(packet);
        channel.finish();
        P2PMessage p2PMessage = channel.readInbound();
        PongEvent pongEvent = (PongEvent) p2PMessage.getEvent();
        assertEquals("abcdef", pongEvent.getPingHexString());
        assertEquals(1533218340901L + 200, pongEvent.getExpires());
    }

    @Test
    public void encodeFind() {
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec(GoenConfig.getSystem().systemKey()));

        FindEvent findEvent = new FindEvent();
        findEvent.setExpires(1533218340901L + 200);
        findEvent.setNearDistance(GoenConfig.getSystem().localNodeId());
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
    public void decodeFind(){
    //fa7640135762b84b065652c6ff70a7f2f78e6b678f4803314145f30d25f4436a01036dac1f660f86def0a7ab6f2a9f6b15c7c2bea9af662fd4ff450f7ce7a451dcb729000753b1cb7980bde840f04e2c8f93f32931b4e03879de4216171874710c8a00eaa0fd5c036c3919c9bf048b59e5edc9e7a940a6b455094d19ea1f96eb0bc152764b8800000164faef40ed
        ByteBuf buf = Unpooled.buffer();
        String hexString = "fa7640135762b84b065652c6ff70a7f2f78e6b678f4803314145f30d25f4436a01036dac1f660f86def0a7ab6f2a9f6b15c7c2bea9af662fd4ff450f7ce7a451dcb729000753b1cb7980bde840f04e2c8f93f32931b4e03879de4216171874710c8a00eaa0fd5c036c3919c9bf048b59e5edc9e7a940a6b455094d19ea1f96eb0bc152764b8800000164faef40ed";

        buf.writeBytes(Hex.decode(hexString));
        ByteBuf input = buf.duplicate();
        DatagramPacket packet = new DatagramPacket(input, new InetSocketAddress("192.168.1.1", 30245));
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec(GoenConfig.getSystem().systemKey()));
        channel.writeInbound(packet);
        channel.finish();
        P2PMessage p2PMessage = channel.readInbound();
        FindEvent findEvent = (FindEvent) p2PMessage.getEvent();
        assertEquals(0, FastByteComparisons.compareTo(GoenConfig.getSystem().localNodeId(), findEvent.getNearDistance()));
        assertEquals(1533218340901L + 200, findEvent.getExpires());
    }


    @Test
    public void encodeNodes() {
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec(GoenConfig.getSystem().systemKey()));

        NodesEvent nodesEvent = new NodesEvent();
        nodesEvent.setExpires(1533218340901L + 200);
        List<Node> nodeList = Lists.newArrayList();
        Node nodeA = new Node("FFFF".getBytes(), InetAddresses.forString("192.168.1.1"),30241);
        Node nodeB = new Node("FFFE".getBytes(), InetAddresses.forString("192.168.1.2"),30241);
        nodeList.add(nodeA);
        nodeList.add(nodeB);
        nodesEvent.setNodes(nodeList);
        P2PMessage p2pMessageForNodes = new P2PMessage(new InetSocketAddress("192.168.1.1", 30245), nodesEvent);

        assertTrue(channel.writeOutbound(p2pMessageForNodes));
        assertTrue(channel.finish());

        DatagramPacket packetForNodes = channel.readOutbound();

        ByteBuf bufForNode = packetForNodes.content();
        byte[] testEncodeDataForNodes = new byte[bufForNode.readableBytes()];
        bufForNode.readBytes(testEncodeDataForNodes);
        System.out.println(Hex.toHexString(testEncodeDataForNodes));

        NodesEvent decodeNodesEvent = new NodesEvent();
        decodeNodesEvent.parse(testEncodeDataForNodes);

        assertEquals(nodeList.size(), decodeNodesEvent.getNodes().size());
        assertEquals(nodeA.getPort(), decodeNodesEvent.getNodes().get(0).getPort());
        assertEquals(nodeB.getPort(), decodeNodesEvent.getNodes().get(1).getPort());
        assertEquals(nodesEvent.getExpires(), decodeNodesEvent.getExpires());
    }

    @Test
    public void decodeNodes() {
        //4575e698286131e9b7698b8b927eddac5032d39e8d4aa6c35d58ddef67b31ec801049670b5b68608e65fdd4d24ddc7de80decea8e6a1b5a1cb1590005560de5ed18e1fc16d1cc18ae0571d7316774f6238cfe6344d21e49d558904e17a2fa6f2bcff00f851f846a2676e6f64653a2f2f3436343634363436403139322e3136382e312e313a3330323431a2676e6f64653a2f2f3436343634363435403139322e3136382e312e323a33303234318800000164faef40ed
        ByteBuf buf = Unpooled.buffer();
        String hexString = "4575e698286131e9b7698b8b927eddac5032d39e8d4aa6c35d58ddef67b31ec801049670b5b68608e65fdd4d24ddc7de80decea8e6a1b5a1cb1590005560de5ed18e1fc16d1cc18ae0571d7316774f6238cfe6344d21e49d558904e17a2fa6f2bcff00f851f846a2676e6f64653a2f2f3436343634363436403139322e3136382e312e313a3330323431a2676e6f64653a2f2f3436343634363435403139322e3136382e312e323a33303234318800000164faef40ed";

        buf.writeBytes(Hex.decode(hexString));
        ByteBuf input = buf.duplicate();
        DatagramPacket packet = new DatagramPacket(input, new InetSocketAddress("192.168.1.1", 30245));
        EmbeddedChannel channel = new EmbeddedChannel(new EventCodec(GoenConfig.getSystem().systemKey()));
        channel.writeInbound(packet);
        channel.finish();
        P2PMessage p2PMessage = channel.readInbound();
        NodesEvent nodesEvent = (NodesEvent) p2PMessage.getEvent();
        assertEquals(2, nodesEvent.getNodes().size());
        assertEquals(30241, nodesEvent.getNodes().get(0).getPort());
        assertEquals(30241, nodesEvent.getNodes().get(1).getPort());
        assertEquals(1533218340901L + 200, nodesEvent.getExpires());
    }

}