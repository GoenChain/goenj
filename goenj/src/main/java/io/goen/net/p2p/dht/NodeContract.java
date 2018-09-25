package io.goen.net.p2p.dht;

import io.goen.net.p2p.Node;
import org.spongycastle.util.encoders.Hex;

import java.util.concurrent.atomic.AtomicInteger;

public class NodeContract implements Comparable<NodeContract> {
    private Node node;
    private long lastTouch;
    private AtomicInteger staleCount;
    private AtomicInteger checkCount;

    public NodeContract(Node node) {
        this.node = node;
        this.lastTouch = System.currentTimeMillis() / 1000L;
        this.staleCount = new AtomicInteger(0);
        this.checkCount = new AtomicInteger(0);
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public long getLastTouch() {
        return lastTouch;
    }

    public void setLastTouch(long lastTouch) {
        this.lastTouch = lastTouch;
    }

    public int getStaleCount() {
        return staleCount.get();
    }

    public int getCheckCount() {
        return checkCount.get();
    }

    public void setStaleCount(AtomicInteger staleCount) {
        this.staleCount = staleCount;
    }

    public void updateTouchNow() {
        this.lastTouch = System.currentTimeMillis() / 1000L;
    }

    public void resetCount() {
        this.staleCount.set(0);
        this.checkCount.set(0);
    }

    public void incrementStaleCount() {
        this.staleCount.incrementAndGet();
    }

    public int incrementCheckCount() {
        return this.checkCount.incrementAndGet();
    }

    public void decrementStaleCount() {
        this.staleCount.decrementAndGet();
    }

    public void refresh() {
        this.updateTouchNow();
        resetCount();
    }

    @Override
    public int compareTo(NodeContract o) {
        if (this.getNode().equals(o.getNode())) {
            return 0;
        }

        return (Hex.toHexString(this.getNode().getNodeId())).compareTo(Hex.toHexString(o.node.getNodeId()));
    }
}
