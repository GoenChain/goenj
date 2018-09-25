package io.goen.net.p2p.event;

import com.google.common.collect.Maps;
import io.goen.net.p2p.Node;
import io.goen.net.p2p.NodesCenter;
import io.goen.net.p2p.P2PMessage;
import io.goen.net.p2p.Sender;
import io.goen.net.p2p.dht.KadConfig;
import io.goen.util.FastByteComparisons;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public class InP2PEventHandler extends SimpleChannelInboundHandler<P2PMessage> {
    private final static Logger logger = LoggerFactory.getLogger("net.p2p");
    private Sender sender;
    public static Map<String, String> pingHexStringMap = Maps.newConcurrentMap();
    private NodesCenter nodesCenter;

    public InP2PEventHandler(Sender sender, NodesCenter nodesCenter) {
        this.sender = sender;
        this.nodesCenter = nodesCenter;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, P2PMessage msg) throws Exception {
        logger.info("rec a msg:{}", msg.getEvent().getClass().getSimpleName());
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
                logger.debug("handle ping event");
                break;
            // PongEvent
            case 2:
                handlePongEvent(node, (PongEvent) event);
                logger.debug("handle pong event");
                break;
            // FindEvent:
            case 3:
                handleFindEvent(node, (FindEvent) event);
                logger.debug("handle find event");
                break;
            // NodesEvent:
            case 4:
                handleNodesEvent(node, (NodesEvent) event);
                logger.debug("handle nodes event");
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
        nodesCenter.nodeInsert(node);
    }

    private void handleFindEvent(Node node, FindEvent findEvent) {
        List<Node> closest = nodesCenter.getLists(node);
        NodesEvent nodesEvent = new NodesEvent();
        nodesEvent.setNodes(closest);
        nodesEvent.setExpires(System.currentTimeMillis() + KadConfig.EXPIRE);
        P2PMessage p2pMessage = new P2PMessage(new InetSocketAddress(node.getIp(), node.getPort()),
                nodesEvent);
        sendEvent(p2pMessage);
    }

    private void handleNodesEvent(Node node, NodesEvent nodesEvent) {
        List<Node> nodes = nodesEvent.getNodes();
        for (Node redNode : nodes) {
            if (FastByteComparisons.compareTo(redNode.getNodeId(), nodesCenter.getSelfNode().getNodeId()) == 0) {
                continue;
            }
            //contain not insert but update stale
            if (nodesCenter.containNode(redNode)) {
                logger.debug("contain not insert node{}", redNode);
                nodesCenter.decreamNodeStale(node);
                continue;
            }
            nodesCenter.nodeInsert(redNode);
        }
    }

    private void sendEvent(P2PMessage message) {
        sender.sendMessage(message);
    }

}
