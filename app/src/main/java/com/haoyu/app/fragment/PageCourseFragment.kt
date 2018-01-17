package com.haoyu.app.fragment

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.google.gson.GsonBuilder
import com.haoyu.app.activity.*
import com.haoyu.app.adapter.CourseActivityAdapter
import com.haoyu.app.adapter.CourseStudyAdapter
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.basehelper.BaseRecyclerAdapter
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.*
import com.haoyu.app.lego.teach.R
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Request
import org.wlf.filedownloader.FileDownloader
import java.io.File
import java.util.*

/**
 * 创建日期：2018/1/12.
 * 描述:课程学习章节活动列表
 * 作者:xiaoma
 */
class PageCourseFragment : BaseFragment() {
    private lateinit var loadView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var tvEmpty: TextView
    private lateinit var recyclerView: RecyclerView
    private var courseId: String? = null
    private var mDatas: MutableList<MultiItemEntity> = ArrayList()
    private lateinit var adapter: CourseStudyAdapter
    private val requestCode = 1

    override fun createView(): Int {
        return R.layout.fragment_page_course
    }

    override fun initView(view: View) {
        courseId = arguments?.getString("entityId")
        loadView = view.findViewById(R.id.loadView)
        loadFailView = view.findViewById(R.id.loadFailView)
        tvEmpty = view.findViewById(R.id.empty_list)
        recyclerView = view.findViewById(R.id.recyclerView)
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        adapter = CourseStudyAdapter(context, mDatas)
        recyclerView.adapter = adapter
    }

    override fun initData() {
        loadView.visibility = View.VISIBLE
        val url = Constants.OUTRT_NET + "/" + courseId + "/teach/m/course/" + courseId + "/teach"
        addSubscription(Flowable.just(url).map(@Throws(Exception::class) { _ -> getData(url) })
                .map(@Throws(Exception::class) { response -> doWith(response) }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    loadView.visibility = View.GONE
                    val sections = response?.responseData?.getmCourse()?.getmSections()
                    val activities = response?.responseData?.getmActivities()
                    if (sections != null && sections.size > 0) {
                        updateUI(sections)
                    } else if (activities != null && activities.size > 0) {
                        updateAT(activities)
                    } else {
                        tvEmpty.visibility = View.VISIBLE
                    }
                }) {
                    loadView.visibility = View.GONE
                    loadFailView.visibility = View.VISIBLE
                })
    }

    @Throws(Exception::class)
    private fun getData(url: String): CourseSectionResult {
        val mDatas = OkHttpClientManager.getAsString(context, url)
        return GsonBuilder().create().fromJson(mDatas, CourseSectionResult::class.java)
    }

    @Throws(Exception::class)
    private fun doWith(response: CourseSectionResult?): CourseSectionResult? {
        response?.responseData?.getmCourse()?.getmSections()?.size?.let {
            for (i in 0 until it) {
                val entity = response.responseData.getmCourse().getmSections()[i]
                entity.childSections.size.let {
                    for (j in 0 until it) {
                        val sectionId = entity.childSections[j].id
                        val result = getActvityList(sectionId)
                        result?.responseData?.let {
                            response.responseData.getmCourse().getmSections()[i].childSections[j].activities = it
                        }
                    }
                }
            }
        }
        return response
    }

    @Throws(Exception::class)
    private fun getActvityList(sectionId: String): CourseActivityListResult? {
        var url = Constants.OUTRT_NET + "/" + sectionId + "/teach/m/activity/ncts" + "?id=" + sectionId
        var json = OkHttpClientManager.getAsString(context, url)
        val gson = GsonBuilder().create()
        val result = gson.fromJson(json, CourseActivityListResult::class.java)
        result?.responseData?.let {
            for (i in 0 until it.size) {
                val activity = it[i]
                activity?.type?.equals("video").let {
                    url = Constants.OUTRT_NET + "/" + activity.id + "/teach/m/activity/ncts/" + activity.id + "/view"
                    json = OkHttpClientManager.getAsString(context, url)
                    val viewResult = gson.fromJson(json, AppActivityViewResult::class.java)
                    viewResult?.responseData?.getmVideoUser()?.let {
                        result.responseData[i].setmVideo(it.getmVideo())
                    }
                }
            }
        }
        return result
    }

    private fun updateUI(list: List<CourseSectionEntity>) {
        recyclerView.visibility = View.VISIBLE
        for (i in list.indices) {
            val entity = list[i]
            mDatas.add(entity)
            entity.childSections.size.let {
                for (j in 0 until it) {
                    val childEntity = entity.childSections[j]
                    mDatas.add(childEntity)
                    childEntity.activities.size.let {
                        for (k in 0 until it) {
                            mDatas.add(childEntity.activities[k])
                        }
                    }
                }
            }
        }
        adapter.notifyDataSetChanged()
        adapter.setOnItemClickListener(object : CourseStudyAdapter.OnItemClickListener {
            override fun onChildSectionClick(position: Int) {
                recyclerView.smoothScrollToPosition(position)
            }

            override fun onActivityClick(activity: CourseSectionActivity) {
                enterActivity(activity)
            }
        })
        adapter.setOnItemLongClickListener({ tv, charSequence ->
            if (overLine(tv)) {
                val dialog = MaterialDialog(context)
                dialog.setTitle(null)
                dialog.setMessage(charSequence)
                dialog.setCanceledOnTouchOutside(true)
                dialog.setCancelable(true)
                dialog.setPositiveButton("关闭", null)
                dialog.show()
            }
        })
    }

    private fun overLine(tv: TextView): Boolean {
        val layout = tv.layout
        if (layout != null && layout.lineCount > 0) {
            val lines = layout.lineCount//获取textview行数
            if (layout.getEllipsisCount(lines - 1) > 0) {//获取最后一行省略掉的字符数，大于0就代表超过行数
                return true
            }
        }
        return false
    }

    private fun updateAT(mDatas: List<CourseSectionActivity>) {
        recyclerView.visibility = View.VISIBLE
        val mAdapter = CourseActivityAdapter(context, mDatas)
        recyclerView.adapter = mAdapter
        mAdapter.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { _, _, _, position ->
            mAdapter.setSelected(mDatas[position].id)
            val activity = mDatas[position]
            enterActivity(activity)
        }
        mAdapter.setOnItemLongClickListener { tv, charSequence ->
            if (overLine(tv)) {
                val dialog = MaterialDialog(context)
                dialog.setTitle(null)
                dialog.setMessage(charSequence)
                dialog.setCanceledOnTouchOutside(true)
                dialog.setCancelable(true)
                dialog.setPositiveButton("关闭", null)
                dialog.show()
            }
        }
    }

    private fun enterActivity(activity: CourseSectionActivity) {
        val url = Constants.OUTRT_NET + "/" + activity.id + "/teach/m/activity/ncts/" + activity.id + "/view"
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<AppActivityViewResult>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                onNetWorkError()
            }

            override fun onResponse(response: AppActivityViewResult?) {
                hideTipDialog()
                if (response != null) {
                    orientationActivity(response)
                }
            }
        }))
    }

    /*根据活动内容进入相关的activity*/
    private fun orientationActivity(response: AppActivityViewResult) {
        if (response.responseData != null && response.responseData.getmActivityResult() != null
                && response.responseData.getmActivityResult().getmActivity() != null) {
            val activity = response.responseData.getmActivityResult().getmActivity()
            if (activity.type != null && activity.type == "video") {   //视频类型
                playVideo(response, activity)
            } else if (activity.type != null && activity.type == "html") {  //课件类型
                openHtml(response, activity)
            } else if (activity.type != null && activity.type == "discussion") {  //课程研讨
                openDiscussion(response, activity)
            } else if (activity.type != null && activity.type == "survey") {  //问卷调查
                openSurvey(response, activity)
            } else if (activity.type != null && activity.type == "test") {  //测验类型
                openTest(response, activity)
            } else if (activity.type != null && activity.type == "assignment") {  //作业类型
                openAssignMent(activity)
            } else {
                toast("系统暂不支持浏览，请到网站完成。")
            }
        } else {
            toast("系统暂不支持浏览，请到网站完成。")
        }
    }

    /*播放视频*/
    private fun playVideo(response: AppActivityViewResult, activity: CourseSectionActivity) {
        if (response.responseData != null && response.responseData.getmVideoUser() != null) {  //教学视频
            val videoEntity = response.responseData.getmVideoUser()
            val video = videoEntity.getmVideo()
            if (video != null) {
                val intent = Intent(context, IJKPlayerActivity::class.java)
                intent.putExtra("videoType", "course")
                if (activity.getmTimePeriod() != null && activity.getmTimePeriod().state != null && activity.getmTimePeriod().state == "进行中")
                    intent.putExtra("running", true)
                else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().minutes > 0)
                    intent.putExtra("running", true)
                else
                    intent.putExtra("running", false)
                intent.putExtra("activityId", activity.id)
                intent.putExtra("videoTitle", activity.title)
                intent.putExtra("videoId", videoEntity.id)
                intent.putExtra("video", video)
                if (!TextUtils.isEmpty(video.urls)) {
                    val fileInfo = FileDownloader.getDownloadFile(video.urls)
                    if (fileInfo != null && fileInfo.filePath != null && File(fileInfo.filePath).exists()) {
                        intent.putExtra("videoUrl", fileInfo.filePath)
                    } else {
                        intent.putExtra("videoUrl", video.urls)
                    }
                    startActivityForResult(intent, requestCode)
                } else if (video.videoFiles.size > 0) {
                    val url = video.videoFiles[0].url
                    val fileInfo = FileDownloader.getDownloadFile(url)
                    if (fileInfo != null && fileInfo.filePath != null && File(fileInfo.filePath).exists()) {
                        intent.putExtra("videoUrl", fileInfo.filePath)
                    } else {
                        intent.putExtra("videoUrl", url)
                    }
                    startActivityForResult(intent, requestCode)
                } else {
                    toast("系统暂不支持浏览，请到网站完成。")
                }
            } else {
                toast("系统暂不支持浏览，请到网站完成。")
            }
        }
    }

    /*打开课件*/
    private fun openHtml(response: AppActivityViewResult, activity: CourseSectionActivity) {
        if (response.responseData != null && response.responseData.getmTextInfoUser() != null) {
            val mTextInfoUser = response.responseData.getmTextInfoUser()
            val intent = Intent()
            if (activity.getmTimePeriod() != null && activity.getmTimePeriod().state != null && activity.getmTimePeriod().state == "进行中")
                intent.putExtra("running", true)
            else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().minutes > 0)
                intent.putExtra("running", true)
            else
                intent.putExtra("running", false)
            intent.putExtra("activityId", activity.id)
            intent.putExtra("mTextInfoUserId", mTextInfoUser.id)
            intent.putExtra("title", activity.title)
            intent.putExtra("viewNum", mTextInfoUser.viewNum)
            if (mTextInfoUser.getmTextInfo() != null) {
                intent.putExtra("interval", mTextInfoUser.getmTextInfo().interval)
                intent.putExtra("needViewNum", mTextInfoUser.getmTextInfo().viewNum)
            }
            if (mTextInfoUser.getmTextInfo() != null && mTextInfoUser.getmTextInfo().type != null
                    && mTextInfoUser.getmTextInfo().type == "file") {  //课件类型为pdf文件
                val pdfUrl = mTextInfoUser.getmTextInfo().pdfUrl
                intent.putExtra("type", "file")
                intent.putExtra("url", pdfUrl)
                intent.setClass(context, CoursewareViewerActivity::class.java)
                startActivityForResult(intent, requestCode)
            } else if (mTextInfoUser.getmTextInfo() != null && mTextInfoUser.getmTextInfo().type != null
                    && mTextInfoUser.getmTextInfo().type == "link") {  //课件类型为外链
                val webUrl = mTextInfoUser.getmTextInfo().content
                intent.setClass(context, CoursewareViewerActivity::class.java)
                intent.putExtra("type", "link")
                intent.putExtra("url", webUrl)
                startActivityForResult(intent, requestCode)
            } else if (mTextInfoUser.getmTextInfo() != null && mTextInfoUser.getmTextInfo().type != null
                    && mTextInfoUser.getmTextInfo().type == "editor") {  // 课件类型为文本
                val editor = mTextInfoUser.getmTextInfo().content
                intent.setClass(context, CoursewareViewerActivity::class.java)
                intent.putExtra("type", "editor")
                intent.putExtra("editor", editor)
                startActivityForResult(intent, requestCode)
            } else {
                toast("系统暂不支持浏览，请到网站完成。")
            }
        } else {
            toast("系统暂不支持浏览，请到网站完成。")
        }
    }

    /*打开课程研讨*/
    private fun openDiscussion(response: AppActivityViewResult, activity: CourseSectionActivity) {
        if (response.responseData != null && response.responseData.getmDiscussionUser() != null) {
            val intent = Intent(context, TeachingDiscussionActivity::class.java)
            if (activity.getmTimePeriod() != null && activity.getmTimePeriod().state != null && activity.getmTimePeriod().state == "进行中")
                intent.putExtra("running", true)
            else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().minutes > 0)
                intent.putExtra("running", true)
            else
                intent.putExtra("running", false)
            intent.putExtra("discussType", "course")
            intent.putExtra("relationId", courseId)
            intent.putExtra("activityId", activity.id)
            intent.putExtra("activityTitle", activity.title)
            intent.putExtra("timePeriod", activity.getmTimePeriod())
            intent.putExtra("discussUser", response.responseData.getmDiscussionUser())
            intent.putExtra("mainNum", response.responseData.getmDiscussionUser().mainPostNum)
            intent.putExtra("subNum", response.responseData.getmDiscussionUser().subPostNum)
            if (response.responseData.getmDiscussionUser().getmDiscussion() != null) {
                val entity = response.responseData.getmDiscussionUser().getmDiscussion()
                intent.putExtra("needMainNum", entity.mainPostNum)
                intent.putExtra("needSubNum", entity.subPostNum)
            }
            startActivityForResult(intent, requestCode)
        } else
            toast("系统暂不支持浏览，请到网站完成。")
    }

    /*打开问卷调查*/
    private fun openSurvey(response: AppActivityViewResult, activity: CourseSectionActivity) {
        val intent = Intent(context, AppSurveyHomeActivity::class.java)
        if (activity.getmTimePeriod() != null && activity.getmTimePeriod().state != null && activity.getmTimePeriod().state == "进行中")
            intent.putExtra("running", true)
        else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().minutes > 0)
            intent.putExtra("running", true)
        else
            intent.putExtra("running", false)
        intent.putExtra("relationId", courseId)
        intent.putExtra("type", "course")
        intent.putExtra("timePeriod", activity.getmTimePeriod())
        if (response.responseData != null && response.responseData.getmSurveyUser() != null) {
            intent.putExtra("surveyUser", response.responseData.getmSurveyUser())
        }
        intent.putExtra("activityId", activity.id)
        intent.putExtra("activityTitle", activity.title)
        startActivityForResult(intent, requestCode)
    }

    /*打开测验*/
    private fun openTest(response: AppActivityViewResult, activity: CourseSectionActivity) {
        val intent = Intent()
        if (activity.getmTimePeriod() != null && activity.getmTimePeriod().state != null && activity.getmTimePeriod().state == "进行中")
            intent.putExtra("running", true)
        else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().minutes > 0)
            intent.putExtra("running", true)
        else
            intent.putExtra("running", false)
        intent.putExtra("relationId", courseId)
        intent.putExtra("testType", "course")
        intent.putExtra("timePeriod", activity.getmTimePeriod())
        intent.putExtra("activityId", activity.id)
        intent.putExtra("activityTitle", activity.title)
        if (response.responseData != null && response.responseData.getmTestUser() != null) {
            intent.putExtra("testUser", response.responseData.getmTestUser())
        }
        if (response.responseData != null && response.responseData.getmTestUser() != null
                && response.responseData.getmTestUser().completionStatus != null
                && response.responseData.getmTestUser().completionStatus == "completed") {
            if (response.responseData.getmActivityResult() != null) {
                intent.putExtra("score", response.responseData.getmActivityResult().score)
            }
            intent.setClass(context, AppTestResultActivity::class.java)
        } else {
            intent.setClass(context, AppTestHomeActivity::class.java)
        }
        startActivityForResult(intent, requestCode)
    }

    /*打开作业*/
    private fun openAssignMent(activity: CourseSectionActivity) {
        val intent = Intent(context, TestAssignmentActivity::class.java)
        if (activity.getmTimePeriod() != null && activity.getmTimePeriod().state != null && activity.getmTimePeriod().state == "进行中")
            intent.putExtra("running", true)
        else if (activity.getmTimePeriod() != null && activity.getmTimePeriod().minutes > 0)
            intent.putExtra("running", true)
        else
            intent.putExtra("running", false)
        intent.putExtra("timePeriod", activity.getmTimePeriod())
        intent.putExtra("activityId", activity.id)
        intent.putExtra("activityTitle", activity.title)
        intent.putExtra("inCurrentDate", activity.isInCurrentDate)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCode && resultCode == Activity.RESULT_OK && data != null) {
            val activity = data.getSerializableExtra("activity") as CourseSectionActivity
            if (mDatas.indexOf(activity) != -1) {
                val index = mDatas.indexOf(activity)
                val entity = mDatas.get(index) as CourseSectionActivity
                entity.completeState = activity.completeState
                mDatas.set(index, entity)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (adapter.getmOnFileDownloadStatusListener() != null) {
            FileDownloader.unregisterDownloadStatusListener(adapter.getmOnFileDownloadStatusListener())
        }
    }
}