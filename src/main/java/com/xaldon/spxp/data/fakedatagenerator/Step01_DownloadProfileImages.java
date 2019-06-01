package com.xaldon.spxp.data.fakedatagenerator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Step01_DownloadProfileImages {

	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		File targetDir = new File("spxp/images");
		if(!targetDir.exists()) {
			targetDir.mkdirs();
		}
		downloadRandomuserMe(targetDir, 0, 100);
		downloadDiverseUiCom(targetDir, 100, 100);
		//downloadTinyfacEs(targetDir, 200, 100);
		System.out.println("Finished.");
	}

	private static void downloadRandomuserMe(File targetDir, int start, int count) throws MalformedURLException {
		for(int i = 0; i < count; i++) {
			URL src = new URL("https://randomuser.me/api/portraits/men/"+i+".jpg");
			File target = new File(targetDir, "m"+(start+i)+".jpg");
			try {
				Tools.downloadTo(src, target);
			} catch (IOException e) {
				System.out.println("Failed downloading "+src);
			}
		}
		for(int i = 0; i < 100; i++) {
			URL src = new URL("https://randomuser.me/api/portraits/women/"+i+".jpg");
			File target = new File(targetDir, "f"+(start+i)+".jpg");
			try {
				Tools.downloadTo(src, target);
			} catch (IOException e) {
				System.out.println("Failed downloading "+src);
			}
		}
	}
	
	private static void downloadDiverseUiCom(File targetDir, int start, int count) throws MalformedURLException {
		int downloaded = 0;
		int next = 1;
		while( (downloaded < count) && (next < 150) ) {
			URL src = new URL("https://d3iw72m71ie81c.cloudfront.net/male-"+(next++)+".jpg"); // https://diverseui.com/
			File target = new File(targetDir, "m"+(start+downloaded)+".jpg");
			try {
				Tools.downloadTo(src, target);
				downloaded++;
			} catch (IOException e) {
				System.out.println("Failed downloading "+src);
			}
		}
		downloaded = 0;
		next = 1;
		while( (downloaded < count) && (next < 150) ) {
			URL src = new URL("https://d3iw72m71ie81c.cloudfront.net/female-"+(next++)+".jpg"); // https://diverseui.com/
			File target = new File(targetDir, "f"+(start+downloaded)+".jpg");
			try {
				Tools.downloadTo(src, target);
				downloaded++;
			} catch (IOException e) {
				System.out.println("Failed downloading "+src);
			}
		}
	}
	
	/*
	private static void downloadTinyfacEs(File targetDir, int start, int count) throws IOException {
		ArrayList<String> male = new ArrayList<String>(100);
		ArrayList<String> female = new ArrayList<String>(100);
		for(int i = 0; i < 100; i++) {
			JSONArray data = new JSONArray(Tools.getResponseFromHttpUrl(new URL("https://tinyfac.es/api/users")));
			for(int x = 0; x < data.length(); x++) {
				JSONObject userData = data.getJSONObject(x);
				String gender = userData.getString("gender");
				JSONArray avatars = userData.getJSONArray("avatars");
				JSONObject smallAvatar = avatars.getJSONObject(0);
				String smallAvatarUrl = smallAvatar.getString("url");
				if(gender.equals("male")) {
					if(!male.contains(smallAvatarUrl)) {
						male.add(smallAvatarUrl);
					}
				} else {
					if(!female.contains(smallAvatarUrl)) {
						female.add(smallAvatarUrl);
					}
				}
			}
		}
		System.out.println("Collected "+male.size()+" male avatars and "+female.size()+" female avatars.");
	}
	*/

}
