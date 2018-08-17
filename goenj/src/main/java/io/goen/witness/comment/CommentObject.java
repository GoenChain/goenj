package io.goen.witness.comment;
import java.math.BigInteger;


public class CommentObject {
    /**
     * ID
     */
    private String contentTxHash;
    /**
     */
    private String author;
    /**
     */
    private String permlink;

    /**
     */
    private long lastUpdate;
    /**
     */
    private long created;

    /**
     */
    private long active;


    private long netShares;

    /**
     */
    private long netVotes;

    /**
     */
    private long cashoutTime;

    /**
     */
    private BigInteger totalWeight;
    /**
     */
    private int rewardWeight;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPermlink() {
        return permlink;
    }

    public void setPermlink(String permlink) {
        this.permlink = permlink;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getActive() {
        return active;
    }

    public void setActive(long active) {
        this.active = active;
    }

    public long getNetShares() {
        return netShares;
    }

    public void setNetShares(long netShares) {
        this.netShares = netShares;
    }

    public long getNetVotes() {
        return netVotes;
    }

    public void setNetVotes(long netVotes) {
        this.netVotes = netVotes;
    }

    public long getCashoutTime() {
        return cashoutTime;
    }

    public void setCashoutTime(long cashoutTime) {
        this.cashoutTime = cashoutTime;
    }

    public BigInteger getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigInteger totalWeight) {
        this.totalWeight = totalWeight;
    }

    public int getRewardWeight() {
        return rewardWeight;
    }

    public void setRewardWeight(int rewardWeight) {
        this.rewardWeight = rewardWeight;
    }
}
