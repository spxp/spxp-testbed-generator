package com.xaldon.spxp.profilegen.v03;

import java.util.Date;

import org.json.JSONObject;

import com.xaldon.spxp.profilegen.utils.Tools;

public class SpxpVideoPost implements SpxpPost {
	
	private Date seqDate;
	
	private Date createDate;
	
	private String message;
	
	private String mediaUrl;
	
	private String previewUrl;
	
	private String place;

	public SpxpVideoPost(Date seqDate, Date createDate, String message, String mediaUrl, String previewUrl, String place) {
		this.seqDate = seqDate;
		this.createDate = createDate;
		this.message = message;
		this.mediaUrl = mediaUrl;
		this.previewUrl = previewUrl;
		this.place = place;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject result = Tools.newOrderPreservingJSONObject();
		result.put("createts", Tools.formatPostsDate(createDate));
		result.put("type", "video");
		result.put("message", message);
		result.put("preview", previewUrl);
		result.put("media", mediaUrl);
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
