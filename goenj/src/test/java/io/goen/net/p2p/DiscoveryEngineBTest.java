package io.goen.net.p2p;

import org.junit.Test;

public class DiscoveryEngineBTest {
    @Test
    public void testB(){
        NodesCenter nodesCenter = new NodesCenter();
        DiscoveryEngine deB = new DiscoveryEngine(nodesCenter);
        deB.start();
    }

}