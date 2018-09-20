package io.goen.util;

import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class TestFastByteComparisons {
    @Test
    public void testEquals() {
        Assert.assertFalse(FastByteComparisons.compare(Hex.decode("9f74b9ede111f097a8e90e0bf7413b7d1139d8a1c5b20479151c82e4af18cb58"), Hex.decode("e91104e590fe97a6b5fc9ec298155691be63df67bed875812e79b7d9a8ae2724")));
        Assert.assertFalse(FastByteComparisons.compare(Hex.decode("d45353a6d7f737ad89d06a6a3b1cf90f900936a41a1935bf876a720038da3398"), Hex.decode("e91104e590fe97a6b5fc9ec298155691be63df67bed875812e79b7d9a8ae2724")));
    }
}
