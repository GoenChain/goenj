package io.goen.net.p2p;

import io.goen.net.p2p.event.Event;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class Sender {
	private Channel channel;
	private static final Logger logger = LoggerFactory.getLogger("discover");

	public Sender(Channel channel) {
		this.channel = channel;
	}

	public void sendMessage(P2PMessage message) {
		channel.writeAndFlush(message);
	}
	public void sendMessage(Event event,Node node) {
		P2PMessage p2pMessage = new P2PMessage(InetSocketAddress.createUnresolved(node.getIp().getHostAddress(),node.getPort()),event);
		channel.writeAndFlush(p2pMessage);
	}
}
