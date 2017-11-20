package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.StudentAssignmentAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.ManagementMemberEntity;
import com.haoyu.app.entity.ManagementMemberResult;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
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
 * Created by acer1 on 2017/2/8.
 * 学员考核
 */
public class StudentAssignmentActivity extends BaseActivity implements View.OnClickListener, XRecyclerView.LoadingListener {
    private StudentAssignmentActivity context = this;
    @BindView(R.id.rl_back)
    RelativeLayout rl_back;
    @BindView(R.id.rl_menu)
    RelativeLayout rl_menu;
    @BindView(R.id.rl_menu_all)
    RelativeLayout rl_menu_all;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.p_all_evaluate)
    TextView p_all_evaluate;//总评价

    @BindView(R.id.choose_count)
    TextView choose_count;//已经选择数量
    @BindView(R.id.send_message)
    TextView send_message;//发送消息
    @BindView(R.id.evaluate)
    TextView evaluate;//批量评价
    @BindView(R.id.tv_wran)
    TextView tv_wran;
    @BindView(R.id.bottom)
    LinearLayout bottom;
    @BindView(R.id.warn_msg)
    TextView warn_msg;
    @BindView(R.id.iv_menu_all)
    TextView iv_menu_all;
    private String workShopId;
    private StudentAssignmentAdapter adapter;
    private List<ManagementMemberEntity> memberEntityList = new ArrayList<>();

    private ArrayList<String> workUserIdList = new ArrayList<>();
    private int page = 1;
    private boolean isRefresh, isLoadMore;
    @BindView(R.id.ll_show)
    RelativeLayout ll_show;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;

    @Override
    public void obBusEvent(MessageEvent event) {
        String action = event.getAction();
        if (action.equals(Action.ASSESS_MEMBER)) {
            memberEntityList.clear();
            initData();
            choose_count.setText("已选(" + String.valueOf(workUserIdList.size()) + ")");
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public int setLayoutResID() {
        return R.layout.activity_student_assignment;
    }

    @Override
    public void initView() {
        workShopId = getIntent().getStringExtra("workshopId");
        adapter = new StudentAssignmentAdapter(context, memberEntityList, workUserIdList);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(manager);
        xRecyclerView.setArrowImageView(R.drawable.refresh_arrow);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(this);
        registRxBus();
    }

    @Override
    public void setListener() {
        rl_back.setOnClickListener(context);
        rl_menu.setOnClickListener(context);
        choose_count.setOnClickListener(context);
        send_message.setOnClickListener(context);
        evaluate.setOnClickListener(context);
        rl_menu_all.setOnClickListener(context);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        adapter.setEvaluateBack(new StudentAssignmentAdapter.EvaluateBack() {
            @Override
            public void putEveluateId(String id, boolean isCheck) {
                if (!workUserIdList.contains(id)) {
                    workUserIdList.add(id);
                } else {
                    workUserIdList.remove(id);
                }
                choose_count.setText("已选(" + String.valueOf(workUserIdList.size()) + ")");
            }
        });

    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/master_" + workShopId + "/m/workshop_user/" + workShopId + "/students?page=" + page + "&limit=20";
        OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<ManagementMemberResult>() {

            @Override
            public void onError(Request request, Exception e) {
                loadFailView.setVisibility(View.VISIBLE);
                loadingView.setVisibility(View.GONE);
                ll_show.setVisibility(View.GONE);
                onNetWorkError(context);
            }

            @Override
            public void onResponse(ManagementMemberResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null && response.getResponseData().getWorkshopUsers() != null) {
                    if (isRefresh) {
                        memberEntityList.clear();
                        xRecyclerView.refreshComplete(true);
                    } else if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                    }
                    if (response.getResponseData().getWorkshopUsers().size() == 0) {
                        warn_msg.setVisibility(View.VISIBLE);
                    }
                    memberEntityList.addAll(response.getResponseData().getWorkshopUsers());
                    if (response.getResponseData().getPaginator() != null && response.getResponseData().getPaginator().getHasNextPage()) {
                        xRecyclerView.setLoadingMoreEnabled(true);
                    } else {
                        xRecyclerView.setLoadingMoreEnabled(false);
                    }
                    adapter.notifyDataSetChanged();
                    if (response.getResponseData().getPaginator() != null && response.getResponseData().getPaginator().getTotalCount() != null) {
                        String message = "共" + response.getResponseData().getPaginator().getTotalCount() + "名学员,研修积分未达30分默认为不合格";
                        SpannableStringBuilder style = new SpannableStringBuilder(message);
                        int startIndex1 = message.indexOf("共") + 1;
                        int endIndex1 = message.indexOf("名");
                        style.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.orange)), startIndex1, endIndex1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tv_wran.setText(style);//将其添加到tv中
                    }
                    ll_show.setVisibility(View.VISIBLE);
                    loadFailView.setVisibility(View.GONE);
                } else {
                    loadFailView.setVisibility(View.VISIBLE);
                }

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_back:
                finish();
                break;
            case R.id.rl_menu:
                //选择按钮
                if (bottom.getVisibility() == View.GONE) {
                    bottom.setVisibility(View.VISIBLE);
                }

                index = "3";
                p_all_evaluate.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                rl_menu_all.setVisibility(View.VISIBLE);
                rl_menu.setVisibility(View.INVISIBLE);
                adapter.notifyDataSetChanged();
                break;
            case R.id.rl_menu_all:
                //全选
                if (index == null || index.equals("")) {
                    checkedAll();
                    iv_menu_all.setText("取消");
                } else if (index.equals("1")) {
                    index = "2";
                    iv_menu_all.setText("全选");
                    clearAll();
                } else if (index.equals("2")) {
                    index = "1";
                    checkedAll();
                    iv_menu_all.setText("取消");
                } else if (index.equals("3")) {
                    index = "1";
                    checkedAll();
                    iv_menu_all.setText("取消");
                }


                break;
            case R.id.send_message:
                //发消息
                if (workUserIdList.size() == 0) {
                    toast(context, "请选择成员");
                } else {
                    Intent intent = new Intent(context, StudentSendMessageActivity.class);
                    intent.putExtra("workshopId", workShopId);
                    intent.putStringArrayListExtra("listId", workUserIdList);
                    startActivity(intent);
                }

                break;
            case R.id.evaluate:
                //批量评价
                if (workUserIdList.size() == 0) {
                    toast(context, "请选择成员");
                } else {
                    Intent intent2 = new Intent(context, StudentEvaluateActivity.class);
                    intent2.putStringArrayListExtra("listId", workUserIdList);
                    intent2.putExtra("workshopId", workShopId);
                    startActivity(intent2);
                }
                break;

        }

    }

    //全选
    private void checkedAll() {
        index = "1";
        p_all_evaluate.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
        chooseAllID();
        choose_count.setText("已选(" + String.valueOf(workUserIdList.size()) + ")");
    }

    //取消全选
    private void clearAll() {
        workUserIdList.clear();
        choose_count.setText("已选(" + String.valueOf(workUserIdList.size()) + ")");
        adapter.notifyDataSetChanged();
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

    //全部选中
    private void chooseAllID() {
        workUserIdList.clear();
        for (int i = 0; i < memberEntityList.size(); i++) {
            if (memberEntityList.get(i) != null && memberEntityList.get(i).getId() != null) {
                workUserIdList.add(memberEntityList.get(i).getId());
            }
        }

    }


    public String index = "0";

    public String getCheckState() {
        return index;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribe();
    }
}
