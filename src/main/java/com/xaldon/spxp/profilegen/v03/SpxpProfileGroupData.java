package com.xaldon.spxp.profilegen.v03;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.spxp.crypto.SpxpCryptoToolsV03;
import org.spxp.crypto.SpxpCryptoToolsV03.KeyIdSize;
import org.spxp.crypto.SpxpSymmetricKeySpec;

import com.xaldon.spxp.profilegen.utils.Tools;

public class SpxpProfileGroupData {
	
	private String displayName;
	
	private String groupId;
	
	private boolean virtual;
	
	private LinkedList<SpxpRoundKey> roundKeys = new LinkedList<>();

	public SpxpProfileGroupData(String displayName, String symmetricKeyGroupId, boolean virtual) {
		this.displayName = displayName;
		this.groupId = symmetricKeyGroupId;
		this.virtual = virtual;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getGroupId() {
		return groupId;
	}
	
	public boolean isVirtual() {
		return virtual;
	}
	
	public void generateRoundKeyForPeriod(long validSince, long validBefore) {
		String roundId = SpxpCryptoToolsV03.generateRandomKeyId(KeyIdSize.SHORT);
		String keyId = groupId+"."+roundId;
		roundKeys.add(new SpxpRoundKey(roundId, validSince, validBefore, new SpxpSymmetricKeySpec(keyId, SpxpCryptoToolsV03.generateSymmetricKey(256))));
	}
	
	public List<SpxpRoundKey> getRoundKeys() {
		return Collections.unmodifiableList(roundKeys);
	}
	
	public SpxpRoundKey getRoundKeyForTime(Date d) {
		long ts = d.getTime();
		if(ts >= roundKeys.getFirst().getValidBefore()) {
			throw new RuntimeException("Requesting group key for time in the future. was looking for "+Tools.formatPostsDate(d));
		}
		Iterator<SpxpRoundKey> it = roundKeys.iterator();
		while(it.hasNext()) {
			SpxpRoundKey rk = it.next();
			if(ts >= rk.getValidSince() && ts < rk.getValidBefore()) {
				return rk;
			}
		}
		throw new RuntimeException("Not enough round keys. was looking for "+Tools.formatPostsDate(d)+" but last available key is valid since "+Tools.formatPostsDate(roundKeys.getLast().getValidSince()));
	}
	
	public SpxpRoundKey getRandomRoundKey(Random rand) {
		return roundKeys.get(rand.nextInt(roundKeys.size()));
	}
	
	public long getOldestRoundKeySince() {
		return roundKeys.getLast().getValidSince();
	}
	
	public int getUsedKeysCount() {
		int result = 0;
		for(SpxpRoundKey rk : roundKeys) {
			if(rk.getKeyUsage() > 0) {
				result++;
			}
		}
		return result;
	}

}
