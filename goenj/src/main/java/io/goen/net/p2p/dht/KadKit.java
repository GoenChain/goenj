package io.goen.net.p2p.dht;

import io.goen.net.p2p.Node;

import java.math.BigInteger;

public class KadKit {
    private KadKit(){}

    public static int calcDistance(Node nodeA, Node nodeB){
        BigInteger bigIntegerA = new BigInteger(nodeA.getNodeId());
        BigInteger bigIntegerB = new BigInteger(nodeA.getNodeId());
        return KadConfig.BINS - getNoneZeroIndex(bigIntegerA.xor(bigIntegerB).toByteArray());
    }

    private static final byte[] noneZeroDict =  new byte[]{
        8,7,6,6,5,5,5,5,4,4,4,4,4,4,4,4,
        3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,
        2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
        2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    };


    public static int getNoneZeroIndex(byte[] bytes){
        int noneZeroCount = 0;

        for (byte b : bytes){
            int noneZero = noneZeroDict[b&0xff];
            noneZeroCount += noneZero;
            if(noneZero != 8){
                break;
            }
        }
        return noneZeroCount;
    }
}
