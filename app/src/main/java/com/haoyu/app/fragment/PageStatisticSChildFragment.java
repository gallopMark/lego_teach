package com.haoyu.app.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.haoyu.app.adapter.CourseRegisterStatsAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.entity.CourseRegisterStats;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.StudentStatisticListResult;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/10/31
 * 描述:统计信息  全部、合格和不合格
 * 作者:马飞奔 Administrator
 */
public class PageStatisticSChildFragment extends BaseFragment implements XRecyclerView.LoadingListener {
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.emptyView)
    TextView emptyView;
    private String courseId;
    private int type = 1;
    private int totalCount;
    private String baseUrl;
    private int page = 1;
    private boolean isRefresh, isLoadMore;
    private List<CourseRegisterStats> mDatas = new ArrayList<>();
    private CourseRegisterStatsAdapter adapter;

    @Override
    public int createView() {
        return R.layout.fragment_list;
    }

    @Override
    public void initView(View view) {
        courseId = getArguments().getString("courseId");
        type = getArguments().getInt("type", 1);
        totalCount = getArguments().getInt("totalCount", 0);
        if (type == 1) {  //全部
            baseUrl = Constants.OUTRT_NET + "/" + courseId + "/teach/m/course_register_stat/" + courseId;
        } else if (type == 2) { //合格
            baseUrl = Constants.OUTRT_NET + "/" + courseId + "/teach/m/course_register_stat/" + courseId + "&courseResultState=pass";
        } else { //不合格
            baseUrl = Constants.OUTRT_NET + "/" + courseId + "/teach/m/course_register_stat/" + courseId + "&courseResultState=nopass";
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new CourseRegisterStatsAdapter(context, mDatas, totalCount);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(this);
        emptyView.setText("没有统计信息~");
    }

    @Override
    public void initData() {
        String url = baseUrl + "&page=" + page;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<StudentStatisticListResult>() {
            @Override
            public void onBefore(Request request) {
                if (!isRefresh && !isLoadMore) {
                    loadingView.setVisibility(View.VISIBLE);
                } else {
                    loadingView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                if (loadingView.getVisibility() != View.GONE)
                    loadingView.setVisibility(View.GONE);
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    xRecyclerView.loadMoreComplete(false);
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onResponse(StudentStatisticListResult response) {
                if (loadingView.getVisibility() != View.GONE)
                    loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().getmCourseRegisterStats() != null
                        && response.getResponseData().getmCourseRegisterStats().size() > 0) {
                    updateUI(response.getResponseData().getmCourseRegisterStats(), response.getResponseData().getPaginator());
                } else {
                    xRecyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateUI(List<CourseRegisterStats> stats, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE)
            xRecyclerView.setVisibility(View.VISIBLE);
        if (isRefresh) {
            mDatas.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        mDatas.addAll(stats);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void onRefresh() {
        isRefresh = true;
        isLoadMore = false;
        page = 1;
        initData();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        page += 1;
        initData();
    }
}
