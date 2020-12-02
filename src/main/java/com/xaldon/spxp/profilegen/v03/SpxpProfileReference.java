package com.xaldon.spxp.profilegen.v03;

import org.json.JSONObject;
import org.spxp.crypto.SpxpProfilePublicKey;

import com.xaldon.spxp.profilegen.utils.Tools;

public class SpxpProfileReference {
    
    private String uri;
    
    private SpxpProfilePublicKey profilePublicKey;

    public SpxpProfileReference(String uri, SpxpProfilePublicKey profilePublicKey) {
        super();
        this.uri = uri;
        this.profilePublicKey = profilePublicKey;
    }

    public String getUri() {
        return uri;
    }

    public SpxpProfilePublicKey getProfilePublicKey() {
        return profilePublicKey;
    }

    public JSONObject toJSONObject() {
        JSONObject result = Tools.newOrderPreservingJSONObject();
        result.put("uri", uri);
        if (profilePublicKey != null) {
            result.put("publicKey", CryptoTools.getOrderedPublicJWK(profilePublicKey));
        }
        return result;
    }

}
