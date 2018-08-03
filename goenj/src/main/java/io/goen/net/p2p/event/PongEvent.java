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
		super();
		this.setType(new byte[] { 2 });
	}

	public PongEvent(String pingHexString) {
		super();
		this.setType(new byte[] { 2 });
		this.pingHexString = pingHexString;
		this.expires = System.currentTimeMillis() / 1000L + 90 * 60;
	}

	@Override
	public void parseData(byte[] encodedData) {
		RLPList list = (RLPList) RLP.decode2OneItem(encodedData, 0);
		this.pingHexString = Hex.toHexString(list.get(0).getRLPData());
		this.expires = ByteUtil.bytesToLong(list.get(1).getRLPData());
	}

	@Override
	public byte[] getDataBytes() {
		byte[] tmpExp = ByteUtil.longToBytes(expires);
		byte[] rlpExp = RLP.encodeElement(tmpExp);

		byte[] rlpRandomHexString = RLP.encodeElement(Hex.decode(this.pingHexString));

		return RLP.encodeList(rlpRandomHexString, rlpExp);
	}

	public String getPingHexString() {
		return pingHexString;
	}

	public void setPingHexString(String pingHexString) {
		this.pingHexString = pingHexString;
	}

	public long getExpires() {
		return expires;
	}

	public void setExpires(long expires) {
		this.expires = expires;
	}
}
