package io.goen.net.p2p;

import io.goen.net.Engine;
import io.goen.net.p2p.event.EventCodec;
import io.goen.net.p2p.event.InP2PEventHandler;
import io.goen.net.p2p.event.OutP2PEventHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.netty.channel.ChannelOption.SO_BROADCAST;

public class DiscoveryEngine implements Engine {
	private final static Logger logger = LoggerFactory.getLogger("net.p2p");
	private InetAddress ip;
	private int port;
	private List<Node> bootNodes;

	private Channel channel;

	private Sender sender;

	public DiscoveryEngine(InetAddress ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public DiscoveryEngine(InetAddress ip, int port, List<Node> bootNodes) {
		this.ip = ip;
		this.port = port;
		this.bootNodes = bootNodes;
	}

	/**
	 * in --> EventCodec --> InP2PEventHandler
	 *
	 * out <-- EventCodec <-- OutP2PEventHandler
	 */
	@Override
	public void start() {

		logger.info("starting discovery engine. listening: :[{}:{}]", ip, port);
		InetSocketAddress inetSocketAddress = new InetSocketAddress(ip,port);
		NioEventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap b = new Bootstrap();
		b.group(group)
		.channel(NioDatagramChannel.class).handler(
				new ChannelInitializer<NioDatagramChannel>() {
					@Override
					public void initChannel(NioDatagramChannel ch) throws Exception {
						sender = new Sender(ch);
						ch.pipeline().addLast(new EventCodec());
						InP2PEventHandler inEventHandler = new InP2PEventHandler(sender);
						ch.pipeline().addLast(inEventHandler);
						OutP2PEventHandler outEventHandler = new OutP2PEventHandler(sender);
						ch.pipeline().addLast(outEventHandler);
					}
				});

		try {
			channel = b.bind(port).sync().channel();

			channel.closeFuture().sync();
		} catch (InterruptedException e) {
			logger.info(" discovery engine listening: :[{}:{}] ,has error {}", ip, port, e);
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		logger.info("discovery engine:[{}:{}]is closing", ip, port);
		if (channel != null) {
			try {
				channel.close().await(10, TimeUnit.SECONDS);
			} catch (Exception e) {
				logger.warn("discovery engine [{}:{}]closing error", ip, port, e);
			}
		}
	}

	public Sender getSender() {
		return sender;
	}
}
