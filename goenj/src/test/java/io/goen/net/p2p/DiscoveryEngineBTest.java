package io.goen.net.p2p;

import io.goen.core.GoenConfig;
import org.junit.Test;

import java.io.File;
import java.net.URL;

public class DiscoveryEngineBTest {
    @Test
    public void testB(){
        URL resource = Thread.currentThread().getContextClassLoader().getResource("server.conf");
        File configFile = new File(resource.getFile());
        NodesCenter nodesCenter = new NodesCenter(new GoenConfig(configFile));
        DiscoveryEngine deB = new DiscoveryEngine(nodesCenter);
        deB.start();
    }

}