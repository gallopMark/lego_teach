package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.adapter.TeachingInvitedAdapter;
import com.haoyu.app.adapter.TeachingResearchInvitedAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.base.BaseResponseResult;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.MobileUserData;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.Common;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Request;

/**
 * Created by acer1 on 2017/3/17.
 * 教研活动的发布，输入受邀人员
 */
public class TeachingResearchInvitedActivity extends BaseActivity implements View.OnClickListener {
    private TeachingResearchInvitedActivity context = this;
    @BindView(R.id.et_name)
    EditText et_name;
    @BindView(R.id.rl_search)
    View rl_search;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.iv_back)
    View iv_back;
    @BindView(R.id.tv_finish)
    TextView tv_finish;
    private List<MobileUser> mobileUserList = new ArrayList<>();
    private TeachingInvitedAdapter lecturerAdapter;
    private ArrayMap<String, List<MobileUser>> arrayMap = new ArrayMap<>();

    @Override
    public int setLayoutResID() {
        return R.layout.teaching_research_invited;
    }

    @Override
    public void initView() {
        List<MobileUser> mobileUsers = (List<MobileUser>) getIntent().getSerializableExtra("mobileUserList");
        mobileUserList.addAll(mobileUsers);
        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        lecturerAdapter = new TeachingInvitedAdapter(context, mobileUserList);
        recyclerView.setAdapter(lecturerAdapter);
    }

    @Override
    public void setListener() {
        rl_search.setOnClickListener(context);
        iv_back.setOnClickListener(context);
        tv_finish.setOnClickListener(context);
        lecturerAdapter.setDisposeCallBack(new TeachingInvitedAdapter.onDisposeCallBack() {
            @Override
            public void onAlter(int position, MobileUser entity) {

            }

            @Override
            public void onDelete(int position, MobileUser entity) {
                mobileUserList.remove(position);
                lecturerAdapter.notifyDataSetChanged();
            }
        });

    }

    private void initData(final String name) {
        if (arrayMap.get(name) != null && arrayMap.get(name).size() > 0) {
            showDialog(arrayMap.get(name));
        } else {
            String url = Constants.OUTRT_NET + "/m/user?id=" + getUserId() + "&paramMap[realName]=" + name;
            OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult<MobileUserData>>() {
                @Override
                public void onBefore(Request request) {
                    super.onBefore(request);
                    showTipDialog();
                }

                @Override
                public void onError(Request request, Exception e) {
                    hideTipDialog();
                    onNetWorkError(context);
                }

                @Override
                public void onResponse(BaseResponseResult<MobileUserData> response) {
                    hideTipDialog();
                    if (response != null && response.getResponseData() != null && response.getResponseData().getmUsers() != null
                            && response.getResponseData().getmUsers().size() > 0) {
                        arrayMap.put(name, response.getResponseData().getmUsers());
                        showDialog(response.getResponseData().getmUsers());
                    } else {
                        toast(context, "没有搜索到相关人员");
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        Common.hideSoftInput(context);
        switch (v.getId()) {
            case R.id.rl_search:
                Search();
                break;
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_finish:
                Intent intent = new Intent();
                intent.putExtra("mobileUserList", (Serializable) mobileUserList);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    private void Search() {
        String name = et_name.getText().toString().trim();
        if (name.length() == 0) {
            toast(context, "请输入搜索人名");
        } else {
            initData(name);
        }
    }

    private void showDialog(final List<MobileUser> mobileUsers) {
        View view = getLayoutInflater().inflate(R.layout.dialog_invited_user, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCanceledOnTouchOutside(false);
        RecyclerView recyclerView = getView(view, R.id.recyclerView);
        LinearLayout ll_select = getView(view, R.id.ll_select);
        final CheckBox checkBox = getView(view, R.id.checkBox);
        final TextView tv_selectAll = getView(view, R.id.tv_selectAll);
        Button makesure = getView(view, R.id.makesure);
        Button cancel = getView(view, R.id.cancel);
        if (mobileUsers.size() > 1) {
            ll_select.setVisibility(View.VISIBLE);
        } else {
            ll_select.setVisibility(View.GONE);
        }
        GridLayoutManager layoutManager = new GridLayoutManager(context, 3);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        final TeachingResearchInvitedAdapter mAdapter = new TeachingResearchInvitedAdapter(mobileUsers);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                mAdapter.updateItem(position);
            }
        });
        mAdapter.setOnSelectListener(new TeachingResearchInvitedAdapter.OnSelectListener() {
            @Override
            public void onSelect(ArrayMap<Integer, Boolean> isSelected) {
                boolean selected = false;
                int select = 0;
                for (Boolean isCheck : isSelected.values()) {
                    if (isCheck) {
                        select++;
                        selected = true;
                    }
                }
                if (!selected) {
                    checkBox.setChecked(false);
                    tv_selectAll.setText("全选");
                }
                if (select == isSelected.size()) {
                    checkBox.setChecked(true);
                    tv_selectAll.setText("取消已选");
                }
            }
        });
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.ll_select:
                    case R.id.checkBox:
                        if (checkBox.isChecked()) {
                            checkBox.setChecked(false);
                            tv_selectAll.setText("全选");
                            mAdapter.clearAll();
                        } else {
                            checkBox.setChecked(true);
                            tv_selectAll.setText("取消已选");
                            mAdapter.selectAll();
                        }
                        break;
                    case R.id.makesure:
                        ArrayMap<Integer, Boolean> isSelected = mAdapter.getIsSelected();
                        for (Integer position : isSelected.keySet()) {
                            if (isSelected.get(position) && !mobileUserList.contains(mobileUsers.get(position))) {
                                mobileUserList.add(mobileUsers.get(position));
                                lecturerAdapter.notifyDataSetChanged();
                            }
                        }
                        dialog.dismiss();
                        break;
                    case R.id.cancel:
                        dialog.dismiss();
                        break;
                }
            }
        };
        ll_select.setOnClickListener(listener);
        checkBox.setOnClickListener(listener);
        makesure.setOnClickListener(listener);
        cancel.setOnClickListener(listener);
        dialog.show();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ScreenUtils.getScreenWidth(context) / 7 * 6,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(view, params);
    }

}
