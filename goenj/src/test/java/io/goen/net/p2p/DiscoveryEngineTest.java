package io.goen.net.p2p;

import io.goen.core.GoenConfig;
import io.goen.net.p2p.dht.KadConfig;
import io.goen.net.p2p.event.PingEvent;
import org.junit.Test;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URL;

public class DiscoveryEngineTest {
    @Test
    public void testA() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("client.conf");
        File configFile = new File(resource.getFile());
        NodesCenter nodesCenter = new NodesCenter(new GoenConfig(configFile));
        DiscoveryEngine deA = new DiscoveryEngine(nodesCenter);

        Thread threadA = new Thread(() -> {
            deA.start();
        });
        threadA.start();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PingEvent pingEvent = new PingEvent();
        pingEvent.setExpires(1533218340901L + KadConfig.EXPIRE);
        pingEvent.setFromIp("127.0.0.1");
        pingEvent.setFromPort(20337);
        pingEvent.setRandomHexString("abcdef");
        pingEvent.setToIp("127.0.0.1");
        pingEvent.setToPort(20338);
        P2PMessage p2PMessage = new P2PMessage(new InetSocketAddress("127.0.0.1", 20338),
                pingEvent);
        deA.getSender().sendMessage(p2PMessage);

        try {
            Thread.sleep(60000);
            deA.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}