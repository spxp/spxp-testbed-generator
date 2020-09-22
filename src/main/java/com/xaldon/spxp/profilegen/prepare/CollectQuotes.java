package com.xaldon.spxp.profilegen.prepare;

import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xaldon.spxp.profilegen.utils.Tools;

public class CollectQuotes {

    public static void main(String[] args) throws Exception {
        ArrayList<String> quotes = new ArrayList<String>(1000);
        for(int i = 0; i < 100; i++) {
            System.out.print(".");
            JSONArray data = new JSONArray(Tools.getResponseFromHttpUrl(new URL("http://quotesondesign.com/wp-json/posts?filter[orderby]=rand&filter[posts_per_page]=30")));
            for(int x = 0; x < data.length(); x++) {
                JSONObject quoteData = data.getJSONObject(x);
                String quote = quoteData.getString("content");
                if(quote.startsWith("<p>")) {
                    quote = quote.substring(3);
                }
                if(quote.endsWith("</p>\n")) {
                    quote = quote.substring(0,quote.length()-5);
                }
                quote = quote.trim();
                if(quote.contains("&") || quote.contains("<") || quote.contains(">")) {
                    continue;
                }
                if(!quotes.contains(quote)) {
                    quotes.add(quote);
                }
            }
        }
        System.out.println();
        System.out.println("Collected "+quotes.size()+" quotes:");
        for(String s : quotes) {
            System.out.println(s);
        }
    }

}
