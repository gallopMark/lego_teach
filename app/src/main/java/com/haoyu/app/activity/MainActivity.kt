package com.haoyu.app.activity

import android.content.Intent
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.haoyu.app.adapter.CoachCourseAdapter
import com.haoyu.app.adapter.ManageWSAdapter
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.base.LegoApplication
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.*
import com.haoyu.app.imageloader.GlideImgManager
import com.haoyu.app.lego.teach.R
import com.haoyu.app.service.VersionUpdateService
import com.haoyu.app.utils.Common
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu
import okhttp3.Request

/**
 * 创建日期：2018/1/24.
 * 描述:主页面
 * 作者:xiaoma
 */
class MainActivity : BaseActivity(), View.OnClickListener {
    private val context = this
    private lateinit var ivToggle: ImageView
    private lateinit var ivMsg: ImageView
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var nsvContent: NestedScrollView
    private lateinit var rvCourse: RecyclerView
    private lateinit var rvWs: RecyclerView
    private val courseDatas = ArrayList<CourseMobileEntity>()
    private val wsDatas = ArrayList<WorkShopMobileEntity>()
    private lateinit var courseAdapter: CoachCourseAdapter
    private lateinit var wsAdapter: ManageWSAdapter

    private lateinit var menu: SlidingMenu

    override fun setLayoutResID(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        findViews()
        rvCourse.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        rvWs.layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
        courseAdapter = CoachCourseAdapter(context, courseDatas)
        rvCourse.adapter = courseAdapter
        wsAdapter = ManageWSAdapter(context, wsDatas)
        rvWs.adapter = wsAdapter
        initMenu()
        getVersion()
    }

    private fun findViews() {
        ivToggle = findViewById(R.id.ivToggle)
        ivMsg = findViewById(R.id.ivMsg)
        loadingView = findViewById(R.id.loadingView)
        loadFailView = findViewById(R.id.loadFailView)
        nsvContent = findViewById(R.id.nsv_content)
        rvCourse = findViewById(R.id.rvCourse)
        rvWs = findViewById(R.id.rvWs)
    }

    private fun initMenu() {
        menu = SlidingMenu(context)
        menu.mode = SlidingMenu.LEFT
        // 设置触摸屏幕的模式
        menu.touchModeAbove = SlidingMenu.TOUCHMODE_FULLSCREEN
        menu.setShadowWidthRes(R.dimen.shadow_width)
        // 设置滑动菜单视图的宽度
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset)
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.35f)
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT)
        val menuView = layoutInflater.inflate(R.layout.app_homepage_menu, LinearLayout(context), false)
        initMenuView(menuView)
        menu.menu = menuView
        getUserInfo()
    }

    private fun initMenuView(menuView: View) {
        val llUserInfo = menuView.findViewById<LinearLayout>(R.id.ll_userInfo)
        llUserInfo.setOnClickListener { startActivityForResult(Intent(context, AppUserInfoActivity::class.java), 2) }
        val ivUserIco = menuView.findViewById<ImageView>(R.id.iv_userIco)
        GlideImgManager.loadCircleImage(context, avatar, R.drawable.user_default, R.drawable.user_default, ivUserIco)
        val tvUserName = menuView.findViewById<TextView>(R.id.tv_userName)
        val tvDeptName = menuView.findViewById<TextView>(R.id.tv_deptName)
        tvUserName.text = if (TextUtils.isEmpty(realName)) "请填写用户名" else realName
        tvDeptName.text = if (TextUtils.isEmpty(deptName)) "请选择单位" else deptName
        val menuViews = arrayOfNulls<TextView>(5)
        menuViews[0] = menuView.findViewById(R.id.tv_education)
        menuViews[1] = menuView.findViewById(R.id.tv_teaching)
        menuViews[2] = menuView.findViewById(R.id.tv_message)
        menuViews[3] = menuView.findViewById(R.id.tv_consulting)
        menuViews[4] = menuView.findViewById(R.id.tv_settings)
        for (view in menuViews) {
            view?.setOnClickListener(context)
        }
    }

    private fun getUserInfo() {
        val url = "${Constants.OUTRT_NET}/m/user/$userId"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<MobileUser>>() {
            override fun onError(request: Request?, e: Exception) {

            }

            override fun onResponse(response: BaseResponseResult<MobileUser>?) {
                response?.responseData?.let {
                    updateUI(it)
                }
            }
        }))
    }

    private fun updateUI(user: MobileUser) {
        val ivUserIco = menu.menu?.findViewById<ImageView>(R.id.iv_userIco)
        GlideImgManager.loadCircleImage(applicationContext, user.avatar, R.drawable.user_default, R.drawable.user_default, ivUserIco)
        val tvUserName = menu.menu?.findViewById<TextView>(R.id.tv_userName)
        tvUserName?.text = if (TextUtils.isEmpty(user.realName)) "请填写用户名" else user.realName
        val tvDeptName = menu.findViewById<TextView>(R.id.tv_deptName)
        tvDeptName.text = if (TextUtils.isEmpty(user.getmDepartment()?.deptName)) "请选择单位" else user.getmDepartment().deptName
    }

    override fun initData() {
        val url = "${Constants.OUTRT_NET}/m/uc/teachIndex"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<TeachMainResult>() {
            override fun onBefore(request: Request?) {
                loadingView.visibility = View.VISIBLE
            }

            override fun onError(request: Request?, e: Exception) {
                loadingView.visibility = View.GONE
                loadFailView.visibility = View.VISIBLE
            }

            override fun onResponse(response: TeachMainResult?) {
                loadingView.visibility = View.VISIBLE
                if (response?.responseData != null) {
                    updateUI(response.responseData)
                } else {
                    if (response == null) {
                        val tvEmpty = findViewById<TextView>(R.id.tv_empty)
                        tvEmpty.text = "暂无培训信息~"
                    } else {
                        loadFailView.visibility = View.VISIBLE
                    }
                }
            }
        }))
    }

    private fun updateUI(mData: TeachMainResult.MData) {
        nsvContent.visibility = View.VISIBLE
        updateCourses(mData.getmCourses())
        updateWS(mData.getmWorkshops())
    }

    private fun updateCourses(mDatas: List<CourseMobileEntity>) {
        if (mDatas.isNotEmpty()) {
            courseDatas.addAll(mDatas)
            courseAdapter.notifyDataSetChanged()
            rvCourse.visibility = View.VISIBLE
        } else {
            rvCourse.visibility = View.GONE
            val tvEmpty = findViewById<TextView>(R.id.tv_emptyCourse)
            tvEmpty.text = "暂无参与辅导的课程"
            tvEmpty.visibility = View.VISIBLE
        }
    }

    private fun updateWS(mDatas: List<WorkShopMobileEntity>) {
        if (mDatas.isNotEmpty()) {
            wsDatas.addAll(mDatas)
            wsAdapter.notifyDataSetChanged()
            rvWs.visibility = View.VISIBLE
        } else {
            rvWs.visibility = View.GONE
            val tvEmpty = findViewById<TextView>(R.id.tv_emptyWS)
            tvEmpty.text = "暂无参与管理的工作坊"
            tvEmpty.visibility = View.VISIBLE
        }
    }

    override fun setListener() {
        ivToggle.setOnClickListener(context)
        ivMsg.setOnClickListener(context)
        courseAdapter.setOnItemClickListener { _, _, _, position ->
            val entity = courseDatas[position]
            entity.getmTimePeriod()?.state?.let {
                if (it == "未开始") {
                    showMaterialDialog("温馨提示", "课程尚未开放")
                    return@setOnItemClickListener
                }
            }
            val courseId = entity.id
            val courseTitle = entity.title
            val intent = Intent(context, CourseTabActivity::class.java).apply {
                putExtra("courseId", courseId)
                putExtra("courseTitle", courseTitle)
            }
            startActivity(intent)
        }
        wsAdapter.setOnItemClickListener { _, _, _, position ->
            val entity = wsDatas[position]
            val intent = Intent(context, WSHomePageActivity::class.java).apply {
                putExtra("workshopId", entity.id)
                putExtra("workshopTitle", entity.title)
            }
            startActivity(intent)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivToggle -> menu.toggle(true)
            R.id.ivMsg -> {
                val intent = Intent(context, AnnouncementActivity::class.java).apply {
                    putExtra("relationId", "system")
                    putExtra("type", "system_announcement")
                }
                startActivity(intent)
            }
            R.id.tv_education -> menu.toggle(true)
            R.id.tv_teaching -> startActivity(Intent(context, CmtsMainActivity::class.java))
            R.id.tv_message -> startActivity(Intent(context, MessageActivity::class.java))
            R.id.tv_consulting -> startActivity(Intent(context, EducationConsultActivity::class.java))
            R.id.tv_settings -> startActivity(Intent(context, SettingActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            2 -> data?.let {
                val avatar = it.getStringExtra("avatar")
                menu.menu?.findViewById<ImageView>(R.id.iv_userIco)?.let {
                    GlideImgManager.loadCircleImage(context.applicationContext, avatar, R.drawable.user_default, R.drawable.user_default, it)
                }
            }
        }
    }

    private var mExitTime: Long = 0

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && menu.isMenuShowing) {
            menu.toggle(true)
            return false
        } else if (keyCode == KeyEvent.KEYCODE_BACK && !menu.isMenuShowing) {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                toast(context, "再按一次退出" + resources.getString(R.string.app_name))
                mExitTime = System.currentTimeMillis()
            } else {
                LegoApplication.exit()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun getVersion() {
        addSubscription(OkHttpClientManager.getAsyn(context, Constants.updateUrl, object : OkHttpClientManager.ResultCallback<VersionEntity>() {
            override fun onError(request: Request, e: Exception) {

            }

            override fun onResponse(entity: VersionEntity) {
                if (entity.versionCode > Common.getVersionCode(context)) {
                    updateTips(entity)
                }
            }
        }))
    }

    private fun updateTips(entity: VersionEntity) {
        val dialog = MaterialDialog(context)
        dialog.setMessage(entity.updateLog)
        dialog.setTitle("发现新版本")
        dialog.setNegativeButton("稍后下载", null)
        dialog.setPositiveButton("立即下载") { _, _ -> startService(entity) }
        dialog.show()
    }

    private fun startService(entity: VersionEntity) {
        val intent = Intent(context, VersionUpdateService::class.java)
        intent.putExtra("url", entity.downurl)
        intent.putExtra("versionName", entity.versionName)
        startService(intent)
        if (!Common.isNotificationEnabled(context)) {
            openTips()
        }
    }

    private fun openTips() {
        val dialog = MaterialDialog(context)
        dialog.setTitle("提示")
        dialog.setMessage("通知已关闭，是否允许应用推送通知？")
        dialog.setPositiveButton("开启") { _, _ -> Common.openSettings(context) }
        dialog.setNegativeButton("取消") { _, _ -> toast(context, "已进入后台更新") }
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(context, VersionUpdateService::class.java))
    }
}