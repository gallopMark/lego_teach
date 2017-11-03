package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.bottomView)
    LinearLayout bottomView;
    @BindView(R.id.tv_name)
    TextView tv_name;  //作业名称
    @BindView(R.id.rv_file)
    RecyclerView rv_file;
    @BindView(R.id.contentRV)
    RecyclerView contentRV;  //评价内容列表
    @BindView(R.id.ll_remark)
    LinearLayout ll_remark;
    @BindView(R.id.et_remark)
    EditText et_remark;
    @BindView(R.id.ll_check)
    LinearLayout ll_check;
    @BindView(R.id.checkBox)
    CheckBox checkBox;
    @BindView(R.id.tv_score)
    TextView tv_score;   //作业打分布局（默认不可见）
    @BindView(R.id.bt_return)
    Button bt_return;
    @BindView(R.id.bt_submit)
    Button bt_submit;   //发回重做，提交按钮
    private String courseId, userName, relationId, mEvaluateSubmissionId, evaluateRelationId;
    private int fullScore = 100;
    private List<EvaluateItemSubmissions> mDatas = new ArrayList<>();
    private ArrayMap<Integer, Integer> evaluateMap = new ArrayMap<>();

    @Override
    public int setLayoutResID() {
        return R.layout.activity_mark_assignment;
    }

    @Override
    public void initView() {
        courseId = getIntent().getStringExtra("courseId");
        userName = getIntent().getStringExtra("userName");
        relationId = getIntent().getStringExtra("relationId");
        toolBar.setTitle_text(userName);
        et_remark.setHint("请输入评语");
        TextView tv_checkTips = findViewById(R.id.tv_checkTips);
        tv_checkTips.setText("评选为优秀作业");
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
        String state;
        if (markAssignment.getmAssignmentUser() != null) {
            scrollView.setVisibility(View.VISIBLE);
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
            et_remark.setText(mAssignmentUser.getComment());
            checkBox.setChecked(mAssignmentUser.isExcellent());
            state = mAssignmentUser.getState();
            if (state != null && state.equals("return")) {
                ll_remark.setVisibility(View.GONE);
                bt_return.setText("已退回重做");
                bt_return.setEnabled(false);
                bt_submit.setVisibility(View.GONE);
            } else if (state != null && state.equals("complete")) {
                ll_remark.setVisibility(View.VISIBLE);
                bt_submit.setText("重新批阅");
                bt_submit.setEnabled(true);
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
                updateUI(markAssignment.getmEvaluateSubmission().getmEvaluateItemSubmissions(), state);
        }
    }

    private int getScore(double score) {
        BigDecimal b = new BigDecimal(score);
        int count = (int) b.setScale(0, BigDecimal.ROUND_HALF_UP).floatValue();
        return count;
    }

    private void updateUI(List<EvaluateItemSubmissions> list, String state) {
        mDatas = list;
        boolean enable = true;
        if (state != null && state.equals("return")) {
            enable = false;
        }
        final double evaluate = fullScore / mDatas.size();
        EvaluateItemAdapter evaluateAdapter = new EvaluateItemAdapter(mDatas, enable, evaluate);
        FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
        layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        contentRV.setLayoutManager(layoutManager);
        contentRV.setAdapter(evaluateAdapter);
        evaluateAdapter.setScoreChangeListener(new EvaluateItemAdapter.ScoreChangeListener() {
            @Override
            public void scoreChange(ArrayMap<Integer, Integer> arrayMap) {
                evaluateMap = arrayMap;
                bt_submit.setEnabled(true);
                int score = 0;
                for (Integer value : evaluateMap.values()) {
                    score += getScore(value, evaluate);
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

    private int getScore(int count, double evaluate) {
        BigDecimal b = new BigDecimal(count * evaluate / 5);
        int value = (int) b.setScale(0, BigDecimal.ROUND_HALF_UP).floatValue();
        return value;
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
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                //获取root在窗体的可视区域
                scrollView.getWindowVisibleDisplayFrame(rect);
                //获取root在窗体的不可视区域高度(被其他View遮挡的区域高度)
                int rootInvisibleHeight = scrollView.getRootView().getHeight() - rect.bottom;
                //若不可视区域高度大于100，则键盘显示
                if (rootInvisibleHeight > 100) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN); //滚动到底部
                        }
                    }, 10);
                }
            }
        });
        et_remark.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                //触摸的是EditText而且当前EditText能够滚动则将事件交给EditText处理。否则将事件交由其父类处理
                if (canVerticalScroll(et_remark)) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                return false;
            }
        });
        ll_check.setOnClickListener(context);
        bt_return.setOnClickListener(context);
        bt_submit.setOnClickListener(context);
    }

    /**
     * EditText竖直方向能否够滚动
     *
     * @param editText 须要推断的EditText
     * @return true：能够滚动   false：不能够滚动
     */
    private boolean canVerticalScroll(EditText editText) {
        //滚动的距离
        int scrollY = editText.getScrollY();
        //控件内容的总高度
        int scrollRange = editText.getLayout().getHeight();
        //控件实际显示的高度
        int scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() - editText.getCompoundPaddingBottom();
        //控件内容总高度与实际显示高度的差值
        int scrollDifference = scrollRange - scrollExtent;
        if (scrollDifference == 0) {
            return false;
        }
        return (scrollY > 0) || (scrollY < scrollDifference - 1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_check:
                if (checkBox.isChecked())
                    checkBox.setChecked(false);
                else
                    checkBox.setChecked(true);
                break;
            case R.id.bt_return:   //发回重做
                Common.hideSoftInput(context, et_remark);
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
                Common.hideSoftInput(context, et_remark);
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
                    Intent intent = new Intent();
                    intent.putExtra("type", 1);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    toastFullScreen("退回失败", false);
                }
            }
        }, map));
    }

    /*提交批阅*/
    private void commit() {
        String userId = getUserId();
        String url = Constants.OUTRT_NET + "/" + courseId + "/teach/unique_uid_"
                + userId + "/m/evaluate/submission/ncts/" + mEvaluateSubmissionId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("evaluateRelation.id", evaluateRelationId);
        map.put("evaluateRelation.relation.id", relationId);
        if (evaluateMap.size() > 0) {
            for (Integer key : evaluateMap.keySet()) {
                String itemId = mDatas.get(key).getId();
                int startCount = evaluateMap.get(key);
                map.put("evaluateItemSubmissionMap[" + itemId + "].score", String.valueOf(startCount));
            }
        }
        boolean isChecked = checkBox.isChecked();
        if (isChecked) {
            map.put("isExcellent", "true");
        } else {
            map.put("isExcellent", "false");
        }
        String comment = et_remark.getText().toString();
        map.put("comment", comment);
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
                    double evaluate = fullScore / mDatas.size();
                    bt_submit.setEnabled(true);
                    int score = 0;
                    for (Integer value : evaluateMap.values()) {
                        score += getScore(value, evaluate);
                    }
                    Intent intent = new Intent();
                    intent.putExtra("type", 2);
                    intent.putExtra("score", score);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    toast(context, "提交失败");
                }
            }
        }, map));
    }
}
