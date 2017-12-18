package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.haoyu.app.adapter.CourseSectionAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.CourseChildSectionEntity;
import com.haoyu.app.entity.CourseSectionEntity;
import com.haoyu.app.entity.CourseSectionResult;
import com.haoyu.app.entity.FileUploadDataResult;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MultiItemEntity;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.RoundRectProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.disposables.Disposable;
import okhttp3.Request;

/**
 * 创建日期：2017/2/8 on 14:18
 * 描述:课程资源上传
 * 作者:马飞奔 Administrator
 */
public class ResourcesUploadActivity extends BaseActivity implements View.OnClickListener {
    private ResourcesUploadActivity context = this;
    private String courseId;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.tv_section)
    TextView tv_section;
    @BindView(R.id.iv_select)
    ImageView iv_select;
    @BindView(R.id.fl_media)
    FrameLayout fl_media;
    @BindView(R.id.iv_media)
    ImageView iv_media;
    @BindView(R.id.ll_progress)
    LinearLayout ll_progress;
    @BindView(R.id.tv_mediaName)
    TextView tv_mediaName;
    @BindView(R.id.progressBar)
    RoundRectProgressBar progressBar;
    @BindView(R.id.tv_progress)
    TextView tv_progress;
    @BindView(R.id.tv_error)
    TextView tv_error;
    @BindView(R.id.iv_delete)
    ImageView iv_delete;
    private List<CourseSectionEntity> courseSections;
    private String sectionId;
    private Disposable mPosable;
    private MFileInfo mFileInfo;
    private boolean isOpenMobile, isUploadding;
    private final int CODE_MEDIA = 1;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_resource_upload;
    }

    @Override
    public void initView() {
        courseId = getIntent().getStringExtra("courseId");
        setToolBar();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fl_media.getLayoutParams();
        int width = ScreenUtils.getScreenWidth(context) / 4 * 3;
        params.width = ScreenUtils.getScreenWidth(context) / 5 * 4;
        params.height = width / 4 * 3;
        fl_media.setLayoutParams(params);
    }

    private void setToolBar() {
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                if (isCheckOut()) {
                    commit();
                }
            }
        });
    }

    /**
     * 获取章节列表
     */
    public void loadData() {
        String url = Constants.OUTRT_NET + "/" + courseId + "/teach/m/course/" + courseId + "/teach";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<CourseSectionResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(CourseSectionResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null && response.getResponseData().getmCourse() != null
                        && response.getResponseData().getmCourse().getmSections() != null
                        && response.getResponseData().getmCourse().getmSections().size() > 0) {
                    courseSections = new ArrayList<>();
                    courseSections.addAll(response.getResponseData().getmCourse().getmSections());
                    showSectionDialog();
                }
            }
        }));
    }

    private void showSectionDialog() {
        Drawable shouqi = ContextCompat.getDrawable(context, R.drawable.course_dictionary_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(), shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context, R.drawable.course_dictionary_xiala);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(), zhankai.getMinimumHeight());
        tv_section.setCompoundDrawables(null, null, shouqi, null);
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.spaceColor));
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        final PopupWindow popupWindow = new PopupWindow(recyclerView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        List<MultiItemEntity> mDatas = new ArrayList<>();
        for (int i = 0; i < courseSections.size(); i++) {
            CourseSectionEntity entity = courseSections.get(i);
            mDatas.add(entity);
            if (entity.getChildSections() != null && entity.getChildSections().size() > 0)
                for (int j = 0; j < entity.getChildSections().size(); j++) {
                    mDatas.add(entity.getChildSections().get(j));
                }
        }
        CourseSectionAdapter sectionAdapter = new CourseSectionAdapter(context, mDatas);
        recyclerView.setAdapter(sectionAdapter);
        sectionAdapter.setOnSectionClickListener(new CourseSectionAdapter.OnSectionClickListener() {
            @Override
            public void onSectionSelected(CourseChildSectionEntity entity) {
                sectionId = entity.getId();
                tv_section.setText(entity.getTitle());
                popupWindow.dismiss();
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv_section.setCompoundDrawables(null, null, zhankai, null);
            }
        });
        popupWindow.showAsDropDown(tv_section);
    }

    @Override
    public void setListener() {
        iv_select.setOnClickListener(context);
        tv_section.setOnClickListener(context);
        iv_delete.setOnClickListener(context);
    }

    @Override
    public void onClick(View v) {
        Common.hideSoftInput(context, et_name);
        switch (v.getId()) {
            case R.id.tv_section:
                if (courseSections == null) {
                    loadData();
                } else {
                    showSectionDialog();
                }
                break;
            case R.id.iv_select:
                showBottomDialog();
                break;
            case R.id.iv_delete:
                MaterialDialog videoDialog = new MaterialDialog(context);
                videoDialog.setTitle("提示");
                videoDialog.setMessage("确定删除此附件吗？");
                videoDialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
                    @Override
                    public void onClick(View v, AlertDialog dialog) {
                        deleteMedia();
                    }
                });
                videoDialog.setNegativeButton("取消", null);
                videoDialog.show();
                break;
        }
    }

    private void deleteMedia() {
        isUploadding = false;
        mFileInfo = null;
        iv_media.setImageResource(0);
        fl_media.setVisibility(View.GONE);
        iv_select.setVisibility(View.VISIBLE);
        if (mPosable != null && !mPosable.isDisposed()) {
            mPosable.dispose();
        }
        handler.removeMessages(CODE_MEDIA);
    }

    private void showBottomDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_selectresource, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        Button bt_picture = view.findViewById(R.id.bt_picture);
        Button bt_video = view.findViewById(R.id.bt_video);
        bt_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                pickerMedia(1);
            }
        });
        bt_video.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                pickerMedia(2);
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(ScreenUtils.getScreenWidth(context), LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setWindowAnimations(R.style.dialogWindowAnim);
        window.setContentView(view);
        window.setGravity(Gravity.BOTTOM);
    }

    private void pickerMedia(final int mediaType) {
        MediaOption.Builder builder = new MediaOption.Builder();
        if (mediaType == 1) {
            builder.setSelectType(MediaOption.TYPE_IMAGE)
                    .setShowCamera(true);
        } else {
            builder.setSelectType(MediaOption.TYPE_VIDEO)
                    .setShowCamera(true);
        }
        MediaOption option = builder.build();
        MediaPicker.getInstance().init(option).selectMedia(context, new MediaPicker.onSelectMediaCallBack() {
            @Override
            public void onSelected(final String path) {
                if (new File(path).exists()) {
                    if (mediaType == 2 && NetStatusUtil.isConnected(context) && !NetStatusUtil.isWifi(context) && !isOpenMobile) {
                        MaterialDialog dialog = new MaterialDialog(context);
                        dialog.setTitle("网络提醒");
                        dialog.setMessage("使用2G/3G/4G网络上传视频会消耗较多流量。确定要开启吗？");
                        dialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
                            @Override
                            public void onClick(View v, AlertDialog dialog) {
                                isOpenMobile = true;
                                setMedia(path);
                            }
                        });
                        dialog.setNegativeButton("取消", null);
                        dialog.show();
                    } else {
                        setMedia(path);
                    }
                } else {
                    toast(context, "文件不存在");
                }
            }
        });
    }

    private void setMedia(String path) {
        File mFile = new File(path);
        iv_select.setVisibility(View.GONE);
        fl_media.setVisibility(View.VISIBLE);
        Glide.with(context).load(path).centerCrop().into(iv_media);
        tv_mediaName.setText(mFile.getName());
        uploadMedia(mFile);
    }

    private void uploadMedia(final File mFile) {
        String url = Constants.OUTRT_NET + "/m/file/uploadTemp";
        mPosable = OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<FileUploadResult>() {
            @Override
            public void onBefore(Request request) {
                isUploadding = true;
                if (ll_progress.getVisibility() != View.VISIBLE) {
                    ll_progress.setVisibility(View.VISIBLE);
                }
                if (tv_error.getVisibility() != View.GONE) {
                    tv_error.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                setError(mFile);
            }

            @Override
            public void onResponse(FileUploadResult response) {
                isUploadding = false;
                handler.removeMessages(CODE_MEDIA);
                if (response != null && response.getResponseData() != null) {
                    mFileInfo = response.getResponseData();
                    tv_progress.setText("已上传");
                }
            }
        }, mFile, mFile.getName(), new OkHttpClientManager.ProgressListener() {
            @Override
            public void onProgress(final long totalBytes, final long remainingBytes, boolean done, File file) {
                Message message = new Message();
                message.what = CODE_MEDIA;
                Bundle bundle = new Bundle();
                bundle.putLong("totalBytes", totalBytes);
                bundle.putLong("remainingBytes", remainingBytes);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        });
    }

    private void setError(final File mFile) {
        isUploadding = false;
        ll_progress.setVisibility(View.GONE);
        tv_error.setVisibility(View.VISIBLE);
        if (mPosable != null && !mPosable.isDisposed()) {
            mPosable.dispose();
        }
        handler.removeMessages(CODE_MEDIA);
        tv_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadMedia(mFile);
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            long totalBytes = bundle.getLong("totalBytes");
            long remainingBytes = bundle.getLong("remainingBytes");
            progressBar.setMax((int) totalBytes);
            progressBar.setProgress((int) (totalBytes - remainingBytes));
            long progress = (totalBytes - remainingBytes) * 100 / totalBytes;
            tv_progress.setText("上传中" + "\u2000" + progress + "%");
        }
    };

    private boolean isCheckOut() {
        String name = et_name.getText().toString().trim();
        if (name.length() == 0) {
            showMaterialDialog("提示", "请输入资源名称");
            return false;
        } else if (sectionId == null) {
            showMaterialDialog("提示", "请选择所属章节");
            return false;
        } else if (isUploadding) {
            showMaterialDialog("提示", "请等待资源上传完毕");
            return false;
        } else if (mFileInfo == null) {
            showMaterialDialog("提示", "请选择上传的文件");
            return false;
        }
        return true;
    }

    private void commit() {
        String name = et_name.getText().toString().trim();
        String url = Constants.OUTRT_NET + "/m/resource";
        Map<String, String> map = new HashMap<>();
        map.put("title", name);
        map.put("resourceRelations[0].relation.id", sectionId);
        map.put("resourceRelations[0].relation.type", "section");
        map.put("belong", "personal");
        map.put("fileInfos[0].id", mFileInfo.getId());
        map.put("fileInfos[0].url", mFileInfo.getUrl());
        map.put("fileInfos[0].fileName", mFileInfo.getFileName());
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<FileUploadDataResult>() {
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
            public void onResponse(FileUploadDataResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    Intent intent = new Intent();
                    intent.putExtra("entity", response.getResponseData());
                    setResult(RESULT_OK, intent);
                    toastFullScreen("上传成功", true);
                    finish();
                } else {
                    toastFullScreen("上传失败", true);
                }
            }
        }, map));
    }

    @Override
    protected void onDestroy() {
        if (mPosable != null && !mPosable.isDisposed()) {
            mPosable.dispose();
        }
        handler.removeMessages(CODE_MEDIA);
        super.onDestroy();
    }
}