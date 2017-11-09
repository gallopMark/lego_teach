package com.haoyu.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.haoyu.app.adapter.TeacherCourseListAdapter;
import com.haoyu.app.adapter.TeacherWorkShopListAdater;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.CaptureResult;
import com.haoyu.app.entity.CourseMobileEntity;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.TeacherHomePageResult;
import com.haoyu.app.entity.UserInfoResult;
import com.haoyu.app.entity.WorkShopMobileEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

/**
 * 创建日期：2017/2/4 on 14:57
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    private MainActivity context = this;
    @BindView(R.id.toggle)
    ImageView toggle;
    @BindView(R.id.iv_msg)
    ImageView iv_msg;
    @BindView(R.id.iv_scan)
    View iv_scan;
    private SlidingMenu menu;
    private ImageView iv_userIco;   //侧滑菜单用户头像
    private TextView tv_userName;   //侧滑菜单用户名
    private TextView tv_deptName;   //侧滑菜单用户部门名称
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.empty_data)
    TextView empty_data;
    @BindView(R.id.contentView)
    View contentView;
    private List<CourseMobileEntity> mCourses = new ArrayList<>();
    private List<WorkShopMobileEntity> mWorkshops = new ArrayList<>();
    private TeacherCourseListAdapter courseListAdapter;
    private TeacherWorkShopListAdater workShopListAdater;
    @BindView(R.id.empty_course)
    TextView empty_course;
    @BindView(R.id.empty_workshop)
    TextView empty_workshop;
    @BindView(R.id.courseRV)
    RecyclerView courseRV;
    @BindView(R.id.workshopRV)
    RecyclerView workshopRV;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        menu = new SlidingMenu(context);
        menu.setMode(SlidingMenu.LEFT);
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        View menuView = LayoutInflater.from(context).inflate(R.layout.app_homepage_menu, null);
        initMenuView(menuView);
        menu.setMenu(menuView);
        FullyLinearLayoutManager courseManager = new FullyLinearLayoutManager(context);
        courseManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        courseRV.setLayoutManager(courseManager);
        FullyLinearLayoutManager workshopManager = new FullyLinearLayoutManager(context);
        workshopManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
        workshopRV.setLayoutManager(workshopManager);
        courseListAdapter = new TeacherCourseListAdapter(context, mCourses);
        courseRV.setAdapter(courseListAdapter);
        workShopListAdater = new TeacherWorkShopListAdater(context, mWorkshops);
        workshopRV.setAdapter(workShopListAdater);
        courseRV.setNestedScrollingEnabled(false);
        workshopRV.setNestedScrollingEnabled(false);
        registRxBus();
    }

    private void initMenuView(View menuView) {
        View ll_userInfo = menuView.findViewById(R.id.ll_userInfo);
        ll_userInfo.setOnClickListener(context);
        iv_userIco = menuView.findViewById(R.id.iv_userIco);
        GlideImgManager.loadCircleImage(context, getAvatar()
                , R.drawable.user_default, R.drawable.user_default, iv_userIco);
        iv_userIco.setOnClickListener(context);
        tv_userName = menuView.findViewById(R.id.tv_userName);
        tv_deptName = menuView.findViewById(R.id.tv_deptName);
        if (TextUtils.isEmpty(getRealName()))
            tv_userName.setText("请填写用户名");
        else
            tv_userName.setText(getRealName());
        if (TextUtils.isEmpty(getDeptName()))
            tv_deptName.setText("请选择单位");
        else
            tv_deptName.setText(getDeptName());
        TextView tv_education = menuView.findViewById(R.id.tv_education);
        tv_education.setOnClickListener(context);
        TextView tv_teaching = menuView.findViewById(R.id.tv_teaching);
        tv_teaching.setOnClickListener(context);
        TextView tv_message = menuView.findViewById(R.id.tv_message);
        tv_message.setOnClickListener(context);
        TextView tv_consulting = menuView.findViewById(R.id.tv_consulting);
        tv_consulting.setOnClickListener(context);
        TextView tv_settings = menuView.findViewById(R.id.tv_settings);
        tv_settings.setOnClickListener(context);
        getUserInfo();
    }

    private void getUserInfo() {
        String url = Constants.OUTRT_NET + "/m/user/" + getUserId();
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<UserInfoResult>() {

            @Override
            public void onError(Request request, Exception e) {
            }

            @Override
            public void onResponse(UserInfoResult response) {
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                }
            }
        }));
    }

    private void updateUI(MobileUser user) {
        GlideImgManager.loadCircleImage(context.getApplicationContext(), user.getAvatar(), R.drawable.user_default,
                R.drawable.user_default, iv_userIco);
        if (TextUtils.isEmpty(user.getRealName()))
            tv_userName.setText("请填写用户名");
        else
            tv_userName.setText(user.getRealName());
        if (user.getmDepartment() != null && user.getmDepartment().getDeptName() != null)
            tv_deptName.setText(user.getmDepartment().getDeptName());
        else
            tv_deptName.setText("请选择单位");
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/uc/teachIndex";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<TeacherHomePageResult>() {
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
            public void onResponse(TeacherHomePageResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    contentView.setVisibility(View.VISIBLE);
                    updateUI(response.getResponseData());
                } else {
                    empty_data.setVisibility(View.VISIBLE);
                }
            }
        }));
    }

    private void updateUI(TeacherHomePageResult.TeacherHomePageData responseData) {
        if (responseData.getmCourses() != null && responseData.getmCourses().size() > 0) {
            courseRV.setVisibility(View.VISIBLE);
            mCourses.addAll(responseData.getmCourses());
            courseListAdapter.notifyDataSetChanged();
        } else {
            empty_course.setVisibility(View.VISIBLE);
        }
        if (responseData.getmWorkshops() != null && responseData.getmWorkshops().size() > 0) {
            workshopRV.setVisibility(View.VISIBLE);
            mWorkshops.addAll(responseData.getmWorkshops());
            workShopListAdater.notifyDataSetChanged();
        } else {
            empty_workshop.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setListener() {
        iv_scan.setOnClickListener(context);
        iv_msg.setOnClickListener(context);
        toggle.setOnClickListener(context);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        courseListAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                CourseMobileEntity entity = mCourses.get(position);
                if (entity.getmTimePeriod() != null && entity.getmTimePeriod().getState() != null && entity.getmTimePeriod().getState().equals("未开始")) {
                    showMaterialDialog("温馨提示", "课程尚未开放");
                    return;
                }
                String courseId = entity.getId();
                String courseTitle = entity.getTitle();
                Intent intent = new Intent(context, TeacherCourseTabActivity.class);
                intent.putExtra("courseId", courseId);
                intent.putExtra("courseTitle", courseTitle);
                startActivity(intent);
            }
        });

        workShopListAdater.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                WorkShopMobileEntity entity = mWorkshops.get(position);
                String workshopId = entity.getId();
                String workshopTitle = entity.getTitle();
                Intent intent = new Intent(context, WorkshopHomePageActivity.class);
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("workshopTitle", workshopTitle);
                startActivity(intent);
            }
        });
    }

    private final static int SCANNIN_GREQUEST_CODE = 1;

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.iv_scan:
                intent.setClass(context, AppCaptureActivity.class);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
                break;
            case R.id.iv_msg:

                break;
            case R.id.toggle:
                menu.toggle(true);
                break;
            case R.id.ll_userInfo:  //侧滑菜单个人信息
                startActivity(new Intent(context, AppUserInfoActivity.class));
                break;
            case R.id.iv_userIco:
                if (getAvatar() != null && getAvatar().length() > 0) {
                    intent.setClass(context, AppMultiImageShowActivity.class);
                    ArrayList<String> imgList = new ArrayList<>();
                    imgList.add(getAvatar());
                    intent.putStringArrayListExtra("photos", imgList);
                    intent.putExtra("position", 0);
                    intent.putExtra("isUser", true);
                    startActivity(intent);
                    overridePendingTransition(R.anim.zoom_in, 0);
                }
                break;
            case R.id.tv_education:  //侧滑菜单教学
                menu.toggle(true);
                break;
            case R.id.tv_teaching:  //侧滑菜单教研
                startActivity(new Intent(context, TeachingResearchActivity.class));
                break;
            case R.id.tv_message:  //侧滑菜单消息
                intent.setClass(context, MessageActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_consulting:  //侧滑菜单教务咨询
                startActivity(new Intent(context, EducationConsultActivity.class));
                break;
            case R.id.tv_settings:  //侧滑菜单设置
                intent.setClass(context, SettingActivity.class);
                startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == SCANNIN_GREQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    parseCaptureResult(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    toast(context, "解析二维码失败");
                }
            }
        }
    }

    private void parseCaptureResult(String result) {
        try {
            Gson gson = new Gson();
            CaptureResult mCaptureResult = gson.fromJson(result, CaptureResult.class);
            String qtId = mCaptureResult.getQtId();
            String service = mCaptureResult.getService();
            String url = Constants.LOGIN_URL;
            login(url, qtId, service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void login(final String url, final String qtId, final String service) {
        showLoadingDialog("登录验证");
        Flowable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return OkHttpClientManager.getInstance().scanLogin(context, url, qtId, service);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isSuccessful) throws Exception {
                        hideLoadingDialog();
                        if (isSuccessful) {
                            toast(context, "验证成功");
                        } else {
                            toast(context, "验证失败");
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        hideLoadingDialog();
                        toast(context, "验证失败");
                    }
                });
    }

    private long mExitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && menu.isMenuShowing()) {
            menu.toggle(true);
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && !menu.isMenuShowing()) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                toast(this, "再按一次退出" + getResources().getString(R.string.app_name));
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.CHANGE_USER_ICO) && event.obj != null && event.obj instanceof String) {
            String avatar = (String) event.obj;
            GlideImgManager.loadCircleImage(context, avatar, R.drawable.user_default, R.drawable.user_default, iv_userIco);
        } else if (event.action.equals(Action.CHANGE_USER_NAME) && event.obj != null && event.obj instanceof String) {
            String realName = (String) event.obj;
            tv_userName.setText(realName);
        } else if (event.action.equals(Action.CHANGE_DEPT_NAME) && event.obj != null && event.obj instanceof String) {
            String deptName = (String) event.obj;
            tv_deptName.setText(deptName);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribe();
    }

}
