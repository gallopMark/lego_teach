package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 创建日期：2017/1/3 on 14:06
 * 描述:
 * 作者:马飞奔 Administrator
 */

/**
 * mWorkshop	工作坊	MWorkshop	N
 * mUser	用户	Object	Y
 * role	角色	String	Y	member:助理
 * master:坊主
 * student:学员
 * evaluate	评价	String	N	null:未评价
 * excellent：优秀
 * qualified：合格
 * fail：不合格
 * point	积分	BigDecimal	N
 * completeActivityNum	完成活动数量	int	N
 * faqQuestionNum	提出问题数量	int	 N
 * uploadResourceNum	上传资源数	Int	N
 */
public class WorkShopMobileUser implements Serializable {
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("mWorkshop")
    private WorkShopMobileEntity mWorkshop;
    @Expose
    @SerializedName("mUser")
    private MobileUser mUser;
    @Expose
    @SerializedName("role")
    private String role;
    @Expose
    @SerializedName("evaluate")
    private String evaluate;
    @Expose
    @SerializedName("point")
    private double point;
    @Expose
    @SerializedName("completeActivityNum")
    private int completeActivityNum;
    @Expose
    @SerializedName("faqQuestionNum")
    private int faqQuestionNum;
    @Expose
    @SerializedName("uploadResourceNum")
    private int uploadResourceNum;
    @Expose
    @SerializedName("evaluateCreator")
    private MobileUser evaluateCreator;
    @Expose
    @SerializedName("state")
    private String state;
    @Expose
    @SerializedName("finallyResult")
    private String finallyResult;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WorkShopMobileEntity getmWorkshop() {
        return mWorkshop;
    }

    public void setmWorkshop(WorkShopMobileEntity mWorkshop) {
        this.mWorkshop = mWorkshop;
    }

    public MobileUser getmUser() {
        return mUser;
    }

    public void setmUser(MobileUser mUser) {
        this.mUser = mUser;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEvaluate() {
        return evaluate;
    }

    public void setEvaluate(String evaluate) {
        this.evaluate = evaluate;
    }

    public double getPoint() {
        return point;
    }

    public void setPoint(double point) {
        this.point = point;
    }

    public int getCompleteActivityNum() {
        return completeActivityNum;
    }

    public void setCompleteActivityNum(int completeActivityNum) {
        this.completeActivityNum = completeActivityNum;
    }

    public int getFaqQuestionNum() {
        return faqQuestionNum;
    }

    public void setFaqQuestionNum(int faqQuestionNum) {
        this.faqQuestionNum = faqQuestionNum;
    }

    public int getUploadResourceNum() {
        return uploadResourceNum;
    }

    public void setUploadResourceNum(int uploadResourceNum) {
        this.uploadResourceNum = uploadResourceNum;
    }

    public MobileUser getEvaluateCreator() {
        return evaluateCreator;
    }

    public void setEvaluateCreator(MobileUser evaluateCreator) {
        this.evaluateCreator = evaluateCreator;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFinallyResult() {
        return finallyResult;
    }

    public void setFinallyResult(String finallyResult) {
        this.finallyResult = finallyResult;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (obj instanceof WorkShopMobileUser) {
            WorkShopMobileUser entity = (WorkShopMobileUser) obj;
            return entity.id.equals(this.id);
        }
        return false;
    }
}
