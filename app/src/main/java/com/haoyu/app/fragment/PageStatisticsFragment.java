package com.haoyu.app.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.entity.CourseStatisticsResult;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import butterknife.BindView;
import butterknife.BindViews;
import okhttp3.Request;


/**
 * 创建日期：2017/2/4 on 17:19
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class PageStatisticsFragment extends BaseFragment {
    private String courseId;   //课程Id
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    @BindView(R.id.contentView)
    LinearLayout contentView;
    @BindViews({R.id.course_title, R.id.course_time, R.id.course_period, R.id.course_enroll})
    TextView[] courseViews;//课程标题,课程开课时间,课程学时,课程报读人数
    @BindViews({R.id.questionNUm, R.id.answerNum, R.id.noteNum, R.id.resourcesNum, R.id.discussNum})
    TextView[] numViews;//提问数，回答数，笔记数，资源数，研讨数
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    private int totalCount;
    private FragmentManager fragmentManager;
    private PageStatisticSChildFragment f1, f2, f3;

    @Override
    public int createView() {
        return R.layout.fragment_page_statistics;
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            courseId = bundle.getString("entityId");
        }
        fragmentManager = getChildFragmentManager();
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/" + courseId + "/teach/m/course_stat/" + courseId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<CourseStatisticsResult>() {
            @Override
            public void onBefore(Request request) {
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(CourseStatisticsResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                } else {
                    tv_empty.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.rb_all:
                        setCheckIndex(1);
                        break;
                    case R.id.rb_qualified:
                        setCheckIndex(2);
                        break;
                    case R.id.rb_noqualified:
                        setCheckIndex(3);
                        break;
                }
            }
        });
    }

    private void updateUI(CourseStatisticsResult.CourseStatisticsData responseData) {
        contentView.setVisibility(View.VISIBLE);
        if (responseData.getmCourse() != null) {
            courseViews[0].setText(responseData.getmCourse().getTitle());
            if (responseData.getmCourse().getmTimePeriod() != null) {
                long startTime = responseData.getmCourse().getmTimePeriod().getStartTime();
                long endTime = responseData.getmCourse().getmTimePeriod().getEndTime();
                courseViews[1].setText("开课时间：" + TimeUtil.convertTimeOfDay(startTime, endTime));
            } else {
                courseViews[1].setText("开课时间：未设置");
            }
            courseViews[2].setText(responseData.getmCourse().getStudyHours() + "学时");
            courseViews[3].setText(responseData.getmCourse().getRegisterNum() + "人报读");
        }
        numViews[0].setText(String.valueOf(responseData.getFaqQuestionNum()));
        numViews[1].setText(String.valueOf(responseData.getFaqAnswerNum()));
        numViews[2].setText(String.valueOf(responseData.getNoteNum()));
        numViews[3].setText(String.valueOf(responseData.getResourceNum()));
        numViews[4].setText(String.valueOf(responseData.getDiscussionNum()));
        totalCount = responseData.getActivityAssignmentNum();
        setCheckIndex(1);
    }

    private void setCheckIndex(int checkIndex) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        switch (checkIndex) {
            case 1:
                if (f1 == null) {
                    f1 = new PageStatisticSChildFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("courseId", courseId);
                    bundle.putInt("type", 1);
                    bundle.putInt("totalCount", totalCount);
                    f1.setArguments(bundle);
                    transaction.add(R.id.content, f1);
                } else {
                    transaction.show(f1);
                }
                break;
            case 2:
                if (f2 == null) {
                    f2 = new PageStatisticSChildFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("courseId", courseId);
                    bundle.putInt("type", 2);
                    bundle.putInt("totalCount", totalCount);
                    f2.setArguments(bundle);
                    transaction.add(R.id.content, f2);
                } else {
                    transaction.show(f2);
                }
                break;
            case 3:
                if (f3 == null) {
                    f3 = new PageStatisticSChildFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("courseId", courseId);
                    bundle.putInt("type", 3);
                    bundle.putInt("totalCount", totalCount);
                    f3.setArguments(bundle);
                    transaction.add(R.id.content, f3);
                } else {
                    transaction.show(f3);
                }
                break;
        }
        transaction.commit();
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (f1 != null)
            transaction.hide(f1);
        if (f2 != null)
            transaction.hide(f2);
        if (f3 != null)
            transaction.hide(f3);
    }
}
