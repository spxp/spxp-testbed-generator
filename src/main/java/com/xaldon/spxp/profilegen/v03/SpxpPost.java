package com.xaldon.spxp.profilegen.v03;

import java.util.Date;

import org.json.JSONObject;

public interface SpxpPost {

    JSONObject toJSONObject();

    Date getSeqDate();

    Date getCreateDate();

}
