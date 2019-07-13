package com.xaldon.spxp.data.fakedatagenerator_crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.apache.cxf.common.util.Base64UrlUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spxp.crypto.SpxpCryptoTools;

import com.xaldon.spxp.data.fakedatagenerator.Tools;

public class SpxpProfileData {
	
	private Random rand;

	private String profileName;
	
	private String fullName;
	
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
	
	private KeyPair profileKeyPair;
	
	private String profileKeyId;
	
	private String profileKeyCurve;
	
	private ArrayList<SpxpProfileGroupData> groups;
	
	private int nonVirtualGroupsCount;
	
	private int targetFriendCount;
	
	private int actualFriendCount;
	
	private LinkedList<SpxpFriendConnectionData> friendConnections = new LinkedList<>();
	
	private HashMap<Integer, LinkedList<Integer>> groupInGroupMemberships = new HashMap<>();
	
	private HashSet<SpxpProfileData> connectedProfilesSet = new HashSet<>();
	
	private LinkedList<SpxpPost> posts = new LinkedList<>();

	public SpxpProfileData(
			Random rand,
			String profileName,
			String fullName,
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
			KeyPair profileKeyPair,
			String profileKeyId,
			String profileKeyCurve,
			ArrayList<SpxpProfileGroupData> groups,
			int targetFriendCount) {
		this.rand = rand;
		this.profileName = profileName;
		this.fullName = fullName;
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
		this.profileKeyId = profileKeyId;
		this.profileKeyCurve = profileKeyCurve;
		this.groups = groups;
		this.nonVirtualGroupsCount = groups.size();
		this.targetFriendCount = targetFriendCount;
		// if we have more then 1 group, simulate that all other groups are a member of the first group
		if(groups.size() > 1) {
			for(int i = 1; i < groups.size(); i++) {
				addGroupInGroupMembership(i, 0);
			}
		}
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

	public String getFullName() {
		return fullName;
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

	public KeyPair getProfileKeyPair() {
		return profileKeyPair;
	}
	
	public ECPrivateKey getProfilePrivateKey() {
		return (ECPrivateKey) profileKeyPair.getPrivate();
	}
	
	public ECPublicKey getProfilePublicKey() {
		return (ECPublicKey) profileKeyPair.getPublic();
	}

	public String getProfileKeyId() {
		return profileKeyId;
	}

	public String getProfileKeyCurve() {
		return profileKeyCurve;
	}

	public ArrayList<SpxpProfileGroupData> getGroups() {
		return groups;
	}
	
	public void extendGroupsByVirtualGroups(int basedOnGroup, int additionalGroupCount) {
		SpxpProfileGroupData base = groups.get(basedOnGroup);
		for(int i = 0; i < additionalGroupCount; i++) {
			groups.add(new SpxpProfileGroupData(base.getDisplayName()+" (virt "+i+")", base.getGroupId()+"-virt_"+i, true));
		}
		for(SpxpFriendConnectionData fcd : friendConnections) {
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
		return connectedProfilesSet.contains(peer);
	}

	public void addFriend(SpxpProfileData peer, byte[] sharedSecret) {
		boolean[] groupMembership = new boolean[groups.size()];
		groupMembership[0] = true;
		for(int i = 1; i <  groups.size(); i++) {
			if(rand.nextInt(2) == 0) {
				groupMembership[i] = true;
			}
		}
		friendConnections.add(new SpxpFriendConnectionData(peer, groupMembership, sharedSecret));
		connectedProfilesSet.add(peer);
		actualFriendCount++;
	}
	
	public SpxpFriendConnectionData getFriendConnectionData(int i) {
		return friendConnections.get(i);
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
	
	public Date getOldestPostDate() {
		Date d = null;
		for(SpxpPost p : posts) {
			if(d == null || p.getPostDate().compareTo(d) < 0) {
				d = p.getPostDate();
			}
		}
		return d;
	}

	public void writeProfileFile(File profilesDir, String baseUrl) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		JSONObject profileObj = new JSONObject();
		profileObj.put("endpoint", baseUrl+profileName);
		profileObj.put("key", CryptoTools.getECJWK(profileKeyId, profileKeyCurve, profileKeyPair));
		JSONArray groupsArray = new JSONArray();
		for(SpxpProfileGroupData grp : groups) {
			JSONObject groupObj = new JSONObject();
			groupObj.put("displayName", grp.getDisplayName());
			groupObj.put("groupId", grp.getGroupId());
			groupObj.put("virtual", grp.isVirtual());
			JSONArray roundKeys = new JSONArray();
			for(SpxpRoundKey rk : grp.getRoundKeys()) {
				JSONObject roundKey = new JSONObject();
				roundKey.put("roundId", rk.getRoundId());
				roundKey.put("validSince", sdf.format(rk.getValidSince()));
				roundKey.put("validBefore", sdf.format(rk.getValidBefore()));
				roundKey.put("key", rk.getRoundKeySilent().getJWK());
				roundKeys.put(roundKey);
			}
			groupObj.put("roundKeys", roundKeys);
			groupsArray.put(groupObj);
		}
		profileObj.put("groups", groupsArray);
		JSONArray groupMembershipsArray = new JSONArray();
		for(int i = 0; i < groups.size(); i++) {
			String groupId = groups.get(i).getGroupId();
			List<Integer> memberOf = groupInGroupMemberships.get(i);
			if(memberOf != null) {
				for(int g : memberOf) {
					JSONObject membershipObj = new JSONObject();
					membershipObj.put("groupId", groupId);
					membershipObj.put("memberOf", groups.get(g).getGroupId());
					groupMembershipsArray.put(membershipObj);
				}
			}
		}
		profileObj.put("groupMemberships", groupMembershipsArray);
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(profilesDir, profileName+".json")), StandardCharsets.UTF_8) ) {
			profileObj.write(out, 4, 0);
		}
	}
	
	public void writeSpxpProfile(File targetDir, Date now, File imageSourceDir) throws Exception {
		JSONObject profileObj = new JSONObject();
		ArrayList<JSONObject> privateData = new ArrayList<>();
		for(int i = 0; i < nonVirtualGroupsCount; i++) {
			privateData.add(new JSONObject());
		}
		profileObj.put("name", fullName);
		addSpxpElement(profileObj, privateData, "about", about);
		addSpxpElement(profileObj, privateData, "gender", gender);
		addSpxpElement(profileObj, privateData, "website", "https://example.com");
		addSpxpElement(profileObj, privateData, "email", email);
		addSpxpElement(profileObj, privateData, "birthDayAndMonth", birthDayAndMonth);
		addSpxpElement(profileObj, privateData, "birthYear", birthYear);
		addSpxpElement(profileObj, privateData, "hometown", hometown);
		addSpxpElement(profileObj, privateData, "location", location);
		JSONObject coordinatesObj = new JSONObject();
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
		profileObj.put("albumsEndpoint", "albums/"+profileName);
		profileObj.put("keysEndpoint", "keys/_read-keys.php?profile="+profileName);
		JSONArray privateArray = new JSONArray();
		for(int i = 0; i < privateData.size(); i++) {
			JSONObject p = privateData.get(i);
			SpxpProfileGroupData grp = groups.get(i);
			SpxpSymmetricKeySpec keySpec = grp.getRandomRoundKey(rand).getRoundKey();
			if(!p.isEmpty()) {
				privateArray.put(SpxpCryptoTools.encryptSymmetricCompact(p.toString(), keySpec.getKeyId(), keySpec.getSymmetricKey()));
			}
		}
		if(!privateArray.isEmpty()) {
			profileObj.put("private", privateArray);
		}
		profileObj.put("publicKey", CryptoTools.getECJWK(profileKeyId, profileKeyCurve, profileKeyPair.getPublic()));
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
	
	private JSONObject encryptProfilePhoto(String profilePhoto, File imageSourceDir, File targetDir) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IOException {
		int keyLength = 256;
		int ivLength = 96;
		int authTagLength = 128;
		int authTagBytes = authTagLength / 8;
		// content encryption key
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(keyLength);
		SecretKey secretKey = keyGen.generateKey();
		// IV
		byte[] iv = CryptoTools.generateSymmetricKey(ivLength);
		// algo params
		AlgorithmParameterSpec algoParamSpec = new GCMParameterSpec(authTagLength, iv);
		// Cipher
		String algorithm = "AES/GCM/NoPadding";
		Cipher c = Cipher.getInstance(algorithm);
		c.init(Cipher.ENCRYPT_MODE, secretKey, algoParamSpec);
		// encrypt
		RestrainLastNBytesOutputStream rlnbos = null;
		CipherOutputStream cos = null;
		byte[] tag;
		try(FileInputStream in = new FileInputStream(new File(imageSourceDir, profilePhoto))) {
			rlnbos = new RestrainLastNBytesOutputStream(new FileOutputStream(new File(targetDir, "images_enc/"+profileName+".encrypted")),authTagBytes);
			cos = new CipherOutputStream(rlnbos,c);
			try {
				Tools.copyStreams(in, cos);
			} finally {
				cos.close();
			}
			tag = rlnbos.getRestrainedBytes();
		}
		// build describing JSON object
		JSONObject result = new JSONObject();
		result.put("iv", Base64UrlUtility.encode(iv));
		result.put("k", Base64UrlUtility.encode(secretKey.getEncoded()));
		result.put("tag", Base64UrlUtility.encode(tag));
		result.put("enc", "A256GCM");
		result.put("url", "images_enc/"+profileName+".encrypted");
		// debug encrypted with tag at the end
		c = Cipher.getInstance(algorithm);
		c.init(Cipher.ENCRYPT_MODE, secretKey, algoParamSpec);
		try(FileInputStream in = new FileInputStream(new File(imageSourceDir, profilePhoto))) {
			try(CipherOutputStream o = new CipherOutputStream(new FileOutputStream(new File(targetDir, "images_enc/"+profileName+".encrypted.debug")),c)) {
				try {
					Tools.copyStreams(in, o);
				} finally {
					o.close();
				}
			}
		}
		return result;
	}

	public void writeSpxpFriends(File targetDir, String baseUrl) throws Exception {
		ArrayList<JSONArray> privateData = new ArrayList<>();
		for(int i = 0; i < nonVirtualGroupsCount; i++) {
			privateData.add(new JSONArray());
		}
		JSONArray friendsData = new JSONArray();
		for(SpxpFriendConnectionData frindConnection : friendConnections) {
			String friendUrl = baseUrl + frindConnection.getPeerProfile().getProfileName();
			if(rand.nextInt(4) != 0) {
				// 75% of friends connections are public
				friendsData.put(friendUrl);
				continue;
			}
			if(privateData.size() == 1 || rand.nextInt(2) == 0) {
				privateData.get(0).put(friendUrl);
				continue;
			}
			boolean atLeastInOneGroup = false;
			for(int i = 1; i < privateData.size(); i++) {
				if(rand.nextInt(3) == 0) {
					privateData.get(i).put(friendUrl);
					atLeastInOneGroup = true;
				}
			}
			if(!atLeastInOneGroup) {
				privateData.get(0).put(friendUrl);
			}
		}
		JSONObject friendsObj = new JSONObject();
		friendsObj.put("data", friendsData);
		JSONArray privateArray = new JSONArray();
		for(int i = 0; i < privateData.size(); i++) {
			JSONArray a = privateData.get(i);
			SpxpProfileGroupData grp = groups.get(i);
			SpxpSymmetricKeySpec keySpec = grp.getRandomRoundKey(rand).getRoundKey();
			if(!a.isEmpty()) {
				JSONObject p = new JSONObject();
				p.put("data", a);
				privateArray.put(SpxpCryptoTools.encryptSymmetricCompact(p.toString(), keySpec.getKeyId(), keySpec.getSymmetricKey()));
			}
		}
		if(!privateArray.isEmpty()) {
			friendsObj.put("private", privateArray);
		}
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, profileName)), StandardCharsets.UTF_8) ) {
			friendsObj.write(out, 4, 0);
		}
	}

	// total 906sec
	public void writeSpxpKeys(File targetDir, boolean condensed) throws Exception {
		try( PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(targetDir, profileName)), StandardCharsets.UTF_8) ) ) {
			out.println("{");
			// groups
			for(int i = 0; i < groups.size(); i++) {
				List<Integer> memberOf = groupInGroupMemberships.get(i);
				if(memberOf == null || memberOf.isEmpty()) {
					continue;
				}
				if(!condensed) {
					out.print("    ");
				}
				out.println("\""+groups.get(i).getGroupId()+"\": {");
				Iterator<Integer> it = memberOf.iterator();
				while(it.hasNext()) {
					int g = it.next();
					if(!condensed) {
						out.print("        ");
					}
					out.print("\""+groups.get(g).getGroupId()+"\": {");
					boolean f2 = true;
					for(SpxpRoundKey rk : groups.get(g).getRoundKeys()) {
						if(rk.getKeyUsage() <= 0) {
							continue;
						}
						if(!f2) {
							out.print(",");
						}
						f2 = false;
						out.println();
						SpxpSymmetricKeySpec keySpec = rk.getRoundKeySilent();
						String jwkString = keySpec.getJWK();
						SpxpSymmetricKeySpec ks = groups.get(i).getRoundKeyForTime(new Date(rk.getValidSince())).getRoundKey();
						String encryptedSymmetricKey = SpxpCryptoTools.encryptSymmetricCompact(jwkString, ks.getKeyId(), ks.getSymmetricKey());
						if(!condensed) {
							out.print("            ");
						}
						String kid = keySpec.getKeyId();
						if(condensed) {
							String[] parts = kid.split("\\.");
							kid = parts[1];
						}
						out.print("\""+kid+"\": \""+encryptedSymmetricKey+"\"");
					}
					out.println();
					if(!condensed) {
						out.print("        ");
					}
					out.print("}");
					if(it.hasNext()) {
						out.print(",");
					}
					out.println();
				}
				if(!condensed) {
					out.print("    ");
				}
				out.println("},");
			}
			// friends
			Iterator<SpxpFriendConnectionData> itFriendConnections = friendConnections.iterator();
			while(itFriendConnections.hasNext()) {
				SpxpFriendConnectionData friend = itFriendConnections.next();
				if(!condensed) {
					out.print("    ");
				}
				out.println("\""+friend.getPeerProfile().getProfileKeyId()+"\": {");
				boolean[] groupMembership = friend.getGroupMembership();
				boolean f1 = true;
				for(int i = 0; i < groups.size(); i++) {
					if(groupMembership[i] && !hasAccessThroughOtherGroup(friend, i)) {
						if(!f1) {
							out.println(",");
						}
						f1 = false;
						if(!condensed) {
							out.print("        ");
						}
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
							SpxpSymmetricKeySpec keySpec = rk.getRoundKeySilent();
							String jwkString = keySpec.getJWK();
							String encryptedSymmetricKey = SpxpCryptoTools.encryptWithSharedSecret(jwkString, friend.getSharedSecret());
							if(condensed) {
								String[] parts = encryptedSymmetricKey.split("\\.");
								encryptedSymmetricKey = parts[2]+"."+parts[3]+"."+parts[4];
							}
							if(!condensed) {
								out.print("            ");
							}
							String kid = keySpec.getKeyId();
							if(condensed) {
								String[] parts = kid.split("\\.");
								kid = parts[1];
							}
							out.print("\""+kid+"\": \""+encryptedSymmetricKey+"\"");
						}
						out.println();
						if(!condensed) {
							out.print("        ");
						}
						out.print("}");
					}
				}
				if(!f1) {
					out.println();
				}
				if(!condensed) {
					out.print("    ");
				}
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

	// total 1797sec
	/*
	public void writeSpxpKeys_OLD(File targetDir) throws Exception {
		JSONObject keysObj = new JSONObject();
		for(SpxpFriendConnectionData friend : friendConnections) {
			JSONObject friendKeysObj = new JSONObject();
			boolean[] groupMembership = friend.getGroupMembership();
			for(int i = 0; i < groups.size(); i++) {
				if(groupMembership[i]) {
					JSONObject roundKeysObj = new JSONObject();
					for(SpxpRoundKey rk : groups.get(i).getRoundKeys()) {
						SpxpSymmetricKeySpec keySpec = rk.getRoundKeySilent();
						String jwkString = keySpec.getJWK();
						String encryptedSymmetricKey = SpxpCryptoTools.encryptGroupKeyCompact(jwkString, friend.getSharedSecret());
						roundKeysObj.put(keySpec.getKeyId(), encryptedSymmetricKey);
					}
					friendKeysObj.put(groups.get(i).getSymmetricGroupKeyId(), roundKeysObj);
				}
			}
			if(!friendKeysObj.isEmpty()) {
				keysObj.put(friend.getPeerProfile().getProfileKeyId(), friendKeysObj);
			}
		}
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, profileName)), StandardCharsets.UTF_8) ) {
			keysObj.write(out, 4, 0);
		}
	}
	*/

	public void writePosts(File targetDir) throws Exception {
		JSONArray postsArray = new JSONArray();
		for(SpxpPost post : posts) {
			postsArray.put(processPostEncryption(post.toJSONObject(), post.getPostDate()));
		}
		JSONObject postsObj = new JSONObject();
		postsObj.put("data", postsArray);
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, profileName)), StandardCharsets.UTF_8) ) {
			postsObj.write(out, 4, 0);
		}
	}

	private JSONObject processPostEncryption(JSONObject obj, Date ts) throws Exception {
		if(rand.nextInt(3) == 0) {
			return obj;
		}
		//SpxpRoundGroupKeys rgk = getRoundGroupKeysForTime(ts);
		JSONObject result = new JSONObject();
		result.put("timestampUTC", obj.get("timestampUTC"));
		obj.remove("timestampUTC");
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
		privateArray.put(CryptoTools.encryptSymmetricJson(obj, result.getString("timestampUTC"), receipients));
		result.put("private", privateArray);
		return result;
	}

}
