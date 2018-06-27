package io.goen.witness;/**
 * Created by yuanhangzhang1 on 2018/6/7.
 */

/**
 *
 *
 * @author yuanhangzhang1
 * @create 2018-06-07 15:45
 **/
public class WitnessNode {
    /**
     */
    private String witName;
    /**
     */
    private String priKey;


    public WitnessNode(String name, String priKey) {
        this.witName = name;
        this.priKey = priKey;
    }

    public String getWitName() {
        return witName;
    }

    public void setWitName(String witName) {
        this.witName = witName;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }
}
