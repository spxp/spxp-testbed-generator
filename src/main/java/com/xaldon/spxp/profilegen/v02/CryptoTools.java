package com.xaldon.spxp.profilegen.v02;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.json.JSONObject;
import org.spxp.crypto.SpxpSymmetricKeySpec;

import com.xaldon.spxp.profilegen.utils.Tools;

public class CryptoTools {

    private static Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();
    
    private static Decoder urlDecoder = Base64.getUrlDecoder();
    
    private CryptoTools() {
        // prevent instantiation
    }
    
    public static String encodeBase64Url(byte[] data) {
        return urlEncoder.encodeToString(data);
    }
    
    public static byte[] decodeBase64Url(String data) {
        return urlDecoder.decode(data);
    }
    
    public static JSONObject getECJWK(String keyId, String curve, KeyPair keyPair) {
        ECPublicKey pub = (ECPublicKey)keyPair.getPublic();
        ECPrivateKey priv = (ECPrivateKey)keyPair.getPrivate();
        JSONObject jwkObj = Tools.newOrderPreservingJSONObject();
        jwkObj.put("kid", keyId);
        jwkObj.put("kty", "EC");
        jwkObj.put("crv", curve);
        jwkObj.put("x", encodeBase64Url(pub.getW().getAffineX().toByteArray()));
        jwkObj.put("y", encodeBase64Url(pub.getW().getAffineY().toByteArray()));
        jwkObj.put("d", encodeBase64Url(priv.getS().toByteArray()));
        return jwkObj;
    }
    
    public static JSONObject getECJWK(String keyId, String curve, PublicKey publicKey) {
        ECPublicKey pub = (ECPublicKey)publicKey;
        JSONObject jwkObj = Tools.newOrderPreservingJSONObject();
        jwkObj.put("kid", keyId);
        jwkObj.put("kty", "EC");
        jwkObj.put("crv", curve);
        jwkObj.put("x", encodeBase64Url(pub.getW().getAffineX().toByteArray()));
        jwkObj.put("y", encodeBase64Url(pub.getW().getAffineY().toByteArray()));
        return jwkObj;
    }
    
    public static JSONObject getSymJWK(SpxpSymmetricKeySpec keySpec, String alg) {
        JSONObject jwkObj = Tools.newOrderPreservingJSONObject();
        jwkObj.put("kid", keySpec.getKeyId());
        jwkObj.put("kty", "oct");
        jwkObj.put("alg", alg);
        jwkObj.put("k", encodeBase64Url(keySpec.getSymmetricKey()));
        return jwkObj;
    }
    
    public static ECPublicKey getECPublicKey(String curve, String encXCoord, String encYCoord) throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException {
        byte[] x = CryptoTools.decodeBase64Url(encXCoord);
        byte[] y = CryptoTools.decodeBase64Url(encYCoord);
        return getECPublicKey(curve, x, y);
    }
    
    public static ECPrivateKey getECPrivateKey(String curve, String encPrivateKey) throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException {
        byte[] priv = CryptoTools.decodeBase64Url(encPrivateKey);
        return getECPrivateKey(curve, priv);
    }
    
    public static ECPublicKey getECPublicKey(String curve, byte[] x, byte[] y) throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException {
        ECPoint pubPoint = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec("sec"+curve.toLowerCase().replace("-", "")+"r1"));
        ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(pubPoint, ecParameters);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return (ECPublicKey) kf.generatePublic(pubSpec);
    }
    
    public static ECPrivateKey getECPrivateKey(String curve, byte[] privateKey) throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException {
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec("sec"+curve.toLowerCase().replace("-", "")+"r1"));
        ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
        ECPrivateKeySpec privateSpec = new ECPrivateKeySpec(new BigInteger(1, privateKey), ecParameters);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return (ECPrivateKey) kf.generatePrivate(privateSpec);
    }

}
