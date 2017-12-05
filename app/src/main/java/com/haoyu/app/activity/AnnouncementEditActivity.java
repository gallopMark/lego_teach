package com.haoyu.app.activity;

import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.Spanned;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.entity.Announcement;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/2/23 on 15:56
 * 描述: 通知公告编辑
 * 作者:马飞奔 Administrator
 */
public class AnnouncementEditActivity extends BaseActivity {
    private AnnouncementEditActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.et_content)
    EditText et_content;
    private String relationId, type, entityId;   //工作坊Id，通知公告Id
    private boolean isAlter;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_announcement_edit;
    }

    @Override
    public void initView() {
        relationId = getIntent().getStringExtra("relationId");
        type = getIntent().getStringExtra("type");
        entityId = getIntent().getStringExtra("entityId");
        isAlter = getIntent().getBooleanExtra("isAlter", false);
    }

    /*获取通知公告详细*/
    public void initData() {
        if (isAlter) {
            String url = Constants.OUTRT_NET + "/m/announcement/view/" + entityId;
            addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<Announcement>>() {
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
                public void onResponse(BaseResponseResult<Announcement> response) {
                    hideTipDialog();
                    if (response != null && response.getResponseData() != null) {
                        updateUI(response.getResponseData());
                    }
                }
            }));
        }
    }

    private void updateUI(Announcement entity) {
        et_title.setText(entity.getTitle());
        Spanned spanned = Html.fromHtml(entity.getContent());
        et_content.setText(spanned);
        Editable editable = et_title.getText();
        Selection.setSelection(editable, editable.length());
    }

    @Override
    public void setListener() {
        et_title.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // et.getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
                Drawable drawable = et_title.getCompoundDrawables()[2];
                //如果右边没有图片，不再处理
                if (drawable == null)
                    return false;
                //如果不是按下事件，不再处理
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (event.getX() > et_title.getWidth()
                        - et_title.getPaddingRight()
                        - drawable.getIntrinsicWidth()) {
                    et_title.setSelection(et_title.getText().length());//将光标移至文字末尾
                }
                return false;
            }
        });
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                String title = et_title.getText().toString().trim();
                String content = et_content.getText().toString().trim();
                if (title.length() == 0) {
                    showMaterialDialog("提示", "请输入公告标题");
                } else if (content.length() == 0) {
                    showMaterialDialog("提示", "请输入公告内容");
                } else {
                    if (isAlter) {
                        alter(title, content);
                    } else {
                        create(title, content);
                    }
                }
            }
        });
    }

    /*创建通知公告*/
    private void create(String title, final String content) {
        /**
         * title	标题	String	Y
         content	内容	String	Y	长度最大为1000
         type	类型	String	Y	系统通知：“system_announcement”
         工作坊的通知公告：“workshop_announcement”
         培训简报：“train_report”
         announcementRelations[0].relation.id	关联Id	String	Y	系统：“system”
         工作坊：工作坊Id

         announcementRelations[0].relation.type	关联类型	String	Y	系统：不需要传此参数
         工作坊：“workshop”

         */
        String url = Constants.OUTRT_NET + "/m/announcement";
        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("content", content);
        map.put("type", type);
        map.put("announcementRelations[0].relation.id", relationId);
        map.put("announcementRelations[0].relation.type", "workshop");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<Announcement>>() {
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
            public void onResponse(BaseResponseResult<Announcement> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_ANNOUNCEMENT;
                    event.obj = response.getResponseData();
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    toast(context, "创建失败");
                }
            }
        }, map));
    }

    /*修改通知公告*/
    private void alter(String title, String content) {
        String url = Constants.OUTRT_NET + "/m/announcement/" + entityId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        map.put("title", title);
        map.put("content", content);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<Announcement>>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(BaseResponseResult<Announcement> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    MessageEvent event = new MessageEvent();
                    event.action = Action.ALTER_ANNOUNCEMENT;
                    event.obj = response.getResponseData();
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    toast(context, "创建失败");
                }
            }
        }, map));
    }

}
