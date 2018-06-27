/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the GoenJ library.
 *
 * The GoenJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The GoenJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the GoenJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package io.goen.core.txn;

import com.medici.firestar.config.BlockchainConfig;
import com.medici.firestar.config.CommonConfig;
import com.medici.firestar.config.SystemProperties;
import com.medici.firestar.core.Block;
import com.medici.firestar.core.BlockchainImpl;
import com.medici.firestar.core.Repository;
import com.medici.firestar.core.myds.Transferable;
import com.medici.firestar.core.txn.handler.TransactionHandler;
import com.medici.firestar.db.BlockStore;
import com.medici.firestar.listener.GoenListener;
import com.medici.firestar.listener.GoenListenerAdapter;
import com.medici.firestar.util.ByteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import static com.medici.firestar.util.BIUtil.*;


public class TxnBaseExecutor {

    private static final Logger logger = LoggerFactory.getLogger("execute");
    private static final Logger stateLogger = LoggerFactory.getLogger("state");

    SystemProperties config;
    CommonConfig commonConfig;
    BlockchainConfig blockchainConfig;

    private TxnBase tx;
    private Repository track;
    private Repository cacheTrack;
    private BlockStore blockStore;
    private boolean readyToExecute = false;
    private String execError;

    private byte[] coinbase;
    private Block currentBlock;

    private final GoenListener listener;

    private ByteArraySet touchedAccounts = new ByteArraySet();

    private BlockchainImpl blockchain;

    public TxnBaseExecutor(TxnBase tx, byte[] coinbase, Repository track, BlockStore blockStore, Block currentBlock, BlockchainImpl blockchain) {
        this(tx, coinbase, track, blockStore,currentBlock, new GoenListenerAdapter(), blockchain);
    }

    public TxnBaseExecutor(TxnBase tx, byte[] coinbase, Repository track, BlockStore blockStore,
        Block currentBlock,
        GoenListener listener, BlockchainImpl blockchain) {

        this.tx = tx;
        this.coinbase = coinbase;
        this.track = track;
        this.cacheTrack = track.startTracking();
        this.blockStore = blockStore;
        this.currentBlock = currentBlock;
        this.listener = listener;
        this.blockchain = blockchain;
        withCommonConfig(CommonConfig.getDefault());
    }

    public TxnBaseExecutor withCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
        this.config = commonConfig.systemProperties();
        this.blockchainConfig = config.getBlockchainConfig().getConfigForBlock(currentBlock.getNumber());
        return this;
    }

    private void execError(String err) {
        logger.warn(err);
        execError = err;
    }

    /**
     */
    public void init() {
        if (!(tx instanceof Transferable)) {
            TransactionHandler transactionHandler = TransactionHandlers.getTransactionHandler(tx, blockchain);
            transactionHandler.handle(tx);
        } else {
            BigInteger reqNonce = track.getNonce(tx.getFrom());
            BigInteger txNonce = toBI(tx.getNonce());
            if (isNotEqual(reqNonce, txNonce)) {
                execError(String.format("Invalid nonce: required: %s , tx.nonce: %s", reqNonce, txNonce));
                return;
            }

            BigInteger totalCost = toBI(((ValueTxnBase) tx).getValue());
            BigInteger senderBalance = track.getBalance(tx.getFrom());

            if (!isCovers(senderBalance, totalCost)) {

                execError(String.format("Not enough cash: Require: %s, Sender cash: %s", totalCost, senderBalance));

                return;
            }

            readyToExecute = true;
        }
    }

    public void execute() {
        call();
    }

    private void call() {
        if (!readyToExecute){
            return;
        }
        try {
            if(tx.getType() == TxnType.TXN || tx.getType() == TxnType.TIP){

                byte[] targetAddress = ((ValueTxnBase)tx).getTo();
                BigInteger endowment = toBI(((ValueTxnBase)tx).getValue());
                transfer(cacheTrack, tx.getFrom(), targetAddress, endowment);
                logger.debug("transfer success {}", Hex.toHexString(tx.getHash()));
                touchedAccounts.add(targetAddress);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            execError("transfer error");
        }

    }

    public void go() {
        if (!readyToExecute) {
            return;
        }
        try {
            cacheTrack.commit();
        }
        catch (Exception e) {
            rollback();
            e.printStackTrace();
            execError("transfer error");
        }
    }

    public void finalization() {
        if (!readyToExecute) {
            return;
        }
        listener.onTxnBaseExecuted(tx);
    }

    private void rollback() {

        cacheTrack.rollback();

        // remove touched account

        if(tx.getType() == TxnType.TXN || tx.getType() == TxnType.TIP) {
            touchedAccounts.remove(
                    ((ValueTxnBase) tx).getTo());
        }
    }

    public String getExecError() {
        return execError;
    }
}
