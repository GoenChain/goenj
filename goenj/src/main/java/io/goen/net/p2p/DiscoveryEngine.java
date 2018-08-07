package io.goen.net.p2p;

import io.goen.net.p2p.event.EventCodec;
import io.goen.net.p2p.event.InP2PEventHandler;
import io.goen.net.p2p.event.OutP2PEventHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;

public class DiscoveryEngine implements Engine {
    private final static Logger logger = LoggerFactory.getLogger("net.p2p");
	private InetAddress ip;
	private int port;
	private List<Node> bootNodes;

	private Channel channel;

	private Bootstrap b;

	private Sender sender;

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
        logger.info("starting discovery engine. listening: ip {}, port:{}",ip,port);
		NioEventLoopGroup group = new NioEventLoopGroup(1);
		b = new Bootstrap();
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
            sender = new Sender(channel);
			channel.closeFuture().sync();
		} catch (InterruptedException e) {
		    logger.info(" discovery engine listening: ip {}, port:{} ,has error ",ip,port,e);
			e.printStackTrace();
		}
	}

    public Sender getSender() {
        return sender;
    }
}
