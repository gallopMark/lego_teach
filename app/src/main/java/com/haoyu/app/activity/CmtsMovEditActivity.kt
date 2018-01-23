package com.haoyu.app.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.dialog.DateTimePickerDialog
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.FileUploadResult
import com.haoyu.app.entity.MFileInfo
import com.haoyu.app.entity.MobileUser
import com.haoyu.app.lego.teach.R
import com.haoyu.app.pickerlib.MediaOption
import com.haoyu.app.pickerlib.MediaPicker
import com.haoyu.app.utils.*
import com.haoyu.app.view.AppToolBar
import com.haoyu.app.view.RoundRectProgressBar
import io.reactivex.disposables.Disposable
import okhttp3.Request
import java.io.File
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 * 创建日期：2018/1/23.
 * 描述:教研发起活动
 * 作者:马飞奔 Administrator
 * “communicationMeeting”：跨校交流会
 * “expertInteraction”：专家互动
 * “lessonViewing”：创课观摩
 * “ticket”：须报名预约，凭电子票入场
 * “free”：在线报名，免费入场
 * “chair”:讲座视频录像+在线问答交流
 */
class CmtsMovEditActivity : BaseActivity(), View.OnClickListener {
    private val context = this
    private lateinit var toolBar: AppToolBar
    private lateinit var scrollview: NestedScrollView
    /** */
    private lateinit var llAddImage: LinearLayout
    private lateinit var flImage: FrameLayout
    private lateinit var ivImage: ImageView  //显示封面图片
    private lateinit var llProgress: LinearLayout  //图片上传进度布局
    private lateinit var tvImageName: TextView  //图片名称
    private lateinit var progressBar: RoundRectProgressBar  //图片上传进度条
    private lateinit var tvProgress: TextView   //显示图片上传百分比
    private lateinit var tvError: TextView  //上传失败
    private lateinit var ivDelete: ImageView  //取消上传或删除图片
    /** */
    private lateinit var tvType: TextView   //活动类型
    private val editTexts = arrayOfNulls<EditText>(5) //标题、活动类型、活动内容、主办单位、票数
    private lateinit var llBeginTime: LinearLayout
    private lateinit var tvBeginTime: TextView
    private lateinit var llEndTime: LinearLayout
    private lateinit var tvEndTime: TextView
    private lateinit var radioGroup: RadioGroup   //参与方式
    private lateinit var llTicket: LinearLayout
    private lateinit var llPartTime: LinearLayout //报名截止时间
    private lateinit var tvPartTime: TextView
    private lateinit var llPartUser: LinearLayout
    private lateinit var tvPartUser: TextView
    private var imageInfo: MFileInfo? = null
    private var isUploading = false  //是否正在上传
    private var disposable: Disposable? = null
    private lateinit var movType: String // 活动类型
    private lateinit var partType: String // 参与方式
    private val movTypeList = ArrayList<String>()
    private lateinit var moveTypeArr: Array<String>
    private lateinit var partTypeArr: Array<String>
    private var movTypeSelect = -1
    private var isTicket = false //是否是电子票入场方式
    private val years = intArrayOf(0, 0, 0)
    private val months = intArrayOf(0, 0, 0)
    private val days = intArrayOf(0, 0, 0)
    private val hours = intArrayOf(0, 0)
    private val minutes = intArrayOf(0, 0)
    private val selectUsers = ArrayList<MobileUser>()

    override fun setLayoutResID(): Int {
        return R.layout.activity_cmts_createmov
    }

    override fun initView() {
        findViews()
        val imageLayout = findViewById<FrameLayout>(R.id.fl_imageLayout)
        val params = imageLayout.layoutParams
        params.height = ScreenUtils.getScreenHeight(context) / 4
        imageLayout.layoutParams = params
        initMovType()
        partTypeArr = arrayOf("ticket", "free", "chair")
        partType = partTypeArr[0]
        isTicket = true
    }

    private fun findViews() {
        toolBar = findViewById(R.id.toolBar)
        scrollview = findViewById(R.id.scrollview)
        llAddImage = findViewById(R.id.ll_addImage)
        flImage = findViewById(R.id.fl_image)
        ivImage = findViewById(R.id.iv_image)
        llProgress = findViewById(R.id.ll_progress)
        tvImageName = findViewById(R.id.tv_imageName)
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tv_progress)
        tvError = findViewById(R.id.tv_error)
        ivDelete = findViewById(R.id.iv_delete)
        editTexts[0] = findViewById(R.id.et_title)
        tvType = findViewById(R.id.tv_type)
        editTexts[1] = findViewById(R.id.et_content)
        editTexts[2] = findViewById(R.id.et_host)
        editTexts[3] = findViewById(R.id.et_address)
        llBeginTime = findViewById(R.id.ll_beginTime)
        tvBeginTime = findViewById(R.id.tv_beginTime)
        llEndTime = findViewById(R.id.ll_endTime)
        tvEndTime = findViewById(R.id.tv_endTime)
        radioGroup = findViewById(R.id.radioGroup)
        llTicket = findViewById(R.id.ll_ticket)
        editTexts[4] = findViewById(R.id.et_ticketNum)
        llPartTime = findViewById(R.id.ll_partTime)
        tvPartTime = findViewById(R.id.tv_partTime)
        llPartUser = findViewById(R.id.ll_partUser)
        tvPartUser = findViewById(R.id.tv_partUser)
    }

    private fun initMovType() {
        moveTypeArr = arrayOf("communicationMeeting", "expertInteraction", "lessonViewing")
        movType = moveTypeArr[0]
        movTypeSelect = 0
        val communicationMeeting = resources.getString(R.string.teach_active_communicationMeeting)
        tvType.text = communicationMeeting
        val expertInteraction = resources.getString(R.string.teach_active_expertInteraction)
        val lessonViewing = resources.getString(R.string.teach_active_lessonViewing)
        movTypeList.add(communicationMeeting)
        movTypeList.add(expertInteraction)
        movTypeList.add(lessonViewing)
    }

    override fun setListener() {
        toolBar.setOnTitleClickListener(object : AppToolBar.TitleOnClickListener {
            override fun onLeftClick(view: View) {
                finish()
            }

            override fun onRightClick(view: View) {
                if (checkOut()) {
                    commit()
                }
            }
        })
        llAddImage.setOnClickListener(context)
        ivDelete.setOnClickListener(context)
        tvType.setOnClickListener(context)
        llBeginTime.setOnClickListener(context)
        llEndTime.setOnClickListener(context)
        llPartTime.setOnClickListener(context)
        llPartUser.setOnClickListener(context)
        radioGroup.setOnCheckedChangeListener { _, checkId ->
            when (checkId) {
                R.id.rb_ticket -> {
                    isTicket = true
                    partType = partTypeArr[0]
                }
                R.id.rb_free -> {
                    isTicket = false
                    partType = partTypeArr[1]
                }
                R.id.rb_chair -> {
                    isTicket = false
                    partType = partTypeArr[2]
                }
            }
            if (isTicket) {
                llTicket.visibility = View.VISIBLE
            } else {
                llTicket.visibility = View.GONE
            }
            scrollview.postDelayed({ scrollview.fullScroll(ScrollView.FOCUS_DOWN) }, 10)
        }
    }

    override fun onClick(view: View) {
        Common.hideSoftInput(context)
        when (view.id) {
            R.id.ll_addImage -> pickerPicture()
            R.id.iv_delete -> deleteVideo()
            R.id.tv_type -> showTypeWindow()
            R.id.ll_beginTime -> setTimeDialog(tvBeginTime, 1)
            R.id.ll_endTime -> setTimeDialog(tvEndTime, 2)
            R.id.ll_partTime -> setTimeDialog(tvPartTime, 3)
            R.id.ll_partUser -> {
                val intent = Intent(context, MultiSearchUsersActivity::class.java)
                intent.putExtra("users", selectUsers as Serializable)
                startActivityForResult(intent, 1)
                overridePendingTransition(0, 0)//用于屏蔽 activity 默认的转场动画效果
            }
        }
    }

    private fun pickerPicture() {
        val option = MediaOption.Builder().setSelectType(MediaOption.TYPE_IMAGE).setShowCamera(true).build()
        MediaPicker.getInstance().init(option).selectMedia(context, object : MediaPicker.onSelectMediaCallBack() {
            override fun onSelected(path: String) {
                setImageFile(path)
            }
        })
    }

    private fun setImageFile(path: String) {
        val image = File(path)
        llAddImage.visibility = View.GONE
        flImage.visibility = View.VISIBLE
        Glide.with(context).load(path).centerCrop().into(ivImage)
        tvImageName.text = image.name
        uploadImage(image)
    }

    private fun uploadImage(image: File) {
        val url = Constants.OUTRT_NET + "/m/file/uploadFileInfoRemote"
        disposable = OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<FileUploadResult>() {
            override fun onBefore(request: Request) {
                isUploading = true
                if (llProgress.visibility != View.VISIBLE) {
                    llProgress.visibility = View.VISIBLE
                }
                if (tvError.visibility != View.GONE) {
                    tvError.visibility = View.GONE
                }
            }

            override fun onError(request: Request, e: Exception) {
                setError(image)
            }

            override fun onResponse(response: FileUploadResult?) {
                isUploading = false
                handler.removeMessages(codeImage)
                if (response?.responseData != null) {
                    imageInfo = response.responseData
                    tvProgress.text = "已上传"
                }
            }
        }, image, image.name, OkHttpClientManager.ProgressListener { totalBytes, remainingBytes, _, _ ->
            val msg = handler.obtainMessage(codeImage)
            val bundle = Bundle()
            bundle.putLong("totalBytes", totalBytes)
            bundle.putLong("remainingBytes", remainingBytes)
            msg.data = bundle
            handler.sendMessage(msg)
        })
    }

    private val codeImage = 1
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            val bundle = msg.data
            val totalBytes = bundle.getLong("totalBytes")
            val remainingBytes = bundle.getLong("remainingBytes")
            progressBar.max = totalBytes.toInt()
            progressBar.progress = (totalBytes - remainingBytes).toInt()
            val progress = (totalBytes - remainingBytes) * 100 / totalBytes
            tvProgress.text = "上传中\u2000$progress%"
        }
    }

    private fun setError(image: File) {
        isUploading = false
        llProgress.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        disposable?.dispose()
        handler.removeMessages(codeImage)
        tvError.setOnClickListener({ uploadImage(image) })
    }

    private fun deleteVideo() {
        val dialog = MaterialDialog(context)
        dialog.setTitle("提示")
        dialog.setMessage("您确定删除封面吗？")
        dialog.setPositiveButton("确定") { _, _ ->
            isUploading = false
            imageInfo = null
            ivImage.setImageResource(0)
            flImage.visibility = View.GONE
            llAddImage.visibility = View.VISIBLE
            disposable?.dispose()
            handler.removeMessages(codeImage)
        }
        dialog.setNegativeButton("取消", null)
        dialog.show()
    }

    private fun showTypeWindow() {
        val shouqi = ContextCompat.getDrawable(context, R.drawable.course_dictionary_shouqi)
        shouqi?.setBounds(0, 0, shouqi.minimumWidth, shouqi.minimumHeight)
        val zhankai = ContextCompat.getDrawable(context, R.drawable.course_dictionary_xiala)
        zhankai?.setBounds(0, 0, zhankai.minimumWidth, zhankai.minimumHeight)
        tvType.setCompoundDrawables(null, null, shouqi, null)
        val recyclerView = RecyclerView(context)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        val popupWindow = PopupWindow(recyclerView, tvType.width, LinearLayout.LayoutParams.WRAP_CONTENT)
        val adapter = TypeAdapter(movTypeList, movTypeSelect)
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener({ _, _, _, position ->
            movTypeSelect = position
            tvType.text = movTypeList[position]
            movType = when (movTypeSelect) {
                0 -> moveTypeArr[0]
                1 -> moveTypeArr[1]
                else -> moveTypeArr[2]
            }
            popupWindow.dismiss()
        })
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable())
        popupWindow.isOutsideTouchable = true
        popupWindow.setOnDismissListener { tvType.setCompoundDrawables(null, null, zhankai, null) }
        popupWindow.showAsDropDown(tvType)
    }

    private inner class TypeAdapter(mDatas: List<String>, private val selectItem: Int) : BaseArrayRecyclerAdapter<String>(mDatas) {
        override fun bindView(viewtype: Int): Int {
            return R.layout.popupwindow_dictionary_item
        }

        override fun onBindHoder(holder: RecyclerHolder, content: String?, position: Int) {
            val tvDict = holder.obtainView<TextView>(R.id.tvDict)
            tvDict.text = content
            val select = ContextCompat.getDrawable(context, R.drawable.train_item_selected)
            select?.setBounds(0, 0, select.minimumWidth, select.minimumHeight)
            if (selectItem == position) {
                tvDict.setCompoundDrawables(null, null, select, null)
                tvDict.compoundDrawablePadding = PixelFormat.dp2px(context, 10f)
            } else {
                tvDict.setCompoundDrawables(null, null, null, null)
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val users = data.getSerializableExtra("users") as ArrayList<MobileUser>
            if (users.isNotEmpty()) {
                selectUsers.clear()
                selectUsers.addAll(users)
                setUserText()
            }
        }
    }

    private fun setUserText() {
        val sb = StringBuilder()
        for (i in selectUsers.indices) {
            val name = selectUsers[i].realName
            sb.append(name)
            if (i < selectUsers.size - 1) {
                sb.append("，")
            }
        }
        tvPartUser.text = sb
        if (selectUsers.size > 4) {
            sb.append(" 等人")
            tvPartUser.text = sb
            tvPartUser.ellipsize = TextUtils.TruncateAt.MIDDLE
        }
    }

    private fun setTimeDialog(tv: TextView, type: Int) {
        val dialog = DateTimePickerDialog(context, true)
        when (type) {
            1 -> dialog.setTitle("活动开始时间")
            2 -> dialog.setTitle("活动结束时间")
            else -> dialog.setTitle("报名截止时间")
        }
        dialog.setPositiveButton("确定")
        dialog.setDateListener(object : DateTimePickerDialog.DateListener {
            private val calendar = Calendar.getInstance()

            override fun date(year: Int, month: Int, day: Int) {
                when (type) {
                    1 -> {
                        years[0] = year
                        months[0] = month
                        days[0] = day
                    }
                    2 -> {
                        years[1] = year
                        months[1] = month
                        days[1] = day
                    }
                    else -> {
                        years[2] = year
                        months[2] = month
                        days[2] = day
                    }
                }
            }

            override fun time(hour: Int, minute: Int) {
                when (type) {
                    1 -> {
                        hours[0] = hour
                        minutes[0] = minute
                        if (checkStartTime()) {
                            calendar.set(Calendar.YEAR, years[0])
                            calendar.set(Calendar.MONTH, months[0] - 1)
                            calendar.set(Calendar.DAY_OF_MONTH, days[0])
                            calendar.set(Calendar.HOUR_OF_DAY, hours[0])
                            calendar.set(Calendar.MINUTE, minutes[0])
                            val text = TimeUtil.convertToTime(calendar.time.time)
                            tv.text = text
                        }
                    }
                    2 -> {
                        hours[1] = hour
                        minutes[1] = minute
                        if (checkEndTime()) {
                            calendar.set(Calendar.YEAR, years[1])
                            calendar.set(Calendar.MONTH, months[1] - 1)
                            calendar.set(Calendar.DAY_OF_MONTH, days[1])
                            calendar.set(Calendar.HOUR_OF_DAY, hours[1])
                            calendar.set(Calendar.MINUTE, minutes[1])
                            val text = TimeUtil.convertToTime(calendar.time.time)
                            tv.text = text
                        }
                    }
                    else -> {
                        if(checkPartTime()){
                            calendar.set(Calendar.YEAR, years[2])
                            calendar.set(Calendar.MONTH, months[2] - 1)
                            calendar.set(Calendar.DAY_OF_MONTH, days[2])
                            val text = TimeUtil.convertToTime(calendar.time.time)
                            tv.text = text
                        }
                    }
                }
            }
        })
        dialog.show()
    }

    private fun checkStartTime(): Boolean {
        val c = Calendar.getInstance()
        val nowYear = c.get(Calendar.YEAR)
        val nowMonth = c.get(Calendar.MONTH) + 1
        val nowDay = c.get(Calendar.DAY_OF_MONTH)
        var message = "活动开始时间不能是"
        if (years[0] < nowYear) {
            message += nowYear.toString() + "年前"
            showMaterialDialog("提示", message)
            return false
        } else if (years[0] == nowYear) {
            if (months[0] < nowMonth) {
                message += nowYear.toString() + "年" + nowMonth + "月前"
                showMaterialDialog("提示", message)
                return false
            } else {
                if (days[0] < nowDay) {
                    message += nowYear.toString() + "年" + nowMonth + "月" + nowDay + "日前"
                    showMaterialDialog("提示", message)
                    return false
                }
            }
        }
        return true
    }

    private fun checkEndTime(): Boolean {
        val startTime = tvBeginTime.text.toString()
        if (TextUtils.isEmpty(startTime)) {
            showMaterialDialog("提示", "请先选择活动开始时间")
            return false
        }
        var message = "活动结束时间不能是"
        if (years[1] < years[0]) {
            message += years[0].toString() + "年前"
            showMaterialDialog("提示", message)
            return false
        } else if (years[1] == years[0]) {
            if (months[1] < months[0]) {
                message += years[0].toString() + "年" + months[0] + "月前"
                showMaterialDialog("提示", message)
                return false
            } else if (months[1] == months[0]) {
                if (days[1] < days[0]) {
                    message += years[0].toString() + "年" + months[0] + "月" + days[0] + "日前"
                    showMaterialDialog("提示", message)
                    return false
                } else if (days[1] == days[0]) {
                    if (hours[1] < hours[0]) {
                        showMaterialDialog("提示", "活动时间结束必须大于活动开始时间")
                        return false
                    } else if (hours[1] == hours[0]) {
                        if (minutes[1] < minutes[0]) {
                            showMaterialDialog("提示", "活动时间结束必须大于活动开始时间")
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    private fun checkPartTime(): Boolean {
        val c = Calendar.getInstance()
        val nowYear = c.get(Calendar.YEAR)
        val nowMonth = c.get(Calendar.MONTH) + 1
        val nowDay = c.get(Calendar.DAY_OF_MONTH)
        var message = "活动开始时间不能是"
        if (years[2] < nowYear) {
            message += nowYear.toString() + "年前"
            showMaterialDialog("提示", message)
            return false
        } else if (years[2] == nowYear) {
            if (days[2] < nowMonth) {
                message += nowYear.toString() + "年" + nowMonth + "月前"
                showMaterialDialog("提示", message)
                return false
            } else {
                if (days[2] < nowDay) {
                    message += nowYear.toString() + "年" + nowMonth + "月" + nowDay + "日前"
                    showMaterialDialog("提示", message)
                    return false
                }
            }
        }
        return true
    }

    private fun checkOut(): Boolean {
        var tipMsg: String? = null
        val title = editTexts[0]?.text.toString().trim()
        val content = editTexts[1]?.text.toString().trim()
        val host = editTexts[2]?.text.toString().trim()
        val address = editTexts[3]?.text.toString().trim()
        val startTime = tvBeginTime.text.toString().trim()
        val endTime = tvEndTime.text.toString().trim()
        when {
            isUploading -> tipMsg = "请等待封面图片上传完毕"
            TextUtils.isEmpty(title) -> tipMsg = "请输入活动标题"
            TextUtils.isEmpty(movType) -> tipMsg = "请选择活动类型"
            TextUtils.isEmpty(content) -> tipMsg = "请输入活动内容"
            TextUtils.isEmpty(address) -> tipMsg = "请输入活动地点"
            TextUtils.isEmpty(host) -> tipMsg = "请输入主办单位"
            TextUtils.isEmpty(startTime) -> tipMsg = "请选择活动开始时间"
            TextUtils.isEmpty(endTime) -> tipMsg = "请选择活动结束时间"
            TextUtils.isEmpty(partType) -> tipMsg = "请选择参与方式"
        }
        if (tipMsg != null) {
            showMaterialDialog("提示", tipMsg)
            return false
        }
        return true
    }

    private fun commit() {
        val title = editTexts[0]?.text.toString()
        val content = editTexts[1]?.text.toString()
        val sponsor = editTexts[2]?.text.toString()
        val location = editTexts[3]?.text.toString()
        val startTime = tvBeginTime.text.toString()
        val endTime = tvEndTime.text.toString()
        val url = "${Constants.OUTRT_NET}/m/movement"
        val map = HashMap<String, String>().apply {
            put("title", title)
            put("type", movType)
            put("content", content)
            put("sponsor", sponsor)
            put("location", location)
            put("movementRelations[0].startTime", startTime)
            put("movementRelations[0].endTime", endTime)
            put("participationType", partType)
            put("status", "verifying")
        }
        imageInfo?.let {
            map.put("image", it.url)
        }
        if (isTicket) {
            val endPartTime = tvPartTime.text.toString()
            val ticketNum = editTexts[4]?.text.toString()
            if (!TextUtils.isEmpty(endPartTime)) {
                map.put("movementRelations[0].registerEndTime", endPartTime)
            }
            if (!TextUtils.isEmpty(ticketNum)) {
                map.put("movementRelations[0].ticketNum", ticketNum)
            }
        }
        if (selectUsers.size > 0) {
            for (i in selectUsers.indices) {
                map.put("movementRegisters[$i].userInfo.id", selectUsers[i].id)
                map.put("movementRegisters[$i].state", "passive")
            }
        }
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError(context)
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                hideTipDialog()
                if (response?.getResponseCode() != null && response.getResponseCode() == "00") {
                    toastFullScreen("发表成功", true)
                    finish()
                } else {
                    toastFullScreen("发表失败", false)
                }
            }
        }, map))
    }

    override fun onDestroy() {
        disposable?.isDisposed
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}