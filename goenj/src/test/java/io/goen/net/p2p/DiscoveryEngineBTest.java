package io.goen.net.p2p;

import com.google.common.net.InetAddresses;
import org.junit.Test;

public class DiscoveryEngineBTest {
    @Test
    public void testB(){
        NodesCenter nodesCenter = new NodesCenter();
        DiscoveryEngine deB = new DiscoveryEngine(InetAddresses.forString("127.0.0.1"), 20356,nodesCenter);
        deB.start();
    }

}