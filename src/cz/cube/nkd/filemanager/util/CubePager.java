package cz.cube.nkd.filemanager.util;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;
import cz.cube.nkd.filemanager.component.ItemListView;

public class CubePager extends ViewGroup {

    private static enum TouchMove {
        NONE,
        VERTICAL,
        HORIZONTAL,
        SNAP;

    }

    private boolean mStartOnMeasure = true;

    private TouchMove mTouchMove = TouchMove.NONE;
    private final int mTouchSlop;

    private ItemListView[] mItemListViews;

    private float mLastMotionX = 0;
    private float mLastMotionY = 0;
    private float mRotationStep;

    private int mPrev;
    private int mCurrent;
    private int mNext;

    private final Scroller mScroller;
    private final int mScrollerDurationInMillis = 500;
    private VelocityTracker mVelocityTracker;

    public CubePager(Context context) {
        super(context);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mScroller = new Scroller(getContext(), new DecelerateInterpolator());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) throw new IllegalStateException("ViewSwitcher can only be used in EXACTLY mode.");
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        if (mStartOnMeasure) {
            scrollTo(0, 0);
            mStartOnMeasure = false;
        }
    }

    public final void setItemListViews(ItemListView[] itemlistViews, int startIndex) {
        this.mItemListViews = itemlistViews;
        this.mCurrent = startIndex;
        addView(mItemListViews[mCurrent]);
        mItemListViews[mCurrent].setVerticalScrollBarEnabled(true);
        scrollTo(0, 0);
    }

    public final int getCurrentIndex() {
        return mCurrent;
    }

    public final ItemListView getCurrent() {
        return mItemListViews[mCurrent];
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    mLastMotionX = ev.getX();
                    mLastMotionY = ev.getY();
                    createVelocityTracker();
                    mVelocityTracker.addMovement(ev);
                    computeScroll();
                    setTouchMove(TouchMove.NONE);
                } else {
                    mLastMotionX = ev.getX();
                    mLastMotionY = ev.getY();
                    createVelocityTracker();
                    mVelocityTracker.addMovement(ev);
                    if (mTouchMove == TouchMove.SNAP) return true;
                    setTouchMove(TouchMove.NONE);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchMove == TouchMove.SNAP) return true;
                if (mTouchMove == TouchMove.HORIZONTAL) return true;
                if (mTouchMove == TouchMove.VERTICAL) return false;
                final float deltaX = Math.abs(ev.getX() - mLastMotionX);
                final float deltaY = Math.abs(ev.getY() - mLastMotionY);
                if (deltaY > mTouchSlop * 2) {
                    setTouchMove(TouchMove.VERTICAL);
                    destroyVelocityTracker();
                } else if (deltaX > mTouchSlop) {
                    prepareTouch();
                    mLastMotionX = ev.getX();
                    mVelocityTracker.addMovement(ev);
                    setTouchMove(TouchMove.HORIZONTAL);
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        mVelocityTracker.addMovement(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (mTouchMove == TouchMove.HORIZONTAL) {
                    int deltaX = (int) (mLastMotionX - ev.getX());
                    mLastMotionX = ev.getX();
                    scrollBy(deltaX, 0);
                    rotateViews();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchMove == TouchMove.HORIZONTAL) {
                    setTouchMove(TouchMove.SNAP);
                    float velocityX = 0;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.computeCurrentVelocity(1000);
                        velocityX = mVelocityTracker.getXVelocity();
                        destroyVelocityTracker();
                    }
                    Util.logI("VelocityX = " + velocityX);
                    final int x = getScrollX();
                    if (velocityX > 500 || x < getWidth() - getWidth() / 3) { // prev
                        Util.logI("Scroll prev");
                        mScroller.startScroll(x, 0, -x, 0, mScrollerDurationInMillis);
                    } else if (velocityX < -500 || x > getWidth() + getWidth() / 3) { //next
                        Util.logI("Scroll next");
                        mScroller.startScroll(x, 0, (getWidth() * 2 - x), 0, mScrollerDurationInMillis);
                    } else { //current
                        Util.logI("Scroll current");
                        mScroller.startScroll(x, 0, getWidth() - x, 0, mScrollerDurationInMillis);
                    }
                    invalidate();
                }
                break;
        }
        return true;
    }

    private void prepareTouch() {
        removeAllViews();
        final int count = mItemListViews.length;
        mPrev = (mCurrent - 1 + count) % count;
        mNext = (mCurrent + 1) % count;

        mRotationStep = getWidth() / 90.0f;

        mItemListViews[mPrev].setVerticalScrollBarEnabled(false);
        mItemListViews[mPrev].setVisibility(VISIBLE);
        mItemListViews[mCurrent].setVerticalScrollBarEnabled(false);
        mItemListViews[mCurrent].setVisibility(VISIBLE);
        mItemListViews[mNext].setVerticalScrollBarEnabled(false);
        mItemListViews[mNext].setVisibility(VISIBLE);

        addView(mItemListViews[mPrev]);
        addView(mItemListViews[mCurrent]);
        addView(mItemListViews[mNext]);

        mItemListViews[mPrev].setPivotX(getWidth());
        mItemListViews[mPrev].setPivotY(getHeight() / 2);

        mItemListViews[mCurrent].setPivotY(getHeight() / 2);

        mItemListViews[mNext].setPivotX(0);
        mItemListViews[mNext].setPivotY(getHeight() / 2);

        scrollTo(getWidth(), 0);
    }

    @Override
    public void computeScroll() {
        if (mTouchMove == TouchMove.SNAP) {
            if (mScroller.computeScrollOffset()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                rotateViews();
                postInvalidate();
            } else {
                setTouchMove(TouchMove.NONE);
                final int count = mItemListViews.length;
                int finalScrollX = getWidth();
                if (getScrollX() < getWidth() / 2) { // prev
                    mCurrent = (mCurrent - 1 + count) % count;
                    finalScrollX = 0;
                } else if (getScrollX() > getWidth() + getWidth() / 2) { //next
                    mCurrent = (mCurrent + 1) % count;
                    finalScrollX = getWidth() * 2;
                }
                mItemListViews[(mCurrent + 1) % count].setVisibility(INVISIBLE);
                mItemListViews[(mCurrent - 1 + count) % count].setVisibility(INVISIBLE);
                mItemListViews[mCurrent].setRotationY(0);
                mItemListViews[mCurrent].setVerticalScrollBarEnabled(true);
                scrollTo(finalScrollX, 0);
                //invalidate();
            }
        }
    }

    private void rotateViews() {
        final int x = getScrollX();
        if (x < getWidth()) {
            if (mItemListViews[mPrev].getVisibility() == INVISIBLE) mItemListViews[mPrev].setVisibility(VISIBLE);
            if (mItemListViews[mNext].getVisibility() == VISIBLE) mItemListViews[mNext].setVisibility(INVISIBLE);

            mItemListViews[mPrev].setRotationY(-x / mRotationStep);
            if (mItemListViews[mCurrent].getPivotX() != 0) mItemListViews[mCurrent].setPivotX(0);
            mItemListViews[mCurrent].setRotationY(90 - x / mRotationStep);
        } else if (x > getWidth()) {
            if (mItemListViews[mPrev].getVisibility() == VISIBLE) mItemListViews[mPrev].setVisibility(INVISIBLE);
            if (mItemListViews[mNext].getVisibility() == INVISIBLE) mItemListViews[mNext].setVisibility(VISIBLE);

            if (mItemListViews[mCurrent].getPivotX() != getWidth()) mItemListViews[mCurrent].setPivotX(getWidth());
            mItemListViews[mCurrent].setRotationY(90 - x / mRotationStep);
            mItemListViews[mNext].setRotationY((-x - getWidth()) / mRotationStep - 90);
        }
    }

    private void setTouchMove(TouchMove touchMove) {
        mTouchMove = touchMove;
        Util.logI("mTouchMove = " + mTouchMove);
    }

    private void createVelocityTracker() {
        destroyVelocityTracker();
        mVelocityTracker = VelocityTracker.obtain();
    }

    private void destroyVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

}
