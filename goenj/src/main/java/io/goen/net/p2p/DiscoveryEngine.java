package io.goen.net.p2p;

import com.google.common.net.InetAddresses;
import io.goen.net.p2p.event.EventCodec;
import io.goen.net.p2p.event.InP2PEventHandler;
import io.goen.net.p2p.event.OutP2PEventHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetAddress;
import java.util.List;

public class DiscoveryEngine implements Engine {
	private InetAddress ip;
	private int port;
	private List<Node> bootNodes;

	private Channel channel;

	public DiscoveryEngine(InetAddress ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public DiscoveryEngine(InetAddress ip, int port, List<Node> bootNodes) {
		this.ip = ip;
		this.port = port;
		this.bootNodes =  bootNodes;
	}

    /**
     *  in --> EventCodec --> InP2PEventHandler
     *
     *  out <-- EventCodec <-- OutP2PEventHandler
     */
	@Override
	public void start() {
		NioEventLoopGroup group = new NioEventLoopGroup(1);
		Bootstrap b = new Bootstrap();
		b.group(group).channel(NioDatagramChannel.class).handler(
				new ChannelInitializer<NioDatagramChannel>() {
					@Override
					public void initChannel(NioDatagramChannel ch) throws Exception {
						ch.pipeline().addLast(new EventCodec());
						InP2PEventHandler inEventHandler = new InP2PEventHandler(ch);
						ch.pipeline().addLast(inEventHandler);
						OutP2PEventHandler outEventHandler = new  OutP2PEventHandler(ch);
						ch.pipeline().addLast(outEventHandler);
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
}
