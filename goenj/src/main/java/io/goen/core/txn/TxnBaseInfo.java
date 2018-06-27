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


import com.medici.firestar.util.RLP;
import com.medici.firestar.util.RLPItem;
import com.medici.firestar.util.RLPList;

import java.math.BigInteger;


public class TxnBaseInfo {
    TxnBase txnBase;
    byte[] blockHash;
    // user for pending transaction
    byte[] parentBlockHash;
    int index;

    public TxnBaseInfo(TxnBase txnBase, byte[] blockHash, int index) {
        this.txnBase = txnBase;
        this.blockHash = blockHash;
        this.index = index;
    }

    /**
     * Creates a pending tx info
     */
    public TxnBaseInfo(TxnBase txnBase) {
        this.txnBase = txnBase;
    }

    public TxnBaseInfo(byte[] rlp) {
        RLPList params = RLP.decode2(rlp);
        RLPList txInfo = (RLPList) params.get(0);
        RLPList receiptRLP = (RLPList) txInfo.get(0);
        RLPItem blockHashRLP  = (RLPItem) txInfo.get(1);
        RLPItem indexRLP = (RLPItem) txInfo.get(2);
        txnBase = TxnBase.create(receiptRLP.getRLPData());

        blockHash = blockHashRLP.getRLPData();
        if (indexRLP.getRLPData() == null)
            index = 0;
        else
            index = new BigInteger(1, indexRLP.getRLPData()).intValue();
    }

    public void setTxnBase(TxnBase tx){
        this.txnBase = tx;
    }

    /* [receipt, blockHash, index] */
    public byte[] getEncoded() {
        byte[] receiptRLP = this.txnBase.getEncoded();
        byte[] blockHashRLP = RLP.encodeElement(blockHash);
        byte[] indexRLP = RLP.encodeInt(index);
        byte[] rlpEncoded = RLP.encodeList(receiptRLP, blockHashRLP, indexRLP);
        return rlpEncoded;
    }

    public TxnBase getTxnBase() {
        return txnBase;
    }

    public byte[] getBlockHash() { return blockHash; }

    public byte[] getParentBlockHash() {
        return parentBlockHash;
    }

    public void setParentBlockHash(byte[] parentBlockHash) {
        this.parentBlockHash = parentBlockHash;
    }

    public int getIndex() { return index; }

    public boolean isPending() {
        return blockHash == null;
    }
}
