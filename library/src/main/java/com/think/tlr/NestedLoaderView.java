package com.think.tlr;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by borney on 4/18/17.
 */

public class NestedLoaderView extends ViewGroup implements NestedScrollingParent {
    private static final String TAG = "LoaderView";
    private static final int FLAG_LABEL_HEAD = 0x01;
    private static final int FLAG_LABEL_CONTENT = 0x02;
    private static final int FLAG_LABEL_FOOT = 0x04;

    private int mViewLabelFlags = 0x00;
    private View mHeaderView;
    private View mContentView;
    private View mFooterView;
    private Scroller mScroller;
    private NestedScrollingParentHelper mParentHelper;
    private OnScrollChangeListener mOnScrollChangeListener;
    private Operate mOperate = Operate.NONE;

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
        void onScrollChange(NestedLoaderView v, int scrollX, int scrollY,
                            int oldScrollX, int oldScrollY);
    }

    public NestedLoaderView(Context context) {
        this(context, null);
    }

    public NestedLoaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedLoaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
        mParentHelper = new NestedScrollingParentHelper(this);
        setWillNotDraw(false);
        ViewCompat.setNestedScrollingEnabled(this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        if (count > 3) {
            throw new RuntimeException("more child!!!");
        }
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            Log.d("child = " + child.getClass().getSimpleName() + " params.label = " + params.label);
            switch (params.label) {
                case FLAG_LABEL_HEAD:
                    if ((mViewLabelFlags & FLAG_LABEL_HEAD) == FLAG_LABEL_HEAD) {
                        throw new RuntimeException("more header view!!!");
                    }
                    mViewLabelFlags ^= FLAG_LABEL_HEAD;
                    mHeaderView = child;
                    break;
                case FLAG_LABEL_CONTENT:
                    if ((mViewLabelFlags & FLAG_LABEL_CONTENT) == FLAG_LABEL_CONTENT) {
                        throw new RuntimeException("more content view!!!");
                    }
                    mViewLabelFlags ^= FLAG_LABEL_CONTENT;
                    mContentView = child;
                    break;
                case FLAG_LABEL_FOOT:
                    if ((mViewLabelFlags & FLAG_LABEL_FOOT) == FLAG_LABEL_FOOT) {
                        throw new RuntimeException("more content view!!!");
                    }
                    mViewLabelFlags ^= FLAG_LABEL_FOOT;
                    mFooterView = child;
                    break;
                default:
                    throw new RuntimeException("must mark child is header/content!!!");
            }
        }
        if (mHeaderView == null) {
            Log.d(TAG, "has not header view!");
        }
        if (mContentView == null) {
            Log.d(TAG, "has not content view!");
        }
        if (mFooterView == null) {
            Log.d(TAG, "has not footer view!");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mHeaderView != null) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
        if (mContentView != null) {
            final LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
            final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin,
                    lp.width);
            final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin, lp.height);
            mContentView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
        if (mFooterView != null) {
            measureChildWithMargins(mFooterView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        if (mHeaderView != null) {
            LayoutParams lp = (LayoutParams) mHeaderView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = -(mHeaderView.getMeasuredHeight() - paddingTop - lp.topMargin);
            final int right = left + mHeaderView.getMeasuredWidth();
            final int bottom = top + mHeaderView.getMeasuredHeight();
            mHeaderView.layout(left, top, right, bottom);
        }
        if (mContentView != null) {
            LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin;
            final int right = left + mContentView.getMeasuredWidth();
            final int bottom = top + mContentView.getMeasuredHeight();
            mContentView.layout(left, top, right, bottom);
        }
        if (mFooterView != null) {
            LayoutParams lp = (LayoutParams) mFooterView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int bottom = getMeasuredHeight() + mFooterView.getMeasuredHeight() - paddingBottom - lp.bottomMargin;
            final int right = left + mFooterView.getMeasuredWidth();
            final int top = bottom - mFooterView.getMeasuredHeight();
            mFooterView.layout(left, top, right, bottom);
        }
    }

    public void setOnScrollChangeListener(OnScrollChangeListener l) {
        mOnScrollChangeListener = l;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.d(TAG, "onStartNestedScroll child = " + child.getClass().getSimpleName() + " target = " + target.getClass().getSimpleName() + " nestedScrollAxes = " + nestedScrollAxes);
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mParentHelper.onNestedScrollAccepted(child, target, axes);
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int oldScrollY = getScrollY();
        scrollBy(0, (int) (dyUnconsumed / 1.8));
        invalidate();
        int scrollY = getScrollY();

        if (isRefresh(target)) {
            if (oldScrollY == 0) {
                setOperate(Operate.REFREASH);
                setState(Operate.State.INITIAL);
            } else if ((Math.abs(scrollY) - mHeaderView.getMeasuredHeight()) <= Math.abs(scrollY - oldScrollY)) {
                setState(Operate.State.START);
            } else {
                setState(Operate.State.PROCESSING);
            }
        }

        if (isLoad(target)) {
            if (oldScrollY == 0) {
                setOperate(Operate.LOAD);
                setState(Operate.State.INITIAL);
            } else if ((Math.abs(scrollY) - mFooterView.getMeasuredHeight()) <= Math.abs(scrollY - oldScrollY)) {
                setState(Operate.State.START);
            } else {
                setState(Operate.State.PROCESSING);
            }
        }
    }

    @Override
    public void onStopNestedScroll(View child) {
        mParentHelper.onStopNestedScroll(child);
        mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), 800);
        invalidate();
    }

    @Override
    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle args) {
        return super.onNestedPrePerformAccessibilityAction(target, action, args);
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (mOnScrollChangeListener != null) {
            mOnScrollChangeListener.onScrollChange(this, l, t, oldl, oldt);
        }
        if (t == 0) {
            if (mOperate.state() == Operate.State.PROCESSING) {
                setState(Operate.State.COMPLETE);
            }
            resetOperate();
        }
    }

    private void resetOperate() {
        setOperate(Operate.NONE);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }

    private boolean isRefresh(View target) {
        return ViewCompat.canScrollVertically(target, 1) && !ViewCompat.canScrollVertically(target, -1);
    }

    private boolean isLoad(View target) {
        return !ViewCompat.canScrollVertically(target, 1) && ViewCompat.canScrollVertically(target, -1);
    }

    private void setOperate(Operate o) {
        if (mOperate == o) {
            return;
        }
        if (o == Operate.NONE) {
            setState(Operate.State.NONE);
        }
        mOperate = o;
        Log.d("set Operate --> " + mOperate);
    }

    private void setState(Operate.State s) {
        if (mOperate == Operate.NONE) {
            throw new RuntimeException("update state on NONE Operate!!!!");
        }
        mOperate.setState(s);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(getContext());
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return generateDefaultLayoutParams();
    }

    public class LayoutParams extends MarginLayoutParams {
        private int label = 0;

        public LayoutParams(Context c) {
            this(c, null);

        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray array = c.obtainStyledAttributes(attrs, R.styleable.TLRLinearLayout_Layout);
            try {
                final int N = array.getIndexCount();
                for (int i = 0; i < N; i++) {
                    int attr = array.getIndex(i);
                    if (attr == R.styleable.TLRLinearLayout_Layout_label) {
                        label = array.getInt(attr, 0);
                        if (label == 0) {
                            throw new RuntimeException("must set label!!!");
                        }
                    }
                }
            } finally {
                array.recycle();
            }
        }
    }
}
