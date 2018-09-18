package io.goen.net.p2p;

import com.google.common.net.InetAddresses;
import io.goen.core.GoenConfig;
import io.goen.net.p2p.dht.DistributedHashTable;
import io.goen.util.HashUtil;
import org.junit.Test;

public class TestDHT {

    @Test
    public void testDHT(){
        byte[] selfId = HashUtil.sha256(GoenConfig.getSystem().publicKey());
        Node node = new  Node(selfId, InetAddresses.forString("127.0.0.1"),30222);
        Node nodeA = new  Node(HashUtil.sha256("nodeA".getBytes()), InetAddresses.forString("127.0.0.5"),30222);
        Node nodeB = new  Node(HashUtil.sha256("nodeB".getBytes()), InetAddresses.forString("127.0.0.2"),30222);
        Node nodeC = new  Node(HashUtil.sha256("nodeC".getBytes()), InetAddresses.forString("127.0.0.3"),30222);
        Node nodeD = new  Node(HashUtil.sha256("nodeD".getBytes()), InetAddresses.forString("127.0.0.4"),30222);
        DistributedHashTable distributedHashTable =  new DistributedHashTable(node);
        distributedHashTable.instertNode(nodeA);
        distributedHashTable.instertNode(nodeB);
        distributedHashTable.instertNode(nodeC);
        distributedHashTable.instertNode(nodeD);

    }

}
