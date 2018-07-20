package io.goen.net.p2p.dht;

import io.goen.net.p2p.Node;

import java.math.BigInteger;
import java.util.Comparator;

public class IdComparator implements Comparator<Node> {
	private final byte[] nodeId;

	public IdComparator(byte[] nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public int compare(Node nodeA, Node nodeB) {
		BigInteger nodeIdA = new BigInteger(nodeA.getNodeId());
		BigInteger nodeIdB = new BigInteger(nodeB.getNodeId());
		BigInteger nodeId = new BigInteger(this.nodeId);

		BigInteger distanceA = nodeIdA.xor(nodeId).abs();
		BigInteger distanceB = nodeIdB.xor(nodeId).abs();

		return distanceA.compareTo(distanceB);
	}
}
