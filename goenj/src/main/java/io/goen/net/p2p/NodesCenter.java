package io.goen.net.p2p;

import io.goen.core.GoenConfig;
import io.goen.net.p2p.dht.DistributedHashTable;
import io.goen.net.p2p.dht.KadConfig;
import io.goen.util.HashUtil;

import java.util.List;

public class NodesCenter {
    private DistributedHashTable dht;

    public NodesCenter() {
        this.dht = new DistributedHashTable(new Node(HashUtil.sha256(GoenConfig.system
                .publicKey()), GoenConfig.system.boundHost(), GoenConfig.system.p2pDiscoveryPort()));
    }

    public List<Node> getAllLists() {
        return this.dht.getAllNodes();
    }

    public List<Node> getLists(Node node) {
        return this.dht.getClosest(node,KadConfig.STALE);
    }

    public void nodeInsert(Node node) {
        this.dht.instertNode(node);
    }
}
