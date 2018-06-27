package io.goen.witness.common;/**
 * Created by yuanhangzhang1 on 2018/5/23.
 */

import java.math.BigInteger;

/**
 *
 *
 * @author yuanhangzhang1
 * @create 2018-05-23 14:08
 **/
public class RewardFundObject {
    /**
     * ID
     */
    private long id;

    /**
     */
    private String name;

    /**
     */
    private long rewardBalance;

    /**
     *
     */
    private BigInteger recentClaims;

    /**
     */
    private long lastUpdate;

    /**
     */
    private BigInteger contentConstants;

    /**
     */
    private int percentCurationRewards;

    /**
     */
    private int percentContentRewards;

    /**
     */
    private CurveEnum authorRewardCurve;

    /**
     */
    private CurveEnum curationRewardCurve;

    /**
     */
    public RewardFundObject() {
        id = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getRewardBalance() {
        return rewardBalance;
    }

    public void setRewardBalance(long rewardBalance) {
        this.rewardBalance = rewardBalance;
    }

    public void addRewardBalance(long rewardBalance) {
        this.rewardBalance += rewardBalance;
    }

    public BigInteger getRecentClaims() {
        return recentClaims;
    }

    public void setRecentClaims(BigInteger recentClaims) {
        this.recentClaims = recentClaims;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public BigInteger getContentConstants() {
        return contentConstants;
    }

    public void setContentConstants(BigInteger contentConstants) {
        this.contentConstants = contentConstants;
    }

    public int getPercentCurationRewards() {
        return percentCurationRewards;
    }

    public void setPercentCurationRewards(int percentCurationRewards) {
        this.percentCurationRewards = percentCurationRewards;
    }

    public int getPercentContentRewards() {
        return percentContentRewards;
    }

    public void setPercentContentRewards(int percentContentRewards) {
        this.percentContentRewards = percentContentRewards;
    }

    public CurveEnum getAuthorRewardCurve() {
        return authorRewardCurve;
    }

    public void setAuthorRewardCurve(CurveEnum authorRewardCurve) {
        this.authorRewardCurve = authorRewardCurve;
    }

    public CurveEnum getCurationRewardCurve() {
        return curationRewardCurve;
    }

    public void setCurationRewardCurve(CurveEnum curationRewardCurve) {
        this.curationRewardCurve = curationRewardCurve;
    }
}
