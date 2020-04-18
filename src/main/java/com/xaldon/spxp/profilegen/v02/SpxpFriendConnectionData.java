package com.xaldon.spxp.profilegen.v02;

import java.util.Arrays;

public class SpxpFriendConnectionData {

	private SpxpProfileData peerProfile;
	
	private boolean[] groupMembership;
	
	private byte[] sharedSecret;

	public SpxpFriendConnectionData(SpxpProfileData peerProfile, boolean[] groupMembership, byte[] sharedSecret) {
		this.peerProfile = peerProfile;
		this.groupMembership = groupMembership;
		this.sharedSecret = sharedSecret;
	}

	public SpxpProfileData getPeerProfile() {
		return peerProfile;
	}

	public boolean[] getGroupMembership() {
		return groupMembership;
	}
	
	public byte[] getSharedSecret() {
		return sharedSecret;
	}

	public void extendGroupsTo(int size) {
		groupMembership = Arrays.copyOf(groupMembership, size);
	}
	
}
