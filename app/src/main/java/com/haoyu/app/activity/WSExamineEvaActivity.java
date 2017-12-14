package com.haoyu.app.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/12/13.
 * 描述:工作坊学员考核批量评价
 * 作者:xiaoma
 */

public class WSExamineEvaActivity extends BaseActivity {
    private WSExamineEvaActivity context;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.bt_evaluate)
    Button bt_evaluate;
    private List<WorkShopMobileUser> mSelects;
    private String workshopId, workshopResult;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_wsexamineeva;
    }

    @Override
    public void initView() {
        context = this;
        workshopId = getIntent().getStringExtra("workshopId");
        mSelects = (List<WorkShopMobileUser>) getIntent().getSerializableExtra("mSelects");
        setToolBar();
    }

    private void setToolBar() {
        toolBar.setTitle_text("学员考核");
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void setListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.rb_excellent:
                        workshopResult = "excellent";
                        break;
                    case R.id.rb_qualified:
                        workshopResult = "qualified";
                        break;
                    case R.id.rb_fail:
                        workshopResult = "fail";
                        break;
                }
            }
        });
        bt_evaluate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(workshopResult)) {
                    showMaterialDialog("提示", "请先选择评价项");
                } else {
                    evaluate();
                }
            }
        });
    }

    private void evaluate() {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/m/workshop_user/evaluate";
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("workshopResult", workshopResult);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mSelects.size(); i++) {
            String id = mSelects.get(i).getId();
            sb.append(id);
            if (i < mSelects.size() - 1) {
                sb.append(",");
            }
        }
        map.put("workshopUserIds", sb.toString());
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
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        }, map));
    }
}
