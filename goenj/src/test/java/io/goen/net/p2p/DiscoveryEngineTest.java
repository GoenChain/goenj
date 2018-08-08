package io.goen.net.p2p;

import java.net.InetSocketAddress;

import org.junit.Test;

import com.google.common.net.InetAddresses;
import io.goen.net.p2p.event.PingEvent;

public class DiscoveryEngineTest {
	@Test
	public void testDiscoryEngine() {
		DiscoveryEngine deA = new DiscoveryEngine(InetAddresses.forString("127.0.0.1"), 20355);

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
		pingEvent.setExpires(1533218340901L + 100);
		pingEvent.setFromIp("127.0.0.1");
		pingEvent.setFromPort(20355);
		pingEvent.setRandomHexString("abcdef");
		pingEvent.setToIp("127.0.0.1");
		pingEvent.setToPort(20356);
		P2PMessage p2PMessage = new P2PMessage(InetSocketAddress.createUnresolved("127.0.0.1", 20356),
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