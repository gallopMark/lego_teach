package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.WorkShopSectionAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.dialog.DatePickerDialog;
import com.haoyu.app.dialog.MaterialDialog;
import com.haoyu.app.entity.AppActivityViewEntity;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.MWorkShopActivityListResult;
import com.haoyu.app.entity.MWorkshopActivity;
import com.haoyu.app.entity.MWorkshopSection;
import com.haoyu.app.entity.TimePeriod;
import com.haoyu.app.entity.VideoMobileEntity;
import com.haoyu.app.entity.WorkShopSingleResult;
import com.haoyu.app.entity.WorkshopPhaseResult;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.NetStatusUtil;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.ColorArcProgressBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.view.StickyScrollView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;


/**
 * 创建日期：2016/12/26 on 16:29
 * 描述:工作坊首页
 * 作者:马飞奔 Administrator
 */
public class WorkshopHomePageActivity extends BaseActivity implements View.OnClickListener {
    private WorkshopHomePageActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.loadingView)
    LoadingView loadingView;
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.contentView)
    StickyScrollView contentView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private WorkShopSectionAdapter mAdapter;
    private List<MWorkshopSection> sectionList = new ArrayList<>();
    @BindView(R.id.progressBar1)
    ColorArcProgressBar progressBar1;
    @BindView(R.id.progressBar2)
    ColorArcProgressBar progressBar2;
    @BindView(R.id.tv_day)
    TextView tv_day;
    @BindView(R.id.tv_carryOut)
    TextView tv_carryOut;
    @BindView(R.id.tv_allDay)
    TextView tv_allDay;
    @BindView(R.id.tv_score)
    TextView tv_score;
    @BindView(R.id.qualifiedPoint)
    TextView qualifiedPoint;
    @BindView(R.id.ll_question)
    LinearLayout ll_question;
    @BindView(R.id.ll_exchange)
    LinearLayout ll_exchange;
    private String workshopId, role;
    @BindView(R.id.task_add_phase_btn)
    View task_add_phase_btn;
    @BindView(R.id.ll_empty)
    LinearLayout ll_empty;//没有数据时显示
    private int alterPosition, activityIndex;
    private final int REQUEST_ACTIVITY = 11;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_workshop_homepage;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        String workshopTitle = getIntent().getStringExtra("workshopTitle");
        toolBar.setTitle_text(workshopTitle);
        ((SimpleItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new WorkShopSectionAdapter(context, sectionList);
        recyclerView.setAdapter(mAdapter);
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
                if (response != null && response.getResponseData() != null) {
                    updateUI(response.getResponseData());
                    task_add_phase_btn.setVisibility(View.VISIBLE);
                }
                if (response != null && response.getResponseData() != null
                        && response.getResponseData().getmWorkshopUser() != null
                        && response.getResponseData().getmWorkshopUser().getRole() != null)
                    role = response.getResponseData().getmWorkshopUser().getRole();
                if (response != null && response.getResponseData() != null && response.getResponseData().getmWorkshopSections() != null
                        && response.getResponseData().getmWorkshopSections().size() > 0) {
                    updateUI(response.getResponseData().getmWorkshopSections());
                } else {
                    recyclerView.setVisibility(View.GONE);
                    ll_empty.setVisibility(View.VISIBLE);
                }
                contentView.setVisibility(View.VISIBLE);
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

    private void updateUI(WorkShopSingleResult.WorkShopSingleResponseData responseData) {
        if (responseData.getmWorkshop() != null) {
            if (responseData.getmWorkshop().getmTimePeriod() != null && responseData.getmWorkshop().getmTimePeriod().getMinutes() > 0) {
                TimePeriod timePeriod = responseData.getmWorkshop().getmTimePeriod();
                tv_day.setVisibility(View.VISIBLE);
                tv_allDay.setVisibility(View.VISIBLE);
                int remainDay = (int) (timePeriod.getMinutes() / 60 / 24);
                long startTime = timePeriod.getStartTime();
                long endTime = timePeriod.getEndTime();
                int allDay = getAllDay(startTime, endTime);
                int expandDay = allDay - remainDay;
                tv_day.setText(String.valueOf(expandDay));
                tv_carryOut.setText("已开展");
                tv_allDay.setText("共" + allDay + "天");
                progressBar1.setMaxValues(allDay);
                progressBar1.setCurrentValues(expandDay);
            } else {
                if (responseData.getmWorkshop().getmTimePeriod() != null && responseData.getmWorkshop().getmTimePeriod().getState() != null)
                    tv_carryOut.setText("工作坊研修\n" + responseData.getmWorkshop().getmTimePeriod().getState());
                else
                    tv_carryOut.setText("工作坊研修\n进行中");
            }
        } else {
            tv_day.setVisibility(View.GONE);
            tv_allDay.setVisibility(View.GONE);
            tv_carryOut.setText("工作坊研修\n进行中");
        }
        int qualityPoint = 0, point = 0;
        if (responseData.getmWorkshop() != null) {
            qualityPoint = responseData.getmWorkshop().getQualifiedPoint();
        }
        if (responseData.getmWorkshopUser() != null) {
            point = (int) responseData.getmWorkshopUser().getPoint();
        }
        progressBar2.setMaxValues(qualityPoint);
        progressBar2.setCurrentValues(point);
        tv_score.setText(String.valueOf(point));
        qualifiedPoint.setText("（达标分数" + qualityPoint + "）");
    }

    private int getAllDay(long startTime, long endTime) {
        long interval = (endTime - startTime) / 1000;
        int day = interval / 24 * 60 * 60 == 0 ? 1 : (int) (interval / (24 * 60 * 60));
        if (day <= 0) {
            return 0;
        }
        return day;
    }

    private void updateUI(List<MWorkshopSection> sections) {
        recyclerView.setVisibility(View.VISIBLE);
        mAdapter.addAll(sections);
        mAdapter.notifyDataSetChanged();
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
        ll_question.setOnClickListener(context);
        ll_exchange.setOnClickListener(context);
        task_add_phase_btn.setOnClickListener(context);

        mAdapter.setOnSectionLongClickListener(new WorkShopSectionAdapter.OnSectionLongClickListener() {
            @Override
            public void onLongClickListener(String taskId, int position, MWorkshopSection entity) {
                showTaskEditDialog(taskId, position, entity);
            }
        });
        mAdapter.setItemCallBack(new WorkShopSectionAdapter.ActivityItemCallBack() {
            @Override
            public void itemCallBack(final MWorkshopActivity activity, final int position) {
                  /*进入活动*/
                String url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/activity/wsts/" + activity.getId() + "/view";
                addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<AppActivityViewResult>() {
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
                    public void onResponse(AppActivityViewResult response) {
                        hideTipDialog();
                        EnterActivity(response);
                    }
                }));
            }

            @Override
            public void onDelete(int mainIndex, int position) {
                deleteActivity(mainIndex, position);
            }
        });

        mAdapter.setAddTaskListener(new WorkShopSectionAdapter.OnAddTaskListener() {
            private String startTime, endTime;

            @Override
            public void inputTitle(final TextView task_title) {
                CommentDialog dialog = new CommentDialog(context, "输出阶段标题", "完成");
                dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
                    @Override
                    public void sendComment(String content) {
                        task_title.setText(content);
                    }
                });
                dialog.show();
            }

            @Override
            public void inputTime(final TextView tv_researchTime) {
                DatePickerDialog pickerDialog = new DatePickerDialog(context, true);
                pickerDialog.setDatePickerListener(new DatePickerDialog.OnDatePickerListener() {
                    @Override
                    public void datePicker(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
                        startTime = startYear + "-" + (startMonth < 10 ? "0" + startMonth : startMonth);
                        endTime = endYear + "-" + (endMonth < 10 ? "0" + endMonth : endMonth);
                        String mStartTime = startYear + "年" + (startMonth < 10 ? "0" + startMonth : startMonth) + "月";
                        String mEndTime = endYear + "年" + (endMonth < 10 ? "0" + endMonth : endMonth) + "月";
                        tv_researchTime.setText(mStartTime + "\u3000-\u3000" + mEndTime);
                    }
                });
                pickerDialog.show();
            }

            @Override
            public void addTask(TextView task_title, TextView tv_researchTime, int sortNum) {
                String title = task_title.getText().toString().trim();
                String time = tv_researchTime.getText().toString().trim();
                if (title.length() == 0) {
                    toast(context, "请输入阶段标题");
                } else if (time.length() == 0) {
                    toast(context, "请选择研修时间");
                } else {
                    addStage(title, startTime, endTime, sortNum);
                    task_title.setText(null);
                    tv_researchTime.setText(null);
                    mAdapter.setAddTask(false);
                    task_add_phase_btn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void cancel() {
                task_add_phase_btn.setVisibility(View.VISIBLE);
            }
        });

        mAdapter.setAddActivityCallBack(new WorkShopSectionAdapter.AddActivityCallBack() {
            @Override
            public void addActivity(int type, String workSectionId, int position) {
                activityIndex = position;
                Intent intent = new Intent();
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("workSectionId", workSectionId);
                if (type == 1) {
                    intent.setClass(context, WSTeachingDiscussActivity.class);
                } else if (type == 2) {
                    intent.setClass(context, WSTeachingEmulateActivity.class);
                } else {
                    intent.setClass(context, WSTeachingStudyEditActivity.class);
                }
                startActivityForResult(intent, REQUEST_ACTIVITY);
            }
        });
    }

    private void showTaskEditDialog(final String taskId, final int position, final MWorkshopSection entity) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_workshop_task, null);
        TextView tv_addTask = view.findViewById(R.id.tv_addTask);
        TextView tv_alterTask = view.findViewById(R.id.tv_alterTask);
        TextView tv_deleteTask = view.findViewById(R.id.tv_deleteTask);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.tv_addTask:
                        smoothToBottom();
                        break;
                    case R.id.tv_alterTask:
                        alterPosition = position;
                        Intent intent = new Intent(context, WorkShopEditTaskActivity.class);
                        intent.putExtra("title", entity.getTitle());
                        if (entity.getTimePeriod() != null) {
                            intent.putExtra("startTime", entity.getTimePeriod().getStartTime());
                            intent.putExtra("endTime", entity.getTimePeriod().getEndTime());
                        }
                        intent.putExtra("workShopId", workshopId);
                        intent.putExtra("relationId", taskId);
                        startActivityForResult(intent, 200);
                        break;
                    case R.id.tv_deleteTask:
                        deleteTask(taskId, position);
                        break;
                    case R.id.tv_cancel:
                        break;
                }
                dialog.dismiss();
            }
        };
        tv_addTask.setOnClickListener(listener);
        tv_alterTask.setOnClickListener(listener);
        tv_deleteTask.setOnClickListener(listener);
        tv_cancel.setOnClickListener(listener);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
        dialog.getWindow().setLayout(ScreenUtils.getScreenWidth(context), LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setWindowAnimations(R.style.dialog_anim);
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 200) {
            String title = data.getStringExtra("title");
            TimePeriod timePeriod = (TimePeriod) data.getSerializableExtra("timePeriod");
            sectionList.get(alterPosition).setTitle(title);
            sectionList.get(alterPosition).setTimePeriod(timePeriod);
            mAdapter.notifyItemChanged(alterPosition);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_ACTIVITY) {
            if (data != null && data.getSerializableExtra("activity") != null && data.getSerializableExtra("activity") instanceof MWorkshopActivity) {
                MWorkshopActivity mWorkshopActivity = (MWorkshopActivity) data.getSerializableExtra("activity");
                sectionList.get(activityIndex).getActivities().add(mWorkshopActivity);
                mAdapter.notifyItemChanged(activityIndex);
            }
        }
    }

    private void EnterActivity(AppActivityViewResult response) {
        if (response != null && response.getResponseData() != null
                && response.getResponseData().getmActivityResult() != null
                && response.getResponseData().getmActivityResult().getmActivity() != null) {
            CourseSectionActivity activity = response.getResponseData().getmActivityResult().getmActivity();
            if (activity.getType() != null && activity.getType().equals("lesson_plan")) {   //集体备课
                toast(context, "系统暂不支持浏览，请到网站完成。");
            } else if (activity.getType() != null && activity.getType().equals("discussion")) {  //教学研讨
                openDiscussion(response, activity);
            } else if (activity.getType() != null && activity.getType().equals("survey")) {  //问卷调查
                openSurvey(response, activity);
            } else if (activity.getType() != null && activity.getType().equals("debate")) {  //在线辩论
                toast(context, "系统暂不支持浏览，请到网站完成。");
            } else if (activity.getType() != null && activity.getType().equals("test")) {  //教学测验
                openTest(response, activity);
            } else if (activity.getType() != null && activity.getType().equals("video")) {  //视频
                if (NetStatusUtil.isConnected(context)) {
                    if (NetStatusUtil.isWifi(context))
                        playVideo(response, activity);
                    else {
                        showNetDialog(response, activity);
                    }
                } else {
                    toast(context, "当前网络不稳定，请检查网络设置！");
                }
            } else if (activity.getType() != null && activity.getType().equals("lcec")) {
                openLcec(response, activity);
            } else {
                toast(context, "系统暂不支持浏览，请到网站完成。");
            }
        }
    }

    private void showNetDialog(final AppActivityViewResult response, final CourseSectionActivity activity) {
        MaterialDialog mainDialog = new MaterialDialog(context);
        mainDialog.setTitle("网络提醒");
        mainDialog.setMessage("使用2G/3G/4G网络观看视频会消耗较多流量。确定要开启吗？");
        mainDialog.setNegativeButton("开启", new MaterialDialog.ButtonClickListener() {
            @Override
            public void onClick(View v, AlertDialog dialog) {
                playVideo(response, activity);
                dialog.dismiss();
            }
        });
        mainDialog.setPositiveButton("取消", null);
        mainDialog.show();
    }

    /*播放视频*/
    private void playVideo(AppActivityViewResult response, CourseSectionActivity activity) {
        if (response.getResponseData() != null && response.getResponseData().getmVideoUser() != null) {  //教学视频
            AppActivityViewEntity.VideoUserMobileEntity videoEntity = response.getResponseData().getmVideoUser();
            VideoMobileEntity video = videoEntity.getmVideo();
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            if (activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
                intent.putExtra("running", true);
            else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
                intent.putExtra("running", true);
            else
                intent.putExtra("running", false);
            if (video != null && video.getVideoFiles() != null && video.getVideoFiles().size() > 0) {
                intent.putExtra("summary", videoEntity.getmVideo().getSummary());
                intent.putExtra("videoUrl", video.getVideoFiles().get(0).getUrl());
                intent.putExtra("fileName", video.getVideoFiles().get(0).getFileName());
                intent.putExtra("videoId", video.getVideoFiles().get(0).getId());
                intent.putExtra("activityId", activity.getId());
                intent.putExtra("attach", video);
                startActivity(intent);
            } else if (video != null && video.getUrls() != null) {
                intent.putExtra("activityId", activity.getId());
                intent.putExtra("activityTitle", activity.getTitle());
                intent.putExtra("summary", videoEntity.getmVideo().getSummary());
                intent.putExtra("videoUrl", video.getUrls());
                intent.putExtra("attach", video);
                startActivity(intent);
            } else if (video != null && video.getAttchFiles() != null && video.getAttchFiles().size() > 0) {
                //教学观摩
                intent.putExtra("activityId", activity.getId());
                intent.putExtra("activityTitle", activity.getTitle());
                intent.putExtra("summary", videoEntity.getmVideo().getSummary());
                intent.putExtra("videoUrl", video.getAttchFiles().get(0).getUrl());
                intent.putExtra("attach", video);
                intent.putExtra("fileName", video.getAttchFiles().get(0).getFileName());
                startActivity(intent);
                startActivity(intent);
            } else {
                toast(context, "系统暂不支持浏览，请到网站完成。");
            }
        }
    }

    /*打开听课评课*/
    private void openLcec(AppActivityViewResult response, CourseSectionActivity activity) {
        if (response.getResponseData() != null && response.getResponseData().getmLcec() != null) {
            Intent intent = new Intent(context, TeachingStudyActivity.class);
            if (activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
                intent.putExtra("running", true);
            else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
                intent.putExtra("running", true);
            else
                intent.putExtra("running", false);
            intent.putExtra("timePeriod", activity.getmTimePeriod());
            intent.putExtra("workshopId", workshopId);
            intent.putExtra("activityId", activity.getId());
            intent.putExtra("activityTitle", activity.getTitle());
            intent.putExtra("mlcec", response.getResponseData().getmLcec());
            startActivity(intent);
        } else {
            toast(context, "系统暂不支持浏览，请到网站完成。");
        }
    }

    /*打开课程研讨*/
    private void openDiscussion(AppActivityViewResult response, CourseSectionActivity activity) {
        if (response.getResponseData() != null && response.getResponseData().getmDiscussionUser() != null) {
            Intent intent = new Intent(context, TeachingDiscussionActivity.class);
            if (activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
                intent.putExtra("running", true);
            else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
                intent.putExtra("running", true);
            else
                intent.putExtra("running", false);
            intent.putExtra("discussType", "workshop");
            intent.putExtra("workshopId", workshopId);
            intent.putExtra("activityId", activity.getId());
            intent.putExtra("activityTitle", activity.getTitle());
            intent.putExtra("timePeriod", activity.getmTimePeriod());
            intent.putExtra("discussUser", response.getResponseData().getmDiscussionUser());
            intent.putExtra("mainNum", response.getResponseData().getmDiscussionUser().getMainPostNum());
            intent.putExtra("subNum", response.getResponseData().getmDiscussionUser().getSubPostNum());
            if (response.getResponseData().getmDiscussionUser().getmDiscussion() != null) {
                DiscussEntity entity = response.getResponseData().getmDiscussionUser().getmDiscussion();
                intent.putExtra("needMainNum", entity.getMainPostNum());
                intent.putExtra("needSubNum", entity.getSubPostNum());
            }
            startActivity(intent);
        } else
            toast(context, "系统暂不支持浏览，请到网站完成。");
    }

    /*打开问卷调查*/
    private void openSurvey(AppActivityViewResult response, CourseSectionActivity activity) {
        Intent intent = new Intent(context, AppSurveyHomeActivity.class);
        if (activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
            intent.putExtra("running", true);
        else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
            intent.putExtra("running", true);
        else
            intent.putExtra("running", false);
        intent.putExtra("relationId", workshopId);
        intent.putExtra("type", "workshop");
        intent.putExtra("timePeriod", activity.getmTimePeriod());
        if (response.getResponseData() != null && response.getResponseData().getmSurveyUser() != null) {
            intent.putExtra("surveyUser", response.getResponseData().getmSurveyUser());
        }
        intent.putExtra("activityId", activity.getId());
        intent.putExtra("activityTitle", activity.getTitle());
        startActivity(intent);
    }

    /*打开测验*/
    private void openTest(AppActivityViewResult response, CourseSectionActivity activity) {
        Intent intent = new Intent();
        if (activity.getmTimePeriod() != null && activity.getmTimePeriod().getState() != null && activity.getmTimePeriod().getState().equals("进行中"))
            intent.putExtra("running", true);
        else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().getMinutes() > 0)
            intent.putExtra("running", true);
        else
            intent.putExtra("running", false);
        intent.putExtra("relationId", workshopId);
        intent.putExtra("testType", "workshop");
        intent.putExtra("timePeriod", activity.getmTimePeriod());
        intent.putExtra("activityId", activity.getId());
        intent.putExtra("activityTitle", activity.getTitle());
        if (response.getResponseData() != null && response.getResponseData().getmTestUser() != null) {
            intent.putExtra("testUser", response.getResponseData().getmTestUser());
        }
        if (response.getResponseData() != null && response.getResponseData().getmTestUser() != null
                && response.getResponseData().getmTestUser().getCompletionStatus() != null
                && response.getResponseData().getmTestUser().getCompletionStatus().equals("completed")) {
            if (response.getResponseData().getmActivityResult() != null) {
                intent.putExtra("score", response.getResponseData().getmActivityResult().getScore());
            }
            intent.setClass(context, AppTestResultActivity.class);
        } else {
            intent.setClass(context, AppTestHomeActivity.class);
        }
        startActivity(intent);
    }

    //删除活动
    private void deleteActivity(final int mainIndex, final int position) {
        String activityId = sectionList.get(mainIndex).getActivities().get(position).getId();
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + getUserId() + "/m/activity/wsts/" + activityId;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
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
                    sectionList.get(mainIndex).getActivities().remove(position);
                    mAdapter.setPressIndex(mainIndex, position);
                    mAdapter.notifyItemChanged(mainIndex);
                } else {
                    toast(context, "删除失败，请稍后再试");
                }
            }
        }, map));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_question:
                Intent intent = new Intent(context, WorkshopQuestionActivity.class);
                intent.putExtra("relationId", workshopId);
                startActivity(intent);
                break;
            case R.id.ll_exchange:
                Intent intent2 = new Intent(context, FreeChatActiviy.class);
                intent2.putExtra("relationId", workshopId);
                intent2.putExtra("role", role);
                startActivity(intent2);
                break;
            case R.id.task_add_phase_btn:
                smoothToBottom();
                break;
        }
    }

    private void smoothToBottom() {
        mAdapter.setAddTask(true);
        addSubscription(Flowable.just(context).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<WorkshopHomePageActivity>() {
            @Override
            public void accept(WorkshopHomePageActivity activity) throws Exception {
                contentView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }));
        task_add_phase_btn.setVisibility(View.GONE);
    }

    //添加新阶段
    private void addStage(String title, String startTime, String endTime, int sortNum) {
        showTipDialog();
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + context.getUserId() + "/m/workshop_section";
        Map<String, String> map = new HashMap<>();
        map.put("workshopId", workshopId);
        map.put("title", title);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("sortNum", String.valueOf(sortNum));
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<WorkshopPhaseResult>() {
            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(WorkshopPhaseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    ll_empty.setVisibility(View.GONE);
                    if (recyclerView.getVisibility() == View.GONE) {
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    MWorkshopSection section = response.getResponseData();
                    sectionList.add(section);
                    mAdapter.notifyItemInserted(sectionList.indexOf(section));
                } else {
                    toastFullScreen("添加失败", false);
                }
            }
        }, map));
    }

    private void showPopupWindow() {
        final View popupView = getLayoutInflater().inflate(R.layout.popwindow_workshop_menu, null);
        final PopupWindow mPopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT,
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
                mPopupWindow.dismiss();
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
                mPopupWindow.dismiss();
                Intent intent = new Intent(context, WorkShopDetailActivity.class);
                intent.putExtra("workshopId", workshopId);
                startActivity(intent);
            }
        });
        ll_brief.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
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
                mPopupWindow.dismiss();
                Intent intent = new Intent(context, StudentAssignmentActivity.class);
                intent.putExtra("workshopId", workshopId);
                startActivity(intent);
            }
        });
        ll_member_management.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                Intent intent = new Intent(context, ManagentMemberActivity.class);
                intent.putExtra("workshopId", workshopId);
                startActivity(intent);
            }
        });
        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupWindow.dismiss();
            }
        });
        mPopupWindow.setTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);
        View view = toolBar.getIv_rightImage();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mPopupWindow.showAsDropDown(view, 0, -10);
        } else {
            // 适配 android 7.0
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x, y + view.getHeight() - 10);
        }
    }

    private void deleteTask(String id, final int position) {
        String url = Constants.OUTRT_NET + "/master_" + workshopId + "/unique_uid_" + context.getUserId() + "/m/workshop_section/" + id;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                toast(context, "删除失败，请稍后再试");
            }

            @Override
            public void onResponse(BaseResponseResult response) {
                hideTipDialog();
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    sectionList.remove(position);
                    mAdapter.notifyItemRangeRemoved(position, 1);
                    mAdapter.notifyItemRangeChanged(position, sectionList.size());
                    if (sectionList.size() == 0) {
                        ll_empty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                } else {
                    toast(context, "删除失败，请稍后再试");
                }
            }
        }, map));
    }
}
