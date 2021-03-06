package io.goen.core;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.goen.crypto.ECKey;
import io.goen.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class GoenConfig {

	private final static Logger logger = LoggerFactory.getLogger("component");

	private static final String DEFAULT_FILE = "goen.conf";

	private static final String SYSTEM_PRIVATEKEY = "system.privatekey";

	private static final String NET_BOUND_IP = "net.bound.ip";
	private static final String NET_PORT = "net.port";
	private static final String P2P_START = "net.p2p.start";
	private static final String P2P_FIND_START = "net.p2p.find.start";
	private static final String P2P_CHECK_START = "net.p2p.check.start";
	private static final String P2P_BOOT_PEERS = "net.p2p.boot.peers";

	private Config config;

	private static GoenConfig system = new GoenConfig();

	public static GoenConfig getSystem() {
		return system;
	}

	public GoenConfig() {
		this(new File(Thread.currentThread().getContextClassLoader().getResource(DEFAULT_FILE).getFile()));
	}

	public GoenConfig(File configFile) {
		this(ConfigFactory.parseFile(configFile));
		logger.info("loading file: {}", configFile.getAbsoluteFile().getAbsolutePath());
	}

	public GoenConfig(Config config) {
		logger.info("goen component loading start");
		this.config = config;
		printConfig();
		logger.info("goen component loading end");
	}


	public void overrideParams(Config overrideOptions) {
		config = overrideOptions.withFallback(config);
	}

	@PrintValue
	public boolean p2pStart() {
		return config.getBoolean(P2P_START);
	}

	@PrintValue
	public boolean p2pFindStart() {
		return config.getBoolean(P2P_FIND_START);
	}

	@PrintValue
	public boolean p2pCheckStart() {
		return config.getBoolean(P2P_CHECK_START);
	}

	@PrintValue
	public InetAddress boundHost() {
		InetAddress host = null;
		try {
			host = InetAddress.getByName(config.getString(NET_BOUND_IP));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return host;
	}

	@PrintValue
	public int boundPort() {
		return config.getInt(NET_PORT);
	}

	@PrintValue
	public List<String> p2pDiscoveryPeers() {
		return config.getStringList(P2P_BOOT_PEERS);
	}

	@PrintValue
	public ECKey systemKey() {
		String privateKey = config.getString(SYSTEM_PRIVATEKEY);
		return ECKey.fromPrivate(Hex.decode(privateKey));
	}

	@PrintValue(PrintType.HEX)
	public byte[] publicKey() {
		return systemKey().getPubKey();
	}

	@PrintValue(PrintType.HEX)
	public byte[] localNodeId() {
		return HashUtil.sha256(systemKey().getPubKey());
	}



	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface PrintValue {
		public PrintType value() default PrintType.STRING;
	}

	enum PrintType {
		STRING, HEX
	}

	private void printConfig() {
		logger.info("goen component:");
		logger.info("=========================");
		for (Method method : getClass().getMethods()) {
			try {
				if (method.isAnnotationPresent(PrintValue.class)) {
					Object result = method.invoke(this);
					switch (method.getAnnotation(PrintValue.class).value()) {
					case HEX:
						logger.info("{}:{}", method.getName(), Hex.toHexString((byte[]) result));
						break;
					case STRING:
						logger.info("{}:{}", method.getName(), result);
						break;
					default:
						logger.info("{}:{}", method.getName(), result);
						break;
					}

				}
			} catch (Exception e) {
				throw new RuntimeException("Error the component parameter is error: " + method, e);
			}
		}
		logger.info("=========================");
	}

}
