package com.haoyu.app.adapter

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.entity.CourseMobileEntity
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.teach.R
import com.haoyu.app.utils.PixelFormat
import com.haoyu.app.utils.ScreenUtils
import com.haoyu.app.utils.TimeUtil

/**
 * 创建日期：2018/1/24.
 * 描述:参与辅导的课程  首页课程列表
 * 作者:xiaoma
 */
class CoachCourseAdapter(private val context: Context, mDatas: List<CourseMobileEntity>) : BaseArrayRecyclerAdapter<CourseMobileEntity>(mDatas) {
    private var imageWidth = 0
    private var imageHeight = 0

    init {
        imageWidth = ScreenUtils.getScreenWidth(context) / 3 - PixelFormat.formatPxToDip(context, 20)
        imageHeight = imageWidth / 3 * 2
    }

    override fun bindView(viewtype: Int): Int {
        return R.layout.coachcourse_item
    }

    override fun onBindHoder(holder: RecyclerHolder, entity: CourseMobileEntity, position: Int) {
        val ivImage = holder.obtainView<ImageView>(R.id.ivImage)
        val tvTitle = holder.obtainView<TextView>(R.id.tvTitle)
        val tvCode = holder.obtainView<TextView>(R.id.tvCode)
        val tvPeriod = holder.obtainView<TextView>(R.id.tvPeriod)
        val tvState = holder.obtainView<TextView>(R.id.tvState)
        val line = holder.obtainView<View>(R.id.line)
        ivImage.layoutParams = LinearLayout.LayoutParams(imageWidth, imageHeight)
        GlideImgManager.loadImage(context, entity.image, R.drawable.app_default, R.drawable.app_default, ivImage)
        tvTitle.text = entity.title
        var code = ""
        entity.code?.let { code += it }
        entity.termNo?.let { code += "/$it" }
        tvCode.text = code
        var textPeriod = "开课："
        val state = if (entity.getmTimePeriod() != null) {
            entity.getmTimePeriod().let {
                textPeriod += TimeUtil.getSlashDate(it.startTime)
                if (it.state != null) {
                    it.state
                } else {
                    if (it.minutes > 0) "进行中" else ""
                }
            }
        } else "进行中"
        tvPeriod.text = textPeriod
        tvState.text = state
        if (position == itemCount - 1) {
            line.visibility = View.GONE
        } else {
            line.visibility = View.VISIBLE
        }
    }
}