package recycler.coverflow

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator


/**
 * Cover Flow layout class
 *
 * Layout the Item by overriding the LayoutManger layout method [(RecyclerView.Recycler, RecyclerView.State)][.onLayoutChildren]
 * and recycle the Item beyond the screen
 *
 * Rolling horizontally by overriding [(int, RecyclerView.Recycler, RecyclerView.State)][.scrollHorizontallyBy]
 * in LayoutManger
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @version V1.0
 * @Datetime 2017-04-18
 *
 * @author blueMix (a.bluemix@gmail.com)
 * @version V1.1
 * @Datetime 2018-01-29
 */

class CoverFlowLayoutManger(isFlat: Boolean, isGreyItem: Boolean,
                            isAlphaItem: Boolean, cstInterval: Float) : RecyclerView.LayoutManager() {

  private val TAG = CoverFlowLayoutManger::class.java.simpleName


  /**
   * Sliding total offset
   */
  private var mOffsetAll = 0

  /**
   * Item wide
   */
  private var mDecoratedChildWidth = 0

  /**
   *  Item height
   */
  private var mDecoratedChildHeight = 0

  /**
   * Item interval and item width ratio
   */
  private var mIntervalRatio = 0.35f

  /**
   * Initial ItemX coordinates
   */
  private var mStartX = 0

  /**
   * The starting Item Y coordinates
   */
  private var mStartY = 0

  /**
   * Save all Item information about the offset up and down
   */
  private val mAllItemFrames = SparseArray<Rect>()

  /**
   * Record Item has appeared on the screen and has not been recovered.
   * true indicates that it has appeared on screen and has not been recycled yet
   */
  private val mHasAttachedItems = SparseBooleanArray()

  /**
   * RecyclerView Item Recycler
   */
  private var mRecycle: RecyclerView.Recycler? = null

  /**
   * RecyclerView's state machine
   */
  private var mState: RecyclerView.State? = null

  /**
   * Scroll animation
   */
  private var mAnimation: ValueAnimator? = null

  /**
   * Item is showing in the middle
   */
  /**
   * Get the selected Item location
   */
  var selectedPos = 0
    private set

  /**
   * The previous one is showing in the middle of the Item
   */
  private var mLastSelectPosition = 0

  /**
   * Select monitor
   */
  private var mSelectedListener: OnSelected? = null

  /**
   * Whether the plane is rolling, there is no overlay between items, there is no zoom
   */
  private var mIsFlatFlow = false

  /**
   * Whether to start Item gradation gradient
   */
  private var mItemGradualGrey = false

  /**
   * Whether to start Item half-graded
   */
  private var mItemGradualAlpha = false


  /**
   * Get the entire layout's horizontal space size
   */
  private val horizontalSpace: Int
    get() = width - paddingRight - paddingLeft

  /**
   * Get the entire layout of the vertical space size
   */
  private val verticalSpace: Int
    get() = height - paddingBottom - paddingTop

  /**
   * Get the maximum offset
   */
  private val maxOffset: Float
    get() = (itemCount - 1) * intervalDistance

  /**
   * Get the Item interval
   */
  private val intervalDistance: Float
    get() = mDecoratedChildWidth * mIntervalRatio

  /**
   * Get the first visible Item position
   *
   * Note: This Item is the first item drawn in the visible area, possibly obscured by the second item
   */
  val firstVisiblePosition: Int
    get() {
      val pos = (0 until mHasAttachedItems.size())
          .takeWhile { !mHasAttachedItems.get(it) }
          .count()
      return pos
    }

  /**
   * Note: This Item is drawn in the visible area of the last Item, may be obscured by the penultimate Item
   */
  val lastVisiblePosition: Int
    get() {
      var pos = mHasAttachedItems.size() - 1
      (mHasAttachedItems.size() - 1 downTo 1)
          .takeWhile { !mHasAttachedItems.get(it) }
          .forEach { pos-- }
      return pos
    }

  /**
   * Get the maximum number of display items in the visible range
   */
  val maxVisibleCount: Int
    get() {
      val oneSide = ((horizontalSpace - mStartX) / intervalDistance).toInt()
      return oneSide * 2 + 1
    }

  /**
   * Get the middle position
   *
   * Note: This method is mainly used for [(int, int)][RecyclerCoverFlow.getChildDrawingOrder]
   * to determine the middle position
   *
   *  Call [()][.getSelectedPos] if you need to get the selected Item position)
   */
  val centerPosition: Int
    get() {
      var pos = (mOffsetAll / intervalDistance).toInt()
      val more = (mOffsetAll % intervalDistance).toInt()
      if (more > intervalDistance * 0.5f) pos++
      return pos
    }

  init {
    mIsFlatFlow = isFlat
    mItemGradualGrey = isGreyItem
    mItemGradualAlpha = isAlphaItem
    if (cstInterval >= 0) {
      mIntervalRatio = cstInterval
    } else {
      if (mIsFlatFlow) {
        mIntervalRatio = 1.1f
      }
    }
  }

  override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
    return RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
  }

  override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
    // If there is no item, return directly
    // Skip preLayout, preLayout is mainly used to support animation
    if (itemCount <= 0 || state!!.isPreLayout) {
      mOffsetAll = 0
      return
    }
    mAllItemFrames.clear()
    mHasAttachedItems.clear()

    // Get sub view of the width and height, the width and height of the item here are the same,
    // so only need to make a measurement)
    val scrap = recycler!!.getViewForPosition(0)
    addView(scrap)
    measureChildWithMargins(scrap, 0, 0)
    // Calculate the width and height of the measurement layout
    mDecoratedChildWidth = getDecoratedMeasuredWidth(scrap)
    mDecoratedChildHeight = getDecoratedMeasuredHeight(scrap)
    mStartX = Math.round((horizontalSpace - mDecoratedChildWidth) * 1.0f / 2)
    mStartY = Math.round((verticalSpace - mDecoratedChildHeight) * 1.0f / 2)

    var offset = mStartX.toFloat()
    for (i in 0 until itemCount) { // Store all item specific location
      var frame: Rect? = mAllItemFrames.get(i)
      if (frame == null) {
        frame = Rect()
      }
      frame.set(Math.round(offset), mStartY, Math.round(offset + mDecoratedChildWidth), mStartY + mDecoratedChildHeight)
      mAllItemFrames.put(i, frame)
      mHasAttachedItems.put(i, false)


      // Accumulation of the original position, otherwise the greater the error behind
      offset += intervalDistance
    }

    // Before layout, Detach all subviews and place them in the Scrap cache
    detachAndScrapAttachedViews(recycler)
    if ((mRecycle == null || mState == null) && // When calling smoothScrollToPosition or scrollToPosition before
        // initialization, only the position is recorded)
        selectedPos != 0) {                 // Therefore, the need to initialize the scroll to the corresponding location
      mOffsetAll = calculateOffsetForPosition(selectedPos)
      onSelectedCallBack()
    }

    layoutItems(recycler, state, SCROLL_RIGHT)

    mRecycle = recycler
    mState = state
  }

  override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler?,
                                    state: RecyclerView.State?): Int {

    mAnimation?.let {
      if (mAnimation?.isRunning == true) mAnimation?.cancel()
    }

    var travel = dx
    if (dx + mOffsetAll < 0) {
      travel = -mOffsetAll
    } else if (dx + mOffsetAll > maxOffset) {
      travel = (maxOffset - mOffsetAll).toInt()
    }
    mOffsetAll += travel // Accumulated offset

    layoutItems(recycler, state, if (dx > 0) SCROLL_RIGHT else SCROLL_LEFT)
    return travel
  }

  /**
   * Layout Item
   *
   * 注意：1，First clear the item that has exceeded the screen
   *
   *      2，Then draw the item that can be displayed on the screen
   */
  private fun layoutItems(recycler: RecyclerView.Recycler?,
                          state: RecyclerView.State?, scrollDirection: Int) {

    if (state!!.isPreLayout) return

    val displayFrame = Rect(mOffsetAll, 0, mOffsetAll + horizontalSpace, verticalSpace)

    (0 until childCount)
        .map { getChildAt(it) }
        .forEach {
          it?.let {
            val position = getPosition(it)

            // Item is not in the display area, indicating the need to recycle
            if (!Rect.intersects(displayFrame, mAllItemFrames.get(position))) {
              removeAndRecycleView(it, recycler!!) // Recycling View out of the screen
              mHasAttachedItems.put(position, false)
            } else { // Item is still in the display area, updating the position of the slid Item
              layoutItem(it, mAllItemFrames.get(position)) // Update Item Location

              mHasAttachedItems.put(position, true)
            }
          }
        }

    for (i in 0 until itemCount) {
      if (Rect.intersects(displayFrame, mAllItemFrames.get(i)) && !mHasAttachedItems.get(i)) { // Reload the Item in the visible range
        val scrap = recycler!!.getViewForPosition(i)
        measureChildWithMargins(scrap, 0, 0)

        // Scroll left, the new Item needs to be added at the top
        if (scrollDirection == SCROLL_LEFT || mIsFlatFlow) {
          addView(scrap, 0)
        } else { // Scroll right to add the new item to be added at the end
          addView(scrap)
        }
        layoutItem(scrap, mAllItemFrames.get(i)) // This lays out the item
        mHasAttachedItems.put(i, true)
      }
    }
  }



  /**
   * Layout Item Location
   *
   * @param child  Item to be laid out
   * @param frame  location information
   */
  private fun layoutItem(child: View, frame: Rect) {
    layoutDecorated(child,
        frame.left - mOffsetAll,
        frame.top,
        frame.right - mOffsetAll,
        frame.bottom)

    // Scaling is not done without the normal scrolling of the plane

    if (!mIsFlatFlow) {

      val x = frame.left - mOffsetAll
      val translation = computeOffset(x)

      child.translationX = translation

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        child.translationZ = (-getPosition(child)).toFloat()
      }

      val scale = computeScale(x)

      child.scaleY = scale
      child.scaleX = scale

    }

    if (mItemGradualAlpha) {
      child.alpha = computeAlpha(frame.left - mOffsetAll)
    }

    if (mItemGradualGrey) {
      greyItem(child, frame)
    }
  }

  /**
   * Change the gray value of Item
   *
   * @param child Need to set the gray value of the Item
   * @param frame location information
   */
  private fun greyItem(child: View, frame: Rect) {
    val value = computeGreyScale(frame.left - mOffsetAll)
    val cm = ColorMatrix(
          floatArrayOf(value, 0f, 0f, 0f, 120 * (1 - value), 0f, value, 0f, 0f, 120 * (1 - value),
              0f, 0f, value, 0f, 120 * (1 - value), 0f, 0f, 0f, 1f, 250 * (1 - value)))

    // Create a paint object with color matrix
    val greyPaint = Paint()
    greyPaint.colorFilter = ColorMatrixColorFilter(cm)

    // Create a hardware layer with the grey paint
    child.setLayerType(View.LAYER_TYPE_HARDWARE, greyPaint)
    if (value >= 1) {
      // Remove the hardware layer
      child.setLayerType(View.LAYER_TYPE_NONE, null)
    }

  }

  override fun onScrollStateChanged(state: Int) {
    super.onScrollStateChanged(state)
    when (state) {
      RecyclerView.SCROLL_STATE_IDLE ->
        // When the scroll stops
        fixOffsetWhenFinishScroll()
      RecyclerView.SCROLL_STATE_DRAGGING -> {
      }
      RecyclerView.SCROLL_STATE_SETTLING -> {
      }
    }// Drag and drop when scrolling
    // Animated scrolling
  }

  override fun scrollToPosition(position: Int) {
    if (position < 0 || position > itemCount - 1) return
    mOffsetAll = calculateOffsetForPosition(position)

    // If RecyclerView has not finished initialization, first record the location to be scrolled
    if (mRecycle == null || mState == null) {
      selectedPos = position
    } else {
      layoutItems(mRecycle, mState, if (position > selectedPos) SCROLL_RIGHT else SCROLL_LEFT)
      onSelectedCallBack()
    }
  }

  override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
    val finalOffset = calculateOffsetForPosition(position)
    // If RecyclerView has not been initialized finished, first record the location to be scrolled
    if (mRecycle == null || mState == null) {
      selectedPos = position
    } else {
      startScroll(mOffsetAll, finalOffset)
    }
  }

  override fun canScrollHorizontally(): Boolean {
    return true
  }

  override fun onAdapterChanged(oldAdapter: RecyclerView.Adapter<*>?, newAdapter: RecyclerView.Adapter<*>?) {
    removeAllViews()
    mRecycle = null
    mState = null
    mOffsetAll = 0
    selectedPos = 0
    mLastSelectPosition = 0
    mHasAttachedItems.clear()
    mAllItemFrames.clear()
  }

  /**
   * Calculate Item scaling factor
   *
   * @param x Item offset
   * @return Zoom factor
   */
  private fun computeScale(x: Int): Float {

    return (horizontalSpace - x).toFloat() /1024f
  }


  /**
   * Calculate Item scaling factor
   *
   * @param x Item offset
   * @return translation offset
   */
  private fun computeOffset(x: Int): Float {
    val x1 = (x - mStartX).toFloat()
    return if (x1 > 0) x1 else x1 * 2.5f
  }


  /**
   *  Calculate Item gray value
   *
   * @param x Item offset
   * @return Gray scale
   */
  private fun computeGreyScale(x: Int): Float {
    val itemMidPos = (x + mDecoratedChildWidth / 2).toFloat() // item midpoint x coordinate

    // item midpoint distance from the midpoint of the control
    val itemDx2Mid = Math.abs(itemMidPos - horizontalSpace / 2)
    var value = 1 - itemDx2Mid * 1.0f / (horizontalSpace / 2)
    if (value < 0.1) value = 0.1f
    if (value > 1) value = 1f
    value = Math.pow(value.toDouble(), .8).toFloat()
    return value
  }

  /**
   * Calculate Item Alpha
   *
   * @param x  Item offset
   * @return Alpha factor
   */
  private fun computeAlpha(x: Int): Float {
    var alpha = 1 - Math.abs(x - mStartX) * 1.0f / Math.abs(mStartX + mDecoratedChildWidth / mIntervalRatio)
    if (alpha < 0.1f) alpha = 0.1f
    if (alpha > 1) alpha = 1.0f
    return alpha
  }

  /**
   * Calculate the location of the Item offset
   *
   * @param position To calculate the Item location
   */
  private fun calculateOffsetForPosition(position: Int): Int {
    return Math.round(intervalDistance * position)
  }

  /**
   * After the correction stops rolling, Item scrolls to the middle position
   */
  private fun fixOffsetWhenFinishScroll() {
    var scrollN = (mOffsetAll * 1.0f / intervalDistance).toInt()
    val moreDx = mOffsetAll % intervalDistance
    if (moreDx > intervalDistance * 0.5) {
      scrollN++
    }
    val finalOffset = (scrollN * intervalDistance).toInt()
    startScroll(mOffsetAll, finalOffset)
    selectedPos = Math.round(finalOffset * 1.0f / intervalDistance)
  }

  /**
   * Scroll to the specified X-axis position
   *
   * @param from X-axis direction of the starting point of the offset
   * @param to   X-axis direction of the end of the offset
   */
  private fun startScroll(from: Int, to: Int) {
    mAnimation.let {
      if (mAnimation?.isRunning == true) {
        mAnimation?.cancel()
      }
    }

    val direction = if (from < to) SCROLL_RIGHT else SCROLL_LEFT
    mAnimation = ValueAnimator.ofFloat(from.toFloat(), to.toFloat())
    mAnimation?.duration = 500
    mAnimation?.interpolator = DecelerateInterpolator()
    mAnimation?.addUpdateListener { animation ->
      mOffsetAll = Math.round(animation.animatedValue as Float)
      layoutItems(mRecycle, mState, direction)
    }
    mAnimation?.addListener(object : Animator.AnimatorListener {
      override fun onAnimationStart(animation: Animator) {

      }

      override fun onAnimationEnd(animation: Animator) {
        onSelectedCallBack()
      }

      override fun onAnimationCancel(animation: Animator) {

      }

      override fun onAnimationRepeat(animation: Animator) {

      }
    })
    mAnimation?.start()
  }

  /**
   * Calculate the current selected location, and callback
   */
  private fun onSelectedCallBack() {
    selectedPos = Math.round(mOffsetAll / intervalDistance)
    if (mSelectedListener != null && selectedPos != mLastSelectPosition) {
      mSelectedListener!!.onItemSelected(selectedPos)
    }
    mLastSelectPosition = selectedPos
  }

  /**
   * Set monitor selected
   *
   * @param l Monitor interface
   */
  fun setOnSelectedListener(l: OnSelected) {
    mSelectedListener = l
  }

  /**
   * Check the monitor interface
   */
  interface OnSelected {
    /**
     * Monitor selected callback
     *
     * @param position The location of the Item displayed in the middle
     */
    fun onItemSelected(position: Int)
  }

  class Builder {
    private var isFlat = false
    private var isGreyItem = false
    private var isAlphaItem = false
    private var cstIntervalRatio = -1f

    fun setFlat(flat: Boolean): Builder {
      isFlat = flat
      return this
    }

    fun setGreyItem(greyItem: Boolean): Builder {
      isGreyItem = greyItem
      return this
    }

    fun setAlphaItem(alphaItem: Boolean): Builder {
      isAlphaItem = alphaItem
      return this
    }

    fun setIntervalRatio(ratio: Float): Builder {
      cstIntervalRatio = ratio
      return this
    }

    fun build(): CoverFlowLayoutManger {
      return CoverFlowLayoutManger(isFlat, isGreyItem,
          isAlphaItem, cstIntervalRatio)
    }
  }

  companion object {

    /**
     * Sliding direction: left
     */
    private val SCROLL_LEFT = 1

    /**
     * Sliding direction: right
     */
    private val SCROLL_RIGHT = 2
  }
}