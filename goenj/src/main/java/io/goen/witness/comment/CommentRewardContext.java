package io.goen.witness.comment;

import com.medici.firestar.witness.common.CurveEnum;

import java.math.BigInteger;

public class CommentRewardContext {

    private long rshares;
    private int rewardWeight = 0;
    private BigInteger totalRewardShares;
    private long totalRewardFund;

    private CurveEnum rewardCurve = CurveEnum.QUADRATIC;		//
    private BigInteger contentConstant;

    public long getRshares() {
        return rshares;
    }

    public void setRshares(long rshares) {
        this.rshares = rshares;
    }

    public int getRewardWeight() {
        return rewardWeight;
    }

    public void setRewardWeight(int rewardWeight) {
        this.rewardWeight = rewardWeight;
    }

    public BigInteger getTotalRewardShares() {
        return totalRewardShares;
    }

    public void setTotalRewardShares(BigInteger totalRewardShares) {
        this.totalRewardShares = totalRewardShares;
    }

    public long getTotalRewardFund() {
        return totalRewardFund;
    }

    public void setTotalRewardFund(long totalRewardFund) {
        this.totalRewardFund = totalRewardFund;
    }

    public CurveEnum getRewardCurve() {
        return rewardCurve;
    }

    public void setRewardCurve(CurveEnum rewardCurve) {
        this.rewardCurve = rewardCurve;
    }

    public BigInteger getContentConstant() {
        return contentConstant;
    }

    public void setContentConstant(BigInteger contentConstant) {
        this.contentConstant = contentConstant;
    }
}
