package io.goen.net.p2p.event;

import io.goen.rlp.RLP;
import io.goen.rlp.RLPList;
import io.goen.util.ByteUtil;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.spongycastle.util.encoders.Hex;

public class PongEvent extends Event {

	String pingHexString;
	long expires;

	public PongEvent() {
		this.setType(new byte[] { 2 });
	}

	public PongEvent(String pingHexString) {
		this.setType(new byte[] { 2 });
		this.pingHexString = pingHexString;
		this.expires = System.currentTimeMillis() / 1000L + 90 * 60;
	}

	@Override
	public void parse(byte[] encodedData) {
		this.pareseBase(encodedData);
		byte[] data = this.getData();
		RLPList list = (RLPList) RLP.decode2OneItem(data, 0);
		this.pingHexString = Hex.toHexString(list.get(1).getRLPData());
		this.expires = ByteUtil.bytesToLong(list.get(2).getRLPData());
	}

	@Override
	public byte[] getBytes() {
		byte[] tmpExp = ByteUtil.longToBytes(expires);
		byte[] rlpExp = RLP.encodeElement(tmpExp);

		byte[] rlpRandomHexString = RLP.encodeElement(Hex.decode(this.pingHexString));

		return RLP.encodeList(rlpRandomHexString, rlpExp);
	}
}
