package com.xaldon.spxp.profilegen.v02;

import java.util.Date;

import org.json.JSONObject;

import com.xaldon.spxp.profilegen.utils.Tools;

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
        JSONObject result = Tools.newOrderPreservingJSONObject();
        result.put("timestampUTC", Tools.formatPostsDate(postDate));
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
