package io.goen.net.p2p.dht;

import java.util.List;
import java.util.TreeSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.goen.net.p2p.Node;

public class DistributedHashTable {
	private Node selfNode;
	private transient Bucket[] buckets;

	public DistributedHashTable(Node selfNode) {
		this.selfNode = selfNode;

		this.initialize();
	}

	public final void initialize() {
		this.buckets = new Bucket[KadConfig.BINS];
		for (int i = 0; i < KadConfig.BINS; i++) {
			buckets[i] = new Bucket(i);
		}
	}

	public Node getNode() {
		return this.selfNode;
	}

	public synchronized Node instertNode(Node node) {
		this.buckets[this.getBucketId(node)].insert(node);
		return null;
	}

	public synchronized List<Node> getClosest(int num) {
		TreeSet<Node> sortedSet = Sets.newTreeSet(new IdComparator(this.selfNode.getNodeId()));
		sortedSet.addAll(this.getAllNodes());

		List<Node> closest = Lists.newArrayListWithCapacity(num);

		int count = 0;
		for (Node n : sortedSet) {
			closest.add(n);
			if (++count == num) {
				break;
			}
		}
		return closest;
	}

	public synchronized final List<Node> getAllNodes() {
		List<Node> nodes = Lists.newArrayList();

		for (Bucket bucket : this.buckets) {
			for (NodeContract nc : bucket.getNode()) {
				nodes.add(nc.getNode());
			}
		}

		return nodes;
	}

	public final int getBucketId(Node node) {
		int bucketId = KadKit.calcDistance(this.selfNode, node) - 1;
		return bucketId < 0 ? 0 : bucketId;
	}

	public synchronized void dropNode(Node n) {
		int bucketId = this.getBucketId(n);
		this.buckets[bucketId].removeNode(n);
	}

	public void dropNodes(List<Node> nodes) {
		if (nodes.isEmpty()) {
			return;
		}
		for (Node n : nodes) {
			this.dropNode(n);
		}
	}

}
