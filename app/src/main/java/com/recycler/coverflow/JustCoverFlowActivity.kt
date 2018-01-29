package com.recycler.coverflow

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView

import me.bluemix.LusciousLayoutManger
import me.bluemix.LusciousRecyclerView

class JustCoverFlowActivity : AppCompatActivity() {

  private lateinit var mList: LusciousRecyclerView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_just_coverflow)
    initList()
  }

  private fun initList() {
    mList = findViewById(R.id.list)
    //        mList.setFlatFlow(true); //平面滚动
    //        mList.setGreyItem(true); //设置灰度渐变
    //        mList.setAlphaItem(true); //设置半透渐变
    mList.adapter = SweetsAdapter(this)
    mList.setOnItemSelectedListener(object : LusciousLayoutManger.OnSelected {
      override fun onItemSelected(position: Int) {
        (findViewById<View>(R.id.index) as TextView).text = (position + 1).toString() + "/" + mList!!.layoutManager.itemCount
      }
    })
  }
}
