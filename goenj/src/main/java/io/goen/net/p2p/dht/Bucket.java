package io.goen.net.p2p.dht;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.goen.net.p2p.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

public class Bucket {
    private static final Logger logger = LoggerFactory.getLogger("net.p2p");
	private final int depth;
	private final TreeSet<NodeContract> nodeContactsSet;
	private final TreeSet<NodeContract> replacementCacheSet;

	public Bucket(int depth) {
		nodeContactsSet = Sets.newTreeSet();
		replacementCacheSet = Sets.newTreeSet();
		this.depth = depth;
	}

	public synchronized void insert(Node node) {
		this.insert(new NodeContract(node));
	}

	public synchronized void insert(NodeContract nodeContract) {
		if (nodeContactsSet.contains(nodeContract)) {
            logger.info("{} is in set", nodeContract.getNode());
			NodeContract tmp = removeFromContacts(nodeContract.getNode());
			tmp.refresh();
			this.nodeContactsSet.add(tmp);
		} else {
			if (nodeContactsSet.size() >= KadConfig.BUCKET_SIZE) {
				NodeContract stalestOne = null;
				for (NodeContract tmp : this.nodeContactsSet) {
					if (tmp.getStaleCount() >= KadConfig.STALE) {
						if (stalestOne == null) {
							stalestOne = tmp;
						} else if (tmp.getStaleCount() > stalestOne.getStaleCount()) {
							stalestOne = tmp;
						}
					}

				}

				if (stalestOne != null) {
					this.nodeContactsSet.remove(stalestOne);
					this.nodeContactsSet.add(nodeContract);
				} else {
					this.insertIntoReplacementCache(nodeContract);
				}
                logger.info("{} add to set with full", nodeContract.getNode());
			} else {
                logger.info("{} add to set not full", nodeContract.getNode());
				this.nodeContactsSet.add(nodeContract);
			}
		}
	}

	public boolean containsNode(Node node) {
		return this.nodeContactsSet.contains(new NodeContract(node));
	}

	public boolean containsNode(NodeContract nodeContract) {
		return this.nodeContactsSet.contains(nodeContract);
	}

	public boolean removeNode(Node node) {
		return this.removeNodeContact(new NodeContract(node));
	}

	public synchronized boolean removeNodeContact(NodeContract nodeContract) {
		if (!this.nodeContactsSet.contains(nodeContract)) {
			return false;
		}

		if (!this.replacementCacheSet.isEmpty()) {
			this.nodeContactsSet.remove(nodeContract);
			NodeContract replacement = this.replacementCacheSet.first();
			this.nodeContactsSet.add(replacement);
			this.replacementCacheSet.remove(replacement);
		} else {
			this.getNodeContractFromBucket(nodeContract.getNode()).incrementStaleCount();
		}

		return true;
	}

	private synchronized NodeContract removeFromContacts(Node node) {
		for (NodeContract nc : this.nodeContactsSet) {
			if (nc.getNode().equals(node)) {
                logger.info("{}  is check", nc.getNode());
				this.nodeContactsSet.remove(nc);
				return nc;
			}
		}

		throw new NoSuchElementException("The node " + node.toString() + " is not in this  nodeContactsSet");
	}

	private synchronized void insertIntoReplacementCache(NodeContract nodeContract) {
		if (this.replacementCacheSet.contains(nodeContract)) {

			NodeContract tmp = this.removeFromReplacementCache(nodeContract.getNode());
			tmp.updateTouchNow();
			this.replacementCacheSet.add(tmp);
		} else if (this.replacementCacheSet.size() > KadConfig.BUCKET_SIZE) {
			/* if our cache is filled, we remove the least recently seen contact */
			this.replacementCacheSet.remove(this.replacementCacheSet.last());
			this.replacementCacheSet.add(nodeContract);
		} else {
			this.replacementCacheSet.add(nodeContract);
		}
	}

	private synchronized NodeContract removeFromReplacementCache(Node node) {
		for (NodeContract nc : this.replacementCacheSet) {
			if (nc.getNode().equals(node)) {
				this.replacementCacheSet.remove(nc);
				return nc;
			}
		}

		/* We got here means this element does not exist */
		throw new NoSuchElementException("The node " + node.toString()
				+ " is not in this replacementCacheSet. ");
	}

	private synchronized NodeContract getNodeContractFromBucket(Node node) {
		for (NodeContract nc : this.nodeContactsSet) {
			if (nc.getNode().equals(node)) {
				return nc;
			}
		}

		throw new NoSuchElementException("The node " + node.toString() + " is not in this  nodeContactsSet");
	}

	public synchronized int countsOfNodes() {
		return this.nodeContactsSet.size();
	}

	public synchronized int getDepth() {
		return this.depth;
	}

	public synchronized List<NodeContract> getNode() {
		final List<NodeContract> ret = Lists.newArrayListWithCapacity(nodeContactsSet.size());

		if (this.nodeContactsSet.isEmpty()) {
			return ret;
		}

		ret.addAll(nodeContactsSet);

		return ret;
	}
}
