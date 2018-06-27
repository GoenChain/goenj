package io.goen.core.txn;

import com.google.common.base.MoreObjects;
import com.medici.firestar.core.myds.Transferable;
import com.medici.firestar.crypto.AddressUtil;
import com.medici.firestar.crypto.ECDSASignature;
import com.medici.firestar.util.Base58;
import com.medici.firestar.util.ByteUtil;
import com.medici.firestar.util.RLP;
import com.medici.firestar.util.RLPList;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Txn extends AbstractTxnBase implements Transferable {

    /*
	type
    to
    value
    extraData
    timestamp
    nonce
    signature
	 */

	/**
	 */
	private byte[] to;

	/**
	 */
    private byte[] value;

	/**
	 */
    private byte[] extraData;

	public Txn(byte[] nonce, byte[] to, byte[] value, byte[] timestamp, byte[] extraData) {
		this.type = TxnType.TXN;
		this.to = to;
		this.value = value;
		this.timestamp = timestamp;
		this.extraData = extraData;
		this.nonce = nonce;
	}

	public Txn(byte[] nonce, byte[] to, byte[] value, byte[] timestamp, byte[] extraData, ECDSASignature signature) {
		this.type = TxnType.TXN;
		this.to = to;
		this.value = value;
		this.timestamp = timestamp;
		this.extraData = extraData;
		this.signature = signature;
		this.nonce = nonce;
	}

	public Txn() {
		this.type = TxnType.TXN;
	}

	@Override
	protected List<byte[]> encodeBodies() {
		List<byte[]> bodiesElements = new ArrayList<>();
		byte[] type = RLP.encodeByte(this.type.asByte());
		byte[] to = RLP.encodeElement(this.to);
		byte[] value = RLP.encodeElement(this.value);
		byte[] timestamp = RLP.encodeElement(this.timestamp);
		byte[] extraData = RLP.encodeElement(this.extraData);
		byte[] nonce = RLP.encodeElement(this.nonce);

		bodiesElements.add(type);
		bodiesElements.add(to);
		bodiesElements.add(value);
        bodiesElements.add(extraData);
		bodiesElements.add(timestamp);
		bodiesElements.add(nonce);

		return bodiesElements;
	}

	@Override
	public void decode(byte[] contentEncode) {
		if (!decoded) {
			RLPList decodedTxList = RLP.decode2(contentEncode);
			RLPList transaction = (RLPList) decodedTxList.get(0);
			this.type = TxnType.fromInt(transaction.get(0).getRLPData()[0]);
			this.to = transaction.get(1).getRLPData();
			this.value = transaction.get(2).getRLPData();
            this.extraData = transaction.get(3).getRLPData();
			this.timestamp = transaction.get(4).getRLPData();
			if (this.extraData == null) {
				this.extraData = new byte[0];
			}
			this.nonce = transaction.get(5).getRLPData();
			if (transaction.get(6).getRLPData() != null) {
				byte v = transaction.get(6).getRLPData()[0];
				byte[] r = transaction.get(7).getRLPData();
				byte[] s = transaction.get(8).getRLPData();
				this.signature = ECDSASignature.fromComponents(r, s, v);
			}
			decoded = true;
		}
	}

	@Override
	public boolean validate() {
		if (getNonce().length > HASH_LENGTH){
			throw new RuntimeException("Nonce is not valid");
		}
		if (to != null && to.length != 0 && to.length != ADDRESS_LENGTH){
			throw new RuntimeException("TxnBase Receive address is not valid");
		}

		if (value != null && value.length > HASH_LENGTH) {
			throw new RuntimeException("TxnBase Value is not valid");
		}
		if (getSignature() != null) {
			if (BigIntegers.asUnsignedByteArray(signature.r).length > HASH_LENGTH) {
				throw new RuntimeException("TxnBase Signature R is not valid");
			}
			if (BigIntegers.asUnsignedByteArray(signature.s).length > HASH_LENGTH) {
				throw new RuntimeException("TxnBase Signature S is not valid");
			}
		}
		return true;
	}

    /**
     * @param to
     * @param value
     * @param extraData
     * @param timestamp
     * @param nonce
     * @return
     */
	public static Txn createTxn(String to, BigInteger value, String extraData, Long timestamp,BigInteger nonce) {
		Txn txn = new Txn();
		txn.setTo(AddressUtil.addressToBytes(to));
		txn.setValue(ByteUtil.bigIntegerToBytes(value));
        txn.setExtraData(Base58.decode(extraData));
		txn.setTimestamp(ByteUtil.longToBytes(timestamp));
		txn.setNonce(ByteUtil.bigIntegerToBytes(nonce));
		return txn;
	}


	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Txn.class).add("hash",getHash()).add("type", type.asByte()).add("to",
				AddressUtil.addressToString(to)).add("value", Hex.toHexString(value)).add("timestamp",
				Hex.toHexString(timestamp)).add("extraData", Hex.toHexString(extraData))
				.add("nonce", Hex.toHexString(nonce))
			.toString();
	}


	public byte[] getTo() {
		return to;
	}

	public void setTo(byte[] to) {
		this.to = to;
	}


	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}


	public byte[] getExtraData() {
		return extraData;
	}

	public void setExtraData(byte[] extraData) {
		this.extraData = extraData;
	}
}
