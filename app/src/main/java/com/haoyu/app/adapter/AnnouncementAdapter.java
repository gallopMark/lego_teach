package com.haoyu.app.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.Announcement;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.swipe.SwipeMenuLayout;
import com.haoyu.app.utils.TimeUtil;

import java.util.List;

/**
 * 创建日期：2017/1/5 on 17:36
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AnnouncementAdapter extends BaseArrayRecyclerAdapter<Announcement> {
    private boolean isTeacher;
    private onItemClickCallBack itemClickCallBack;
    private onDisposeCallBack onDisposeCallBack;

    public AnnouncementAdapter(List<Announcement> mDatas) {
        super(mDatas);
    }

    public AnnouncementAdapter(List<Announcement> mDatas, boolean isTeacher) {
        super(mDatas);
        this.isTeacher = isTeacher;
    }

    public void setItemClickCallBack(onItemClickCallBack itemClickCallBack) {
        this.itemClickCallBack = itemClickCallBack;
    }

    public void setOnDisposeCallBack(AnnouncementAdapter.onDisposeCallBack onDisposeCallBack) {
        this.onDisposeCallBack = onDisposeCallBack;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final Announcement entity, final int position) {
        final SwipeMenuLayout swipeLayout = holder.obtainView(R.id.swipeLayout);
        View item_layout = holder.obtainView(R.id.item_layout);
        ImageView ic_consult = holder.obtainView(R.id.ic_consult);
        TextView tv_title = holder.obtainView(R.id.tv_title);
        TextView tv_createDate = holder.obtainView(R.id.tv_createDate);
        Button bt_alter = holder.obtainView(R.id.bt_alter);
        Button bt_delete = holder.obtainView(R.id.bt_delete);
        if (isTeacher) {
            swipeLayout.setSwipeEnable(true);
        } else {
            swipeLayout.setSwipeEnable(false);
        }
        swipeLayout.setIos(true);
        if (entity.isHadView()) {
            ic_consult.setVisibility(View.GONE);
        } else {
            ic_consult.setVisibility(View.VISIBLE);
        }
        tv_title.setText(entity.getTitle());
        tv_createDate.setText(TimeUtil.getSlashDate(entity.getCreateTime()));
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.item_layout:
                        if (swipeLayout.isExpand()) {
                            swipeLayout.smoothClose();
                        } else {
                            if (itemClickCallBack != null) {
                                itemClickCallBack.onItemCallBack(position);
                            }
                        }
                        break;
                    case R.id.bt_alter:
                        swipeLayout.smoothClose();
                        if (onDisposeCallBack != null) {
                            onDisposeCallBack.onAlter(position, entity);
                        }
                        break;
                    case R.id.bt_delete:
                        swipeLayout.smoothClose();
                        if (onDisposeCallBack != null) {
                            onDisposeCallBack.onDelete(position, entity);
                        }
                        break;
                }
            }
        };
        item_layout.setOnClickListener(listener);
        bt_alter.setOnClickListener(listener);
        bt_delete.setOnClickListener(listener);
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.announcements_item;
    }
    public interface onItemClickCallBack {
        void onItemCallBack(int position);
    }

    public interface onDisposeCallBack {
        void onAlter(int position, Announcement entity);

        void onDelete(int position, Announcement entity);
    }
}
