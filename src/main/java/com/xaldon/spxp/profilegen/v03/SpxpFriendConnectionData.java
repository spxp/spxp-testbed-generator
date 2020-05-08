package com.xaldon.spxp.profilegen.v03;

import java.util.Arrays;

import org.spxp.crypto.SpxpSymmetricKeySpec;

public class SpxpFriendConnectionData {

	private SpxpProfileData peerProfile;
	
	private boolean[] groupMembership;
	
	private SpxpSymmetricKeySpec readerKey;

	public SpxpFriendConnectionData(SpxpProfileData peerProfile, boolean[] groupMembership, SpxpSymmetricKeySpec readerKey) {
		this.peerProfile = peerProfile;
		this.groupMembership = groupMembership;
		this.readerKey = readerKey;
	}

	public SpxpProfileData getPeerProfile() {
		return peerProfile;
	}

	public boolean[] getGroupMembership() {
		return groupMembership;
	}
	
	public SpxpSymmetricKeySpec getReaderKey() {
		return readerKey;
	}

	public void extendGroupsTo(int size) {
		groupMembership = Arrays.copyOf(groupMembership, size);
	}
	
}
