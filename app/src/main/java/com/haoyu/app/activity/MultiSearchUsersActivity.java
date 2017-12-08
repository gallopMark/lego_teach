package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.GridUserAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.MobileUserData;
import com.haoyu.app.entity.Paginator;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.view.AppToolBar;
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

public class MultiSearchUsersActivity extends BaseActivity implements XRecyclerView.LoadingListener, View.OnClickListener {
    private MultiSearchUsersActivity context;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.iv_search)
    ImageView iv_search;
    @BindView(R.id.tv_current)
    TextView tv_current;
    @BindView(R.id.tv_user)
    TextView tv_user;
    @BindView(R.id.rl_result)
    RelativeLayout rl_result;
    @BindView(R.id.xRecyclerView)
    XRecyclerView xRecyclerView;
    @BindView(R.id.tv_empty)
    TextView tv_empty;
    private List<MobileUser> mDatas = new ArrayList<>();
    private GridUserAdapter adapter;
    private boolean isRefresh, isLoadMore;
    private String userName;
    private int page = 1, limit = 30;
    private List<MobileUser> selectList = new ArrayList<>();   //已选择的人员集合

    @Override
    public int setLayoutResID() {
        return R.layout.activity_searchuser;
    }

    @Override
    public void initView() {
        context = this;
        setToolBar();
        tv_current.setText("当前受邀人员：");
        setUsers();
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new GridUserAdapter(context, mDatas);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setPullRefreshEnabled(false);
        xRecyclerView.setLoadingListener(context);
    }

    private void setUsers() {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.go_into);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tv_user.setCompoundDrawables(null, null, drawable, null);
        int drwablePadding = PixelFormat.dp2px(context, 12);
        tv_user.setCompoundDrawablePadding(drwablePadding);
        List<MobileUser> mDatas = (List<MobileUser>) getIntent().getSerializableExtra("mobileUserList");
        if (mDatas != null) {
            selectList.addAll(mDatas);
        }
        setUser_text();
    }

    private void setToolBar() {
        toolBar.setTitle_text("受邀人员");
        toolBar.setRight_button_text("完成");
        toolBar.setShow_right_button(true);
        toolBar.setOnTitleClickListener(new AppToolBar.TitleOnClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }

            @Override
            public void onRightClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("mobileUserList", (Serializable) selectList);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public void setListener() {
        iv_search.setOnClickListener(context);
        tv_user.setOnClickListener(context);
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                int selected = position - 1;
                if (selected >= 0 && selected < mDatas.size()) {
                    MobileUser user = mDatas.get(selected);
                    if (!selectList.contains(user)) {
                        selectList.add(user);
                    }
                    setUser_text();
                }
            }
        });
    }

    private void setUser_text() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectList.size(); i++) {
            String name = selectList.get(i).getRealName();
            sb.append(name);
            if (i < selectList.size() - 1) {
                sb.append("，");
            }
        }
        tv_user.setText(sb);
        if (overLine(tv_user)) {
            tv_user.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            sb.append(" 等人");
            tv_user.setText(sb);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_search:
                init();
                userName = et_name.getText().toString().trim();
                if (TextUtils.isEmpty(userName)) {
                    showMaterialDialog("提示", "请输入姓名");
                } else {
                    searchUsers();
                }
                return;
            case R.id.tv_user:
                showSelects();
                return;
        }
    }

    private void init() {
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
                        tv_empty.setVisibility(View.VISIBLE);
                    }
                }
            }
        }));
    }

    private void updateUI(List<MobileUser> users, Paginator paginator) {
        if (rl_result.getVisibility() != View.VISIBLE) {
            rl_result.setVisibility(View.VISIBLE);
        }
        if (isRefresh) {
            mDatas.clear();
        } else if (isLoadMore) {
            xRecyclerView.loadMoreComplete(true);
        }
        mDatas.addAll(users);
        adapter.notifyDataSetChanged();
        if (paginator != null && paginator.getHasNextPage()) {
            xRecyclerView.setLoadingMoreEnabled(true);
        } else {
            xRecyclerView.setLoadingMoreEnabled(false);
        }
    }

    private void showSelects() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_multiuser, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        TextView tv_current = view.findViewById(R.id.tv_current);
        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        final TextView tv_empty = view.findViewById(R.id.tv_empty);
        tv_current.setText("当前受邀人员");
        tv_empty.setText("没有受邀人员~");
        if (selectList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            final MultiAdapter adapter = new MultiAdapter(selectList);
            recyclerView.setAdapter(adapter);
            adapter.setOnItemRemoveListener(new OnItemRemoveListener() {
                @Override
                public void onItemRemove(int positioin) {
                    selectList.remove(positioin);
                    adapter.notifyDataSetChanged();
                    setUser_text();
                    if (selectList.size() == 0) {
                        recyclerView.setVisibility(View.GONE);
                        tv_empty.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
        int width = ScreenUtils.getScreenWidth(context);
        int height = ScreenUtils.getScreenHeight(context) / 2;
        dialog.getWindow().setLayout(width, height);
        dialog.getWindow().setWindowAnimations(R.style.dialog_anim);
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
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

    private class MultiAdapter extends BaseArrayRecyclerAdapter<MobileUser> {
        private OnItemRemoveListener onItemRemoveListener;

        public MultiAdapter(List<MobileUser> mDatas) {
            super(mDatas);
        }

        public void setOnItemRemoveListener(OnItemRemoveListener onItemRemoveListener) {
            this.onItemRemoveListener = onItemRemoveListener;
        }

        @Override
        public int bindView(int viewtype) {
            return R.layout.multiuser_item;
        }

        @Override
        public void onBindHoder(RecyclerHolder holder, MobileUser user, final int position) {
            Button bt_delete = holder.obtainView(R.id.bt_delete);
            TextView tv_name = holder.obtainView(R.id.tv_name);
            tv_name.setText(user.getRealName());
            bt_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemRemoveListener != null) {
                        onItemRemoveListener.onItemRemove(position);
                    }
                }
            });
        }
    }

    public interface OnItemRemoveListener {
        void onItemRemove(int positioin);
    }
}
