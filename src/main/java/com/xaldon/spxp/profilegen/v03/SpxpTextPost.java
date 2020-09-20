package com.xaldon.spxp.profilegen.v03;

import java.util.Date;

import org.json.JSONObject;

import com.xaldon.spxp.profilegen.utils.Tools;

public class SpxpTextPost implements SpxpPost {
	
	private Date seqDate;
	
	private Date createDate;
	
	private String message;
	
	private SpxpProfileReference place;

	public SpxpTextPost(Date seqDate, Date createDate, String message, SpxpProfileReference place) {
		this.seqDate = seqDate;
		this.createDate = createDate;
		this.message = message;
		this.place = place;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject result = Tools.newOrderPreservingJSONObject();
		result.put("createts", Tools.formatPostsDate(createDate));
		result.put("type", "text");
		result.put("message", message);
		if(place != null) {
			result.put("place", place.toJSONObject());
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
