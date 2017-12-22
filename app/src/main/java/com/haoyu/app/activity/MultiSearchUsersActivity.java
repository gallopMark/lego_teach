package com.haoyu.app.activity;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.PeerAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.MobileUserData;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.swipe.OnActivityTouchListener;
import com.haoyu.app.swipe.RecyclerTouchListener;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * 创建日期：2017/12/7.
 * 描述:搜索用户多选
 * 作者:xiaoma
 */

public class MultiSearchUsersActivity extends BaseActivity implements XRecyclerView.LoadingListener, RecyclerTouchListener.RecyclerTouchListenerHelper, View.OnClickListener {
    private MultiSearchUsersActivity context;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.tv_clear)
    TextView tv_clear;
    @BindView(R.id.tv_search)
    TextView tv_search;
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.rb_result)
    RadioButton rb_result;
    @BindView(R.id.rb_list)
    RadioButton rb_list;
    @BindView(R.id.line_result)
    View line_result;
    @BindView(R.id.line_list)
    View line_list;
    @BindView(R.id.rl_result)
    RelativeLayout rl_result;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_empty1)
    TextView tv_empty1;
    @BindView(R.id.rl_list)
    RelativeLayout rl_list;
    @BindView(R.id.rv_list)
    RecyclerView rv_list;
    @BindView(R.id.tv_empty2)
    TextView tv_empty2;
    @BindView(R.id.tv_finish)
    TextView tv_finish;
    private List<MobileUser> mDatas = new ArrayList<>();
    private PeerAdapter adapter;
    private boolean isRefresh, isLoadMore;
    private String userName;
    private int page = 1, limit = 30;
    private List<MobileUser> selectList = new ArrayList<>();   //已选择的人员集合
    private MultiAdapter mAdapter;
    private RecyclerTouchListener onTouchListener;
    private OnActivityTouchListener touchListener;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_searchusers;
    }

    @Override
    public void initView() {
        context = this;
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new PeerAdapter(context, mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setPullRefreshEnabled(false);
        xRecyclerView.setLoadingListener(context);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv_list.setLayoutManager(llm);
        mAdapter = new MultiAdapter(selectList);
        rv_list.setAdapter(mAdapter);
        onTouchListener = new RecyclerTouchListener(context, rv_list);
        rv_list.addOnItemTouchListener(onTouchListener);
        setUsers();
    }

    private void setUsers() {
        List<MobileUser> mDatas = (List<MobileUser>) getIntent().getSerializableExtra("users");
        if (mDatas != null) {
            selectList.addAll(mDatas);
        }
        mAdapter.notifyDataSetChanged();
        if (selectList.size() > 0) {
            radioGroup.check(R.id.rb_list);
            rl_list.setVisibility(View.VISIBLE);
        }
        setSelect_state();
    }

    private void setSelect_state() {
        String text = "已选（" + selectList.size() + "）";
        rb_list.setText(text);
        tv_finish.setText("完成（" + selectList.size() + "）");
        if (selectList.size() > 0) {
            rv_list.setVisibility(View.VISIBLE);
            tv_empty2.setVisibility(View.GONE);
            tv_finish.setVisibility(View.VISIBLE);
        } else {
            tv_finish.setVisibility(View.GONE);
            rv_list.setVisibility(View.GONE);
            tv_empty2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setListener() {
        tv_cancel.setOnClickListener(context);
        tv_search.setOnClickListener(context);
        tv_finish.setOnClickListener(context);
        tv_clear.setOnClickListener(context);
        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence)) {
                    tv_clear.setVisibility(View.GONE);
                } else {
                    tv_clear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                line_result.setVisibility(View.INVISIBLE);
                line_list.setVisibility(View.INVISIBLE);
                rl_result.setVisibility(View.GONE);
                rl_list.setVisibility(View.GONE);
                switch (checkId) {
                    case R.id.rb_result:
                        rl_result.setVisibility(View.VISIBLE);
                        line_result.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_list:
                        rl_list.setVisibility(View.VISIBLE);
                        line_list.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                int selected = position - 1;
                if (selected >= 0 && selected < mDatas.size()) {
                    MobileUser user = mDatas.get(selected);
                    if (!selectList.contains(user)) {
                        selectList.add(user);
                    }
                    mAdapter.notifyDataSetChanged();
                    setSelect_state();
                }
            }
        });
        onTouchListener.setSwipeOptionViews(R.id.bt_delete).setSwipeable(R.id.ll_rowFG, R.id.bt_delete, new RecyclerTouchListener.OnSwipeOptionsClickListener() {
            @Override
            public void onSwipeOptionClicked(int viewID, int position) {
                selectList.remove(position);
                mAdapter.notifyDataSetChanged();
                setSelect_state();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_clear:
                et_name.setText(null);
                break;
            case R.id.tv_search:
                init();
                userName = et_name.getText().toString().trim();
                if (TextUtils.isEmpty(userName)) {
                    showMaterialDialog("提示", "请输入姓名");
                } else {
                    searchUsers();
                }
                return;
            case R.id.tv_finish:
                Intent intent = new Intent();
                intent.putExtra("users", (Serializable) selectList);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    private void init() {
        radioGroup.check(R.id.rb_result);
        Common.hideSoftInput(context, et_name);
        isRefresh = true;
        isLoadMore = false;
        page = 1;
    }

    private void searchUsers() {
        String url = Constants.OUTRT_NET + "/m/user?paramMap[realName]=" + userName + "&page=" + page + "&limit=" + limit;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MobileUserData>>() {
            @Override
            public void onBefore(Request request) {
                if (!isLoadMore) {
                    showTipDialog();
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(BaseResponseResult<MobileUserData> response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null && response.getResponseData().getmUsers().size() > 0) {
                    updateUI(response.getResponseData().getmUsers(), response.getResponseData().getPaginator());
                } else {
                    if (isLoadMore) {
                        xRecyclerView.loadMoreComplete(true);
                        xRecyclerView.setLoadingMoreEnabled(false);
                    } else {
                        xRecyclerView.setVisibility(View.GONE);
                        tv_empty1.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    private void updateUI(List<MobileUser> users, Paginator paginator) {
        if (xRecyclerView.getVisibility() != View.VISIBLE) {
            xRecyclerView.setVisibility(View.VISIBLE);
        }
        if (tv_empty1.getVisibility() != View.GONE) {
            tv_empty1.setVisibility(View.GONE);
        }
        if (isRefresh) {
            mDatas.clear();
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        mDatas.addAll(users);
        adapter.notifyDataSetChanged();
        int totalCount = 0;
        if (paginator != null) {
            totalCount = paginator.getTotalCount();
            if (paginator.getHasNextPage()) {
                xRecyclerView.setLoadingMoreEnabled(true);
            } else {
                xRecyclerView.setLoadingMoreEnabled(false);
            }
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
        setResult_text(totalCount);
    }

    private void setResult_text(int totalCount) {
        rb_result.setText("全部（" + totalCount + "）");
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        page += 1;
        searchUsers();
    }

    @Override
    public void setOnActivityTouchListener(OnActivityTouchListener listener) {
        this.touchListener = listener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (isShouldHideInput(et_name, ev)) {
                Common.hideSoftInput(context, et_name);
            }
            return super.dispatchTouchEvent(ev);
        }
        if (touchListener != null) touchListener.getTouchCoordinates(ev);
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(EditText et, MotionEvent event) {
        int[] leftTop = {0, 0};
        //获取输入框当前的location位置
        et.getLocationInWindow(leftTop);
        int left = leftTop[0];
        int top = leftTop[1];
        int bottom = top + et.getHeight();
        int right = left + et.getWidth();
        if (event.getX() > left && event.getX() < right
                && event.getY() > top && event.getY() < bottom) {
            // 点击的是输入框区域，保留点击EditText的事件
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);//用于屏蔽 activity 默认的转场动画效果
    }

    private class MultiAdapter extends BaseArrayRecyclerAdapter<MobileUser> {

        public MultiAdapter(List<MobileUser> mDatas) {
            super(mDatas);
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.multiuser_item;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, MobileUser user, final int position) {
            ImageView iv_ico = holder.obtainView(R.id.iv_ico);
            TextView tv_name = holder.obtainView(R.id.tv_name);
            TextView tv_dept = holder.obtainView(R.id.tv_dept);
            tv_name.setText(user.getRealName());
            GlideImgManager.loadCircleImage(context, user.getAvatar(), R.drawable.user_default, R.drawable.user_default, iv_ico);
            if (TextUtils.isEmpty(user.getDeptName())) {
                tv_dept.setVisibility(View.GONE);
            } else {
                tv_dept.setText(user.getDeptName());
                tv_dept.setVisibility(View.VISIBLE);
            }
        }
    }
}
