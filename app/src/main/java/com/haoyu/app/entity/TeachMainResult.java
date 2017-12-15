package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/2/4 on 15:41
 * 描述: 教师版首页结果集（参与辅导的课程列表、参与管理的工作坊列表）
 * 作者:马飞奔 Administrator
 */
public class TeachMainResult extends BaseResponseResult<TeachMainResult.MData> {

    public class MData {
        @Expose
        @SerializedName("mCourses")
        private List<CourseMobileEntity> mCourses;
        @Expose
        @SerializedName("mWorkshops")
        private List<WorkShopMobileEntity> mWorkshops;

        public List<CourseMobileEntity> getmCourses() {
            if (mCourses == null) {
                return new ArrayList<>();
            }
            return mCourses;
        }

        public void setmCourses(List<CourseMobileEntity> mCourses) {
            this.mCourses = mCourses;
        }

        public List<WorkShopMobileEntity> getmWorkshops() {
            if (mWorkshops == null) {
                return new ArrayList<>();
            }
            return mWorkshops;
        }

        public void setmWorkshops(List<WorkShopMobileEntity> mWorkshops) {
            this.mWorkshops = mWorkshops;
        }
    }
}
