package io.goen.net.goen;

import io.goen.core.GoenConfig;
import io.goen.net.Engine;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoenEngine implements Engine {
    private final static Logger logger = LoggerFactory.getLogger("net.goen");

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    ChannelFuture channelFuture;

    private GoenConfig goenConfig;
    private int port;

    @Autowired
    public GoenEngine(GoenConfig goenConfig) {
        this.goenConfig = goenConfig;
    }

    @Override
    public void start() {

        port = goenConfig.boundPort();

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();

        b.group(bossGroup, workerGroup);
        b.channel(NioServerSocketChannel.class);

        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);

        b.handler(new LoggingHandler());

        logger.info("Goen Engine is starting, port: [{}] ", port);
        try {
            channelFuture = b.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {

        }
    }

    @Override
    public void stop() {
        if (channelFuture != null && channelFuture.channel().isOpen()) {
            try {
                logger.info("Goen Engine is closing");
                channelFuture.channel().close().sync();
                logger.info("PeerServer closed.");
            } catch (Exception e) {
                logger.warn("Problems closing server channel", e);
            }
        }
    }
}
