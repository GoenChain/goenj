package io.goen.net.p2p.task;

import io.goen.net.p2p.Node;
import io.goen.net.p2p.NodesCenter;
import io.goen.net.p2p.Sender;
import io.goen.net.p2p.event.FindEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FindTask implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger("net.p2p");
    private NodesCenter nodesCenter;
    private Sender sender;

    public FindTask(Sender sender, NodesCenter nodesCenter) {
        this.nodesCenter = nodesCenter;
        this.sender = sender;
    }

    @Override
    public void run() {
        Node selfNode = nodesCenter.getSelfNode();
        List<Node> allLists = nodesCenter.getAllLists();
        for (Node node : allLists) {
            FindEvent findEvent = new FindEvent();
            findEvent.setExpires(System.currentTimeMillis() + 2000);
            findEvent.setNearDistance(selfNode.getNodeId());
            sender.sendMessage(findEvent, node);
            logger.info("find event send to node:{} ", node);
        }
    }
}
