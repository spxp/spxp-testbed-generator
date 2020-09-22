package com.xaldon.spxp.profilegen.v03;

import org.json.JSONObject;
import org.spxp.crypto.SpxpProfileKeyPair;

public class SpxpProfileImpersonationKey {
    
    private SpxpProfileKeyPair keyPair;
    
    private JSONObject certificate;

    public SpxpProfileImpersonationKey(SpxpProfileKeyPair keyPair, JSONObject certificate) {
        this.keyPair = keyPair;
        this.certificate = certificate;
    }

    public SpxpProfileKeyPair getKeyPair() {
        return keyPair;
    }
    
    public JSONObject getCertificate() {
        return certificate;
    }

}
