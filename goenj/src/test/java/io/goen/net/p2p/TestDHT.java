package io.goen.net.p2p;

import com.google.common.net.InetAddresses;
import io.goen.core.GoenConfig;
import io.goen.net.p2p.dht.DistributedHashTable;
import io.goen.util.HashUtil;
import org.junit.Test;

import java.net.InetSocketAddress;

public class TestDHT {

    @Test
    public void testDHT(){
        byte[] selfId = HashUtil.sha256(GoenConfig.system.publicKey());
        Node node = new  Node(selfId, InetAddresses.forString("127.0.0.1"),30222);
        DistributedHashTable distributedHashTable =  new DistributedHashTable(node);
        distributedHashTable.instertNode(node);
    }

}
