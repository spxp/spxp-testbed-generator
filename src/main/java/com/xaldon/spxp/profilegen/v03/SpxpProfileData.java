package com.xaldon.spxp.profilegen.v03;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spxp.crypto.SpxpCertificatePermission;
import org.spxp.crypto.SpxpCryptoException;
import org.spxp.crypto.SpxpCryptoToolsV03;
import org.spxp.crypto.SpxpProfileKeyPair;
import org.spxp.crypto.SpxpSymmetricKeySpec;

import com.xaldon.spxp.profilegen.utils.Tools;

public class SpxpProfileData {
	
	private Random rand;

	private String profileUri;

	private String profileName;
	
	private String fullName;
    
    private String shortInfo;
    
    private String publicShortInfo;
	
	private String about;
	
	private String gender;
	
	private String email;
	
	private String birthDayAndMonth;
	
	private String birthYear;
	
	private String hometown;
	
	private String location;
	
	private String latitude;
	
	private String longitude;
	
	private String profilePhoto;
	
	private SpxpProfileKeyPair profileKeyPair;
	
	private ArrayList<SpxpProfileGroupData> groups;
	
	private int nonVirtualGroupsCount;
	
	private int targetFriendCount;
	
	private int actualFriendCount;
	
	private LinkedHashMap<String, SpxpFriendConnectionData> friendConnections = new LinkedHashMap<>();
	
	private ArrayList<String> friendConnectionKeys;
	
	private HashMap<Integer, LinkedList<Integer>> groupInGroupMemberships = new HashMap<>();
	
	private LinkedList<SpxpPost> posts = new LinkedList<>();
	
	private LinkedList<SpxpProfileImpersonationKey> allImpersonationKeys = new LinkedList<>();
	
	private ArrayList<SpxpProfileImpersonationKey> extraPostSigningImpersonationKeys = new ArrayList<>();
	
	private ArrayList<SpxpProfileImpersonationKey> extraFriendsSigningImpersonationKeys = new ArrayList<>();

	public SpxpProfileData(
			Random rand,
			String baseUri,
			String profileName,
			String fullName,
            String shortInfo,
            String publicShortInfo,
			String about,
			String gender,
			String email,
			String birthDayAndMonth,
			String birthYear,
			String hometown,
			String location,
			String latitude,
			String longitude,
			String profilePhoto,
			SpxpProfileKeyPair profileKeyPair,
			ArrayList<SpxpProfileGroupData> groups,
			int targetFriendCount) throws SpxpCryptoException {
		this.rand = rand;
		this.profileName = profileName;
		this.profileUri = baseUri + profileName;
		this.fullName = fullName;
		this.shortInfo = shortInfo;
        this.publicShortInfo = publicShortInfo;
        this.about = about;
		this.gender = gender;
		this.email = email;
		this.birthDayAndMonth = birthDayAndMonth;
		this.birthYear = birthYear;
		this.hometown = hometown;
		this.location = location;
		this.latitude = latitude;
		this.longitude = longitude;
		this.profilePhoto = profilePhoto;
		this.profileKeyPair = profileKeyPair;
		this.groups = groups;
		this.nonVirtualGroupsCount = groups.size();
		this.targetFriendCount = targetFriendCount;
		// if we have more then 1 group, simulate that all other groups are a member of the first group
		if(groups.size() > 1) {
			for(int i = 1; i < groups.size(); i++) {
				addGroupInGroupMembership(i, 0);
			}
		}
		// create multi level keys and certificates
		createImpersonationKeys();
	}
	
	private void createImpersonationKeys() throws SpxpCryptoException {
		SpxpCertificatePermission[] caPermissions = new SpxpCertificatePermission[] {
				SpxpCertificatePermission.CA,
				SpxpCertificatePermission.GRANT,
				SpxpCertificatePermission.FRIENDS,
				SpxpCertificatePermission.IMPERSONATE,
				SpxpCertificatePermission.POST };
		SpxpCertificatePermission[] grantPermissions = new SpxpCertificatePermission[] {
				SpxpCertificatePermission.GRANT,
				SpxpCertificatePermission.FRIENDS,
				SpxpCertificatePermission.IMPERSONATE,
				SpxpCertificatePermission.POST };
		SpxpCertificatePermission[] ipcPermissions = new SpxpCertificatePermission[] {
				SpxpCertificatePermission.IMPERSONATE,
				SpxpCertificatePermission.POST };
		SpxpCertificatePermission[] fPermissions = new SpxpCertificatePermission[] {
				SpxpCertificatePermission.FRIENDS };
		// fake root authority
		SpxpProfileImpersonationKey rootAuthority = new SpxpProfileImpersonationKey(profileKeyPair, null);
		// directly signed by profile key pair
		this.extraPostSigningImpersonationKeys.add(createImpersonationKey(ipcPermissions, rootAuthority));
		this.extraFriendsSigningImpersonationKeys.add(createImpersonationKey(fPermissions, rootAuthority));
		// signed by a key that has permission to GRANT i-p-c permissions
		SpxpProfileImpersonationKey grantKeyDirect = createImpersonationKey(grantPermissions, rootAuthority);
		this.extraPostSigningImpersonationKeys.add(createImpersonationKey(ipcPermissions, grantKeyDirect));
		this.extraFriendsSigningImpersonationKeys.add(createImpersonationKey(fPermissions, grantKeyDirect));
		// signed by a key that has permission to GRANT i-p-c permissions from a CA
		SpxpProfileImpersonationKey caImpersonateKey = createImpersonationKey(caPermissions, rootAuthority);
		SpxpProfileImpersonationKey grantKeyCA = createImpersonationKey(grantPermissions, caImpersonateKey);
		this.extraPostSigningImpersonationKeys.add(createImpersonationKey(ipcPermissions, grantKeyCA));
		this.extraFriendsSigningImpersonationKeys.add(createImpersonationKey(fPermissions, grantKeyCA));
	}
	
	private SpxpProfileImpersonationKey createImpersonationKey(SpxpCertificatePermission[] permissions, SpxpProfileImpersonationKey parentAuthority) throws SpxpCryptoException {
		SpxpProfileKeyPair keyPair = SpxpCryptoToolsV03.generateProfileKeyPair();
		JSONObject certificate = CryptoTools.createCertificate(keyPair, permissions, parentAuthority.getKeyPair(), parentAuthority.getCertificate());
		SpxpProfileImpersonationKey result = new SpxpProfileImpersonationKey(keyPair, certificate);
		this.allImpersonationKeys.add(result);
		return result;
	}
	
	public void addGroupInGroupMembership(int groupId, int memberOfGroupId) {
		if(!groupInGroupMemberships.containsKey(groupId)) {
			groupInGroupMemberships.put(groupId, new LinkedList<>());
		}
		groupInGroupMemberships.get(groupId).add(memberOfGroupId);
	}
	
	public List<Integer> getGroupsAGroupIsMemberOf(int groupId) {
		return groupInGroupMemberships.get(groupId);
	}

	public String getProfileName() {
		return profileName;
	}

	public String getProfileUri() {
		return profileUri;
	}

	public String getFullName() {
		return fullName;
	}

    public String getShortInfo() {
        return shortInfo;
    }

    public String getPublicShortInfo() {
        return publicShortInfo;
    }

	public String getAbout() {
		return about;
	}

	public String getGender() {
		return gender;
	}

	public String getEmail() {
		return email;
	}

	public String getBirthDayAndMonth() {
		return birthDayAndMonth;
	}

	public String getBirthYear() {
		return birthYear;
	}

	public String getHometown() {
		return hometown;
	}

	public String getLocation() {
		return location;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public String getProfilePhoto() {
		return profilePhoto;
	}

	public SpxpProfileKeyPair getProfileKeyPair() {
		return profileKeyPair;
	}

	public ArrayList<SpxpProfileGroupData> getGroups() {
		return groups;
	}
	
	public void extendGroupsByVirtualGroups(int basedOnGroup, int additionalGroupCount) {
		SpxpProfileGroupData base = groups.get(basedOnGroup);
		for(int i = 0; i < additionalGroupCount; i++) {
			groups.add(new SpxpProfileGroupData(base.getDisplayName()+" (virt "+i+")", base.getGroupId()+"-virt_"+i, true));
		}
		for(SpxpFriendConnectionData fcd : friendConnections.values()) {
			fcd.extendGroupsTo(groups.size());
		}
	}

	public int getTargetFriendCount() {
		return targetFriendCount;
	}

	public int getActualFriendCount() {
		return actualFriendCount;
	}
	
	public boolean wantsAdditionalFriends() {
		return actualFriendCount < targetFriendCount;
	}
	
	public boolean isFriend(SpxpProfileData peer) {
		return friendConnections.containsKey(peer.getProfileName());
	}

	public void addFriend(SpxpProfileData peer) throws SpxpCryptoException {
		// assign peer profile to random selection of publishing groups
		if(nonVirtualGroupsCount != groups.size()) {
			throw new RuntimeException("Friend assignment must be finished before virtual group generation");
		}
		boolean[] groupMembership = new boolean[groups.size()];
		groupMembership[0] = true;
		for(int i = 1; i <  groups.size(); i++) {
			if(rand.nextInt(2) == 0) {
				groupMembership[i] = true;
			}
		}
		friendConnections.put(peer.getProfileName(), new SpxpFriendConnectionData(peer, groupMembership));
		actualFriendCount++;
	}
	
	public void issueReaderKeyAndCertificateToAllConnected() throws SpxpCryptoException {
		for(SpxpFriendConnectionData fcd : friendConnections.values()) {
			SpxpProfileData peer = fcd.getPeerProfile();
			// create reader key for peer profile
			byte[] readerKey = SpxpCryptoToolsV03.generateSymmetricKey(256);
			String readerKid = profileName+"-readerkey-for-"+peer.getProfileName();
			SpxpSymmetricKeySpec issuedReaderKey = new SpxpSymmetricKeySpec(readerKid, readerKey);
			// grant publishing permissions to own posts with 75% probability
			JSONObject issuedCertificate = null;
			if(rand.nextInt(4) > 0) {
				SpxpCertificatePermission[] pcPermissions = new SpxpCertificatePermission[] {
						SpxpCertificatePermission.POST };
				issuedCertificate = CryptoTools.createCertificate(peer.getProfileKeyPair(), pcPermissions, profileKeyPair, null);
			}
			fcd.setIssuedReaderKeyAndCertificate(issuedReaderKey, issuedCertificate);
			peer.provideConnectionData(this, issuedReaderKey, issuedCertificate);
		}
	}
	
	public void provideConnectionData(SpxpProfileData peer, SpxpSymmetricKeySpec grantedReaderKey, JSONObject grantedCertificate) {
		friendConnections.get(peer.getProfileName()).setGrantedReaderKeyAndCertificate(grantedReaderKey, grantedCertificate);
	}

	public Collection<SpxpFriendConnectionData> getFriendConnections() {
		return Collections.unmodifiableCollection(friendConnections.values());
	}
	
	public SpxpFriendConnectionData getRandomFriendConnection() {
		if(friendConnectionKeys == null) {
			friendConnectionKeys = new ArrayList<>(friendConnections.keySet());
		}
		String randomFriendProfileName = friendConnectionKeys.get(rand.nextInt(friendConnectionKeys.size()));
		return friendConnections.get(randomFriendProfileName);
	}
	
	public List<Integer> getGroupsMembersOfGroup(int grp) {
		return groupInGroupMemberships.get(grp);
	}
	
	public int getUsedGroupKeysCount() {
		int result = 0;
		for(SpxpProfileGroupData grp : groups) {
			result += grp.getUsedKeysCount();
		}
		return result;
	}
	
	public void addPost(SpxpPost newPost) {
		posts.add(newPost);
	}
	
	public Date getOldestPostCreateDate() {
		Date d = null;
		for(SpxpPost p : posts) {
			if(d == null || p.getCreateDate().compareTo(d) < 0) {
				d = p.getCreateDate();
			}
		}
		return d;
	}

	public void writePrivateData(File profilesDir) throws Exception {
		JSONObject profileObj = Tools.newOrderPreservingJSONObject();
		// basics
		profileObj.put("profileUri", profileUri);
		profileObj.put("profileKeyPair", CryptoTools.getOrderedKeypairJWK(profileKeyPair));
		// impersonationKeys
		JSONArray impersonationKeysArray = new JSONArray();
		for(SpxpProfileImpersonationKey pik : allImpersonationKeys) {
			JSONObject pikObj = Tools.newOrderPreservingJSONObject();
			pikObj.put("key", CryptoTools.getOrderedKeypairJWK(pik.getKeyPair()));
			pikObj.put("certificate", pik.getCertificate());
			impersonationKeysArray.put(pikObj);
		}
		profileObj.put("impersonationKeys", impersonationKeysArray);
		// connections
		JSONArray connectionsArray = new JSONArray();
		for(SpxpFriendConnectionData fcd : friendConnections.values()) {
			JSONObject connectionObj = Tools.newOrderPreservingJSONObject();
			connectionObj.put("profileUri", fcd.getPeerProfile().getProfileUri());
			connectionObj.put("issuedReaderKey", CryptoTools.getOrderedSymmetricJWK(fcd.getIssuedReaderKey()));
			if(fcd.getIssuedCertificate() != null) {
				connectionObj.put("issuedCertificate", fcd.getIssuedCertificate());
			}
			connectionObj.put("grantedReaderKey", CryptoTools.getOrderedSymmetricJWK(fcd.getGrantedReaderKey()));
			if(fcd.getGrantedCertificate() != null) {
				connectionObj.put("grantedCertificate", fcd.getGrantedCertificate());
			}
			JSONArray connectionGroupsArray = new JSONArray();
			boolean[] groupMembership = fcd.getGroupMembership();
			for(int i = 0; i < groups.size(); i++) {
				if(groupMembership[i]) {
					connectionGroupsArray.put(groups.get(i).getGroupId());
				}
			}
			connectionObj.put("allGroups", connectionGroupsArray);
			connectionsArray.put(connectionObj);
		}
		profileObj.put("connections", connectionsArray);
		// groups
		JSONArray groupsArray = new JSONArray();
		for(SpxpProfileGroupData grp : groups) {
			JSONObject groupObj = Tools.newOrderPreservingJSONObject();
			groupObj.put("displayName", grp.getDisplayName());
			groupObj.put("groupId", grp.getGroupId());
			groupObj.put("virtual", grp.isVirtual());
			JSONArray roundKeys = new JSONArray();
			for(SpxpRoundKey rk : grp.getRoundKeys()) {
				JSONObject roundKey = Tools.newOrderPreservingJSONObject();
				roundKey.put("roundId", rk.getRoundId());
				roundKey.put("validSince", Tools.formatPostsDate(rk.getValidSince()));
				roundKey.put("validBefore", Tools.formatPostsDate(rk.getValidBefore()));
				roundKey.put("key", rk.getRoundKeyJwkSilent());
				roundKeys.put(roundKey);
			}
			groupObj.put("roundKeys", roundKeys);
			groupsArray.put(groupObj);
		}
		profileObj.put("groups", groupsArray);
		// group memberships
		JSONArray groupMembershipsArray = new JSONArray();
		for(int i = 0; i < groups.size(); i++) {
			String groupId = groups.get(i).getGroupId();
			List<Integer> memberOf = groupInGroupMemberships.get(i);
			if(memberOf != null) {
				for(int g : memberOf) {
					JSONObject membershipObj = Tools.newOrderPreservingJSONObject();
					membershipObj.put("groupId", groupId);
					membershipObj.put("memberOf", groups.get(g).getGroupId());
					groupMembershipsArray.put(membershipObj);
				}
			}
		}
		profileObj.put("groupMemberships", groupMembershipsArray);
		// write result
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(profilesDir, profileName+".json")), StandardCharsets.UTF_8) ) {
			profileObj.write(out, 4, 0);
		}
	}
	
	public void writeSpxpProfile(File targetDir, File imageSourceDir) throws Exception {
		JSONObject profileObj = Tools.newOrderPreservingJSONObject();
		ArrayList<JSONObject> privateData = new ArrayList<>();
		for(int i = 0; i < nonVirtualGroupsCount; i++) {
			privateData.add(Tools.newOrderPreservingJSONObject());
		}
		profileObj.put("ver", "0.3");
		profileObj.put("name", fullName);
        addSpxpElement(profileObj, privateData, "shortInfo", shortInfo);
        if(!profileObj.has("shortInfo")) {
            profileObj.put("shortInfo", publicShortInfo);
        }
		addSpxpElement(profileObj, privateData, "about", about);
		addSpxpElement(profileObj, privateData, "gender", gender);
		addSpxpElement(profileObj, privateData, "website", "https://example.com");
		addSpxpElement(profileObj, privateData, "email", email);
		addSpxpElement(profileObj, privateData, "birthDayAndMonth", birthDayAndMonth);
		addSpxpElement(profileObj, privateData, "birthYear", birthYear);
		addSpxpElement(profileObj, privateData, "hometown", hometown);
		addSpxpElement(profileObj, privateData, "location", location);
		JSONObject coordinatesObj = Tools.newOrderPreservingJSONObject();
		coordinatesObj.put("latitude", latitude);
		coordinatesObj.put("longitude", longitude);
		addSpxpElement(profileObj, privateData, "coordinates", coordinatesObj);
		if(rand.nextInt(2) == 0) {
			profileObj.put("profilePhoto", encryptProfilePhoto(profilePhoto, imageSourceDir, targetDir));
		} else {
			profileObj.put("profilePhoto", "images/"+profilePhoto);
		}
		profileObj.put("friendsEndpoint", "friends/"+profileName);
		profileObj.put("postsEndpoint", "posts/_read-posts.php?profile="+profileName);
		profileObj.put("keysEndpoint", "keys/_read-keys.php?profile="+profileName);
		JSONArray privateArray = new JSONArray();
		for(int i = 0; i < privateData.size(); i++) {
			JSONObject p = privateData.get(i);
			SpxpProfileGroupData grp = groups.get(i);
			SpxpSymmetricKeySpec keySpec = grp.getRandomRoundKey(rand).getRoundKey();
			if(!p.isEmpty()) {
				SpxpCryptoToolsV03.signObject(p, profileKeyPair);
				privateArray.put(SpxpCryptoToolsV03.encryptSymmetricCompact(p.toString(), keySpec));
			}
		}
		profileObj.put("publicKey", CryptoTools.getOrderedPublicJWK(profileKeyPair));
		// TODO: MISSING connect
		if(!privateArray.isEmpty()) {
			profileObj.put("private", privateArray);
		}
		SpxpCryptoToolsV03.signObject(profileObj, profileKeyPair);
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, profileName)), StandardCharsets.UTF_8) ) {
			profileObj.write(out, 4, 0);
		}
	}

	private void addSpxpElement(JSONObject profileObj, ArrayList<JSONObject> privateData, String name, Object value) {
		if(rand.nextInt(3) == 0) {
			profileObj.put(name, value);
			return;
		}
		if(privateData.size() == 1 || rand.nextInt(2) == 0) {
			privateData.get(0).put(name, value);
			return;
		}
		boolean atLeastInOneGroup = false;
		for(int i = 1; i < privateData.size(); i++) {
			if(rand.nextInt(3) == 0) {
				privateData.get(i).put(name, value);
				atLeastInOneGroup = true;
			}
		}
		if(!atLeastInOneGroup) {
			privateData.get(0).put(name, value);
		}
	}
	
	private JSONObject encryptProfilePhoto(String profilePhoto, File imageSourceDir, File targetDir) throws IOException, JSONException, SpxpCryptoException {
		// encrypt
		FileInputStream src = new FileInputStream(new File(imageSourceDir, profilePhoto));
		FileOutputStream dest = new FileOutputStream(new File(targetDir, "images_enc/"+profileName+".encrypted"));
		JSONObject o = new JSONObject(SpxpCryptoToolsV03.encryptResource(src, dest, "images_enc/"+profileName+".encrypted"));
		// return object ordered as in the spec
		JSONObject result = Tools.newOrderPreservingJSONObject();
		result.put("iv", o.getString("iv"));
		result.put("k", o.getString("k"));
		result.put("tag", o.getString("tag"));
		result.put("uri", o.getString("uri"));
		return result;
	}

	public void writeSpxpFriends(File targetDir) throws Exception {
		ArrayList<JSONArray> privateData = new ArrayList<>();
		for(int i = 0; i < nonVirtualGroupsCount; i++) {
			privateData.add(new JSONArray());
		}
		JSONArray friendsData = new JSONArray();
		for(SpxpFriendConnectionData frindConnection : friendConnections.values()) {
			String friendUri = frindConnection.getPeerProfile().getProfileUri();
			if(rand.nextInt(4) != 0) {
				// 75% of friends connections are public
				friendsData.put(friendUri);
				continue;
			}
			if(privateData.size() == 1 || rand.nextInt(2) == 0) {
				privateData.get(0).put(friendUri);
				continue;
			}
			boolean atLeastInOneGroup = false;
			for(int i = 1; i < privateData.size(); i++) {
				if(rand.nextInt(3) == 0) {
					privateData.get(i).put(friendUri);
					atLeastInOneGroup = true;
				}
			}
			if(!atLeastInOneGroup) {
				privateData.get(0).put(friendUri);
			}
		}
		JSONObject friendsObj = Tools.newOrderPreservingJSONObject();
		friendsObj.put("data", friendsData);
		JSONArray privateArray = new JSONArray();
		for(int i = 0; i < privateData.size(); i++) {
			JSONArray a = privateData.get(i);
			SpxpProfileGroupData grp = groups.get(i);
			SpxpSymmetricKeySpec keySpec = grp.getRandomRoundKey(rand).getRoundKey();
			if(!a.isEmpty()) {
				JSONObject p = Tools.newOrderPreservingJSONObject();
				p.put("data", a);
				signFriendsObj(p);
				privateArray.put(SpxpCryptoToolsV03.encryptSymmetricCompact(p.toString(), keySpec));
			}
		}
		if(!privateArray.isEmpty()) {
			friendsObj.put("private", privateArray);
		}
		signFriendsObj(friendsObj);
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, profileName)), StandardCharsets.UTF_8) ) {
			friendsObj.write(out, 4, 0);
		}
	}
	
	private void signFriendsObj(JSONObject friendsObj) throws SpxpCryptoException {
		if(rand.nextInt(2)==0) {
			SpxpCryptoToolsV03.signObject(friendsObj, profileKeyPair);
			return;
		}
		SpxpProfileImpersonationKey pik = extraFriendsSigningImpersonationKeys.get(rand.nextInt(extraFriendsSigningImpersonationKeys.size()));
		SpxpCryptoToolsV03.signObject(friendsObj, pik.getKeyPair());
		friendsObj.getJSONObject("signature").put("key", pik.getCertificate());
	}

	// total 906sec
	public void writeSpxpKeys(File targetDir) throws Exception {
		try( PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(targetDir, profileName)), StandardCharsets.UTF_8) ) ) {
			out.println("{");
			// groups
			for(int iGroups = 0; iGroups < groups.size(); iGroups++) {
				List<Integer> memberOf = groupInGroupMemberships.get(iGroups);
				SpxpProfileGroupData group = groups.get(iGroups);
				if(memberOf == null || memberOf.isEmpty()) {
					continue;
				}
				out.print("    ");
				out.println("\""+group.getGroupId()+"\": {");
				Iterator<Integer> it = memberOf.iterator();
				while(it.hasNext()) {
					int memberOfGroupIdx = it.next();
					SpxpProfileGroupData memberOfGroup = groups.get(memberOfGroupIdx);
					out.print("        ");
					out.print("\""+memberOfGroup.getGroupId()+"\": {");
					boolean f2 = true;
					for(SpxpRoundKey rk : memberOfGroup.getRoundKeys()) {
						if(rk.getKeyUsage() <= 0) {
							continue;
						}
						if(!f2) {
							out.print(",");
						}
						f2 = false;
						out.println();
						String jwkString = rk.getRoundKeyJwkSilent();
						SpxpSymmetricKeySpec ks = group.getRoundKeyForTime(new Date(rk.getValidSince())).getRoundKey();
						String encryptedSymmetricKey = SpxpCryptoToolsV03.encryptSymmetricCompact(jwkString, ks);
						out.print("            ");
						String kid = rk.getRoundKeySilent().getKeyId();
						String[] kidParts = kid.split("\\.");
						String roundId = kidParts[1];
						out.print("\""+roundId+"\": \""+encryptedSymmetricKey+"\"");
					}
					out.println();
					out.print("        ");
					out.print("}");
					if(it.hasNext()) {
						out.print(",");
					}
					out.println();
				}
				out.print("    ");
				out.println("},");
			}
			// connections
			Iterator<SpxpFriendConnectionData> itFriendConnections = friendConnections.values().iterator();
			while(itFriendConnections.hasNext()) {
				SpxpFriendConnectionData friend = itFriendConnections.next();
				out.print("    ");
				out.println("\""+friend.getIssuedReaderKey().getKeyId()+"\": {");
				boolean[] groupMembership = friend.getGroupMembership();
				boolean f1 = true;
				for(int i = 0; i < groups.size(); i++) {
					if(groupMembership[i] && !hasAccessThroughOtherGroup(friend, i)) {
						if(!f1) {
							out.println(",");
						}
						f1 = false;
						out.print("        ");
						out.print("\""+groups.get(i).getGroupId()+"\": {");
						boolean f2 = true;
						for(SpxpRoundKey rk : groups.get(i).getRoundKeys()) {
							if(rk.getKeyUsage() <= 0) {
								continue;
							}
							if(!f2) {
								out.print(",");
							}
							f2 = false;
							out.println();
							String jwkString = rk.getRoundKeyJwkSilent();
							String encryptedSymmetricKey = SpxpCryptoToolsV03.encryptSymmetricCompact(jwkString, friend.getIssuedReaderKey());
							out.print("            ");
							String kid = rk.getRoundKeySilent().getKeyId();
							String[] kidParts = kid.split("\\.");
							String roundId = kidParts[1];
							out.print("\""+roundId+"\": \""+encryptedSymmetricKey+"\"");
						}
						out.println();
						out.print("        ");
						out.print("}");
					}
				}
				if(!f1) {
					out.println();
				}
				out.print("    ");
				out.print("}");
				if(itFriendConnections.hasNext()) {
					out.print(",");
				}
				out.println();
			}
			out.println("}");
		}
	}

	public boolean hasAccessThroughOtherGroup(SpxpFriendConnectionData friend, int g) {
		boolean[] groupMembership = friend.getGroupMembership();
		for(int i = 0; i < groups.size(); i++) {
			if(i==g) {
				continue;
			}
			if(!groupMembership[i]) {
				continue;
			}
			List<Integer> l = groupInGroupMemberships.get(i);
			if(l!=null && l.contains(g)) {
				return true;
			}
		}
		return false;
	}

	public void writePosts(File targetDir) throws Exception {
		JSONArray postsArray = new JSONArray();
		for(SpxpPost post : posts) {
			JSONObject postJsonObject = post.toJSONObject();
			if(rand.nextInt(5) == 0) {
				// 20% of posts without createts
				postJsonObject.remove("createts");
			}
			postJsonObject = processPostSignatureAndEncryption(post.toJSONObject(), post.getCreateDate());
			postJsonObject.put("seqts", Tools.formatPostsDate(post.getSeqDate()));
			postsArray.put(postJsonObject);
		}
		JSONObject postsObj = Tools.newOrderPreservingJSONObject();
		postsObj.put("data", postsArray);
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, profileName)), StandardCharsets.UTF_8) ) {
			postsObj.write(out, 4, 0);
		}
	}

	private JSONObject processPostSignatureAndEncryption(JSONObject obj, Date ts) throws Exception {
		if(rand.nextInt(3) == 0) {
			// 33%  are public
			signPostObj(obj);
			return obj;
		}
		JSONObject result = Tools.newOrderPreservingJSONObject();
		LinkedList<SpxpSymmetricKeySpec> receipients = new LinkedList<>();
		StringBuilder sb = new StringBuilder();
		if(nonVirtualGroupsCount == 1 || rand.nextInt(2) == 0) {
			receipients.add(groups.get(0).getRoundKeyForTime(ts).getRoundKey());
			sb.append('[');
			sb.append(groups.get(0).getDisplayName());
			sb.append(']');
			sb.append(' ');
		} else {
			for(int i = 1; i < nonVirtualGroupsCount; i++) {
				if(rand.nextInt(3) == 0) {
					receipients.add(groups.get(i).getRoundKeyForTime(ts).getRoundKey());
					sb.append('[');
					sb.append(groups.get(i).getDisplayName());
					sb.append(']');
					sb.append(' ');
				}
			}
			if(receipients.size() == 0) {
				receipients.add(groups.get(0).getRoundKeyForTime(ts).getRoundKey());
				sb.append('[');
				sb.append(groups.get(0).getDisplayName());
				sb.append(']');
				sb.append(' ');
			}
		}
		sb.append(obj.getString("message"));
		obj.put("message", sb.toString());
		JSONArray privateArray = new JSONArray();
		signPostObj(obj);
		privateArray.put(new JSONObject(SpxpCryptoToolsV03.encryptSymmetricJson(obj.toString(), receipients)));
		result.put("private", privateArray);
		return result;
	}
	
	private void  signPostObj(JSONObject postObj) throws SpxpCryptoException {
		if(rand.nextInt(10)==0) {
			// 10% of posts originate from another profile
			while(true) {
				SpxpFriendConnectionData fcd = getRandomFriendConnection();
				if(fcd.getIssuedCertificate() == null) {
					continue;
				}
				postObj.put("author", fcd.getPeerProfile().getProfileUri());
				SpxpCryptoToolsV03.signObject(postObj, fcd.getPeerProfile().getProfileKeyPair());
				postObj.getJSONObject("signature").put("key", fcd.getIssuedCertificate());
				return;
			}
		}
		if(rand.nextInt(2)==0) {
			SpxpCryptoToolsV03.signObject(postObj, profileKeyPair);
			return;
		}
		SpxpProfileImpersonationKey pik = extraPostSigningImpersonationKeys.get(rand.nextInt(extraPostSigningImpersonationKeys.size()));
		SpxpCryptoToolsV03.signObject(postObj, pik.getKeyPair());
		postObj.getJSONObject("signature").put("key", pik.getCertificate());
	}

}
