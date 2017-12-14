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
        TextView tv_finallyResult = holder.obtainView(R.id.tv_finallyResult);
        if (isEdit) {
            checkBox.setVisibility(View.VISIBLE);
            tv_finallyResult.setVisibility(View.GONE);
        } else {
            checkBox.setVisibility(View.GONE);
            tv_finallyResult.setVisibility(View.VISIBLE);
        }
        if (entity.getmUser() != null) {
            tv_name.setText(entity.getmUser().getRealName());
        } else {
            tv_name.setText("");
        }
        tv_score.setText(String.valueOf((int) entity.getPoint()));
        int color_evaluate, color_finally;
        String evaluate, finallyResult;
        if (entity.getEvaluate() != null && entity.getEvaluate().equals("excellent")) {
            color_evaluate = ContextCompat.getColor(context, R.color.darkorange);
            evaluate = "优秀";
        } else if (entity.getEvaluate() != null && entity.getEvaluate().equals("qualified")) {
            color_evaluate = ContextCompat.getColor(context, R.color.mediumseagreen);
            evaluate = "合格";
        } else if (entity.getEvaluate() != null && entity.getEvaluate().equals("fail")) {
            color_evaluate = ContextCompat.getColor(context, R.color.pink);
            evaluate = "未达标";
        } else {
            color_evaluate = ContextCompat.getColor(context, R.color.skyblue);
            evaluate = "待评价";
        }
        tv_evaluate.setTextColor(color_evaluate);
        tv_evaluate.setText(evaluate);
        if (entity.getEvaluateCreator() != null && entity.getEvaluateCreator().getRealName() != null) {
            tv_creator.setText("评估人：" + entity.getEvaluateCreator().getRealName());
            tv_creator.setVisibility(View.VISIBLE);
        } else {
            tv_creator.setVisibility(View.GONE);
        }
        if (entity.getFinallyResult() != null && entity.getFinallyResult().equals("excellent")) {
            color_finally = ContextCompat.getColor(context, R.color.darkorange);
            finallyResult = "优秀";
        } else if (entity.getFinallyResult() != null && entity.getFinallyResult().equals("qualified")) {
            color_finally = ContextCompat.getColor(context, R.color.mediumseagreen);
            finallyResult = "合格";
        } else if (entity.getFinallyResult() != null && entity.getFinallyResult().equals("fail")) {
            color_finally = ContextCompat.getColor(context, R.color.pink);
            finallyResult = "未达标";
        } else {
            color_finally = ContextCompat.getColor(context, R.color.skyblue);
            finallyResult = "－－";
        }
        tv_finallyResult.setTextColor(color_finally);
        tv_finallyResult.setText(finallyResult);
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
