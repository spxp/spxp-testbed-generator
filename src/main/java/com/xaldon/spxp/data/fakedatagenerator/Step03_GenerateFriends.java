package com.xaldon.spxp.data.fakedatagenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Step03_GenerateFriends {

	Random rand = new Random(9876543210l);

	public static void main(String[] args) throws Exception {
		(new Step03_GenerateFriends()).run(args);
	}

	public void run(String[] args) throws Exception {
		System.out.println("Starting...");
		File inputDir = new File("spxp");
		if(!inputDir.exists()) {
			System.out.println("ERROR no input dir. Cannot find ./spxp");
			return;
		}
		File targetDir = new File(inputDir, "friends");
		if(!targetDir.exists()) {
			targetDir.mkdirs();
		}
		String[] userNames = Tools.getUsernames(inputDir);
		System.out.println("Found "+userNames.length+" users");
		for(int i = 0; i < userNames.length; i++) {
			processUser(targetDir, userNames, i);
		}
		System.out.println("Finished.");
	}

	private void processUser(File targetDir, String[] userNames, int i) throws Exception {
		// according to http://www.pewresearch.org/fact-tank/2014/02/03/what-people-like-dislike-about-facebook/
		// mean 338 / median 200
		// according to https://www.telegraph.co.uk/news/science/science-news/12108412/Facebook-users-have-155-friends-but-would-trust-just-four-in-a-crisis.html
		// average 155
		// Dunbar Number: 150
		int friendCount = 5 + rand.nextInt(200);
		ArrayList<Integer> friendIds = new ArrayList<Integer>(friendCount);
		while(friendIds.size() < friendCount) {
			int nextFriend = rand.nextInt(userNames.length);
			if(!friendIds.contains(nextFriend)) {
				friendIds.add(nextFriend);
			}
		}
		try( OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(targetDir, userNames[i])), StandardCharsets.UTF_8) ) {
			out.write("{\n");
			out.write("    \"data\" : [\n");
			Iterator<Integer> itFriends = friendIds.iterator();
			while(itFriends.hasNext()) {
				out.write("        \"http://xaldon.com/spxp/"+userNames[itFriends.next().intValue()]+"\"");
				if(itFriends.hasNext()) {
					out.write(",");
				}
				out.write("\n");
			}
			out.write("    ]\n");
			out.write("}");
		}
	}

}
