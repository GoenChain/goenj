package io.goen.net.p2p.event;

import io.goen.util.FastByteComparisons;
import io.goen.util.HashUtil;
import io.netty.buffer.ByteBuf;

public abstract class Event {

    private byte[] mdc;

    private byte[] signature;

    private byte[] type;

    private byte[] data;

    private byte[] version;

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
    public abstract void parse(byte[] encodedData);
    //To byte[]
    public abstract byte[] getBytes();

    public void pareseBase(byte[] encodedData){

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

        if (check != 0)
            throw new RuntimeException("MDC check failed");
        if (version[0] != 1)
            throw new RuntimeException("version can't handle");
    }
}
