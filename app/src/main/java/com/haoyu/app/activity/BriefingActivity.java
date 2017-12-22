package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.haoyu.app.adapter.BriefingAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.BriefingEntity;
import com.haoyu.app.entity.BriefingsResult;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.swipe.OnActivityTouchListener;
import com.haoyu.app.swipe.RecyclerTouchListener;
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
 * 创建日期：2017/1/8 on 9:28
 * 描述:研修简报列表
 * 作者:马飞奔 Administrator
 */
public class BriefingActivity extends BaseActivity implements XRecyclerView.LoadingListener, RecyclerTouchListener.RecyclerTouchListenerHelper {
    private BriefingActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.ll_empty)
    LinearLayout ll_empty;
    @BindView(R.id.iv_add)
    ImageView iv_add;
    private List<BriefingEntity> mDatas = new ArrayList<>();
    private BriefingAdapter adapter;
    private String relationId, relationType, type;
    private int page = 1;
    private boolean isRefresh, isLoadMore;
    private int index = -1;
    private RecyclerTouchListener onTouchListener;
    private OnActivityTouchListener touchListener;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_briefing;
    }

    @Override
    public void initView() {
        relationId = getIntent().getStringExtra("relationId");
        relationType = getIntent().getStringExtra("relationType");
        type = getIntent().getStringExtra("type");
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new BriefingAdapter(mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(context);
        onTouchListener = new RecyclerTouchListener(context, xRecyclerView);
        xRecyclerView.addOnItemTouchListener(onTouchListener);
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/briefing?announcementRelations[0].relation.id=" + relationId
                + "&page=" + page + "&limit=20" + "&orders=CREATE_TIME.DESC";
        if (relationType != null) {
            url += "&announcementRelations[0].relation.type" + relationType;
        }
        if (type != null) {
            url += "&type=" + type;
        }
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BriefingsResult>() {
            @Override
            public void onBefore(Request request) {
                if (isRefresh || isLoadMore)
                    loadingView.setVisibility(View.GONE);
                else
                    loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    xRecyclerView.loadMoreComplete(false);
                    page -= 1;
                } else
                    loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(BriefingsResult researchResult) {
                loadingView.setVisibility(View.GONE);
                if (researchResult != null && researchResult.getResponseData() != null
                        && researchResult.getResponseData().getAnnouncements() != null
                        && researchResult.getResponseData().getAnnouncements().size() > 0) {
                    updateBriefList(researchResult.getResponseData().getAnnouncements(), researchResult.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                        xRecyclerView.setLoadingMoreEnabled(false);
                    } else {
                        xRecyclerView.setLoadingMoreEnabled(false);
                        xRecyclerView.setVisibility(View.GONE);
                        ll_empty.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    private void updateBriefList(List<BriefingEntity> announcements, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE)
            xRecyclerView.setVisibility(View.VISIBLE);
        if (isRefresh) {
            xRecyclerView.refreshComplete(true);
            mDatas.clear();
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        mDatas.addAll(announcements);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BriefingEditActivity.class);
                intent.putExtra("relationId", relationId);
                intent.putExtra("relationType", relationType);
                startActivityForResult(intent, 1);
            }
        });
        onTouchListener.setClickable(new RecyclerTouchListener.OnRowClickListener() {
            @Override
            public void onRowClicked(int position) {
                int select = position - 1;
                if (select >= 0 && select < mDatas.size()) {
                    String id = mDatas.get(select).getId();
                    Intent intent = new Intent(context, BriefingDetailActivity.class);
                    intent.putExtra("relationId", id);
                    startActivity(intent);
                }
            }

            @Override
            public void onIndependentViewClicked(int independentViewID, int position) {

            }
        }).setSwipeOptionViews(R.id.bt_alter, R.id.bt_delete).setSwipeable(R.id.ll_rowFG, R.id.ll_rowBG, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
            @Override
            public void onSwipeOptionClicked(int viewID, int position) {
                int selected = position - 1;
                if (viewID == R.id.bt_alter) {
                    if (selected >= 0 && selected < mDatas.size()) {
                        index = selected;
                        String briefId = mDatas.get(index).getId();
                        Intent intent = new Intent(context, BriefingEditActivity.class);
                        intent.putExtra("briefId", briefId);
                        intent.putExtra("isAlter", true);
                        startActivityForResult(intent, 2);
                    }
                } else if (viewID == R.id.bt_delete) {
                    if (selected >= 0 && selected < mDatas.size()) {
                        delete(selected);
                    }
                }
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

    private void delete(final int position) {
        String id = mDatas.get(position).getId();
        String url = Constants.OUTRT_NET + "/m/briefing/" + id;
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
                    adapter.notifyDataSetChanged();
                    if (mDatas.size() == 0) {
                        ll_empty.setVisibility(View.VISIBLE);
                        xRecyclerView.setVisibility(View.GONE);
                    }
                } else {
                    toastFullScreen("删除失败", true);
                }
            }
        }, new OkHttpClientManager.Param("_method", "delete")));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK && data != null) {
                    BriefingEntity entity = (BriefingEntity) data.getSerializableExtra("entity");
                    if (!xRecyclerView.isLoadingMoreEnabled()) {
                        mDatas.add(entity);
                        adapter.notifyDataSetChanged();
                    }
                    if (mDatas.size() > 0) {
                        if (xRecyclerView.getVisibility() != View.VISIBLE) {
                            xRecyclerView.setVisibility(View.VISIBLE);
                        }
                        if (ll_empty.getVisibility() != View.GONE) {
                            ll_empty.setVisibility(View.GONE);
                        }
                    }
                }
                break;
            case 2:
                if (resultCode == RESULT_OK && data != null) {
                    BriefingEntity entity = (BriefingEntity) data.getSerializableExtra("entity");
                    mDatas.set(index, entity);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }


    @Override
    public void setOnActivityTouchListener(OnActivityTouchListener listener) {
        touchListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (touchListener != null) touchListener.getTouchCoordinates(ev);
        return super.dispatchTouchEvent(ev);
    }
}
