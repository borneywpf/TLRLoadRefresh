package com.think.tlr;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by borney on 5/3/17.
 */
class CoordinateCalculator {
    private float mDownX, mDownY;
    private float mLastX, mLastY;
    private float mOffsetX, mOffsetY;
    private int mTotalOffsetY, mTotalOffsetX;
    private float mRefreshThreshold = 1.0f;
    private float mLoadThreshold = 1.0f;
    private float mResistance = 1.6f;
    private int mHeadHeight;
    private int mFootHeight;
    private ValueAnimator mValueAnimator;
    private TLRLinearLayout mTLRLinearLayout;
    private int mTouchSlop;
    private boolean isRefresh;
    private boolean isLoad;
    private Direction mDirection = Direction.NONE;

    public CoordinateCalculator(TLRLinearLayout layout, AttributeSet attrs) {
        mTLRLinearLayout = layout;
        Context context = layout.getContext();
        initAttrs(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TLRLinearLayout);
        if (array == null) {
            Log.e("initAttrs array is null");
            return;
        }
        try {
            final int N = array.getIndexCount();
            for (int i = 0; i < N; i++) {
                int index = array.getIndex(i);
                if (index == R.styleable.TLRLinearLayout_refreshThreshold) {
                    mRefreshThreshold = array.getFloat(index, mRefreshThreshold);
                } else if (index == R.styleable.TLRLinearLayout_loadThreshold) {
                    mLoadThreshold = array.getFloat(index, mLoadThreshold);
                } else if (index == R.styleable.TLRLinearLayout_resistance) {
                    mResistance = array.getFloat(index, mResistance);
                }
            }
        } finally {
            array.recycle();
        }
    }

    public void setTLR(boolean refresh, boolean load) {
        isRefresh = refresh;
        isLoad = load;
    }

    void setHeadViewHeight(int height) {
        mHeadHeight = height;
    }

    void setFootViewHeight(int height) {
        mFootHeight = height;
    }

    /**
     * call by layout touch down
     */
    void eventDown(float x, float y) {
        if (mValueAnimator != null && mValueAnimator.isStarted()) {
            mValueAnimator.end();
        }
        mLastX = mDownX = x;
        mLastY = mDownY = y;
    }

    /**
     * call by layout touch move
     */
    void eventMove(float x, float y) {
        float xDiff = x - mLastX;
        float yDiff = y - mLastY;
        setDirection(xDiff, yDiff);
        setOffset(xDiff, yDiff);
        mLastX = x;
        mLastY = y;
    }

    /**
     * call by layout touch up/cancel
     */
    void eventUp(float x, float y) {
        mDirection = Direction.NONE;
        startResetAnimator();
    }

    /**
     * eventMove distance must more than {@link ViewConfiguration#getScaledTouchSlop()}
     *
     * @return
     */
    boolean canCalculatorV() {
        if (mDirection == Direction.DOWN || mDirection == Direction.UP) {
            return Math.abs(mLastY - mDownY) > mTouchSlop;
        }
        return false;
    }

    void touchMoveLayoutView() {
        moveLayoutView((int) mOffsetY);
    }

    /**
     * call view {@link android.view.View#offsetTopAndBottom(int)} method must cast offset to int
     *
     * @return
     */
    private void moveLayoutView(int y) {
        mTotalOffsetY += y;
        Log.d("total:" + mTotalOffsetY + " y:" + y + " d:" + mDirection);
        mTLRLinearLayout.move(y);
    }

    private void reset() {
        mLastX = mDownX = 0;
        mLastY = mDownY = 0;
        mTotalOffsetY = 0;
        mDirection = Direction.NONE;
        setOffset(0, 0);
    }

    private void setDirection(float xDiff, float yDiff) {
        if (Math.abs(xDiff) > Math.abs(yDiff)) {
            if (xDiff > 0) {
                mDirection = Direction.RIGHT;
            } else {
                mDirection = Direction.LEFT;
            }
        } else {
            if (yDiff > 0) {
                mDirection = Direction.DOWN;
            } else {
                mDirection = Direction.UP;
            }
        }
    }

    private void setOffset(float offsetX, float offsetY) {
        mOffsetX = offsetX / mResistance;
        mOffsetY = offsetY / mResistance;
    }

    void startAutoRefresh() {
        Log.d("startAutoRefresh mHeadHeight:" + mHeadHeight);
        if (mHeadHeight == 0) {
            mTLRLinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Log.d("startAutoRefresh onGlobalLayout mHeadHeight:" + mHeadHeight);
                    startAutoRefreshAnimator();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mTLRLinearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mTLRLinearLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        } else {
            startAutoRefreshAnimator();
        }
    }

    private void startAutoRefreshAnimator() {
        startYAnimator(-mHeadHeight, 0, new AccelerateDecelerateInterpolator(), 800);
    }

    private void startResetAnimator() {
        startYAnimator(mTotalOffsetY, 0, new DecelerateInterpolator(), 500);
    }

    private void startYAnimator(final int startY, int endY, TimeInterpolator interpolator, long duration) {
        mValueAnimator = ValueAnimator.ofInt(startY, endY);
        mValueAnimator.setDuration(duration);
        mValueAnimator.setInterpolator(interpolator);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int lastY = startY;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                moveLayoutView(value - lastY);
                lastY = value;
            }
        });
        mValueAnimator.start();
    }

    Direction touchDirection() {
        return mDirection;
    }

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        NONE
    }
}
