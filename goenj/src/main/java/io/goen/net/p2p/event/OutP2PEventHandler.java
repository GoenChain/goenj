package io.goen.net.p2p.event;

import io.goen.net.p2p.P2PMessage;
import io.goen.net.p2p.Sender;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutP2PEventHandler extends ChannelOutboundHandlerAdapter{


    private final static Logger logger = LoggerFactory.getLogger("net");

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(msg instanceof P2PMessage){
            P2PMessage message = (P2PMessage)msg;
            logger.trace("SEND dest:{}, P2PMessge:{},",message.getEvent().getClass().getSimpleName(),message.getInetSocketAddress());
            ctx.writeAndFlush(message);
        }
    }
}
