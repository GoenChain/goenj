package io.goen.net.p2p.event;

import com.google.common.base.Verify;
import io.goen.core.GoenConfig;
import io.goen.net.p2p.Node;
import io.goen.net.p2p.P2PMessage;
import io.goen.net.p2p.dht.DistributedHashTable;
import io.goen.util.HashUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class InP2PEventHandler extends SimpleChannelInboundHandler<P2PMessage> {
    private final static Logger logger = LoggerFactory.getLogger("net");
	private Channel channel;

	private DistributedHashTable dht = new DistributedHashTable(new Node(HashUtil.sha256(GoenConfig.system.publicKey()),GoenConfig.system.boundHost(),GoenConfig.system.p2pDiscoveryPort()));

	public InP2PEventHandler(Channel channel) {
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
            handldPingEvent(node, (PingEvent) event);
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

	private void handldPingEvent(Node node ,PingEvent pingEvent){
		PongEvent pongEvent = new PongEvent(pingEvent.getRandomHexString());
		P2PMessage p2pMessage = new P2PMessage(new InetSocketAddress(node.getIp(),node.getPort()),pingEvent);
		sendEvent(p2pMessage);
	}

    private void handldPongEvent(Node node ,PongEvent pongEvent){
        Verify.verify(false,"pongEvent is vertify error", pongEvent);
        //update the Node status
        dht.instertNode(node);
    }


	private void sendEvent(P2PMessage p2pMessage){
        channel.writeAndFlush(p2pMessage);
	}

}
