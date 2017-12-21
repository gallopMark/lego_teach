package com.haoyu.app.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.WorkShopMobileUser;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.teach.R;

import java.util.List;

/**
 * 创建日期：2017/12/15.
 * 描述:
 * 作者:xiaoma
 */

public class WSMemberAdapter extends BaseArrayRecyclerAdapter<WorkShopMobileUser> {
    private Context context;
    private OnItemDeleteListener onItemDeleteListener;

    public WSMemberAdapter(Context context, List<WorkShopMobileUser> mDatas) {
        super(mDatas);
        this.context = context;
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.wsmanagermeb_item;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final WorkShopMobileUser entity, final int position) {
        ImageView iv_ico = holder.obtainView(R.id.iv_ico);
        TextView tv_realName = holder.obtainView(R.id.tv_realName);
        TextView tv_deptName = holder.obtainView(R.id.tv_deptName);
//        Button bt_delete = holder.obtainView(R.id.bt_delete);
        if (entity.getmUser() != null && entity.getmUser().getAvatar() != null) {
            String avatar = entity.getmUser().getAvatar();
            GlideImgManager.loadCircleImage(context, avatar, R.drawable.user_default, R.drawable.user_default, iv_ico);
        } else {
            iv_ico.setImageResource(R.drawable.user_default);
        }
        if (entity.getmUser() != null) {
            tv_realName.setText(entity.getmUser().getRealName());
            if (!TextUtils.isEmpty(entity.getmUser().getDeptName())) {
                tv_deptName.setVisibility(View.VISIBLE);
                tv_deptName.setText(entity.getmUser().getDeptName());
            } else {
                tv_deptName.setVisibility(View.GONE);
            }
        } else {
            tv_realName.setText("");
            tv_deptName.setVisibility(View.GONE);
        }
//        bt_delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (onItemDeleteListener != null) {
//                    onItemDeleteListener.onItemDelete(entity, position);
//                }
//            }
//        });
    }

    public interface OnItemDeleteListener {
        void onItemDelete(WorkShopMobileUser entity, int position);
    }

    public void setOnItemDeleteListener(OnItemDeleteListener onItemDeleteListener) {
        this.onItemDeleteListener = onItemDeleteListener;
    }
}
