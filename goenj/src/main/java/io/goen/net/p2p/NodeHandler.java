package io.goen.net.p2p;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeHandler {
    private Channel channel;
    private static final Logger logger = LoggerFactory.getLogger("discover");

    public void sendMessage(P2PMessage message){
        DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(message.getEvent().getData()), message.getInetSocketAddress());
        channel.write(packet);
        channel.flush();
    }
}
