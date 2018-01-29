package com.recycler.coverflow

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.recycler.coverflow.recyclerview.RecyclerViewActivity
import com.recycler.coverflow.viewpager.ViewpagerActivity

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
  }

  fun onJustCoverFlowClick(view: View) {
    val intent = Intent(this, JustCoverFlowActivity::class.java)
    startActivity(intent)
  }

  fun onViewPagerClick(view: View) {
    val intent = Intent(this, ViewpagerActivity::class.java)
    startActivity(intent)
  }

  fun onRecyclerViewClick(view: View) {
    val intent = Intent(this, RecyclerViewActivity::class.java)
    startActivity(intent)
  }

}
