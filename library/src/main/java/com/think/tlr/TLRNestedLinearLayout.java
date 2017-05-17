package com.think.tlr;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author borney
 * @date 4/28/17
 * @see TLRLinearLayout
 */
public class TLRNestedLinearLayout extends TLRLinearLayout implements NestedScrollingParent {
    private TLRCalculator mCalculator;
    private NestedScrollingParentHelper mParentHelper;

    public TLRNestedLinearLayout(Context context) {
        this(context, null);
    }

    public TLRNestedLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TLRNestedLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCalculator = calculator();
        mParentHelper = new NestedScrollingParentHelper(this);
        ViewCompat.setNestedScrollingEnabled(this, true);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if (!isEnableLoad() && !isEnableRefresh()) {
            return false;
        }
        if (mCalculator.hasAnyAnimatorRunning()) {
            return false;
        }
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
            int dyUnconsumed) {
        setTouchView(target);
        mCalculator.touchMoveLayoutView((int) (-dyUnconsumed / getResistance()));
    }

    @Override
    public void onStopNestedScroll(View child) {
        mParentHelper.onStopNestedScroll(child);
        mCalculator.eventUp(0, 0);
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            return dispatchTouchEvent(false, ev);
        }
        if (action == MotionEvent.ACTION_MOVE && !mCalculator.isBackStatus()) {
            return dispatchTouchEvent(false, ev);
        }
        return dispatchTouchEvent(true, ev);
    }
}
