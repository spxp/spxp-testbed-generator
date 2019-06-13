package com.xaldon.spxp.data.fakedatagenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.json.JSONObject;

public class Tools {
	
	private Tools() {
		// prevent instantiation
	}
	
	public static void copyStreams(InputStream in, OutputStream out) throws IOException {
        int bytesRead = -1;
        byte[] buffer = new byte[4096];
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
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

}
