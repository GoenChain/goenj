package io.goen.net.p2p;

import io.goen.net.p2p.event.Event;
import io.goen.net.p2p.event.InP2PEventHandler;
import io.goen.net.p2p.event.PingEvent;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.net.InetSocketAddress;

public class Sender {
	private final static Logger logger = LoggerFactory.getLogger("net.p2p");
	private Channel channel;


	public Sender(Channel channel) {
		this.channel = channel;
	}

	public void sendMessage(P2PMessage message) {
		logger.info("sed a msg:{}", message.getEvent().getClass().getSimpleName());
		if(message.getEvent().getType() == new byte[]{1}){
			InP2PEventHandler.pingHexStringMap.put(Hex.toHexString(message.getEvent().getNodeId()), ((PingEvent)message.getEvent()).getRandomHexString());
		}
		channel.writeAndFlush(message);
	}
	public void sendMessage(Event event,Node node) {
		P2PMessage p2pMessage = new P2PMessage(new InetSocketAddress("127.0.0.1", node.getPort()), event);
		sendMessage(p2pMessage);
	}
}
