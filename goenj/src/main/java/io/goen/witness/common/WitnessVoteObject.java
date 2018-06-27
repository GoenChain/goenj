package io.goen.witness.common;

/**
 */

/**
 *
 * @author yuanhangzhang1
 **/
public class WitnessVoteObject {
    /**
     * ID
     */
    private long id;

    private String witnessName;

    private String accountName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWitnessName() {
        return witnessName;
    }

    public void setWitnessName(String witnessName) {
        this.witnessName = witnessName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
