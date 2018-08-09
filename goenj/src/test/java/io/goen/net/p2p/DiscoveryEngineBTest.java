package io.goen.net.p2p;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.net.InetAddresses;
import io.goen.net.p2p.event.PingEvent;

public class DiscoveryEngineBTest {
    @Test
    public void testB(){
        DiscoveryEngine deB = new DiscoveryEngine(InetAddresses.forString("127.0.0.1"), 20356);
        deB.start();
    }

}