package io.goen.witness.common;/**
 * Created by yuanhangzhang1 on 2017/5/7.
 */

/**
 *
 * @author yuanhangzhang1
 * @create 2017-05-07 14:44
 **/
public class WitnessScheduleObject {
    /**
     * ID
     */
    private long id;

    /**
     */
    private String[] witnesses;

    /**
     */
    private int numWitness;

    private int topWeight = 1;
    private int timeshareWeight = 5;
    private int minerWeight = 1;

    private int normalizationFactor = 25;


    public WitnessScheduleObject() {
//        witnesses = new String[WitnessConstants.GOEN_MAX_WITNESSES];
//        numWitness = 0;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String[] getWitnesses() {
        return witnesses;
    }

    public void setWitnesses(String[] witnesses) {
        this.witnesses = witnesses;
    }

    public int getNumWitness() {
        return numWitness;
    }

    public void setNumWitness(int numWitness) {
        this.numWitness = numWitness;
    }

    public int getTopWeight() {
        return topWeight;
    }

    public void setTopWeight(int topWeight) {
        this.topWeight = topWeight;
    }

    public int getTimeshareWeight() {
        return timeshareWeight;
    }

    public void setTimeshareWeight(int timeshareWeight) {
        this.timeshareWeight = timeshareWeight;
    }

    public int getNormalizationFactor() {
        return normalizationFactor;
    }

    public void setNormalizationFactor(int normalizationFactor) {
        this.normalizationFactor = normalizationFactor;
    }

    public int getMinerWeight() {
        return minerWeight;
    }

    public void setMinerWeight(int minerWeight) {
        this.minerWeight = minerWeight;
    }
}
