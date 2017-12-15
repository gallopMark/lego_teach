package com.haoyu.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.WSMemberAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.WSMobileUsers;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/12/14.
 * 描述:工作坊成员管理
 * 作者:xiaoma
 */

public class WSManagerMebActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private WSManagerMebActivity context;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.container)
    RelativeLayout container;
    @BindView(R.id.tv_topTips)
    TextView tv_topTips;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    private String workshopId;
    private int page = 1, limit = 20;
    private boolean isRefresh, isLoadMore, firstLoad = true;
    private List<WorkShopMobileUser> mDatas = new ArrayList<>();
    private WSMemberAdapter adapter;
    private DeleteReceiver receiver;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_wsmanagermeb;
    }

    @Override
    public void initView() {
        context = this;
        workshopId = getIntent().getStringExtra("workshopId");
        setToolBar();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new WSMemberAdapter(context, mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(context);
    }

    private void setToolBar() {
        toolBar.setTitle_text("管理成员");
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                Intent intent = new Intent(context, WSSearchMebActivity.class);
                intent.putExtra("workshopId", workshopId);
                startActivity(intent);
                overridePendingTransition(0, 0);//用于屏蔽 activity 默认的转场动画效果
                receiver = new DeleteReceiver();
                IntentFilter filter = new IntentFilter("delete");
                registerReceiver(receiver, filter);
            }
        });
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/m/workshop_user/" + workshopId + "/members?page=" + page + "&limit=" + limit;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<WSMobileUsers>() {
            @Override
            public void onBefore(Request request) {
                if (isRefresh || isLoadMore) {
                    loadingView.setVisibility(View.GONE);
                } else {
                    loadingView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    page -= 1;
                    xRecyclerView.loadMoreComplete(false);
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onResponse(WSMobileUsers response) {
                loadingView.setVisibility(View.GONE);
                if (container.getVisibility() != View.VISIBLE) {
                    container.setVisibility(View.VISIBLE);
                }
                if (response != null && response.getResponseData() != null && firstLoad) {
                    setTopTips(response.getResponseData().getPaginator());
                    firstLoad = false;
                }
                if (response != null && response.getResponseData() != null && response.getResponseData().getWorkshopUsers().size() > 0) {
                    updateUI(response.getResponseData().getWorkshopUsers(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    } else {
                        tv_empty.setVisibility(View.VISIBLE);
                        xRecyclerView.setVisibility(View.GONE);
                    }
                }
            }
        }));
    }

    private void setTopTips(Paginator paginator) {
        int total = 0;
        if (paginator != null) {
            total = paginator.getTotalCount();
        }
        tv_topTips.setText("共" + total + "名成员");
    }

    private void updateUI(List<WorkShopMobileUser> users, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE) {
            xRecyclerView.setVisibility(View.VISIBLE);
        }
        if (isRefresh) {
            mDatas.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        mDatas.addAll(users);
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
        adapter.setOnItemDeleteListener(new WSMemberAdapter.OnItemDeleteListener() {
            @Override
            public void onItemDelete(WorkShopMobileUser entity, int position) {
                delete(entity, position);
            }
        });
    }

    private void delete(WorkShopMobileUser entity, final int position) {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/m/workshop_user/" + entity.getId();
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    mDatas.remove(position);
                    adapter.notifyItemRemoved(position);
                    if (mDatas.size() == 0) {
                        xRecyclerView.setVisibility(View.GONE);
                        tv_empty.setVisibility(View.VISIBLE);
                    }
                }
            }
        }, new OkHttpClientManager.Param("_method", "delete")));
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

    private class DeleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("delete")) {
                WorkShopMobileUser entity = (WorkShopMobileUser) intent.getSerializableExtra("entity");
                if (mDatas.contains(entity)) {
                    mDatas.remove(entity);
                    adapter.notifyDataSetChanged();
                }
                if (mDatas.size() == 0) {
                    xRecyclerView.setVisibility(View.GONE);
                    tv_empty.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }
}
