package com.haoyu.app.fragment

import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import com.haoyu.app.adapter.CourseRegisterStatsAdapter
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.entity.CourseRegisterStats
import com.haoyu.app.entity.CourseStatistics
import com.haoyu.app.entity.CourseStatisticsResult
import com.haoyu.app.entity.Paginator
import com.haoyu.app.lego.teach.R
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.utils.TimeUtil
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.xrecyclerview.XRecyclerView
import okhttp3.Request

/**
 * 创建日期：2018/1/16.
 * 描述:课程统计信息
 * 作者:xiaoma
 */
class PageStatisticsFragment : BaseFragment(), XRecyclerView.LoadingListener {
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var llContent: LinearLayout
    private lateinit var radioGroup: RadioGroup
    private val textViews = arrayOfNulls<TextView>(9)
    private val framelayouts = arrayOfNulls<FrameLayout>(3)
    private val lfvs = arrayOfNulls<LoadFailView>(3)
    private val tvEmpties = arrayOfNulls<TextView>(3)
    private val xrvs = arrayOfNulls<XRecyclerView>(3)
    private var courseId: String? = null   //课程Id
    private var totalCount = 0
    private val gets = arrayOf(false, false, false)
    private val loads = arrayOf(false, false, false)
    private val mArrayDatas = arrayOfNulls<ArrayList<CourseRegisterStats>>(3)
    private val adapters = arrayOfNulls<CourseRegisterStatsAdapter>(3)
    private var type = 1
    private var page1 = 1
    private var page2 = 1
    private var page3 = 1
    override fun createView(): Int {
        return R.layout.fragment_page_statistics
    }

    override fun initView(view: View) {
        courseId = arguments?.getString("entityId")
        loadingView = view.findViewById(R.id.loadingView)
        loadFailView = view.findViewById(R.id.loadFailView)
        llContent = view.findViewById(R.id.ll_content)
        textViews[0] = view.findViewById(R.id.tv_empty)
        textViews[1] = view.findViewById(R.id.tv_title)
        textViews[2] = view.findViewById(R.id.tv_time)
        textViews[3] = view.findViewById(R.id.tv_hours)
        textViews[4] = view.findViewById(R.id.tv_questionNum)
        textViews[5] = view.findViewById(R.id.tv_answerNum)
        textViews[6] = view.findViewById(R.id.tv_noteNum)
        textViews[7] = view.findViewById(R.id.tv_resourcesNum)
        textViews[8] = view.findViewById(R.id.tv_discussNum)
        radioGroup = view.findViewById(R.id.radioGroup)
        framelayouts[0] = view.findViewById(R.id.fl1)
        framelayouts[1] = view.findViewById(R.id.fl2)
        framelayouts[2] = view.findViewById(R.id.fl3)
        lfvs[0] = view.findViewById(R.id.lfv1)
        lfvs[1] = view.findViewById(R.id.lfv2)
        lfvs[2] = view.findViewById(R.id.lfv3)
        xrvs[0] = view.findViewById(R.id.xrv1)
        xrvs[1] = view.findViewById(R.id.xrv2)
        xrvs[2] = view.findViewById(R.id.xrv3)
        tvEmpties[0] = view.findViewById(R.id.tv1)
        tvEmpties[1] = view.findViewById(R.id.tv2)
        tvEmpties[2] = view.findViewById(R.id.tv3)
        mArrayDatas[0] = ArrayList()
        mArrayDatas[1] = ArrayList()
        mArrayDatas[2] = ArrayList()
        adapters[0] = CourseRegisterStatsAdapter(context, mArrayDatas[0], totalCount)
        adapters[1] = CourseRegisterStatsAdapter(context, mArrayDatas[1], totalCount)
        adapters[2] = CourseRegisterStatsAdapter(context, mArrayDatas[2], totalCount)
        xrvs[0]?.let {
            val llm = LinearLayoutManager(context)
            llm.orientation = LinearLayoutManager.VERTICAL
            it.layoutManager = llm
            it.adapter = adapters[0]
            it.isPullRefreshEnabled = false
            it.setLoadingListener(this)
        }
        xrvs[1]?.let {
            val llm = LinearLayoutManager(context)
            llm.orientation = LinearLayoutManager.VERTICAL
            it.layoutManager = llm
            it.adapter = adapters[1]
            it.isPullRefreshEnabled = false
            it.setLoadingListener(this)
        }
        xrvs[2]?.let {
            val llm = LinearLayoutManager(context)
            llm.orientation = LinearLayoutManager.VERTICAL
            it.layoutManager = llm
            it.adapter = adapters[2]
            it.isPullRefreshEnabled = false
            it.setLoadingListener(this)
        }
        for (tv in tvEmpties) {
            tv?.text = "没有统计信息~"
        }
    }

    override fun initData() {
        val url = Constants.OUTRT_NET + "/" + courseId + "/teach/m/course_stat/" + courseId
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<CourseStatisticsResult>() {
            override fun onBefore(request: Request) {
                loadingView.visibility = View.VISIBLE
            }

            override fun onError(request: Request, e: Exception) {
                loadingView.visibility = View.GONE
                loadFailView.visibility = View.VISIBLE
            }

            override fun onResponse(response: CourseStatisticsResult?) {
                loadingView.visibility = View.GONE
                if (response?.responseData != null) {
                    updateUI(response.responseData)
                } else {
                    textViews[0]?.visibility = View.VISIBLE
                }
            }
        }))
    }

    private fun updateUI(mData: CourseStatisticsResult.CourseStatisticsData) {
        llContent.visibility = View.VISIBLE
        mData.getmCourse()?.let {
            textViews[1]?.text = it.title
            val studyHours = it.studyHours.toString() + "学时"
            textViews[3]?.text = studyHours
            it.getmTimePeriod()?.let {
                val text = "开课时间：" + TimeUtil.convertTimeOfDay(it.startTime, it.endTime)
                textViews[2]?.text = text
            }
        }
        textViews[4]?.let {
            val text = mData.faqQuestionNum.toString() + "\n提问数"
            val end = text.indexOf("提") - 1
            setTextSpan(text, 0, end, it)
        }
        textViews[5]?.let {
            val text = mData.faqAnswerNum.toString() + "\n回答数"
            val end = text.indexOf("回") - 1
            setTextSpan(text, 0, end, it)
        }
        textViews[6]?.let {
            val text = mData.noteNum.toString() + "\n笔记数"
            val end = text.indexOf("笔") - 1
            setTextSpan(text, 0, end, it)
        }
        textViews[7]?.let {
            val text = mData.resourceNum.toString() + "\n资源数"
            val end = text.indexOf("资") - 1
            setTextSpan(text, 0, end, it)
        }
        textViews[8]?.let {
            val text = mData.resourceNum.toString() + "\n研讨数"
            val end = text.indexOf("研") - 1
            setTextSpan(text, 0, end, it)
        }
        totalCount = mData.activityAssignmentNum
        setTab(1)
    }

    private fun setTextSpan(text: String, start: Int, end: Int, textView: TextView) {
        val color = ContextCompat.getColor(context, R.color.darksalmon)
        val ssb = SpannableString(text)
        ssb.setSpan(AbsoluteSizeSpan(20, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = ssb
    }

    private fun setTab(tab: Int) {
        for (layout in framelayouts) layout?.visibility = View.GONE
        when (tab) {
            1 -> {
                framelayouts[0]?.visibility = View.VISIBLE
                if (!gets[0]) {
                    getData(1)
                }
            }
            2 -> {
                framelayouts[1]?.visibility = View.VISIBLE
                if (!gets[1]) {
                    getData(2)
                }
            }
            3 -> {
                framelayouts[2]?.visibility = View.VISIBLE
                if (!gets[2]) {
                    getData(3)
                }
            }
        }
    }

    private fun getData(type: Int) {
        val url = when (type) {
            1 -> Constants.OUTRT_NET + "/" + courseId + "/teach/m/course_register_stat/" + courseId + "?limit=20&page=" + page1
            2 -> Constants.OUTRT_NET + "/" + courseId + "/teach/m/course_register_stat/" + courseId + "?courseResultState=pass&limit=20&page=" + page2
            else -> Constants.OUTRT_NET + "/" + courseId + "/teach/m/course_register_stat/" + courseId + "?courseResultState=nopass&limit=20&page=" + page3
        }
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<CourseStatistics>() {
            override fun onError(request: Request, e: Exception) {
                onError(type)
            }

            override fun onResponse(response: CourseStatistics?) {
                onResponse(type)
                if (response?.responseData != null && response.responseData.getmCourseRegisterStats().size > 0) {
                    onDatas(type, response.responseData.getmCourseRegisterStats(), response.responseData.paginator)
                } else {
                    onEmpty(type)
                }
            }
        }))
    }

    private fun onError(type: Int) {
        when (type) {
            1 -> {
                if (loads[0]) {
                    page1 -= 1
                    xrvs[0]?.loadMoreComplete(false)
                } else {
                    lfvs[0]?.visibility = View.VISIBLE
                }
            }
            2 -> {
                if (loads[1]) {
                    page2 -= 1
                    xrvs[1]?.loadMoreComplete(false)
                } else {
                    lfvs[1]?.visibility = View.VISIBLE
                }
            }
            3 -> {
                if (loads[2]) {
                    page3 -= 1
                    xrvs[2]?.loadMoreComplete(false)
                } else {
                    lfvs[2]?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun onResponse(type: Int) {
        when (type) {
            1 -> {
                gets[0] = true
            }
            2 -> {
                gets[1] = true
            }
            3 -> {
                gets[2] = true
            }
        }
    }

    private fun onDatas(type: Int, mDatas: List<CourseRegisterStats>, paginator: Paginator?) {
        when (type) {
            1 -> {
                if (xrvs[0]?.visibility != View.VISIBLE) xrvs[0]?.visibility = View.VISIBLE
                if (loads[0]) {
                    xrvs[0]?.loadMoreComplete(true)
                }
                mArrayDatas[0]?.addAll(mDatas)
                adapters[0]?.notifyDataSetChanged()
                xrvs[0]?.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
            }
            2 -> {
                if (xrvs[1]?.visibility != View.VISIBLE) xrvs[1]?.visibility = View.VISIBLE
                if (loads[1]) {
                    xrvs[1]?.loadMoreComplete(true)
                }
                mArrayDatas[1]?.addAll(mDatas)
                adapters[1]?.notifyDataSetChanged()
                xrvs[1]?.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
            }
            3 -> {
                if (xrvs[2]?.visibility != View.VISIBLE) xrvs[2]?.visibility = View.VISIBLE
                if (loads[2]) {
                    xrvs[2]?.loadMoreComplete(true)
                }
                mArrayDatas[2]?.addAll(mDatas)
                adapters[2]?.notifyDataSetChanged()
                xrvs[2]?.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
            }
        }
    }

    private fun onEmpty(type: Int) {
        when (type) {
            1 -> {
                if (loads[0]) {
                    xrvs[0]?.loadMoreComplete(true)
                    xrvs[0]?.isLoadingMoreEnabled = false
                } else {
                    xrvs[0]?.visibility = View.GONE
                    tvEmpties[0]?.visibility = View.VISIBLE
                }
            }
            2 -> {
                if (loads[1]) {
                    xrvs[1]?.loadMoreComplete(true)
                    xrvs[1]?.isLoadingMoreEnabled = false
                } else {
                    xrvs[1]?.visibility = View.GONE
                    tvEmpties[1]?.visibility = View.VISIBLE
                }
            }
            3 -> {
                if (loads[2]) {
                    xrvs[2]?.loadMoreComplete(true)
                    xrvs[2]?.isLoadingMoreEnabled = false
                } else {
                    xrvs[2]?.visibility = View.GONE
                    tvEmpties[2]?.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun setListener() {
        loadFailView.setOnRetryListener { initData() }
        lfvs[0]?.setOnRetryListener { getData(1) }
        lfvs[1]?.setOnRetryListener { getData(2) }
        lfvs[2]?.setOnRetryListener { getData(3) }
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_all -> {
                    type = 1
                    setTab(1)
                }
                R.id.rb_qualified -> {
                    type = 2
                    setTab(2)
                }
                R.id.rb_noqualified -> {
                    type = 3
                    setTab(3)
                }
            }
        }
    }

    override fun onRefresh() {
    }

    override fun onLoadMore() {
        when (type) {
            1 -> {
                loads[0] = true
                page1 += 1
                getData(1)
            }
            2 -> {
                loads[1] = true
                page2 += 1
                getData(2)
            }
            3 -> {
                loads[2] = true
                page3 += 1
                getData(3)
            }
        }
    }

}