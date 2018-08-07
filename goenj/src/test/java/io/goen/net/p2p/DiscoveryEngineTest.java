package io.goen.net.p2p;

import com.google.common.net.InetAddresses;
import io.goen.net.p2p.event.PingEvent;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static org.junit.Assert.*;

public class DiscoveryEngineTest {
    @Test
    public void testDiscoryEngine(){
        DiscoveryEngine deA = new DiscoveryEngine(InetAddresses.forString("127.0.0.1"), 20355);
        DiscoveryEngine deB = new DiscoveryEngine(InetAddresses.forString("127.0.0.1"), 20356);

        Thread threadA = new Thread(()->{deA.start();});
        threadA.start();
        Thread threadB = new Thread(()->{deB.start();});
        threadB.start();

        try {
            Thread.sleep(3000);
            PingEvent pingEvent = new PingEvent();
            pingEvent.setExpires(1533218340901L+100);
            pingEvent.setFromIp("127.0.0.1");
            pingEvent.setFromPort(20355);
            pingEvent.setRandomHexString("abcdef");
            pingEvent.setToIp("127.0.0.1");
            pingEvent.setToPort(20356);
            P2PMessage p2PMessage = new P2PMessage(InetSocketAddress.createUnresolved("127.0.0.1",20356),pingEvent);
            deA.getSender().sendMessage(p2PMessage);
            threadA.join();
            threadB.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}