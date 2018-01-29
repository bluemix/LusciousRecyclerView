package me.bluemix

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import me.bluemix.LusciousLayoutManger

/**
 * Inherit RecyclerView
 * Rewrite [(int, int)][.getChildDrawingOrder] Control the drawing order of Item
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @version V1.0
 * @Datetime 2017-04-18
 *
 * @author blueMix (a.bluemix@gmail.com)
 * @version V1.1
 * @Datetime 2018-01-29
 */

class LusciousRecyclerView : RecyclerView {
  /**
   * Press the X axis coordinate
   */
  private var mDownX: Float = 0.toFloat()

  /**
   * Layout Builder
   */
  private lateinit var mManagerBuilder: LusciousLayoutManger.Builder

  /**
   * Get LayoutManger, and cast to LusciousLayoutManger
   */
  val coverFlowLayout: LusciousLayoutManger
    get() = layoutManager as LusciousLayoutManger

  /**
   * Get the selected Item location
   */
  val selectedPos: Int
    get() = coverFlowLayout.selectedPos

  constructor(context: Context) : super(context) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
    init()
  }

  private fun init() {
    createManageBuilder()
    layoutManager = mManagerBuilder.build()
    isChildrenDrawingOrderEnabled = true //开启重新排序 (Open reordering)
    overScrollMode = View.OVER_SCROLL_NEVER
  }

  /**
   * Create a layout builder
   */
  private fun createManageBuilder() {
    mManagerBuilder = LusciousLayoutManger.Builder()
  }

  /**
   * Whether to set the normal plane rolling
   * @param isFlat true: flat scroll; false: overlay scaled scroll
   */
  fun setFlatFlow(isFlat: Boolean) {
    createManageBuilder()
    mManagerBuilder.setFlat(isFlat)
    layoutManager = mManagerBuilder.build()
  }

  /**
   * Set Item gray gradient
   * @param greyItem true: Item gray scale gradient; false: Item gray scale
   */
  fun setGreyItem(greyItem: Boolean) {
    createManageBuilder()
    mManagerBuilder.setGreyItem(greyItem)
    layoutManager = mManagerBuilder.build()
  }

  /**
   * Set Item gray gradient
   * @param alphaItem true: Item Translucent; false: Item No change in transparency
   */
  fun setAlphaItem(alphaItem: Boolean) {
    createManageBuilder()
    mManagerBuilder.setAlphaItem(alphaItem)
    layoutManager = mManagerBuilder.build()
  }

  /**
   * Set Item interval ratio
   * @param intervalRatio The item interval ratio.
   * Name: item width x intervalRatio
   */
  fun setIntervalRatio(intervalRatio: Float) {
    createManageBuilder()
    mManagerBuilder.setIntervalRatio(intervalRatio)
    layoutManager = mManagerBuilder.build()
  }

  override fun setLayoutManager(layout: RecyclerView.LayoutManager) {
    if (layout !is LusciousLayoutManger) {
      throw IllegalArgumentException("The layout manager must be LusciousLayoutManger")
    }
    super.setLayoutManager(layout)
  }

  override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
    var center = coverFlowLayout.centerPosition - coverFlowLayout.firstVisiblePosition // Calculate the middle of all Item being displayed
    if (center < 0)
      center = 0
    else if (center > childCount) center = childCount
    val order: Int
    if (i == center) {
      order = childCount - 1
    } else if (i > center) {
      order = center + childCount - 1 - i
    } else {
      order = i
    }
    return order
  }

  /**
   * Set monitor selected
   * @param l Listening interface
   */
  fun setOnItemSelectedListener(l: LusciousLayoutManger.OnSelected) {
    coverFlowLayout.setOnSelectedListener(l)
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    when (ev.action) {
      MotionEvent.ACTION_DOWN -> {
        mDownX = ev.x
        // Set parent class does not intercept sliding events
        parent.requestDisallowInterceptTouchEvent(true)
      }
      MotionEvent.ACTION_MOVE ->
        if (ev.x > mDownX && coverFlowLayout.centerPosition == 0 ||
            ev.x < mDownX && coverFlowLayout.centerPosition == coverFlowLayout.itemCount - 1) {
        // If it is sliding to the front and the end, open the parent sliding event to intercept
        parent.requestDisallowInterceptTouchEvent(false)
      } else {
        // Swipe to the middle to set the parent class to not block the swipe event
        parent.requestDisallowInterceptTouchEvent(true)
      }
    }
    return super.dispatchTouchEvent(ev)
  }
}
