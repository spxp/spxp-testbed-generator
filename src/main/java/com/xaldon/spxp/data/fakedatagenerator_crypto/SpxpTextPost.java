package com.xaldon.spxp.data.fakedatagenerator_crypto;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

public class SpxpTextPost implements SpxpPost {
	
	private Date postDate;
	
	private String message;
	
	private String place;

	public SpxpTextPost(Date postDate, String message, String place) {
		this.postDate = postDate;
		this.message = message;
		this.place = place;
	}

	@Override
	public JSONObject toJSONObject() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		JSONObject result = new JSONObject();
		result.put("timestampUTC", sdf.format(postDate));
		result.put("type", "text");
		result.put("message", message);
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
