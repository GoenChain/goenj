package io.goen.net.p2p;

import com.google.common.net.InetAddresses;
import io.goen.net.p2p.event.EventCodec;
import io.goen.net.p2p.event.EventHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetAddress;

public class DiscoveryEngine implements Engine {
	private InetAddress ip;
	private int port;

	private Channel channel;

	public DiscoveryEngine(InetAddress ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	@Override
	public void start() {
		NioEventLoopGroup group = new NioEventLoopGroup(1);
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioDatagramChannel.class).handler(
				new ChannelInitializer<NioDatagramChannel>() {
					@Override
					public void initChannel(NioDatagramChannel ch) throws Exception {
						ch.pipeline().addLast(new EventCodec());
						EventHandler messageHandler = new EventHandler(ch);
						ch.pipeline().addLast(messageHandler);
					}
				});

		try {
			channel = b.bind(ip, port).sync().channel();
			channel.closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Channel getChannel() {
		return channel;
	}

	public static void main(String[] args) {
		DiscoveryEngine discoveryEngine = new DiscoveryEngine(InetAddresses.forString("127.0.0.1"),9321);
		discoveryEngine.start();
	}
}
