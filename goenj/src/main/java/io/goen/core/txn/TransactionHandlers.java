package io.goen.core.txn;


import com.medici.firestar.core.BlockchainImpl;
import com.medici.firestar.core.txn.handler.*;

public class TransactionHandlers {
    public static TransactionHandler getTransactionHandler(TxnBase tx, BlockchainImpl blockchain) {
        TxnType txnType = tx.getType();
        switch (txnType) {
            case CONTENT:
                return new ContentHandler();
            case LIKE:
                return new LikeHandler();
            case TXN:
                return new TxnHandler();
            case TIP:
                return new TipHandler();
            case VOTE:
                return new VoteHandler();
            case APPLY:
                return new ApplyHandler(blockchain);
            default:
                return null;
        }
    }
}
