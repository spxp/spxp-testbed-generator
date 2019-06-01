package com.xaldon.spxp.data.fakedatagenerator_crypto;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

public class SpxpPhotoPost implements SpxpPost {
	
	private Date postDate;
	
	private String message;
	
	private String fullUrl;
	
	private String smallUrl;
	
	private String place;

	public SpxpPhotoPost(Date postDate, String message, String fullUrl, String smallUrl, String place) {
		this.postDate = postDate;
		this.message = message;
		this.fullUrl = fullUrl;
		this.smallUrl = smallUrl;
		this.place = place;
	}

	@Override
	public JSONObject toJSONObject() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		JSONObject result = new JSONObject();
		result.put("timestampUTC", sdf.format(postDate));
		result.put("type", "photo");
		result.put("message", message);
		result.put("full", fullUrl);
		result.put("small", smallUrl);
		if(place != null) {
			result.put("place", place);
		}
		return result;
	}

	@Override
	public Date getPostDate() {
		return postDate;
	}

}
