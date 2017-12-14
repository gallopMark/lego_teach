package com.haoyu.app.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.lego.teach.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建日期：2017/12/13.
 * 描述:工作坊学员考核列表适配器
 * 作者:xiaoma
 */

public class WSMobileUserAdapter extends BaseArrayRecyclerAdapter<WorkShopMobileUser> {

    private Context context;
    private boolean isEdit;
    private List<WorkShopMobileUser> mSelects = new ArrayList<>();
    private OnItemSelectListener onItemSelectListener;

    public WSMobileUserAdapter(Context context, List<WorkShopMobileUser> mDatas) {
        super(mDatas);
        this.context = context;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
        notifyDataSetChanged();
    }

    public void selecetAll() {
        mSelects.clear();
        mSelects.addAll(mDatas);
        notifyDataSetChanged();
    }

    public void cancelAll() {
        mSelects.clear();
        notifyDataSetChanged();
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.wsmobileuser_item;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final WorkShopMobileUser entity, int position) {
        final CheckBox checkBox = holder.obtainView(R.id.checkBox);
        TextView tv_name = holder.obtainView(R.id.tv_name);
        TextView tv_score = holder.obtainView(R.id.tv_score);
        TextView tv_evaluate = holder.obtainView(R.id.tv_evaluate);
        TextView tv_creator = holder.obtainView(R.id.tv_creator);
        if (isEdit) {
            checkBox.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.GONE);
        }
        if (entity.getmUser() != null) {
            tv_name.setText(entity.getmUser().getRealName());
        } else {
            tv_name.setText("");
        }
        tv_score.setText(String.valueOf((int) entity.getPoint()));
        int textColor;
        String finallyResult;
        if (entity.getFinallyResult() != null && entity.getFinallyResult().equals("excellent")) {
            textColor = ContextCompat.getColor(context, R.color.darkorange);
            finallyResult = "优秀";
        } else if (entity.getFinallyResult() != null && entity.getFinallyResult().equals("qualified")) {
            textColor = ContextCompat.getColor(context, R.color.mediumseagreen);
            finallyResult = "合格";
        } else if (entity.getFinallyResult() != null && entity.getFinallyResult().equals("fail")) {
            textColor = ContextCompat.getColor(context, R.color.pink);
            finallyResult = "未达标";
        } else {
            textColor = ContextCompat.getColor(context, R.color.skyblue);
            finallyResult = "待评价";
        }
        tv_evaluate.setTextColor(textColor);
        tv_evaluate.setText(finallyResult);
        if (entity.getEvaluateCreator() != null && entity.getEvaluateCreator().getRealName() != null) {
            tv_creator.setText(entity.getEvaluateCreator().getRealName());
            tv_creator.setVisibility(View.VISIBLE);
        } else {
            tv_creator.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEdit) {
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false);
                    } else {
                        checkBox.setChecked(true);
                    }
                }
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (!mSelects.contains(entity)) {
                        mSelects.add(entity);
                    }
                } else {
                    mSelects.remove(entity);
                }
                if (onItemSelectListener != null) {
                    onItemSelectListener.onItemSelect(mSelects);
                }
            }
        });
        checkBox.setChecked(mSelects.contains(entity));
    }

    public interface OnItemSelectListener {
        void onItemSelect(List<WorkShopMobileUser> mSelects);
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        this.onItemSelectListener = onItemSelectListener;
    }

    public List<WorkShopMobileUser> getmSelects() {
        return mSelects;
    }
}
