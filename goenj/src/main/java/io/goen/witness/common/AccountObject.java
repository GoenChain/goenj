package io.goen.witness.common;
/**
 */

public class AccountObject {

    /**
     * ID
     */
    private long id;

    /**
     */
    private String accoutName;

    /**
     */
    private long balance;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccoutName() {
        return accoutName;
    }

    public void setAccoutName(String accoutName) {
        this.accoutName = accoutName;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void addBalance(long rewards) {
        this.balance += rewards;
    }
}
