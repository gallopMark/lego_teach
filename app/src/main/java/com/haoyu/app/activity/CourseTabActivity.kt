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
import java.util.*

/**
 * 创建日期：2018/1/12.
 * 描述:课程学习首页
 * 作者:xiaoma
 */
class CourseTabActivity : BaseActivity() {
    private val context = this
    private val PAGE_COURSE = 0  //学习
    private val PAGE_RESOURCES = 1    //资源
    private val PAGE_DISCUSSION = 2   //讨论
    private val PAGE_QUESTION = 3 //问答
    private val PAGE_HOMEWORK = 4 //作业
    private val PAGE_STATISTICS = 5 //统计
    private val fragments = ArrayList<Fragment>()
    private var courseId: String? = null
    private var pageCourseFragmemt: PageCourseFragment? = null
    private var pageResourcesFragment: PageResourcesFragment? = null
    private var pageDiscussionFragment: PageDiscussionFragment? = null
    private var pageQuestionFragment: PageQuestionFragment? = null
    private var pageHomeWorkFragment: PageHomeWorkFragment? = null
    private var pageStatisticsFragment: PageStatisticsFragment? = null
    override fun setLayoutResID(): Int {
        return R.layout.activity_course_tab
    }

    override fun initView() {
        setToolBar()
        courseId = intent.getStringExtra("courseId")
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        setTab(PAGE_COURSE)
        radioGroup.setOnCheckedChangeListener({ radioGroup, checkId ->
            when (checkId) {
                R.id.rb_section -> {
                    setTab(PAGE_COURSE)
                }
                R.id.rb_resources -> {
                    setTab(PAGE_RESOURCES)
                }
                R.id.rb_discuss -> {
                    setTab(PAGE_DISCUSSION)
                }
                R.id.rb_wenda -> {
                    setTab(PAGE_QUESTION)
                }
                R.id.rb_homeWork -> {
                    setTab(PAGE_HOMEWORK)
                }
                R.id.rb_Statistics -> {
                    setTab(PAGE_STATISTICS)
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
            PAGE_COURSE -> {
                if (pageCourseFragmemt == null) {
                    pageCourseFragmemt = PageCourseFragment()
                    val bundle = Bundle()
                    bundle.putString("entityId", courseId)
                    pageCourseFragmemt!!.arguments = bundle
                    transaction.add(R.id.content, pageCourseFragmemt)
                    fragments.add(pageCourseFragmemt!!)
                } else {
                    transaction.show(pageCourseFragmemt!!)
                }
            }
            PAGE_RESOURCES -> {
                if (pageResourcesFragment == null) {
                    pageResourcesFragment = PageResourcesFragment()
                    val bundle = Bundle()
                    bundle.putString("entityId", courseId)
                    pageResourcesFragment!!.arguments = bundle
                    transaction.add(R.id.content, pageResourcesFragment!!)
                    fragments.add(pageResourcesFragment!!)
                } else {
                    transaction.show(pageResourcesFragment!!)
                }
            }
            PAGE_DISCUSSION -> {
                if (pageDiscussionFragment == null) {
                    pageDiscussionFragment = PageDiscussionFragment()
                    val bundle = Bundle()
                    bundle.putString("entityId", courseId)
                    pageDiscussionFragment!!.arguments = bundle
                    transaction.add(R.id.content, pageDiscussionFragment!!)
                    fragments.add(pageDiscussionFragment!!)
                } else {
                    transaction.show(pageDiscussionFragment!!)
                }
            }
            PAGE_QUESTION -> {
                if (pageQuestionFragment == null) {
                    pageQuestionFragment = PageQuestionFragment()
                    val bundle = Bundle()
                    bundle.putString("entityId", courseId)
                    pageQuestionFragment!!.arguments = bundle
                    transaction.add(R.id.content, pageQuestionFragment!!)
                    fragments.add(pageQuestionFragment!!)
                } else {
                    transaction.show(pageQuestionFragment!!)
                }
            }
            PAGE_HOMEWORK -> {
                if (pageHomeWorkFragment == null) {
                    pageHomeWorkFragment = PageHomeWorkFragment()
                    val bundle = Bundle()
                    bundle.putString("entityId", courseId)
                    pageHomeWorkFragment!!.arguments = bundle
                    transaction.add(R.id.content, pageHomeWorkFragment!!)
                    fragments.add(pageHomeWorkFragment!!)
                } else {
                    transaction.show(pageHomeWorkFragment)
                }
            }
            PAGE_STATISTICS -> {
                if (pageStatisticsFragment == null) {
                    pageStatisticsFragment = PageStatisticsFragment()
                    val bundle = Bundle()
                    bundle.putString("entityId", courseId)
                    pageStatisticsFragment!!.arguments = bundle
                    transaction.add(R.id.content, pageStatisticsFragment!!)
                    fragments.add(pageStatisticsFragment!!)
                } else {
                    transaction.show(pageStatisticsFragment!!)
                }
            }
        }
        transaction.commit()
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        for (fragment in fragments) {
            if (fragment != null) {
                transaction.hide(fragment)
            }
        }
    }
}