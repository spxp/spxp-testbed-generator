package com.xaldon.spxp.data.fakedatagenerator_crypto;

public class SpxpRoundKey {

	private final String roundId;
	
	private final long validSince;
	
	private final long validBefore;
	
	private final SpxpSymmetricKeySpec roundKey;
	
	private int keyUsage;

	public SpxpRoundKey(String roundId, long validSince, long validBefore, SpxpSymmetricKeySpec roundKey) {
		super();
		this.roundId = roundId;
		this.validSince = validSince;
		this.validBefore = validBefore;
		this.roundKey = roundKey;
		this.keyUsage = 0;
	}
	
	public String getRoundId() {
		return roundId;
	}

	public long getValidSince() {
		return validSince;
	}

	public long getValidBefore() {
		return validBefore;
	}

	public SpxpSymmetricKeySpec getRoundKey() {
		keyUsage++;
		return roundKey;
	}

	public SpxpSymmetricKeySpec getRoundKeySilent() {
		return roundKey;
	}

	public int getKeyUsage() {
		return keyUsage;
	}
	
}
