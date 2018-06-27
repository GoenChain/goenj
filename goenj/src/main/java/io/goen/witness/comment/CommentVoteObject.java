package io.goen.witness.comment;/**
 * Created by yuanhangzhang1 on 2018/5/24.
 */

import java.math.BigInteger;

public class CommentVoteObject {
    /**
     * ID
     */
    private long id;

    /**
     */
    private String accountName;

    /**
     * CommentID
     */
    private long commentId;

    /**
     */
    private BigInteger weight;

    /**
     */
    private long rshares = 0;

    /**
     */
    private long lastUpdate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getCommentId() {
        return commentId;
    }

    public void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public BigInteger getWeight() {
        return weight;
    }

    public void setWeight(BigInteger weight) {
        this.weight = weight;
    }

    public long getRshares() {
        return rshares;
    }

    public void setRshares(long rshares) {
        this.rshares = rshares;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
