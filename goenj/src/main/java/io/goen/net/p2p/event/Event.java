package io.goen.net.p2p.event;

import com.google.common.base.Verify;
import io.goen.core.GoenConfig;
import io.goen.net.crypto.ECDSASignature;
import io.goen.util.ByteUtil;
import io.goen.util.FastByteComparisons;
import io.goen.util.HashUtil;
import org.spongycastle.util.BigIntegers;

public abstract class Event {


    private byte[] mdc;

    private byte[] signature;

    private byte[] type;

    private byte[] data;

    private byte[] version;

    public Event(){
        this.version = new byte[]{1};
    }

    public byte[] getMdc() {
        return mdc;
    }

    public void setMdc(byte[] mdc) {
        this.mdc = mdc;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getType() {
        return type;
    }

    public void setType(byte[] type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public byte[] getVersion() {
        return version;
    }

    public void setVersion(byte[] version) {
        this.version = version;
    }
    //From byte[]
    public abstract void parseData(byte[] encodedData);
    //To byte[]
    public abstract byte[] getDataBytes();


    /**
     *
     [mdc(32byte)][version(1byte)][type(1byte)][signature(65byte)][data(undefined)

     **/
    public void parse(byte[] encodedData){

        this.mdc = new byte[32];
        System.arraycopy(encodedData, 0, mdc, 0, 32);

        this.version = new byte[1];
        version[0] = encodedData[32];

        this.type = new byte[1];
        type[0] = encodedData[33];


        byte[] signature = new byte[65];
        System.arraycopy(encodedData, 34, signature, 0, 65);

        this.data = new byte[encodedData.length - 99];
        System.arraycopy(encodedData, 99, data, 0, data.length);

        byte[] checkData = new byte[encodedData.length - 32];

        System.arraycopy(encodedData, 32, checkData, 0, checkData.length);

        byte[] mdcCheck = HashUtil.sha256(checkData);

        int check = FastByteComparisons.compareTo(mdc, 0, mdc.length, mdcCheck, 0, mdcCheck.length);

        Verify.verify(check == 0,"MDC check failed");
        Verify.verify(version[0] == 1,"version can't handle");

        //sub evnet
        parseData(data);
    }

/**
*
[mdc(32byte)][version(1byte)][type(1byte)][signature(65byte)][data(undefined)

 **/
    byte[] getBytes(){
        this.data = getDataBytes();
        byte[] checkData = new byte[2 + this.getData().length];
        checkData[0] = this.getVersion()[0];
        checkData[1] = this.getType()[0];

        System.arraycopy(this.getData(), 0, checkData, 2, this.getData().length);
        byte[] forSig = HashUtil.sha256(checkData);

        ECDSASignature signature = GoenConfig.system.systemKey().sign(forSig);

        signature.v -= 27;

        byte[] sigBytes = ByteUtil.merge(BigIntegers.asUnsignedByteArray(32, signature.r), BigIntegers
                .asUnsignedByteArray(32, signature.s), new byte[] { signature.v });

        byte[] forSha = ByteUtil.merge(this.getVersion(), this.getType(), sigBytes,this.getData());
        byte[] mdc = HashUtil.sha256(forSha);

        return ByteUtil.merge(mdc, forSha);
    }
}
