package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.haoyu.app.adapter.WSMemberAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.WSMobileUsers;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/12/15.
 * 描述:工作坊成员搜索
 * 作者:xiaoma
 */

public class WSSearchMebActivity extends BaseActivity implements View.OnClickListener, XRecyclerView.LoadingListener {
    private WSSearchMebActivity context;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.tv_clear)
    TextView tv_clear;
    @BindView(R.id.tv_search)
    TextView tv_search;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    private String workshopId;
    private int page = 1, limit = 20;
    private List<WorkShopMobileUser> mDatas = new ArrayList<>();
    private WSMemberAdapter adapter;
    private String realName;
    private boolean isRefresh, isLoadMore, isSearch;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_wssearchmeb;
    }

    @Override
    public void initView() {
        context = this;
        workshopId = getIntent().getStringExtra("workshopId");
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new WSMemberAdapter(context, mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(context);
    }

    @Override
    public void setListener() {
        tv_cancel.setOnClickListener(context);
        tv_search.setOnClickListener(context);
        tv_clear.setOnClickListener(context);
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence)) {
                    tv_clear.setVisibility(View.GONE);
                } else {
                    tv_clear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        adapter.setOnItemDeleteListener(new WSMemberAdapter.OnItemDeleteListener() {
            @Override
            public void onItemDelete(WorkShopMobileUser entity, int position) {
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
                    adapter.notifyDataSetChanged();
                    if (mDatas.size() == 0) {
                        xRecyclerView.setVisibility(View.GONE);
                        tv_empty.setVisibility(View.VISIBLE);
                    }
                    Intent intent = new Intent("delete");
                    intent.putExtra("entity", entity);
                    sendBroadcast(intent);
                }
            }
        }, new OkHttpClientManager.Param("_method", "delete")));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_search:
                String userName = et_name.getText().toString().trim();
                if (TextUtils.isEmpty(userName)) {
                    showMaterialDialog("提示", "请输入姓名");
                } else {
                    realName = userName;
                    isSearch = true;
                    onRefresh();
                }
                break;
            case R.id.tv_clear:
                et_name.setText(null);
                break;
        }
    }

    private void search() {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/m/workshop_user/" + workshopId + "/members?page=" + page + "&limit=" + limit + "&realName=" + realName;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<WSMobileUsers>() {
            @Override
            public void onBefore(Request request) {
                if (tv_empty.getVisibility() != View.GONE) {
                    tv_empty.setVisibility(View.GONE);
                }
                if (!isRefresh && !isLoadMore) {
                    showTipDialog();
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                if (isRefresh) {
                    xRecyclerView.refreshComplete(false);
                } else if (isLoadMore) {
                    page -= 1;
                    xRecyclerView.loadMoreComplete(false);
                } else {
                    onNetWorkError(context);
                }
            }

            @Override
            public void onResponse(WSMobileUsers response) {
                hideTipDialog();
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

    private void updateUI(List<WorkShopMobileUser> users, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE) {
            xRecyclerView.setVisibility(View.VISIBLE);
        }
        if (tv_empty.getVisibility() != View.GONE) {
            tv_empty.setVisibility(View.GONE);
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
    public void onRefresh() {
        isRefresh = true;
        isLoadMore = false;
        page = 1;
        search();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        page += 1;
        search();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (isShouldHideInput(et_name, ev)) {
                Common.hideSoftInput(context, et_name);
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(EditText et, MotionEvent event) {
        int[] leftTop = {0, 0};
        //获取输入框当前的location位置
        et.getLocationInWindow(leftTop);
        int left = leftTop[0];
        int top = leftTop[1];
        int bottom = top + et.getHeight();
        int right = left + et.getWidth();
        if (event.getX() > left && event.getX() < right
                && event.getY() > top && event.getY() < bottom) {
            // 点击的是输入框区域，保留点击EditText的事件
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);//用于屏蔽 activity 默认的转场动画效果
    }
}
