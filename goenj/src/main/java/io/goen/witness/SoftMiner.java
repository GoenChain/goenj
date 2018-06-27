package io.goen.witness;

import com.medici.firestar.core.Block;
import com.medici.firestar.witness.comment.CommentObject;
import com.medici.firestar.witness.comment.CommentVoteObject;
import com.medici.firestar.witness.common.*;

import java.math.BigInteger;
import java.util.List;

public interface SoftMiner {

    /**
     * @return
     */
    GlobalObject getGlobalObject();

    /**
     * @return
     */
    WitnessScheduleObject getWitnessScheduleObject();


    /**
     * @return
     */
    RewardFundObject getRewardFundObject();


    boolean addWitness(WitnessObject witnessObject);
//    boolean delWitness(WitnessObject witnessObject);
//    boolean modWitness(WitnessObject witnessObject);
    WitnessObject getWitness(String witnessName);
    List<WitnessObject> getWitnessListByVotes();


    boolean addAccountObject(AccountObject accountObject);
    boolean delAccountObject(AccountObject accountObject);
    boolean modAccountObject(AccountObject accountObject);
    AccountObject getAccountObject(String accountName);


    boolean addWitVoteObject(WitnessVoteObject witVote);
    boolean delWitVoteObject(WitnessVoteObject witVote);
    boolean modWitVoteObject(WitnessVoteObject witVote);
    WitnessVoteObject getWitVoteObject(String accountName, String witName);

    //TODO -- comment
    List<CommentObject> getCommentListByCashoutTime();

    //TODO -- commentVote
    List<CommentVoteObject> getCommentVoteListByCommentId();



    long headBlockTime();

    int headBlockNum();

    byte[] headBlockId();

    String getScheduledWitness(int slotNum);

    long getSlotTime(int slotNum);

    int getSlotAtTime(long when);

    boolean generateBlock(long when, String witness, String privateKey);

    BigInteger adjustAccountReward(String accName, BigInteger rewards);


    BigInteger createBlockReward();


    void handleSoftMiner();

    void updateContextAndReward(Block block);
}
