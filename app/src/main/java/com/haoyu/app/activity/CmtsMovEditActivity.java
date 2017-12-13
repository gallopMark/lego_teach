package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.AppBaseAdapter;
import com.haoyu.app.basehelper.ViewHolder;
import com.haoyu.app.dialog.DateTimePickerDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.RoundRectProgressBar;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.disposables.Disposable;
import okhttp3.Request;

/**
 * 创建日期：2017/12/7.
 * 描述:教研发起活动
 * 作者:马飞奔 Administrator
 * “communicationMeeting”：跨校交流会
 * “expertInteraction”：专家互动
 * “lessonViewing”：创课观摩
 * “ticket”：须报名预约，凭电子票入场
 * “free”：在线报名，免费入场
 * “chair”:讲座视频录像+在线问答交流
 * 作者:xiaoma
 */

public class CmtsMovEditActivity extends BaseActivity implements View.OnClickListener {
    private CmtsMovEditActivity context;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.scrollview)
    ScrollView scrollview;
    /*****************************/
    @BindView(R.id.fl_imageLayout)
    FrameLayout fl_imageLayout;
    @BindView(R.id.ll_addImage)
    LinearLayout ll_addImage;
    @BindView(R.id.fl_image)
    FrameLayout fl_image;
    @BindView(R.id.iv_image)
    ImageView iv_image;  //显示封面图片
    @BindView(R.id.ll_progress)
    LinearLayout ll_progress;   //图片上传进度布局
    @BindView(R.id.tv_imageName)
    TextView tv_imageName;   //图片名称
    @BindView(R.id.progressBar)
    RoundRectProgressBar progressBar;  //图片上传进度条
    @BindView(R.id.tv_progress)
    TextView tv_progress;   //显示图片上传百分比
    @BindView(R.id.tv_error)
    TextView tv_error;   //上传失败
    @BindView(R.id.iv_delete)
    ImageView iv_delete;  //取消上传或删除图片
    /*****************************/
    @BindView(R.id.et_title)
    EditText et_title;   //标题
    @BindView(R.id.tv_type)
    TextView tv_type;   //活动类型
    @BindView(R.id.et_content)
    EditText et_content;  //活动内容
    @BindView(R.id.et_host)
    EditText et_host;  //主办单位
    @BindView(R.id.et_address)
    EditText et_address;
    @BindView(R.id.ll_beginTime)
    LinearLayout ll_beginTime;
    @BindView(R.id.tv_beginTime)
    TextView tv_beginTime;
    @BindView(R.id.ll_endTime)
    LinearLayout ll_endTime;
    @BindView(R.id.tv_endTime)
    TextView tv_endTime;
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;   //参与方式
    @BindView(R.id.ll_ticket)
    LinearLayout ll_ticket;
    @BindView(R.id.et_ticketNum)
    EditText et_ticketNum;
    @BindView(R.id.ll_partTime)
    LinearLayout ll_partTime;   //报名截止时间
    @BindView(R.id.tv_partTime)
    TextView tv_partTime;
    @BindView(R.id.ll_partUser)
    LinearLayout ll_partUser;
    @BindView(R.id.tv_partUser)
    TextView tv_partUser;
    private MFileInfo imageInfo;
    private boolean isUploading;  //是否正在上传
    private Disposable disposable;
    private String movType, partType;// 活动类型，参与方式
    private List<String> movTypeList = new ArrayList<>();
    private String[] moveTypeArr, partTypeArr;
    private int movTypeSelect = -1;
    private boolean isTicket; //是否是电子票入场方式
    private int startYear, startMonth, startDay, startHour, startMinute;
    private int endYear, endMonth, endDay, endHour, endMinute;
    private int partYear, partMonth, partDay;
    private List<MobileUser> selectUsers = new ArrayList<>();

    private class TypeAdapter extends AppBaseAdapter<String> {

        private int selectItem;

        public TypeAdapter(Context context, List<String> mDatas, int selectItem) {
            super(context, mDatas);
            this.selectItem = selectItem;
        }

        @Override
        public void convert(ViewHolder holder, String content, int position) {
            TextView tvDict = holder.getView(R.id.tvDict);
            tvDict.setText(content);
            Drawable select = ContextCompat.getDrawable(context, R.drawable.train_item_selected);
            select.setBounds(0, 0, select.getMinimumWidth(), select.getMinimumHeight());
            if (selectItem == position) {
                tvDict.setCompoundDrawables(null, null, select, null);
                tvDict.setCompoundDrawablePadding(PixelFormat.dp2px(context, 10));
            } else {
                tvDict.setCompoundDrawables(null, null, null, null);
            }
        }

        @Override
        public int getmItemLayoutId() {
            return R.layout.popupwindow_dictionary_item;
        }
    }

    @Override
    public int setLayoutResID() {
        return R.layout.activity_cmts_createmov;
    }

    @Override
    public void initView() {
        context = this;
        int imageHeight = ScreenUtils.getScreenHeight(context) / 4;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imageHeight);
        fl_imageLayout.setLayoutParams(params);
        initMovType();
        partTypeArr = new String[]{"ticket", "free", "chair"};
        partType = partTypeArr[0];
        isTicket = true;
    }

    private void initMovType() {
        moveTypeArr = new String[]{"communicationMeeting", "expertInteraction", "lessonViewing"};
        movType = moveTypeArr[0];
        movTypeSelect = 0;
        String communicationMeeting = getResources().getString(R.string.teach_active_communicationMeeting);
        tv_type.setText(communicationMeeting);
        String expertInteraction = getResources().getString(R.string.teach_active_expertInteraction);
        String lessonViewing = getResources().getString(R.string.teach_active_lessonViewing);
        movTypeList.add(communicationMeeting);
        movTypeList.add(expertInteraction);
        movTypeList.add(lessonViewing);
    }

    @Override
    public void setListener() {
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                if (checkOut()) {
                    commit();
                }
            }
        });
        ll_addImage.setOnClickListener(context);
        iv_delete.setOnClickListener(context);
        tv_type.setOnClickListener(context);
        ll_beginTime.setOnClickListener(context);
        ll_endTime.setOnClickListener(context);
        ll_partTime.setOnClickListener(context);
        ll_partUser.setOnClickListener(context);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.rb_ticket:
                        isTicket = true;
                        partType = partTypeArr[0];
                        break;
                    case R.id.rb_free:
                        isTicket = false;
                        partType = partTypeArr[1];
                        break;
                    case R.id.rb_chair:
                        isTicket = false;
                        partType = partTypeArr[2];
                        break;
                }
                if (isTicket) {
                    ll_ticket.setVisibility(View.VISIBLE);
                } else {
                    ll_ticket.setVisibility(View.GONE);
                }
                scrollview.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                }, 10);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_addImage:
                pickerPicture();
                break;
            case R.id.iv_delete:
                deleteVideo();
                break;
            case R.id.tv_type:
                showTypeWindow();
                break;
            case R.id.ll_beginTime:
                setTimeDialog(tv_beginTime, 1);
                break;
            case R.id.ll_endTime:
                setTimeDialog(tv_endTime, 2);
                break;
            case R.id.ll_partTime:
                setTimeDialog(tv_partTime, 3);
                break;
            case R.id.ll_partUser:
                Intent intent = new Intent(context, MultiSearchUsersActivity.class);
                intent.putExtra("mobileUserList", (Serializable) selectUsers);
                startActivityForResult(intent, 1);
                break;
        }
    }

    private void pickerPicture() {
        MediaOption option = new MediaOption.Builder()
                .setSelectType(MediaOption.TYPE_IMAGE)
                .setShowCamera(true)
                .build();
        MediaPicker.getInstance().init(option).selectMedia(context, new MediaPicker.onSelectMediaCallBack() {
            @Override
            public void onSelected(String path) {
                setImageFile(path);
            }
        });
    }

    private void setImageFile(String path) {
        File image = new File(path);
        ll_addImage.setVisibility(View.GONE);
        fl_image.setVisibility(View.VISIBLE);
        Glide.with(context).load(path).centerCrop().into(iv_image);
        tv_imageName.setText(image.getName());
        uploadImage(image);
    }

    private void uploadImage(final File image) {
        String url = Constants.OUTRT_NET + "/m/file/uploadFileInfoRemote";
        disposable = OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<FileUploadResult>() {
            @Override
            public void onBefore(Request request) {
                isUploading = true;
                if (ll_progress.getVisibility() != View.VISIBLE) {
                    ll_progress.setVisibility(View.VISIBLE);
                }
                if (tv_error.getVisibility() != View.GONE) {
                    tv_error.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                setError(image);
            }

            @Override
            public void onResponse(FileUploadResult response) {
                isUploading = false;
                handler.removeMessages(CODE_IMAGE);
                if (response != null && response.getResponseData() != null) {
                    imageInfo = response.getResponseData();
                    tv_progress.setText("已上传");
                }
            }
        }, image, image.getName(), new OkHttpClientManager.ProgressListener() {
            @Override
            public void onProgress(final long totalBytes, final long remainingBytes, boolean done, File file) {
                Message msg = handler.obtainMessage(CODE_IMAGE);
                Bundle bundle = new Bundle();
                bundle.putLong("totalBytes", totalBytes);
                bundle.putLong("remainingBytes", remainingBytes);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
    }

    private final int CODE_IMAGE = 1;
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

    private void setError(final File image) {
        isUploading = false;
        ll_progress.setVisibility(View.GONE);
        tv_error.setVisibility(View.VISIBLE);
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        handler.removeMessages(CODE_IMAGE);
        tv_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage(image);
            }
        });
    }

    private void deleteVideo() {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage("您确定删除封面吗？");
        dialog.setPositiveButton("确定", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                isUploading = false;
                imageInfo = null;
                iv_image.setImageResource(0);
                fl_image.setVisibility(View.GONE);
                ll_addImage.setVisibility(View.VISIBLE);
                if (disposable != null) {
                    disposable.dispose();
                }
                handler.removeMessages(CODE_IMAGE);
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    private void showTypeWindow() {
        Drawable shouqi = ContextCompat.getDrawable(context, R.drawable.course_dictionary_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(), shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context, R.drawable.course_dictionary_xiala);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(), zhankai.getMinimumHeight());
        tv_type.setCompoundDrawables(null, null, shouqi, null);
        ListView listView = new ListView(context);
        listView.setDivider(null);
        listView.setSelector(ContextCompat.getDrawable(context, R.drawable.item_click_selector));
        listView.setBackgroundResource(R.drawable.dictionary_background);
        final PopupWindow popupWindow = new PopupWindow(listView, tv_type.getWidth(), LinearLayout.LayoutParams.WRAP_CONTENT);
        TypeAdapter adapter = new TypeAdapter(context, movTypeList, movTypeSelect);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                movTypeSelect = position;
                tv_type.setText(movTypeList.get(position));
                if (movTypeSelect == 0) {
                    movType = moveTypeArr[0];
                } else if (movTypeSelect == 1) {
                    movType = moveTypeArr[1];
                } else {
                    movType = moveTypeArr[2];
                }
                popupWindow.dismiss();
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv_type.setCompoundDrawables(null, null, zhankai, null);
            }
        });
        popupWindow.showAsDropDown(tv_type);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            List<MobileUser> mobileUserList = (List<MobileUser>) data.getSerializableExtra("mobileUserList");
            if (mobileUserList != null && mobileUserList.size() > 0) {
                selectUsers.clear();
                selectUsers.addAll(mobileUserList);
                setUser_text();
            }
        }
    }

    private void setUser_text() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectUsers.size(); i++) {
            String name = selectUsers.get(i).getRealName();
            sb.append(name);
            if (i < selectUsers.size() - 1) {
                sb.append("，");
            }
        }
        tv_partUser.setText(sb);
        if (overLine(tv_partUser)) {
            tv_partUser.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            sb.append(" 等人");
            tv_partUser.setText(sb);
        }
    }

    private boolean overLine(TextView tv) {
        Layout layout = tv.getLayout();
        if (layout != null && layout.getLineCount() > 0) {
            int lines = layout.getLineCount();//获取textview行数
            if (layout.getEllipsisCount(lines - 1) > 0) {//获取最后一行省略掉的字符数，大于0就代表超过行数
                return true;
            }
        }
        return false;
    }

    private void setTimeDialog(final TextView tv, final int type) {
        DateTimePickerDialog dialog = new DateTimePickerDialog(context, true);
        if (type == 1) {
            dialog.setTitle("活动开始时间");
        } else if (type == 2) {
            dialog.setTitle("活动结束时间");
        } else {
            dialog.setTitle("报名截止时间");
        }
        dialog.setPositiveButton("确定");
        dialog.setDateListener(new DateTimePickerDialog.DateListener() {
            private StringBuilder sb = new StringBuilder();

            @Override
            public void Date(int year, int month, int day) {
                String mMonth = month < 10 ? "0" + month : String.valueOf(month);
                String mDay = day < 10 ? "0" + day : String.valueOf(day);
                String text = year + "-" + mMonth + "-" + mDay;
                sb.append(text);
                if (type == 1) {
                    startYear = year;
                    startMonth = month;
                    startDay = day;
                } else if (type == 2) {
                    endYear = year;
                    endMonth = month;
                    endDay = day;
                } else {
                    partYear = year;
                    partMonth = month;
                    partDay = day;
                }
            }

            @Override
            public void Time(int hour, int minute) {
                String hourStr = hour < 10 ? " 0" + hour : " " + hour;
                sb.append(hourStr);
                sb.append(":");
                String minuteStr = minute < 10 ? " 0" + minute : " " + minute;
                sb.append(minuteStr);
                if (type == 1) {
                    startHour = hour;
                    startMinute = minute;
                    if (checkStartTime()) {
                        tv.setText(sb);
                    }
                } else if (type == 2) {
                    endHour = hour;
                    endMinute = minute;
                    if (checkEndTime()) {
                        tv.setText(sb);
                    }
                } else {
                    if (checkPartTime()) {
                        tv.setText(sb);
                    }
                }
            }
        });
        dialog.show();
    }

    private boolean checkStartTime() {
        Calendar c = Calendar.getInstance();
        int nowYear = c.get(Calendar.YEAR);
        int nowMonth = c.get(Calendar.MONTH) + 1;
        int nowDay = c.get(Calendar.DAY_OF_MONTH);
        String message = "活动开始时间不能是";
        if (startYear < nowYear) {
            message += nowYear + "年前";
            showMaterialDialog("提示", message);
            return false;
        } else if (startYear == nowYear) {
            if (startMonth < nowMonth) {
                message += nowYear + "年" + nowMonth + "月前";
                showMaterialDialog("提示", message);
                return false;
            } else {
                if (startDay < nowDay) {
                    message += nowYear + "年" + nowMonth + "月" + nowDay + "日前";
                    showMaterialDialog("提示", message);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkEndTime() {
        String startTime = tv_beginTime.getText().toString();
        if (TextUtils.isEmpty(startTime)) {
            showMaterialDialog("提示", "请先选择活动开始时间");
            return false;
        }
        String message = "活动结束时间不能是";
        if (endYear < startYear) {
            message += startYear + "年前";
            showMaterialDialog("提示", message);
            return false;
        } else if (endYear == startYear) {
            if (endMonth < startMonth) {
                message += startYear + "年" + startMonth + "月前";
                showMaterialDialog("提示", message);
                return false;
            } else {
                if (endDay < startDay) {
                    message += startYear + "年" + startMonth + "月" + startDay + "日前";
                    showMaterialDialog("提示", message);
                    return false;
                } else if (endDay == startDay) {
                    if (endHour < startHour) {
                        showMaterialDialog("提示", "活动时间结束必须大于活动开始时间");
                        return false;
                    } else if (endHour == startHour) {
                        if (endMinute < startMinute) {
                            showMaterialDialog("提示", "活动时间结束必须大于活动开始时间");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean checkPartTime() {
        Calendar c = Calendar.getInstance();
        int nowYear = c.get(Calendar.YEAR);
        int nowMonth = c.get(Calendar.MONTH) + 1;
        int nowDay = c.get(Calendar.DAY_OF_MONTH);
        String message = "活动开始时间不能是";
        if (partYear < nowYear) {
            message += nowYear + "年前";
            showMaterialDialog("提示", message);
            return false;
        } else if (partYear == nowYear) {
            if (partMonth < nowMonth) {
                message += nowYear + "年" + nowMonth + "月前";
                showMaterialDialog("提示", message);
                return false;
            } else {
                if (partDay < nowDay) {
                    message += nowYear + "年" + nowMonth + "月" + nowDay + "日前";
                    showMaterialDialog("提示", message);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkOut() {
        String tipMsg = null;
        String title = et_title.getText().toString().trim();
        String content = et_content.getText().toString().trim();
        String host = et_host.getText().toString().trim();
        String address = et_address.getText().toString().trim();
        String startTime = tv_beginTime.getText().toString().trim();
        String endTime = tv_endTime.getText().toString().trim();
        if (isUploading) {
            tipMsg = "请等待封面图片上传完毕";
        } else if (TextUtils.isEmpty(title)) {
            tipMsg = "请输入活动标题";
        } else if (TextUtils.isEmpty(movType)) {
            tipMsg = "请选择活动类型";
        } else if (TextUtils.isEmpty(content)) {
            tipMsg = "请输入活动内容";
        } else if (TextUtils.isEmpty(address)) {
            tipMsg = "请输入活动地点";
        } else if (TextUtils.isEmpty(host)) {
            tipMsg = "请输入主办单位";
        } else if (TextUtils.isEmpty(startTime)) {
            tipMsg = "请选择活动开始时间";
        } else if (TextUtils.isEmpty(endTime)) {
            tipMsg = "请选择活动结束时间";
        } else if (TextUtils.isEmpty(partType)) {
            tipMsg = "请选择参与方式";
        }
        if (tipMsg != null) {
            showMaterialDialog("提示", tipMsg);
            return false;
        }
        return true;
    }

    private void commit() {
        String title = et_title.getText().toString();
        String content = et_content.getText().toString();
        String sponsor = et_host.getText().toString();
        String location = et_address.getText().toString();
        String startTime = tv_beginTime.getText().toString();
        String endTime = tv_endTime.getText().toString();
        String url = Constants.OUTRT_NET + "/m/movement";
        Map<String, String> map = new HashMap<>();
        if (imageInfo != null) {
            map.put("image", imageInfo.getUrl());
        }
        map.put("title", title);
        map.put("type", movType);
        map.put("content", content);
        map.put("sponsor", sponsor);
        map.put("location", location);
        map.put("movementRelations[0].startTime", startTime);
        map.put("movementRelations[0].endTime", endTime);
        map.put("participationType", partType);
        map.put("status", "verifying");
        if (isTicket) {
            String endPartTime = tv_partTime.getText().toString();
            String ticketNum = et_ticketNum.getText().toString();
            if (!TextUtils.isEmpty(endPartTime)) {
                map.put("movementRelations[0].registerEndTime", endPartTime);
            }
            if (!TextUtils.isEmpty(ticketNum)) {
                map.put("movementRelations[0].ticketNum", ticketNum);
            }
        }
        if (selectUsers.size() > 0) {
            for (int i = 0; i < selectUsers.size(); i++) {
                map.put("movementRegisters[" + i + "].userInfo.id", selectUsers.get(i).getId());
                map.put("movementRegisters[" + i + "].state", "passive");
            }
        }
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
                    toastFullScreen("发表成功", true);
                    finish();
                } else {
                    toastFullScreen("发表失败", false);
                }
            }
        }, map));
    }

    @Override
    protected void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
