package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.AppBaseAdapter;
import com.haoyu.app.basehelper.ViewHolder;
import com.haoyu.app.dialog.DateTimePickerDialog;
import com.haoyu.app.dialog.FileUploadDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.ComstomListView;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.Request;


/**
 * 创建日期：2017/3/13 on 9:35
 * 描述:教研发起活动
 * 作者:马飞奔 Administrator
 * <p/>
 * “communicationMeeting”：跨校交流会
 * “expertInteraction”：专家互动
 * “lessonViewing”：创课观摩
 * <p/>
 * “ticket”：须报名预约，凭电子票入场
 * “free”：在线报名，免费入场
 * “chair”:讲座视频录像+在线问答交流
 */
public class CmtsMovementCreateActivity extends BaseActivity implements View.OnClickListener {
    private CmtsMovementCreateActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.add_picture)
    View add_picture;   //添加封面
    @BindView(R.id.line_content)
    View line_content;
    @BindView(R.id.line_host)
    View line_host;
    @BindView(R.id.line_address)
    View line_address;
    @BindView(R.id.line_ticketNum)
    View line_ticketNum;
    @BindView(R.id.et_title)
    EditText et_title;      //活动标题
    @BindView(R.id.tv_atType)
    TextView tv_atType;     //活动类型
    @BindView(R.id.et_content)
    EditText et_content;    //活动内容
    @BindView(R.id.et_host)
    EditText et_host;       //主办单位
    @BindView(R.id.et_address)
    EditText et_address;    //活动地点
    @BindView(R.id.tv_beginTime)
    TextView tv_beginTime;   //开始时间
    @BindView(R.id.tv_endTime)
    TextView tv_endTime;   //结束时间
    @BindView(R.id.tv_partType)
    TextView tv_partType;  //参与方式
    @BindView(R.id.ll_partType)
    View ll_partType;
    @BindView(R.id.et_ticketNum)
    EditText et_ticketNum;   //发票数量
    @BindView(R.id.tv_partTime)
    TextView tv_partTime;   //报名截止时间
    @BindView(R.id.ll_partUser)
    LinearLayout ll_partUser;
    @BindView(R.id.tv_partUser)
    TextView tv_partUser;   //应邀人员
    @BindView(R.id.iv_img)
    ImageView iv_img;
    private int indexPosition;
    private int index = 0;
    private List<MobileUser> userList = new ArrayList<>();
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @BindView(R.id.ll_type)
    LinearLayout ll_type;//活动类型
    @BindView(R.id.ll_beginTime)
    LinearLayout ll_beginTime;//开始时间
    @BindView(R.id.ll_endTime)
    LinearLayout ll_endTime;//结束时间
    @BindView(R.id.ll_tv_partType)
    LinearLayout ll_tv_partType;//参与方式
    @BindView(R.id.ll_partTime)
    LinearLayout ll_partTime;//报名截止时间
    @BindView(R.id.scrollview)
    NestedScrollView scrollview;
    @BindView(R.id.cl_parttype)
    ComstomListView cl_parttype;
    private String activityType;//活动类型
    private String mPartType;//参与方式
    private List<String> atTypeList = new ArrayList<>();
    private List<String> atPartList = new ArrayList<>();
    private int atTypeSelect = -1, atPartSelect = -1;
    private File uploadFile;

    private class StringListAdapter extends AppBaseAdapter<String> {

        private int selectItem;

        public StringListAdapter(Context context, List<String> mDatas, int selectItem) {
            super(context, mDatas);
            this.selectItem = selectItem;
        }

        @Override
        public void convert(ViewHolder holder, String content, int position) {
            TextView tvDict = holder.getView(R.id.tvDict);
            tvDict.setText(content);
            Drawable select = ContextCompat.getDrawable(context,
                    R.drawable.train_item_selected);
            select.setBounds(0, 0, select.getMinimumWidth(),
                    select.getMinimumHeight());
            if (selectItem == position) {
                tvDict.setPressed(true);
                tvDict.setCompoundDrawables(null, null, select, null);
                tvDict.setCompoundDrawablePadding(PixelFormat.formatPxToDip(context, 10));
            } else {
                tvDict.setPressed(false);
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
        LinearLayout.LayoutParams imgparms = new LinearLayout
                .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtils.getScreenHeight(context) / 7 * 2);
        add_picture.setLayoutParams(imgparms);
        iv_img.setLayoutParams(imgparms);
        initList();
    }

    private void initList() {
        atTypeList.add("跨校交流会");
        atTypeList.add("专家互动");
        atTypeList.add("创课观摩");
        atPartList.add("须报名预约，凭电子票入场");
        atPartList.add("在线报名，免费入场");
        atPartList.add("讲座视频录像+在线问答交流");
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
                if (indexPosition == 0) {
                    initContent();
                } else {
                    initContent2();
                }
            }
        });
        add_picture.setOnClickListener(context);
        ll_partUser.setOnClickListener(context);
        iv_img.setOnClickListener(context);
        ll_type.setOnClickListener(context);
        ll_beginTime.setOnClickListener(context);
        ll_endTime.setOnClickListener(context);
        ll_tv_partType.setOnClickListener(context);
        ll_partTime.setOnClickListener(context);
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    switch (view.getId()) {
                        case R.id.et_content:
                            scrollview.smoothScrollTo(0, (int) line_content.getY());
                            break;
                        case R.id.et_host:
                            scrollview.smoothScrollTo(0, (int) line_host.getY());
                            break;
                        case R.id.et_address:
                            scrollview.smoothScrollTo(0, (int) line_address.getY());
                            break;
                        case R.id.et_ticketNum:
                            scrollview.smoothScrollTo(0, (int) line_ticketNum.getY());
                            break;
                    }
                }
            }
        };
        et_address.setOnFocusChangeListener(onFocusChangeListener);
        et_host.setOnFocusChangeListener(onFocusChangeListener);
        et_content.setOnFocusChangeListener(onFocusChangeListener);
        et_ticketNum.setOnFocusChangeListener(onFocusChangeListener);
    }

    private StringBuilder builder3 = new StringBuilder();

    @Override
    public void onClick(View view) {
        Common.hideSoftInput(context);
        switch (view.getId()) {
            case R.id.add_picture:
                pickerPicture();
                break;
            case R.id.iv_img:
                pickerPicture();
                break;
            case R.id.ll_type:
                showPopupWindow();
                break;
            case R.id.ll_tv_partType:
                if (cl_parttype.getVisibility() == View.GONE) {
                    cl_parttype.setVisibility(View.VISIBLE);
                    partType();
                } else {
                    cl_parttype.setVisibility(View.GONE);
                }
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
                break;
            case R.id.ll_beginTime:
                builder1.setLength(0);
                index = 1;
                initTimePicker("选择活动开始时间");
                break;
            case R.id.ll_endTime:
                builder2.setLength(0);
                index = 2;
                initTimePicker("选择活动结束时间");
                break;
            case R.id.ll_partTime:
                builder3.setLength(0);
                index = 3;
                initTimePicker("选择报名截止时间");
                break;
            case R.id.ll_partUser:
                Intent intent = new Intent(context, TeachingResearchInvitedActivity.class);
                intent.putExtra("mobileUserList", (Serializable) userList);
                startActivityForResult(intent, 1);
                break;
        }
    }

    private int year1, month1, day1, year2, month2, day2, year3, month3, day3;
    private int hour1, minu1, hour2, minu2, hour3, minu3;
    private StringBuilder builder1 = new StringBuilder();
    private StringBuilder builder2 = new StringBuilder();

    public void initTimePicker(String title) {
        DateTimePickerDialog timePickerDialog = new DateTimePickerDialog(context, true);
        timePickerDialog.setTitle(title);
        timePickerDialog.setDateListener(new DateTimePickerDialog.DateListener() {
            @Override
            public void Date(int year, int monthOfYear, int dayOfMonth) {
                if (index == 1) {
                    year1 = year;
                    month1 = monthOfYear;
                    day1 = dayOfMonth;
                    builder1.append(year);
                    builder1.append("-");
                    if (monthOfYear < 10) {
                        builder1.append("0").append(monthOfYear);
                    } else {
                        builder1.append(monthOfYear);
                    }
                    builder1.append("-");
                    if (dayOfMonth < 10) {
                        builder1.append("0").append(dayOfMonth);
                    } else {
                        builder1.append(dayOfMonth);
                    }
                } else if (index == 2) {
                    year2 = year;
                    month2 = monthOfYear;
                    day2 = dayOfMonth;
                    builder2.append(year);
                    builder2.append("-");
                    if (monthOfYear < 10) {
                        builder2.append("0").append(monthOfYear);
                    } else {
                        builder2.append(monthOfYear);
                    }
                    builder2.append("-");
                    if (dayOfMonth < 10) {
                        builder2.append("0").append(dayOfMonth);
                    } else {
                        builder2.append(dayOfMonth);
                    }
                } else if (index == 3) {
                    year3 = year;
                    month3 = monthOfYear;
                    day3 = dayOfMonth;
                    builder3.append(year);
                    builder3.append("-");
                    if (monthOfYear < 10) {
                        builder3.append("0").append(monthOfYear);
                    } else {
                        builder3.append(monthOfYear);
                    }
                    builder3.append("-");
                    if (dayOfMonth < 10) {
                        builder3.append("0").append(dayOfMonth);
                    } else {
                        builder3.append(dayOfMonth);
                    }
                }
            }

            @Override
            public void Time(int hourOfDay, int minute) {
                if (index == 1) {
                    hour1 = hourOfDay;
                    minu1 = minute;
                    if (hourOfDay < 10) {
                        builder1.append(" 0").append(hourOfDay);
                    } else {
                        builder1.append(" ").append(hourOfDay);
                    }
                    builder1.append(":");
                    if (minute < 10) {
                        builder1.append(" 0").append(minute);
                    } else {
                        builder1.append(minute);
                    }
                    builder1.append(":00");
                    String time1 = builder1.toString();
                    if (time1 != null) {
                        tv_beginTime.setText(time1);
                    }

                } else if (index == 2) {
                    hour2 = hourOfDay;
                    minu2 = minute;
                    if (hourOfDay < 10) {
                        builder2.append(" 0").append(hourOfDay);
                    } else {
                        builder2.append(" ").append(hourOfDay);
                    }
                    builder2.append(":");
                    if (minute < 10) {
                        builder2.append("0").append(minute);
                    } else {
                        builder2.append(minute);
                    }
                    builder2.append(":00");
                    String time2 = builder2.toString();
                    if (time2 != null) {
                        tv_endTime.setText(time2);
                    }
                } else if (index == 3) {
                    hour3 = hourOfDay;
                    minu3 = minute;
                    if (hourOfDay < 10) {
                        builder3.append(" 0").append(hourOfDay);
                    } else {
                        builder3.append(" ").append(hourOfDay);
                    }
                    builder3.append(":");
                    if (minute < 10) {
                        builder3.append(" 0").append(minute);
                    } else {
                        builder3.append(minute);
                    }
                    builder3.append(":00");
                    String time3 = builder3.toString();
                    if (time3 != null) {
                        tv_partTime.setText(time3);
                    }

                }
            }
        });
        timePickerDialog.show();
    }

    private void initContent2() {
        //需报名预约，凭电子票入场
        String title = et_title.getText().toString().trim();
        String content = et_content.getText().toString().trim();
        String type = tv_atType.getText().toString().trim();
        String host = et_host.getText().toString().trim();
        String address = et_address.getText().toString().trim();
        String startTime = tv_beginTime.getText().toString().trim();
        String endTime = tv_endTime.getText().toString().trim();
        String partType = tv_partType.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            showToast("请输活动入标题");
        } else if (TextUtils.isEmpty(type)) {
            showToast("请选择活动类型");
        } else if (TextUtils.isEmpty(content)) {
            showToast("请输入活动入内容");
        } else if (TextUtils.isEmpty(host)) {
            showToast("请输入主办单位");
        } else if (TextUtils.isEmpty(address)) {
            showToast("请输入活动地点");
        } else if (TextUtils.isEmpty(startTime)) {
            showToast("请选择活动开始时间");
        } else if (TextUtils.isEmpty(endTime)) {
            showToast("请选择活动结束时间");
        } else if (TextUtils.isEmpty(partType)) {
            showToast("请选择活动参与方式");
        } else {
            checkTime2();
            if (timeOK) {
                try {
                    String timea = format.format(format.parse(startTime));
                    String timeb = format.format(format.parse(endTime));
                    commitActivity(title, activityType, content, host, address, timea, timeb, mPartType, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initContent() {
        //在线报名，讲座视频
        String title = et_title.getText().toString().trim();
        String content = et_content.getText().toString().trim();
        String type = tv_atType.getText().toString().trim();
        String host = et_host.getText().toString().trim();
        String address = et_address.getText().toString().trim();
        String startTime = tv_beginTime.getText().toString().trim();
        String endTime = tv_endTime.getText().toString().trim();
        String partType = tv_partType.getText().toString().trim();
        String ticketNum = et_ticketNum.getText().toString().trim();
        String endPartTime = tv_partTime.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            showToast("请输入活动标题");
        } else if (TextUtils.isEmpty(type)) {
            showToast("请选择活动类型");
        } else if (TextUtils.isEmpty(content)) {
            showToast("请输入活动内容");
        } else if (TextUtils.isEmpty(host)) {
            showToast("请输入主办单位");
        } else if (TextUtils.isEmpty(address)) {
            showToast("请输入活动地点");
        } else if (TextUtils.isEmpty(startTime)) {
            showToast("请选择活动开始时间");
        } else if (TextUtils.isEmpty(endTime)) {
            showToast("请选择活动结束时间");
        } else if (TextUtils.isEmpty(partType)) {
            showToast("请选择活动参与方式");
        } else if (TextUtils.isEmpty(endPartTime)) {
            showToast("请选择活动报名截止时间");
        } else if (TextUtils.isEmpty(ticketNum)) {
            showToast("请输入发票数量");
        } else {
            int tickNum = Integer.valueOf(ticketNum);
            if (tickNum >= 1 && tickNum < 10000) {
                checkTime();
                if (timeOK) {
                    try {
                        String timea = format.format(format.parse(startTime));
                        String timeb = format.format(format.parse(endTime));
                        String endpart = format.format(format.parse(endPartTime));
                        commitActivity(title, activityType, content, host, address, timea, timeb, mPartType, ticketNum, endpart);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                showToast("请输入正确的票数");
            }

        }
    }

    private boolean timeOK = false;

    private void checkTime() {
        Calendar calendar = Calendar.getInstance();
        int nowYear = calendar.get(Calendar.YEAR);
        int nowMonth = calendar.get(Calendar.MONTH) + 1;
        int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
        int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMin = calendar.get(Calendar.MINUTE);
        if (year2 < nowYear) {
            showTostNow();
        } else if (year2 == nowYear) {
            if (month2 < nowMonth) {
                showTostNow();
            } else if (month2 == nowMonth) {
                if (day2 < nowDay) {
                    showTostNow();
                } else if (day2 == nowDay) {
                    if (hour2 < hour1) {
                        showTostNow();
                    } else if (hour2 == nowHour) {
                        if (minu2 < nowMin) {
                            showTostNow();
                        } else {
                            timeOK = true;
                        }
                    } else {
                        timeOK = true;
                    }

                } else {
                    timeOK = true;
                }
            } else {
                timeOK = true;
            }
            endPart();
        } else if (year2 < year1) {
            showToast();
        } else if (year2 == year1) {
            if (month2 < month1) {
            } else if (month2 == month1) {
                if (day2 < day1) {
                    showToast();
                } else if (hour2 < hour1) {
                    showToast();
                } else if (hour2 == hour1) {
                    if (minu2 < minu1) {
                        showToast();
                    } else {
                        timeOK = true;
                    }
                } else {
                    timeOK = true;
                }

            } else {
                timeOK = true;
            }
            endPart();
        } else if (year2 > year1) {
            timeOK = true;
            endPart();
        }
    }

    //在线报名，讲座视频
    private void checkTime2() {
        Calendar calendar = Calendar.getInstance();
        int nowYear = calendar.get(Calendar.YEAR);
        int nowMonth = calendar.get(Calendar.MONTH) + 1;
        int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
        int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMin = calendar.get(Calendar.MINUTE);
        if (year2 < nowYear) {
            showTostNow();
        } else if (year2 == nowYear) {
            if (month2 < nowMonth) {
                showTostNow();
            } else if (month2 == nowMonth) {
                if (day2 < nowDay) {
                    showTostNow();
                } else if (day2 == nowDay) {
                    if (hour2 < hour1) {
                        showTostNow();
                    } else if (hour2 == nowHour) {
                        if (minu2 < nowMin) {
                            showTostNow();
                        } else {
                            timeOK = true;
                        }
                    } else {
                        timeOK = true;
                    }
                } else {
                    timeOK = true;
                }
            } else {
                timeOK = true;
            }
        } else if (year2 < year1) {
            showToast();
        } else if (year2 == year1) {
            if (month2 < month1) {
            } else if (month2 == month1) {
                if (day2 < day1) {
                    showToast();
                } else if (hour2 < hour1) {
                    showToast();
                } else if (hour2 == hour1) {
                    if (minu2 < minu1) {
                        showToast();
                    } else {
                        timeOK = true;
                    }
                } else {
                    timeOK = true;
                }

            } else {
                timeOK = true;
            }

        } else if (year2 > year1) {
            timeOK = true;
        }
    }

    private void endPart() {
        //活动报名截止日期
        if (year3 > year2) {
            timeOK = false;
            showEndMovement();
        } else if (year3 < year1) {
            timeOK = false;
            showStartMovement();
        } else if (year3 == year2) {
            if (month2 < month3) {
                timeOK = false;
                showEndMovement();
            } else if (month2 > month3) {
                timeOK = true;
            } else {
                if (day3 > day2) {
                    showEndMovement();
                    timeOK = false;
                } else if (day3 == day2) {
                    if (hour3 > hour2) {
                        showEndMovement();
                        timeOK = false;
                    } else if (hour3 == hour2) {
                        if (minu3 > minu2) {
                            showEndMovement();
                            timeOK = false;
                        } else {
                            timeOK = true;
                        }
                    } else {
                        timeOK = true;
                    }

                } else {
                    timeOK = true;
                }
            }
        }
    }

    private void showStartMovement() {
        tipDialog("报名日期不能小于活动开始日期");
    }

    private void showEndMovement() {
        tipDialog("报名日期要小于活动结束日期");
    }

    private void showToast() {
        tipDialog("结束日期不能小于开始日期");
    }

    private void showTostNow() {
        tipDialog("结束日期不能小于当前日期时间");
    }

    private void showToast(String message) {
        tipDialog(message);
    }

    private void tipDialog(String message) {
        MaterialDialog dialog = new MaterialDialog(context);
        dialog.setTitle("提示");
        dialog.setMessage(message);
        dialog.setPositiveTextColor(ContextCompat.getColor(context, R.color.defaultColor));
        dialog.setPositiveButton("我知道了", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void pickerPicture() {
        MediaOption option = new MediaOption.Builder()
                .setSelectType(MediaOption.TYPE_IMAGE)
                .setShowCamera(true)
                .build();
        MediaPicker.getInstance().init(option).selectMedia(context, new MediaPicker.onSelectMediaCallBack() {
            @Override
            public void onSelected(String path) {
                Glide.with(context).load(path).into(iv_img);
                if (path != null) {
                    add_picture.setVisibility(View.GONE);
                    iv_img.setVisibility(View.VISIBLE);
                }
                uploadFile = new File(path);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            List<MobileUser> mobileUserList = (List<MobileUser>) data.getSerializableExtra("mobileUserList");
            if (mobileUserList != null && mobileUserList.size() > 0) {
                userList.clear();
                userList.addAll(mobileUserList);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < userList.size(); i++) {
                    sb.append(userList.get(i).getRealName());
                    if (i < userList.size() - 1) {
                        sb.append(",");
                    }
                }
                tv_partUser.setText(sb.toString());
            }
        }
    }

    private void showPopupWindow() {
        Drawable shouqi = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_shouqi);
        shouqi.setBounds(0, 0, shouqi.getMinimumWidth(),
                shouqi.getMinimumHeight());
        final Drawable zhankai = ContextCompat.getDrawable(context,
                R.drawable.course_dictionary_xiala);
        zhankai.setBounds(0, 0, zhankai.getMinimumWidth(),
                zhankai.getMinimumHeight());
        tv_atType.setCompoundDrawables(null, null, shouqi, null);
        View view = View.inflate(context, R.layout.popupwindow_listview,
                null);
        ListView lv = view.findViewById(R.id.listView);
        StringListAdapter adapter = new StringListAdapter(context, atTypeList, atTypeSelect);
        lv.setAdapter(adapter);
        final PopupWindow popupWindow = new PopupWindow(view, ll_type.getWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                tv_atType.setCompoundDrawables(null, null, zhankai, null);
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                atTypeSelect = position;
                tv_atType.setText(atTypeList.get(position));
                if (position == 0) {
                    activityType = "communicationMeeting";
                } else if (position == 1) {
                    activityType = "expertInteraction";
                } else {
                    activityType = "lessonViewing";
                }
                popupWindow.dismiss();
            }
        });
        popupWindow.showAsDropDown(ll_type);
    }

    //参与方式
    private void partType() {
        final StringListAdapter adapter = new StringListAdapter(context, atPartList, atPartSelect);
        cl_parttype.setAdapter(adapter);
        cl_parttype.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                atPartSelect = position;
                tv_partType.setText(atPartList.get(position));
                indexPosition = position;
                if (position == 0) {
                    mPartType = "ticket";
                    ll_partType.setVisibility(View.VISIBLE);
                    ll_partUser.setVisibility(View.GONE);
                } else if (position == 1) {
                    mPartType = "free";
                    ll_partType.setVisibility(View.GONE);
                    ll_partUser.setVisibility(View.VISIBLE);
                } else {
                    mPartType = "chair";
                    ll_partType.setVisibility(View.GONE);
                    ll_partUser.setVisibility(View.VISIBLE);
                }
                cl_parttype.setVisibility(View.GONE);
            }
        });
    }

    /*提交信息到服务器，如果用户选择了活动封面，则先提交封面*/
    private void commitActivity(final String title, final String activityType,
                                final String content, final String sponsor, final String location,
                                final String startTime, final String endTime,
                                final String mPartType, final String ticketNum, final String endpart) {
        if (uploadFile != null && uploadFile.exists()) {
            String url = Constants.OUTRT_NET + "/m/file/uploadFileInfoRemote";
            final FileUploadDialog dialog = new FileUploadDialog(context, uploadFile.getName(), "正在提交");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            final Disposable disposable = OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<FileUploadResult>() {

                @Override
                public void onBefore(Request request) {
                    dialog.show();
                }

                @Override
                public void onError(Request request, Exception e) {
                    dialog.dismiss();
                }

                @Override
                public void onResponse(FileUploadResult response) {
                    dialog.dismiss();
                    if (response != null && response.getResponseData() != null) {
                        String RImgURL = response.getResponseData().getUrl();
                        upContent(RImgURL, title, activityType, content, sponsor, location, startTime, endTime, mPartType, ticketNum, endpart);
                    }
                }
            }, uploadFile, uploadFile.getName(), new OkHttpClientManager.ProgressListener() {
                @Override
                public void onProgress(long totalBytes, long remainingBytes, boolean done, File file) {
                    Flowable.just(new long[]{totalBytes, remainingBytes}).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<long[]>() {
                                @Override
                                public void accept(long[] params) throws Exception {
                                    dialog.setUploadProgressBar(params[0], params[1]);
                                    dialog.setUploadText(params[0], params[1]);
                                }
                            });
                }
            });
            dialog.setCancelListener(new FileUploadDialog.CancelListener() {
                @Override
                public void cancel() {
                    disposable.dispose();
                }
            });
        } else {
            upContent(null, title, activityType, content, sponsor, location, startTime, endTime, mPartType, ticketNum, endpart);
        }
    }

    private void upContent(String imageURL, String title, String type, String content, String sponsor,
                           String location, String startTime, String endTime, String partType,
                           String ticketNum, String endPartTime) {
        String url = Constants.OUTRT_NET + "/m/movement";
        Map<String, String> map = new HashMap<>();
        map.put("image", imageURL);
        map.put("title", title);
        map.put("type", type);
        map.put("content", content);
        map.put("sponsor", sponsor);
        map.put("location", location);
        map.put("movementRelations[0].startTime", startTime);
        map.put("movementRelations[0].endTime", endTime);
        map.put("participationType", partType);
        map.put("status", "verifying");
        if (indexPosition == 0) {
            map.put("movementRelations[0].registerEndTime", endPartTime);
            map.put("movementRelations[0].ticketNum", ticketNum);
        } else {
            if (userList.size() > 0) {
                for (int i = 0; i < userList.size(); i++) {
                    MobileUser mobileUser = userList.get(i);
                    if (mobileUser != null && mobileUser.getId() != null) {
                        map.put("movementRegisters[" + i + "].userInfo.id", mobileUser.getId());
                    }
                    map.put("movementRegisters[" + i + "].state", "passive");
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
                    toastFullScreen("发表成功", true);
                    MessageEvent event = new MessageEvent();
                    event.action = Action.CREATE_MOVEMENT;
                    RxBus.getDefault().post(event);
                    finish();
                } else {
                    toastFullScreen("发表失败", false);
                }
            }
        }, map);
    }
}
