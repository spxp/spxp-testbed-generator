package com.xaldon.spxp.data.fakedatagenerator_crypto;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.LinkedList;
import java.util.List;

import org.apache.cxf.common.util.Base64UrlUtility;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.rs.security.jose.jwa.ContentAlgorithm;
import org.apache.cxf.rs.security.jose.jwa.KeyAlgorithm;
import org.apache.cxf.rs.security.jose.jwe.AesGcmContentEncryptionAlgorithm;
import org.apache.cxf.rs.security.jose.jwe.ContentEncryptionProvider;
import org.apache.cxf.rs.security.jose.jwe.EcdhAesWrapKeyEncryptionAlgorithm;
import org.apache.cxf.rs.security.jose.jwe.EcdhDirectKeyJweEncryption;
import org.apache.cxf.rs.security.jose.jwe.JweCompactProducer;
import org.apache.cxf.rs.security.jose.jwe.JweEncryption;
import org.apache.cxf.rs.security.jose.jwe.JweEncryptionProvider;
import org.apache.cxf.rs.security.jose.jwe.JweHeaders;
import org.apache.cxf.rs.security.jose.jwe.JweJsonProducer;
import org.apache.cxf.rs.security.jose.jwe.JweUtils;
import org.apache.cxf.rs.security.jose.jwe.KeyEncryptionProvider;
import org.apache.cxf.rt.security.crypto.CryptoUtils;
import org.json.JSONObject;

public class CryptoTools {

	private CryptoTools() {
		// prevent instantiation
	}
	
	private static SecureRandom secureRandom = new SecureRandom();
	
	public static String encryptECKeyWrap(String payload, ECPublicKey peerPublicKey, String curve) throws Exception
	{
		KeyAlgorithm keyAlgo = KeyAlgorithm.ECDH_ES_A256KW;
		ContentAlgorithm ctAlgo = ContentAlgorithm.A256GCM;
		KeyEncryptionProvider keyEncryption = new EcdhAesWrapKeyEncryptionAlgorithm(
				peerPublicKey,
				curve,
                keyAlgo,
                ctAlgo);
		ContentEncryptionProvider contentEncryption = new AesGcmContentEncryptionAlgorithm(ctAlgo, true);
		JweHeaders headers = new JweHeaders();
		headers.setKeyEncryptionAlgorithm(keyAlgo);
		headers.setContentEncryptionAlgorithm(ctAlgo);
		JweCompactProducer p = new JweCompactProducer(headers, payload);
		return p.encryptWith(new JweEncryption(keyEncryption, contentEncryption));
	}
	
	public static String encryptECDirect(String payload, ECPublicKey peerPublicKey, String curve) throws Exception
	{
		KeyAlgorithm keyAlgo = KeyAlgorithm.ECDH_ES_DIRECT;
		ContentAlgorithm ctAlgo = ContentAlgorithm.A256GCM;
		JweHeaders headers = new JweHeaders();
		headers.setKeyEncryptionAlgorithm(keyAlgo);
		headers.setContentEncryptionAlgorithm(ctAlgo);
		JweCompactProducer p = new JweCompactProducer(headers, payload);
		return p.encryptWith(new EcdhDirectKeyJweEncryption(peerPublicKey, curve, ctAlgo));
	}
	
	public static JSONObject encryptSymmetricJson(JSONObject payload, String extraAAD, List<SpxpSymmetricKeySpec> recipientKeys) throws Exception
	{
		ContentEncryptionProvider contentEncryption = new AesGcmContentEncryptionAlgorithm(ContentAlgorithm.A256GCM, true);
		List<JweEncryptionProvider> jweProviders = new LinkedList<JweEncryptionProvider>();
		List<JweHeaders> perRecipientHeades = new LinkedList<JweHeaders>();
		for(SpxpSymmetricKeySpec recipientKey : recipientKeys)
		{
			jweProviders.add(new JweEncryption(JweUtils.getSecretKeyEncryptionAlgorithm(CryptoUtils.createSecretKeySpec(recipientKey.getSymmetricKey(), "AES"), KeyAlgorithm.A256GCMKW), contentEncryption));
			perRecipientHeades.add(new JweHeaders(recipientKey.getKeyId()));
		}
		JweHeaders sharedUnprotectedHeaders = new JweHeaders();
		sharedUnprotectedHeaders.setKeyEncryptionAlgorithm(KeyAlgorithm.A256GCMKW);
		JweHeaders protectedHeaders = new JweHeaders(ContentAlgorithm.A256GCM);
		JweJsonProducer p = new JweJsonProducer(protectedHeaders,
                sharedUnprotectedHeaders,
                StringUtils.toBytesUTF8(payload.toString()),
                extraAAD == null ? null : StringUtils.toBytesUTF8(extraAAD),
                false);
        String jweJsonOut = p.encryptWith(jweProviders, perRecipientHeades);
		return new JSONObject(jweJsonOut);
	}

	public static byte[] calculateSHA256Digest(byte[] input) throws NoSuchAlgorithmException
	{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input);
    }
	
	public static byte[] generateSymmetricKey(int bitlen)
	{
		if(bitlen % 8 != 0)
		{
			throw new IllegalArgumentException("key bitlen must be multiple of 8");
		}
		byte[] result = new byte[bitlen/8];
		secureRandom.nextBytes(result);
		return result;
	}
	
	public static String generateRandomKeyId(boolean longFormat)
	{
		byte[] result = new byte[longFormat ? 6 : 12];
		secureRandom.nextBytes(result);
		return Base64UrlUtility.encode(result);
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
