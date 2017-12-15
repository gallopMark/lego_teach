package com.haoyu.app.adapter;

import android.app.Activity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter;
import com.haoyu.app.entity.WorkShopMobileEntity;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.teach.R;
import com.haoyu.app.utils.PixelFormat;
import com.haoyu.app.utils.ScreenUtils;

import java.util.List;

/**
 * 创建日期：2017/2/4 on 16:15
 * 描述:我参与管理的工作坊  首页
 * 作者:马飞奔 Administrator
 */
public class ManageWSAdapter extends BaseArrayRecyclerAdapter<WorkShopMobileEntity> {
    private Activity context;
    private int imageWidth;
    private int imageHeight;

    public ManageWSAdapter(Activity context, List<WorkShopMobileEntity> mDatas) {
        super(mDatas);
        this.context = context;
        imageWidth = ScreenUtils.getScreenWidth(context) / 3 - PixelFormat.formatPxToDip(context, 20);
        imageHeight = imageWidth / 3 * 2;
    }

    @Override
    public void onBindHoder(RecyclerHolder holder, final WorkShopMobileEntity entity, int position) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                imageWidth, imageHeight);
        ImageView workshop_img = holder.obtainView(R.id.workshop_img);
        workshop_img.setLayoutParams(params);
        TextView workshop_title = holder.obtainView(R.id.workshop_title);
        TextView workshop_trainName = holder.obtainView(R.id.workshop_trainName);
        GlideImgManager.loadImage(context, entity.getImageUrl(), R.drawable.app_default, R.drawable.app_default, workshop_img);
        workshop_title.setText(entity.getTitle());
        workshop_trainName.setText(entity.getTrainName());
    }

    @Override
    public int bindView(int viewtype) {
        return R.layout.managews_item;
    }
}
