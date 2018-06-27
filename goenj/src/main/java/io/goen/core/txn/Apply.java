package io.goen.core.txn;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.medici.firestar.crypto.AddressUtil;
import com.medici.firestar.crypto.ECDSASignature;
import com.medici.firestar.util.ByteUtil;
import com.medici.firestar.util.RLP;
import com.medici.firestar.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Apply extends AbstractTxnBase{


    /*
    type
    witnessName
    timestamp
    nonce
    signature
    */


	private byte[] witnessName;

	/**
	 *
	 */
	private byte[] timestamp;

	public Apply() {
		this.type = TxnType.APPLY;
	}

	/**

	 * @param nonce
	 * @param witnessName
	 * @param timestamp
	 * @return
	 */
	public static Apply create(String witnessName,Long timestamp,BigInteger nonce) {
		Apply apply = new Apply();
		apply.setNonce(ByteUtil.bigIntegerToBytes(nonce));
		apply.setWitnessName(witnessName.getBytes(Charsets.UTF_8));
		apply.setTimestamp(ByteUtil.longToBytes(timestamp));
		return apply;
	}

	@Override
	public TxnType getType() {
		return TxnType.APPLY;
	}

	@Override
	protected List<byte[]> encodeBodies() {
		List<byte[]> bodiesElements = new ArrayList<>();
		byte[] type = RLP.encodeByte(this.type.asByte());
		byte[] witnessName = RLP.encodeElement(this.witnessName);
		byte[] timestamp = RLP.encodeElement(this.timestamp);
		byte[] nonce = RLP.encodeElement(this.nonce);

		bodiesElements.add(type);
		bodiesElements.add(witnessName);
		bodiesElements.add(timestamp);
		bodiesElements.add(nonce);

		return bodiesElements;
	}

	@Override
	public void decode(byte[] contentEncode) {
		if (!decoded) {
			RLPList decodedTxList = RLP.decode2(contentEncode);
			RLPList apply = (RLPList) decodedTxList.get(0);
			this.type = TxnType.fromInt(apply.get(0).getRLPData()[0]);
			this.witnessName = apply.get(1).getRLPData();
			this.timestamp = apply.get(2).getRLPData();
			this.nonce = apply.get(3).getRLPData();

			if (apply.get(4).getRLPData() != null) {
				byte v = apply.get(4).getRLPData()[0];
				byte[] r = apply.get(5).getRLPData();
				byte[] s = apply.get(6).getRLPData();
				this.signature = ECDSASignature.fromComponents(r, s, v);
			}
			decoded = true;
		}
	}

	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public String toString() {
		String str = "";
		try {
			str = MoreObjects.toStringHelper(Txn.class).add("hashId", getHash()).add("type", type.asByte())
					.add("witnessName", AddressUtil.addressToString(witnessName))
					.add("timestamp", Hex.toHexString(timestamp))
					.add("nonce", Hex.toHexString(nonce))
					.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	public byte[] getWitnessName() {
		return witnessName;
	}

	public void setWitnessName(byte[] witnessName) {
		this.witnessName = witnessName;
	}


}
