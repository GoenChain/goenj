package io.goen.net.p2p;

import io.goen.core.GoenConfig;
import io.goen.crypto.ECKey;
import io.goen.net.p2p.dht.DistributedHashTable;
import io.goen.net.p2p.dht.KadConfig;
import io.goen.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class NodesCenter {
    private DistributedHashTable dht;

    @Autowired
    private GoenConfig config;

    private Node selfNode;

    private ECKey priKey;

    public NodesCenter() {
    }

    public NodesCenter(GoenConfig config) {
        this.config = config;
        init();
    }

    @PostConstruct
    public void init() {
        this.selfNode = new Node(HashUtil.sha256(config
                .publicKey()), config.boundHost(), config.p2pDiscoveryPort());
        this.dht = new DistributedHashTable(selfNode);
        this.priKey = config.systemKey();
    }

    public List<Node> getAllLists() {
        return this.dht.getAllNodes();
    }

    public List<Node> getLists(Node node) {
        return this.dht.getClosest(node, KadConfig.STALE);
    }

    public void nodeInsert(Node node) {
        this.dht.instertNode(node);
    }

    public Node getSelfNode() {
        return selfNode;
    }

    public boolean findAble() {
        return config.p2pFindStart();
    }

    public boolean checkAble() {
        return config.p2pCheckStart();
    }

    public ECKey getPriKey() {
        return priKey;
    }
}
