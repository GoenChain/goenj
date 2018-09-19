package io.goen.net.p2p;

import io.goen.net.Engine;
import io.goen.net.p2p.event.EventCodec;
import io.goen.net.p2p.event.InP2PEventHandler;
import io.goen.net.p2p.event.OutP2PEventHandler;
import io.goen.net.p2p.task.TaskExecutor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DiscoveryEngine implements Engine {
    private final static Logger logger = LoggerFactory.getLogger("net.p2p");
    private InetAddress ip;
    private int port;
    private List<Node> bootNodes;

    private NodesCenter nodesCenter;


    private Channel channel;

    private Sender sender;

    public DiscoveryEngine(NodesCenter nodesCenter) {
        this.ip = nodesCenter.getSelfNode().getIp();
        this.port = nodesCenter.getSelfNode().getPort();
        this.nodesCenter = nodesCenter;
    }

    public DiscoveryEngine(InetAddress ip, int port, List<Node> bootNodes, NodesCenter nodesCenter) {
        this.ip = ip;
        this.port = port;
        this.bootNodes = bootNodes;
        this.nodesCenter = nodesCenter;
    }

    /**
     * in --> EventCodec --> InP2PEventHandler
     * <p>
     * out <-- EventCodec <-- OutP2PEventHandler
     */
    @Override
    public void start() {

        logger.info("starting discovery engine. listening: :[{}:{}]", ip, port);

        NioEventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class).handler(
                new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    public void initChannel(NioDatagramChannel ch) throws Exception {
                        if (sender == null) {
                            sender = new Sender(ch);
                        }
                        ch.pipeline().addLast(new EventCodec(nodesCenter.getPriKey()));
                        InP2PEventHandler inEventHandler = new InP2PEventHandler(sender,nodesCenter);
                        ch.pipeline().addLast(inEventHandler);
                        OutP2PEventHandler outEventHandler = new OutP2PEventHandler();
                        ch.pipeline().addLast(outEventHandler);
                    }
                });

        try {
            channel = b.bind(port).sync().channel();
            sender = new Sender(channel);

            TaskExecutor taskExecutor = new TaskExecutor(sender, nodesCenter);
            taskExecutor.start();

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
