package io.goen.net.p2p.event;

import java.util.List;

import io.goen.net.p2p.P2PMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [mdc(32byte)][version(1byte)][type(1byte)][signature(65byte)][data(undefined)
 * ]
 */
public class EventCodec extends MessageToMessageCodec<DatagramPacket, P2PMessage> {
	private final static Logger logger = LoggerFactory.getLogger("net.p2p");
	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, P2PMessage message, List<Object> list){
		Event event = message.getEvent();
		byte[] encodedData = event.getBytes();
		DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(encodedData), message.getInetSocketAddress());
		list.add(packet);
	}

	/**
	 *
	 * @param channelHandlerContext
	 * @param packet
	 * @param list
	 * @throws Exception
	 */
	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket packet,
			List<Object> list){
		ByteBuf buf = packet.content();
		byte[] encodedData = new byte[buf.readableBytes()];

		buf.readBytes(encodedData);
		Event event =  Event.genEvent(encodedData);
		P2PMessage message = new P2PMessage(packet.sender(),event);
		list.add(message);
	}
}
