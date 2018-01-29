package com.recycler.coverflow.recyclerview

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.recycler.coverflow.R
import com.recycler.coverflow.SweetsAdapter
import me.bluemix.LusciousLayoutManger
import me.bluemix.LusciousRecyclerView

/**
 * FIXME
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @version RecyclerCoverFlow
 * @Datetime 2017-08-05 10:57
 * @Copyright (c) 2017 中国邮政电子商务运营中心. All rights reserved
 * @since RecyclerCoverFlow
 */

class ListAdapter : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

  private val TYPE_COVER_FLOW = 1
  private val TYPE_TEXT = 2

  private var mCoverFlowPosition = 0

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    var view: View? = null
    if (viewType == TYPE_COVER_FLOW) {
      view = LayoutInflater.from(parent.context).inflate(R.layout.item_coverflow, parent, false)
    } else {
      view = LayoutInflater.from(parent.context).inflate(R.layout.item_text, parent, false)
    }
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val viewType = getItemViewType(position)
    when (viewType) {
      TYPE_COVER_FLOW -> intiCoverFlow(holder)
      TYPE_TEXT -> holder.text.text = position.toString() + ""
    }
  }

  private fun intiCoverFlow(holder: ViewHolder) {
    holder.coverFlow.adapter = SweetsAdapter(holder.itemView.context)
    holder.coverFlow.setOnItemSelectedListener(object : LusciousLayoutManger.OnSelected {
      override fun onItemSelected(position: Int) {
        mCoverFlowPosition = position
        holder.text.text = (position + 1).toString() + "/" + holder.coverFlow.layoutManager.itemCount
      }
    })
    holder.coverFlow.scrollToPosition(mCoverFlowPosition)
  }

  override fun getItemCount(): Int {
    return 50
  }

  override fun getItemViewType(position: Int): Int {
    return if (position == 0) {
      TYPE_COVER_FLOW
    } else {
      TYPE_TEXT
    }
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var text: TextView
    var coverFlow: LusciousRecyclerView

    init {
      text = itemView.findViewById<View>(R.id.text) as TextView
      coverFlow = itemView.findViewById<View>(R.id.cover_flow) as LusciousRecyclerView
    }
  }
}
