package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/2/7 on 11:04
 * 描述: 作业评价内容对象
 * id	评价项ID	String	Y	用于提交批阅
 * content	评价项内容	String	Y
 * score	评价项分数	Double	Y
 * <p>
 * 作者:马飞奔 Administrator
 */
public class EvaluateItemSubmissions implements Serializable {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("content")
    private String content;
    @Expose
    @SerializedName("score")
    private double score;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
