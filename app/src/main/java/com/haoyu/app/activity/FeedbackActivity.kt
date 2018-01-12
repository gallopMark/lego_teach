package com.haoyu.app.activity

import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.base.BaseResponseResult
import com.haoyu.app.dialog.MaterialDialog
import com.haoyu.app.lego.teach.R
import com.haoyu.app.utils.Constants
import com.haoyu.app.utils.OkHttpClientManager
import com.haoyu.app.view.AppToolBar
import okhttp3.Request
import java.util.*

/**
 * 创建日期：2018/1/12.
 * 描述:意见反馈
 * 作者:xiaoma
 */
class FeedbackActivity : BaseActivity() {
    lateinit var context: FeedbackActivity
    lateinit var et_problem_detail: EditText
    override fun setLayoutResID(): Int {
        return R.layout.activity_feekback
    }

    override fun initView() {
        context = this
        setToolBar()
        et_problem_detail = findViewById(R.id.et_problem_detail)
        val bt_submit = findViewById<Button>(R.id.bt_submit)
        bt_submit.setOnClickListener({
            val et_text = et_problem_detail.text.toString().trim()
            if (TextUtils.isEmpty(et_text)) {
                showMaterialDialog("提示", "请输入问题描述！")
            } else {
                commit(et_text)
            }
        })
    }

    private fun setToolBar() {
        val toolBar = findViewById<AppToolBar>(R.id.toolBar)
        toolBar.setTitle_text("意见反馈")
        toolBar.setOnLeftClickListener { finish() }
    }

    private fun commit(problem_detail: String) {
        val url = Constants.OUTRT_NET + "/m/feedback/create"
        val map = HashMap<String, String>()
        map.put("content", problem_detail)
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
                if (response != null && response.getResponseCode() != null && response.getResponseCode() == "00") {
                    showTipsDialog()
                }
            }
        }, map))
    }

    private fun showTipsDialog() {
        val dialog = MaterialDialog(context)
        dialog.setTitle("反馈结果")
        dialog.setMessage("感谢您提交的反馈信息！\n您的意见将有助于改进我们的平台。")
        dialog.setNegativeButton("返回上一级") { v, dialog -> finish() }
        dialog.setPositiveButton("留在此页", null)
        dialog.show()
    }
}