package com.haoyu.app.activity;

import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.WSMobileUsers;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.swipe.SwipeMenuLayout;
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
    @BindView(R.id.ll_search)
    LinearLayout ll_search;
    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.iv_search)
    ImageView iv_search;
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
    private boolean isRefresh, isLoadMore, firstLoad = true, isSearch;
    private List<WorkShopMobileUser> mDatas = new ArrayList<>();
    private MemberAdapter adapter;
    private String userName;

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
        adapter = new MemberAdapter(mDatas);
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
                setEdit();
            }
        });
    }

    private void setEdit() {
        toolBar.setShow_left_button(false);
        toolBar.setShow_right_button(false);
        toolBar.getTv_title().setVisibility(View.INVISIBLE);
        ll_search.setVisibility(View.VISIBLE);
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/m/workshop_user/" + workshopId + "/members?page=" + page + "&limit=" + limit;
        if (!TextUtils.isEmpty(userName)) {
            url += "&realName=" + userName;
        }
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
                        if (isSearch) {
                            tv_empty.setVisibility(View.VISIBLE);
                            xRecyclerView.setVisibility(View.GONE);
                            isSearch = false;
                        }
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
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.iv_cancel:
                        cancelEdit();
                        break;
                    case R.id.iv_search:
                        String name = et_name.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) {
                            showMaterialDialog("提示", "请输入姓名");
                        } else {
                            userName = name;
                            onRefresh();
                        }
                        break;
                }
            }
        };
        iv_cancel.setOnClickListener(listener);
        iv_search.setOnClickListener(listener);
    }

    private void cancelEdit() {
        toolBar.setShow_left_button(true);
        toolBar.setShow_right_button(true);
        toolBar.getTv_title().setVisibility(View.VISIBLE);
        ll_search.setVisibility(View.GONE);
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

    private class MemberAdapter extends BaseArrayRecyclerAdapter<WorkShopMobileUser> {

        public MemberAdapter(List<WorkShopMobileUser> mDatas) {
            super(mDatas);
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.wsmanagermeb_item;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, final WorkShopMobileUser entity, final int position) {
            final SwipeMenuLayout swipeLayout = holder.obtainView(R.id.swipeLayout);
            LinearLayout contentView = holder.obtainView(R.id.contentView);
            ImageView iv_ico = holder.obtainView(R.id.iv_ico);
            TextView tv_realName = holder.obtainView(R.id.tv_realName);
            TextView tv_deptName = holder.obtainView(R.id.tv_deptName);
            Button bt_delete = holder.obtainView(R.id.bt_delete);
            if (entity.getmUser() != null && entity.getmUser().getAvatar() != null) {
                String avatar = entity.getmUser().getAvatar();
                GlideImgManager.loadCircleImage(context, avatar, R.drawable.user_default, R.drawable.user_default, iv_ico);
            } else {
                iv_ico.setImageResource(R.drawable.user_default);
            }
            if (entity.getmUser() != null) {
                tv_realName.setText(entity.getmUser().getRealName());
                if (!TextUtils.isEmpty(entity.getmUser().getDeptName())) {
                    tv_deptName.setVisibility(View.VISIBLE);
                    tv_deptName.setText(entity.getmUser().getDeptName());
                } else {
                    tv_deptName.setVisibility(View.GONE);
                }
            } else {
                tv_realName.setText("");
                tv_deptName.setVisibility(View.GONE);
            }
            contentView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    swipeLayout.smoothClose();
                    return false;
                }
            });
            bt_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delete(entity);
                }
            });
        }

        private void delete(final WorkShopMobileUser entity) {
            String id = entity.getId();
            String url = Constants.OUTRT_NET + "/master_" + workshopId + "/m/workshop_user/" + id;
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
                        mDatas.remove(entity);
                        notifyDataSetChanged();
                        if (mDatas.size() == 0) {
                            xRecyclerView.setVisibility(View.GONE);
                            tv_empty.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }, new OkHttpClientManager.Param("_method", "delete")));
        }
    }
}
