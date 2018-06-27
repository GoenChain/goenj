package io.goen.witness.comment;


import java.math.BigInteger;

public class RewardFundContext {
    private BigInteger recentClaims;
    private long       rewardBalance;
    private long  goenAwarded;

    public BigInteger getRecentClaims() {
        return recentClaims;
    }

    public void setRecentClaims(BigInteger recentClaims) {
        this.recentClaims = recentClaims;
    }

    public void addRecentClaims(BigInteger recentClaims) {
        this.recentClaims = this.recentClaims.add(recentClaims);
    }

    public long getRewardBalance() {
        return rewardBalance;
    }

    public void setRewardBalance(long rewardBalance) {
        this.rewardBalance = rewardBalance;
    }

    public long getGoenAwarded() {
        return goenAwarded;
    }

    public void setGoenAwarded(long goenAwarded) {
        this.goenAwarded = goenAwarded;
    }

    public void addGoenAwarded(long awarded) {
        this.goenAwarded += awarded;
    }
}
