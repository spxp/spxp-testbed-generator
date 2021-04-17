package org.spxp.tools.testbedgen.v03;

import java.util.Arrays;

import org.json.JSONObject;
import org.spxp.crypto.SpxpSymmetricKeySpec;

public class SpxpFriendConnectionData {

    private SpxpProfileData peerProfile;
    
    private boolean[] groupMembership;
    
    private SpxpSymmetricKeySpec issuedReaderKey;
    
    private JSONObject issuedCertificate;
    
    private SpxpSymmetricKeySpec grantedReaderKey;
    
    private JSONObject grantedCertificate;

    public SpxpFriendConnectionData(SpxpProfileData peerProfile, boolean[] groupMembership) {
        this.peerProfile = peerProfile;
        this.groupMembership = groupMembership;
    }

    public SpxpProfileData getPeerProfile() {
        return peerProfile;
    }

    public boolean[] getGroupMembership() {
        return groupMembership;
    }
    
    public void setIssuedReaderKeyAndCertificate(SpxpSymmetricKeySpec issuedReaderKey, JSONObject issuedCertificate) {
        this.issuedReaderKey = issuedReaderKey;
        this.issuedCertificate = issuedCertificate;
    }
    
    public SpxpSymmetricKeySpec getIssuedReaderKey() {
        return issuedReaderKey;
    }
    
    public JSONObject getIssuedCertificate() {
        return issuedCertificate;
    }
    
    public void setGrantedReaderKeyAndCertificate(SpxpSymmetricKeySpec grantedReaderKey, JSONObject grantedCertificate) {
        this.grantedReaderKey = grantedReaderKey;
        this.grantedCertificate = grantedCertificate;
    }
    
    public SpxpSymmetricKeySpec getGrantedReaderKey() {
        return grantedReaderKey;
    }
    
    public JSONObject getGrantedCertificate() {
        return grantedCertificate;
    }

    public void extendGroupsTo(int size) {
        groupMembership = Arrays.copyOf(groupMembership, size);
    }
    
}
