package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Announcement implements Serializable {
    @Expose
    @SerializedName("content")
    private String content;
    @Expose
    @SerializedName("createTime")
    private Long createTime;
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("hadView")
    private boolean hadView;

    public String getContent() {
        return content;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isHadView() {
        return hadView;
    }

    public void setHadView(boolean hadView) {
        this.hadView = hadView;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj.getClass() == Announcement.class) {
            return ((Announcement) obj).id.equals(this.id);
        }
        return false;
    }
}
