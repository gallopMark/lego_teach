package com.haoyu.app.adapter;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.BriefingEntity;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.swipe.SwipeMenuLayout;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2017/1/5 on 14:31
 * 描述: 工作坊简报适配器
 * 作者:马飞奔 Administrator
 */
public class BriefingAdapter extends BaseArrayRecyclerAdapter<BriefingEntity> {
    private onDisposeCallBack disposeCallBack;
    private boolean swipeEnable;

    public BriefingAdapter(List<BriefingEntity> mDatas, boolean swipeEnable) {
        super(mDatas);
        this.swipeEnable = swipeEnable;
    }

    public void setDisposeCallBack(onDisposeCallBack disposeCallBack) {
        this.disposeCallBack = disposeCallBack;
    }

    @Override
    public void onBindHoder(final RecyclerHolder holder, final BriefingEntity entity, final int position) {
        final SwipeMenuLayout swipeLayout = holder.obtainView(R.id.swipeLayout);
        LinearLayout ll_content = holder.obtainView(R.id.ll_content);
        TextView tv_title = holder.obtainView(R.id.tv_title);
        TextView tv_time = holder.obtainView(R.id.tv_time);
        tv_title.setText(entity.getTitle());
        tv_time.setText(TimeUtil.getSlashDate(entity.getCreateTime()));
        swipeLayout.setSwipeEnable(swipeEnable);
        TextView tv_alter = holder.obtainView(R.id.tv_alter);
        TextView tv_delete = holder.obtainView(R.id.tv_delete);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.ll_content:
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(BriefingAdapter.this, holder, holder.itemView, holder.getAdapterPosition());
                        }
                        break;
                    case R.id.tv_alter:
                        swipeLayout.quickClose();
                        if (disposeCallBack != null) {
                            disposeCallBack.onAlter(position, entity);
                        }
                        break;
                    case R.id.tv_delete:
                        swipeLayout.quickClose();
                        if (disposeCallBack != null) {
                            disposeCallBack.onDelete(position, entity);
                        }
                        break;
                }
            }
        };
        ll_content.setOnClickListener(listener);
        tv_alter.setOnClickListener(listener);
        tv_delete.setOnClickListener(listener);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.brief_item;
    }

    public interface onDisposeCallBack {
        void onAlter(int position, BriefingEntity entity);

        void onDelete(int position, BriefingEntity entity);
    }
}
