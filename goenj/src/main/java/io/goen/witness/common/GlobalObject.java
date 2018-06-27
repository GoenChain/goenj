package io.goen.witness.common;/**
 * Created by yuanhangzhang1 on 2018/5/7.
 */

/**
 *
 *
 * @author yuanhangzhang1
 * @create 2018-05-07 14:43
 **/
public class GlobalObject {
    /**
     * ID
     */
    private long id;

    /**
     */
    private int headBlockNumber = 0;

    /**
     */
    private byte[] headBlockId;

    /**
     */
    private long blockTime;

    /**
     */
    private String currentWitness;

    /**
     * slot
     */
    private int currentSlot = 0;

    private long totalSupply = 0;

    public GlobalObject() {
        headBlockNumber = 0;
        currentSlot = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getHeadBlockNumber() {
        return headBlockNumber;
    }

    public void setHeadBlockNumber(int headBlockNumber) {
        this.headBlockNumber = headBlockNumber;
    }

    public byte[] getHeadBlockId() {
        return headBlockId;
    }

    public void setHeadBlockId(byte[] headBlockId) {
        this.headBlockId = headBlockId;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(long blockTime) {
        this.blockTime = blockTime;
    }

    public String getCurrentWitness() {
        return currentWitness;
    }

    public void setCurrentWitness(String currentWitness) {
        this.currentWitness = currentWitness;
    }

    public int getCurrentSlot() {
        return currentSlot;
    }

    public void setCurrentSlot(int currentSlot) {
        this.currentSlot = currentSlot;
    }

    public long getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(long totalSupply) {
        this.totalSupply = totalSupply;
    }

    public void addTotalSupply(long supply) {
        this.totalSupply += supply;
    }

}
