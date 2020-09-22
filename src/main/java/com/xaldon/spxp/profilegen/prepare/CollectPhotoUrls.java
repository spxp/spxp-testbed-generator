package com.xaldon.spxp.profilegen.prepare;

import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xaldon.spxp.profilegen.utils.Tools;

public class CollectPhotoUrls {

    // see also https://www.pexels.com/
    
    public static void main(String[] args) throws Exception {
        ArrayList<String> small = new ArrayList<String>(2400);
        ArrayList<String> regular = new ArrayList<String>(2400);
        for(int i = 0; i < 200; i++) {
            System.out.print(".");
            JSONArray data = new JSONArray(Tools.getResponseFromHttpUrl(new URL("https://unsplash.com/napi/photos?page="+i+"&per_page=12&order_by=latest")));
            for(int x = 0; x < data.length(); x++) {
                JSONObject imageData = data.getJSONObject(x);
                JSONObject urls = imageData.getJSONObject("urls");
                String smallUrl = urls.getString("small");
                String regularUrl = urls.getString("regular");
                small.add(smallUrl);
                regular.add(regularUrl);
            }
        }
        System.out.println();
        System.out.println("Collected "+small.size()+" URLs:");
        System.out.println("Small:");
        for(String s : small) {
            System.out.println(s);
        }
        System.out.println("Regular:");
        for(String r : regular) {
            System.out.println(r);
        }
    }

}
