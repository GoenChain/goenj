package io.goen.witness;

import com.medici.firestar.witness.common.WitnessObject;
import com.medici.firestar.witness.common.WitnessScheduleObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class WitnessScheduled {

    private static final Logger logger = LoggerFactory.getLogger("witness");

    /**
     * @param db
     */
    public static void updateWitnessScheduled(DbProxy db) {
        if(db.headBlockNum() % WitnessConstants.GOEN_MAX_WITNESSES != 0) {
            return;
        }

        WitnessScheduleObject wso = db.getWitnessScheduleObject();
        ArrayList<String> targetWitnessArray = new ArrayList<>(WitnessConstants.GOEN_MAX_WITNESSES);

        List<WitnessObject> witLst = db.getWitnessListByVotes();
        int voteMaxCnt = WitnessConstants.GOEN_MAX_WITNESSES;

        int curCnt = 0;
        for(WitnessObject wo : witLst) {
            if(null == wo.getPublicKey()) {
                continue;
            }

            curCnt++;
            String name = wo.getOwner();
            logger.info("   witness_{}{}", curCnt, name);

            targetWitnessArray.add(name);
            if( curCnt >= voteMaxCnt) {
                break;
            }
        }

        int actCnt = targetWitnessArray.size();

        {
            String[] witnessArray = new String[WitnessConstants.GOEN_MAX_WITNESSES];
            for (int idx = 0; idx < actCnt; ++idx) {
                witnessArray[idx] = targetWitnessArray.get(idx);
            }
            for (int idx=actCnt; idx<WitnessConstants.GOEN_MAX_WITNESSES; ++idx) {
                witnessArray[idx] = "";
            }

            ShuffledWitnesses(witnessArray, actCnt, db.headBlockTime());
            wso.setWitnesses(witnessArray);

            printWitness(witnessArray, actCnt);

            int numWitness = Math.max(actCnt, 1);
            wso.setNumWitness(numWitness);
        }
    }

    private static void ShuffledWitnesses(String[] witArray, int witCnt, long tm) {

        BigInteger hi = BigInteger.valueOf(tm).shiftLeft(32);
        BigInteger coef = BigInteger.valueOf(2685821657736338717L);
        for( int i = 0; i < witCnt; ++i )
        {
            /// High performance random generator
            /// http://xorshift.di.unimi.it/

            BigInteger k = hi.add( coef.multiply( new BigInteger(i+"")));
            k = k.xor( k.shiftRight(12) );
            k = k.xor( k.shiftLeft(25) );
            k = k.xor( k.shiftRight(27) );
            k = k.multiply( coef );

            long jmax = witCnt - i;
            int j = i + k.remainder(BigInteger.valueOf(jmax)).intValue();

            String tmp = witArray[i];
            witArray[i] = witArray[j];
            witArray[j] = tmp;
        }
    }

    private static void printWitness(String[] witArray, int witCnt) {
        for (int idx=0; idx<witCnt; ++idx) {
            logger.info(" NO.{} {}", idx+1, witArray[idx]);
        }
    }
}
