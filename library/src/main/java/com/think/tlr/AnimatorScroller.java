package com.think.tlr;

import android.content.Context;
import android.widget.OverScroller;

/**
 * Created by borney on 5/4/17.
 */
public class AnimatorScroller extends OverScroller {
    private int mOffsetX, mOffsetY;
    private int mLastX, mLastY;

    public AnimatorScroller(Context context) {
        super(context);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, duration);
        mLastX = startX;
        mLastY = startY;
    }

    @Override
    public boolean computeScrollOffset() {
        boolean computeScrollOffset = super.computeScrollOffset();
        if (computeScrollOffset) {
            int curX = getCurrX();
            int currY = getCurrY();
            mOffsetX = curX - mLastX;
            mOffsetY = currY - mLastY;
            mLastX = curX;
            mLastY = currY;
        }
        return computeScrollOffset;
    }

    public int getOffsetX() {
        return mOffsetX;
    }

    public int getOffsetY() {
        return mOffsetY;
    }
}
