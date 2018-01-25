package com.haoyu.app.adapter

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.entity.WorkShopMobileEntity
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.teach.R
import com.haoyu.app.utils.PixelFormat
import com.haoyu.app.utils.ScreenUtils

/**
 * 创建日期：2018/1/24.
 * 描述:我参与管理的工作坊  首页
 * 作者:xiaoma
 */
class ManageWSAdapter(private val context: Context, mDatas: List<WorkShopMobileEntity>) : BaseArrayRecyclerAdapter<WorkShopMobileEntity>(mDatas) {

    private var imageWidth = 0
    private var imageHeight = 0

    init {
        imageWidth = ScreenUtils.getScreenWidth(context) / 3 - PixelFormat.formatPxToDip(context, 20)
        imageHeight = imageWidth / 3 * 2
    }

    override fun bindView(viewtype: Int): Int {
        return R.layout.managews_item
    }

    override fun onBindHoder(holder: RecyclerHolder, entity: WorkShopMobileEntity, position: Int) {
        val ivImage = holder.obtainView<ImageView>(R.id.ivImage)
        ivImage.layoutParams = LinearLayout.LayoutParams(imageWidth, imageHeight)
        val tvTitle = holder.obtainView<TextView>(R.id.tvTitle)
        val tvTrainName = holder.obtainView<TextView>(R.id.tvTrainName)
        GlideImgManager.loadImage(context, entity.imageUrl, R.drawable.app_default, R.drawable.app_default, ivImage)
        tvTitle.text = entity.title
        tvTrainName.text = entity.trainName
    }
}