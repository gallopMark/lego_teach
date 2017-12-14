package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.WSMobileUserAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.WSMobileUserData;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/12/13.
 * 描述:工作坊学员考核
 * 作者:xiaoma
 */

public class WSExamineActivity extends BaseActivity implements XRecyclerView.LoadingListener {
    private WSExamineActivity context;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.rl_edit)
    RelativeLayout rl_edit;
    @BindView(R.id.tv_select)
    TextView tv_select;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.container)
    RelativeLayout container;
    @BindView(R.id.tv_tips)
    TextView tv_tips;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    @BindView(R.id.bottom)
    LinearLayout bottom;
    @BindView(R.id.tv_selected)
    TextView tv_selected;
    @BindView(R.id.tv_send)
    TextView tv_send;
    @BindView(R.id.tv_evaluate)
    TextView tv_evaluate;
    private String workshopId;
    private int qualifiedPoint;
    private int total, page = 1, limit = 30;
    private boolean isRefresh, isLoadMore, firstLoad = true;
    private String text_edit = "编辑", text_selectAll = "全选", text_cancelAll = "全不选", text_selected = "已选", text_cancel = "取消";
    private int selectType = 1;
    private List<WorkShopMobileUser> mDatas = new ArrayList<>();
    private WSMobileUserAdapter adapter;
    private final int REQUEST_MSG = 1, REQUEST_EVA = 2;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_wsexamine;
    }

    @Override
    public void initView() {
        context = this;
        setToolBar();
        tv_select.setText(text_selectAll);
        tv_cancel.setText(text_cancel);
        tv_selected.setText(text_selected);
        workshopId = getIntent().getStringExtra("workshopId");
        qualifiedPoint = getIntent().getIntExtra("qualifiedPoint", 0);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new WSMobileUserAdapter(context, mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(context);
    }

    private void setToolBar() {
        toolBar.setTitle_text("学员考核");
        toolBar.setRight_button_text(text_edit);
        toolBar.setShow_right_button(false);
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
        adapter.setEdit(true);
        rl_edit.setVisibility(View.VISIBLE);
        bottom.setVisibility(View.VISIBLE);
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/m/workshop_user/" + workshopId + "/students?page=" + page + "&limit=" + limit;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<WSMobileUserData>>() {
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
            public void onResponse(BaseResponseResult<WSMobileUserData> response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    if (firstLoad) {
                        setTopTips(response.getResponseData().getPaginator());
                        if (response.getResponseData().getWorkshopUsers().size() > 0) {
                            toolBar.setShow_right_button(true);
                        }
                        firstLoad = false;
                    }
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
        container.setVisibility(View.VISIBLE);
        if (paginator != null) {
            total = paginator.getTotalCount();
        }
        setTips();
    }

    private void setTips() {
        String text = "共" + total + "名学员，研修积分未达" + qualifiedPoint + "分默认为未达标学员。";
        SpannableString ss = new SpannableString(text);
        int start = text.indexOf("共") + 1;
        int end = text.indexOf("名");
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = text.indexOf("达") + 1;
        end = text.lastIndexOf("分");
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_tips.setText(ss);
    }

    private void updateUI(List<WorkShopMobileUser> list, Paginator paginator) {
        if (isRefresh) {
            mDatas.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        mDatas.addAll(list);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    @Override
    public void setListener() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_select:
                        if (selectType == 1) {
                            adapter.selecetAll();
                            tv_select.setText(text_cancelAll);
                            selectType = 2;
                        } else {
                            adapter.cancelAll();
                            tv_select.setText(text_selectAll);
                            selectType = 1;
                        }
                        break;
                    case R.id.tv_cancel:
                        cancelEdit();
                        break;
                    case R.id.tv_send:  //发送消息
                        if (adapter.getmSelects().size() > 0) {
                            List<WorkShopMobileUser> mSelects = adapter.getmSelects();
                            Intent intent = new Intent(context, WSExamineMsgActivity.class);
                            intent.putExtra("mSelects", (Serializable) mSelects);
                            startActivityForResult(intent, REQUEST_MSG);
                        } else {
                            toast(context, "请选择学员");
                        }
                        break;
                    case R.id.tv_evaluate:  //批量评价
                        if (adapter.getmSelects().size() > 0) {
                            List<WorkShopMobileUser> mSelects = adapter.getmSelects();
                            Intent intent = new Intent(context, WSExamineEvaActivity.class);
                            intent.putExtra("workshopId", workshopId);
                            intent.putExtra("mSelects", (Serializable) mSelects);
                            startActivityForResult(intent, REQUEST_EVA);
                        } else {
                            toast(context, "请选择学员");
                        }
                        break;
                }
            }
        };
        tv_select.setOnClickListener(onClickListener);
        tv_cancel.setOnClickListener(onClickListener);
        tv_send.setOnClickListener(onClickListener);
        tv_evaluate.setOnClickListener(onClickListener);
        adapter.setOnItemLongClickListener(new BaseRecyclerAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                setEdit();
            }
        });
        adapter.setOnItemSelectListener(new WSMobileUserAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelect(List<WorkShopMobileUser> mSelects) {
                if (mSelects.size() == mDatas.size()) {
                    tv_select.setText(text_cancelAll);
                    selectType = 2;
                } else {
                    tv_select.setText(text_selectAll);
                    selectType = 1;
                }
                if (mSelects.size() > 0) {
                    tv_selected.setText(text_selected + "(" + mSelects.size() + ")");
                } else {
                    tv_selected.setText(text_selected);
                }
            }
        });
    }

    private void cancelEdit() {
        toolBar.setShow_left_button(true);
        toolBar.setShow_right_button(true);
        adapter.cancelAll();
        adapter.setEdit(false);
        rl_edit.setVisibility(View.GONE);
        bottom.setVisibility(View.GONE);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_MSG:
                if (resultCode == RESULT_OK) {
                    cancelEdit();
                }
                break;
            case REQUEST_EVA:
                if (resultCode == RESULT_OK) {
                    cancelEdit();
                    onRefresh();
                }
                break;
        }
    }
}
