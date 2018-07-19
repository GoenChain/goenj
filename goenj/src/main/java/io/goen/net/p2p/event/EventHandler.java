package io.goen.net.p2p.event;

import io.goen.net.p2p.Node;
import io.goen.net.p2p.P2PMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;

public class EventHandler extends SimpleChannelInboundHandler<P2PMessage> {
	private Channel channel;

	public EventHandler(Channel channel) {
		this.channel = channel;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, P2PMessage msg) throws Exception {
		Event event = msg.getEvent();
		InetSocketAddress inetSocketAddress = msg.getInetSocketAddress();
		Node node = new Node(event.getType(), inetSocketAddress.getAddress(), inetSocketAddress.getPort());
	}

	private void actionEvent(Node node, Event event) {
		switch (event.getType()[0]) {
		// PingEvent
		case 1:
			break;
		// PongEvent
		case 2:
			break;
		// FindEvent:
		case 3:
			break;
		// NodesEvent:
		case 4:
			break;
		default:
			throw new RuntimeException("not known type:" + event.getType()[0]);

		}
	}

	public void handldPingEvent(Node node ,PingEvent pingEvent){
		PongEvent pongEvent = new PongEvent();
	}

}
