package com.haoyu.app.activity;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 创建日期：2017/12/13.
 * 描述:工作坊成员考核或向学员发送消息
 * 作者:xiaoma
 */

public class WSExamineMsgActivity extends BaseActivity {
    private WSExamineMsgActivity context;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_content)
    EditText et_content;
    @BindView(R.id.bt_send)
    Button bt_send;
    private List<WorkShopMobileUser> mSelects;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_wsexaminemsg;
    }

    @Override
    public void initView() {
        context = this;
        mSelects = (List<WorkShopMobileUser>) getIntent().getSerializableExtra("mSelects");
        setToolBar();
        et_content.setHint("此处输入消息内容");
    }

    private void setToolBar() {
        toolBar.setTitle_text("发送消息");
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void setListener() {
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = et_content.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    showMaterialDialog("提示", "请输入消息内容");
                } else {
                    sendMsg(content);
                }
            }
        });
    }

    private void sendMsg(String content) {
        showTipDialog();
        addSubscription(Flowable.just(content).map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String content) throws Exception {
                return send(content);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean finish) throws Exception {
                hideTipDialog();
                toast(context, "发送成功");
                setResult(RESULT_OK);
                finish();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                hideTipDialog();
                toast(context, "发送失败");
            }
        }));
    }

    private boolean send(String content) {
        String url = Constants.OUTRT_NET + "/m/message";
        Map<String, String> map = new HashMap<>();
        map.put("sender.id", getUserId());
        map.put("content", content);
        for (int i = 0; i < mSelects.size(); i++) {
            if (mSelects.get(i).getmUser() != null) {
                try {
                    String receiverId = mSelects.get(i).getmUser().getId();
                    map.put("receiver.id", receiverId);
                    OkHttpClientManager.postAsString(context, url, map);
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return true;
    }
}
