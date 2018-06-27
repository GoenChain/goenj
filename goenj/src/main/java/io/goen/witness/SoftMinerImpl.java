package io.goen.witness;



import com.medici.firestar.core.*;
import com.medici.firestar.core.txn.TxnBase;
import com.medici.firestar.crypto.AddressUtil;
import com.medici.firestar.db.BlockStore;
import com.medici.firestar.facade.Goen;
import com.medici.firestar.facade.GoenImpl;
import com.medici.firestar.util.TimeUtils;
import com.medici.firestar.witness.comment.CommentObject;
import com.medici.firestar.witness.comment.CommentRewardContext;
import com.medici.firestar.witness.comment.CommentVoteObject;
import com.medici.firestar.witness.comment.RewardFundContext;
import com.medici.firestar.witness.common.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Component("softMiner")
@Lazy
public class SoftMinerImpl implements SoftMiner {

    private static final Logger logger = LoggerFactory.getLogger("witness");

    private GlobalObject globalObject;
    private WitnessScheduleObject witnessScheduleObject;
    private RewardFundObject rewardFundObject;
    private HashMap<String, AccountObject> accountObjectMap;
    private HashMap<String, WitnessObject> witnessObjectMap;
    private HashMap<String, WitnessVoteObject> witnessVoteObjectMap;

    private HashMap<String, CommentObject> commentObjectHashMap;
    private HashMap<String, CommentVoteObject> commentVoteObjectHashMap;

    @Autowired
    protected PendingState pendingState;

    @Autowired
    private Blockchain blockchain;

    @Autowired
    private BlockStore blockStore;

    @Autowired
    private Goen goen;

    @PostConstruct
    public void init() {

        String init_public_key = WitnessConstants.GOEN_INIT_PUBLIC_KEY_STR;

        accountObjectMap = new HashMap<>(512);
        witnessObjectMap = new HashMap<>(512);
        witnessVoteObjectMap = new HashMap<>(512);

        int initCnt = WitnessConstants.GOEN_NUM_INIT_MINERS;
        for (int i=0; i<initCnt; ++i) {

            String accName = WitnessConstants.GOEN_INIT_MINER_NAME + (i>0?"_" + i:"" );

            AccountObject ao = new AccountObject();
            ao.setAccoutName(accName);
            ao.setBalance(0L);
            accountObjectMap.put(accName, ao);

            WitnessObject wo = new WitnessObject();
            wo.setPublicKey(Hex.decode(init_public_key));
            wo.setOwner(accName);
            wo.setWitnessType(WitnessTypeEnum.MINER);

            String addr = AddressUtil.getAddressFromPubKey(wo.getPublicKey());
            wo.setWitnessAddr(AddressUtil.addressToBytes(addr));

            addWitness(wo);
        }

        // GlobalObj
        globalObject = new GlobalObject();
        globalObject.setCurrentWitness(WitnessConstants.GOEN_INIT_MINER_NAME);
        globalObject.setBlockTime(WitnessConstants.GENESIS_TIME);

        // WitnessScheduleObject
        witnessScheduleObject = new WitnessScheduleObject();
        {
            String[] witArray = new String[WitnessConstants.GOEN_MAX_WITNESSES];
            witArray[0] = WitnessConstants.GOEN_INIT_MINER_NAME;
            witnessScheduleObject.setWitnesses(witArray);
            witnessScheduleObject.setNumWitness(1);

            witnessScheduleObject.setTopWeight(1);
            witnessScheduleObject.setTimeshareWeight(1);        // GOEN 5
            witnessScheduleObject.setMinerWeight(1);
            witnessScheduleObject.setNormalizationFactor(WitnessConstants.GOEN_MAX_WITNESSES);    // GOEN 25
        }


        rewardFundObject = new RewardFundObject();
        {
            rewardFundObject.setName(WitnessConstants.REWARD_FUND_NAME);
            rewardFundObject.setLastUpdate(0L);
            rewardFundObject.setContentConstants(new BigInteger(WitnessConstants.REWARD_CONTENT_CONSTANT));

            rewardFundObject.setPercentCurationRewards(25);
            rewardFundObject.setPercentContentRewards(100);

            rewardFundObject.setRewardBalance(0L);
            rewardFundObject.setRecentClaims(BigInteger.ZERO);

            rewardFundObject.setAuthorRewardCurve(CurveEnum.LINEAR);
            rewardFundObject.setCurationRewardCurve(CurveEnum.SQUARE_ROOT);
        }

        // Comment
        commentObjectHashMap = new HashMap<>(512);
        commentVoteObjectHashMap = new HashMap<>(512);


        testCommentData();
    }

    private void testCommentData() {

        String author = WitnessConstants.GOEN_INIT_MINER_NAME;

        long cid = System.currentTimeMillis();

        CommentObject co = new CommentObject();
        co.setId(cid);
        co.setAuthor(author);
        co.setRewardWeight(100);
        co.setNetShares(1000);
        co.setTotalWeight(BigInteger.valueOf(300L));
        co.setCashoutTime( TimeUtils.millisToSeconds(cid + 20000) );
        commentObjectHashMap.put(cid+"", co);

        {
            String voter = WitnessConstants.GOEN_INIT_MINER_NAME+"_1";
            CommentVoteObject cvo = new CommentVoteObject();
            cvo.setId(cid+1);
            cvo.setCommentId(cid);
            cvo.setAccountName(voter);
            cvo.setWeight(BigInteger.valueOf(200L));
            commentVoteObjectHashMap.put(cvo.getId() + "", cvo);
        }
        {
            String voter = WitnessConstants.GOEN_INIT_MINER_NAME+"_2";
            CommentVoteObject cvo = new CommentVoteObject();
            cvo.setId(cid+2);
            cvo.setCommentId(cid);
            cvo.setAccountName(voter);
            cvo.setWeight(BigInteger.valueOf(100L));
            commentVoteObjectHashMap.put(cvo.getId() + "", cvo);
        }


    }

    @Override
    public GlobalObject getGlobalObject() {
        return globalObject;
    }



    @Override
    public WitnessScheduleObject getWitnessScheduleObject() {
        return witnessScheduleObject;
    }

    @Override
    public RewardFundObject getRewardFundObject() {
        return rewardFundObject;
    }

    @Override
    public boolean addWitness(WitnessObject witnessObject) {
//        String owner = witnessObject.getOwner();
//        witnessObjectMap.putIfAbsent(owner, witnessObject);

        ((BlockchainImpl) blockchain).addWitness(witnessObject);
        return true;
    }

//
//    @Override
//    public boolean delWitness(WitnessObject witnessObject) {
//        return false;
//    }
//
//    @Override
//    public boolean modWitness(WitnessObject witnessObject) {
//        String owner = witnessObject.getOwner();
//        if(witnessObjectMap.containsKey(owner)) {
//            witnessObjectMap.put(owner, witnessObject);
//            return true;
//        }
//        return false;
//    }

    @Override
    public WitnessObject getWitness(String witnessName) {

        // return witnessObjectMap.get(witnessName);
        return ((BlockchainImpl) blockchain).getWitness(witnessName);
    }

    @Override
    public List<WitnessObject> getWitnessListByVotes() {

        return ((BlockchainImpl) blockchain).getAllWitnesses();
    }

    @Override
    public boolean addAccountObject(AccountObject accountObject) {
        String name = accountObject.getAccoutName();
        accountObjectMap.putIfAbsent(name, accountObject);
        return true;
    }

    @Override
    public boolean delAccountObject(AccountObject accountObject) {
        return false;
    }

    @Override
    public boolean modAccountObject(AccountObject accountObject) {
        String name = accountObject.getAccoutName();
        if(accountObjectMap.containsKey(name)) {
            accountObjectMap.put(name, accountObject);
            return true;
        }
        return false;
    }

    @Override
    public AccountObject getAccountObject(String accountName) {
        return accountObjectMap.get(accountName);
    }

    @Override
    public boolean addWitVoteObject(WitnessVoteObject witVote) {
        String accName = witVote.getAccountName();
        String witName = witVote.getWitnessName();
        String key = accName + "_" + witName;
        witnessVoteObjectMap.putIfAbsent(key, witVote);
        return true;
    }

    @Override
    public boolean delWitVoteObject(WitnessVoteObject witVote) {
        String accName = witVote.getAccountName();
        String witName = witVote.getWitnessName();
        String key = accName + "_" + witName;
        if(witnessVoteObjectMap.containsKey(key)) {
            witnessVoteObjectMap.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public boolean modWitVoteObject(WitnessVoteObject witVote) {
        return false;
    }

    @Override
    public WitnessVoteObject getWitVoteObject(String accountName, String witName) {
        String key = accountName + "_" + witName;
        return witnessVoteObjectMap.get(key);
    }

    @Override
    public List<CommentObject> getCommentListByCashoutTime() {
        ArrayList<CommentObject> comList = new ArrayList<>(commentObjectHashMap.values());

        //Open Source Later
        comList.sort(Comparator.comparing(CommentObject::getCashoutTime));
        return comList;
    }

    @Override
    public List<CommentVoteObject> getCommentVoteListByCommentId() {
        ArrayList<CommentVoteObject> comVList = new ArrayList<>(commentVoteObjectHashMap.values());

        //Open Source Later
        comVList.sort(Comparator.comparing(CommentVoteObject::getCommentId));
        return comVList;
    }

    @Override
    public long headBlockTime() {
        return getGlobalObject().getBlockTime();
    }

    @Override
    public int headBlockNum() {
        return getGlobalObject().getHeadBlockNumber();
    }

    @Override
    public byte[] headBlockId() {
        return getGlobalObject().getHeadBlockId();
    }

    @Override
    public String getScheduledWitness(int slotNum) {
        GlobalObject go = getGlobalObject();
        WitnessScheduleObject wso = getWitnessScheduleObject();

        int currentSlot = go.getCurrentSlot() + slotNum;

        String[] witArray = wso.getWitnesses();
        int num = wso.getNumWitness();
        return witArray[currentSlot % num];
    }

    @Override
    public long getSlotTime(int slotNum) {
        if(slotNum == 0) {
            return 0L;
        }

        int interval = WitnessConstants.GOEN_BLOCK_INTERVAL;
        GlobalObject globalObject = getGlobalObject();

        if(headBlockNum() == 0) {
            // n.b. first block is at genesis_time plus one block interval
            long genesisTime = globalObject.getBlockTime();
            return genesisTime + slotNum * interval;
        }

        long headBlockAbSlot = headBlockTime() / interval;
        long headSlotTime = headBlockAbSlot * interval;

        return headSlotTime + slotNum * interval;
    }

    @Override
    public int getSlotAtTime(long when) {
        long firstSlotTime = getSlotTime(1);
     //Open Source Later
        return Math.toIntExact(num) + 1;
    }

    @Override
    public boolean generateBlock(long when, String witness, String privateKey) {
        logger.info("=========================================------------------------");

        Block block = getNewBlockForMining(when, witness, privateKey);
        ImportResult importResult = ((GoenImpl) Goen).addNewMinedBlock(block);
        if (importResult != ImportResult.IMPORTED_BEST) {
            return  false;
        }

        updateContextAndReward(block);

        return true;
    }

    @Override
    public BigInteger adjustAccountReward(String accName, BigInteger rewards) {
        if (rewards.compareTo(BigInteger.ZERO) == -1) {
            return BigInteger.ZERO;
        }
        Repository repository = ((BlockchainImpl) blockchain).getRepository();
        WitnessObject witnessObject = ((BlockchainImpl) blockchain).getWitness(repository, accName);
        repository.addBalance(witnessObject.getWitnessAddr(), rewards);
        return repository.getBalance(witnessObject.getWitnessAddr());
    }

    private BigInteger addUserProfit(String accName, BigInteger rewards) {
        if (rewards.compareTo(BigInteger.ZERO) == -1) {
            return BigInteger.ZERO;
        }
        Repository repository = ((BlockchainImpl) blockchain).getRepository();
        WitnessObject witnessObject = ((BlockchainImpl) blockchain).getWitness(repository, accName);
        repository.addUserProfit(witnessObject.getWitnessAddr(), rewards);
        return repository.getUserProfit(witnessObject.getWitnessAddr());
    }


    @Override
    public BigInteger createBlockReward() {

        logger.info("start produce coin");

        GlobalObject go = getGlobalObject();
        WitnessScheduleObject wso = getWitnessScheduleObject();

        long reward = getRewardByBlock();

        long contentReward = reward * 90/100;
        contentReward = RewardFunds(contentReward);

        long wintessReward = reward - contentReward;

        String witName = go.getCurrentWitness();
        WitnessObject wo = getWitness( witName );
        wintessReward *= WitnessConstants.GOEN_MAX_WITNESSES;

        switch (wo.getWitnessType()) {
            case VOTE_TOPN:
                wintessReward *= wso.getTopWeight();
                break;
            case TIMESHARE:
                wintessReward *= wso.getTimeshareWeight();
                break;
            case MINER:
                wintessReward *= wso.getMinerWeight();
                break;
            default:
                break;
        }
        wintessReward /= wso.getNormalizationFactor();

        long newReward = contentReward + wintessReward;

        go.addTotalSupply(newReward);

        BigInteger finalReward = BigInteger.valueOf(wintessReward);
        adjustAccountReward(witName, finalReward);
        addUserProfit(witName,finalReward);
        return finalReward;
    }


    private long getRewardByBlock() {

        long tmSpan = headBlockTime() - WitnessConstants.GENESIS_TIME;

        int years = Math.toIntExact(tmSpan / WitnessConstants.MAX_SECONDS_PER_YEAR);
        long totalPerYear = 0L;
            //Open Source Later

        long reward = totalPerYear / WitnessConstants.BLOCKS_PER_YEAR;

        return reward;
    }

    /**
     *
     * @param reward
     * @return
     */
    private long RewardFunds(long reward) {
        RewardFundObject rfo = getRewardFundObject();

        long r = reward * rfo.getPercentContentRewards() / 100;

        logger.info("\t\t {}.", r);

        rfo.addRewardBalance( r );
        logger.info("\t\t -- {}.", rfo.getRewardBalance());

        return r;
    }

    @Override
    public void handleSoftMiner() {
        logger.info(".");

        GlobalObject go = getGlobalObject();
        RewardFundObject rfo = getRewardFundObject();
        {
            // RecentClaims --
            rfo.setLastUpdate(headBlockTime());
        }

        RewardFundContext rfc = new RewardFundContext();
        rfc.setRecentClaims(rfo.getRecentClaims());
        rfc.setRewardBalance(rfo.getRewardBalance());

        // cashoutTimecomment
        logger.info("\tComment.");
        List<CommentObject> commentObjectList = getCommentListByCashoutTime();
        for (CommentObject co: commentObjectList) {

            if(co.getCashoutTime() > headBlockTime()) {
                break;
            }

            logger.info("\t Comment, id={}.", co.getId());

            if(co.getNetShares() > 0) {
                BigInteger bigNetShares = BigInteger.valueOf(co.getNetShares());
                BigInteger evaluateRewardCurve = RewardRule.evaluateRewardCurve( bigNetShares, rfo.getAuthorRewardCurve(), rfo.getContentConstants() );

                logger.info("\t\tnetShares={}, ({})  {}.", co.getNetShares(), rfo.getAuthorRewardCurve(), evaluateRewardCurve.toString());

                //
                rfc.addRecentClaims(evaluateRewardCurve);
            }
        }
        logger.info("\tRecentClaims={}", rfc.getRecentClaims().toString());

        //
        logger.info("\tComment.");
        CommentRewardContext crc = new CommentRewardContext();
        for (CommentObject co: commentObjectList) {
            if(co.getCashoutTime() > headBlockTime()) {
                break;
            }

            logger.info("\t Comment, id={}.", co.getId());

            crc.setTotalRewardShares(rfc.getRecentClaims());
            crc.setTotalRewardFund(rfc.getRewardBalance());

            long rewarded = cashoutComment(crc, co);
            rfc.addGoenAwarded( rewarded );
        }

        //
        if(rfc.getGoenAwarded() > 0L) {

            long current = rfo.getRewardBalance();
            rfo.setRewardBalance( current - rfc.getRewardBalance());
            rfo.setRecentClaims( rfc.getRecentClaims() );
        }

        logger.info(".");

    }

    @Override
    public void updateContextAndReward(Block block) {
        logger.info("context:"+block);
        Exception e = new Exception();
        logger.info("context:",e);

//        //
//        updateGlobalData(block.getTimestamp(), block.getWitness());
//
//        //
//        WitnessScheduled.updateWitnessScheduled(this);
//
//        //
//        createBlockReward();
//
//        //
//        handleSoftMiner();
    }

    private void testUpdateAndReward(Block block) {
        //
        updateGlobalData(block.getTimestamp(), block.getWitness());

        //
        WitnessScheduled.updateWitnessScheduled(this);

        //
        createBlockReward();

        //
        handleSoftMiner();
    }

    private long cashoutComment(CommentRewardContext crc, CommentObject co) {
        long rewarded = 0L;

        if(co.getNetShares() <= 0) {
            return rewarded;
        }

        logger.info("\t\tComment[{}] .", co.getId());
        RewardFundObject rfo = getRewardFundObject();

        crc.setRshares( co.getNetShares() );
        crc.setRewardWeight( co.getRewardWeight() );
        crc.setRewardCurve( rfo.getCurationRewardCurve() );
        crc.setContentConstant( rfo.getContentConstants() );

        // content_reward
        logger.info("\t\t.");
        long shareRewarded = getSharesReward(crc);
        if( shareRewarded > 0L ) {
            //
            logger.info("\t\t{}.", shareRewarded);

            //  25%
            BigInteger bigVote = BigInteger.valueOf(shareRewarded).multiply(BigInteger.valueOf(25L)).divide(BigInteger.valueOf(100L));
            long upvoteReward = bigVote.longValue();
            logger.info("\t\t[](25%)={}.", upvoteReward);

            //  75%
            long authorReward = shareRewarded - upvoteReward;
            logger.info("\t\t[](75%)={}.", authorReward);

            //
            long unRewarded = payUpvoteRewards(co, upvoteReward);
            upvoteReward -= unRewarded;
            logger.info("\t\t[](25%)={}.", upvoteReward);

            authorReward += unRewarded;
            logger.info("\t\t[](25%)={}.", authorReward);

            //
            rewarded = authorReward + upvoteReward;

            //
            logger.info("\t\t.");
            adjustAccountReward(co.getAuthor(), BigInteger.valueOf(authorReward));
            addUserProfit(co.getAuthor(), BigInteger.valueOf(authorReward));
        }

        // CommentObject
        logger.info("\t\tCashoutTime.");
        co.setCashoutTime(Long.MAX_VALUE);

        logger.info("\t\tComment[{}] .", co.());
        return rewarded;
    }

    /**
     *
     * @param co
     * @param max_rewards
     * @return
     */
    private long payUpvoteRewards( CommentObject co, long max_rewards) {

        logger.info("\t\t.");
        BigInteger totalW = co.getTotalWeight();
        long unRewarded = max_rewards;

        if(co.getTotalWeight().compareTo(BigInteger.ZERO) > 0) {
//            unRewarded = 0L;
//            max_rewards = 0L;

            List<CommentVoteObject> lst = getCommentVoteListByCommentId();
            boolean bHead = false;
            for(int idx=0; idx<lst.size(); idx++) {
                CommentVoteObject cvo = lst.get(idx);

                //
                if(!bHead && cvo.getCommentId() == co.getId() ) {
                    bHead = true;
                }

                if(bHead && cvo.getCommentId() != co.getId() ) {
                    //
                    break;
                }

                String AccountName = cvo.getAccountName();
                logger.info("\t\t\t{}", AccountName);

                BigInteger weight = cvo.getWeight();
                BigInteger pay = BigInteger.valueOf(max_rewards).multiply(weight).divide(totalW);
                logger.info("\t\t\t({}) * ({}) / ({}) = ({}).", max_rewards,
                        weight.toString(), totalW.toString(), pay.toString());

                long payReward = pay.longValue();
                if( payReward > 0L) {
                    unRewarded -= payReward;

                    logger.info("\t\t\t");
                    //
                    adjustAccountReward(AccountName, BigInteger.valueOf(payReward));
                    addUserProfit(AccountName, BigInteger.valueOf(payReward));
                }
            }
        }

        logger.info("\t\t.");
        return unRewarded;
    }



    private long getSharesReward(CommentRewardContext crc) {
        BigInteger rf = BigInteger.valueOf( crc.getTotalRewardFund() );
        BigInteger total = crc.getTotalRewardShares();

        BigInteger claims = RewardRule.evaluateRewardCurve( BigInteger.valueOf( crc.getRshares() ), crc.getRewardCurve(), crc.getContentConstant());
        logger.info("\t\t\tRshares={}, ({})  {}.", crc.getRshares(), crc.getRewardCurve(), claims.toString());


//        claims = claims.multiply( new BigInteger( crc.getRewardWeight() + "")).divide(BigInteger.valueOf(100L));

        BigInteger pay = rf.multiply(claims).divide(total);

        logger.info("\t\t\t: ({}) * Comment({}) / Comment({}) = ({})", rf.toString(),
                            claims.toString(), total.toString(), pay.toString());

        // pay

        return pay.longValue();
    }

    //TODO --
    private void updateGlobalData(long blockTm, String witnessName) {
        GlobalObject go = getGlobalObject();

        //
        int missed_blocks = 0;
        if( headBlockTime() != 0L )
        {
            missed_blocks = getSlotAtTime( blockTm );
            assert( missed_blocks != 0 );
            missed_blocks--;
            for( int i = 0; i < missed_blocks; ++i )
            {
                String witnessMissedName = getScheduledWitness(i + 1);
                WitnessObject witnessMissed = getWitness(witnessMissedName);
                if( !witnessMissed.getOwner().equals(witnessName) ) {
                    //  miss

                    // miss >
                    //
                }
            }
        }

        //
        go.setBlockTime(blockTm);
        go.setCurrentWitness(witnessName);

        int slot = go.getCurrentSlot();
        go.setCurrentSlot(slot + missed_blocks + 1);

        int blockNum = go.getHeadBlockNumber();
        go.setHeadBlockNumber(blockNum+1);

        logger.info(":{}, Num:{}, Slot:{}, :{}", go.getCurrentWitness()
                , go.getHeadBlockNumber(), go.getCurrentSlot(), go.getBlockTime());

    }

    protected synchronized Block getNewBlockForMining(long timestamp, String witness, String privKey) {
        Block bestBlockchain = blockchain.getBestBlock();
        Block bestPendingState = ((PendingStateImpl) pendingState).getBestBlock();

        logger.info("getNewBlockForMining best blocks: PendingState: " + bestPendingState.getShortDescr() +
            ", Blockchain: " + bestBlockchain.getShortDescr());

        Block newMiningBlock = blockchain.createNewBlock(bestPendingState, getAllPendingTxnBases(), timestamp, witness, privKey);
        return newMiningBlock;
    }

    protected List<TxnBase> getAllPendingTxnBases() {
        PendingStateImpl.TxnBaseSortedSet ret = new PendingStateImpl.TxnBaseSortedSet();
        ret.addAll(pendingState.getPendingTxnBases());
        return new ArrayList<>(ret);
    }

}
