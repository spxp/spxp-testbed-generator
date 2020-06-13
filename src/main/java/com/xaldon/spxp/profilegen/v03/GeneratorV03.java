package com.xaldon.spxp.profilegen.v03;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.spxp.crypto.SpxpCryptoToolsV03;
import org.spxp.crypto.SpxpProfileKeyPair;

import com.xaldon.spxp.profilegen.utils.Tools;

public class GeneratorV03 {

	private final static String TARGET_DIR_PATH = "./v0.3";

	private final static String PROFILE_ROOT_URL = "http://testbed.spxp.org/0.3/";

	private final static int GENERATED_PROFILES_COUNT = 1000; // must be even
	
	// turned off by default to avoid hitting the QR code API on each test run
	// please be nice and respect the service limits of qrserver.com
	private final static boolean GENERATE_QR_CODES = false;
	
	private final static boolean LINK_QR_CODES = true;

	private final static int POSTS_PER_PROFILE_COUNT = 500;

	private final static String POSTS_START_TIMESTAMP = "2020-01-01T00:00:00.000";

	private final static int PROFILE_IMAGE_COUNT_FEMALE = 200;

	private final static int PROFILE_IMAGE_COUNT_MALE = 200;
	
	private ArrayList<String> sampleQuotes = new ArrayList<String>(1000);
	
	private ArrayList<String> samplePlaces = new ArrayList<String>(5);
	
	private ArrayList<String> sampleTextPostMessages = new ArrayList<String>(1000);
	
	private ArrayList<String> sampleWebPostMessages = new ArrayList<String>(5);
	
	private ArrayList<String> sampleWebLinks = new ArrayList<String>(5);
	
	private ArrayList<String> samplePhotoPostMessages = new ArrayList<String>(5);
	
	private ArrayList<String> sampleSmallPhotoUrls = new ArrayList<String>(2400);
	
	private ArrayList<String> sampleRegularPhotoUrls = new ArrayList<String>(2400);
	
	private ArrayList<String> sampleVideoPostMessages = new ArrayList<String>(5);
	
	private ArrayList<String> samplePreviewVideoUrls = new ArrayList<String>(5);
	
	private ArrayList<String> sampleMediaVideoUrls = new ArrayList<String>(5);

	private Random rand = new Random(9876543210l);
	
	private static int ONE_YEAR = 365;
	
	private final static long MS_PER_DAY = 24 * 60 * 60 * 1000;

	public static void main(String[] args) throws Exception {
		(new GeneratorV03()).run(args);
	}
	
	public void run(String[] args) throws Exception {
		int defaultPeriodInYears = 5;
		int unfriendEventsPerYear = 2;
		int defaultPeriodLength = ONE_YEAR * defaultPeriodInYears;
		int unfriendEventsPerPeriod = unfriendEventsPerYear * defaultPeriodInYears;
		int maxUsersPerGroup = 20;
		System.out.println("Initializing...");
		Date now = Tools.parsePostsDate(POSTS_START_TIMESTAMP);
		loadSampleData();
		File imageSourceDir = new File("./profile_images");
		File targetDir = new File(TARGET_DIR_PATH);
		if(!targetDir.exists()) {
			targetDir.mkdirs();
		}
		File privateDataDir = new File(targetDir, "private_profile_data");
		if(!privateDataDir.exists()) {
			privateDataDir.mkdirs();
		}
		File friendsDir = new File(targetDir, "friends");
		if(!friendsDir.exists()) {
			friendsDir.mkdirs();
		}
		File keysDir = new File(targetDir, "keys");
		if(!keysDir.exists()) {
			keysDir.mkdirs();
		}
		File postsDir = new File(targetDir, "posts");
		if(!postsDir.exists()) {
			postsDir.mkdirs();
		}
		File encryptedImagesDir = new File(targetDir, "images_enc");
		if(!encryptedImagesDir.exists()) {
			encryptedImagesDir.mkdirs();
		}
		System.out.println("Generating profiles and profile KeyPairs...");
		List<SpxpProfileData> profiles = generateProfiles(PROFILE_ROOT_URL, GENERATED_PROFILES_COUNT/2, GENERATED_PROFILES_COUNT/2);
		System.out.println("Generating posts...");
		generatePosts(profiles, POSTS_PER_PROFILE_COUNT, now);
		System.out.println("Assigning friends...");
		assignFriends(profiles);
		System.out.println("Generating reader keys and issuing certificates...");
		generateReaderKeysAndCertificates(profiles);
		System.out.println("Creating virtual groups...");
		if(maxUsersPerGroup > 0) {
			createVirtualGroups(profiles, maxUsersPerGroup);
		}
		System.out.println("Generating round keys...");
		generateRoundKeys(profiles, now, defaultPeriodLength, unfriendEventsPerPeriod);
		System.out.println("Encrypting private data and writing files...");
		long start = System.currentTimeMillis();
		int i = 0;
		for(SpxpProfileData profile : profiles) {
			profile.writePrivateData(privateDataDir);
			profile.writeSpxpProfile(targetDir, imageSourceDir);
			profile.writeSpxpFriends(friendsDir);
			profile.writePosts(postsDir);
			profile.writeSpxpKeys(keysDir);
			System.out.print(".");
			i++;
			if(i % 50 == 0) {
				System.out.print(Integer.toString(i));
			}
			if(i % 100 == 0) {
				System.out.println();
			}
		}
		long end = System.currentTimeMillis();
		long duration = (end - start) / 1000;
		Tools.generateStaticPhpFile("com/xaldon/spxp/profilegen/v03/_read-posts.php", new File(postsDir, "_read-posts.php"));
		Tools.generateStaticPhpFile("com/xaldon/spxp/profilegen/v03/_read-keys.php", new File(keysDir, "_read-keys.php"));
		System.out.println("writing all files took "+duration+"sec");
		if(GENERATE_QR_CODES) {
			System.out.println("Generating QR codes...");
			File targetQrDir = new File(targetDir, "qr");
			if(!targetQrDir.exists()) {
				targetQrDir.mkdirs();
			}
			for(SpxpProfileData profile : profiles) {
				String profileName = profile.getProfileName();
				Tools.generateQrForProfile(new File(targetQrDir, profileName+".png"), PROFILE_ROOT_URL + profileName);
				Thread.sleep(100);
			}
		}
		writeIndexFiles(targetDir, profiles);
		System.out.println("Finished.");
	}
	
	private void writeIndexFiles(File targetDir, List<SpxpProfileData> profiles)  throws Exception {
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, "profiles.txt")), StandardCharsets.UTF_8) ) {
			for(SpxpProfileData profile : profiles) {
				out.write(PROFILE_ROOT_URL);
				out.write(profile.getProfileName());
				out.write("\n");
			}
		}
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, "index.html")), StandardCharsets.UTF_8) ) {
			out.write("<html>\n");
			out.write("<head><meta charset=\"utf-8\"/><title>Testbed of SPXP profiles of version 0.3</title></head>\n");
			out.write("<body>\n");
			out.write("<h1>Testbed of SPXP profiles of version 0.3</h1>\n");
			for(SpxpProfileData profile : profiles) {
				String profileName = profile.getProfileName();
				String fullName = profile.getFullName();
				out.write("<a href=\"");
				out.write(PROFILE_ROOT_URL);
				out.write(profileName);
				out.write("\">");
				out.write(PROFILE_ROOT_URL);
				out.write(profileName);
				out.write("</a> (");
				out.write(fullName);
				out.write(")");
				if(LINK_QR_CODES) {
					out.write(" <a href=\"qr/");
					out.write(profileName);
					out.write(".png\">QR</a>");
				}
				out.write(" <a href=\"private_profile_data/");
				out.write(profileName);
				out.write(".json\">private</a>");
				out.write("<br/>\n");
			}
			out.write("</body>\n");
			out.write("</html>\n");
		}
	}

	public List<SpxpProfileData> generateProfiles(String baseUrl, int countFemale, int countMale) throws Exception {
		ArrayList<SpxpProfileData> result = new ArrayList<SpxpProfileData>(countFemale+countMale);
		HashSet<String> uniqueNameCheck = new HashSet<String>(countFemale+countMale);
		generateProfilesPerGender(result, uniqueNameCheck, baseUrl, "female", countFemale);
		generateProfilesPerGender(result, uniqueNameCheck, baseUrl, "male", countMale);
		return result;
	}
	
	public void generateProfilesPerGender(ArrayList<SpxpProfileData> result, HashSet<String> uniqueNameCheck, String baseUrl, String gender, int count) throws Exception {
		JSONArray randomUsers = getRandomUsers(gender, count);
		for(int i = 0; i < count; i++) {
			SpxpProfileData newProfile = generateSingleProfile(baseUrl, randomUsers.getJSONObject(i), i, gender);
			if(uniqueNameCheck.contains(newProfile.getProfileName().toLowerCase())) {
				throw new RuntimeException("duplicate profile name: " + newProfile.getProfileName());
			}
			uniqueNameCheck.add(newProfile.getProfileName().toLowerCase());
			result.add(newProfile);
		}
	}
	
	public SpxpProfileData generateSingleProfile(String baseUrl, JSONObject obj, int id, String gender) throws Exception {
		JSONObject nameObj = obj.getJSONObject("name");
		String fullName = Tools.uppercaseFirstChar(nameObj.getString("first")) + " " + Tools.uppercaseFirstChar(nameObj.getString("last"));
		JSONObject loginObj = obj.getJSONObject("login");
		String profileName = loginObj.getString("username");
		String about = sampleQuotes.get(rand.nextInt(sampleQuotes.size()));
		String email = obj.getString("email");
		String dobDate = obj.getJSONObject("dob").getString("date");
		String birthYear = dobDate.substring(0, 4);
		String birthDayAndMonth = dobDate.substring(8, 10) + "-" + dobDate.substring(5, 7);
		String hometown = samplePlaces.get(rand.nextInt(samplePlaces.size()));
		String location = samplePlaces.get(rand.nextInt(samplePlaces.size()));
		JSONObject coordinates = obj.getJSONObject("location").getJSONObject("coordinates");
		String latitude = coordinates.getString("latitude");
		String longitude = coordinates.getString("longitude");
		int profileImageCount = gender.equals("male") ? PROFILE_IMAGE_COUNT_MALE : PROFILE_IMAGE_COUNT_FEMALE;
		int imageid = id % profileImageCount;
		String profilePhoto = gender.substring(0,1)+imageid+".jpg";
		SpxpProfileKeyPair profileKeyPair = SpxpCryptoToolsV03.generateProfileKeyPair();
		profileKeyPair = new SpxpProfileKeyPair("key-"+profileName, profileKeyPair.getSecretKey(), profileKeyPair.getPublicKey());
		ArrayList<SpxpProfileGroupData> groupData = new ArrayList<SpxpProfileGroupData>(4);
		groupData.add(new SpxpProfileGroupData("Friends", "grp-"+profileName+"-friends", false));
		if(rand.nextInt(2) == 0) {
			groupData.add(new SpxpProfileGroupData("Close Friends", "grp-"+profileName+"-close_friends", false));
		}
		if(rand.nextInt(2) == 0) {
			groupData.add(new SpxpProfileGroupData("Family", "grp-"+profileName+"-family", false));
		}
		if(rand.nextInt(2) == 0) {
			groupData.add(new SpxpProfileGroupData("Business contacts", "grp-"+profileName+"-business_contacts", false));
		}
		// according to http://www.pewresearch.org/fact-tank/2014/02/03/what-people-like-dislike-about-facebook/
		// mean 338 / median 200
		// according to https://www.telegraph.co.uk/news/science/science-news/12108412/Facebook-users-have-155-friends-but-would-trust-just-four-in-a-crisis.html
		// average 155
		// Dunbar Number: 150
		// we are using a normal distribution for now with a mean of 300 and deviation of 200
		// a better solution would be a weibul distribution modelled after
		// https://blog.stephenwolfram.com/2013/04/data-science-of-the-facebook-world/
		int targetFriendCount = (int)Math.round(nextCompressedGaussianRand()*200d + 300d);
		return new SpxpProfileData(rand, baseUrl, profileName, fullName, about, gender, email, birthDayAndMonth, birthYear, hometown, location, latitude, longitude, profilePhoto, profileKeyPair, groupData, targetFriendCount);
	}
	
	private void generatePosts(List<SpxpProfileData> profiles, int postsPerProfile, Date now) throws Exception {
		for(SpxpProfileData profile : profiles) {
			generatePostsPerProfile(profile, postsPerProfile, now);
		}
	}
	
	private void generatePostsPerProfile(SpxpProfileData profile, int count, Date now) throws Exception {
		// instagram stats of 2016: 95 mn posts per day at 500mn users --> one post every 5 days. see
		// https://www.brandwatch.com/blog/instagram-stats/
		// https://blog.hootsuite.com/instagram-statistics/
		// we use a normal distribution with a mean of 4 and a deviation of 3
		double r = nextCompressedGaussianRand();
		double avgPostsPerMonth = r*3d + 4d;
		if(avgPostsPerMonth < 1.0  || avgPostsPerMonth > 7.0) {
			throw new RuntimeException("Something went wrong :-( avgPostsPerMonth="+avgPostsPerMonth+" r="+r);
		}
		double avgDaysBetweenPosts = 30.5 / avgPostsPerMonth;
		double halfBandwidth = avgDaysBetweenPosts / 4.0;
		if(avgDaysBetweenPosts < 4.2  || avgDaysBetweenPosts > 30.0) {
			throw new RuntimeException("Something went wrong :-( avgDaysBetweenPosts="+avgDaysBetweenPosts);
		}
		long seqTs = now.getTime();
		for(int i = 0; i < count; i++) {
			double distanceInDays = nextCompressedGaussianRand()*halfBandwidth + avgDaysBetweenPosts;
			long distanceInMs = Math.round(distanceInDays * (double)MS_PER_DAY);
			if(distanceInMs <= 0) {
				throw new RuntimeException("Something went wrong :-( distanceInMs="+distanceInMs);
			}
			seqTs -= distanceInMs;
			Date seqDate = new Date(seqTs);
        	profile.addPost(generatePost(seqDate));
		}
	}
	
	private SpxpPost generatePost(Date seqDate) {
		int r = rand.nextInt(100);
		if( r < 45) {
			// 45%
        	return generateTextPost(seqDate);
		} else if(r < 50) {
			// 5%
        	return generateWebPost(seqDate);
		} else if(r < 95) {
			// 45%
        	return generatePhotoPost(seqDate);
		} else {
			// 5%
        	return generateVideoPost(seqDate);
		}
	}
	
	private SpxpPost generateTextPost(Date seqDate) {
		String message = sampleTextPostMessages.get(rand.nextInt(sampleTextPostMessages.size()));
		String place = (rand.nextInt(2) != 0) ? samplePlaces.get(rand.nextInt(samplePlaces.size())) : null;
		Date createDate = new Date(seqDate.getTime() - rand.nextInt(4 * 60 * 60 * 1000)); // post created up to 4 hrs before being received by server
		return new SpxpTextPost(seqDate, createDate, message, place);
	}
	
	private SpxpPost generateWebPost(Date seqDate) {
		String message = sampleWebPostMessages.get(rand.nextInt(sampleWebPostMessages.size()));
		String link = sampleWebLinks.get(rand.nextInt(sampleWebLinks.size()));
		Date createDate = new Date(seqDate.getTime() - rand.nextInt(4 * 60 * 60 * 1000)); // post created up to 4 hrs before being received by server
		return new SpxpWebPost(seqDate, createDate, message, link);
	}

	private SpxpPost generatePhotoPost(Date seqDate) {
		String message = samplePhotoPostMessages.get(rand.nextInt(samplePhotoPostMessages.size()));
		String place = (rand.nextInt(2) != 0) ? samplePlaces.get(rand.nextInt(samplePlaces.size())) : null;
        int i = rand.nextInt(sampleSmallPhotoUrls.size());
        String fullUrl = sampleRegularPhotoUrls.get(i);
        String smallUrl = sampleSmallPhotoUrls.get(i);
		Date createDate = new Date(seqDate.getTime() - rand.nextInt(4 * 60 * 60 * 1000)); // post created up to 4 hrs before being received by server
		return new SpxpPhotoPost(seqDate, createDate, message, fullUrl, smallUrl, place);
	}

	private SpxpPost generateVideoPost(Date seqDate) {
		String message = sampleVideoPostMessages.get(rand.nextInt(sampleVideoPostMessages.size()));
		String place = (rand.nextInt(2) != 0) ? samplePlaces.get(rand.nextInt(samplePlaces.size())) : null;
        int i = rand.nextInt(samplePreviewVideoUrls.size());
        String mediaUrl = sampleMediaVideoUrls.get(i);
        String previewUrl = samplePreviewVideoUrls.get(i);
		Date createDate = new Date(seqDate.getTime() - rand.nextInt(4 * 60 * 60 * 1000)); // post created up to 4 hrs before being received by server
		return new SpxpVideoPost(seqDate, createDate, message, mediaUrl, previewUrl, place);
	}

	private void assignFriends(List<SpxpProfileData> profiles) throws Exception {
		LinkedList<SpxpProfileData> process = new LinkedList<>();
		process.addAll(profiles);
		while(!process.isEmpty()) {
			SpxpProfileData next = process.pollFirst();
			if(!next.wantsAdditionalFriends()) {
				continue;
			}
			LinkedList<SpxpProfileData> candidates = new LinkedList<>();
			for(SpxpProfileData pd : process) {
				if(pd.wantsAdditionalFriends() && !pd.isFriend(next)) {
					candidates.add(pd);
				}
			}
			int missingFriendCount = next.getTargetFriendCount() - next.getActualFriendCount();
			if(missingFriendCount >= candidates.size()) {
				// System.out.println("Insufficient friend candidates for "+next.getProfileName()+". Needed "+missingFriendCount+" new friends, but got only "+candidates.size()+" candidates.");
				// add all candidates
				for(SpxpProfileData pd : candidates) {
					makeFriendConnection(next, pd);
				}
			} else {
				HashSet<Integer> selectedFriendIds = new HashSet<>();
				while(selectedFriendIds.size() < missingFriendCount) {
					selectedFriendIds.add(rand.nextInt(candidates.size()));
				}
				int i = 0;
				for(SpxpProfileData pd : candidates) {
					if(selectedFriendIds.contains(i)) {
						makeFriendConnection(next, pd);
					}
					i++;
				}
			}
		}
	}

	private void makeFriendConnection(SpxpProfileData first, SpxpProfileData second) throws Exception {
		first.addFriend(second);
		second.addFriend(first);
	}

	private void generateReaderKeysAndCertificates(List<SpxpProfileData> profiles) throws Exception {
		for(SpxpProfileData profile : profiles) {
			profile.issueReaderKeyAndCertificateToAllConnected();
		}
	}
	
	private void createVirtualGroups(List<SpxpProfileData> profiles, int maxUsersPerGroup) {
		for(SpxpProfileData profile : profiles) {
			createVirtualGroupsPerProfile(profile, maxUsersPerGroup);
		}
	}
	
	private void createVirtualGroupsPerProfile(SpxpProfileData profile, int maxUsersPerGroup) {
		for(int i = 0; i < profile.getGroups().size(); i++) {
			List<SpxpFriendConnectionData> friendsOnlyInThisGroup = new LinkedList<>();
			for(SpxpFriendConnectionData friend : profile.getFriendConnections()) {
				boolean[] memberships = friend.getGroupMembership();
				if(memberships[i] && !profile.hasAccessThroughOtherGroup(friend, i)) {
					friendsOnlyInThisGroup.add(friend);
				}
			}
			int requiredGroups = ceilDiv(friendsOnlyInThisGroup.size(), maxUsersPerGroup);
			if(requiredGroups > 1) {
				int sizeBeforeExtend = profile.getGroups().size();
				int nextGroup = sizeBeforeExtend;
				profile.extendGroupsByVirtualGroups(i, requiredGroups);
				int cnt = 0;
				for(SpxpFriendConnectionData fcd : friendsOnlyInThisGroup) {
					fcd.getGroupMembership()[nextGroup] = true;
					cnt++;
					if(cnt >= maxUsersPerGroup) {
						nextGroup++;
						cnt = 0;
					}
				}
				for(int x = sizeBeforeExtend; x < profile.getGroups().size(); x++) {
					profile.addGroupInGroupMembership(x, i);
				}
			}
		}
	}
	
	private void generateRoundKeys(List<SpxpProfileData> profiles, Date now, int defaultPeriodLength, int unfriendEventsPerPeriod) throws Exception {
		for(SpxpProfileData profile : profiles) {
			generateRoundKeysPerProfile(profile, now, defaultPeriodLength, unfriendEventsPerPeriod);
		}
	}
	
	private void generateRoundKeysPerProfile(SpxpProfileData profile, Date now, int defaultPeriodLength, int unfriendEventsPerPeriod) {
		ArrayList<LinkedList<Date>> separationDates = new ArrayList<>(profile.getGroups().size());
		int groupsCnt = profile.getGroups().size();
		for(int i = 0; i < groupsCnt; i++) {
			separationDates.add(new LinkedList<Date>());
		}
		long endDateOfCurrentCycle = now.getTime() + ((30 + rand.nextInt(90)) * MS_PER_DAY);
		for(LinkedList<Date> l : separationDates) {
			l.add(new Date(endDateOfCurrentCycle));
		}
		long oldestPostCreateTs = profile.getOldestPostCreateDate().getTime();
		long currentTs = endDateOfCurrentCycle;
		while(currentTs > oldestPostCreateTs) {
			int PeriodLengthInDays = (defaultPeriodLength + rand.nextInt(5));
			long oldTs = currentTs;
			currentTs -=  PeriodLengthInDays * MS_PER_DAY;
			for(LinkedList<Date> l : separationDates) {
				l.add(new Date(currentTs));
			}
			for(int i = 0; i < unfriendEventsPerPeriod; i++) {
				long ts = oldTs - ((1+rand.nextInt(PeriodLengthInDays-2)) * MS_PER_DAY);
				SpxpFriendConnectionData randomFriendConnection = profile.getRandomFriendConnection();
				boolean[] needToRoll = getSetOfGroupKeysFriendCanDecrypt(profile, randomFriendConnection);
				for(int ii = 0; ii < groupsCnt; ii++) {
					if(needToRoll[ii]) {
						separationDates.get(ii).add(new Date(ts));
					}
				}
			}
		}
		for(LinkedList<Date> l : separationDates) {
			Collections.sort(l, Collections.reverseOrder());
		}
		for(int i = 0; i < groupsCnt; i++) {
			SpxpProfileGroupData pgd = profile.getGroups().get(i);
			Iterator<Date> it = separationDates.get(i).iterator();
			Date d = it.next();
			while(it.hasNext()) {
				Date n = it.next();
				pgd.generateRoundKeyForPeriod(n.getTime(), d.getTime());
				d = n;
			}
		}
	}

	private boolean[] getSetOfGroupKeysFriendCanDecrypt(SpxpProfileData profile, SpxpFriendConnectionData friendConnectionData) {
		boolean[] userGroupMemberships = friendConnectionData.getGroupMembership();
		boolean[] result = new boolean[profile.getGroups().size()];
		for(int i = 0; i < userGroupMemberships.length; i++) {
			if(userGroupMemberships[i]) {
				markGroupAndAllGroupsItCanAccess(profile, result, i);
			}
		}
		return result;
	}

	private void markGroupAndAllGroupsItCanAccess(SpxpProfileData profile, boolean[] result, int i) {
		result[i] = true;
		List<Integer> memberOf = profile.getGroupsAGroupIsMemberOf(i);
		if(memberOf != null) {
			for(int x : memberOf) {
				markGroupAndAllGroupsItCanAccess(profile, result, x);
			}
		}
	}

	private void loadSampleData() throws IOException {
		Tools.loadDataFromFile("dataset/sample-quotes.txt", sampleQuotes);
		Tools.loadDataFromFile("dataset/sample-places-v03.txt", samplePlaces);
		Tools.loadDataFromFile("dataset/sample-web-post-messages.txt", sampleWebPostMessages);
		Tools.loadDataFromFile("dataset/sample-web-links.txt", sampleWebLinks);
		Tools.loadDataFromFile("dataset/sample-photo-post-messages.txt", samplePhotoPostMessages);
		Tools.loadDataFromFile("dataset/sample-small-photos.txt", sampleSmallPhotoUrls);
		Tools.loadDataFromFile("dataset/sample-regular-photos.txt", sampleRegularPhotoUrls);
		Tools.loadDataFromFile("dataset/sample-video-post-messages.txt", sampleVideoPostMessages);
		Tools.loadDataFromFile("dataset/sample-preview-videos.txt", samplePreviewVideoUrls);
		Tools.loadDataFromFile("dataset/sample-media-videos.txt", sampleMediaVideoUrls);
		try(Scanner s = new Scanner(new File("dataset/sample-text-post-messages.txt")/*, "UTF-8"*/)) {
			s.useDelimiter("\\r\\n|\\n");
			while(s.hasNext()) {
				String x = Tools.cleanMessage(s.next());
				if(x != null) {
					sampleTextPostMessages.add(x);
				}
			}
			if(s.ioException() != null) {
				throw s.ioException();
			}
		}
	}
	
	public JSONArray getRandomUsers(String gender, int count) throws Exception {
		// try to use stored responses from a file to get predictable results
		// randomuser.me_2500_female.json
		if(count > 2500) {
			throw new FileNotFoundException("We have stored sample data only up to 2500 users");
		}
		JSONObject data = new JSONObject(Tools.getStringFromFile(new File("dataset/randomuser.me_2500_"+gender+".json")));
		// initial implementation getting live data
		// JSONObject data = new JSONObject(Tools.getResponseFromHttpUrl(new URL("https://randomuser.me/api/?results="+count+"&gender="+gender)));
		return data.getJSONArray("results");
	}
	
	private double nextCompressedGaussianRand() {
		double d = rand.nextGaussian() * 0.25;
		if(d < -1.0) {
			d = -1.0;
		} else if (d > 1.0) {
			d = 1.0;
		}
		return d;
	}
	
	private int ceilDiv(int x, int y) {
	    // kudos: https://stackoverflow.com/a/27643634
		return -Math.floorDiv(-x,y);
	}

}
