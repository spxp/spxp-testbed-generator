package com.xaldon.spxp.data.fakedatagenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.TimeZone;

import org.json.JSONObject;

public class Step04_GeneratePosts {

	public static void main(String[] args) throws Exception {
		(new Step04_GeneratePosts()).run(args);
	}

	private Random rand = new Random(9876543210l);
	
	private ArrayList<String> sampleTextPostMessages = new ArrayList<String>(1000);
	
	private ArrayList<String> samplePhotoPostMessages = new ArrayList<String>(5);
	
	private ArrayList<String> samplePlaces = new ArrayList<String>(5);
	
	private ArrayList<String> sampleSmallPhotoUrls = new ArrayList<String>(2400);
	
	private ArrayList<String> sampleRegularPhotoUrls = new ArrayList<String>(2400);

	public void run(String[] args) throws Exception {
		System.out.println("Starting...");
		File inputDir = new File("spxp");
		if(!inputDir.exists()) {
			System.out.println("ERROR no input dir. Cannot find ./spxp");
			return;
		}
		File targetDir = new File(inputDir, "posts");
		if(!targetDir.exists()) {
			targetDir.mkdirs();
		}
		String[] userNames = Tools.getUsernames(inputDir);
		loadSampleData();
		System.out.println("Found "+userNames.length+" users");
		for(int i = 0; i < userNames.length; i++) {
			processUser(targetDir, userNames[i], 500);
		}
		System.out.println("Finished.");
	}
	
	private void loadSampleData() throws IOException {
		try(Scanner s = new Scanner(new File("sample-text-post-messages.txt")/*, "UTF-8"*/)) {
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
		Tools.loadDataFromFile("sample-photo-post-messages.txt", samplePhotoPostMessages);
		Tools.loadDataFromFile("sample-places.txt", samplePlaces);
		Tools.loadDataFromFile("sample-small-photos.txt", sampleSmallPhotoUrls);
		Tools.loadDataFromFile("sample-regular-photos.txt", sampleRegularPhotoUrls);
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
	
	public long MS_PER_DAY = 24 * 60 * 60 * 1000;

	private void processUser(File targetDir, String userName, int count) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		long daysBetweenPosts = rand.nextInt(60);
		long msBetweenPosts = daysBetweenPosts * MS_PER_DAY;
		long ts = System.currentTimeMillis();
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
                out.write("            \"timestampUTC\" : \""+sdf.format(new Date(ts))+"\",\n");
                switch(rand.nextInt(2)) {
                case 0: // text
                	generateTextPost(out);
                	break;
                case 1: // photo
                	generatePhotoPost(out);
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

	private void generateTextPost(OutputStreamWriter out) throws IOException {
        out.write("            \"type\" : \"text\",\n");
		out.write("            \"message\" : ");
		JSONObject.quote(sampleTextPostMessages.get(rand.nextInt(sampleTextPostMessages.size())), out);
        if(rand.nextInt(2) != 0) {
            out.write(",\n            \"place\" : ");
    		JSONObject.quote(samplePlaces.get(rand.nextInt(samplePlaces.size())), out);
        }
        out.write("\n");
	}

	private void generatePhotoPost(OutputStreamWriter out) throws IOException {
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

}
