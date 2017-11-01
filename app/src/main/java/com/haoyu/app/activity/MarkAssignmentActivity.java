package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.haoyu.app.adapter.EvaluateItemAdapter;
import com.haoyu.app.adapter.MFileInfoAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.EvaluateItemSubmissions;
import com.haoyu.app.entity.MAssignmentUser;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MarkAssignmentResult;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/2/6 on 17:38
 * 描述:批改作业
 * 作者:马飞奔 Administrator
 */
public class MarkAssignmentActivity extends BaseActivity implements View.OnClickListener {
    private MarkAssignmentActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    @BindView(R.id.contentView)
    ScrollView contentView;
    @BindView(R.id.bottomView)
    LinearLayout bottomView;
    @BindView(R.id.tv_name)
    TextView tv_name;  //作业名称
    @BindView(R.id.rv_file)
    RecyclerView rv_file;
    @BindView(R.id.contentRV)
    RecyclerView contentRV;  //评价内容列表
    @BindView(R.id.tv_score)
    TextView tv_score;   //作业打分布局（默认不可见）
    @BindView(R.id.bt_return)
    Button bt_return;
    @BindView(R.id.bt_submit)
    Button bt_submit;   //发回重做，提交按钮
    private String courseId, userName, relationId, state, mEvaluateSubmissionId, evaluateRelationId;
    private int fullScore = 100;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_mark_assignment;
    }

    @Override
    public void initView() {
        courseId = getIntent().getStringExtra("courseId");
        userName = getIntent().getStringExtra("userName");
        relationId = getIntent().getStringExtra("relationId");
        state = getIntent().getStringExtra("state");
        toolBar.setTitle_text(userName);
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/" + courseId + "/teach/m/assignment/mark/" + relationId + "/markByTeacher";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<MarkAssignmentResult>() {
            @Override
            public void onBefore(Request request) {
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(MarkAssignmentResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                } else {
                    tv_empty.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateUI(MarkAssignmentResult.MarkAssignment markAssignment) {
        if (markAssignment.getmAssignmentUser() != null) {
            contentView.setVisibility(View.VISIBLE);
            bottomView.setVisibility(View.VISIBLE);
            MAssignmentUser mAssignmentUser = markAssignment.getmAssignmentUser();
            if (mAssignmentUser.getmFileInfos() != null && mAssignmentUser.getmFileInfos().size() > 0) {
                final List<MFileInfo> mDatas = mAssignmentUser.getmFileInfos();
                FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
                layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
                rv_file.setLayoutManager(layoutManager);
                MFileInfoAdapter adapter = new MFileInfoAdapter(mDatas);
                rv_file.setAdapter(adapter);
                adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                        Intent intent = new Intent(context, MFileInfoActivity.class);
                        intent.putExtra("fileInfo", mDatas.get(position));
                        startActivity(intent);
                    }
                });
            }
            if (mAssignmentUser.getState() != null && mAssignmentUser.getState().equals("return")) {
                bt_return.setText("已退回重做");
                bt_return.setEnabled(false);
                bt_submit.setVisibility(View.GONE);
            } else if (mAssignmentUser.getState() != null && mAssignmentUser.getState().equals("complete")) {
                bt_submit.setText("重新批阅");
                bt_submit.setEnabled(false);
                tv_score.setText(String.valueOf(getScore(mAssignmentUser.getResponseScore())));
            }
            if (mAssignmentUser.getmAssignment() != null) {
                tv_name.setText(mAssignmentUser.getmAssignment().getTitle());
                fullScore = (int) (100 - mAssignmentUser.getmAssignment().getMarkScorePct());
            } else {
                fullScore = 100;
            }
        } else {
            tv_empty.setVisibility(View.VISIBLE);
            return;
        }
        if (markAssignment.getmEvaluateSubmission() != null) {
            mEvaluateSubmissionId = markAssignment.getmEvaluateSubmission().getId();
            evaluateRelationId = markAssignment.getmEvaluateSubmission().getEvaluateRelationId();
            if (markAssignment.getmEvaluateSubmission().getmEvaluateItemSubmissions() != null)
                updateUI(markAssignment.getmEvaluateSubmission().getmEvaluateItemSubmissions());
        }
    }

    private int getScore(double score) {
        BigDecimal b = new BigDecimal(score);
        int count = (int) b.setScale(0, BigDecimal.ROUND_HALF_UP).floatValue();
        return count;
    }

    private void updateUI(List<EvaluateItemSubmissions> mDatas) {
        for (EvaluateItemSubmissions item : mDatas) {
            item.setEvaluateMark(fullScore / mDatas.size());
        }
        EvaluateItemAdapter evaluateAdapter = new EvaluateItemAdapter(mDatas, state);
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
        layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        contentRV.setLayoutManager(layoutManager);
        contentRV.setAdapter(evaluateAdapter);
        evaluateAdapter.setScoreChangeListener(new EvaluateItemAdapter.ScoreChangeListener() {
            @Override
            public void scoreChange(ArrayMap<Integer, EvaluateItemSubmissions> evaluateMap) {
                itemSubmissionsMap = evaluateMap;
                bt_submit.setEnabled(true);
                int score = 0;
                for (Integer index : evaluateMap.keySet()) {
                    score += evaluateMap.get(index).getScore();
                }
                tv_score.setVisibility(View.VISIBLE);
                setScoreText(score);
            }
        });
    }

    private void setScoreText(int score) {
        String text = "您为作业打分：" + score + "分/满分" + fullScore + "分";
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), text.indexOf("：") + 1, text.indexOf("/") - 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), text.lastIndexOf("满分") + 2, text.lastIndexOf("分"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_score.setText(null);
        tv_score.append(ss);
    }

    private ArrayMap<Integer, EvaluateItemSubmissions> itemSubmissionsMap;

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
        bt_return.setOnClickListener(context);
        bt_submit.setOnClickListener(context);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_return:   //发回重做
                MaterialDialog dialog = new MaterialDialog(context);
                dialog.setTitle("提示");
                dialog.setMessage("作业退回后无法重新批阅，只能等待学员再次提交，确定要退回该作业吗？");
                dialog.setNegativeButton("取消", null);
                dialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
                    @Override
                    public void onClick(View v, AlertDialog dialog) {
                        dialog.dismiss();
                        backtoRedo();
                    }
                });
                dialog.show();
                break;
            case R.id.bt_submit:  //提交批阅
                MaterialDialog mainDialog = new MaterialDialog(context);
                mainDialog.setTitle("提示");
                mainDialog.setMessage("确定要提交对作业的打分吗？");
                mainDialog.setNegativeButton("取消", null);
                mainDialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
                    @Override
                    public void onClick(View v, AlertDialog dialog) {
                        commit();
                    }
                });
                mainDialog.show();
                break;
        }
    }

    /*发回重做*/
    private void backtoRedo() {
        String userId = getUserId();
        String url = Constants.OUTRT_NET + "/" + courseId + "/teach/unique_uid_" + userId
                + "/m/assignment/user/" + relationId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("state", "return");
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
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
                    MessageEvent event = new MessageEvent();
                    event.action = Action.RETURN_ASSIGNMENT_REDO;
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    toastFullScreen("退回失败", false);
                }
            }
        }, map);
    }

    /*提交批阅*/
    private void commit() {
        String userId = getUserId();
        String url = Constants.OUTRT_NET + "/" + courseId + "/teach/unique_uid_"
                + userId + "/m/evaluate/submission/" + mEvaluateSubmissionId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("evaluateRelation.id", evaluateRelationId);
        map.put("evaluateRelation.relation.id", relationId);
        if (itemSubmissionsMap != null) {
            for (Integer index : itemSubmissionsMap.keySet()) {
                EvaluateItemSubmissions item = itemSubmissionsMap.get(index);
                if (item != null) {
                    map.put("evaluateItemSubmissionMap[" + item.getId() + "].score", String.valueOf(item.getStarCount()));
                }
            }
        }
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
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
                    int score = 0;
                    if (itemSubmissionsMap != null) {
                        for (Integer index : itemSubmissionsMap.keySet()) {
                            score += itemSubmissionsMap.get(index).getScore();
                        }
                    }
                    MessageEvent event = new MessageEvent();
                    event.action = Action.READ_OVER_ASSIGNMENT;
                    event.arg1 = score;
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    toast(context, "提交失败");
                }
            }
        }, map);
    }
}
