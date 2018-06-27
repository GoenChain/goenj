package io.goen.core.txn;

import com.medici.firestar.crypto.ECDSASignature;
import com.medici.firestar.util.RLP;
import com.medici.firestar.util.RLPList;

/**
 *
 */
public interface TxnBase {
	/**
	 */
	byte[] encodeForSign();

	/**
	 */
	byte[] encodeWithSign(String priKey);

	/**
	 *
	 * @return
	 */
	void decode(byte[] contentEncode);

	/**
	 */
	ECDSASignature sign(String priKey);

	TxnType getType();

	byte[] getHash();

	byte[] getEncoded();

	byte[] getNonce();

	byte[] getTimestamp();

	void setNonce(byte[] nonce);

	boolean validate();

	void verify();

	byte[] getFrom();

	static TxnType getType(byte[] encoded) {
		RLPList decodedTxList = RLP.decode2(encoded);
		RLPList transaction = (RLPList) decodedTxList.get(0);
		return TxnType.fromInt(transaction.get(0).getRLPData()[0]);
	}

	static TxnBase create(byte[] encoded) {
		TxnBase txnBase = null;
		TxnType txnType = TxnBase.getType(encoded);
		switch (txnType) {
		case LIKE:
			txnBase = new Like();
			break;
		case TXN:
			txnBase = new Txn();
			break;
		case TIP:
			txnBase = new Tip();
			break;
		case APPLY:
			txnBase = new Apply();
			break;
		case VOTE:
			txnBase = new Vote();
			break;
		case CONTENT:
			txnBase = new Content();
			break;
		case UNKNOWN:
			break;
		default:
			throw new RuntimeException("can not construct TxnBase");
		}
		if (txnBase != null) {
			txnBase.decode(encoded);
		}
		return txnBase;
	}
}
