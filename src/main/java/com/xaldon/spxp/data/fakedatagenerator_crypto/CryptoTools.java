package com.xaldon.spxp.data.fakedatagenerator_crypto;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import org.apache.cxf.common.util.Base64UrlUtility;
import org.json.JSONObject;
import org.spxp.crypto.SpxpSymmetricKeySpec;

public class CryptoTools {

	private CryptoTools() {
		// prevent instantiation
	}
	
	public static JSONObject getECJWK(String keyId, String curve, KeyPair keyPair) {
		ECPublicKey pub = (ECPublicKey)keyPair.getPublic();
		ECPrivateKey priv = (ECPrivateKey)keyPair.getPrivate();
		JSONObject jwkObj = new JSONObject();
		jwkObj.put("kty", "EC");
		jwkObj.put("kid", keyId);
		jwkObj.put("crv", curve);
		jwkObj.put("x", Base64UrlUtility.encode(pub.getW().getAffineX().toByteArray()));
		jwkObj.put("y", Base64UrlUtility.encode(pub.getW().getAffineY().toByteArray()));
		jwkObj.put("d", Base64UrlUtility.encode(priv.getS().toByteArray()));
		return jwkObj;
	}
	
	public static JSONObject getECJWK(String keyId, String curve, PublicKey publicKey) {
		ECPublicKey pub = (ECPublicKey)publicKey;
		JSONObject jwkObj = new JSONObject();
		jwkObj.put("kty", "EC");
		jwkObj.put("kid", keyId);
		jwkObj.put("crv", curve);
		jwkObj.put("x", Base64UrlUtility.encode(pub.getW().getAffineX().toByteArray()));
		jwkObj.put("y", Base64UrlUtility.encode(pub.getW().getAffineY().toByteArray()));
		return jwkObj;
	}
	
	public static JSONObject getSymJWK(SpxpSymmetricKeySpec keySpec, String alg) {
		JSONObject jwkObj = new JSONObject();
		jwkObj.put("kty", "oct");
		jwkObj.put("kid", keySpec.getKeyId());
		jwkObj.put("alg", alg);
		jwkObj.put("k", Base64UrlUtility.encode(keySpec.getSymmetricKey()));
		return jwkObj;
	}

}
