package io.goen.core.txn;

import com.google.common.base.MoreObjects;
import com.medici.firestar.core.myds.Transferable;
import com.medici.firestar.crypto.ECDSASignature;
import com.medici.firestar.util.Base58;
import com.medici.firestar.util.ByteUtil;
import com.medici.firestar.util.RLP;
import com.medici.firestar.util.RLPList;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class Tip extends AbstractTxnBase implements Transferable {

    /*
	type
    contentId
    value
    extraData
    timestamp
    nonce
    signature
	 */


    /**
     */
    private byte[] contentId;


    /**
     */
    protected byte[] value;

    /**
     */
    protected byte[] extraData;

    public Tip(byte[] nonce,byte[] contentId, byte[] value, byte[] timestamp, byte[] extraData, ECDSASignature signature) {
        this.type = TxnType.TIP;
        this.contentId = contentId;
        this.value = value;
        this.timestamp = timestamp;
        this.extraData = extraData;
        this.signature = signature;
        this.nonce = nonce;
    }

    public Tip(byte[] nonce, byte[] contentId, byte[] value, byte[] timestamp, byte[] extraData) {
        this.contentId = contentId;
        this.value = value;
        this.timestamp = timestamp;
        this.extraData = extraData;
        this.nonce = nonce;
    }

    public Tip() {
        this.type = TxnType.TIP;
    }

    /**
     *
     * @param contentId
     * @param value
     * @param timestamp
     * @param extraData
     * @return
     */

        /*
	type
    contentId
    value
    extraData
    timestamp
    nonce
    signature
	 */

    /**
     * @param contentId
     * @param value
     * @param extraData
     * @param timestamp
     * @param nonce
     * @return
     */
    public static Tip create(String contentId, BigInteger value, String extraData,Long timestamp, BigInteger nonce) {
        Tip tip = new Tip();
        tip.setContentId(Base58.decode(contentId));
        tip.setValue(ByteUtil.bigIntegerToBytes(value));
        tip.setExtraData(Base58.decode(extraData));
        tip.setTimestamp(ByteUtil.longToBytes(timestamp));
        tip.setNonce(ByteUtil.bigIntegerToBytes(nonce));
        return tip;
    }



    @Override
    protected List<byte[]> encodeBodies() {
        List<byte[]> bodiesElements = new ArrayList<>();
        byte[] type = RLP.encodeByte(this.type.asByte());
        byte[] contentId = RLP.encodeElement(this.contentId);
        byte[] value = RLP.encodeElement(this.value);
        byte[] timestamp = RLP.encodeElement(this.timestamp);
        byte[] extraData = RLP.encodeElement(this.extraData);
        byte[] nonce = RLP.encodeElement(this.nonce);

        bodiesElements.add(type);
        bodiesElements.add(contentId);
        bodiesElements.add(value);
        bodiesElements.add(timestamp);
        bodiesElements.add(extraData);
        bodiesElements.add(nonce);

        return bodiesElements;
    }

    @Override
    public void decode(byte[] contentEncode) {
        if (!decoded) {
            RLPList decodedTxList = RLP.decode2(contentEncode);
            RLPList transaction = (RLPList) decodedTxList.get(0);
            this.type = TxnType.fromInt(transaction.get(0).getRLPData()[0]);
            this.contentId = transaction.get(1).getRLPData();
            this.value = transaction.get(2).getRLPData();
            this.timestamp = transaction.get(3).getRLPData();
            this.extraData = transaction.get(4).getRLPData();
            this.nonce = transaction.get(5).getRLPData();
            if(extraData == null) {
                extraData = new byte[0];
            }
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
        return true;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Txn.class).add("hash",getHash()).add("type", type.asByte()).add("contentId",
                Base58.encode(contentId)).add("value", Hex.toHexString(value)).add("timestamp",
                timestamp).add("extraData", Hex.toHexString(extraData)).add("nonce",
                Hex.toHexString(nonce)).toString();
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

    public byte[] getContentId() {
        return contentId;
    }

    public void setContentId(byte[] contentId) {
        this.contentId = contentId;
    }
}
