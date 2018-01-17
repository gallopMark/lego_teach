package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/2/6 on 12:38
 * 描述:已领取作业列表
 * 作者:马飞奔 Administrator
 */
public class CourseHomeworks extends BaseResponseResult<CourseHomeworks.MData> {

    public class MData {
        @Expose
        @SerializedName("mAssignmentUsers")
        private List<MAssignmentUser> mAssignmentUsers;  //作业列表
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<MAssignmentUser> getmAssignmentUsers() {
            if (mAssignmentUsers == null)
                return new ArrayList<>();
            return mAssignmentUsers;
        }

        public void setmAssignmentUsers(List<MAssignmentUser> mAssignmentUsers) {
            this.mAssignmentUsers = mAssignmentUsers;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }

}
