package com.xaldon.spxp.profilegen.v02;

import java.security.KeyPair;

public class SpxpECKeySpec {
    
    private final String keyId;
    
    private final String keyCurve;
    
    private final KeyPair keyPair;
    
    public SpxpECKeySpec(String keyId, String keyCurve, KeyPair keyPair) {
        this.keyId = keyId;
        this.keyCurve = keyCurve;
        this.keyPair = keyPair;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getKeyCurve() {
        return keyCurve;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

}
