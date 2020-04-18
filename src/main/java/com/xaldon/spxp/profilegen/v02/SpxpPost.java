package com.xaldon.spxp.profilegen.v02;

import java.util.Date;

import org.json.JSONObject;

public interface SpxpPost {

	JSONObject toJSONObject();

	Date getPostDate();

}
