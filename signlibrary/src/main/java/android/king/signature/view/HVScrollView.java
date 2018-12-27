
package android.king.signature.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.InputDeviceCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;

import android.king.signature.R;

import java.util.List;


/**
 * 作者：LuckyJayce
 * HVScrollView is just like {@link ScrollView}, but it supports acting
 * as both a nested scrolling parent and child on both new and old versions of Android.
 * can scroll horizontal and vertical
 * 能够水平和垂直滚动的 ScrollView，
 * 默认同时可以水平和垂直滚动
 * 当canScrollH 设置为false的时候就是一个垂直的的ScrollView
 * 当canScrollV 设置为false的时候就是一个水平的的ScrollView
 * 代码修改于v4的25.0.0版本的NestedScrollView，参考了RecyclerView的双向滚动的事件，参考了FrameLayoutd的onMeasure和ScrollView的HorizontalScrollView的onMeasure
 * 支持滑动末尾，ViewPager的页面切换
 */
public class HVScrollView extends ViewGroup implements NestedScrollingParent,
        NestedScrollingChild, ScrollingView {
    private boolean DEBUG = false;
    static final int ANIMATED_SCROLL_GAP = 250;

    static final float MAX_SCROLL_FACTOR = 0.5f;

    private static final String TAG = "NestedScrollView";
    private boolean mChildLayoutCenter;
    private int mInitialTouchY;
    private int mInitialTouchX;
//    private boolean mIsBeingDragged;

    public void setChildLayoutCenter(boolean childLayoutCenter) {
        if (mChildLayoutCenter != childLayoutCenter) {
            this.mChildLayoutCenter = childLayoutCenter;
            requestLayout();
        }
    }

    /**
     * Interface definition for a callback to be invoked when the scroll
     * X or Y positions of a view change.
     * <p>
     * This version of the interface works on all versions of Android, back to API v4.
     *
     * @see #setOnScrollChangeListener(OnScrollChangeListener)
     */
    public interface OnScrollChangeListener {
        /**
         * Called when the scroll position of a view changes.
         *
         * @param v          The view whose scroll position has changed.
         * @param scrollX    Current horizontal scroll origin.
         * @param scrollY    Current vertical scroll origin.
         * @param oldScrollX Previous horizontal scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        void onScrollChange(HVScrollView v, int scrollX, int scrollY,
                            int oldScrollX, int oldScrollY);
    }

    private long mLastScroll;

    private final Rect mTempRect = new Rect();
    private ScrollerCompat mScroller;
    private EdgeEffectCompat mEdgeGlowTop;
    private EdgeEffectCompat mEdgeGlowBottom;
    private EdgeEffectCompat mEdgeGlowLeft;
    private EdgeEffectCompat mEdgeGlowRight;

    /**
     * Position of the last motion event.
     */
    private int mLastMotionY;

    private int mLastMotionX;

    /**
     * True when the layout has changed but the traversal has not come through yet.
     * Ideally the view hierarchy would keep track of this for us.
     */
    private boolean mIsLayoutDirty = true;
    private boolean mIsLaidOut = false;

    /**
     * The child to give focus to in the event that a child has requested focus while the
     * layout is dirty. This prevents the scroll from being wrong if the child has not been
     * laid out before requesting focus.
     */
    private View mChildToScrollTo = null;

//    /**
//     * True if the user is currently dragging this ScrollView around. This is
//     * not the same as 'is being flinged', which can be checked by
//     * mScroller.isFinished() (flinging begins when the user lifts his finger).
//     */
//    private boolean mIsBeingDragged = false;

    /**
     * The RecyclerView is not currently scrolling.
     */
    public static final int SCROLL_STATE_IDLE = 0;

    /**
     * The RecyclerView is currently being dragged by outside input such as user touch input.
     */
    public static final int SCROLL_STATE_DRAGGING = 1;

    /**
     * The RecyclerView is currently animating to a final position while not under
     * outside control.
     */
    public static final int SCROLL_STATE_SETTLING = 2;

    // Touch/scrolling handling

    private int mScrollState = SCROLL_STATE_IDLE;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;

    /**
     * Whether arrow scrolling is animated.
     */
    private boolean mSmoothScrollingEnabled = true;

    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;

    /**
     * Used during scrolling to retrieve the new offset within the window.
     */
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedYOffset;
    private int mNestedXOffset;

    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

    private SavedState mSavedState;

    private static final AccessibilityDelegate ACCESSIBILITY_DELEGATE = new AccessibilityDelegate();

    private boolean mFillViewportH;
    private boolean mFillViewportV;
    private final NestedScrollingParentHelper mParentHelper;
    private final NestedScrollingChildHelper mChildHelper;

    private float mVerticalScrollFactor;

    private OnScrollChangeListener mOnScrollChangeListener;

    public HVScrollView(Context context) {
        this(context, null);
    }

    public HVScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public static final int SCROLL_ORIENTATION_NONE = 0;
    public static final int SCROLL_ORIENTATION_HORIZONTAL = 1;
    public static final int SCROLL_ORIENTATION_VERTICAL = 2;
    public static final int SCROLL_ORIENTATION_BOTH = 3;
    private int mScrollOrientation;

    public void setScrollOrientation(int scrollOrientation) {
        this.mScrollOrientation = scrollOrientation;
        requestLayout();
    }

    public int getScrollOrientation() {
        return mScrollOrientation;
    }

    public HVScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initScrollView();

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.HVScrollView, defStyleAttr, 0);

        mChildLayoutCenter = a.getBoolean(R.styleable.HVScrollView_childLayoutCenter, false);
        mFillViewportH = a.getBoolean(R.styleable.HVScrollView_fillViewportH, false);
        mFillViewportV = a.getBoolean(R.styleable.HVScrollView_fillViewportV, false);
        mScrollOrientation = a.getInt(R.styleable.HVScrollView_scrollOrientation, SCROLL_ORIENTATION_BOTH);

        a.recycle();

        mParentHelper = new NestedScrollingParentHelper(this);
        mChildHelper = new NestedScrollingChildHelper(this);

        // ...because why else would you be using this widget?
        setNestedScrollingEnabled(true);

        ViewCompat.setAccessibilityDelegate(this, ACCESSIBILITY_DELEGATE);
    }

    // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    // NestedScrollingParent

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return ((nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && canScrollVertically()) || ((nestedScrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0 && canScrollHorizontally());
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        int axes = ViewCompat.SCROLL_AXIS_NONE;
        axes = canScrollHorizontally() ? axes | ViewCompat.SCROLL_AXIS_HORIZONTAL : axes;
        axes = canScrollVertically() ? axes | ViewCompat.SCROLL_AXIS_VERTICAL : axes;
        startNestedScroll(axes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        mParentHelper.onStopNestedScroll(target);
        stopNestedScroll();
    }

    public boolean canScrollHorizontally() {
        return (mScrollOrientation & SCROLL_ORIENTATION_HORIZONTAL) == SCROLL_ORIENTATION_HORIZONTAL;
    }

    public boolean canScrollVertically() {
        return (mScrollOrientation & SCROLL_ORIENTATION_VERTICAL) == SCROLL_ORIENTATION_VERTICAL;
    }


    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                               int dyUnconsumed) {
        final int oldScrollY = getScrollY();
        final int oldScrollX = getScrollX();
        scrollBy(canScrollHorizontally() ? dxUnconsumed : 0, canScrollVertically() ? dyUnconsumed : 0);
        final int myConsumedY = getScrollY() - oldScrollY;
        final int myConsumedX = getScrollX() - oldScrollX;
        final int myUnconsumedY = dyUnconsumed - myConsumedY;
        final int myUnconsumedX = dxUnconsumed - myConsumedX;
        dispatchNestedScroll(myConsumedX, myConsumedY, myUnconsumedX, myUnconsumedY, null);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        dispatchNestedPreScroll(dx, dy, consumed, null);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (!consumed) {
            fling((int) velocityX, (int) velocityY);
            return true;
        }
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }

    // ScrollView import

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    @Override
    protected float getLeftFadingEdgeStrength() {
        if (getChildCount() == 0 || !canScrollHorizontally()) {
            return 0.0f;
        }

        final int length = getHorizontalFadingEdgeLength();
        final int scrollX = getScrollX();
        if (scrollX < length) {
            return scrollX / (float) length;
        }
        return 1.0f;
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        if (getChildCount() == 0 || !canScrollVertically()) {
            return 0.0f;
        }

        final int length = getVerticalFadingEdgeLength();
        final int scrollY = getScrollY();
        if (scrollY < length) {
            return scrollY / (float) length;
        }
        return 1.0f;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        if (getChildCount() == 0 || !canScrollVertically()) {
            return 0.0f;
        }

        final int length = getVerticalFadingEdgeLength();
        final int bottomEdge = getHeight() - getPaddingBottom();
        final int span = getChildAt(0).getBottom() - getScrollY() - bottomEdge;
        if (span < length) {
            return span / (float) length;
        }

        return 1.0f;
    }

    @Override
    protected float getRightFadingEdgeStrength() {
        if (getChildCount() == 0 || !canScrollHorizontally()) {
            return 0.0f;
        }

        final int length = getHorizontalFadingEdgeLength();
        final int rightEdge = getWidth() - getPaddingRight();
        final int span = getChildAt(0).getRight() - getScrollX() - rightEdge;
        if (span < length) {
            return span / (float) length;
        }

        return 1.0f;
    }

    /**
     * @return The maximum amount this scroll view will scroll in response to
     * an arrow event.
     */
    public int getMaxScrollAmountY() {
        return (int) (MAX_SCROLL_FACTOR * getHeight());
    }

    public int getMaxScrollAmountX() {
        return (int) (MAX_SCROLL_FACTOR * getWidth());
    }

    private void initScrollView() {
        mScroller = ScrollerCompat.create(getContext(), null);
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    public void addView(View child) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }

        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }

        super.addView(child, index);
    }

    @Override
    public void addView(View child, LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }

        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("ScrollView can host only one direct child");
        }

        super.addView(child, index, params);
    }

    /**
     * Register a callback to be invoked when the scroll X or Y positions of
     * this view change.
     * This version of the method works on all versions of Android, back to API v4.
     *
     * @param l The listener to notify when the scroll X or Y position changes.
     * @see View#getScrollX()
     * @see View#getScrollY()
     */
    public void setOnScrollChangeListener(OnScrollChangeListener l) {
        mOnScrollChangeListener = l;
    }

    /**
     * @return Returns true this ScrollView can be scrolled
     */
    private boolean canScroll() {
        View child = getChildAt(0);
        if (child != null) {
            int childHeight = child.getHeight();
            int childWidth = child.getWidth();
            boolean canY = getHeight() < childHeight + getPaddingTop() + getPaddingBottom();
            canY |= canScrollVertically();
            boolean canX = getWidth() < childWidth + getPaddingLeft() + getPaddingRight();
            canX |= canScrollHorizontally();
            return canX || canY;
        }
        return false;
    }

    /**
     * @return Whether arrow scrolling will animate its transition.
     */
    public boolean isSmoothScrollingEnabled() {
        return mSmoothScrollingEnabled;
    }

    /**
     * Set whether arrow scrolling will animate its transition.
     *
     * @param smoothScrollingEnabled whether arrow scrolling will animate its transition
     */
    public void setSmoothScrollingEnabled(boolean smoothScrollingEnabled) {
        mSmoothScrollingEnabled = smoothScrollingEnabled;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (mOnScrollChangeListener != null) {
            mOnScrollChangeListener.onScrollChange(this, l, t, oldl, oldt);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Let the focused view and/or our descendants get the key first
        return super.dispatchKeyEvent(event) || executeKeyEvent(event);
    }

    /**
     * You can call this function yourself to have the scroll view perform
     * scrolling from a key event, just as if the event had been dispatched to
     * it by the view hierarchy.
     *
     * @param event The key event to execute.
     * @return Return true if the event was handled, else false.
     */
    public boolean executeKeyEvent(KeyEvent event) {
        mTempRect.setEmpty();

        if (!canScroll()) {
            if (isFocused() && event.getKeyCode() != KeyEvent.KEYCODE_BACK) {
                View currentFocused = findFocus();
                if (currentFocused == this) currentFocused = null;
                View nextFocused = FocusFinder.getInstance().findNextFocus(this,
                        currentFocused, View.FOCUS_DOWN);
                return nextFocused != null
                        && nextFocused != this
                        && nextFocused.requestFocus(View.FOCUS_DOWN);
            }
            return false;
        }

        boolean handled = false;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (canScrollVertically()) {
                        if (!event.isAltPressed()) {
                            handled = arrowScrollVertically(View.FOCUS_UP);
                        } else {
                            handled = fullScrollVertically(View.FOCUS_UP);
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (canScrollVertically()) {
                        if (!event.isAltPressed()) {
                            handled = arrowScrollVertically(View.FOCUS_DOWN);
                        } else {
                            handled = fullScrollVertically(View.FOCUS_DOWN);
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (canScrollHorizontally()) {
                        if (!event.isAltPressed()) {
                            handled = arrowScrollHorizontally(View.FOCUS_LEFT);
                        } else {
                            handled = fullScrollHorizontally(View.FOCUS_LEFT);
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (canScrollHorizontally()) {
                        if (!event.isAltPressed()) {
                            handled = arrowScrollHorizontally(View.FOCUS_DOWN);
                        } else {
                            handled = fullScrollHorizontally(View.FOCUS_DOWN);
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_SPACE:
                    if (canScrollHorizontally()) {
                        pageScrollHorizontally(event.isShiftPressed() ? View.FOCUS_UP : View.FOCUS_DOWN);
                    } else if (canScrollVertically()) {
                        pageScrollVertically(event.isShiftPressed() ? View.FOCUS_UP : View.FOCUS_DOWN);
                    }
                    break;
            }
        }

        return handled;
    }

    private boolean inChild(int x, int y) {
        if (getChildCount() > 0) {
            final int scrollY = getScrollY();
            final View child = getChildAt(0);
            return !(y < child.getTop() - scrollY
                    || y >= child.getBottom() - scrollY
                    || x < child.getLeft()
                    || x >= child.getRight());
        }
        return false;
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private void setScrollState(int state) {
        if (state == mScrollState) {
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "setting scroll state to " + state + " from " + mScrollState,
                    new Exception());
        }
        mScrollState = state;
        if (state != SCROLL_STATE_SETTLING) {
//            stopScrollersInternal();
        }
//        dispatchOnScrollStateChanged(state);
    }

    private void cancelTouch() {
        resetTouch();
        setScrollState(SCROLL_STATE_IDLE);
    }

    private void resetTouch() {
        recycleVelocityTracker();
        stopNestedScroll();
        releaseGlows();
    }

    private void releaseGlows() {
        boolean needsInvalidate = false;
        if (mEdgeGlowLeft != null) needsInvalidate = mEdgeGlowLeft.onRelease();
        if (mEdgeGlowTop != null) needsInvalidate |= mEdgeGlowTop.onRelease();
        if (mEdgeGlowRight != null) needsInvalidate |= mEdgeGlowRight.onRelease();
        if (mEdgeGlowBottom != null) needsInvalidate |= mEdgeGlowBottom.onRelease();
        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */


        final int action = ev.getAction();
//        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
//            return true;
//        }

        if (action == MotionEvent.ACTION_MOVE && mScrollState == SCROLL_STATE_DRAGGING) {
            return true;
        }

        setScrollState(SCROLL_STATE_IDLE);

        final boolean canScrollHorizontally = canScrollHorizontally();
        final boolean canScrollVertically = canScrollVertically();

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                 * Locally do absolute value. mLastMotionY is set to the y value
                 * of the down event.
                 */
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                if (pointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + activePointerId
                            + " in onInterceptTouchEvent");
                    break;
                }

                final int y = (int) MotionEventCompat.getY(ev, pointerIndex);
                final int x = (int) MotionEventCompat.getX(ev, pointerIndex);
                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    final int dx = x - mInitialTouchX;
                    final int dy = y - mInitialTouchY;
                    boolean startScroll = false;
                    if (canScrollHorizontally && Math.abs(dx) > mTouchSlop) {
                        mLastMotionX = mInitialTouchX + mTouchSlop * (dx < 0 ? -1 : 1);
                        startScroll = true;
                    }
                    if (canScrollVertically && Math.abs(dy) > mTouchSlop) {
                        mLastMotionY = mInitialTouchY + mTouchSlop * (dy < 0 ? -1 : 1);
                        startScroll = true;
                    }
                    initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(ev);
                    mNestedYOffset = 0;
                    if (startScroll) {
//                        mIsBeingDragged = true;
                        setScrollState(SCROLL_STATE_DRAGGING);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final int y = (int) ev.getY();
                final int x = (int) ev.getX();
                if (!inChild(x, y)) {
                    setScrollState(SCROLL_STATE_IDLE);
                    recycleVelocityTracker();
                    break;
                }


                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionY = y;
                mLastMotionX = x;
                mInitialTouchY = mLastMotionY;
                mInitialTouchX = mLastMotionX;

                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't. mScroller.isFinished should be false when
                 * being flinged. We need to call computeScrollOffset() first so that
                 * isFinished() is correct.
                 */
                mScroller.computeScrollOffset();
//                mIsBeingDragged = !mScroller.isFinished();

                if (mScrollState == SCROLL_STATE_SETTLING) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    setScrollState(SCROLL_STATE_DRAGGING);
                }
                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
                if (canScrollHorizontally) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
                }
                if (canScrollVertically) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
                }
                startNestedScroll(nestedScrollAxis);
                break;
            }

            case MotionEvent.ACTION_CANCEL:
//                mIsBeingDragged = false;
                cancelTouch();
                break;
            case MotionEvent.ACTION_UP:
                /* Release the drag */
//                mIsBeingDragged = false;
//                mActivePointerId = INVALID_POINTER;
                recycleVelocityTracker();
//                if (mScroller.springBack(getScrollX(), getScrollY(), 0, getScrollRangeX(), 0, getScrollRangeY())) {
//                    ViewCompat.postInvalidateOnAnimation(this);
//                }
                stopNestedScroll();
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        initVelocityTrackerIfNotExists();

        MotionEvent vtev = MotionEvent.obtain(ev);

        final boolean canScrollHorizontally = canScrollHorizontally();
        final boolean canScrollVertically = canScrollVertically();

        final int actionMasked = MotionEventCompat.getActionMasked(ev);

        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mNestedYOffset = 0;
            mNestedXOffset = 0;
        }
        vtev.offsetLocation(mNestedXOffset, mNestedYOffset);

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {

                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                // Remember where the motion event started
                mInitialTouchX = mLastMotionY = (int) ev.getY();
                mInitialTouchY = mLastMotionX = (int) ev.getX();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
                if (canScrollHorizontally) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
                }
                if (canScrollVertically) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
                }
                startNestedScroll(nestedScrollAxis);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev,
                        mActivePointerId);
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                    return false;
                }

                final int y = (int) MotionEventCompat.getY(ev, activePointerIndex);
                final int x = (int) MotionEventCompat.getX(ev, activePointerIndex);
                int dy = mLastMotionY - y;
                int dx = mLastMotionX - x;
                if (dispatchNestedPreScroll(dx, dy, mScrollConsumed, mScrollOffset)) {
                    dy -= mScrollConsumed[1];
                    dx -= mScrollConsumed[0];
                    vtev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
                    mNestedYOffset += mScrollOffset[1];
                    mNestedXOffset += mScrollOffset[0];
                }

                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    boolean startScroll = false;
                    if (canScrollHorizontally && Math.abs(dx) > mTouchSlop) {
                        if (dx > 0) {
                            dx -= mTouchSlop;
                        } else {
                            dx += mTouchSlop;
                        }
                        startScroll = true;
                    }
                    if (canScrollVertically && Math.abs(dy) > mTouchSlop) {
                        if (dy > 0) {
                            dy -= mTouchSlop;
                        } else {
                            dy += mTouchSlop;
                        }
                        startScroll = true;
                    }
                    if (startScroll) {
                        setScrollState(SCROLL_STATE_DRAGGING);
                    }
                }

                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    // Scroll to follow the motion event
                    mLastMotionY = y - mScrollOffset[1];
                    mLastMotionX = x - mScrollOffset[0];

                    final int oldY = getScrollY();
                    final int oldX = getScrollX();
                    final int rangeY = getScrollRangeY();
                    final int rangeX = getScrollRangeX();
                    final int overscrollMode = ViewCompat.getOverScrollMode(this);
                    boolean canOverscrollY = overscrollMode == ViewCompat.OVER_SCROLL_ALWAYS ||
                            (overscrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS &&
                                    rangeY > 0);
                    boolean canOverscrollX = overscrollMode == ViewCompat.OVER_SCROLL_ALWAYS ||
                            (overscrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS &&
                                    rangeX > 0);
                    boolean canOverscroll = (canOverscrollY && canScrollVertically()) || (canOverscrollX && canScrollHorizontally());

//                    final ViewParent parent = getParent();
//                    if (parent != null) {
//                        parent.requestDisallowInterceptTouchEvent(true);
//                    }

                    // Calling overScrollByCompat will call onOverScrolled, which
                    // calls onScrollChanged if applicable.
                    if (overScrollByCompat(dx, dy, getScrollX(), getScrollY(), rangeX, rangeY, 0, 0, true)) {
                        // Break our velocity if we hit a scroll barrier.
//                        mVelocityTracker.clear();
                        if (mChildHelper.hasNestedScrollingParent()) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }

                    final int scrolledDeltaY = getScrollY() - oldY;
                    final int scrolledDeltaX = getScrollX() - oldX;
                    final int unconsumedY = dy - scrolledDeltaY;
                    final int unconsumedX = dx - scrolledDeltaX;
                    if (dispatchNestedScroll(scrolledDeltaX, scrolledDeltaY, unconsumedX, unconsumedY, mScrollOffset)) {
                        mLastMotionY -= mScrollOffset[1];
                        mLastMotionX -= mScrollOffset[0];
                        vtev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
                        mNestedYOffset += mScrollOffset[1];
                        mNestedXOffset += mScrollOffset[0];
                    } else if (canOverscroll) {
                        ensureGlows();
                        final int pulledToY = oldY + dy;
                        final int pulledToX = oldX + dx;
                        if (canScrollVertically()) {
                            if (pulledToY < 0) {
                                mEdgeGlowTop.onPull((float) dy / getHeight(), MotionEventCompat.getX(ev, activePointerIndex) / getWidth());
                                if (!mEdgeGlowBottom.isFinished()) {
                                    mEdgeGlowBottom.onRelease();
                                }
                            } else if (pulledToY > rangeY) {
                                mEdgeGlowBottom.onPull((float) dy / getHeight(), 1.f - MotionEventCompat.getX(ev, activePointerIndex) / getWidth());
                                if (!mEdgeGlowTop.isFinished()) {
                                    mEdgeGlowTop.onRelease();
                                }
                            }
                        }
                        if (canScrollHorizontally()) {
                            if (pulledToX < 0) {
                                mEdgeGlowLeft.onPull((float) dx / getWidth(), MotionEventCompat.getX(ev, activePointerIndex) / getHeight());
                                if (!mEdgeGlowLeft.isFinished()) {
                                    mEdgeGlowLeft.onRelease();
                                }
                            } else if (pulledToX > rangeX) {
                                mEdgeGlowRight.onPull((float) dx / getWidth(), 1.f - MotionEventCompat.getX(ev, activePointerIndex) / getHeight());
                                if (!mEdgeGlowRight.isFinished()) {
                                    mEdgeGlowRight.onRelease();
                                }
                            }
                        }
                        if (mEdgeGlowTop != null && (!mEdgeGlowTop.isFinished() || !mEdgeGlowBottom.isFinished() || !mEdgeGlowLeft.isFinished() || !mEdgeGlowRight.isFinished())) {
                            ViewCompat.postInvalidateOnAnimation(this);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                final float xvel = canScrollHorizontally ?
                        -VelocityTrackerCompat.getXVelocity(mVelocityTracker, mActivePointerId) : 0;
                final float yvel = canScrollVertically ?
                        -VelocityTrackerCompat.getYVelocity(mVelocityTracker, mActivePointerId) : 0;

                if (fling((int) xvel, (int) yvel)) {
                    setScrollState(SCROLL_STATE_IDLE);
                } else if (mScroller.springBack(getScrollX(), getScrollY(), 0, getScrollRangeX(), 0, getScrollRangeY())) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                mActivePointerId = INVALID_POINTER;
                resetTouch();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    if (mScroller.springBack(getScrollX(), getScrollY(), 0, getScrollRangeX(), 0, getScrollRangeY())) {
                        ViewCompat.postInvalidateOnAnimation(this);
                    }
                }
                mActivePointerId = INVALID_POINTER;
                cancelTouch();
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mInitialTouchX = mLastMotionY = (int) MotionEventCompat.getY(ev, index);
                mInitialTouchX = mLastMotionX = (int) MotionEventCompat.getX(ev, index);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >>
                MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mInitialTouchX = mLastMotionY = (int) MotionEventCompat.getY(ev, newPointerIndex);
            mInitialTouchY = mLastMotionX = (int) MotionEventCompat.getX(ev, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((MotionEventCompat.getSource(event) & InputDeviceCompat.SOURCE_CLASS_POINTER) != 0) {
            switch (event.getAction()) {
                case MotionEventCompat.ACTION_SCROLL: {
                    final float vscroll = MotionEventCompat.getAxisValue(event, MotionEventCompat.AXIS_VSCROLL);
                    final float hscroll = MotionEventCompat.getAxisValue(event, MotionEventCompat.AXIS_HSCROLL);
                    if (vscroll != 0) {
                        int newScrollY = getScrollY(), oldScrollY = newScrollY;
                        int newScrollX = getScrollX(), oldScrollX = newScrollX;
                        if (canScrollHorizontally()) {
                            final int rangeX = getScrollRangeX();
                            final int deltaX = (int) (hscroll * getHorizontalScrollFactorCompat());
                            newScrollX = oldScrollX - deltaX;
                            if (newScrollX < 0) {
                                newScrollX = 0;
                            } else if (newScrollX > rangeX) {
                                newScrollX = rangeX;
                            }
                        }
                        if (canScrollVertically()) {
                            final int deltaY = (int) (vscroll * getVerticalScrollFactorCompat());
                            final int rangeY = getScrollRangeY();
                            newScrollY = oldScrollY - deltaY;
                            if (newScrollY < 0) {
                                newScrollY = 0;
                            } else if (newScrollY > rangeY) {
                                newScrollY = rangeY;
                            }
                        }
                        if (newScrollY != oldScrollY || newScrollX != oldScrollX) {
                            super.scrollTo(newScrollX, newScrollY);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private float getVerticalScrollFactorCompat() {
        if (mVerticalScrollFactor == 0) {
            TypedValue outValue = new TypedValue();
            final Context context = getContext();
            if (!context.getTheme().resolveAttribute(
                    android.R.attr.listPreferredItemHeight, outValue, true)) {
                throw new IllegalStateException(
                        "Expected theme to define listPreferredItemHeight.");
            }
            mVerticalScrollFactor = outValue.getDimension(
                    context.getResources().getDisplayMetrics());
        }
        return mVerticalScrollFactor;
    }

    private float getHorizontalScrollFactorCompat() {
        if (mVerticalScrollFactor == 0) {
            TypedValue outValue = new TypedValue();
            final Context context = getContext();
            if (!context.getTheme().resolveAttribute(
                    android.R.attr.listPreferredItemHeight, outValue, true)) {
                throw new IllegalStateException(
                        "Expected theme to define listPreferredItemHeight.");
            }
            mVerticalScrollFactor = outValue.getDimension(
                    context.getResources().getDisplayMetrics());
        }
        return mVerticalScrollFactor;
    }

    protected void onOverScrolled(int scrollX, int scrollY,
                                  boolean clampedX, boolean clampedY) {
        super.scrollTo(scrollX, scrollY);
    }

    boolean overScrollByCompat(int deltaX, int deltaY,
                               int scrollX, int scrollY,
                               int scrollRangeX, int scrollRangeY,
                               int maxOverScrollX, int maxOverScrollY,
                               boolean isTouchEvent) {
        int oldScrollX = getScrollX();
        int oldScrollY = getScrollY();

        final int overScrollMode = ViewCompat.getOverScrollMode(this);
        final boolean canScrollHorizontal =
                computeHorizontalScrollRange() > computeHorizontalScrollExtent() && canScrollHorizontally();
        final boolean canScrollVertical =
                computeVerticalScrollRange() > computeVerticalScrollExtent() && canScrollVertically();
        final boolean overScrollHorizontal = overScrollMode == ViewCompat.OVER_SCROLL_ALWAYS ||
                (overScrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal);
        final boolean overScrollVertical = overScrollMode == ViewCompat.OVER_SCROLL_ALWAYS ||
                (overScrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical);

        int newScrollX = scrollX;
        if (canScrollHorizontally()) {
            newScrollX += deltaX;
        }

        int newScrollY = scrollY;
        if (canScrollVertically()) {
            newScrollY += deltaY;
        }

        // Clamp values if at the limits and record
        final int left = -maxOverScrollX;
        final int right = maxOverScrollX + scrollRangeX;
        final int top = -maxOverScrollY;
        final int bottom = maxOverScrollY + scrollRangeY;

        boolean clampedX = false;
        if (newScrollX > right) {
            newScrollX = right;
            clampedX = true;
        } else if (newScrollX < left) {
            newScrollX = left;
            clampedX = true;
        }

        boolean clampedY = false;
        if (newScrollY > bottom) {
            newScrollY = bottom;
            clampedY = true;
        } else if (newScrollY < top) {
            newScrollY = top;
            clampedY = true;
        }

        if (clampedY && clampedX) {
            mScroller.springBack(newScrollX, newScrollY, 0, getScrollRangeX(), 0, getScrollRangeY());
        }

        onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);

        return getScrollX() - oldScrollX == deltaX || getScrollY() - oldScrollY == deltaY;
    }

    private int getScrollRangeY() {
        int scrollRange = 0;
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            scrollRange = Math.max(0, child.getHeight() - (getHeight() - getPaddingBottom() - getPaddingTop()));
        }
        return scrollRange;
    }

    private int getScrollRangeX() {
        int scrollRange = 0;
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            scrollRange = Math.max(0, child.getWidth() - (getWidth() - getPaddingLeft() - getPaddingRight()));
        }
        return scrollRange;
    }

    /**
     * Finds the next focusable component that fits in the specified bounds.
     *
     * @param topFocus look for a candidate is the one at the top of the bounds
     *                 if topFocus is true, or at the bottom of the bounds if topFocus is
     *                 false
     * @param top      the top offset of the bounds in which a focusable must be
     *                 found
     * @param bottom   the bottom offset of the bounds in which a focusable must
     *                 be found
     * @return the next focusable component in the bounds or null if none can
     * be found
     */
    private View findFocusableViewInBoundsVertically(boolean topFocus, int top, int bottom) {

        List<View> focusables = getFocusables(View.FOCUS_FORWARD);
        View focusCandidate = null;

        /*
         * A fully contained focusable is one where its top is below the bound's
         * top, and its bottom is above the bound's bottom. A partially
         * contained focusable is one where some part of it is within the
         * bounds, but it also has some part that is not within bounds.  A fully contained
         * focusable is preferred to a partially contained focusable.
         */
        boolean foundFullyContainedFocusable = false;

        int count = focusables.size();
        for (int i = 0; i < count; i++) {
            View view = focusables.get(i);
            int viewTop = view.getTop();
            int viewBottom = view.getBottom();

            if (top < viewBottom && viewTop < bottom) {
                /*
                 * the focusable is in the target area, it is a candidate for
                 * focusing
                 */

                final boolean viewIsFullyContained = (top < viewTop) &&
                        (viewBottom < bottom);

                if (focusCandidate == null) {
                    /* No candidate, take this one */
                    focusCandidate = view;
                    foundFullyContainedFocusable = viewIsFullyContained;
                } else {
                    final boolean viewIsCloserToBoundary =
                            (topFocus && viewTop < focusCandidate.getTop()) ||
                                    (!topFocus && viewBottom > focusCandidate
                                            .getBottom());

                    if (foundFullyContainedFocusable) {
                        if (viewIsFullyContained && viewIsCloserToBoundary) {
                            /*
                             * We're dealing with only fully contained views, so
                             * it has to be closer to the boundary to beat our
                             * candidate
                             */
                            focusCandidate = view;
                        }
                    } else {
                        if (viewIsFullyContained) {
                            /* Any fully contained view beats a partially contained view */
                            focusCandidate = view;
                            foundFullyContainedFocusable = true;
                        } else if (viewIsCloserToBoundary) {
                            /*
                             * Partially contained view beats another partially
                             * contained view if it's closer
                             */
                            focusCandidate = view;
                        }
                    }
                }
            }
        }

        return focusCandidate;
    }

    private View findFocusableViewInBoundsHorizontally(boolean topFocus, int left, int right) {
        List<View> focusables = getFocusables(View.FOCUS_FORWARD);
        View focusCandidate = null;

        /*
         * A fully contained focusable is one where its top is below the bound's
         * top, and its bottom is above the bound's bottom. A partially
         * contained focusable is one where some part of it is within the
         * bounds, but it also has some part that is not within bounds.  A fully contained
         * focusable is preferred to a partially contained focusable.
         */
        boolean foundFullyContainedFocusable = false;

        int count = focusables.size();
        for (int i = 0; i < count; i++) {
            View view = focusables.get(i);
            int viewLeft = view.getLeft();
            int viewRight = view.getRight();

            if (left < viewRight && viewLeft < viewRight) {
                /*
                 * the focusable is in the target area, it is a candidate for
                 * focusing
                 */

                final boolean viewIsFullyContained = (left < viewLeft) &&
                        (viewRight < right);

                if (focusCandidate == null) {
                    /* No candidate, take this one */
                    focusCandidate = view;
                    foundFullyContainedFocusable = viewIsFullyContained;
                } else {
                    final boolean viewIsCloserToBoundary =
                            (topFocus && viewLeft < focusCandidate.getTop()) ||
                                    (!topFocus && viewRight > focusCandidate
                                            .getBottom());

                    if (foundFullyContainedFocusable) {
                        if (viewIsFullyContained && viewIsCloserToBoundary) {
                            /*
                             * We're dealing with only fully contained views, so
                             * it has to be closer to the boundary to beat our
                             * candidate
                             */
                            focusCandidate = view;
                        }
                    } else {
                        if (viewIsFullyContained) {
                            /* Any fully contained view beats a partially contained view */
                            focusCandidate = view;
                            foundFullyContainedFocusable = true;
                        } else if (viewIsCloserToBoundary) {
                            /*
                             * Partially contained view beats another partially
                             * contained view if it's closer
                             */
                            focusCandidate = view;
                        }
                    }
                }
            }
        }

        return focusCandidate;
    }

    /**
     * Handles scrolling in response to a "page up/down" shortcut press. This
     * method will scroll the view by one page up or down and give the focus
     * to the topmost/bottommost component in the new visible area. If no
     * component is a good candidate for focus, this scrollview reclaims the
     * focus.
     *
     * @param direction the scroll direction: {@link View#FOCUS_UP}
     *                  to go one page up or
     *                  {@link View#FOCUS_DOWN} to go one page down
     * @return true if the key event is consumed by this method, false otherwise
     */
    public boolean pageScrollHorizontally(int direction) {
        boolean down = direction == View.FOCUS_DOWN;
        int width = getWidth();

        if (down) {
            mTempRect.left = getScrollX() + width;
            int count = getChildCount();
            if (count > 0) {
                View view = getChildAt(count - 1);
                if (mTempRect.left + width > view.getRight()) {
                    mTempRect.left = view.getRight() - width;
                }
            }
        } else {
            mTempRect.left = getScrollY() - width;
            if (mTempRect.left < 0) {
                mTempRect.left = 0;
            }
        }
        mTempRect.right = mTempRect.left + width;
        return scrollAndFocusHorizontally(direction, mTempRect.left, mTempRect.right);
    }

    public boolean pageScrollVertically(int direction) {
        boolean down = direction == View.FOCUS_DOWN;
        int height = getHeight();

        if (down) {
            mTempRect.top = getScrollY() + height;
            int count = getChildCount();
            if (count > 0) {
                View view = getChildAt(count - 1);
                if (mTempRect.top + height > view.getBottom()) {
                    mTempRect.top = view.getBottom() - height;
                }
            }
        } else {
            mTempRect.top = getScrollY() - height;
            if (mTempRect.top < 0) {
                mTempRect.top = 0;
            }
        }
        mTempRect.bottom = mTempRect.top + height;
        return scrollAndFocusVertically(direction, mTempRect.top, mTempRect.bottom);
    }

    /**
     * Handles scrolling in response to a "home/end" shortcut press. This
     * method will scroll the view to the top or bottom and give the focus
     * to the topmost/bottommost component in the new visible area. If no
     * component is a good candidate for focus, this scrollview reclaims the
     * focus.
     *
     * @param direction the scroll direction: {@link View#FOCUS_UP}
     *                  to go the top of the view or
     *                  {@link View#FOCUS_DOWN} to go the bottom
     * @return true if the key event is consumed by this method, false otherwise
     */
    public boolean fullScrollVertically(int direction) {
        boolean down = direction == View.FOCUS_DOWN;
        int height = getHeight();

        mTempRect.top = 0;
        mTempRect.bottom = height;

        if (down) {
            int count = getChildCount();
            if (count > 0) {
                View view = getChildAt(count - 1);
                mTempRect.bottom = view.getBottom() + getPaddingBottom();
                mTempRect.top = mTempRect.bottom - height;
            }
        }

        return scrollAndFocusVertically(direction, mTempRect.top, mTempRect.bottom);
    }

    /**
     * Handles scrolling in response to a "home/end" shortcut press. This
     * method will scroll the view to the top or bottom and give the focus
     * to the topmost/bottommost component in the new visible area. If no
     * component is a good candidate for focus, this scrollview reclaims the
     * focus.
     *
     * @param direction the scroll direction: {@link View#FOCUS_UP}
     *                  to go the top of the view or
     *                  {@link View#FOCUS_DOWN} to go the bottom
     * @return true if the key event is consumed by this method, false otherwise
     */
    public boolean fullScrollHorizontally(int direction) {
        boolean down = direction == View.FOCUS_RIGHT;
        int width = getWidth();

        mTempRect.left = 0;
        mTempRect.right = width;

        if (down) {
            int count = getChildCount();
            if (count > 0) {
                View view = getChildAt(count - 1);
                mTempRect.right = view.getRight() + getPaddingLeft();
                mTempRect.left = mTempRect.right - width;
            }
        }

        return scrollAndFocusHorizontally(direction, mTempRect.left, mTempRect.right);
    }


    private boolean scrollAndFocusHorizontally(int direction, int left, int right) {
        boolean handled = true;

        int width = getWidth();
        int containerLeft = getScrollX();
        int containerRight = containerLeft + width;
        boolean up = direction == View.FOCUS_LEFT;

        View newFocused = findFocusableViewInBoundsHorizontally(up, left, right);
        if (newFocused == null) {
            newFocused = this;
        }

        if (left >= containerLeft && right <= containerRight) {
            handled = false;
        } else {
            int delta = up ? (left - containerLeft) : (right - containerRight);
            doScrollX(delta);
        }

        if (newFocused != findFocus()) newFocused.requestFocus(direction);

        return handled;
    }

    /**
     * Scrolls the view to make the area defined by <code>top</code> and
     * <code>bottom</code> visible. This method attempts to give the focus
     * to a component visible in this area. If no component can be focused in
     * the new visible area, the focus is reclaimed by this ScrollView.
     *
     * @param direction the scroll direction: {@link View#FOCUS_UP}
     *                  to go upward, {@link View#FOCUS_DOWN} to downward
     * @param top       the top offset of the new area to be made visible
     * @param bottom    the bottom offset of the new area to be made visible
     * @return true if the key event is consumed by this method, false otherwise
     */
    private boolean scrollAndFocusVertically(int direction, int top, int bottom) {
        boolean handled = true;

        int height = getHeight();
        int containerTop = getScrollY();
        int containerBottom = containerTop + height;
        boolean up = direction == View.FOCUS_UP;

        View newFocused = findFocusableViewInBoundsVertically(up, top, bottom);
        if (newFocused == null) {
            newFocused = this;
        }

        if (top >= containerTop && bottom <= containerBottom) {
            handled = false;
        } else {
            int delta = up ? (top - containerTop) : (bottom - containerBottom);
            doScrollY(delta);
        }

        if (newFocused != findFocus()) newFocused.requestFocus(direction);

        return handled;
    }

    /**
     * Handle scrolling in response to an up or down arrow click.
     *
     * @param direction The direction corresponding to the arrow key that was
     *                  pressed
     * @return True if we consumed the event, false otherwise
     */
    public boolean arrowScrollHorizontally(int direction) {

        View currentFocused = findFocus();
        if (currentFocused == this) currentFocused = null;

        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);

        final int maxJump = getMaxScrollAmountX();

        if (nextFocused != null && isWithinDeltaOfScreenX(nextFocused, maxJump, getWidth())) {
            nextFocused.getDrawingRect(mTempRect);
            offsetDescendantRectToMyCoords(nextFocused, mTempRect);
            int[] scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
            doScrollX(scrollDelta[0]);
            nextFocused.requestFocus(direction);
        } else {
            // no new focus
            int scrollDelta = maxJump;

            if (direction == View.FOCUS_UP && getScrollX() < scrollDelta) {
                scrollDelta = getScrollX();
            } else if (direction == View.FOCUS_DOWN) {
                if (getChildCount() > 0) {
                    int daRight = getChildAt(0).getRight();
                    int screenRight = getScrollX() + getWidth() - getPaddingRight();
                    if (daRight - screenRight < maxJump) {
                        scrollDelta = daRight - screenRight;
                    }
                }
            }
            if (scrollDelta == 0) {
                return false;
            }
            doScrollX(direction == View.FOCUS_DOWN ? scrollDelta : -scrollDelta);
        }

        if (currentFocused != null && currentFocused.isFocused() && isOffScreenX(currentFocused)) {
            // previously focused item still has focus and is off screen, give
            // it up (take it back to ourselves)
            // (also, need to temporarily force FOCUS_BEFORE_DESCENDANTS so we are
            // sure to
            // get it)
            final int descendantFocusability = getDescendantFocusability();  // save
            setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            requestFocus();
            setDescendantFocusability(descendantFocusability);  // restore
        }
        return true;
    }

    /**
     * Handle scrolling in response to an up or down arrow click.
     *
     * @param direction The direction corresponding to the arrow key that was
     *                  pressed
     * @return True if we consumed the event, false otherwise
     */
    public boolean arrowScrollVertically(int direction) {

        View currentFocused = findFocus();
        if (currentFocused == this) currentFocused = null;

        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);

        final int maxJump = getMaxScrollAmountY();

        if (nextFocused != null && isWithinDeltaOfScreenY(nextFocused, maxJump, getHeight())) {
            nextFocused.getDrawingRect(mTempRect);
            offsetDescendantRectToMyCoords(nextFocused, mTempRect);
            int[] scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
            doScrollY(scrollDelta[1]);
            nextFocused.requestFocus(direction);
        } else {
            // no new focus
            int scrollDelta = maxJump;

            if (direction == View.FOCUS_UP && getScrollY() < scrollDelta) {
                scrollDelta = getScrollY();
            } else if (direction == View.FOCUS_DOWN) {
                if (getChildCount() > 0) {
                    int daBottom = getChildAt(0).getBottom();
                    int screenBottom = getScrollY() + getHeight() - getPaddingBottom();
                    if (daBottom - screenBottom < maxJump) {
                        scrollDelta = daBottom - screenBottom;
                    }
                }
            }
            if (scrollDelta == 0) {
                return false;
            }
            doScrollY(direction == View.FOCUS_DOWN ? scrollDelta : -scrollDelta);
        }

        if (currentFocused != null && currentFocused.isFocused()
                && isOffScreenY(currentFocused)) {
            // previously focused item still has focus and is off screen, give
            // it up (take it back to ourselves)
            // (also, need to temporarily force FOCUS_BEFORE_DESCENDANTS so we are
            // sure to
            // get it)
            final int descendantFocusability = getDescendantFocusability();  // save
            setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            requestFocus();
            setDescendantFocusability(descendantFocusability);  // restore
        }
        return true;
    }

    /**
     * @return whether the descendant of this scroll view is scrolled off
     * screen.
     */
    private boolean isOffScreenX(View descendant) {
        return !isWithinDeltaOfScreenX(descendant, 0, getWidth());
    }


    /**
     * @return whether the descendant of this scroll view is scrolled off
     * screen.
     */
    private boolean isOffScreenY(View descendant) {
        return !isWithinDeltaOfScreenY(descendant, 0, getHeight());
    }

    /**
     * @return whether the descendant of this scroll view is within delta
     * pixels of being on the screen.
     */
    private boolean isWithinDeltaOfScreenX(View descendant, int delta, int width) {
        descendant.getDrawingRect(mTempRect);
        offsetDescendantRectToMyCoords(descendant, mTempRect);

        return (mTempRect.right + delta) >= getScrollX()
                && (mTempRect.left - delta) <= (getScrollX() + width);
    }

    /**
     * @return whether the descendant of this scroll view is within delta
     * pixels of being on the screen.
     */
    private boolean isWithinDeltaOfScreenY(View descendant, int delta, int height) {
        descendant.getDrawingRect(mTempRect);
        offsetDescendantRectToMyCoords(descendant, mTempRect);

        return (mTempRect.bottom + delta) >= getScrollY()
                && (mTempRect.top - delta) <= (getScrollY() + height);
    }

    private void doScrollXY(int[] delta) {
        doScrollXY(delta[0], delta[1]);
    }

    private void doScrollXY(int deltaX, int deltaY) {
        if (deltaX != 0 || deltaY != 0) {
            if (mSmoothScrollingEnabled) {
                smoothScrollBy(deltaX, deltaY);
            } else {
                scrollBy(deltaX, deltaY);
            }
        }
    }

    /**
     * Smooth scroll by a Y delta
     *
     * @param delta the number of pixels to scroll by on the Y axis
     */
    private void doScrollY(int delta) {
        if (delta != 0) {
            if (mSmoothScrollingEnabled) {
                smoothScrollBy(0, delta);
            } else {
                scrollBy(0, delta);
            }
        }
    }

    private void doScrollX(int delta) {
        if (delta != 0) {
            if (mSmoothScrollingEnabled) {
                smoothScrollBy(delta, 0);
            } else {
                scrollBy(delta, 0);
            }
        }
    }

    /**
     * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
     *
     * @param dx the number of pixels to scroll by on the X axis
     * @param dy the number of pixels to scroll by on the Y axis
     */
    public final void smoothScrollBy(int dx, int dy) {
        if (getChildCount() == 0) {
            // Nothing to do.
            return;
        }
        long duration = AnimationUtils.currentAnimationTimeMillis() - mLastScroll;
        if (duration > ANIMATED_SCROLL_GAP) {
            setScrollState(SCROLL_STATE_SETTLING);

            final int height = getHeight() - getPaddingBottom() - getPaddingTop();
            final int bottom = getChildAt(0).getHeight();
            final int width = getWidth() - getPaddingRight() - getPaddingLeft();
            final int right = getChildAt(0).getWidth();
            final int maxY = Math.max(0, bottom - height);
            final int maxX = Math.max(0, right - width);
            final int scrollY = getScrollY();
            final int scrollX = getScrollX();
            dy = Math.max(0, Math.min(scrollY + dy, maxY)) - scrollY;
            dx = Math.max(0, Math.min(scrollX + dx, maxX)) - scrollX;

            mScroller.startScroll(scrollX, scrollY, dx, dy);
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            setScrollState(SCROLL_STATE_IDLE);
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            scrollBy(dx, dy);
        }
        mLastScroll = AnimationUtils.currentAnimationTimeMillis();
    }

    /**
     * Like {@link #scrollTo}, but scroll smoothly instead of immediately.
     *
     * @param x the position where to scroll on the X axis
     * @param y the position where to scroll on the Y axis
     */
    public final void smoothScrollTo(int x, int y) {
        smoothScrollBy(x - getScrollX(), y - getScrollY());
    }


    /**
     * The scroll range of a scroll view is the overall height of all of its
     * children.
     */
    @Override
    public int computeVerticalScrollRange() {
        if (!canScrollVertically()) {
            return super.computeVerticalScrollRange();
        }
        final int count = getChildCount();
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        if (count == 0) {
            return contentHeight;
        }

        int scrollRange = getChildAt(0).getBottom();
        final int scrollY = getScrollY();
        final int overscrollBottom = Math.max(0, scrollRange - contentHeight);
        if (scrollY < 0) {
            scrollRange -= scrollY;
        } else if (scrollY > overscrollBottom) {
            scrollRange += scrollY - overscrollBottom;
        }

        return scrollRange;
    }

    /**
     *
     */
    @Override
    public int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    /**
     *
     */
    @Override
    public int computeVerticalScrollExtent() {
        return super.computeVerticalScrollExtent();
    }

    /**
     *
     */
    @Override
    public int computeHorizontalScrollRange() {
        if (!canScrollHorizontally()) {
            return super.computeHorizontalScrollRange();
        }
        final int count = getChildCount();
        final int contentWidth = getWidth() - getPaddingRight() - getPaddingLeft();
        if (count == 0) {
            return contentWidth;
        }

        int scrollRange = getChildAt(0).getRight();
        final int scrollX = getScrollX();
        final int overscrollRight = Math.max(0, scrollRange - contentWidth);
        if (scrollX < 0) {
            scrollRange -= scrollX;
        } else if (scrollX > overscrollRight) {
            scrollRange += scrollX - overscrollRight;
        }
        return scrollRange;
    }

    /**
     *
     */
    @Override
    public int computeHorizontalScrollOffset() {
        return Math.max(0, super.computeHorizontalScrollOffset());
    }

    /**
     *
     */
    @Override
    public int computeHorizontalScrollExtent() {
        return super.computeHorizontalScrollExtent();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() == 0) {
            return;
        }
        View child = getChildAt(0);
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        int childLeft = getPaddingLeft() + lp.leftMargin;
        int childTop = getPaddingTop() + lp.topMargin;
        if (mChildLayoutCenter) {
            if (getMeasuredWidth() > child.getMeasuredWidth()) {
                childLeft = (getMeasuredWidth() - child.getMeasuredWidth()) / 2;
            }
            if (getMeasuredHeight() > child.getMeasuredHeight()) {
                childTop = (getMeasuredHeight() - child.getMeasuredHeight()) / 2;
            }
        }
        int measureHeight = child.getMeasuredHeight();
        int measuredWidth = child.getMeasuredWidth();
        child.layout(childLeft, childTop, measuredWidth + childLeft, childTop + measureHeight);


        mIsLayoutDirty = false;
        // Give a child focus if it needs it
        if (mChildToScrollTo != null && isViewDescendantOf(mChildToScrollTo, this)) {
            scrollToChild(mChildToScrollTo);
        }
        mChildToScrollTo = null;

        if (!mIsLaidOut) {
            if (mSavedState != null) {
                scrollTo(mSavedState.scrollXPosition, mSavedState.scrollYPosition);
                mSavedState = null;
            } // mScrollY default value is "0"

            final int childHeight = (getChildCount() > 0) ? getChildAt(0).getMeasuredHeight() : 0;
            final int scrollRangeY = Math.max(0,
                    childHeight - (b - t - getPaddingBottom() - getPaddingTop()));

            final int childWidth = (getChildCount() > 0) ? getChildAt(0).getMeasuredWidth() : 0;
            final int scrollRangeX = Math.max(0,
                    childWidth - (b - t - getPaddingRight() - getPaddingLeft()));

            int sY = getScrollY();
            int sX = getScrollX();

            // Don't forget to clamp
            if (getScrollY() > scrollRangeY) {
                sY = scrollRangeY;
            } else if (getScrollY() < 0) {
                sY = 0;
            }
            if (getScrollX() > scrollRangeX) {
                sX = scrollRangeX;
            } else if (getScrollX() < 0) {
                sX = 0;
            }
            if (sX != getScrollX() || sY != getScrollY()) {
                scrollTo(sX, sY);
            }
        }

        // Calling this with the present values causes it to re-claim them
        scrollTo(getScrollX(), getScrollY());
        mIsLaidOut = true;
    }

    @Override
    public MarginLayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    @Override
    protected MarginLayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() == 0) {
            return;
        }
        View child = getChildAt(0);
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        int widthPadding = lp.leftMargin + lp.rightMargin + getPaddingLeft() + getPaddingRight();
        int heightPadding = lp.topMargin + lp.bottomMargin + getPaddingTop() + getPaddingBottom();
        int maxWidth = widthPadding;
        int maxHeight = heightPadding;
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        int childState = 0;
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        if (canScrollVertically()) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.UNSPECIFIED);
        } else {
            childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingBottom() + getPaddingTop() + lp.topMargin + lp.bottomMargin, lp.height);
        }
        if (canScrollHorizontally()) {
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.UNSPECIFIED);
        } else {
            childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
        }
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }
        maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
        maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);

        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        int measuredWidth = ViewCompat.resolveSizeAndState(maxWidth, widthMeasureSpec, childState);
        int measuredHeight = ViewCompat.resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT);
        setMeasuredDimension(measuredWidth, measuredHeight);


        boolean needMeasure = false;
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.UNSPECIFIED && mFillViewportV) {
            if (child.getMeasuredHeight() < measuredHeight - heightPadding) {
                int newChildHeightMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight - heightPadding, MeasureSpec.EXACTLY);
                if (newChildHeightMeasureSpec != childHeightMeasureSpec) {
                    childHeightMeasureSpec = newChildHeightMeasureSpec;
                    needMeasure = true;
                }
            }
        }
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.UNSPECIFIED && mFillViewportH) {
            if (child.getMeasuredWidth() < measuredWidth - widthPadding) {
                int newChildWidthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth - widthPadding, MeasureSpec.EXACTLY);
                if (newChildWidthMeasureSpec != childWidthMeasureSpec) {
                    childWidthMeasureSpec = newChildWidthMeasureSpec;
                    needMeasure = true;
                }
            }
        }
        if (needMeasure) {
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (oldX != x || oldY != y) {
                final int rangeY = getScrollRangeY();
                final int rangeX = getScrollRangeX();
                final int overscrollMode = ViewCompat.getOverScrollMode(this);
                final boolean canOverscrollY = overscrollMode == ViewCompat.OVER_SCROLL_ALWAYS ||
                        (overscrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS && rangeY > 0);
                final boolean canOverscrollX = overscrollMode == ViewCompat.OVER_SCROLL_ALWAYS ||
                        (overscrollMode == ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS && rangeX > 0);
                boolean canOverscroll = canOverscrollY || canOverscrollX;

                overScrollByCompat(x - oldX, y - oldY, oldX, oldY, rangeX, rangeY,
                        0, 0, false);

                if (canOverscroll) {
                    ensureGlows();
                    if (y <= 0 && oldY > 0) {
                        mEdgeGlowTop.onAbsorb((int) mScroller.getCurrVelocity());
                    } else if (y >= rangeY && oldY < rangeY) {
                        mEdgeGlowBottom.onAbsorb((int) mScroller.getCurrVelocity());
                    }

                    if (x <= 0 && oldX > 0) {
                        mEdgeGlowLeft.onAbsorb((int) mScroller.getCurrVelocity());
                    } else if (x >= rangeX && oldX < rangeX) {
                        mEdgeGlowRight.onAbsorb((int) mScroller.getCurrVelocity());
                    }
                }
            }

//            final boolean fullyConsumedVertical = canScrollVertically()
//                    && x == getScrollX();
//            final boolean fullyConsumedHorizontal = canScrollHorizontally()
//                    && x == getScrollY();
//            final boolean fullyConsumedAny = fullyConsumedHorizontal
//                    || fullyConsumedVertical;

            if (mScroller.isFinished()) {
                setScrollState(SCROLL_STATE_IDLE); // setting state to idle will stop this.
            } else {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }

    /**
     * Scrolls the view to the given child.
     *
     * @param child the View to scroll to
     */
    private void scrollToChild(View child) {
        child.getDrawingRect(mTempRect);

        /* Offset from child's local coordinates to ScrollView coordinates */
        offsetDescendantRectToMyCoords(child, mTempRect);

        int scrollDelta[] = computeScrollDeltaToGetChildRectOnScreen(mTempRect);

        if (scrollDelta[0] != 0 || scrollDelta[1] != 0) {
            scrollBy(scrollDelta[0], scrollDelta[1]);
        }
    }

    /**
     * If rect is off screen, scroll just enough to get it (or at least the
     * first screen size chunk of it) on screen.
     *
     * @param rect      The rectangle.
     * @param immediate True to scroll immediately without animation
     * @return true if scrolling was performed
     */
    private boolean scrollToChildRect(Rect rect, boolean immediate) {
        final int delta[] = computeScrollDeltaToGetChildRectOnScreen(rect);
        final boolean scroll = delta[0] != 0 || delta[1] != 0;
        if (scroll) {
            if (delta[0] < 0) {
                delta[0] = 5 * delta[0];
            }
            if (immediate) {
                scrollBy(delta[0], delta[1]);
            } else {

                smoothScrollBy(delta[0], delta[1]);
            }
        }
        return scroll;
    }

    /**
     * Compute the amount to scroll in the Y direction in order to get
     * a rectangle completely on the screen (or, if taller than the screen,
     * at least the first screen size chunk of it).
     *
     * @param rect The rect.
     * @return The scroll delta.
     */
    protected int[] computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        if (getChildCount() == 0) return new int[]{0, 0};

        int height = getHeight();
        int width = getWidth();
        int screenTop = getScrollY();
        int screenBottom = screenTop + height;
        int screenLeft = getScrollX();
        int screenRight = screenLeft + width;

        int fadingEdgeY = getVerticalFadingEdgeLength();
        int fadingEdgeX = getHorizontalFadingEdgeLength();

        // leave room for top fading edge as long as rect isn't at very top
        if (rect.top > 0) {
            screenTop += fadingEdgeY;
        }
        if (rect.left > 0) {
            screenLeft += fadingEdgeX;
        }

        // leave room for bottom fading edge as long as rect isn't at very bottom
        if (rect.bottom < getChildAt(0).getHeight()) {
            screenBottom -= fadingEdgeY;
        }
        if (rect.right < getChildAt(0).getWidth()) {
            screenRight -= fadingEdgeX;
        }

        int scrollYDelta = 0;
        int scrollXDelta = 0;

        if (rect.bottom > screenBottom && rect.top > screenTop) {
            // need to move down to get it in view: move down just enough so
            // that the entire rectangle is in view (or at least the first
            // screen size chunk).

            if (rect.height() > height) {
                // just enough to get screen size chunk on
                scrollYDelta += (rect.top - screenTop);
            } else {
                // get entire rect at bottom of screen
                scrollYDelta += (rect.bottom - screenBottom);
            }

            // make sure we aren't scrolling beyond the end of our content
            int bottom = getChildAt(0).getBottom();
            int distanceToBottom = bottom - screenBottom;
            scrollYDelta = Math.min(scrollYDelta, distanceToBottom);

        } else if (rect.top < screenTop && rect.bottom < screenBottom) {
            // need to move up to get it in view: move up just enough so that
            // entire rectangle is in view (or at least the first screen
            // size chunk of it).

            if (rect.height() > height) {
                // screen size chunk
                scrollYDelta -= (screenBottom - rect.bottom);
            } else {
                // entire rect at top
                scrollYDelta -= (screenTop - rect.top);
            }

            // make sure we aren't scrolling any further than the top our content
            scrollYDelta = Math.max(scrollYDelta, -getScrollY());
        }
        if (rect.right > screenRight && rect.left > screenLeft) {
            // need to move down to get it in view: move down just enough so
            // that the entire rectangle is in view (or at least the first
            // screen size chunk).

            if (rect.width() > width) {
                // just enough to get screen size chunk on
                scrollXDelta += (rect.left - screenLeft);
            } else {
                // get entire rect at bottom of screen
                scrollXDelta += (rect.right - screenRight);
            }

            // make sure we aren't scrolling beyond the end of our content
            int right = getChildAt(0).getRight();
            int distanceToRight = right - screenRight;
            scrollXDelta = Math.min(scrollXDelta, distanceToRight);

        } else if (rect.left < screenLeft && rect.right < screenRight) {
            // need to move up to get it in view: move up just enough so that
            // entire rectangle is in view (or at least the first screen
            // size chunk of it).

            if (rect.width() > width) {
                // screen size chunk
                scrollXDelta -= (screenRight - rect.right);
            } else {
                // entire rect at top
                scrollXDelta -= (screenLeft - rect.left);
            }

            // make sure we aren't scrolling any further than the top our content
            scrollXDelta = Math.max(scrollXDelta, -getScrollX());
        }
        return new int[]{scrollXDelta, scrollYDelta};
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        if (!mIsLayoutDirty) {
            scrollToChild(focused);
        } else {
            // The child may not be laid out yet, we can't compute the scroll yet
            mChildToScrollTo = focused;
        }
        super.requestChildFocus(child, focused);
    }


    /**
     * When looking for focus in children of a scroll view, need to be a little
     * more careful not to give focus to something that is scrolled off screen.
     * <p>
     * This is more expensive than the default {@link ViewGroup}
     * implementation, otherwise this behavior might have been made the default.
     */
    @Override
    protected boolean onRequestFocusInDescendants(int direction,
                                                  Rect previouslyFocusedRect) {

        // convert from forward / backward notation to up / down / left / right
        // (ugh).
        if (direction == View.FOCUS_FORWARD) {
            direction = View.FOCUS_DOWN;
        } else if (direction == View.FOCUS_BACKWARD) {
            direction = View.FOCUS_UP;
        }

        final View nextFocus = previouslyFocusedRect == null ?
                FocusFinder.getInstance().findNextFocus(this, null, direction) :
                FocusFinder.getInstance().findNextFocusFromRect(this,
                        previouslyFocusedRect, direction);

        if (nextFocus == null) {
            return false;
        }

        if (canScrollHorizontally()) {
            if (isOffScreenX(nextFocus)) {
                return false;
            }
        } else if (canScrollVertically()) {
            if (isOffScreenY(nextFocus)) {
                return false;
            }
        }

        return nextFocus.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
                                                 boolean immediate) {
        // offset into coordinate space of this scroll view
        rectangle.offset(child.getLeft() - child.getScrollX(),
                child.getTop() - child.getScrollY());
        return scrollToChildRect(rectangle, immediate);
    }

    @Override
    public void requestLayout() {
        mIsLayoutDirty = true;
        super.requestLayout();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        mIsLaidOut = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        View currentFocused = findFocus();
        if (null == currentFocused || this == currentFocused)
            return;

        // If the currently-focused view was visible on the screen when the
        // screen was at the old height, then scroll the screen to make that
        // view visible with the new screen height.
        if (isWithinDeltaOfScreenX(currentFocused, 0, oldw) || isWithinDeltaOfScreenY(currentFocused, 0, oldh)) {
            currentFocused.getDrawingRect(mTempRect);
            offsetDescendantRectToMyCoords(currentFocused, mTempRect);
            int scrollDelta[] = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
            doScrollXY(scrollDelta);
        }
    }

    /**
     * Return true if child is a descendant of parent, (or equal to the parent).
     */
    private static boolean isViewDescendantOf(View child, View parent) {
        if (child == parent) {
            return true;
        }

        final ViewParent theParent = child.getParent();
        return (theParent instanceof ViewGroup) && isViewDescendantOf((View) theParent, parent);
    }

    private boolean fling(int velocityX, int velocityY) {
        final boolean canScrollHorizontal = canScrollHorizontally();
        final boolean canScrollVertical = canScrollVertically();

        if (!canScrollHorizontal || Math.abs(velocityX) < mMinimumVelocity) {
            velocityX = 0;
        }
        if (!canScrollVertical || Math.abs(velocityY) < mMinimumVelocity) {
            velocityY = 0;
        }
        if (velocityX == 0 && velocityY == 0) {
            // If we don't have any velocity, return false
            return false;
        }

        if (!dispatchNestedPreFling(velocityX, velocityY)) {
            final int scrollY = getScrollY();
            final int scrollX = getScrollX();
            final boolean canFlingY = (scrollY > 0 || velocityY > 0) &&
                    (scrollY < getScrollRangeY() || velocityY < 0);
            final boolean canFlingX = (scrollX > 0 || velocityX > 0) &&
                    (scrollX < getScrollRangeX() || velocityX < 0);
            boolean canFling = canFlingY || canFlingX;
            dispatchNestedFling(velocityX, velocityY, canFling);
            if (canFling) {
                setScrollState(SCROLL_STATE_SETTLING);

                velocityX = Math.max(-mMaximumVelocity, Math.min(velocityX, mMaximumVelocity));
                velocityY = Math.max(-mMaximumVelocity, Math.min(velocityY, mMaximumVelocity));

                int height = getHeight() - getPaddingBottom() - getPaddingTop();
                int width = getWidth() - getPaddingRight() - getPaddingLeft();
                int bottom = getChildAt(0).getHeight();
                int right = getChildAt(0).getWidth();
                mScroller.fling(getScrollX(), getScrollY(), velocityX, velocityY, 0, Math.max(0, right - width), 0,
                        Math.max(0, bottom - height), width / 2, height / 2);

//                ViewCompat.postInvalidateOnAnimation(this);
                return true;
            }
        }
        return false;
    }

//    private void endDrag() {
//        mIsBeingDragged = false;
//
//        recycleVelocityTracker();
//        stopNestedScroll();
//
//        if (mEdgeGlowTop != null) {
//            mEdgeGlowTop.onRelease();
//            mEdgeGlowBottom.onRelease();
//            mEdgeGlowLeft.onRelease();
//            mEdgeGlowRight.onRelease();
//        }
//    }

    /**
     * {@inheritDoc}
     * <p>
     * This version also clamps the scrolling to the bounds of our child.
     */
    @Override
    public void scrollTo(int x, int y) {
        // we rely on the fact the View.scrollBy calls scrollTo.
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            x = clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), child.getWidth());
            y = clamp(y, getHeight() - getPaddingBottom() - getPaddingTop(), child.getHeight());
            if (x != getScrollX() || y != getScrollY()) {
                super.scrollTo(x, y);
            }
        }
    }

    private void ensureGlows() {
        if (ViewCompat.getOverScrollMode(this) != ViewCompat.OVER_SCROLL_NEVER) {
            if (mEdgeGlowTop == null) {
                Context context = getContext();
                mEdgeGlowTop = new EdgeEffectCompat(context);
                mEdgeGlowBottom = new EdgeEffectCompat(context);
                mEdgeGlowLeft = new EdgeEffectCompat(context);
                mEdgeGlowRight = new EdgeEffectCompat(context);
            }
        } else {
            mEdgeGlowTop = null;
            mEdgeGlowBottom = null;
            mEdgeGlowLeft = null;
            mEdgeGlowRight = null;
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mEdgeGlowTop != null) {
            final int scrollX = getScrollX();
            final int scrollY = getScrollY();
            if (!mEdgeGlowTop.isFinished()) {
                final int restoreCount = canvas.save();
                final int width = getWidth() - getPaddingLeft() - getPaddingRight();

                canvas.translate(getPaddingLeft() + scrollX, Math.min(0, scrollY));
                mEdgeGlowTop.setSize(width, getHeight());
                if (mEdgeGlowTop.draw(canvas)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                canvas.restoreToCount(restoreCount);
            }
            if (!mEdgeGlowBottom.isFinished()) {
                final int restoreCount = canvas.save();
                final int width = getWidth() - getPaddingLeft() - getPaddingRight();
                final int height = getHeight();

                canvas.translate(-width + getPaddingLeft() + scrollX,
                        Math.max(getScrollRangeY(), scrollY) + height);
                canvas.rotate(180, width, 0);
                mEdgeGlowBottom.setSize(width, height);
                if (mEdgeGlowBottom.draw(canvas)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                canvas.restoreToCount(restoreCount);
            }
            if (!mEdgeGlowLeft.isFinished()) {

                final int restoreCount = canvas.save();
                final int width = getWidth();
                final int height = getHeight() - getPaddingTop() - getPaddingBottom();

                canvas.rotate(270);

                canvas.translate(-height + getPaddingTop() - scrollY, Math.min(0, scrollX));
//                canvas.rotate(90, 0, 0);
                mEdgeGlowLeft.setSize(height, width);
                if (mEdgeGlowLeft.draw(canvas)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                canvas.restoreToCount(restoreCount);
            }
            if (!mEdgeGlowRight.isFinished()) {
                final int restoreCount = canvas.save();
                final int width = getWidth();
                final int height = getHeight() - getPaddingTop() - getPaddingBottom();

                canvas.rotate(90);
                canvas.translate(-getPaddingTop() + scrollY,
                        -(Math.max(getScrollRangeX(), scrollX) + width));
                mEdgeGlowRight.setSize(height, width);
                if (mEdgeGlowRight.draw(canvas)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                canvas.restoreToCount(restoreCount);
            }
        }
    }

    private static int clamp(int n, int my, int child) {
        if (my >= child || n < 0) {
            /* my >= child is this case:
             *                    |--------------- me ---------------|
             *     |------ child ------|
             * or
             *     |--------------- me ---------------|
             *            |------ child ------|
             * or
             *     |--------------- me ---------------|
             *                                  |------ child ------|
             *
             * n < 0 is this case:
             *     |------ me ------|
             *                    |-------- child --------|
             *     |-- mScrollX --|
             */
            return 0;
        }
        if ((my + n) > child) {
            /* this case:
             *                    |------ me ------|
             *     |------ child ------|
             *     |-- mScrollX --|
             */
            return child - my;
        }
        return n;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mSavedState = ss;
        requestLayout();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.scrollYPosition = getScrollY();
        ss.scrollXPosition = getScrollX();
        return ss;
    }

    static class SavedState extends BaseSavedState {
        public int scrollYPosition;
        public int scrollXPosition;

        SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            scrollYPosition = source.readInt();
            scrollXPosition = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(scrollYPosition);
            dest.writeInt(scrollXPosition);
        }

        @Override
        public String toString() {
            return "HorizontalScrollView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " scrollXPosition=" + scrollXPosition + " scrollYPosition=" + scrollYPosition + "}";
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    static class AccessibilityDelegate extends AccessibilityDelegateCompat {
        @Override
        public boolean performAccessibilityAction(View host, int action, Bundle arguments) {
            if (super.performAccessibilityAction(host, action, arguments)) {
                return true;
            }
            final HVScrollView nsvHost = (HVScrollView) host;
            if (!nsvHost.isEnabled()) {
                return false;
            }
            switch (action) {
                case AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD: {
                    final int viewportHeight = nsvHost.getHeight() - nsvHost.getPaddingBottom()
                            - nsvHost.getPaddingTop();
                    final int viewportWidth = nsvHost.getWidth() - nsvHost.getPaddingRight()
                            - nsvHost.getPaddingLeft();
                    final int targetScrollY = Math.min(nsvHost.getScrollY() + viewportHeight,
                            nsvHost.getScrollRangeY());
                    final int targetScrollX = Math.min(nsvHost.getScrollX() + viewportWidth,
                            nsvHost.getScrollRangeX());
                    if (targetScrollY != nsvHost.getScrollY() || targetScrollX != nsvHost.getScrollX()) {
                        nsvHost.smoothScrollTo(targetScrollX, targetScrollY);
                        return true;
                    }
                }
                return false;
                case AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD: {
                    final int viewportHeight = nsvHost.getHeight() - nsvHost.getPaddingBottom()
                            - nsvHost.getPaddingTop();
                    final int viewportWidth = nsvHost.getWidth() - nsvHost.getPaddingRight()
                            - nsvHost.getPaddingLeft();
                    final int targetScrollY = Math.max(nsvHost.getScrollY() - viewportHeight, 0);
                    final int targetScrollX = Math.min(nsvHost.getScrollX() - viewportWidth, 0);
                    if (targetScrollY != nsvHost.getScrollY() || targetScrollX != nsvHost.getScrollX()) {
                        nsvHost.smoothScrollTo(targetScrollX, targetScrollY);
                        return true;
                    }
                }
                return false;
            }
            return false;
        }

        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            final HVScrollView nsvHost = (HVScrollView) host;
            info.setClassName(ScrollView.class.getName());
            if (nsvHost.isEnabled()) {
                final int scrollRangeY = nsvHost.getScrollRangeY();
                final int scrollRangeX = nsvHost.getScrollRangeX();
                if (scrollRangeY > 0 || scrollRangeX > 0) {
                    info.setScrollable(true);
                    if (nsvHost.getScrollY() > 0 || nsvHost.getScrollX() > 0) {
                        info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD);
                    }
                    if (nsvHost.getScrollY() < scrollRangeY || nsvHost.getScrollX() < scrollRangeX) {
                        info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD);
                    }
                }
            }
        }

        @Override
        public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(host, event);
            final HVScrollView nsvHost = (HVScrollView) host;
            event.setClassName(ScrollView.class.getName());
            final AccessibilityRecordCompat record = AccessibilityEventCompat.asRecord(event);
            final boolean scrollableX = nsvHost.getScrollRangeY() > 0;
            final boolean scrollableY = nsvHost.getScrollRangeX() > 0;
            boolean scrollable = scrollableX || scrollableY;
            record.setScrollable(scrollable);
            record.setScrollX(nsvHost.getScrollX());
            record.setScrollY(nsvHost.getScrollY());
            record.setMaxScrollX(nsvHost.getScrollRangeX());
            record.setMaxScrollY(nsvHost.getScrollRangeY());
        }
    }


    public void setFillViewportH(boolean fillViewportH) {
        if (mFillViewportH != fillViewportH) {
            this.mFillViewportH = fillViewportH;
            requestLayout();
        }
    }

    public void setFillViewportV(boolean fillViewportV) {
        if (mFillViewportV != fillViewportV) {
            this.mFillViewportV = fillViewportV;
            requestLayout();
        }
    }

    public void setFillViewportHV(boolean fillViewportH, boolean fillViewportV) {
        if (mFillViewportH != fillViewportH || mFillViewportV != fillViewportV) {
            this.mFillViewportH = fillViewportH;
            this.mFillViewportV = fillViewportV;
            requestLayout();
        }
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (direction > 0) {
            return getScrollX() < getScrollRangeX();
        } else {
            return getScrollX() > 0 && getScrollRangeX() > 0;
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (direction > 0) {
            return getScrollY() < getScrollRangeY();
        } else {
            return getScrollY() > 0 && getScrollRangeY() > 0;
        }
    }


}
