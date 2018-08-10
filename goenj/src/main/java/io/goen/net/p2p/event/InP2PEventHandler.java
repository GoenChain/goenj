package io.goen.net.p2p.event;

import com.google.common.collect.Maps;
import io.goen.core.GoenConfig;
import io.goen.net.p2p.Node;
import io.goen.net.p2p.P2PMessage;
import io.goen.net.p2p.Sender;
import io.goen.net.p2p.dht.DistributedHashTable;
import io.goen.util.HashUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;

public class InP2PEventHandler extends SimpleChannelInboundHandler<P2PMessage> {
	private final static Logger logger = LoggerFactory.getLogger("net.p2p");
	private Sender sender;
	public static Map<String, String> pingHexStringMap = Maps.newConcurrentMap();

	private DistributedHashTable dht = new DistributedHashTable(new Node(HashUtil.sha256(GoenConfig.system
			.publicKey()), GoenConfig.system.boundHost(), GoenConfig.system.p2pDiscoveryPort()));

	public InP2PEventHandler(Sender sender) {
		this.sender = sender;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, P2PMessage msg) throws Exception {
		logger.info("rec a msg:{}", msg.getEvent().getType());
		Event event = msg.getEvent();
		InetSocketAddress inetSocketAddress = msg.getInetSocketAddress();
		Node node = new Node(event.getNodeId(), inetSocketAddress.getAddress(), inetSocketAddress.getPort());
		actionEvent(node, msg.getEvent());
	}

	private void actionEvent(Node node, Event event) {
		switch (event.getType()[0]) {
		// PingEvent
		case 1:
			handlePingEvent(node, (PingEvent) event);
			break;
		// PongEvent
		case 2:
			handlePongEvent(node, (PongEvent) event);
			break;
		// FindEvent:
		case 3:
			handleFindEvent(node, (FindEvent) event);
			break;
		// NodesEvent:
		case 4:
			handleNodesEvent(node, (NodesEvent) event);
			break;
		default:
			throw new RuntimeException("not known type:" + event.getType()[0]);

		}
	}

	private void handlePingEvent(Node node, PingEvent pingEvent) {
		PongEvent pongEvent = new PongEvent(pingEvent.getRandomHexString());
		P2PMessage p2pMessage = new P2PMessage(new InetSocketAddress(node.getIp(), node.getPort()), pongEvent);
		sendEvent(p2pMessage);
	}

	private void handlePongEvent(Node node, PongEvent pongEvent) {
		// update the Node status
		dht.instertNode(node);
		System.out.println(dht.getAllNodes());
	}

	private void handleFindEvent(Node node, FindEvent findEvent) {

	}

	private void handleNodesEvent(Node node, NodesEvent nodesEvent) {

	}

	private void sendEvent(P2PMessage message) {
		sender.sendMessage(message);
	}

}
