package io.goen.core.txn;

import java.util.HashMap;
import java.util.Map;

public enum  TxnType {
    TXN(0x1), TIP(0x2), LIKE(0x3), APPLY(0x4), VOTE(0x5), CONTENT(0x6), UNKNOWN(0x0);
    TxnType(int code) {
        this.code = code;
    }

    private int code;

    private static final Map<Integer, TxnType> intToTypeMap = new HashMap<>();

    static {
        for (TxnType type : TxnType.values()) {
            intToTypeMap.put(type.code, type);
        }
    }

    public static TxnType fromInt(int code) {
        TxnType type = intToTypeMap.get(code);
        if (type == null) {
            return TxnType.UNKNOWN;
        }
        return type;
    }

    public byte asByte() {
        return (byte) code;
    }
}
