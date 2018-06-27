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

public class Content extends AbstractTxnBase{
    /*
    type
    contentHash
    extraData
    timestamp
    nonce
    signature
    */
    public Content(){
        this.type = TxnType.CONTENT;
    }
    /**
     */
    private byte[] contentHash;

    /**
     */
    private byte[] extraData;

    @Override
    protected List<byte[]> encodeBodies() {
        List<byte[]> bodiesElements = new ArrayList<>();
        byte[] type = RLP.encodeByte(this.type.asByte());
        byte[] contentHash = RLP.encodeElement(this.contentHash);
        byte[] extraData = RLP.encodeElement(this.extraData);
        byte[] nonce = RLP.encodeElement(this.nonce);
        byte[] timestamp = RLP.encodeElement(this.timestamp);

        bodiesElements.add(type);
        bodiesElements.add(contentHash);
        bodiesElements.add(extraData);
        bodiesElements.add(timestamp);
        bodiesElements.add(nonce);

        return bodiesElements;
    }

    @Override
    public void decode(byte[] contentEncode) {
        if (!decoded) {
            RLPList decodedTxList = RLP.decode2(contentEncode);
            RLPList content = (RLPList) decodedTxList.get(0);
            this.type = TxnType.fromInt(content.get(0).getRLPData()[0]);
            this.contentHash = content.get(1).getRLPData();
            this.extraData = content.get(2).getRLPData();
            this.timestamp = content.get(3).getRLPData();
            this.nonce = content.get(4).getRLPData();
            if (content.get(5).getRLPData() != null) {
                byte v = content.get(5).getRLPData()[0];
                byte[] r = content.get(6).getRLPData();
                byte[] s = content.get(7).getRLPData();
                this.signature = ECDSASignature.fromComponents(r, s, v);
            }
            decoded = true;
        }
    }

    /**
     * @param contentHash
     * @param extraData
     * @param timestamp
     * @param nonce
     * @return
     */
    public static Content create(String contentHash, String extraData,long timestamp, BigInteger nonce){
        Content content = new Content();
        content.setContentHash(Base58.decode(contentHash));
        content.setExtraData(Base58.decode(extraData));
        content.setTimestamp(ByteUtil.longToBytes(timestamp));
        content.setNonce(ByteUtil.bigIntegerToBytes(nonce));
        return content;
    }

    private static final int CONTENT_HASH_LENGTH = 32;
    @Override
    public boolean validate() {
        if( this.type == null || this.type != TxnType.CONTENT ){
            return false;
        }

        if( this.contentHash.length > CONTENT_HASH_LENGTH ){
            return false;
        }

        return true;
    }


    @Override
    public String toString() {
        String str = "";
        try {
            str = MoreObjects.toStringHelper(Txn.class).add("hash", getHash()).add("type", type.asByte())
                    .add("contentHash", Hex.toHexString(contentHash))
                    .add("extraData", Hex.toHexString(extraData))
                    .add("timestamp", Hex.toHexString(timestamp))
                    .add("nonce", Hex.toHexString(nonce))
                    .toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }
    public byte[] getExtraData() {
        return extraData;
    }

    public void setExtraData(byte[] extraData) {
        this.extraData = extraData;
    }

    public byte[] getContentHash() {
        return contentHash;
    }

    public void setContentHash(byte[] contentHash) {
        this.contentHash = contentHash;
    }
}
