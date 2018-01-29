package com.recycler.coverflow.viewpager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import com.recycler.coverflow.R
import java.util.*

/**
 * 嵌套ViewPager Demo
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @version RecyclerCoverFlow
 * @Datetime 2017-07-26 15:05
 * @since RecyclerCoverFlow
 */

class ViewpagerActivity : AppCompatActivity() {

  private lateinit var mViewPager: ViewPager
  private lateinit var mAdapter: ViewPagerAdapter

  private val mFragments = ArrayList<Fragment>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_viewpager)
    newFragment()
    initViewPager()
  }

  private fun newFragment() {
    mFragments.add(MyFragment.newInstance())
    mFragments.add(MyFragment.newInstance())
    mFragments.add(MyFragment.newInstance())
    mFragments.add(MyFragment.newInstance())
  }

  private fun initViewPager() {
    mViewPager = findViewById(R.id.viewpager)
    mAdapter = ViewPagerAdapter(supportFragmentManager)
    mViewPager.adapter = mAdapter
  }

  internal inner class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
      return mFragments.size
    }

    override fun getItem(position: Int): Fragment {
      return mFragments[position]
    }
  }
}
