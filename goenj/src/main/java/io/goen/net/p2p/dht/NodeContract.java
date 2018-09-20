package io.goen.net.p2p.dht;

import io.goen.net.p2p.Node;
import org.spongycastle.util.encoders.Hex;

public class NodeContract implements Comparable<NodeContract> {
    private Node node;
    private long lastTouch;
    private int staleCount;

    public NodeContract(Node node) {
        this.node = node;
        this.lastTouch = System.currentTimeMillis() / 1000L;
        this.staleCount = 0;
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
        return staleCount;
    }

    public void setStaleCount(int staleCount) {
        this.staleCount = staleCount;
    }

    public void updateTouchNow() {
        this.lastTouch = System.currentTimeMillis() / 1000L;
    }

    public void resetStaleCount() {
        this.staleCount = 0;
    }

    public void incrementStaleCount() {
        this.staleCount++;
    }

    public void refresh() {
        this.updateTouchNow();
        resetStaleCount();
    }

    @Override
    public int compareTo(NodeContract o) {
        if (this.getNode().equals(o.getNode())) {
            return 0;
        }

        return (Hex.toHexString(this.getNode().getNodeId())).compareTo(Hex.toHexString(o.node.getNodeId()));
    }
}
