package io.goen.witness.common;/**
 * Created by yuanhangzhang1 on 2018/5/7.
 */

import com.medici.firestar.witness.WitnessTypeEnum;

/**
 *
 *
 * @author yuanhangzhang1
 * @create 2018-05-07 14:42
 **/
public class WitnessObject {
    /**
     * ID
     */
    private String applyHash;

    /**
     */
    private String owner;

    /**
     */
    private long created;

    /**
     */
    private byte[] publicKey;

    /**
     */
    private long votes;

    /**
     *
     */
    private WitnessTypeEnum witnessType;

    /**
     *
     */
    private byte[] witnessAddr;

    public WitnessObject() {
        votes = 0;
    }

    public String getApplyHash() {
        return applyHash;
    }

    public void setApplyHash(String applyHash) {
        this.applyHash = applyHash;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }

    public WitnessTypeEnum getWitnessType() {
        return witnessType;
    }

    public void setWitnessType(WitnessTypeEnum witnessType) {
        this.witnessType = witnessType;
    }

    public byte[] getWitnessAddr() {
        return witnessAddr;
    }

    public void setWitnessAddr(byte[] witnessAddr) {
        this.witnessAddr = witnessAddr;
    }
}
