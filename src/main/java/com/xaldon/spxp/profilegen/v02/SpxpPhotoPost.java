package com.xaldon.spxp.profilegen.v02;

import java.util.Date;

import org.json.JSONObject;

import com.xaldon.spxp.profilegen.utils.Tools;

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
        JSONObject result = Tools.newOrderPreservingJSONObject();
        result.put("timestampUTC", Tools.formatPostsDate(postDate));
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
