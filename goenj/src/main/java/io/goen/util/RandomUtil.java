package io.goen.util;

import java.security.SecureRandom;

public class RandomUtil {
    private static SecureRandom random = new SecureRandom();
    public static SecureRandom getRandom() {
        return random;
    }
}
