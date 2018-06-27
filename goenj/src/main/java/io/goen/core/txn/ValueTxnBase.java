package io.goen.core.txn;

public interface ValueTxnBase extends TxnBase{
    byte[] getValue();
    byte[] getTo();
}
