package io.goen.net.p2p;

import io.goen.core.GoenConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class P2PServer {
    Logger logger = LoggerFactory.getLogger("p2p");

    @Autowired
    NodesCenter nodesCenter;

    @Autowired
    GoenConfig config;

    @PostConstruct
    public void init() {
        init(config.p2pDiscoveryPeers());
    }

    public void init(List<String> peers) {
        logger.info("starting p2p Server");
        final List<Node> bootNodes = new ArrayList<>();

        for (String bootPeerURI : peers) {
            bootNodes.add(new Node(bootPeerURI));
        }
        DiscoveryEngine engine = new DiscoveryEngine(nodesCenter);
        engine.start();
    }

    public static void main(String[] args) {
        P2PServer p2PServer = new P2PServer();
    }


}
