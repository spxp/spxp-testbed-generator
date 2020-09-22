package com.xaldon.spxp.profilegen.v03;

import java.util.Date;

import org.json.JSONObject;

import com.xaldon.spxp.profilegen.utils.Tools;

public class SpxpWebPost implements SpxpPost {
    
    private Date seqDate;
    
    private Date createDate;
    
    private String message;
    
    private String link;

    public SpxpWebPost(Date seqDate, Date createDate, String message, String link) {
        this.seqDate = seqDate;
        this.createDate = createDate;
        this.message = message;
        this.link = link;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject result = Tools.newOrderPreservingJSONObject();
        result.put("createts", Tools.formatPostsDate(createDate));
        result.put("type", "web");
        if(message != null) {
            result.put("message", message);
        }
        result.put("link", link);
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
