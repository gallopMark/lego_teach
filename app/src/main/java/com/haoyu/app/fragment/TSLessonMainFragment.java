package com.haoyu.app.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.lego.teach.R;

import java.math.BigDecimal;

import butterknife.BindString;
import butterknife.BindView;

/**
 * 创建日期：2017/8/15 on 10:35
 * 描述:教研创课
 * 作者:马飞奔 Administrator
 */
public class TSLessonMainFragment extends BaseFragment {
    @BindView(R.id.radioGroup)
    RadioGroup radioGroup;
    @BindView(R.id.rb_all)
    RadioButton rb_all;
    @BindView(R.id.rb_my)
    RadioButton rb_my;
    @BindString(R.string.gen_class_all)
    String text_all;
    @BindString(R.string.gen_class_my)
    String text_my;
    private TSLessonChildFragment f1, f2;
    private FragmentManager fragmentManager;
    private int checkIndex = 1;

    @Override
    public int createView() {
        return R.layout.fragment_teachstudy_main;
    }

    @Override
    public void initData() {
        rb_all.setText(text_all);
        rb_my.setText(text_my);
        fragmentManager = getChildFragmentManager();
        setCheckIndex(checkIndex);
    }

    public void setCheckIndex(int checkIndex) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideFragments(transaction);
        switch (checkIndex) {
            case 1:
                if (f1 == null) {
                    f1 = new TSLessonChildFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", 1);
                    f1.setArguments(bundle);
                    f1.setOnResponseListener(new TSLessonChildFragment.OnResponseListener() {
                        @Override
                        public void getTotalCount(int totalCount) {
                            rb_all.setText(text_all + "（" + getCount(totalCount) + "）");
                        }
                    });
                    transaction.add(R.id.content, f1);
                } else {
                    transaction.show(f1);
                }
                break;
            case 2:
                if (f2 == null) {
                    f2 = new TSLessonChildFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", 2);
                    f2.setArguments(bundle);
                    f2.setOnResponseListener(new TSLessonChildFragment.OnResponseListener() {
                        @Override
                        public void getTotalCount(int totalCount) {
                            rb_my.setText(text_my + "（" + getCount(totalCount) + "）");
                        }
                    });
                    transaction.add(R.id.content, f2);
                } else {
                    transaction.show(f2);
                }
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (f1 != null)
            transaction.hide(f1);
        if (f2 != null)
            transaction.hide(f2);
    }

    private String getCount(int count) {
        if (count < 10000) {
            return String.valueOf(count);
        }
        double num = (double) count / 10000;
        BigDecimal bd = new BigDecimal(num);
        num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (num < 10000) {
            if (num / 1000 > 1) {
                bd = new BigDecimal(num / 1000);
                num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                return num + "千万";
            } else if (num / 100 > 1) {
                bd = new BigDecimal(num / 100);
                num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                return num + "百万";
            } else if (num / 10 > 1) {
                bd = new BigDecimal(num / 10);
                num = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                return num + "十万";
            }
            return num + "万";
        }
        return "大于1亿";
    }

    @Override
    public void setListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                switch (checkId) {
                    case R.id.rb_all:
                        checkIndex = 1;
                        break;
                    case R.id.rb_my:
                        checkIndex = 2;
                        break;
                }
                setCheckIndex(checkIndex);
            }
        });
    }
}
