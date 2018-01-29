package com.recycler.coverflow.viewpager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.recycler.coverflow.R
import com.recycler.coverflow.SweetsAdapter

import me.bluemix.LusciousLayoutManger
import me.bluemix.LusciousRecyclerView

/**
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @version RecyclerCoverFlow
 * @Datetime 2017-07-26 15:11
 * @since RecyclerCoverFlow
 */

class MyFragment : Fragment() {
  private lateinit var mList: LusciousRecyclerView
  private lateinit var mIndex: TextView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val rootView = inflater.inflate(R.layout.fragment, container, false)
    initList(rootView)
    return rootView
  }

  private fun initList(rootView: View) {
    mList = rootView.findViewById<View>(R.id.list) as LusciousRecyclerView
    mIndex = rootView.findViewById<View>(R.id.index) as TextView
    //        mList.setFlatFlow(true); //平面滚动
    mList.setGreyItem(true) //设置灰度渐变
    //        mList.setAlphaItem(true); //设置半透渐变
    mList.adapter = SweetsAdapter(activity!!)
    mList.setOnItemSelectedListener(object : LusciousLayoutManger.OnSelected {
      override fun onItemSelected(position: Int) {
        mIndex.text = (position + 1).toString() + "/" + mList!!.layoutManager.itemCount
      }
    })
  }

  companion object {


    fun newInstance(): Fragment {
      return MyFragment()
    }
  }
}
