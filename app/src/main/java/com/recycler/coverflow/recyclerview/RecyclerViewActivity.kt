package com.recycler.coverflow.recyclerview

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

import com.recycler.coverflow.R

/**
 * 嵌套RecyclerView Demo
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @version RecyclerCoverFlow
 * @Datetime 2017-08-05 10:50
 * @since RecyclerCoverFlow
 */

class RecyclerViewActivity : AppCompatActivity() {
  private lateinit var mList: RecyclerView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_recyclerview)
    initList()
  }

  private fun initList() {
    mList = findViewById(R.id.list)
    mList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    mList.adapter = ListAdapter()
  }
}
