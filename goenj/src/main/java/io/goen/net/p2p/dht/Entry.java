package io.goen.net.p2p.dht;
import io.goen.net.p2p.Node;

public class Entry {
    private Node node;
    private byte[] selfId;
    private byte[] nodeId;
    private int distance;

    public void Entry(byte[] selfId, Node node){
        this.node = node;
        this.selfId =  selfId;
        this.nodeId = node.getNodeId();
    }


}
