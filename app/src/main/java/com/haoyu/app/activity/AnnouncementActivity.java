package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.adapter.AnnouncementAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.Announcement;
import com.haoyu.app.entity.Announcements;
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
 * 创建日期：2017/1/5 on 17:16
 * 描述:通知公告
 * 作者:马飞奔 Administrator
 */
public class AnnouncementActivity extends BaseActivity implements XRecyclerView.LoadingListener, RecyclerTouchListener.RecyclerTouchListenerHelper {
    private AnnouncementActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    @BindView(R.id.iv_add)
    ImageView iv_add;
    private List<Announcement> mDatas = new ArrayList<>();
    private AnnouncementAdapter adapter;
    private boolean isRefresh, isLoadMore;
    private String relationId, relationType, type;
    private int page = 1;
    private int index;
    private final int CREATE_CODE = 1, READ_CODE = 2, ALERT_CODE = 3;
    private RecyclerTouchListener onTouchListener;
    private OnActivityTouchListener touchListener;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_announcement;
    }

    @Override
    public void initView() {
        relationId = getIntent().getStringExtra("relationId");
        relationType = getIntent().getStringExtra("relationType");
        type = getIntent().getStringExtra("type");
        tv_empty.setText("暂时没有通知~");
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new AnnouncementAdapter(mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(context);
        onTouchListener = new RecyclerTouchListener(context, xRecyclerView);
        xRecyclerView.addOnItemTouchListener(onTouchListener);
    }

    /*获取通知公告列表*/
    public void initData() {
        String url = Constants.OUTRT_NET + "/m/announcement?announcementRelations[0].relation.id="
                + relationId + "&page=" + page + "&limit=20" + "&orders=CREATE_TIME.DESC";
        if (relationType != null) {
            url += "&announcementRelations[0].relation.type" + relationType;
        }
        if (type != null) {
            url += "&type=" + type;
        }
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<Announcements>() {
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
                    page -= 1;
                    xRecyclerView.loadMoreComplete(false);
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onResponse(Announcements response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().getAnnouncements() != null && response.getResponseData().getAnnouncements().size() > 0) {
                    UpdateUI(response.getResponseData().getAnnouncements(), response.getResponseData().getPaginator());
                } else {
                    if (isRefresh) {
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                        xRecyclerView.setLoadingMoreEnabled(false);
                    } else {
                        tv_empty.setVisibility(View.VISIBLE);
                        xRecyclerView.setVisibility(View.GONE);
                        xRecyclerView.setLoadingMoreEnabled(false);
                    }
                }
            }
        }));
    }

    private void UpdateUI(List<Announcement> announcements, Paginator paginator) {
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
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AnnouncementEditActivity.class);
                intent.putExtra("relationId", relationId);
                intent.putExtra("relationType", relationType);
                intent.putExtra("type", type);
                startActivityForResult(intent, CREATE_CODE);
            }
        });
        onTouchListener.setClickable(new RecyclerTouchListener.OnRowClickListener() {
            @Override
            public void onRowClicked(int position) {
                if (position - 1 >= 0 && position - 1 < mDatas.size()) {
                    index = position - 1;
                    Intent intent = new Intent(context, AnnouncementDetailActivity.class);
                    intent.putExtra("relationId", mDatas.get(index).getId());
                    startActivityForResult(intent, READ_CODE);
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
                        String entityId = mDatas.get(selected).getId();
                        Intent intent = new Intent(context, AnnouncementEditActivity.class);
                        intent.putExtra("entityId", entityId);
                        intent.putExtra("isAlter", true);
                        startActivityForResult(intent, ALERT_CODE);
                    }
                } else if (viewID == R.id.bt_delete) {
                    if (selected >= 0 && selected < mDatas.size()) {
                        delete(selected);
                    }
                }
            }
        });
    }

    private void delete(final int position) {
        String id = mDatas.get(position).getId();
        String url = Constants.OUTRT_NET + "/m/announcement/" + id;
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
                        xRecyclerView.setVisibility(View.GONE);
                        tv_empty.setVisibility(View.VISIBLE);
                    }
                }
            }
        }, new OkHttpClientManager.Param("_method", "delete")));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CREATE_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    Announcement entity = (Announcement) data.getSerializableExtra("entity");
                    if (!xRecyclerView.isLoadingMoreEnabled()) {
                        mDatas.add(entity);
                        adapter.notifyDataSetChanged();
                    }
                    if (mDatas.size() > 0) {
                        if (xRecyclerView.getVisibility() != View.VISIBLE) {
                            xRecyclerView.setVisibility(View.VISIBLE);
                        }
                        if (tv_empty.getVisibility() != View.GONE) {
                            tv_empty.setVisibility(View.GONE);
                        }
                    }
                }
                break;
            case READ_CODE:
                if (resultCode == RESULT_OK) {
                    mDatas.get(index).setHadView(true);
                    adapter.notifyDataSetChanged();
                }
                break;
            case ALERT_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    String title = data.getStringExtra("title");
                    String content = data.getStringExtra("content");
                    mDatas.get(index).setTitle(title);
                    mDatas.get(index).setContent(content);
                    adapter.notifyDataSetChanged();
                }
                break;
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
