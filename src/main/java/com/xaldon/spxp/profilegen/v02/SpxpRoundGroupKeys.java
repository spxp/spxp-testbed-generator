package com.xaldon.spxp.profilegen.v02;

import org.spxp.crypto.SpxpSymmetricKeySpec;

public class SpxpRoundGroupKeys {

    private final long validSince;
    
    private final long validBefore;
    
    private final SpxpSymmetricKeySpec[] roundKeys;
    
    private int[] keyUsage;

    public SpxpRoundGroupKeys(long validSince, long validBefore, SpxpSymmetricKeySpec[] roundKeys) {
        super();
        this.validSince = validSince;
        this.validBefore = validBefore;
        this.roundKeys = roundKeys;
        this.keyUsage = new int[roundKeys.length];
    }

    public long getValidSince() {
        return validSince;
    }

    public long getValidBefore() {
        return validBefore;
    }

    public SpxpSymmetricKeySpec getRoundKey(int i) {
        keyUsage[i]++;
        return roundKeys[i];
    }

    public SpxpSymmetricKeySpec getRoundKeySilent(int i) {
        return roundKeys[i];
    }

    public int getKeyUsage(int i) {
        return keyUsage[i];
    }
    
}
