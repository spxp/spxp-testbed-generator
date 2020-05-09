package com.xaldon.spxp.profilegen.v03;

import java.util.Arrays;

import org.json.JSONObject;
import org.spxp.crypto.SpxpSymmetricKeySpec;

public class SpxpFriendConnectionData {

	private SpxpProfileData peerProfile;
	
	private boolean[] groupMembership;
	
	private SpxpSymmetricKeySpec issuedReaderKey;
	
	private JSONObject issuedCertificate;

	public SpxpFriendConnectionData(SpxpProfileData peerProfile, boolean[] groupMembership, SpxpSymmetricKeySpec issuedReaderKey, JSONObject issuedCertificate) {
		this.peerProfile = peerProfile;
		this.groupMembership = groupMembership;
		this.issuedReaderKey = issuedReaderKey;
		this.issuedCertificate = issuedCertificate;
	}

	public SpxpProfileData getPeerProfile() {
		return peerProfile;
	}

	public boolean[] getGroupMembership() {
		return groupMembership;
	}
	
	public SpxpSymmetricKeySpec getIssuedReaderKey() {
		return issuedReaderKey;
	}
	
	public JSONObject getIssuedCertificate() {
		return issuedCertificate;
	}

	public void extendGroupsTo(int size) {
		groupMembership = Arrays.copyOf(groupMembership, size);
	}
	
}
