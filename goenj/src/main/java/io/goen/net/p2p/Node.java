package io.goen.net.p2p;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.google.common.base.MoreObjects;
import com.google.common.net.InetAddresses;

import io.goen.net.p2p.common.P2PConstant;
import io.goen.rlp.RLP;

public class Node {
	private Logger logger = LoggerFactory.getLogger("net");
	private byte[] nodeId;
	private InetAddress ip;
	private int port;

	public Node(byte[] nodeId, InetAddress ip, int port) {
		this.nodeId = nodeId;
		this.ip = ip;
		this.port = port;
	}

	/**
	 * gnode url: gnode://nodeId@host:port
	 * 
	 * @param gnodeURL
	 */
	public Node(String gnodeURL) {
		try {
			URI gnodeURI = new URI(gnodeURL);
			if (gnodeURI.getScheme() == null
					|| !P2PConstant.GOEN_SCHEME.equalsIgnoreCase(gnodeURI.getScheme())) {
				logger.error("gnodeURL is not correct {}", gnodeURL);
			}
			String nodeIdString = gnodeURI.getUserInfo();
			this.nodeId = Hex.decode(nodeIdString);
			this.ip = InetAddresses.forString(gnodeURI.getHost());
			this.port = gnodeURI.getPort();

		} catch (URISyntaxException e) {
			logger.error("gnodeURL is not correct {}", gnodeURL);
		}
	}

	public Node(byte[] gnodeRPL){
		this(new String(RLP.decode2OneItem(gnodeRPL, 0).getRLPData()));
	}

	public byte[] getBytes() {
		String gnodeURL = getGnodeURL();
		return RLP.encodeString(gnodeURL);
	}

	public String getGnodeURL() {
		StringBuilder sb = new StringBuilder("gnode://");
		sb.append(Hex.toHexString(nodeId)).append("@").append(ip.getHostAddress()).append(":").append(port);
		return sb.toString();
	}

	public byte[] getNodeId() {
		return nodeId;
	}

	public void setNodeId(byte[] nodeId) {
		this.nodeId = nodeId;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Node.class).add("nodeId", Hex.toHexString(nodeId)).add("ip", ip)
				.add("port", port).toString();
	}

}
