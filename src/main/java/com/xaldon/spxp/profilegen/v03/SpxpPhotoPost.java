package com.xaldon.spxp.profilegen.v03;

import java.util.Date;

import org.json.JSONObject;

import com.xaldon.spxp.profilegen.utils.Tools;

public class SpxpPhotoPost implements SpxpPost {
	
	private Date seqDate;
	
	private Date createDate;
	
	private String message;
	
	private String fullUrl;
	
	private String smallUrl;
	
	private String place;

	public SpxpPhotoPost(Date seqDate, Date createDate, String message, String fullUrl, String smallUrl, String place) {
		this.seqDate = seqDate;
		this.createDate = createDate;
		this.message = message;
		this.fullUrl = fullUrl;
		this.smallUrl = smallUrl;
		this.place = place;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject result = Tools.newOrderPreservingJSONObject();
		result.put("createts", Tools.formatPostsDate(createDate));
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
	public Date getSeqDate() {
		return seqDate;
	}

	@Override
	public Date getCreateDate() {
		return createDate;
	}

}
