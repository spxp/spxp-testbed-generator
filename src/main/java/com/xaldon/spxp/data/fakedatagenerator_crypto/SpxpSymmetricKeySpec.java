package com.xaldon.spxp.data.fakedatagenerator_crypto;

public class SpxpSymmetricKeySpec {
	
	private final String keyId;
	
	private final byte[] symmetricKey;
	
	private String jwk;

	public SpxpSymmetricKeySpec(String keyId, byte[] symmetricKey) {
		super();
		this.keyId = keyId;
		this.symmetricKey = symmetricKey;
	}

	public String getKeyId() {
		return keyId;
	}

	public byte[] getSymmetricKey() {
		return symmetricKey;
	}
	
	public String getJWK() {
		if(jwk == null) {
			jwk = CryptoTools.getSymJWK(this, "A256GCM").toString();
		}
		return jwk;
	}

}
