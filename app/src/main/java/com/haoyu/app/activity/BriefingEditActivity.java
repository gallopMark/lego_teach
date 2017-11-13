package com.haoyu.app.activity;

import android.content.Intent;
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
import com.haoyu.app.entity.BriefingEntity;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 研修简报创建或修改
 */
public class BriefingEditActivity extends BaseActivity {
    private BriefingEditActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_title)
    EditText et_title;
    @BindView(R.id.et_content)
    EditText et_content;
    private String relationId, relationType, briefId;
    private boolean isAlter;
    private long createTime;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_briefing_edit;
    }

    @Override
    public void initView() {
        relationId = getIntent().getStringExtra("relationId");
        relationType = getIntent().getStringExtra("relationType");
        briefId = getIntent().getStringExtra("briefId");
        isAlter = getIntent().getBooleanExtra("isAlter", false);
        if (isAlter)
            toolBar.setTitle_text("编辑简报");
        else
            toolBar.setTitle_text("研修简报");
    }

    public void initData() {
        if (isAlter) {
            String url = Constants.OUTRT_NET + "/m/announcement/view/" + briefId;
            addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<BriefingEntity>>() {
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
                public void onResponse(BaseResponseResult<BriefingEntity> response) {
                    hideTipDialog();
                    if (response != null && response.getResponseData() != null) {
                        initContent(response.getResponseData());
                    }
                }
            }));
        }
    }

    private void initContent(BriefingEntity entity) {
        et_title.setText(entity.getTitle());
        Spanned spanned = Html.fromHtml(entity.getContent());
        et_content.setText(spanned);
        Editable editable = et_title.getText();
        Selection.setSelection(editable, editable.length());
        createTime = entity.getCreateTime();
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
                    toast(context, "请输入简报标题");
                } else if (content.length() == 0) {
                    toast(context, "请输入简报内容");
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

    /*创建研修简报*/
    private void create(String title, final String content) {
        /**
         * title	标题	String	Y
         content	内容	String	Y	长度最大为1000
         type	类型	String	Y	工作坊的研修简报：“workshop_briefing”
         announcementRelations[0].relation.id	关联Id	String	Y	工作坊：工作坊Id
         announcementRelations[0].relation.type	关联类型	String	Y	工作坊：“workshop”
         */
        String url = Constants.OUTRT_NET + "/m/briefing";
        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("content", content);
        map.put("type", "workshop_briefing");
        map.put("announcementRelations[0].relation.id", relationId);
        map.put("announcementRelations[0].relation.type", relationType);
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<BriefingEntity>>() {
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
            public void onResponse(BaseResponseResult<BriefingEntity> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    BriefingEntity entity = response.getResponseData();
                    Intent intent = new Intent();
                    intent.putExtra("entity", entity);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    toast(context, "创建失败");
                }
            }
        }, map));
    }

    /*修改研修简报*/
    private void alter(String title, final String content) {
        String url = Constants.OUTRT_NET + "/m/briefing/" + briefId;
        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("content", content);
        map.put("_method", "put");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<BriefingEntity>>() {
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
            public void onResponse(BaseResponseResult<BriefingEntity> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    BriefingEntity entity = response.getResponseData();
                    entity.setCreateTime(createTime);
                    Intent intent = new Intent();
                    intent.putExtra("entity", entity);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    toast(context, "修改失败");
                }
            }
        }, map));
    }
}
