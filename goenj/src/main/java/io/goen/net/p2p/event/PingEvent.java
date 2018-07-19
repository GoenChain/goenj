package io.goen.net.p2p.event;

import io.goen.rlp.RLP;
import io.goen.rlp.RLPList;
import io.goen.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

public class PingEvent extends Event {
	String toIp;
	int toPort;
	String fromIp;
	int fromPort;
	String randomHexString;
	long expires;

	public PingEvent() {
		this.setType(new byte[] { 1 });
	}

	@Override
	public void parse(byte[] encodedData) {
		this.pareseBase(encodedData);
		byte[] data = this.getData();
		RLPList list = (RLPList) RLP.decode2OneItem(data, 0);
		this.toIp = ByteUtil.bytesToIp(list.get(0).getRLPData());
		this.toPort = ByteUtil.bytesToInt(list.get(1).getRLPData());

		this.fromIp = ByteUtil.bytesToIp(list.get(2).getRLPData());
		this.fromPort = ByteUtil.bytesToInt(list.get(3).getRLPData());

		this.randomHexString = Hex.toHexString(list.get(4).getRLPData());

		this.expires = ByteUtil.bytesToLong(list.get(5).getRLPData());

	}

	@Override
	public byte[] getBytes() {

		byte[] tmpExp = ByteUtil.longToBytes(expires);
		byte[] rlpExp = RLP.encodeElement(tmpExp);

		byte[] rlpFromIp = RLP.encodeElement(ByteUtil.ipTobytes(this.fromIp));
		byte[] rlpFromPort = RLP.encodeInt(this.fromPort);

		byte[] rlpToIp = RLP.encodeElement(ByteUtil.ipTobytes(this.toIp));
		byte[] rlpToPort = RLP.encodeInt(this.toPort);

		byte[] rlpRandomHexString = RLP.encodeElement(Hex.decode(this.randomHexString));

        return RLP.encodeList(rlpToIp, rlpToPort, rlpFromIp, rlpFromPort, rlpRandomHexString, rlpExp);
	}
}
