package io.goen.net.p2p;

import io.goen.core.GoenConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class P2PServer {
    private final static Logger logger = LoggerFactory.getLogger("net.p2p");

    @Autowired
    NodesCenter nodesCenter;

    @Autowired
    GoenConfig config;

    @PostConstruct
    public void init() {
        init(config.p2pDiscoveryPeers());
    }

    public void init(List<String> peers) {
        if (config.p2pStart()) {
            logger.info("starting p2p Server");

            for (String bootPeerURI : peers) {
                nodesCenter.nodeInsert(new Node(bootPeerURI));
                logger.info("loading node:{}", bootPeerURI);
            }

            DiscoveryEngine engine = new DiscoveryEngine(nodesCenter);
            engine.start();
        } else {
            logger.info("p2p Server not starting");
        }
    }
}
