package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.AppDiscussionAdapter;
import com.haoyu.app.adapter.MFileInfoAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.dialog.CommentDialog;
import com.haoyu.app.entity.AppActivityViewEntity;
import com.haoyu.app.entity.AppActivityViewResult;
import com.haoyu.app.entity.AttitudeMobileResult;
import com.haoyu.app.entity.CourseSectionActivity;
import com.haoyu.app.entity.DiscussEntity;
import com.haoyu.app.entity.MFileInfo;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.ReplyEntity;
import com.haoyu.app.entity.ReplyListResult;
import com.haoyu.app.entity.ReplyResult;
import com.haoyu.app.entity.TimePeriod;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.TimeUtil;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.FullyLinearLayoutManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.StickyScrollView;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;

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
 * 创建日期：2017/8/17 on 10:05
 * 描述:教学研讨（包括课程学习教学研讨活动和工作坊研修教学研讨活动）
 * 作者:马飞奔 Administrator
 */
public class TeachingDiscussionActivity extends BaseActivity implements View.OnClickListener {
    private TeachingDiscussionActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.ll_tips)
    LinearLayout ll_tips;
    @BindView(R.id.tv_tips)
    TextView tv_tips;
    @BindView(R.id.tv_close)
    TextView tv_close;
    @BindView(R.id.scrollView)
    StickyScrollView scrollView;
    @BindView(R.id.tv_time)
    TextView tv_time;   //教学研讨时间
    @BindView(R.id.tv_title)
    TextView tv_title;  //教学研讨标题
    @BindView(R.id.tv_partNum)
    TextView tv_partNum;   //参与人数
    @BindView(R.id.tv_browseNum)
    TextView tv_browseNum;   //浏览人数
    @BindView(R.id.tv_content)
    TextView tv_content;   //教学研讨内容
    @BindView(R.id.rv_file)
    RecyclerView rv_file;
    @BindView(R.id.ll_discussion)
    LinearLayout ll_discussion;
    @BindView(R.id.tv_discussCount)
    TextView tv_discussCount;   //研讨回复数量
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.tv_emptyComment)
    TextView tv_emptyComment;
    @BindView(R.id.tv_more_reply)
    TextView tv_more_reply;   //更多回复内容
    @BindView(R.id.tv_bottomView)
    TextView tv_bottomView;
    private boolean running;   //是否在培训时间内、活动是否进行中
    private TimePeriod timePeriod;
    private String discussType, activityId, workshopId, discussionRelationId, baseUrl, postUrl, mainUrl;
    private int needMainNum, needSubNum, mainNum, subNum;  //要求完成主回复，子回复；已完成主回复，子回复。
    private AppActivityViewEntity.DiscussionUserMobileEntity discussEntity;
    private int discussNum;//总回复数
    private List<ReplyEntity> mDatas = new ArrayList<>();
    private AppDiscussionAdapter adapter;
    private int childPosition, replyPosition;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_teaching_discussion;
    }

    @Override
    public void initView() {
        running = getIntent().getBooleanExtra("running", false);
        timePeriod = (TimePeriod) getIntent().getSerializableExtra("timePeriod");
        discussType = getIntent().getStringExtra("discussType");
        workshopId = getIntent().getStringExtra("workshopId");
        activityId = getIntent().getStringExtra("activityId");
        needMainNum = getIntent().getIntExtra("needMainNum", 0);
        needSubNum = getIntent().getIntExtra("needSubNum", 0);
        mainNum = getIntent().getIntExtra("mainNum", 0);
        subNum = getIntent().getIntExtra("subNum", 0);
        discussEntity = (AppActivityViewEntity.DiscussionUserMobileEntity) getIntent().getSerializableExtra("discussUser");
        if (discussType != null && discussType.equals("course")) {
            baseUrl = Constants.OUTRT_NET + "/" + activityId + "/teach/m/discussion/post";
            postUrl = Constants.OUTRT_NET + "/" + activityId + "/unique_uid_" + getUserId() + "/m/discussion/post";
        } else {
            baseUrl = Constants.OUTRT_NET + "/student_" + workshopId + "/m/discussion/post";
            postUrl = Constants.OUTRT_NET + "/student_" + workshopId + "/unique_uid_" + getUserId() + "/m/discussion/post";
        }
        mainUrl = baseUrl + "?discussionUser.discussionRelation.id=" + discussionRelationId + "&orders=CREATE_TIME.ASC";
        setSupportToolbar();
        showTips();
        if (discussEntity != null && discussEntity.getmDiscussion() != null && discussEntity.getmDiscussion().getmDiscussionRelations() != null && discussEntity.getmDiscussion().getmDiscussionRelations().size() > 0)
            discussionRelationId = discussEntity.getmDiscussion().getmDiscussionRelations().get(0).getId();
        if (discussEntity != null && discussEntity.getmDiscussion() != null)
            showData(discussEntity.getmDiscussion());
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AppDiscussionAdapter(context, mDatas, getUserId());
        recyclerView.setAdapter(adapter);
        registRxBus();
    }

    private void setSupportToolbar() {
        if (discussType != null && discussType.equals("course"))
            toolBar.setTitle_text("主题研讨");
        else
            toolBar.setTitle_text("教学研讨");
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                showTopView();
            }
        });
    }

    private void showTips() {
        toolBar.setShow_right_button(false);
        String message = "要求完成 <font color='#ffa500'>" + needMainNum + "</font> 个回复，<font color='#ffa500'>" + needSubNum + "</font> 个子回复 / 您已完成 <font color='#ffa500'>" + mainNum + "</font> 个回复，<font color='#ffa500'>" + subNum + "</font> 个子回复。";
        tv_tips.setText(Html.fromHtml(message));
    }

    private void showTopView() {
        ll_tips.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.scale_show);
        if (animation != null) {
            ll_tips.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    toolBar.setShow_right_button(false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void showData(DiscussEntity entity) {
        if (running) {
            if (timePeriod != null && timePeriod.getMinutes() > 0) {
                String time = "离活动结束还剩：" + TimeUtil.dateDiff(timePeriod.getMinutes());
                tv_time.setText(Html.fromHtml(time));
            } else {
                if (timePeriod != null && timePeriod.getState() != null) {
                    tv_time.setText("活动" + timePeriod.getState());
                } else {
                    tv_time.setText("活动进行中");
                }
            }
        } else {
            tv_time.setText("活动已结束");
        }
        if (entity.getmDiscussionRelations() != null && entity.getmDiscussionRelations().size() > 0) {
            int browseNum = entity.getmDiscussionRelations().get(0).getBrowseNum();
            tv_browseNum.setText("浏览人数：" + browseNum);
            int followNum = entity.getmDiscussionRelations().get(0).getParticipateNum();
            tv_partNum.setText("参与人数：" + followNum);
        }
        if (entity.getTitle() != null) {
            tv_title.setText(Html.fromHtml(entity.getTitle()));
        }
        setContent_text(entity.getContent());
        setFileInfos(entity.getmFileInfos());
    }

    private void setContent_text(String content) {
        if (content != null && content.trim().length() > 0) {
            Html.ImageGetter imageGetter = new HtmlHttpImageGetter(tv_content, Constants.REFERER, true);
            Spanned spanned = Html.fromHtml(content, imageGetter, null);
            tv_content.setMovementMethod(LinkMovementMethod.getInstance());
            tv_content.setText(spanned);
            tv_content.setVisibility(View.VISIBLE);
        } else {
            tv_content.setVisibility(View.GONE);
        }
    }

    private void setFileInfos(final List<MFileInfo> fileInfos) {
        if (fileInfos != null && fileInfos.size() > 0) {
            rv_file.setVisibility(View.VISIBLE);
            FullyLinearLayoutManager layoutManager = new FullyLinearLayoutManager(context);
            layoutManager.setOrientation(FullyLinearLayoutManager.VERTICAL);
            rv_file.setLayoutManager(layoutManager);
            MFileInfoAdapter adapter = new MFileInfoAdapter(fileInfos, true);
            rv_file.setAdapter(adapter);
            rv_file.setAdapter(adapter);
            adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                    MFileInfo fileInfo = fileInfos.get(position);
                    Intent intent = new Intent(context, MFileInfoActivity.class);
                    intent.putExtra("fileInfo", fileInfo);
                    startActivity(intent);
                }
            });
        } else {
            rv_file.setVisibility(View.GONE);
        }
    }

    @Override
    public void initData() {
        String url = mainUrl + "&page=1" + "&limit=5";
        tv_more_reply.setVisibility(View.GONE);
        showTipDialog();
        addSubscription(Flowable.just(url).map(new Function<String, ReplyListResult>() {
            @Override
            public ReplyListResult apply(String url) throws Exception {
                return getReply(url);
            }
        }).map(new Function<ReplyListResult, ReplyListResult>() {
            @Override
            public ReplyListResult apply(ReplyListResult result) throws Exception {
                if (result != null && result.getResponseData() != null && result.getResponseData().getmDiscussionPosts().size() > 0) {
                    return getChildReply(result, result.getResponseData().getmDiscussionPosts());
                }
                return result;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ReplyListResult>() {
                    @Override
                    public void accept(ReplyListResult response) throws Exception {
                        hideTipDialog();
                        updateUI(response);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        hideTipDialog();
                        loadFailView.setVisibility(View.VISIBLE);
                    }
                }));
    }

    /*获取主回复*/
    private ReplyListResult getReply(String url) throws Exception {
        String json = OkHttpClientManager.getAsString(context, url);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, ReplyListResult.class);
    }

    /*通过主回复id获取子回复*/
    private ReplyListResult getChildReply(ReplyListResult result, List<ReplyEntity> list) {
        for (int i = 0; i < list.size(); i++) {
            String mainPostId = list.get(i).getId();
            String url = mainUrl + "&mainPostId=" + mainPostId;
            try {
                ReplyListResult mResult = getReply(url);
                if (mResult != null && mResult.getResponseData() != null) {
                    List<ReplyEntity> childList = mResult.getResponseData().getmDiscussionPosts();
                    result.getResponseData().getmDiscussionPosts().get(i).setChildReplyEntityList(childList);
                }
            } catch (Exception e) {
                continue;
            }
        }
        return result;
    }

    private void updateUI(ReplyListResult response) {
        mDatas.clear();
        if (ll_discussion.getVisibility() != View.VISIBLE) {
            ll_discussion.setVisibility(View.VISIBLE);
        }
        if (tv_bottomView.getVisibility() != View.VISIBLE) {
            tv_bottomView.setVisibility(View.VISIBLE);
        }
        if (response != null && response.getResponseData() != null && response.getResponseData().getmDiscussionPosts() != null
                && response.getResponseData().getmDiscussionPosts().size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            mDatas.addAll(response.getResponseData().getmDiscussionPosts());
            adapter.notifyDataSetChanged();
            if (response.getResponseData().getPaginator() != null) {
                if (response.getResponseData().getPaginator().getHasNextPage()) {
                    tv_more_reply.setText("查看所有评论");
                    tv_more_reply.setVisibility(View.VISIBLE);
                }
                discussNum = response.getResponseData().getPaginator().getTotalCount();
            }
        } else {
            setEmpty_text();
        }
        setDiscussCount(discussNum);
    }

    private void setEmpty_text() {
        tv_emptyComment.setVisibility(View.VISIBLE);
        String text = "目前还没人参与评论，\n赶紧去发表您的评论吧！";
        SpannableString ssb = new SpannableString(text);
        int start = text.indexOf("去") + 1;
        int end = text.indexOf("吧");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                if (running) {
                    showCommentDialog(false);
                } else {
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
                }
            }
        };
        ssb.setSpan(clickableSpan, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.defaultColor)),
                start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_emptyComment.setMovementMethod(LinkMovementMethod.getInstance());
        tv_emptyComment.setText(ssb);
    }

    private void setDiscussCount(int discussNum) {
        String text = "已有 " + discussNum + " 回复";
        tv_discussCount.setText(text);
    }

    @Override
    public void setListener() {
        tv_close.setOnClickListener(context);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
        ll_discussion.setOnClickListener(context);
        tv_bottomView.setOnClickListener(context);
        tv_more_reply.setOnClickListener(context);
        adapter.setOnPostClickListener(new AppDiscussionAdapter.OnPostClickListener() {
            @Override
            public void onTargetClick(View view, int position, ReplyEntity entity) {

            }

            @Override
            public void onChildClick(View view, int position) {
                childPosition = position;
                if (running) {
                    showCommentDialog(true);
                } else {
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
                }
            }
        });
        adapter.setSupportCallBack(new AppDiscussionAdapter.SupportCallBack() {
            @Override
            public void support(int position, TextView tv_like) {
                createLike(position);
            }
        });
        adapter.setMoreReplyCallBack(new AppDiscussionAdapter.MoreReplyCallBack() {
            @Override
            public void callBack(ReplyEntity entity, int position) {
                replyPosition = position;
                Intent intent = new Intent(context, AppMoreChildReplyActivity.class);
                intent.putExtra("entity", entity);
                intent.putExtra("entity", entity);
                intent.putExtra("discussType", discussType);
                intent.putExtra("activityId", activityId);
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("relationId", discussionRelationId);
                if (running)
                    intent.putExtra("canEdit", true);
                else
                    intent.putExtra("canEdit", false);
                startActivity(intent);
            }
        });
        adapter.setDeleteMainReply(new AppDiscussionAdapter.DeleteMainReply() {
            @Override
            public void deleteMainReply(String id, int position) {
                if (running) {
                    deleteReply(id, position);
                } else {
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
                }
            }
        });
    }

    private void showCommentDialog(final boolean sendChild) {
        CommentDialog dialog = new CommentDialog(context, "请输入评论内容");
        dialog.show();
        dialog.setSendCommentListener(new CommentDialog.OnSendCommentListener() {
            @Override
            public void sendComment(String content) {
                if (sendChild) {
                    sendChildReply(childPosition, content);
                } else {
                    sendMainReply(content);
                }
            }
        });
    }

    /*创建子回复*/
    private void sendChildReply(final int position, final String content) {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("mainPostId", mDatas.get(position).getId());
        map.put("discussionUser.discussionRelation.id", discussionRelationId);
        addSubscription(OkHttpClientManager.postAsyn(context, postUrl, new OkHttpClientManager.ResultCallback<ReplyResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            public void onError(Request request, Exception exception) {
                hideTipDialog();
                onNetWorkError(context);
            }

            public void onResponse(ReplyResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    int childPostCount = mDatas.get(position).getChildPostCount() + 1;
                    mDatas.get(position).setChildPostCount(childPostCount);
                    if (mDatas.get(position).getChildReplyEntityList() != null && mDatas.get(position).getChildReplyEntityList().size() < 10) {
                        ReplyEntity entity = response.getResponseData();
                        entity.setCreator(getCreator(entity.getCreator()));
                        mDatas.get(position).getChildReplyEntityList().add(entity);
                    } else {
                        toastFullScreen("评论成功", true);
                    }
                    adapter.notifyDataSetChanged();
                    getActivityInfo();
                } else {
                    toastFullScreen("评论失败", false);
                }
            }
        }, map));
    }

    /*创建主回复*/
    private void sendMainReply(String content) {
        Map<String, String> map = new HashMap<>();
        map.put("content", content);
        map.put("discussionUser.discussionRelation.id", discussionRelationId);
        addSubscription(OkHttpClientManager.postAsyn(context, postUrl, new OkHttpClientManager.ResultCallback<ReplyResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
            }

            @Override
            public void onResponse(ReplyResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    if (recyclerView.getVisibility() != View.VISIBLE)
                        recyclerView.setVisibility(View.VISIBLE);
                    if (tv_emptyComment.getVisibility() != View.GONE)
                        tv_emptyComment.setVisibility(View.GONE);
                    if (mDatas.size() < 5) {
                        ReplyEntity entity = response.getResponseData();
                        entity.setCreator(getCreator(entity.getCreator()));
                        mDatas.add(entity);
                        adapter.notifyDataSetChanged();
                    } else {
                        tv_more_reply.setVisibility(View.VISIBLE);
                        toastFullScreen("评论成功", true);
                    }
                    getActivityInfo();
                    discussNum++;
                    setDiscussCount(discussNum);
                } else {
                    toastFullScreen("评论失败", false);
                }
            }
        }, map));
    }

    private MobileUser getCreator(MobileUser creaotr) {
        if (creaotr == null) {
            creaotr = new MobileUser();
            creaotr.setId(getUserId());
            creaotr.setAvatar(getAvatar());
            creaotr.setRealName(getRealName());
        } else {
            creaotr.setId(getUserId());
            creaotr.setAvatar(getAvatar());
            creaotr.setRealName(getRealName());
        }
        return creaotr;
    }

    /**
     * 创建评论列表点赞观点(点赞)
     *
     * @param position
     */
    private void createLike(final int position) {
        String url = Constants.OUTRT_NET + "/m/attitude";
        String relationId = mDatas.get(position).getId();
        HashMap<String, String> map = new HashMap<>();
        map.put("attitude", "support");
        map.put("relation.id", relationId);
        map.put("relation.type", "discussion_post");
        addSubscription(OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<AttitudeMobileResult>() {
            public void onError(Request request, Exception exception) {
                onNetWorkError(context);
            }

            public void onResponse(AttitudeMobileResult response) {
                if (response != null && response.getResponseCode() != null && response.getResponseCode().equals("00")) {
                    int count = mDatas.get(position).getSupportNum() + 1;
                    mDatas.get(position).setSupportNum(count);
                    adapter.notifyDataSetChanged();
                } else if (response != null && response.getResponseMsg() != null) {
                    toast(context, "您已点赞过");
                } else {
                    toast(context, "点赞失败");
                }
            }
        }, map));
    }

    private void deleteReply(String id, final int position) {
        String url = postUrl + "/" + id;
        Map<String, String> map = new HashMap<>();
        map.put("_method", "delete");
        map.put("discussionUser.discussionRelation.id", discussionRelationId);
        map.put("mainPostId", id);
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
                if (response != null && response.getResponseCode() != null
                        && response.getResponseCode().equals("00")) {
                    mDatas.remove(position);
                    adapter.notifyDataSetChanged();
                    if (mDatas.size() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        tv_emptyComment.setVisibility(View.VISIBLE);
                    }
                    discussNum--;
                    setDiscussCount(discussNum);
                    getActivityInfo();
                    initData();
                }
            }
        }, map));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_close:
                hideTopView();
                return;
            case R.id.ll_discussion:
                scrollView.scrollTo(0, recyclerView.getTop());
                return;
            case R.id.tv_comment:
                if (running) {
                    showCommentDialog(false);
                } else {
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
                }
                return;
            case R.id.tv_bottomView:
                if (running) {
                    showCommentDialog(false);
                } else {
                    showMaterialDialog("提示", "活动已结束，无法参与研讨");
                }
                return;
            case R.id.tv_more_reply:
                Intent intent = new Intent(context, AppMoreMainReplyActivity.class);
                intent.putExtra("type", "comment");
                intent.putExtra("discussType", discussType);
                intent.putExtra("activityId", activityId);
                intent.putExtra("workshopId", workshopId);
                intent.putExtra("relationId", discussionRelationId);
                if (running)
                    intent.putExtra("canEdit", true);
                else
                    intent.putExtra("canEdit", false);
                startActivity(intent);
                return;
        }
    }

    private void hideTopView() {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.scale_hide);
        if (animation != null) {
            ll_tips.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ll_tips.setVisibility(View.GONE);
                    toolBar.setShow_right_button(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            ll_tips.setVisibility(View.GONE);
            toolBar.setShow_right_button(true);
        }
    }

    @Override
    public void obBusEvent(MessageEvent event) {
        if (event.action.equals(Action.CREATE_MAIN_REPLY) && event.obj != null && event.obj instanceof ReplyEntity) {
            discussNum++;
            setDiscussCount(discussNum);
            ReplyEntity entity = (ReplyEntity) event.obj;
            if (mDatas.size() < 5) {
                mDatas.add(entity);
                adapter.notifyDataSetChanged();
            }
            getActivityInfo();
        } else if (event.action.equals(Action.CREATE_CHILD_REPLY)) {
            /*来自更多评论列表界面创建子回复*/
            if (event.getBundle() != null && event.getBundle().getSerializable("mainReply") != null
                    && event.getBundle().getSerializable("mainReply") instanceof ReplyEntity) {
                ReplyEntity mainReply = (ReplyEntity) event.getBundle().getSerializable("mainReply");
                int position = mDatas.indexOf(mainReply);
                if (position != -1 && event.getBundle().getSerializable("childReply") != null
                        && event.getBundle().getSerializable("childReply") instanceof ReplyEntity) {
                    ReplyEntity childReply = (ReplyEntity) event.getBundle().getSerializable("childReply");
                    int childPostNum = mDatas.get(position).getChildPostCount() + 1;
                    mDatas.get(position).setChildPostCount(childPostNum);
                    if (mDatas.get(position).getChildReplyEntityList() != null && mDatas.get(position).getChildReplyEntityList().size() < 10) {
                        mDatas.get(position).getChildReplyEntityList().add(childReply);
                    }
                    adapter.notifyDataSetChanged();
                }
            } else {   //来自更多回复列表创建回复
                int childPostNum = mDatas.get(replyPosition).getChildPostCount() + 1;
                mDatas.get(replyPosition).setChildPostCount(childPostNum);
                adapter.notifyDataSetChanged();
            }
            getActivityInfo();
        } else if (event.action.equals(Action.CREATE_LIKE)) {
            if (event.obj != null && event.obj instanceof ReplyEntity) {
                ReplyEntity entity = (ReplyEntity) event.obj;
                if (mDatas.indexOf(entity) != -1) {
                    mDatas.set(mDatas.indexOf(entity), entity);
                    adapter.notifyDataSetChanged();
                }
            } else {
                int supportNum = mDatas.get(replyPosition).getSupportNum();
                mDatas.get(replyPosition).setSupportNum(supportNum + 1);
                adapter.notifyDataSetChanged();
            }
        } else if (event.action.equals(Action.DELETE_MAIN_REPLY) && event.obj != null
                && event.obj instanceof ReplyEntity) {
            discussNum--;
            setDiscussCount(discussNum);
            ReplyEntity entity = (ReplyEntity) event.obj;
            if (mDatas.contains(entity)) {
                mDatas.remove(entity);
                adapter.notifyDataSetChanged();
                initData();
            }
            getActivityInfo();
        }
    }

    private void getActivityInfo() {
        String url;
        if (discussType != null && discussType.equals("course"))
            url = Constants.OUTRT_NET + "/" + activityId + "/teach/m/activity/ncts/" + activityId + "/view";
        else
            url = Constants.OUTRT_NET + "/student_" + workshopId + "/m/activity/wsts/" + activityId + "/view";
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<AppActivityViewResult>() {
            @Override
            public void onError(Request request, Exception e) {
                onNetWorkError(context);
            }

            @Override
            public void onResponse(AppActivityViewResult response) {
                if (response != null && response.getResponseData() != null) {
                    if (response.getResponseData().getmDiscussionUser() != null) {
                        mainNum = response.getResponseData().getmDiscussionUser().getMainPostNum();
                        subNum = response.getResponseData().getmDiscussionUser().getSubPostNum();
                    }
                    showTips();
                    showTopView();
                    if (response.getResponseData().getmActivityResult() != null && response.getResponseData().getmActivityResult().getmActivity() != null) {
                        CourseSectionActivity activity = response.getResponseData().getmActivityResult().getmActivity();
                        Intent intent = new Intent();
                        intent.putExtra("activity", activity);
                        setResult(RESULT_OK, intent);
                    }
                }
            }
        }));
    }
}
