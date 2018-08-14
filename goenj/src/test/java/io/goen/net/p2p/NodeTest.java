package io.goen.net.p2p;

import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.*;

public class NodeTest {
    @Test
    public void getBytes() throws Exception {
        String gnodeURL = "gnode://ff@127.0.0.1:23333";
        Node node = new Node(gnodeURL);
        String newURL = node.getGnodeURL();
        Assert.assertEquals(gnodeURL,newURL);


        byte[] bytesFromNode = node.getBytes();
        Node nodeFromByte = new Node(bytesFromNode);
        Assert.assertEquals(Hex.toHexString(node.getNodeId()),Hex.toHexString(nodeFromByte.getNodeId()));
        Assert.assertEquals(node.getPort(),nodeFromByte.getPort());
        Assert.assertEquals(node.getIp().getHostAddress(),nodeFromByte.getIp().getHostAddress());
    }

}