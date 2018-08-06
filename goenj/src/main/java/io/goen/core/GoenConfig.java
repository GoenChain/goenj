package io.goen.core;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.goen.net.crypto.ECKey;

public class GoenConfig {

	private final static Logger logger = LoggerFactory.getLogger("config");

	private static final String DEFAULT_FILE = "goen.conf";

	private static final String SYSTEM_PRIVATEKEY = "system.privatekey";

	private static final String P2P_DISCOVERY_BOUND_IP = "p2p.discovery.bound.ip";
	private static final String P2P_DISCOVERY_PORT = "p2p.discovery.port";
	private static final String P2P_START = "p2p.start";
	private static final String P2P_BOOT_PEERS = "p2p.boot.peers";

	public GoenConfig() {
		this(new File(Thread.currentThread().getContextClassLoader().getResource(DEFAULT_FILE).getFile()));
	}

	public GoenConfig(File configFile) {
		this(ConfigFactory.parseFile(configFile));
		logger.info("loading file: {}", configFile.getAbsoluteFile().getAbsolutePath());
	}

	public GoenConfig(Config config) {
		logger.info("goen config loading start");
		this.config = config;
		printConfig();
		logger.info("goen config loading end");
	}

	private Config config;

	public static GoenConfig system = new GoenConfig();

	@PrintValue
	public boolean p2pStart() {
		return config.getBoolean(P2P_START);
	}

	@PrintValue
	public InetAddress boundHost() {
		InetAddress host = null;
		try {
			host = InetAddress.getByName(config.getString(P2P_DISCOVERY_BOUND_IP));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return host;
	}

	@PrintValue
	public int p2pDiscoveryPort() {
		return config.getInt(P2P_DISCOVERY_PORT);
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

	public void overrideParams(Config overrideOptions) {
		config = overrideOptions.withFallback(config);
	}

	public static void main(String[] args) {
		List<String> strings = GoenConfig.system.p2pDiscoveryPeers();
		for (String peer : strings) {
			System.out.println(peer);
		}
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
		logger.info("goen config:");
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
				throw new RuntimeException("Error the config parameter is error: " + method, e);
			}
		}
		logger.info("=========================");
	}

}
