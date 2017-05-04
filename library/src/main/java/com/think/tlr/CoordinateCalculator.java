package com.think.tlr;

import android.content.Context;
import android.view.ViewConfiguration;

/**
 * Created by borney on 5/3/17.
 */
class CoordinateCalculator {
    private float mDownX, mDownY;
    private float mLastX, mLastY;
    private float mOffsetX, mOffsetY;
    private int mTotalOffsetY, mTotalOffsetX;
    private AnimatorScroller mAnimScroller;
    private float mRefreshThreshold;
    private float mLoadThreshold;
    private TLRLinearLayout mTLRLinearLayout;
    private float mResistance;
    private int mTouchSlop;
    private Direction mDirection = Direction.NONE;

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        NONE
    }

    CoordinateCalculator(TLRLinearLayout layout, float refreshThreshold, float loadThreshold, float resistance) {
        mTLRLinearLayout = layout;
        mRefreshThreshold = refreshThreshold;
        mLoadThreshold = loadThreshold;
        mResistance = resistance;
        Context context = layout.getContext();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mAnimScroller = new AnimatorScroller(context);
    }

    /**
     * call by layout touch down
     */
    void eventDown(float x, float y) {
        if (!mAnimScroller.isFinished()) {
            mAnimScroller.abortAnimation();
            moveLayoutView(-mTotalOffsetY);
        }
        reset();
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

    void scrollerMoveLayoutView() {
        moveLayoutView(-mAnimScroller.getOffsetY());
    }

    /**
     * call view {@link android.view.View#offsetTopAndBottom(int)} method must cast offset to int
     *
     * @return
     */
    private void moveLayoutView(int y) {
        mTotalOffsetY += y;
        mTLRLinearLayout.move(y);
    }

    boolean computeScrollOffset() {
        return mAnimScroller.computeScrollOffset();
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
        mOffsetY = offsetY / mResistance;
        mOffsetX = offsetX / mResistance;
    }

    void startResetAnimator() {
        mAnimScroller.startScroll(0, 0, 0, mTotalOffsetY, 600);
        mTLRLinearLayout.invalidate();
    }

    Direction getDirection() {
        return mDirection;
    }
}
