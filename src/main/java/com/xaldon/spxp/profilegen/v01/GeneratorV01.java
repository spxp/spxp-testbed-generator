package com.xaldon.spxp.profilegen.v01;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xaldon.spxp.profilegen.utils.Tools;

public class GeneratorV01 {

	private final static String TARGET_DIR_PATH = "./v0.1";

	private final static String PROFILE_ROOT_URL = "http://testbed.spxp.org/0.1/";

	private final static int GENERATED_PROFILES_COUNT = 1000; // must be even
	
	private final static boolean GENERATE_DYNAMIC_PHP_POSTS = true;
	
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
	
	private ArrayList<String> samplePhotoPostMessages = new ArrayList<String>(5);
	
	private ArrayList<String> sampleSmallPhotoUrls = new ArrayList<String>(2400);
	
	private ArrayList<String> sampleRegularPhotoUrls = new ArrayList<String>(2400);
	
	private ArrayList<String> phpCode = new ArrayList<String>(100);

	private Random rand = new Random(9876543210l);
	
	private final static long MS_PER_DAY = 24 * 60 * 60 * 1000;
	
	public static void main(String[] args) throws Exception {
		(new GeneratorV01()).run(args);
	}
	
	public void run(String[] args) throws Exception {
		System.out.println("Generating profiles...");
		File targetDir = new File(TARGET_DIR_PATH);
		if(!targetDir.exists()) {
			targetDir.mkdirs();
		}
		File targetFriendsDir = new File(targetDir, "friends");
		if(!targetFriendsDir.exists()) {
			targetFriendsDir.mkdirs();
		}
		File targetPostsDir = new File(targetDir, "posts");
		if(!targetPostsDir.exists()) {
			targetPostsDir.mkdirs();
		}
		loadSampleData();
		ArrayList<String> profileNames = new ArrayList<>(GENERATED_PROFILES_COUNT);
		ArrayList<String> fullNames = new ArrayList<>(GENERATED_PROFILES_COUNT);
		runPerGender(targetDir, "female", GENERATED_PROFILES_COUNT/2, profileNames, fullNames);
		runPerGender(targetDir, "male", GENERATED_PROFILES_COUNT/2, profileNames, fullNames);
		for(int i = 0; i < profileNames.size(); i++) {
			generateFriendsForProfile(targetFriendsDir, profileNames, i);
			generatePostsForProfile(targetPostsDir, profileNames.get(i), POSTS_PER_PROFILE_COUNT);
		}
		if(GENERATE_QR_CODES) {
			System.out.println("Generating QR codes...");
			File targetQrDir = new File(targetDir, "qr");
			if(!targetQrDir.exists()) {
				targetQrDir.mkdirs();
			}
			for(int i = 0; i < profileNames.size(); i++) {
				String profileName = profileNames.get(i);
				Tools.generateQrForProfile(new File(targetQrDir, profileName+".png"), PROFILE_ROOT_URL + profileName);
				Thread.sleep(100);
			}
		}
		writeIndexFiles(targetDir, profileNames, fullNames);
		System.out.println("Finished.");
	}

	public void runPerGender(File targetDir, String gender, int count, ArrayList<String> profileNames, ArrayList<String> fullNames) throws Exception {
		JSONArray randomUsers = getRandomUsers(gender, count);
		for(int i = 0; i < count; i++) {
			 processReandomUser(targetDir, randomUsers.getJSONObject(i), i, gender, profileNames, fullNames);
		}
	}
	
	public void processReandomUser(File targetDir, JSONObject obj, int id, String gender, ArrayList<String> profileNames, ArrayList<String> fullNames) throws Exception {
		JSONObject nameObj = obj.getJSONObject("name");
		String fullName = Tools.uppercaseFirstChar(nameObj.getString("first")) + " " + Tools.uppercaseFirstChar(nameObj.getString("last"));
		JSONObject loginObj = obj.getJSONObject("login");
		String profileName = loginObj.getString("username");
		String about = sampleQuotes.get(rand.nextInt(sampleQuotes.size()));
		String email = obj.getString("email");
		String dobDate = obj.getJSONObject("dob").getString("date");
		String birthYear = dobDate.substring(0, 4);
		String birthDayAndMonth = dobDate.substring(8, 10) + "-" + dobDate.substring(5, 7);
		JSONObject coordinates = obj.getJSONObject("location").getJSONObject("coordinates");
		String latitude = coordinates.getString("latitude");
		String longitude = coordinates.getString("longitude");
		writeSpxpProfile(targetDir, profileName, fullName, about, id, gender, email, birthDayAndMonth, birthYear, latitude, longitude);
		profileNames.add(profileName);
		fullNames.add(fullName);
	}
	
	private void writeIndexFiles(File targetDir, ArrayList<String> profileNames, ArrayList<String> fullNames)  throws Exception {
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, "profiles.txt")), StandardCharsets.UTF_8) ) {
			for(String profileName : profileNames) {
				out.write(PROFILE_ROOT_URL);
				out.write(profileName);
				out.write("\n");
			}
		}
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, "index.html")), StandardCharsets.UTF_8) ) {
			out.write("<html>\n");
			out.write("<head><meta charset=\"utf-8\"/><title>Testbed of SPXP profiles of version 0.1</title></head>\n");
			out.write("<body>\n");
			out.write("<h1>Testbed of SPXP profiles of version 0.1</h1>\n");
			for(int i = 0; i < profileNames.size(); i++) {
				String profileName = profileNames.get(i);
				String fullName = fullNames.get(i);
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
				out.write("<br/>\n");
			}
			out.write("</body>\n");
			out.write("</html>\n");
		}
	}
	
	public void writeSpxpProfile(File targetDir, String profileName, String name, String about, int id, String gender, String email, String birthDayAndMonth, String birthYear, String latitude, String longitude) throws Exception {
		int profileImageCount = gender.equals("male") ? PROFILE_IMAGE_COUNT_MALE : PROFILE_IMAGE_COUNT_FEMALE;
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, profileName)), StandardCharsets.UTF_8) ) {
			out.write("{\n");
			Tools.writeElement(out, 1, "ver", "0.1");
			Tools.writeElement(out, 1, "name", name);
			Tools.writeElement(out, 1, "about", about);
			Tools.writeElement(out, 1, "gender", gender);
			Tools.writeElement(out, 1, "website", "https://example.com");
			Tools.writeElement(out, 1, "email", email);
			Tools.writeElement(out, 1, "birthDayAndMonth", birthDayAndMonth);
			Tools.writeElement(out, 1, "birthYear", birthYear);
			Tools.writeElement(out, 1, "hometown", samplePlaces.get(rand.nextInt(samplePlaces.size())));
			Tools.writeElement(out, 1, "location", samplePlaces.get(rand.nextInt(samplePlaces.size())));
			out.write("    \"coordinates\" : {\n");
			Tools.writeElement(out, 2, "latitude", latitude);
			Tools.writeElement(out, 2, "longitude", longitude, false);
			out.write("    },\n");
			int imageid = id % profileImageCount;
			Tools.writeElement(out, 1, "profilePhoto", "images/"+gender.substring(0,1)+imageid+".jpg");
			Tools.writeElement(out, 1, "friendsEndpoint", "friends/"+profileName);
			Tools.writeElement(out, 1, "postsEndpoint", "posts/"+profileName, false);
			out.write("}");
		}
	}

	private void generateFriendsForProfile(File targetDir, ArrayList<String> profileNames, int i) throws Exception {
		// according to http://www.pewresearch.org/fact-tank/2014/02/03/what-people-like-dislike-about-facebook/
		// mean 338 / median 200
		// according to https://www.telegraph.co.uk/news/science/science-news/12108412/Facebook-users-have-155-friends-but-would-trust-just-four-in-a-crisis.html
		// average 155
		// Dunbar Number: 150
		int friendCount = 5 + rand.nextInt(200);
		ArrayList<Integer> friendIds = new ArrayList<Integer>(friendCount);
		while(friendIds.size() < friendCount) {
			int nextFriend = rand.nextInt(profileNames.size());
			if(!friendIds.contains(nextFriend)) {
				friendIds.add(nextFriend);
			}
		}
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, profileNames.get(i))), StandardCharsets.UTF_8) ) {
			out.write("{\n");
			out.write("    \"data\" : [\n");
			Iterator<Integer> itFriends = friendIds.iterator();
			while(itFriends.hasNext()) {
				out.write("        \""+PROFILE_ROOT_URL+profileNames.get(itFriends.next().intValue())+"\"");
				if(itFriends.hasNext()) {
					out.write(",");
				}
				out.write("\n");
			}
			out.write("    ]\n");
			out.write("}");
		}
	}

	private void generatePostsForProfile(File targetDir, String userName, int count) throws Exception {
		if(GENERATE_DYNAMIC_PHP_POSTS) {
			generatePostsForProfilePhp(targetDir,userName,count);
		} else {
			generatePostsForProfileJson(targetDir,userName,count);
		}
	}

	private void generatePostsForProfileJson(File targetDir, String userName, int count) throws Exception {
		long daysBetweenPosts = rand.nextInt(60);
		long msBetweenPosts = daysBetweenPosts * MS_PER_DAY;
		long ts = Tools.parsePostsDate(POSTS_START_TIMESTAMP).getTime();
		long variance = (msBetweenPosts / 2);
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, userName)), StandardCharsets.UTF_8) ) {
			out.write("{\n");
			out.write("    \"data\" : [\n");
			for(int i = 0; i < count; i++) {
				double d = (rand.nextDouble() - 0.5); // -0.5 ... +0.5
				long t = Math.round(d * variance);    // -variance/2 ... +variance/2
				long dist = msBetweenPosts + t;
				ts -= dist;
                out.write("        {\n");
                out.write("            \"timestampUTC\" : \""+Tools.formatPostsDate(ts)+"\",\n");
                switch(rand.nextInt(2)) {
                case 0: // text
                	generateTextPostJson(out);
                	break;
                case 1: // photo
                	generatePhotoPostJson(out);
                	break;
/*                case 2: // video
                	break;
                case 3: // link
                	break;
                case 4: // profile
                	break;*/
                }
                out.write("        }");
                if(i < count-1) {
                    out.write(",");
                }
                out.write("\n");
			}
			out.write("    ]\n");
			out.write("}");
		}
	}

	private void generateTextPostJson(OutputStreamWriter out) throws IOException {
        out.write("            \"type\" : \"text\",\n");
		out.write("            \"message\" : ");
		JSONObject.quote(sampleTextPostMessages.get(rand.nextInt(sampleTextPostMessages.size())), out);
        if(rand.nextInt(2) != 0) {
            out.write(",\n            \"place\" : ");
    		JSONObject.quote(samplePlaces.get(rand.nextInt(samplePlaces.size())), out);
        }
        out.write("\n");
	}

	private void generatePhotoPostJson(OutputStreamWriter out) throws IOException {
        out.write("            \"type\" : \"photo\",\n");
		out.write("            \"message\" : ");
		JSONObject.quote(samplePhotoPostMessages.get(rand.nextInt(samplePhotoPostMessages.size())), out);
        out.write(",\n");
        int i = rand.nextInt(sampleSmallPhotoUrls.size());
		out.write("            \"full\" : ");
		JSONObject.quote(sampleRegularPhotoUrls.get(i), out);
        out.write(",\n");
		out.write("            \"small\" : ");
		JSONObject.quote(sampleSmallPhotoUrls.get(i), out);
        if(rand.nextInt(2) != 0) {
            out.write(",\n            \"place\" : ");
    		JSONObject.quote(samplePlaces.get(rand.nextInt(samplePlaces.size())), out);
        }
        out.write("\n");
	}

	private void generatePostsForProfilePhp(File targetDir, String userName, int count) throws Exception {
		long daysBetweenPosts = rand.nextInt(60);
		long msBetweenPosts = daysBetweenPosts * MS_PER_DAY;
		long ts = Tools.parsePostsDate(POSTS_START_TIMESTAMP).getTime();
		long variance = (msBetweenPosts / 2);
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, userName+".php")), StandardCharsets.UTF_8) ) {
			out.write("<?php\n");
			out.write("\n");
			out.write("$data = array(\n");
			for(int i = 0; i < count; i++) {
				double d = (rand.nextDouble() - 0.5); // -0.5 ... +0.5
				long t = Math.round(d * variance);    // -variance/2 ... +variance/2
				long dist = msBetweenPosts + t;
				ts -= dist;
                out.write("    array(\n");
                out.write("        \"timestampUTC\" => \""+Tools.formatPostsDate(ts)+"\",\n");
                switch(rand.nextInt(2)) {
                case 0: // text
                	generateTextPostPhp(out);
                	break;
                case 1: // photo
                	generatePhotoPostPhp(out);
                	break;
/*                case 2: // video
                	break;
                case 3: // link
                	break;
                case 4: // profile
                	break;*/
                }
                out.write("    )");
                if(i < count-1) {
                    out.write(",");
                }
                out.write("\n");
			}
			out.write(");\n");
			out.write("\n");
			for(String s : phpCode) {
				out.write(s);
				out.write('\n');
			}
			out.write("?>\n");
		}
	}

	private void generateTextPostPhp(OutputStreamWriter out) throws IOException {
        out.write("        \"type\" => \"text\",\n");
		out.write("        \"message\" => ");
		JSONObject.quote(sampleTextPostMessages.get(rand.nextInt(sampleTextPostMessages.size())), out);
        if(rand.nextInt(2) != 0) {
            out.write(",\n        \"place\" => ");
    		JSONObject.quote(samplePlaces.get(rand.nextInt(samplePlaces.size())), out);
        }
        out.write("\n");
	}

	private void generatePhotoPostPhp(OutputStreamWriter out) throws IOException {
        out.write("        \"type\" => \"photo\",\n");
		out.write("        \"message\" => ");
		JSONObject.quote(samplePhotoPostMessages.get(rand.nextInt(samplePhotoPostMessages.size())), out);
        out.write(",\n");
        int i = rand.nextInt(sampleSmallPhotoUrls.size());
		out.write("        \"full\" => ");
		JSONObject.quote(sampleRegularPhotoUrls.get(i), out);
        out.write(",\n");
		out.write("        \"small\" => ");
		JSONObject.quote(sampleSmallPhotoUrls.get(i), out);
        if(rand.nextInt(2) != 0) {
            out.write(",\n        \"place\" => ");
    		JSONObject.quote(samplePlaces.get(rand.nextInt(samplePlaces.size())), out);
        }
        out.write("\n");
	}
	
	private void loadSampleData() throws IOException {
		Tools.loadDataFromFile("dataset/sample-quotes.txt", sampleQuotes);
		Tools.loadDataFromFile("dataset/sample-places-v01.txt", samplePlaces);
		try(Scanner s = new Scanner(new File("dataset/sample-text-post-messages.txt")/*, "UTF-8"*/)) {
			s.useDelimiter("\\r\\n|\\n");
			while(s.hasNext()) {
				String x = cleanMessage(s.next());
				if(x != null) {
					sampleTextPostMessages.add(x);
				}
			}
			if(s.ioException() != null) {
				throw s.ioException();
			}
		}
		Tools.loadDataFromFile("dataset/sample-photo-post-messages.txt", samplePhotoPostMessages);
		Tools.loadDataFromFile("dataset/sample-small-photos.txt", sampleSmallPhotoUrls);
		Tools.loadDataFromFile("dataset/sample-regular-photos.txt", sampleRegularPhotoUrls);
		Tools.loadDataFromResource("com/xaldon/spxp/profilegen/v01/php-code.txt", phpCode);
	}

	private String cleanMessage(String in) {
		int p = in.lastIndexOf(" ");
		if(p > 0) {
			p = in.lastIndexOf(" ", p-1);
		}
		if(p >= 0) {
			in = in.substring(0, p);
		}
		in = in.trim();
		String test = in.toLowerCase();
		if(test.contains("http://") || test.contains("https://")) {
			return null;
		}
		return in;
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

}
