package io.goen.net.p2p.dht;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.goen.net.p2p.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TreeSet;

public class DistributedHashTable {
    private static final Logger logger = LoggerFactory.getLogger("net.p2p");
    private Node selfNode;
    private transient Bucket[] buckets;

    public DistributedHashTable(Node selfNode) {
        this.selfNode = selfNode;

        this.initialize();
    }

    public final void initialize() {
        this.buckets = new Bucket[KadConfig.BINS];
        for (int i = 0; i < KadConfig.BINS; i++) {
            buckets[i] = new Bucket(i);
        }
    }

    public Node getSelfNode() {
        return this.selfNode;
    }

    public synchronized void insertNode(Node node) {
        logger.debug("start insert {}", node);
        this.buckets[this.getBucketId(node)].insert(node);
        logger.debug("end insert {}", node);
    }

    public synchronized List<Node> getClosest(int num) {
        return this.getClosest(this.selfNode, num);
    }

    public synchronized List<Node> getClosest(Node node, int num) {
        TreeSet<Node> sortedSet = Sets.newTreeSet(new IdComparator(node.getNodeId()));
        sortedSet.addAll(this.getAllNodes());

        List<Node> closest = Lists.newArrayListWithCapacity(num);

        int count = 0;
        for (Node n : sortedSet) {
            closest.add(n);
            if (++count == num) {
                break;
            }
        }
        return closest;
    }

    public synchronized final List<Node> getAllNodes() {
        List<Node> nodes = Lists.newArrayList();

        for (Bucket bucket : this.buckets) {
            for (NodeContract nc : bucket.getNode()) {
                nodes.add(nc.getNode());
            }
        }

        return nodes;
    }

    public synchronized final List<NodeContract> getAllNodeContracts() {
        List<NodeContract> nodes = Lists.newArrayList();

        for (Bucket bucket : this.buckets) {
            for (NodeContract nc : bucket.getNode()) {
                nodes.add(nc);
            }
        }

        return nodes;
    }

    public final int getBucketId(Node node) {
        int bucketId = KadKit.calcDistance(this.selfNode, node) - 1;
        return bucketId < 0 ? 0 : bucketId;
    }

    public final int getBucketIdByNodeContract(NodeContract nc) {
        int bucketId = KadKit.calcDistance(this.selfNode, nc.getNode()) - 1;
        return bucketId < 0 ? 0 : bucketId;
    }

    public synchronized void dropNode(NodeContract nc) {
        int bucketId = this.getBucketIdByNodeContract(nc);
        this.buckets[bucketId].removeNodeContact(nc);
    }

    public synchronized boolean containNode(Node node) {
        int bucketId = this.getBucketId(node);
        return this.buckets[bucketId].containsNode(node);
    }

    public synchronized void decrement(Node node) {
        int bucketId = this.getBucketId(node);
        NodeContract nodeContract = this.buckets[bucketId].getNodeContract(node);
        nodeContract.decrementStaleCount();
    }

}
