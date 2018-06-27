package io.goen.core.txn;

import com.medici.firestar.crypto.AddressUtil;
import com.medici.firestar.crypto.ECDSASignature;
import com.medici.firestar.crypto.HashUtil;
import com.medici.firestar.util.RLP;
import org.spongycastle.util.BigIntegers;

import java.util.List;

public abstract class AbstractTxnBase implements TxnBase {
    public static final int HASH_LENGTH = 32;
    public static final int ADDRESS_LENGTH = 24;


    protected TxnType type;


    protected byte[] timestamp;


    protected byte[] nonce;


    protected ECDSASignature signature;


    protected byte[] encodeNoSign;


    protected byte[] encodeWithSign;


    protected boolean decoded = false;


    abstract protected List<byte[]> encodeBodies();


    @Override
    public byte[] encodeForSign() {
        if (this.encodeNoSign != null) {
            return this.encodeNoSign;
        }
        List<byte[]> encodedElements = encodeBodies();
        byte[] encodeForSign = RLP.encodeList(encodedElements.toArray(new byte[0][]));
        this.encodeNoSign = encodeForSign;
        return encodeForSign;
    }

    @Override
    public byte[] encodeWithSign(String priKey) {
        List<byte[]> encodedElements = encodeBodies();
        if (signature == null) {
            this.encodeNoSign = RLP.encodeList(encodedElements.toArray(new byte[0][]));
            signature = AddressUtil.sign(encodeNoSign, priKey);
        }

        return encodeWithSignature(signature, encodedElements);

    }

    protected byte[] encodeWithSignature(ECDSASignature signature, List<byte[]> encodedElements) {
        byte[] v = RLP.encodeByte(signature.v);
        byte[] r = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.r));
        byte[] s = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.s));
        encodedElements.add(v);
        encodedElements.add(r);
        encodedElements.add(s);

        byte[] encodeWithSign = RLP.encodeList(encodedElements.toArray(new byte[0][]));
        this.encodeWithSign = encodeWithSign;
        return encodeWithSign;
    }

    @Override
    public ECDSASignature sign(String priKey) {
        byte[] encodeForSign = encodeForSign();
        ECDSASignature signature = AddressUtil.sign(encodeForSign, priKey);
        this.signature = signature;
        return signature;
    }

    @Override
    public abstract void decode(byte[] contentEncode);


    @Override
    public void verify() {
        decode(getEncoded());
        validate();
    }

    @Override
    public byte[] getHash() {
        if (encodeWithSign == null && signature != null) {
            encodeImplWithSignature();
            return HashUtil.sha256(encodeWithSign);
        } else if (encodeWithSign != null) {
            return HashUtil.sha256(encodeWithSign);
        } else {
            return null;
        }
    }

    @Override
    public byte[] getEncoded() {
        if (encodeWithSign == null && signature != null) {
            encodeImplWithSignature();
        }
        return encodeWithSign;
    }

    private void encodeImplWithSignature() {
        List<byte[]> encodedElements = encodeBodies();
        encodeWithSignature(signature, encodedElements);
    }

    @Override
    public byte[] getFrom() {
		if (encodeNoSign == null) {
			encodeForSign();
		}
		String pubK = AddressUtil.getPubKeyFromSignature(signature, encodeNoSign);
		String addr = AddressUtil.getAddressFromPubKey(pubK);
		return AddressUtil.addressToBytes(addr);
    }



    @Override
    public TxnType getType() {
        return this.type;
    }

    @Override
    public byte[] getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(byte[] timestamp){
        this.timestamp = timestamp;
    }

    public ECDSASignature getSignature() {
        return signature;
    }

    public void setSignature(ECDSASignature signature) {
        this.signature = signature;
    }

    @Override
    public byte[] getNonce() {
        return nonce;
    }

    @Override
    public void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TxnBase)){
            return false;
        }
        TxnBase tx = (TxnBase) obj;
        return tx.getHash() == this.getHash();
    }

    @Override
    public int hashCode() {
        byte[] hash = this.getHash();
        int hashCode = 0;

        for (int i = 0; i < hash.length; ++i) {
            hashCode += hash[i] * i;
        }

        return hashCode;
    }
}
