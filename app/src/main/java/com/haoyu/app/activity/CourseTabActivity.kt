package com.haoyu.app.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.widget.RadioGroup
import com.haoyu.app.base.BaseActivity
import com.haoyu.app.fragment.*
import com.haoyu.app.lego.teach.R
import com.haoyu.app.view.AppToolBar

/**
 * 创建日期：2018/1/12.
 * 描述:课程学习首页
 * 作者:xiaoma
 */
class CourseTabActivity : BaseActivity() {
    private val context = this
    private val tabs = intArrayOf(1,2,3,4,5,6) //学习、资源、讨论、问答、作业、统计
    private var courseId: String? = null
    private val fragments = arrayOfNulls<Fragment>(6)
    override fun setLayoutResID(): Int {
        return R.layout.activity_course_tab
    }

    override fun initView() {
        setToolBar()
        courseId = intent.getStringExtra("courseId")
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        setTab(tabs[0])
        radioGroup.setOnCheckedChangeListener({ _, checkId ->
            when (checkId) {
                R.id.rb_section -> {
                    setTab(tabs[0])
                }
                R.id.rb_resources -> {
                    setTab(tabs[1])
                }
                R.id.rb_discuss -> {
                    setTab(tabs[2])
                }
                R.id.rb_wenda -> {
                    setTab(tabs[3])
                }
                R.id.rb_homeWork -> {
                    setTab(tabs[4])
                }
                R.id.rb_Statistics -> {
                    setTab(tabs[5])
                }
            }
        })
    }

    private fun setToolBar() {
        val courseTitle = intent.getStringExtra("courseTitle")
        val toolBar = findViewById<AppToolBar>(R.id.toolBar)
        toolBar.setTitle_text(courseTitle)
        toolBar.setOnTitleClickListener(object : AppToolBar.TitleOnClickListener {
            override fun onLeftClick(view: View) {
                finish()
            }

            override fun onRightClick(view: View) {
                startActivity(Intent(context, AppDownloadActivity::class.java))
            }
        })
    }

    private fun setTab(index: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        hideFragments(transaction)
        when (index) {
            tabs[0] -> {
                if (fragments[0] == null) {
                    fragments[0] = PageCourseFragment()
                    fragments[0]?.let {
                        val bundle = Bundle()
                        bundle.putString("entityId", courseId)
                        it.arguments = bundle
                        transaction.add(R.id.content, it)
                    }
                } else {
                    transaction.show(fragments[0])
                }
            }
            tabs[1] -> {
                if (fragments[1] == null) {
                    fragments[1] = PageResourcesFragment()
                    fragments[1]?.let {
                        val bundle = Bundle()
                        bundle.putString("entityId", courseId)
                        it.arguments = bundle
                        transaction.add(R.id.content, it)
                    }
                } else {
                    transaction.show(fragments[1])
                }
            }
            tabs[2] -> {
                if (fragments[2] == null) {
                    fragments[2] = PageDiscussionFragment()
                    fragments[2]?.let {
                        val bundle = Bundle()
                        bundle.putString("entityId", courseId)
                        it.arguments = bundle
                        transaction.add(R.id.content, it)
                    }
                } else {
                    transaction.show(fragments[2])
                }
            }
            tabs[3] -> {
                if (fragments[3] == null) {
                    fragments[3] = PageQuestionFragment()
                    fragments[3]?.let {
                        val bundle = Bundle()
                        bundle.putString("entityId", courseId)
                        it.arguments = bundle
                        transaction.add(R.id.content, it)
                    }
                } else {
                    transaction.show(fragments[3])
                }
            }
            tabs[4] -> {
                if (fragments[4] == null) {
                    fragments[4] = PageHomeWorkFragment()
                    fragments[4]?.let {
                        val bundle = Bundle()
                        bundle.putString("entityId", courseId)
                        it.arguments = bundle
                        transaction.add(R.id.content, it)
                    }
                } else {
                    transaction.show(fragments[4])
                }
            }
            tabs[5] -> {
                if (fragments[5] == null) {
                    fragments[5] = PageStatisticsFragment()
                    fragments[5]?.let {
                        val bundle = Bundle()
                        bundle.putString("entityId", courseId)
                        it.arguments = bundle
                        transaction.add(R.id.content, it)
                    }
                } else {
                    transaction.show(fragments[5])
                }
            }
        }
        transaction.commit()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        for (i in 0 until fragments.size) {
            fragments[i]?.let {
                transaction.hide(it)
            }
        }
    }
}