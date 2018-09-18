package io.goen.core;

import org.junit.Test;

import java.util.List;


public class GoenConfigTest {

	@Test
	public void test() {
		List<String> strings = GoenConfig.getSystem().p2pDiscoveryPeers();
		for (String peer : strings) {
			System.out.println(peer);
		}
	}

}