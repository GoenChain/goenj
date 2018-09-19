package io.goen.net.p2p.task;

import io.goen.net.p2p.Node;
import io.goen.net.p2p.NodesCenter;
import io.goen.net.p2p.P2PMessage;
import io.goen.net.p2p.Sender;
import io.goen.net.p2p.dht.KadConfig;
import io.goen.net.p2p.event.PingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.net.InetSocketAddress;
import java.util.List;

public class CheckTask implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger("net.p2p");
    private NodesCenter nodesCenter;
    private Sender sender;

    public CheckTask(Sender sender, NodesCenter nodesCenter) {
        this.nodesCenter = nodesCenter;
        this.sender = sender;
    }

    @Override
    public void run() {
        Node selfNode = nodesCenter.getSelfNode();
        logger.info("running running");
        List<Node> allLists = nodesCenter.getAllLists();
        for (Node node : allLists) {
            PingEvent pingEvent = new PingEvent();
            pingEvent.setExpires(System.currentTimeMillis() + KadConfig.EXPIRE);
            pingEvent.setFromIp(selfNode.getIp().getHostAddress());
            pingEvent.setFromPort(selfNode.getPort());
            pingEvent.setRandomHexString(Hex.toHexString("GOEN".getBytes()));
            pingEvent.setToIp(node.getIp().getHostAddress());
            pingEvent.setToPort(node.getPort());
            P2PMessage p2PMessage = new P2PMessage(new InetSocketAddress(node.getIp().getHostAddress(), node.getPort()),
                    pingEvent);
            sender.sendMessage(p2PMessage);
            logger.info("check node:{} ", node);

        }
    }
}
