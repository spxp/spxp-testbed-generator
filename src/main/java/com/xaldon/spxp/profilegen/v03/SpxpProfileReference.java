package com.xaldon.spxp.profilegen.v03;

import org.json.JSONObject;
import org.spxp.crypto.SpxpProfileKeyPair;

import com.xaldon.spxp.profilegen.utils.Tools;

public class SpxpProfileReference {
    
    private String uri;
    
    private boolean keySourceDns;
    
    private SpxpProfileKeyPair profileKeyPair;

    public SpxpProfileReference(String uri, boolean keySourceDns, SpxpProfileKeyPair profileKeyPair) {
        super();
        this.uri = uri;
        this.keySourceDns = keySourceDns;
        this.profileKeyPair = profileKeyPair;
    }

    public String getUri() {
        return uri;
    }

    public boolean isKeySourceDns() {
        return keySourceDns;
    }

    public SpxpProfileKeyPair getProfileKeyPair() {
        return profileKeyPair;
    }

    public JSONObject toJSONObject() {
        JSONObject result = Tools.newOrderPreservingJSONObject();
        result.put("uri", uri);
        if(keySourceDns) {
            JSONObject publicKeyDef = Tools.newOrderPreservingJSONObject();
            publicKeyDef.put("src", "dns");
            result.put("publicKey", publicKeyDef);
        } else {
            result.put("publicKey", CryptoTools.getOrderedPublicJWK(profileKeyPair));
        }
        return result;
    }

}
