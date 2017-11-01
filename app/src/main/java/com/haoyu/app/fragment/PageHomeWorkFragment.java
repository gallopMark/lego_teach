package com.haoyu.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.haoyu.app.activity.MarkAssignmentActivity;
import com.haoyu.app.adapter.AssignmentListAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.AppBaseAdapter;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.basehelper.ViewHolder;
import com.haoyu.app.entity.AssignmentListResult;
import com.haoyu.app.entity.AssignmentUserListResult;
import com.haoyu.app.entity.AssignmentUserNumResult;
import com.haoyu.app.entity.MAssignmentEntity;
import com.haoyu.app.entity.MAssignmentUser;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.entity.State;
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
 * 创建日期：2017/2/4 on 17:14
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class PageHomeWorkFragment extends BaseFragment implements View.OnClickListener, XRecyclerView.LoadingListener {
    private String courseId;
    @BindView(R.id.loadView)
    LoadingView loadView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.contentView)
    LinearLayout contentView;
    @BindView(R.id.ll_shake)
    LinearLayout ll_shake;
    @BindView(R.id.ic_state)
    ImageView ic_state;  //领取状态图片标识
    @BindView(R.id.tv_tips)
    TextView tv_tips;
    private List<MAssignmentEntity> mAssignments = new ArrayList<>();
    @BindView(R.id.tv_assignment)
    TextView tv_assignment;
    @BindView(R.id.tv_assignmentState)
    TextView tv_assignmentState;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    private List<MAssignmentUser> mDatas = new ArrayList<>();
    private AssignmentListAdapter adapter;
    private boolean isFirst = true, isRefresh, isLoadMore, needDialog = true;
    private int page = 1;
    private int limit = 20;
    private boolean isLoad = false;
    private int selectId, selectState;
    private String assignmentId, state;
    private int selected = -1;

    @Override
    public int createView() {
        return R.layout.fragment_page_homework;
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            courseId = bundle.getString("entityId");
        }
        adapter = new AssignmentListAdapter(context, mDatas);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(this);
        mAssignments.add(0, null);
    }

    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/" + courseId + "/teach/m/assignment/user/getAssignmentUserNum?relationId=" + courseId;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<AssignmentUserNumResult>() {
            @Override
            public void onBefore(Request request) {
                if (isFirst) {
                    loadView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                loadView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(AssignmentUserNumResult response) {
                isFirst = false;
                loadView.setVisibility(View.GONE);
                contentView.setVisibility(View.VISIBLE);
                updateUI(response);
                getAssignmentList();
            }
        }));
    }

    private void updateUI(AssignmentUserNumResult result) {
        if (result != null && result.getResponseData() != null) {
            int allNum = result.getResponseData().getAllNum();  //作业总数
            int markNum = result.getResponseData().getMarkNum(); //批阅数
            int notReceiveNum = result.getResponseData().getNotReceivedNum();
            SpannableString ssb;
            if (notReceiveNum > 0) {
                ic_state.setImageResource(R.drawable.assignment_queren_tip);
                String text = "您已批阅 " + markNum + "/" + allNum + " 份作业，还有 " + notReceiveNum
                        + " 份待领取，点击领取作业";
                ssb = new SpannableString(text);
                ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), text.indexOf("阅") + 1, text.indexOf("份"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), text.indexOf("有") + 1, text.lastIndexOf("份"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_tips.setText(null);
                tv_tips.append(ssb);
                ll_shake.setEnabled(true);
            } else {
                ic_state.setImageResource(R.drawable.assignment_get_tips);
                String text = "您已批阅 " + markNum + "/" + allNum + " 份作业，暂无待领取的作业。";
                ssb = new SpannableString(text);
                ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), text.indexOf("阅") + 1, text.indexOf("份"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_tips.setText(null);
                tv_tips.append(ssb);
                ll_shake.setEnabled(false);
            }
        }
    }

    /*获取作业列表*/
    private void getAssignmentList() {
        String url = Constants.OUTRT_NET + "/" + courseId + "/teach/m/assignment/user?relationId=" + courseId
                + "&page=" + page + "&limit=" + limit;
        if (assignmentId != null) {
            url += "&assignmentId=" + assignmentId;
        }
        if (state != null) {
            url += "&state=" + state;
        }
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<AssignmentUserListResult>() {

            @Override
            public void onBefore(Request request) {
                if (tv_empty.getVisibility() != View.GONE) {
                    tv_empty.setVisibility(View.GONE);
                }
                if (needDialog) {
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
                    onNetWorkError();
                }
            }

            @Override
            public void onResponse(AssignmentUserListResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null && response.getResponseData().getmAssignmentUsers() != null
                        && response.getResponseData().getmAssignmentUsers().size() > 0) {
                    updateUI(response.getResponseData().getmAssignmentUsers(), response.getResponseData().getPaginator());
                } else {
                    xRecyclerView.setVisibility(View.GONE);
                    tv_empty.setVisibility(View.VISIBLE);
                    tv_empty.setText("没有作业~");
                }
            }
        }));
    }

    private void updateUI(List<MAssignmentUser> mAssignmentUsers, Paginator paginator) {
        if (tv_empty.getVisibility() != View.GONE)
            tv_empty.setVisibility(View.VISIBLE);
        if (xRecyclerView.getVisibility() != View.VISIBLE)
            xRecyclerView.setVisibility(View.VISIBLE);
        if (isRefresh) {
            mDatas.clear();
            xRecyclerView.refreshComplete(true);
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        mDatas.addAll(mAssignmentUsers);
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
        tv_assignment.setOnClickListener(this);
        tv_assignmentState.setOnClickListener(this);
        ll_shake.setOnClickListener(this);
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                selected = position - 1;
                if (selected >= 0 && selected < mDatas.size()) {
                    Intent intent = new Intent(context, MarkAssignmentActivity.class);
                    intent.putExtra("courseId", courseId);
                    MAssignmentUser mAssignmentUser = mDatas.get(selected);
                    if (mAssignmentUser.getmUser() != null) {
                        intent.putExtra("userName", mAssignmentUser.getmUser().getRealName());
                    }
                    intent.putExtra("state", mAssignmentUser.getState());
                    intent.putExtra("relationId", mAssignmentUser.getId());
                    startActivityForResult(intent, 1);
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_shake:
                getHomeWork();
                break;
            case R.id.tv_assignment:
                if (!isLoad) {
                    getAssignment();
                } else {
                    setAssignmentPopupView(mAssignments);
                }
                break;
            case R.id.tv_assignmentState:
                List<State> mStates = getStates();
                setStatePopupView(mStates);
                break;
        }
    }

    /**
     * 领取作业
     */
    private void getHomeWork() {
        String userId = getUserId();
        String url = Constants.OUTRT_NET + "/" + courseId + "/teach/unique_uid_" + userId + "/m/assignment/mark/" + courseId;
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    try {
                        int num = Integer.parseInt((String) response.getResponseData());
                        toastFullScreen("成功领取了" + num + "分作业", true);
                    } catch (Exception e) {
                    }
                    isRefresh = true;
                    page = 1;
                    initData();
                } else {
                    if (response != null && response.getResponseMsg() != null) {
                        toast("暂时没有未批阅的作业");
                    }
                }
            }
        }));
    }

    /*作业活动列表*/
    private void getAssignment() {
        String url = Constants.OUTRT_NET + "/" + courseId + "/teach/m/assignment?relationId=" + courseId
                + "&markType=teach";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<AssignmentListResult>() {
            @Override
            public void onError(Request request, Exception e) {
                isLoad = false;
            }

            @Override
            public void onResponse(AssignmentListResult response) {
                isLoad = true;
                if (response != null && response.getResponseData() != null) {
                    mAssignments.addAll(response.getResponseData());
                    setAssignmentPopupView(mAssignments);
                }
            }
        }));
    }

    private List<State> getStates() {
        List<State> mDatas = new ArrayList<>();
        mDatas.add(new State("", "全部状态"));
        mDatas.add(new State("commit", "待批阅"));
        mDatas.add(new State("complete", "已批阅"));
        mDatas.add(new State("return", "发回重做"));
        return mDatas;
    }

    private void setAssignmentPopupView(final List<MAssignmentEntity> list) {
        Drawable shouqi = ContextCompat.getDrawable(context,
                R.drawable.assignment_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(),
                shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context,
                R.drawable.assignment_zhankai);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(),
                zhankai.getMinimumHeight());
        tv_assignment.setCompoundDrawables(null, null, shouqi, null);
        View view = View.inflate(context, R.layout.popupwindow_listview, null);
        final PopupWindow AssignmentPopupWindow = new PopupWindow(view, tv_assignment.getWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ListView lv = view.findViewById(R.id.listView);
        final AssignmentAdapter adapter = new AssignmentAdapter(context, list, selectId);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                AssignmentPopupWindow.dismiss();
                selectId = position;
                adapter.setSelectItem(selectId);
                if (position == 0) {
                    assignmentId = null;
                    tv_assignment.setText("全部作业");
                } else {
                    assignmentId = list.get(position).getId();
                    tv_assignment.setText(list.get(position).getTitle());
                }
                isRefresh = true;
                needDialog = true;
                page = 1;
                getAssignmentList();
            }
        });
        AssignmentPopupWindow.setFocusable(true);
        AssignmentPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        AssignmentPopupWindow.setOutsideTouchable(true);
        AssignmentPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv_assignment.setCompoundDrawables(null, null, zhankai, null);
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssignmentPopupWindow.dismiss();
            }
        });
        AssignmentPopupWindow.showAsDropDown(tv_assignment);
    }

    private void setStatePopupView(final List<State> list) {
        Drawable shouqi = ContextCompat.getDrawable(context,
                R.drawable.assignment_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(),
                shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context,
                R.drawable.assignment_zhankai);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(),
                zhankai.getMinimumHeight());
        tv_assignmentState.setCompoundDrawables(null, null, shouqi, null);
        View view = View.inflate(context, R.layout.popupwindow_listview,
                null);
        final PopupWindow statePopupWindow = new PopupWindow(view, tv_assignmentState.getWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ListView lv = view.findViewById(R.id.listView);
        final StateAdapter adapter = new StateAdapter(context, list, selectState);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                statePopupWindow.dismiss();
                selectState = position;
                adapter.setSelectItem(selectState);
                tv_assignmentState.setText(list.get(position).getContent());
                if (position == 0) {
                    state = null;
                } else {
                    state = list.get(position).getState();
                }
                isRefresh = true;
                needDialog = true;
                page = 1;
                getAssignmentList();
            }
        });
        statePopupWindow.setFocusable(true);
        statePopupWindow.setBackgroundDrawable(new BitmapDrawable());
        statePopupWindow.setOutsideTouchable(true);
        statePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv_assignmentState.setCompoundDrawables(null, null, zhankai, null);
            }
        });
        statePopupWindow.showAsDropDown(tv_assignmentState);
    }

    @Override
    public void onRefresh() {
        isRefresh = true;
        isLoadMore = false;
        needDialog = false;
        page = 1;
        getAssignmentList();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        needDialog = false;
        page += 1;
        getAssignmentList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            int type = data.getIntExtra("type", 0);
            if (type == 1) {  //退回重做
                if (selected >= 0 && selected < mDatas.size()) {
                    mDatas.get(selected).setState("return");
                    adapter.notifyDataSetChanged();
                }
            } else if (type == 2) {
                int score = data.getIntExtra("score", 0);
                if (selected >= 0 && selected < mDatas.size()) {
                    mDatas.get(selected).setState("complete");
                    mDatas.get(selected).setResponseScore(score);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    class AssignmentAdapter extends AppBaseAdapter<MAssignmentEntity> {
        private int selectItem;
        private Context context;

        public AssignmentAdapter(Context context, List<MAssignmentEntity> mDatas, int selectItem) {
            super(context, mDatas);
            this.selectItem = selectItem;
            this.context = context;
        }

        @Override
        public void convert(ViewHolder holder, MAssignmentEntity entity, int position) {
            TextView tv_name = holder.getView(R.id.tv_name);
            if (entity == null) {
                tv_name.setText("全部作业");
            } else {
                tv_name.setText(entity.getTitle());
            }
            if (selectItem == position) {
                tv_name.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
            } else {
                tv_name.setTextColor(ContextCompat.getColor(context, R.color.gray));
            }
        }

        public void setSelectItem(int selectItem) {
            this.selectItem = selectItem;
            notifyDataSetChanged();
        }

        @Override
        public int getmItemLayoutId() {
            return R.layout.assignment_name_list_item;
        }
    }

    class StateAdapter extends AppBaseAdapter<State> {
        private int selectItem;
        private Context context;

        public StateAdapter(Context context, List<State> mDatas, int selectItem) {
            super(context, mDatas);
            this.selectItem = selectItem;
            this.context = context;
        }

        @Override
        public void convert(ViewHolder holder, State state, int position) {
            TextView tv_name = holder.getView(R.id.tv_name);
            tv_name.setText(state.getContent());
            if (selectItem == position) {
                tv_name.setTextColor(ContextCompat.getColor(context, R.color.defaultColor));
            } else {
                tv_name.setTextColor(ContextCompat.getColor(context, R.color.gray));
            }
        }

        public void setSelectItem(int selectItem) {
            this.selectItem = selectItem;
            notifyDataSetChanged();
        }

        @Override
        public int getmItemLayoutId() {
            return R.layout.assignment_name_list_item;
        }
    }
}
