package org.spxp.tools.testbedgen.v03;

import org.json.JSONArray;
import org.json.JSONObject;
import org.spxp.crypto.SpxpCertificatePermission;
import org.spxp.crypto.SpxpConnectKeyPair;
import org.spxp.crypto.SpxpConnectPublicKey;
import org.spxp.crypto.SpxpCryptoException;
import org.spxp.crypto.SpxpCryptoToolsV03;
import org.spxp.crypto.SpxpProfileKeyPair;
import org.spxp.crypto.SpxpProfilePublicKey;
import org.spxp.crypto.SpxpSymmetricKeySpec;
import org.spxp.tools.testbedgen.utils.Tools;

public class CryptoTools {
    
    private CryptoTools() {
        // prevent instantiation
    }
    
    private static String[] KEYPAIR_ORDER = {"kid", "kty", "crv", "x", "d"};
    
    private static String[] PUBLICKEY_ORDER = {"kid", "kty", "crv", "x"};
    
    private static String[] SYMMETRICKEY_ORDER = {"kid", "kty", "alg", "k"};
    
    public static JSONObject getOrderedKeypairJWK(SpxpProfileKeyPair keyPair) {
        return Tools.orderObject(SpxpCryptoToolsV03.getKeypairJWK(keyPair), KEYPAIR_ORDER);
    }
    
    public static JSONObject getOrderedPublicJWK(SpxpProfilePublicKey publicKey) {
        return Tools.orderObject(SpxpCryptoToolsV03.getPublicJWK(publicKey), PUBLICKEY_ORDER);
    }
    
    public static JSONObject getOrderedKeypairJWK(SpxpConnectKeyPair keyPair) {
        return Tools.orderObject(SpxpCryptoToolsV03.getKeypairJWK(keyPair), KEYPAIR_ORDER);
    }
    
    public static JSONObject getOrderedPublicJWK(SpxpConnectPublicKey publicKey) {
        return Tools.orderObject(SpxpCryptoToolsV03.getPublicJWK(publicKey), PUBLICKEY_ORDER);
    }
    
    public static JSONObject getOrderedSymmetricJWK(SpxpSymmetricKeySpec keySpec) {
        return Tools.orderObject(SpxpCryptoToolsV03.getSymmetricJWK(keySpec), SYMMETRICKEY_ORDER);
    }
    
    public static JSONObject createCertificate(SpxpProfilePublicKey authorizedSigningPublicKey, SpxpCertificatePermission[] permissions, SpxpProfileKeyPair profileKeyPair, JSONObject keyCertificate) throws SpxpCryptoException {
        JSONObject result = Tools.newOrderPreservingJSONObject();
        result.put("publicKey", CryptoTools.getOrderedPublicJWK(authorizedSigningPublicKey));
        JSONArray grantArray = new JSONArray();
        for(SpxpCertificatePermission permission : permissions) {
            grantArray.put(permission.getKey());
        }
        result.put("grant", grantArray);
        SpxpCryptoToolsV03.signObject(result, profileKeyPair);
        if(keyCertificate != null) {
            result.getJSONObject("signature").put("key", keyCertificate);
        }
        return result;
    }

}
