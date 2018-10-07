package io.goen.net.goen.message;

import java.util.HashMap;
import java.util.Map;

public enum GoenMessageCodes {
    STATUS(0x00),
    NEW_BLOCK_HASHES(0x01),
    TRANSACTIONS(0x02),
    GET_BLOCK_HEADERS(0x03),
    BLOCK_HEADERS(0x04),
    GET_BLOCK_BODIES(0x05),
    BLOCK_BODIES(0x06),
    NEW_BLOCK(0x07),
    GET_NODE_DATA(0x08),
    NODE_DATA(0x09);
    private int code;
    private static final Map<Integer, GoenMessageCodes> intToTypeMap = new HashMap<>();

    GoenMessageCodes(int code) {
        this.code = code;
    }

    static {
        for (GoenMessageCodes goenMessage : GoenMessageCodes.values()) {
            intToTypeMap.put(goenMessage.code, goenMessage);
        }
    }
}
