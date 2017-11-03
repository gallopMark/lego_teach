package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by acer1 on 2017/1/7.
 */
public class MAssignmentUser implements Serializable {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("mAssignment")
    private MAssignmentEntity mAssignment;
    @Expose
    @SerializedName("state")
    private String state;
    @Expose
    @SerializedName("responseTime")
    private Long responseTime;
    @Expose
    @SerializedName("responseScore")
    private double responseScore;
    @Expose
    @SerializedName("markNum")
    private int markNum;
    @Expose
    @SerializedName("markScore")
    private double markScore;
    @Expose
    @SerializedName("markedNum")
    private int markedNum;
    @Expose
    @SerializedName("assignmentRelationId")
    private String assignmentRelationId;
    @Expose
    @SerializedName("mFileInfos")
    private List<MFileInfo> mFileInfos;
    @Expose
    @SerializedName("mUser")
    private MobileUser mUser;
    @Expose
    @SerializedName("comment")
    private String comment;  //评语
    @Expose
    @SerializedName("isExcellent")
    private boolean isExcellent;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MAssignmentEntity getmAssignment() {
        return mAssignment;
    }

    public void setmAssignment(MAssignmentEntity mAssignment) {
        this.mAssignment = mAssignment;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public double getResponseScore() {
        return responseScore;
    }

    public void setResponseScore(double responseScore) {
        this.responseScore = responseScore;
    }

    public int getMarkNum() {
        return markNum;
    }

    public void setMarkNum(int markNum) {
        this.markNum = markNum;
    }

    public double getMarkScore() {
        return markScore;
    }

    public void setMarkScore(double markScore) {
        this.markScore = markScore;
    }

    public int getMarkedNum() {
        return markedNum;
    }

    public void setMarkedNum(int markedNum) {
        this.markedNum = markedNum;
    }

    public String getAssignmentRelationId() {
        return assignmentRelationId;
    }

    public void setAssignmentRelationId(String assignmentRelationId) {
        this.assignmentRelationId = assignmentRelationId;
    }

    public List<MFileInfo> getmFileInfos() {
        return mFileInfos;
    }

    public void setmFileInfos(List<MFileInfo> mFileInfos) {
        this.mFileInfos = mFileInfos;
    }

    public MobileUser getmUser() {
        return mUser;
    }

    public void setmUser(MobileUser mUser) {
        this.mUser = mUser;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isExcellent() {
        return isExcellent;
    }

    public void setExcellent(boolean excellent) {
        isExcellent = excellent;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj.getClass() == MAssignmentUser.class) {
            return ((MAssignmentUser) obj).id.equals(this.id);
        }
        return false;
    }
}