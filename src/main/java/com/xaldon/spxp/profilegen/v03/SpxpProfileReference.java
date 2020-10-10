package com.xaldon.spxp.profilegen.v03;

import org.json.JSONObject;
import org.spxp.crypto.SpxpProfilePublicKey;

import com.xaldon.spxp.profilegen.utils.Tools;

public class SpxpProfileReference {
    
    private String uri;
    
    private boolean keySourceDns;
    
    private SpxpProfilePublicKey profilePublicKey;

    public SpxpProfileReference(String uri, boolean keySourceDns, SpxpProfilePublicKey profilePublicKey) {
        super();
        this.uri = uri;
        this.keySourceDns = keySourceDns;
        this.profilePublicKey = profilePublicKey;
    }

    public String getUri() {
        return uri;
    }

    public boolean isKeySourceDns() {
        return keySourceDns;
    }

    public SpxpProfilePublicKey getProfilePublicKey() {
        return profilePublicKey;
    }

    public JSONObject toJSONObject() {
        JSONObject result = Tools.newOrderPreservingJSONObject();
        result.put("uri", uri);
        if(keySourceDns) {
            JSONObject publicKeyDef = Tools.newOrderPreservingJSONObject();
            publicKeyDef.put("src", "dns");
            result.put("publicKey", publicKeyDef);
        } else if (profilePublicKey != null) {
            result.put("publicKey", CryptoTools.getOrderedPublicJWK(profilePublicKey));
        }
        return result;
    }

}
