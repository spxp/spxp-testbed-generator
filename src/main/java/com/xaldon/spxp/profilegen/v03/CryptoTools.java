package com.xaldon.spxp.profilegen.v03;

import org.json.JSONArray;
import org.json.JSONObject;
import org.spxp.crypto.SpxpCertificatePermission;
import org.spxp.crypto.SpxpConnectKeyPair;
import org.spxp.crypto.SpxpCryptoException;
import org.spxp.crypto.SpxpCryptoToolsV03;
import org.spxp.crypto.SpxpProfileKeyPair;
import org.spxp.crypto.SpxpSymmetricKeySpec;

import com.xaldon.spxp.profilegen.utils.Tools;

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
	
	public static JSONObject getOrderedPublicJWK(SpxpProfileKeyPair keyPair) {
		return Tools.orderObject(SpxpCryptoToolsV03.getPublicJWK(keyPair), PUBLICKEY_ORDER);
	}
	
	public static JSONObject getOrderedSymmetricJWK(SpxpSymmetricKeySpec keySpec) {
		return Tools.orderObject(SpxpCryptoToolsV03.getSymmetricJWK(keySpec), SYMMETRICKEY_ORDER);
	}
    
    public static JSONObject getOrderedPublicJWK(SpxpConnectKeyPair keyPair) {
        return Tools.orderObject(SpxpCryptoToolsV03.getPublicJWK(keyPair), PUBLICKEY_ORDER);
    }
	
	public static JSONObject createCertificate(SpxpProfileKeyPair authorizedSigningKeyPair, SpxpCertificatePermission[] permissions, SpxpProfileKeyPair profileKeyPair, JSONObject keyCertificate) throws SpxpCryptoException {
		JSONObject result = Tools.newOrderPreservingJSONObject();
		result.put("publicKey", CryptoTools.getOrderedPublicJWK(authorizedSigningKeyPair));
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
