package io.goen.witness;
/**
 * Created by yuanhangzhang1 on 2018/5/7.
 */

import com.google.common.base.Strings;
import com.medici.firestar.config.SystemProperties;
import com.medici.firestar.crypto.AddressUtil;
import com.medici.firestar.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class WitnessManager {

    private static final Logger logger = LoggerFactory.getLogger("witness");

    /**
     */
    private HashSet<String> localWitness;
    /**
     */
    private HashMap<String, String> priKyWitness;
    /**
     */
    private boolean productEnable = false;

    private ScheduledExecutorService mainWorker = Executors.newSingleThreadScheduledExecutor();

    private SystemProperties config;

//    @Autowired
    private DbProxy dbProxy;


    @Autowired
    private WitnessManager(final SystemProperties config, final DbProxy dbProxy) {

        this.config = config;
        this.dbProxy = dbProxy;

        parseConfig();

//        String confName = WitnessConstants.GOEN_INIT_MINER_NAME;
//        String confPub  = WitnessConstants.GOEN_INIT_PUBLIC_KEY_STR;
//        String confPri  = "9f65ddf79758b5fe4b0d29e6bd0fb8d2029dad5bca601d15fbe6b307c5d1bccb";
//
//        String confNam1 = WitnessConstants.GOEN_INIT_MINER_NAME + "_1";
//        String confPub1 = "0000000000000000000000000000000000000001";
//        String confPri1 = "a175f5d723d6b96eb71eff879b40403340b108e34747d8f32c34ff990a2706aa";
//
//        String confNam2 = WitnessConstants.GOEN_INIT_MINER_NAME + "_2";
//        String confPub2 = "0000000000000000000000000000000000000002";
//        String confPri2 = "4c2074070c038069af9bb6082ed0662e965e89fc83c596496f07d9146c72e011";
//
//        localWitness = new HashSet<>();
//        localWitness.add(confName);
//        localWitness.add(confNam1);
//        localWitness.add(confNam2);
//
//        priKyWitness = new HashMap<>();
//        priKyWitness.put(confPub, confPri);
//        priKyWitness.put(confPub1, confPri1);
//        priKyWitness.put(confPub2, confPri2);


    }

    /**
     */
    private void parseConfig() {

        localWitness = new HashSet<>();
        priKyWitness = new HashMap<>();

        List<WitnessNode> lst = this.config.witnessActive();

        for(WitnessNode wn : lst) {

            String name = wn.getWitName();
            if(Strings.isNullOrEmpty(name) || localWitness.contains(name)) {
                continue;
            }
            localWitness.add(name);

            String priKey = wn.getPriKey();
            if(Strings.isNullOrEmpty(priKey) || priKyWitness.containsKey(priKey)) {
                continue;
            }
            String pubKey = AddressUtil.getPubKeyFromPriKey(priKey);
            priKyWitness.put(pubKey, priKey);
        }
    }

    @PostConstruct
    public void init() {
        logger.info("WitnessManager Init() ========================");
        mainWorker.scheduleWithFixedDelay(() -> {
            try {
                scheduleProductionLoop();
            } catch (Throwable t) {
                logger.error("WitnessManager scheduleProductionLoop error{}", t);
            }
        }, 0, 1, TimeUnit.SECONDS);

    }

    /**
     * @return
     */
    private BlockProduceConditionEnum scheduleProductionLoop() {
        long now = System.currentTimeMillis();
        if( now < WitnessConstants.GENESIS_TIME ) {
            logger.debug("wait until genesis time: {}", WitnessConstants.GENESIS_TIME_STR);
            return BlockProduceConditionEnum.WAIT_FOR_GENESIS;
        }
        logger.info("===============#################################");
        BlockProduceConditionEnum result = maybeProduceBlock();

        switch (result) {
            default:
                logger.info("scheduleProductionLoop result:{}", result);
                break;
        }

        return result;
    }

    private BlockProduceConditionEnum maybeProduceBlock() {

        long now = TimeUtils.millisToSeconds(System.currentTimeMillis());

        // synced
        if(!productEnable) {
            long slotTm = dbProxy.getSlotTime(1);
            logger.debug("slotTime(1)={}, now={}", slotTm, now);

            if(slotTm >= now) {
                productEnable = true;
            } else {
                return BlockProduceConditionEnum.NOT_SYNCED;
            }
        }

        long nextBlockTm = dbProxy.getSlotTime(1);
        logger.info(" #### nextBlockTm = {}.", nextBlockTm);

        int slot = dbProxy.getSlotAtTime(now);
        logger.info(" #### slot = {}.", slot);

        if( slot == 0) {
            logger.debug("not time yet.");
            return BlockProduceConditionEnum.NOT_TIME_YET;
        }

        int slot2 = dbProxy.getSlotAtTime(now);

        String currentWitName = dbProxy.getScheduledWitness(slot);
        long scheduledTime = dbProxy.getSlotTime(slot);
        logger.info("scheduledTime={} , witnessName={}.", scheduledTime, currentWitName);

        if(!localWitness.contains(currentWitName)) {
            logger.debug("not my turn.");
            return BlockProduceConditionEnum.NOT_MY_TURN;
        }
        logger.debug("now is {} turn to product block.", currentWitName);
        logger.debug("verify whether has privateKey.");

        byte[] publicKey = dbProxy.getWitness(currentWitName).getPublicKey();
        String pubKeyStr = Hex.toHexString(publicKey);
        if(!priKyWitness.containsKey(pubKeyStr)) {
            logger.debug("no private key.");
            return BlockProduceConditionEnum.NO_PRIVATE_KEY;
        }


        String privateKey = priKyWitness.get(pubKeyStr);
        logger.debug("get privateKey :{}", privateKey);

        dbProxy.generateBlock(scheduledTime, currentWitName, privateKey);

        return BlockProduceConditionEnum.PRODUCED;
    }
}
