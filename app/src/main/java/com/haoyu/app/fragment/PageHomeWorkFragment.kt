package com.haoyu.app.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.PaintDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.haoyu.app.activity.MarkAssignmentActivity
import com.haoyu.app.adapter.AssignmentListAdapter
import com.haoyu.app.base.BaseFragment
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.basehelper.BaseArrayRecyclerAdapter
import com.haoyu.app.basehelper.BaseRecyclerAdapter
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.entity.*
import com.haoyu.app.lego.teach.R
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.utils.OkHttpClientManager.getAsyn
import com.haoyu.app.view.LoadFailView
import com.haoyu.app.view.LoadingView
import com.haoyu.app.xrecyclerview.XRecyclerView
import okhttp3.Request

/**
 * 创建日期：2018/1/16.
 * 描述:课程学习学员作业信息
 * 作者:xiaoma
 */
class PageHomeWorkFragment : BaseFragment(), XRecyclerView.LoadingListener {
    private lateinit var loadingView: LoadingView
    private lateinit var loadFailView: LoadFailView
    private lateinit var tvEmpty: TextView
    private lateinit var llContent: LinearLayout
    private lateinit var llShake: LinearLayout
    private lateinit var icState: ImageView
    private val textViews = arrayOfNulls<TextView>(4)
    private lateinit var xRecyclerView: XRecyclerView
    private val mDatas = ArrayList<MAssignmentUser>()
    private lateinit var adapter: AssignmentListAdapter
    private var courseId: String? = null
    private val booleans = arrayOf(false, false, false, false, false)
    private var page = 1
    private val mAssignments = ArrayList<MAssignmentEntity>()
    private val states = ArrayList<State>()
    private var selectId = 0
    private var selectState = 0
    private var assignmentId: String? = null
    private var state: String? = null
    private var selected = -1

    private class State(var state: String?, var content: String?)

    override fun createView(): Int {
        return R.layout.fragment_page_homework
    }

    override fun initView(view: View) {
        courseId = arguments?.getString("entityId")
        loadingView = view.findViewById(R.id.loadingView)
        loadFailView = view.findViewById(R.id.loadFailView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        llContent = view.findViewById(R.id.ll_content)
        llShake = view.findViewById(R.id.ll_shake)
        icState = view.findViewById(R.id.ic_state)
        textViews[0] = view.findViewById(R.id.tv_tips)
        textViews[1] = view.findViewById(R.id.tv_all)
        textViews[2] = view.findViewById(R.id.tv_state)
        textViews[3] = view.findViewById(R.id.tv_empty)
        xRecyclerView = view.findViewById(R.id.xRecyclerView)
        adapter = AssignmentListAdapter(context, mDatas)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        xRecyclerView.layoutManager = layoutManager
        xRecyclerView.adapter = adapter
        xRecyclerView.setLoadingListener(this)
        initStates()
    }

    private fun initStates() {
        states.add(State(null, "全部状态"))
        states.add(State("commit", "待批阅"))
        states.add(State("complete", "已批阅"))
        states.add(State("return", "发回重做"))
    }

    override fun initData() {
        val url = Constants.OUTRT_NET + "/" + courseId + "/teach/m/assignment/user/getAssignmentUserNum?relationId=" + courseId
        addSubscription(getAsyn(context, url, object : OkHttpClientManager.ResultCallback<CoursehwNumResult>() {
            override fun onBefore(request: Request) {
                if (!booleans[0]) {
                    loadingView.visibility = View.VISIBLE
                }
            }

            override fun onError(request: Request, e: Exception) {
                loadingView.visibility = View.GONE
                loadFailView.visibility = View.VISIBLE
            }

            override fun onResponse(response: CoursehwNumResult?) {
                booleans[0] = true
                if (loadingView.visibility != View.GONE) loadingView.visibility = View.GONE
                if (response?.responseData != null) {
                    updateUI(response.responseData)
                    getAssignmentList()
                } else {
                    tvEmpty.text = "没有作业信息~"
                    tvEmpty.visibility = View.VISIBLE
                }
            }
        }))
    }

    private fun updateUI(mData: CoursehwNumResult.MData) {
        if (llContent.visibility != View.VISIBLE) llContent.visibility = View.VISIBLE
        val allNum = mData.allNum  //作业总数
        val markNum = mData.markNum //批阅数
        val notReceiveNum = mData.notReceivedNum
        val ssb: SpannableString
        if (notReceiveNum > 0) {
            icState.setImageResource(R.drawable.assignment_queren_tip)
            val text = ("您已批阅 " + markNum + "/" + allNum + " 份作业，还有 " + notReceiveNum
                    + " 份待领取，点击领取作业")
            ssb = SpannableString(text)
            ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), text.indexOf("阅") + 1, text.indexOf("份"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), text.indexOf("有") + 1, text.lastIndexOf("份"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            textViews[0]?.text = ssb
            llShake.isEnabled = true
        } else {
            icState.setImageResource(R.drawable.assignment_get_tips)
            val text = "您已批阅 $markNum/$allNum 份作业，暂无待领取的作业。"
            ssb = SpannableString(text)
            ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.orange)), text.indexOf("阅") + 1, text.indexOf("份"), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            textViews[0]?.text = ssb
            llShake.isEnabled = false
        }
    }

    /*获取作业列表*/
    private fun getAssignmentList() {
        var url = (Constants.OUTRT_NET + "/" + courseId + "/teach/m/assignment/user?relationId=" + courseId + "&page=" + page + "&limit=20")
        if (assignmentId != null) url += "&assignmentId=" + assignmentId
        if (state != null) url += "&state=" + state
        addSubscription(getAsyn(context, url, object : OkHttpClientManager.ResultCallback<CourseHomeworks>() {

            override fun onBefore(request: Request) {
                if (textViews[3]?.visibility != View.GONE) {
                    textViews[3]?.visibility = View.GONE
                }
                if (booleans[3]) {
                    showTipDialog()
                }
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
                when {
                    booleans[1] -> xRecyclerView.refreshComplete(false)
                    booleans[2] -> {
                        page -= 1
                        xRecyclerView.loadMoreComplete(false)
                    }
                    else -> onNetWorkError()
                }
            }

            override fun onResponse(response: CourseHomeworks?) {
                hideTipDialog()
                if (response?.responseData != null && response.responseData.getmAssignmentUsers().size > 0) {
                    updateUI(response.responseData.getmAssignmentUsers(), response.responseData.paginator)
                } else {
                    xRecyclerView.visibility = View.GONE
                    textViews[3]?.let {
                        it.visibility = View.VISIBLE
                        it.text = "没有作业~"
                    }
                }
            }
        }))
    }

    private fun updateUI(mAssignmentUsers: List<MAssignmentUser>, paginator: Paginator?) {
        if (textViews[3]?.visibility != View.GONE)
            textViews[3]?.visibility = View.GONE
        if (xRecyclerView.visibility != View.VISIBLE)
            xRecyclerView.visibility = View.VISIBLE
        if (booleans[1]) {
            mDatas.clear()
            xRecyclerView.refreshComplete(true)
        } else if (booleans[2]) {
            xRecyclerView.loadMoreComplete(true)
        }
        mDatas.addAll(mAssignmentUsers)
        adapter.notifyDataSetChanged()
        xRecyclerView.isLoadingMoreEnabled = paginator != null && paginator.hasNextPage
    }

    override fun setListener() {
        loadFailView.setOnRetryListener { initData() }
        llShake.setOnClickListener({
            getHomeWork()
        })
        textViews[1]?.setOnClickListener({
            if (!booleans[4]) {
                getAssignment()
            } else {
                textViews[1]?.let {
                    showPopupView(it, 1)
                }
            }
        })
        textViews[2]?.setOnClickListener({
            textViews[2]?.let {
                showPopupView(it, 2)
            }
        })
        adapter.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { _, _, _, position ->
            selected = position - 1
            if (selected >= 0 && selected < mDatas.size) {
                val entity = mDatas[selected]
                if (entity.state != null && entity.state == "return") {
                    showTips()
                } else {
                    val intent = Intent(context, MarkAssignmentActivity::class.java)
                    intent.putExtra("courseId", courseId)
                    if (entity.getmUser() != null) {
                        intent.putExtra("userName", entity.getmUser().realName)
                    }
                    intent.putExtra("state", entity.state)
                    intent.putExtra("relationId", entity.id)
                    startActivityForResult(intent, 1)
                }
            }
        }
    }

    /* 领取作业*/
    private fun getHomeWork() {
        val url = Constants.OUTRT_NET + "/" + courseId + "/teach/unique_uid_" + userId + "/m/assignment/mark/" + courseId
        addSubscription(OkHttpClientManager.postAsyn(context, url, object : OkHttpClientManager.ResultCallback<BaseResponseResult<*>>() {
            override fun onBefore(request: Request) {
                showTipDialog()
            }

            override fun onError(request: Request, e: Exception) {
                hideTipDialog()
            }

            override fun onResponse(response: BaseResponseResult<*>?) {
                hideTipDialog()
                if (response?.getResponseData() != null) {
                    booleans[1] = true
                    page = 1
                    initData()
                } else {
                    response?.responseMsg?.let {
                        toast("暂时没有未批阅的作业")
                    }
                }
            }
        }))
    }

    /*作业活动列表*/
    private fun getAssignment() {
        val url = (Constants.OUTRT_NET + "/" + courseId + "/teach/m/assignment?relationId=" + courseId + "&markType=teach")
        addSubscription(OkHttpClientManager.getAsyn(context, url, object : OkHttpClientManager.ResultCallback<AssignmentListResult>() {
            override fun onError(request: Request, e: Exception) {
                booleans[4] = false
            }

            override fun onResponse(response: AssignmentListResult?) {
                booleans[4] = true
                response?.responseData?.let {
                    val entity = MAssignmentEntity()
                    entity.title = "全部作业"
                    mAssignments.add(entity)
                    mAssignments.addAll(it)
                    textViews[1]?.let {
                        showPopupView(it, 1)
                    }
                }
            }
        }))
    }

    private fun showPopupView(tv: TextView, type: Int) {
        val shouqi = ContextCompat.getDrawable(context, R.drawable.assignment_shouqi)
        shouqi?.setBounds(0, 0, shouqi.minimumWidth, shouqi.minimumHeight)
        val zhankai = ContextCompat.getDrawable(context, R.drawable.assignment_zhankai)
        zhankai?.setBounds(0, 0, zhankai.minimumWidth, zhankai.minimumHeight)
        val recyclerView = RecyclerView(context)
        recyclerView.setBackgroundResource(R.drawable.dictionary_background)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
        val popupWindow = PopupWindow(recyclerView, tv.width, ViewGroup.LayoutParams.WRAP_CONTENT)
        tv.setCompoundDrawables(null, null, shouqi, null)
        val adapter = when (type) {
            1 -> {
                AssignmentAdapter(context, mAssignments, selectId)
            }
            else -> {
                StateAdapter(context, states, selectState)
            }
        }
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener({ _, _, _, position ->
            when (type) {
                1 -> {
                    selectId = position
                    (adapter as AssignmentAdapter).setSelectItem(selectId)
                    assignmentId = mAssignments[position].id
                    tv.text = mAssignments[position].title
                }
                else -> {
                    selectState = position
                    (adapter as StateAdapter).setSelectItem(selectState)
                    tv.text = states[position].content
                    state = states[position].state
                }
            }
            popupWindow.dismiss()
            booleans[1] = true
            booleans[3] = true
            page = 1
            getAssignmentList()
        })
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.setOnDismissListener { tv.setCompoundDrawables(null, null, zhankai, null) }
        popupWindow.showAsDropDown(tv)
    }

    private fun showTips() {
        val dialog = MaterialDialog(context)
        dialog.setTitle("提示")
        dialog.setMessage("作业已退回重做无法重新批阅，需要等待学员再次提交！")
        dialog.setPositiveButton("我知道了", null)
        dialog.show()
    }

    override fun onRefresh() {
        booleans[1] = true
        booleans[2] = false
        booleans[3] = false
        page = 1
        getAssignmentList()
    }

    override fun onLoadMore() {
        booleans[1] = false
        booleans[2] = true
        booleans[3] = false
        page += 1
        getAssignmentList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val type = data.getIntExtra("type", 0)
            if (type == 1) {  //退回重做
                if (selected >= 0 && selected < mDatas.size) {
                    mDatas[selected].state = "return"
                    adapter.notifyDataSetChanged()
                }
            } else if (type == 2) {
                val score = data.getIntExtra("score", 0)
                if (selected >= 0 && selected < mDatas.size) {
                    mDatas[selected].state = "complete"
                    mDatas[selected].responseScore = score.toDouble()
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private class AssignmentAdapter(private val context: Context, mDatas: List<MAssignmentEntity>, private var selectItem: Int) : BaseArrayRecyclerAdapter<MAssignmentEntity>(mDatas) {

        override fun bindView(viewtype: Int): Int {
            return R.layout.popupwindow_dictionary_item
        }

        override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: MAssignmentEntity, position: Int) {
            val tvDict = holder.obtainView<TextView>(R.id.tvDict)
            tvDict.text = entity.title
            if (selectItem == position) {
                tvDict.setTextColor(ContextCompat.getColor(context, R.color.defaultColor))
            } else {
                tvDict.setTextColor(ContextCompat.getColor(context, R.color.gray))
            }
        }

        fun setSelectItem(selectItem: Int) {
            this.selectItem = selectItem
            notifyDataSetChanged()
        }
    }

    private class StateAdapter(private val context: Context, mDatas: List<State>, private var selectItem: Int) : BaseArrayRecyclerAdapter<State>(mDatas) {
        override fun bindView(viewtype: Int): Int {
            return R.layout.popupwindow_dictionary_item
        }

        override fun onBindHoder(holder: RecyclerHolder, state: State, position: Int) {
            val tvDict = holder.obtainView<TextView>(R.id.tvDict)
            tvDict.text = state.content
            if (selectItem == position) {
                tvDict.setTextColor(ContextCompat.getColor(context, R.color.defaultColor))
            } else {
                tvDict.setTextColor(ContextCompat.getColor(context, R.color.gray))
            }
        }

        fun setSelectItem(selectItem: Int) {
            this.selectItem = selectItem
            notifyDataSetChanged()
        }
    }
}

