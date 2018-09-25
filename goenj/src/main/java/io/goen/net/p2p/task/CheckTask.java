package io.goen.net.p2p.task;

import io.goen.net.p2p.Node;
import io.goen.net.p2p.NodesCenter;
import io.goen.net.p2p.P2PMessage;
import io.goen.net.p2p.Sender;
import io.goen.net.p2p.dht.KadConfig;
import io.goen.net.p2p.dht.NodeContract;
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
        List<NodeContract> allLists = nodesCenter.getAllNCLists();
        for (NodeContract nodeContract : allLists) {
            //check invalid
            logger.info("node:{},stale:{},check:{}", nodeContract.getNode(), nodeContract.getStaleCount(), nodeContract.getCheckCount());

            if (nodeContract.incrementCheckCount() > KadConfig.CHECK_CYCLE) {
                nodeContract.resetCount();
            }

            if (nodeContract.getStaleCount() > KadConfig.STALE || System.currentTimeMillis() / 1000 - nodeContract.getLastTouch() > KadConfig.EXPIRE) {
                nodesCenter.nodeDrop(nodeContract);
                logger.info("remove node{}", nodeContract.getNode());
                continue;
            }
            //action ping
            Node node = nodeContract.getNode();
            PingEvent pingEvent = new PingEvent();
            pingEvent.setExpires(System.currentTimeMillis() + KadConfig.EXPIRE);
            pingEvent.setFromIp(selfNode.getIp().getHostAddress());
            pingEvent.setFromPort(selfNode.getPort());
            pingEvent.setRandomHexString(Hex.toHexString("GOEN".getBytes()));
            pingEvent.setToIp(node.getIp().getHostAddress());
            pingEvent.setToPort(node.getPort());
            P2PMessage p2PMessage = new P2PMessage(new InetSocketAddress(node.getIp().getHostAddress(), node.getPort()),
                    pingEvent);
            nodeContract.incrementStaleCount();
            sender.sendMessage(p2PMessage);
            logger.info("check node:{} ", node);

        }
    }
}
