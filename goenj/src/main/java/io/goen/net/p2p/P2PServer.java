package io.goen.net.p2p;

import io.goen.core.GoenConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class P2PServer {
    Logger logger = LoggerFactory.getLogger("p2p");

    NodesCenter nodesCenter = new NodesCenter();

    private int port;
    private InetAddress host;

    public P2PServer() {

        init(GoenConfig.system.p2pDiscoveryPeers());
    }

    public void init(List<String> peers) {
        logger.info("starting p2p Server");
        final List<Node> bootNodes = new ArrayList<>();

        for (String bootPeerURI : peers) {
            bootNodes.add(new Node(bootPeerURI));
        }
        DiscoveryEngine engine = new DiscoveryEngine(host, port, nodesCenter);
        engine.start();
    }

    public static void main(String[] args) {
        P2PServer p2PServer = new P2PServer();
    }


}
