package com.haoyu.app.activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.entity.MWorkShopActivityListResult;
import com.haoyu.app.entity.MWorkshopSection;
import com.haoyu.app.entity.WorkShopMobileEntity;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.entity.WorkShopSingleResult;
import com.haoyu.app.fragment.WSHomeFragment;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.SupportPopupWindow;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * 创建日期：2016/12/26 on 16:29
 * 描述:工作坊首页
 * 作者:马飞奔 Administrator
 */
public class WorkshopHomeActivity extends BaseActivity {
    private WorkshopHomeActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    private String workshopId;
    private int qualifiedPoint;  //工作坊达标积分

    @Override
    public int setLayoutResID() {
        return R.layout.activity_currency;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        String workshopTitle = getIntent().getStringExtra("workshopTitle");
        toolBar.setTitle_text(workshopTitle);
        toolBar.getIv_rightImage().setImageResource(R.drawable.workshop_menu);
        toolBar.setShow_right_button(false);
    }

    public void initData() {
        loadingView.setVisibility(View.VISIBLE);
        String url = Constants.OUTRT_NET + "/m/workshop/" + workshopId;
        addSubscription(Flowable.just(url).map(new Function<String, WorkShopSingleResult>() {
            @Override
            public WorkShopSingleResult apply(String url) throws Exception {
                return getResponse(url);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<WorkShopSingleResult>() {
            @Override
            public void accept(WorkShopSingleResult response) throws Exception {
                loadingView.setVisibility(View.GONE);
                updateUI(response);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }
        }));
    }

    private WorkShopSingleResult getResponse(String url) throws Exception {
        String responseStr = OkHttpClientManager.getAsString(context, url);
        Gson gson = new GsonBuilder().create();
        WorkShopSingleResult response = new GsonBuilder().create().fromJson(responseStr, WorkShopSingleResult.class);
        if (response != null && response.getResponseData() != null && response.getResponseData().getmWorkshopSections() != null) {
            for (int i = 0; i < response.getResponseData().getmWorkshopSections().size(); i++) {
                String atUrl = Constants.OUTRT_NET + "/m/activity/wsts/" + response.getResponseData().getmWorkshopSections().get(i).getId();
                String atStr = OkHttpClientManager.getAsString(context, atUrl);
                MWorkShopActivityListResult mActivityList = gson.fromJson(atStr, MWorkShopActivityListResult.class);
                if (mActivityList != null && mActivityList.getResponseData() != null) {
                    response.getResponseData().getmWorkshopSections().get(i).setActivities(mActivityList.getResponseData());
                }
            }
        }
        return response;
    }

    private void updateUI(WorkShopSingleResult response) {
        if (response != null && response.getResponseData() != null) {
            toolBar.setShow_right_button(true);
            WorkShopMobileEntity mWorkshop = response.getResponseData().getmWorkshop();
            if (mWorkshop != null) {
                qualifiedPoint = mWorkshop.getQualifiedPoint();
            }
            WorkShopMobileUser mWorkshopUser = response.getResponseData().getmWorkshopUser();
            List<MWorkshopSection> mWorkshopSections = response.getResponseData().getmWorkshopSections();
            FragmentManager fragmentManager = getSupportFragmentManager();
            WSHomeFragment fragment = new WSHomeFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("mWorkshop", mWorkshop);
            bundle.putSerializable("mWorkshopUser", mWorkshopUser);
            bundle.putSerializable("mWorkshopSections", (Serializable) mWorkshopSections);
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.container, fragment).commitAllowingStateLoss();
        } else {
            tv_empty.setText("没有相关信息~");
            tv_empty.setVisibility(View.VISIBLE);
        }
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
                showPopupWindow();
            }
        });
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
    }

    private void showPopupWindow() {
        final View popupView = getLayoutInflater().inflate(R.layout.popwindow_workshop_menu, null);
        final SupportPopupWindow pw = new SupportPopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT, true);
        View ll_notice = popupView.findViewById(R.id.ll_notice);
        View ll_introduct = popupView.findViewById(R.id.ll_introduct);
        //研修简报
        View ll_brief = popupView.findViewById(R.id.ll_brief);
        //学员测试
        View ll_studen_test = popupView.findViewById(R.id.ll_studen_test);
        //成员考核
        View ll_member_management = popupView.findViewById(R.id.ll_member_management);
        ll_notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
                Intent intent = new Intent(context, AnnouncementActivity.class);
                intent.putExtra("relationId", workshopId);
                intent.putExtra("relationType", "workshop");
                intent.putExtra("type", "workshop_announcement");
                startActivity(intent);
            }
        });
        ll_introduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
                Intent intent = new Intent(context, WorkShopDetailActivity.class);
                intent.putExtra("workshopId", workshopId);
                startActivity(intent);
            }
        });
        ll_brief.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
                Intent intent = new Intent(context, BriefingActivity.class);
                intent.putExtra("relationId", workshopId);
                intent.putExtra("relationType", "workshop");
                intent.putExtra("type", "workshop_briefing");
                startActivity(intent);

            }
        });
        ll_studen_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
                Intent intent = new Intent(context, WSExamineActivity.class);
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("qualifiedPoint", qualifiedPoint);
                startActivity(intent);
            }
        });
        ll_member_management.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
                Intent intent = new Intent(context, ManagentMemberActivity.class);
                intent.putExtra("workshopId", workshopId);
                startActivity(intent);
            }
        });
        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pw.dismiss();
            }
        });
        pw.setTouchable(true);
        pw.setBackgroundDrawable(new BitmapDrawable());
        pw.setOutsideTouchable(true);
        View view = toolBar.getIv_rightImage();
        pw.showAsDropDown(view, 0, -10);
    }
}
