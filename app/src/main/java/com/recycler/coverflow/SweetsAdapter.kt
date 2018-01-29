package com.recycler.coverflow

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.recycler.coverflow.recyclerview.ItemModel

/**
 * Created by chenxiaoping on 2017/3/28.
 */

class SweetsAdapter( val mContext: Context) : RecyclerView.Adapter<SweetsAdapter.ViewHolder>() {

  val sweets = listOf(
      ItemModel(R.drawable.cake1, "Sweet 1", "$10"),
      ItemModel(R.drawable.cake2, "Sweet 2", "$15"),
      ItemModel(R.drawable.cake3, "Sweet 3", "$12"),
      ItemModel(R.drawable.cake4, "Sweet 4", "$9"),
      ItemModel(R.drawable.cake5, "Sweet 5", "$15"),
      ItemModel(R.drawable.cake6, "Sweet 6", "$5"),
      ItemModel(R.drawable.cake7, "Sweet 7", "$7"),
      ItemModel(R.drawable.cake8, "Sweet 8", "$6"),
      ItemModel(R.drawable.cake9, "Sweet 9", "$20"),
      ItemModel(R.drawable.cake10, "Sweet 10", "$13"),
      ItemModel(R.drawable.cake11, "Sweet 11", "$3"),
      ItemModel(R.drawable.cake12, "Sweet 12", "$7")
  )

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val v = LayoutInflater.from(mContext).inflate(R.layout.layout_item, parent, false)
    return ViewHolder(v)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val sweet = sweets[position % sweets.size]
    Glide.with(mContext).load(sweet.thumb)
        .into(holder.img)
    holder.name.text = sweet.name
    holder.price.text = sweet.price
    holder.itemView.setOnClickListener { Toast.makeText(mContext, "Clicked on：" + position, Toast.LENGTH_SHORT).show() }
  }

  override fun getItemCount(): Int {
    return 50
  }

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var img: ImageView = itemView.findViewById(R.id.thumb) as ImageView
    var name: Button = itemView.findViewById(R.id.sweetName) as Button
    var price: Button = itemView.findViewById(R.id.price) as Button

  }
}
