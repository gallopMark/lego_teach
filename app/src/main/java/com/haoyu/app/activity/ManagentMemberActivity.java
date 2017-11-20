package com.haoyu.app.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.fragment.WSManagerMemberFragment;
import com.haoyu.app.fragment.WSManagerSearchFragment;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.view.AppToolBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by acer1 on 2017/2/8.
 * 成员管理
 */
public class ManagentMemberActivity extends BaseActivity {
    private ManagentMemberActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    private String workshopId;
    private Bundle bundle;
    private FragmentManager fragmentManager;
    private WSManagerMemberFragment managerMemberFragment;
    private WSManagerSearchFragment managerSearchFragment;
    private final int MANAGER_MEMBER = 1;
    private final int MANAGERR_SERCH = 2;
    private List<Fragment> fragments = new ArrayList<>();
    private boolean isSerch;

    @Override
    public void obBusEvent(MessageEvent event) {
        String action = event.getAction();
        if (action.equals("nameSearch")) {
            toolBar.setShow_right_button(true);
            isSerch = false;
            changeManagerFragment(MANAGER_MEMBER);
        }
    }

    @Override
    public int setLayoutResID() {
        return R.layout.activity_management_member;
    }

    @Override
    public void initView() {
        workshopId = getIntent().getStringExtra("workshopId");
        fragmentManager = getSupportFragmentManager();
        changeManagerFragment(MANAGER_MEMBER);
        registRxBus();
    }


    @Override
    public void setListener() {

        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                if (isSerch) {

                    toolBar.setShow_right_button(true);
                    changeManagerFragment(MANAGER_MEMBER);
                    isSerch = false;
                } else {
                    finish();
                }
            }
        });
        toolBar.setOnRightClickListener(new AppToolBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                isSerch = true;
                toolBar.setShow_right_button(false);
                changeManagerFragment(MANAGERR_SERCH);
            }
        });
    }


    private void changeManagerFragment(int index) {
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragment(transaction);
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString("workshopId", workshopId);
        switch (index) {
            case 1:
                if (managerMemberFragment == null) {
                    managerMemberFragment = new WSManagerMemberFragment();
                    managerMemberFragment.setArguments(bundle);
                    transaction.add(R.id.framelayout, managerMemberFragment);
                    fragments.add(managerMemberFragment);

                } else {
                    transaction.show(managerMemberFragment);
                }
                break;
            case 2:
                index:
                if (managerSearchFragment == null) {
                    managerSearchFragment = new WSManagerSearchFragment();
                    transaction.add(R.id.framelayout, managerSearchFragment);
                    fragments.add(managerSearchFragment);
                } else {
                    transaction.show(managerSearchFragment);
                }
                break;

        }
        transaction.commitAllowingStateLoss();
    }

    private void hideFragment(FragmentTransaction transaction) {
        for (Fragment fragment : fragments) {
            if (fragment != null) {
                transaction.hide(fragment);
            }
        }
    }

}
