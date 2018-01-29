package recycler.coverflow;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Inherit RecyclerView
 * Rewrite {@link #getChildDrawingOrder (int, int)} Control the drawing order of Item
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @version V1.0
 * @Datetime 2017-04-18
 *
 * @author blueMix (a.bluemix@gmail.com)
 * @version V1.1
 * @Datetime 2018-01-29
 */

public class LusciousRecycler extends RecyclerView {
    /**
     * Press the X axis coordinate
     */
    private float mDownX;

    /**
     * Layout Builder
     */
    private LusciousRecycler.Builder mManagerBuilder;

    public LusciousRecycler(Context context) {
        super(context);
        init();
    }

    public LusciousRecycler(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LusciousRecycler(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        createManageBuilder();
        setLayoutManager(mManagerBuilder.build());
        setChildrenDrawingOrderEnabled(true); //开启重新排序 (Open reordering)
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    /**
     * Create a layout builder
     */
    private void createManageBuilder() {
        if (mManagerBuilder == null) {
            mManagerBuilder = new LusciousRecycler.Builder();
        }
    }

    /**
     * Whether to set the normal plane rolling
     * @param isFlat true: flat scroll; false: overlay scaled scroll
     */
    public void setFlatFlow(boolean isFlat) {
        createManageBuilder();
        mManagerBuilder.setFlat(isFlat);
        setLayoutManager(mManagerBuilder.build());
    }

    /**
     * Set Item gray gradient
     * @param greyItem true: Item gray scale gradient; false: Item gray scale
     */
    public void setGreyItem(boolean greyItem) {
        createManageBuilder();
        mManagerBuilder.setGreyItem(greyItem);
        setLayoutManager(mManagerBuilder.build());
    }

    /**
     * Set Item gray gradient
     * @param alphaItem true: Item Translucent; false: Item No change in transparency
     */
    public void setAlphaItem(boolean alphaItem) {
        createManageBuilder();
        mManagerBuilder.setAlphaItem(alphaItem);
        setLayoutManager(mManagerBuilder.build());
    }

    /**
     * Set Item interval ratio
     * @param intervalRatio The item interval ratio.
     *                      Name: item width x intervalRatio
     *
     */
    public void setIntervalRatio(float intervalRatio) {
        createManageBuilder();
        mManagerBuilder.setIntervalRatio(intervalRatio);
        setLayoutManager(mManagerBuilder.build());
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        if (!(layout instanceof LusciousRecycler)) {
            throw new IllegalArgumentException("The layout manager must be LusciousRecycler");
        }
        super.setLayoutManager(layout);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        int center = getCoverFlowLayout().getCenterPosition()
            - getCoverFlowLayout().getFirstVisiblePosition(); //计算正在显示的所有Item的中间位置
        if (center < 0) center = 0;
        else if (center > childCount) center = childCount;
        int order;
        if (i == center) {
            order = childCount - 1;
        } else if (i > center) {
            order = center + childCount - 1 - i;
        } else {
            order = i;
        }
        return order;
    }

    /**
     * Get LayoutManger, and cast to LusciousRecycler
     */
    public LusciousRecycler getCoverFlowLayout() {
        return ((LusciousRecycler)getLayoutManager());
    }

    /**
     * Get the selected Item location
     */
    public int getSelectedPos() {
        return getCoverFlowLayout().getSelectedPos();
    }

    /**
     * Set monitor selected
     * @param l Listening interface
     */
    public void setOnItemSelectedListener(LusciousRecycler.OnSelected l) {
        getCoverFlowLayout().setOnSelectedListener(l);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                // Set parent class does not intercept sliding events
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if ((ev.getX() > mDownX && getCoverFlowLayout().getCenterPosition() == 0) ||
                    (ev.getX() < mDownX && getCoverFlowLayout().getCenterPosition() ==
                        getCoverFlowLayout().getItemCount() -1)) {
                    // If it is sliding to the front and the end, open the parent sliding event to intercept
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    // Swipe to the middle to set the parent class to not block the swipe event
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
