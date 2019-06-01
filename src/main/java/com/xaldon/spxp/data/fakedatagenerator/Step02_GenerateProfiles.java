package com.xaldon.spxp.data.fakedatagenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

public class Step02_GenerateProfiles {

	private final static int PROFILE_IMAGE_COUNT_FEMALE = 200;

	private final static int PROFILE_IMAGE_COUNT_MALE = 200;
	
	private ArrayList<String> sampleQuotes = new ArrayList<String>(1000);
	
	private ArrayList<String> samplePlaces = new ArrayList<String>(5);

	Random rand = new Random(9876543210l);
	
	public static void main(String[] args) throws Exception {
		(new Step02_GenerateProfiles()).run(args);
	}
	
	public void run(String[] args) throws Exception {
		System.out.println("Starting...");
		File targetDir = new File("spxp");
		if(!targetDir.exists()) {
			targetDir.mkdirs();
		}
		File profilesDir = new File("profiles");
		if(!profilesDir.exists()) {
			profilesDir.mkdirs();
		}
		loadSampleData();
		runPerGender(targetDir, profilesDir, "female", 500);
		runPerGender(targetDir, profilesDir, "male", 500);
		System.out.println("Finished.");
	}
	
	public void runPerGender(File targetDir, File profilesDir, String gender, int count) throws Exception {
		JSONArray randomUsers = getRandomUsers(gender, count);
		for(int i = 0; i < count; i++) {
			 processReandomUser(targetDir, profilesDir, randomUsers.getJSONObject(i), i, gender);
		}
	}
	
	public void processReandomUser(File targetDir, File profilesDir, JSONObject obj, int id, String gender) throws Exception {
		JSONObject nameObj = obj.getJSONObject("name");
		String fullName = Tools.uppercaseFirstChar(nameObj.getString("first")) + " " + Tools.uppercaseFirstChar(nameObj.getString("last"));
		JSONObject loginObj = obj.getJSONObject("login");
		String userName = loginObj.getString("username");
		String about = sampleQuotes.get(rand.nextInt(sampleQuotes.size()));
		String email = obj.getString("email");
		String dobDate = obj.getJSONObject("dob").getString("date");
		String birthYear = dobDate.substring(0, 4);
		String birthDayAndMonth = dobDate.substring(8, 10) + "-" + dobDate.substring(5, 7);
		JSONObject coordinates = obj.getJSONObject("location").getJSONObject("coordinates");
		String latitude = coordinates.getString("latitude");
		String longitude = coordinates.getString("longitude");
		writeSpxpProfile(targetDir, userName, fullName, about, id, gender, email, birthDayAndMonth, birthYear, latitude, longitude);
	}
	
	public void writeSpxpProfile(File targetDir, String profileName, String name, String about, int id, String gender, String email, String birthDayAndMonth, String birthYear, String latitude, String longitude) throws Exception {
		int profileImageCount = gender.equals("male") ? PROFILE_IMAGE_COUNT_MALE : PROFILE_IMAGE_COUNT_FEMALE;
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, profileName)), StandardCharsets.UTF_8) ) {
			out.write("{\n");
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
			Tools.writeElement(out, 1, "postsEndpoint", "posts/"+profileName);
			Tools.writeElement(out, 1, "albumsEndpoint", "albums/"+profileName, false);
			out.write("}");
		}
	}
	
	private void loadSampleData() throws IOException {
		Tools.loadDataFromFile("sample-quotes.txt", sampleQuotes);
		Tools.loadDataFromFile("sample-places.txt", samplePlaces);
	}
	
	public JSONArray getRandomUsers(String gender, int count) throws Exception {
		// try to use stored responses from a file to get predictable results
		// randomuser.me_2500_female.json
		if(count > 2500) {
			throw new FileNotFoundException("We have stored sample data only up to 2500 users");
		}
		JSONObject data = new JSONObject(Tools.getStringFromFile(new File("randomuser.me_2500_"+gender+".json")));
		// initial implementation getting live data
		// JSONObject data = new JSONObject(Tools.getResponseFromHttpUrl(new URL("https://randomuser.me/api/?results="+count+"&gender="+gender)));
		return data.getJSONArray("results");
	}

}
