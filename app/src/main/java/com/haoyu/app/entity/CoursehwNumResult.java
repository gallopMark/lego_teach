package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

/**
 * 创建日期：2017/2/6 on 11:33
 * 描述:作业相关数量结果集
 * 作者:马飞奔 Administrator
 */
public class CoursehwNumResult extends BaseResponseResult<CoursehwNumResult.MData> {
    public class MData {
        @Expose
        @SerializedName("notReceivedNum")
        private int notReceivedNum;  //待领取数
        @Expose
        @SerializedName("markNum")
        private int markNum;  //已批阅数
        @Expose
        @SerializedName("allNum")
        private int allNum;  //全部数量
        @Expose
        @SerializedName("notMarkedNum")
        private int notMarkedNum;  //未批阅数

        public int getNotReceivedNum() {
            return notReceivedNum;
        }

        public void setNotReceivedNum(int notReceivedNum) {
            this.notReceivedNum = notReceivedNum;
        }

        public int getMarkNum() {
            return markNum;
        }

        public void setMarkNum(int markNum) {
            this.markNum = markNum;
        }

        public int getAllNum() {
            return allNum;
        }

        public void setAllNum(int allNum) {
            this.allNum = allNum;
        }

        public int getNotMarkedNum() {
            return notMarkedNum;
        }

        public void setNotMarkedNum(int notMarkedNum) {
            this.notMarkedNum = notMarkedNum;
        }
    }
}
