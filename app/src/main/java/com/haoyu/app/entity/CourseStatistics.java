package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/2/8 on 16:17
 * 描述:课程学习学员信息统计类
 * 作者:马飞奔 Administrator
 */
public class CourseStatistics extends BaseResponseResult<CourseStatistics.MData> {

    public class MData {
        @Expose
        @SerializedName("mCourseRegisterStats")
        private List<CourseRegisterStats> mCourseRegisterStats;
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<CourseRegisterStats> getmCourseRegisterStats() {
            if (mCourseRegisterStats == null) {
                return new ArrayList<>();
            }
            return mCourseRegisterStats;
        }

        public void setmCourseRegisterStats(List<CourseRegisterStats> mCourseRegisterStats) {
            this.mCourseRegisterStats = mCourseRegisterStats;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
