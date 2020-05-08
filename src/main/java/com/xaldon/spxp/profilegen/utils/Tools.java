package com.xaldon.spxp.profilegen.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;

import org.json.JSONObject;

public class Tools {
	
	private final static SimpleDateFormat POSTS_DATE_FORMAT = createPostsDateFormat();
	
	private Tools() {
		// prevent instantiation
	}
	
	private static SimpleDateFormat createPostsDateFormat() {
		SimpleDateFormat result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		result.setTimeZone(TimeZone.getTimeZone("UTC"));
		return result;
	}
	
	public static String formatPostsDate(Date  d) {
		return POSTS_DATE_FORMAT.format(d);
	}
	
	public static String formatPostsDate(long ts) {
		return POSTS_DATE_FORMAT.format(new Date(ts));
	}
	
	public static Date parsePostsDate(String s) throws ParseException {
		return POSTS_DATE_FORMAT.parse(s);
	}
	
	public static void copyStreams(InputStream in, OutputStream out) throws IOException {
        int bytesRead = -1;
        byte[] buffer = new byte[4096];
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
	}

	public static void generateQrForProfile(File targetFile, String profileUrl) throws Exception {
		String encodedProfileUrl = URLEncoder.encode(profileUrl, "UTF-8");
		// http://goqr.me/api/doc/create-qr-code/
		String apiUrl = "https://api.qrserver.com/v1/create-qr-code/?data="+encodedProfileUrl+"&size=200x200&format=png";
		Tools.downloadTo(new URL(apiUrl), targetFile);
	}

	public static void downloadTo(URL url, File target) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try( FileOutputStream fos = new FileOutputStream(target) ) {
        	try( InputStream inputStream = conn.getInputStream() ) {
        		copyStreams(inputStream, fos);
        	}
        }
	}

	public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in, "UTF-8");
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }

	public static String getStringFromFile(File f) throws IOException {
		try(Scanner s = new Scanner(f, "UTF-8")) {
			s.useDelimiter("\\Z");
			return s.next();
		}
    }
	
	public static String[] getUsernames(File inputDir) {
		File[] allFiles = inputDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File arg) {
				return arg.isFile();
			}});
		String[] userNames = new String[allFiles.length];
		for(int i = 0; i < allFiles.length; i++) {
			userNames[i] = allFiles[i].getName();
		}
		Arrays.sort(userNames);
		return userNames;
	}
	
	public static void loadDataFromFile(String fileName, List<String> target) throws IOException {
		try(Scanner s = new Scanner(new File(fileName), "UTF-8")) {
			s.useDelimiter("\\r\\n|\\n");
			while(s.hasNext()) {
				target.add(s.next());
			}
			if(s.ioException() != null) {
				throw s.ioException();
			}
		}
	}
	
	public static void loadDataFromResource(String resourceName, List<String> target) throws IOException {
		try(Scanner s = new Scanner(Tools.class.getClassLoader().getResourceAsStream(resourceName), "UTF-8")) {
			s.useDelimiter("\\r\\n|\\n");
			while(s.hasNext()) {
				target.add(s.next());
			}
			if(s.ioException() != null) {
				throw s.ioException();
			}
		}
	}
	public static void generateStaticPhpFile(String resourceName, File target) throws IOException {
		try(InputStream is = Tools.class.getClassLoader().getResourceAsStream(resourceName)) {
			try(OutputStream os = new FileOutputStream(target)) {
				copyStreams(is, os);
			}
		}
	}
	
	public static String uppercaseFirstChar(String in) {
		return in.substring(0, 1).toUpperCase() + in.substring(1);
	}

	public static void writeElement(Writer out, int level, String name, String value) throws IOException {
		writeElement(out, level, name, value, true);
	}
	
	public static void writeElement(Writer out, int level, String name, String value, boolean hasNext) throws IOException {
		for(int i=0; i < level; i++) out.write("    ");
		JSONObject.quote(name, out);
		out.write(" : ");
		JSONObject.quote(value, out);
		if(hasNext) out.write(",");
		out.write("\n");
	}
	
	public static JSONObject newOrderPreservingJSONObject() {
		JSONObject jsonObject = new JSONObject();
		makeJSONObjectOrderPreserving(jsonObject);
		return jsonObject;
	}
	
	public static JSONObject orderObject(JSONObject in, String[] keys) {
		JSONObject result = newOrderPreservingJSONObject();
		for(String key : keys) {
			result.put(key, in.get(key));
			in.remove(key);
		}
		if(!in.isEmpty()) {
			throw new IllegalArgumentException("JSONObject contains more members than expected");
		}
		return result;
	}
	
	public static void makeJSONObjectOrderPreserving(JSONObject jsonObject) {
		try {
			Field map = jsonObject.getClass().getDeclaredField("map");
			map.setAccessible(true);
			map.set(jsonObject, new LinkedHashMap<>());
			map.setAccessible(false);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String cleanMessage(String in) {
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

}
