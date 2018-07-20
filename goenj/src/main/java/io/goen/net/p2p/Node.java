package io.goen.net.p2p;

import com.google.common.net.InetAddresses;
import io.goen.net.p2p.common.P2PConstant;
import io.goen.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

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
	 * gnode url: gnode://pubkey@host:port
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
			this.nodeId = HashUtil.sha256(Hex.decode(nodeIdString));
			this.ip = InetAddresses.forString(gnodeURI.getHost());
			this.port = gnodeURI.getPort();

		} catch (URISyntaxException e) {
			logger.error("gnodeURL is not correct {}", gnodeURL);
		}
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
}
