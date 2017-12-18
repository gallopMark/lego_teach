package com.haoyu.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.haoyu.app.activity.MFileInfoActivity;
import com.haoyu.app.activity.ResourcesUploadActivity;
import com.haoyu.app.adapter.PageResourcesAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.entity.CourseResourceListResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.ResourcesEntity;
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
 * 创建日期：2016/11/29 on 17:11
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class PageResourcesFragment extends BaseFragment implements XRecyclerView.LoadingListener {
    @BindView(R.id.loadView)
    LoadingView loadView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.li_emptyResources)
    LinearLayout emptyResources; // 空资源
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.bt_upload)
    Button bt_upload;
    private boolean isLoadMore = false;
    private boolean isRefresh = false;
    private List<ResourcesEntity> resourcesList = new ArrayList<>();
    private PageResourcesAdapter adapter;
    private String courseId;
    private int limit = 20;
    private int page = 1; // 按页数查询
    private String orders = "CREATE_TIME.DESC";

    @Override
    public int createView() {
        return R.layout.fragment_page_resources;
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            courseId = bundle.getString("entityId");
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new PageResourcesAdapter(context, resourcesList);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(this);
        bt_upload.setVisibility(View.VISIBLE);
    }

    @Override
    public void initData() {
        if (!isRefresh && !isLoadMore) {
            loadView.setVisibility(View.VISIBLE);
        }
        String url = Constants.OUTRT_NET + "/m/resource/ncts?resourceRelations[0].relation.id="
                + courseId + "&resourceRelations[0].relation.type=course" + "&page=" + page
                + "&limit=" + limit + "&orders=" + orders;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<CourseResourceListResult>() {
            @Override
            public void onError(Request request, Exception e) {
                xRecyclerView.refreshComplete(false);
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    xRecyclerView.loadMoreComplete(false);
                    page -= 1;
                } else {
                    loadView.setVisibility(View.GONE);
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onResponse(CourseResourceListResult response) {
                hideTipDialog();
                loadView.setVisibility(View.GONE);
                xRecyclerView.setVisibility(View.VISIBLE);
                if (response != null && response.getResponseData() != null
                        && response.getResponseData().getResources() != null
                        && response.getResponseData().getResources().size() > 0) {
                    updateUI(response.getResponseData().getResources(), response.getResponseData().getPaginator());
                } else {
                    xRecyclerView.setVisibility(View.GONE);
                    emptyResources.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateUI(List<ResourcesEntity> resources, Paginator paginator) {
        if (isRefresh) {
            resourcesList.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        resourcesList.addAll(resources);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        adapter.setCallBack(new PageResourcesAdapter.OpenResourceCallBack() {
            @Override
            public void open(String resourcesName, MFileInfo mFileInfo) {
                mFileInfo.setFileName(resourcesName);
                Intent intent = new Intent(context, MFileInfoActivity.class);
                intent.putExtra("fileInfo", mFileInfo);
                startActivity(intent);
            }
        });
        bt_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ResourcesUploadActivity.class);
                intent.putExtra("courseId", courseId);
                startActivityForResult(intent, 1);
            }
        });
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            ResourcesEntity entity = (ResourcesEntity) data.getSerializableExtra("entity");
            resourcesList.add(0, entity);
            adapter.notifyDataSetChanged();
            emptyResources.setVisibility(View.GONE);
            if (xRecyclerView.getVisibility() == View.GONE) {
                xRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}
