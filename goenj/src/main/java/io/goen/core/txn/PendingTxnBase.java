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


import com.medici.firestar.core.Transaction;
import com.medici.firestar.util.ByteUtil;

import java.math.BigInteger;
import java.util.Arrays;


public class PendingTxnBase {


    private TxnBase txnBase;

    private long blockNumber;

    public PendingTxnBase(byte[] bytes) {
        parse(bytes);
    }

    public PendingTxnBase(TxnBase txnBase) {
        this(txnBase, 0);
    }

    public PendingTxnBase(TxnBase txnBase, long blockNumber) {
        this.txnBase = txnBase;
        this.blockNumber = blockNumber;
    }

    public TxnBase getTxnBase() {
        return txnBase;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public byte[] getSender() {
        return txnBase.getFrom();
    }

    public byte[] getHash() {
        return txnBase.getHash();
    }

    public byte[] getBytes() {
        byte[] numberBytes = BigInteger.valueOf(blockNumber).toByteArray();
        byte[] txBytes = txnBase.getEncoded();
        byte[] bytes = new byte[1 + numberBytes.length + txBytes.length];

        bytes[0] = (byte) numberBytes.length;
        System.arraycopy(numberBytes, 0, bytes, 1, numberBytes.length);

        System.arraycopy(txBytes, 0, bytes, 1 + numberBytes.length, txBytes.length);

        return bytes;
    }

    private void parse(byte[] bytes) {
        byte[] numberBytes = new byte[bytes[0]];
        byte[] txBytes = new byte[bytes.length - 1 - numberBytes.length];

        System.arraycopy(bytes, 1, numberBytes, 0, numberBytes.length);

        System.arraycopy(bytes, 1 + numberBytes.length, txBytes, 0, txBytes.length);

        this.blockNumber = new BigInteger(numberBytes).longValue();
        this.txnBase = TxnBase.create(txBytes);
    }

    @Override
    public String toString() {
        return "PendingTxnBase [" +
                "  tx=" + txnBase +
                ", blockNumber=" + blockNumber +
                ']';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingTxnBase)) return false;

        PendingTxnBase that = (PendingTxnBase) o;

        return Arrays.equals(getSender(), that.getSender()) &&
                Arrays.equals(txnBase.getNonce(), that.getTxnBase().getNonce());
    }

    @Override
    public int hashCode() {
        return ByteUtil.byteArrayToInt(getSender()) + ByteUtil.byteArrayToInt(txnBase.getNonce());
    }
}
