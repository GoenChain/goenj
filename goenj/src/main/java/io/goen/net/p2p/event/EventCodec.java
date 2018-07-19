package io.goen.net.p2p.event;

import java.util.List;

import io.goen.core.GoenConfig;
import io.goen.net.crypto.ECDSASignature;
import io.goen.net.p2p.P2PMessage;
import io.goen.util.ByteUtil;
import io.goen.util.FastByteComparisons;
import io.goen.util.HashUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageCodec;
import org.spongycastle.util.BigIntegers;

/**
 * [mdc(32byte)][version(1byte)][type(1byte)][signature(65byte)][data(undefined)
 * ]
 */
public class EventCodec extends MessageToMessageCodec<DatagramPacket, P2PMessage> {
	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, P2PMessage message, List<Object> list)
			throws Exception {
		Event event = message.getEvent();
		byte[] checkData = new byte[1 + event.getData().length];
		checkData[0] = event.getType()[0];
		checkData[1] = event.getVersion()[0];
		System.arraycopy(event.getData(), 0, checkData, 2, event.getData().length);
		byte[] forSig = HashUtil.sha256(checkData);

		ECDSASignature signature = GoenConfig.system.getSystemKey().sign(forSig);

		signature.v -= 27;

		byte[] sigBytes = ByteUtil.merge(BigIntegers.asUnsignedByteArray(32, signature.r), BigIntegers
				.asUnsignedByteArray(32, signature.s), new byte[] { signature.v });

		byte[] forSha = ByteUtil.merge(event.getVersion(), event.getType(), sigBytes, event.getData());
		byte[] mdc = HashUtil.sha256(forSha);

		byte[] encodedData = ByteUtil.merge(mdc, forSha);
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
			List<Object> list) throws Exception {
		ByteBuf buf = packet.content();
		byte[] encodedData = new byte[buf.readableBytes()];
		buf.readBytes(encodedData);

		byte[] mdc = new byte[32];
		System.arraycopy(encodedData, 0, mdc, 0, 32);

		byte[] version = new byte[1];
		version[0] = encodedData[32];

		byte[] type = new byte[2];
		type[0] = encodedData[33];




		byte[] signature = new byte[65];
		System.arraycopy(encodedData, 34, signature, 0, 65);

		byte[] data = new byte[encodedData.length - 99];
		System.arraycopy(encodedData, 99, data, 0, data.length);

		byte[] checkData = new byte[encodedData.length - 32];
		System.arraycopy(encodedData, 32, checkData, 0, checkData.length);

		byte[] mdcCheck = HashUtil.sha256(checkData);

		int check = FastByteComparisons.compareTo(mdc, 0, mdc.length, mdcCheck, 0, mdcCheck.length);

		if (check != 0)
			throw new RuntimeException("MDC check failed");
		if (version[0] != 1)
			throw new RuntimeException("version can't handle");

		Event event;
		switch (type[0]) {
		case 1:
			event = new PingEvent();
			break;
		case 2:
			event = new PongEvent();
			break;
		case 3:
			event = new FindEvent();
			break;
		case 4:
			event = new NodesEvent();
			break;
		default:
			throw new RuntimeException("p2p message is error type: " + type[0]);
		}

		event.setMdc(mdc);
		event.setSignature(signature);
		event.setVersion(version);
		event.setType(type);
		event.setData(data);
		event.parse(data);

		P2PMessage message = new P2PMessage(packet.sender(),event);
		list.add(message);
	}
}
