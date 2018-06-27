package io.goen.core.txn;

import com.google.common.base.MoreObjects;
import com.medici.firestar.crypto.ECDSASignature;
import com.medici.firestar.util.Base58;
import com.medici.firestar.util.ByteUtil;
import com.medici.firestar.util.RLP;
import com.medici.firestar.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class Like extends AbstractTxnBase {

	/*
	type
    contentId
    likeOrUnlike
    extraData
    timestamp
    nonce
    signature
	 */
	private byte[] contentId;

	private byte[] likeOrUnlike;

	private byte[] extraData;




	public Like(byte[] contentId, byte[] likeOrUnlike, byte[] timestamp, byte[] extraData, ECDSASignature signature) {
		this.type = TxnType.LIKE;
		this.contentId = contentId;
		this.likeOrUnlike = likeOrUnlike;
		this.timestamp = timestamp;
		this.extraData = extraData;
		this.signature = signature;
	}

	public Like(byte[] nonce, byte[] contentId, byte[] likeOrUnlike, byte[] timestamp, byte[] extraData) {
		this.nonce = nonce;
		this.type = TxnType.LIKE;
		this.contentId = contentId;
		this.likeOrUnlike = likeOrUnlike;
		this.timestamp = timestamp;
		this.extraData = extraData;
	}

	public Like() {
		this.type = TxnType.LIKE;
	}


    /**
     * @param contentId
     * @param likeOrUnlike
     * @param extraData
     * @param timestamp
     * @param nonce
     * @return
     */
	public static Like create(String contentId, int likeOrUnlike, String extraData, Long timestamp,BigInteger nonce) {
		Like like = new Like();
		like.setContentId(Base58.decode(contentId));
		like.setLikeOrUnlike(ByteUtil.intToBytes(likeOrUnlike));
        like.setExtraData(Base58.decode(extraData));
        like.setTimestamp(ByteUtil.longToBytes(timestamp));
        like.setNonce(ByteUtil.bigIntegerToBytes(nonce));

		return like;
	}

	@Override
	protected List<byte[]> encodeBodies() {
		List<byte[]> bodiesElements = new ArrayList<>();
		byte[] type = RLP.encodeByte(this.type.asByte());
		byte[] contentId = RLP.encodeElement(this.contentId);
		byte[] likeOrUnlike = RLP.encodeElement(this.likeOrUnlike);
		byte[] extraData = RLP.encodeElement(this.extraData);
		byte[] timestamp = RLP.encodeElement(this.timestamp);
		byte[] nonce = RLP.encodeElement(this.nonce);

		bodiesElements.add(type);
		bodiesElements.add(contentId);
		bodiesElements.add(likeOrUnlike);
		bodiesElements.add(extraData);
		bodiesElements.add(timestamp);
		bodiesElements.add(nonce);

		return bodiesElements;
	}



	@Override
	public void decode(byte[] contentEncode) {
	    if (!decoded) {
            RLPList decodedTxList = RLP.decode2(contentEncode);
            RLPList like = (RLPList) decodedTxList.get(0);
            this.type = TxnType.fromInt(like.get(0).getRLPData()[0]);
            this.contentId = like.get(1).getRLPData();
            this.likeOrUnlike = like.get(2).getRLPData();
            this.extraData = like.get(3).getRLPData();
			this.timestamp = like.get(4).getRLPData();
            this.nonce = like.get(5).getRLPData();
            if(extraData == null) {
                extraData = new byte[0];
            }
            if (like.get(6).getRLPData() != null) {
                byte v = like.get(6).getRLPData()[0];
                byte[] r = like.get(7).getRLPData();
                byte[] s = like.get(8).getRLPData();
                this.signature = ECDSASignature.fromComponents(r, s, v);
            }
            decoded = true;
	    }
	}

	private static final int CONTENT_HASH_LENGTH = 32;
	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public String toString() {
	    String str = "";
	    try {
            str = MoreObjects.toStringHelper(Txn.class).add("hash", getHash())
            .add("type", type.asByte())
            .add("contentId",Hex.toHexString(contentId))
            .add("likeOrUnlike", Hex.toHexString(likeOrUnlike))
            .add("timestamp",Hex.toHexString(timestamp))
            .add("extraData", Hex.toHexString(extraData))
            .add("nonce", Hex.toHexString(nonce)).toString();
        }catch (Exception e) {
            System.out.println("");
        }
		return str;
	}




	public byte[] getLikeOrUnlike() {
		return likeOrUnlike;
	}

	public void setLikeOrUnlike(byte[] likeOrUnlike) {
		this.likeOrUnlike = likeOrUnlike;
	}


	public byte[] getContentId() {
		return contentId;
	}

	public void setContentId(byte[] contentId) {
		this.contentId = contentId;
	}

	public byte[] getExtraData() {
		return extraData;
	}

	public void setExtraData(byte[] extraData) {
		this.extraData = extraData;
	}
}
