package com.think.tlr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.think.tlr.TLRLinearLayout.LoadStatus;
import com.think.tlr.TLRLinearLayout.RefreshStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author borney
 * @date 4/28/17
 */
class TLRCalculator {
    private static final boolean DEBUG = false;
    private float mDownX, mDownY;
    private float mLastX, mLastY;
    /**
     * Touch View 移动阀值
     */
    private float mOffsetX, mOffsetY;
    /**
     * View 偏移坐标
     */
    private int mTotalOffsetY, mTotalOffsetX;

    /**
     * 刷新阀值系数，加载阀值系数
     */
    private float mRefreshThreshold = 1.0f, mLoadThreshold = 1.0f;
    /**
     * 阻尼系数
     */
    private float mResistance = 1.6f;
    /**
     * 关闭和打开动画时间
     */
    private int mCloseAnimDuration = 500, mOpenAnimDuration = 800;

    private int mHeadHeight;
    private int mFootHeight;

    /**
     * 刷新或加载移动的最大距离
     */
    private int mRefreshMaxMoveDistance = -1, mLoadMaxMoveDistance = -1;

    /**
     * 是否保存刷新view/加载view
     */
    private boolean isKeepHeadRefreshing = true, isKeepFootLoading = true;

    /**
     * UI是否回归到初始状态
     */
    private boolean isBackStatus = true;

    private TLRStatusController mStatusController;

    private TLRUIHandler mTLRUiHandler;

    private ValueAnimator mAutoAnimator, mResetAnimator, mKeepAnimator;

    private final List<TLRUIHandlerHook> mHooks = new ArrayList<>();
    /**
     * 刷新阀值(高度), 加载阀值
     */
    int refreshThresholdHeight, loadThresholdHeight;

    TLRLinearLayout tLRLinearLayout;
    private int mTouchSlop;
    private Direction mDirection = Direction.NONE;

    public TLRCalculator(TLRLinearLayout layout, AttributeSet attrs) {
        tLRLinearLayout = layout;
        Context context = layout.getContext();
        initAttrs(context, attrs);
        mStatusController = new TLRStatusController(this, context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TLRLinearLayout);
        if (array == null) {
            TLRLog.e("initAttrs array is null");
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
                } else if (index == R.styleable.TLRLinearLayout_closeAnimDuration) {
                    mCloseAnimDuration = array.getInt(index, mCloseAnimDuration);
                } else if (index == R.styleable.TLRLinearLayout_openAnimDuration) {
                    mOpenAnimDuration = array.getInt(index, mOpenAnimDuration);
                } else if (index == R.styleable.TLRLinearLayout_keepHeadRefreshing) {
                    isKeepHeadRefreshing = array.getBoolean(index, isKeepHeadRefreshing);
                } else if (index == R.styleable.TLRLinearLayout_keepFootLoading) {
                    isKeepFootLoading = array.getBoolean(index, isKeepFootLoading);
                } else if (index == R.styleable.TLRLinearLayout_refreshMaxMoveDistance) {
                    mRefreshMaxMoveDistance = array.getDimensionPixelOffset(index,
                            mRefreshMaxMoveDistance);
                } else if (index == R.styleable.TLRLinearLayout_loadMaxMoveDistance) {
                    mLoadMaxMoveDistance = array.getDimensionPixelOffset(index,
                            mLoadMaxMoveDistance);
                }
            }
        } finally {
            array.recycle();
        }
        TLRLog.v("isKeepHeadRefreshing = " + isKeepHeadRefreshing);
        TLRLog.v("isKeepFootLoading = " + isKeepFootLoading);
        TLRLog.v("mRefreshMaxMoveDistance = " + mRefreshMaxMoveDistance);
        TLRLog.v("mLoadMaxMoveDistance = " + mLoadMaxMoveDistance);
    }

    public void setTLRUiHandler(TLRUIHandler uiHandler) {
        mTLRUiHandler = uiHandler;
        mStatusController.setTLRUiHandler(mTLRUiHandler);
    }

    public void setHeadViewHeight(int height) {
        mHeadHeight = height;
        refreshThresholdHeight = (int) (mHeadHeight * mRefreshThreshold);
    }

    public void setFootViewHeight(int height) {
        mFootHeight = height;
        loadThresholdHeight = (int) (mFootHeight * mLoadThreshold);
    }

    /**
     * call by layout touch down
     */
    public void eventDown(float x, float y) {
        mLastX = mDownX = x;
        mLastY = mDownY = y;
    }

    /**
     * call by layout touch move
     */
    public void eventMove(float x, float y) {
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
    public void eventUp(float x, float y) {
        mDirection = Direction.NONE;
        mStatusController.calculatorUpRefreshStatus();
        mStatusController.calculatorUpLoadStatus();
        if (isKeepFootLoading && mTotalOffsetY <= -loadThresholdHeight) {
            startKeepAnimator();
        } else if (isKeepHeadRefreshing && mTotalOffsetY >= refreshThresholdHeight) {
            startKeepAnimator();
        } else {
            startResetAnimator();
        }
    }

    /**
     * eventMove distance must more than {@link ViewConfiguration#getScaledTouchSlop()}
     */
    public boolean canCalculatorV() {
        if (mDirection == Direction.DOWN || mDirection == Direction.UP) {
            return Math.abs(mLastY - mDownY) > mTouchSlop;
        }
        return false;
    }

    public void touchMoveLayoutView() {
        touchMoveLayoutView((int) mOffsetY);
    }

    public void touchMoveLayoutView(int offsetY) {
        moveOffsetY(offsetY);
    }

    /**
     * call view {@link android.view.View#offsetTopAndBottom(int)} method must cast offset to int
     */
    private void moveOffsetY(int y) {
        if (y == 0) {
            return;
        }
        //start ui move
        if (mTotalOffsetY == 0 && isBackStatus) {
            isBackStatus = false;
        }

        y = calculateMaxMoveDistance(y, mTotalOffsetY);

        if (y == 0) {
            return;
        }

        //TLRLog.d("mTotalOffsetY:" + mTotalOffsetY + " y:" + y);

        //move view
        mTotalOffsetY += y;

        tLRLinearLayout.move(y);

        //calculate move status
        mStatusController.calculateMoveRefreshStatus(y > 0);
        mStatusController.calculateMoveLoadStatus(y < 0);

        //notify offset
        if (mTotalOffsetY > 0 && mStatusController.getLoadStatus() == LoadStatus.IDLE) {
            notifyPixOffset(mTotalOffsetY, refreshThresholdHeight, y);
        }
        if (mTotalOffsetY < 0 && mStatusController.getRefreshStatus() == RefreshStatus.IDLE) {
            notifyPixOffset(mTotalOffsetY, loadThresholdHeight, y);
        }

        //end ui move
        if (mTotalOffsetY == 0 && !isBackStatus) {
            isBackStatus = true;
            notifyPixOffset(0, 0, y);
        }
    }

    private int calculateMaxMoveDistance(int y, int totalOffsetY) {
        int tempTotalOffsetY = totalOffsetY + y;

        // calculate refresh over max move distance
        if (tempTotalOffsetY > 0 && mRefreshMaxMoveDistance > 0
                && tempTotalOffsetY > mRefreshMaxMoveDistance) {
            y = mRefreshMaxMoveDistance - mTotalOffsetY;
        }

        // calculate load over max move distance
        if (tempTotalOffsetY < 0 && mLoadMaxMoveDistance > 0
                && -tempTotalOffsetY > mLoadMaxMoveDistance) {
            y = -mLoadMaxMoveDistance - mTotalOffsetY;
        }
        return y;
    }

    private void notifyPixOffset(int totalOffsetY, int height, int y) {
        int totalThresholdY = totalOffsetY;
        if (Math.abs(totalThresholdY) >= height) {
            totalThresholdY = height * Integer.signum(totalThresholdY);
        }
        float offset = 0.0f;
        if (totalThresholdY != 0) {
            offset = (float) (Math.round(((float) totalThresholdY / height) * 100)) / 100;
        }
        boolean isRefresh = totalOffsetY != 0 ? totalOffsetY > 0 : y < 0;
        mTLRUiHandler.onOffsetChanged(tLRLinearLayout.getTouchView(), isRefresh, totalOffsetY,
                totalThresholdY, y, offset);
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

    public void startAutoRefresh() {
        TLRLog.d("autoRefresh mHeadHeight:" + mHeadHeight);
        mStatusController.setAutoRefreshing(true);
        if (mHeadHeight == 0) {
            tLRLinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            TLRLog.d("autoRefresh onGlobalLayout mHeadHeight:" + mHeadHeight);
                            startAutoRefreshAnimator();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                tLRLinearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(
                                        this);
                            } else {
                                tLRLinearLayout.getViewTreeObserver().removeGlobalOnLayoutListener(
                                        this);
                            }
                        }
                    });
        } else {
            startAutoRefreshAnimator();
        }
    }

    private void startAutoRefreshAnimator() {
        if (mHeadHeight != 0) {
            endAllRunningAnimator();
            int startY = -mHeadHeight;
            mAutoAnimator = ValueAnimator.ofInt(startY, 0);
            mAutoAnimator.setDuration(mOpenAnimDuration);
            mAutoAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mAutoAnimator.addUpdateListener(new AnimUpdateListener(startY));
            mAutoAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAutoAnimator.removeListener(this);
                    TLRLog.v("startAutoRefreshAnimator isKeepHeadRefreshing:"
                            + isKeepHeadRefreshing);
                    if (isKeepHeadRefreshing) {
                        startKeepAnimator();
                    } else {
                        startResetAnimator();
                    }
                }
            });
            mAutoAnimator.start();
        }
    }

    public boolean hasAnyAnimatorRunning() {
        if (mAutoAnimator != null) {
            return mAutoAnimator.isRunning() || mAutoAnimator.isStarted();
        }
        if (mResetAnimator != null) {
            return mResetAnimator.isRunning() || mResetAnimator.isStarted();
        }
        if (mKeepAnimator != null) {
            return mKeepAnimator.isRunning() || mKeepAnimator.isStarted();
        }
        return false;
    }

    private void startKeepAnimator() {
        endAllRunningAnimator();
        int startY = mTotalOffsetY;
        int endY;
        if (mTotalOffsetY > 0) {
            endY = refreshThresholdHeight;
        } else {
            endY = -loadThresholdHeight;
        }
        if (DEBUG) {
            TLRLog.d("startKeepAnimator startY:" + startY + " endY:" + endY);
        }
        if (startY != 0 && startY != endY) {
            mKeepAnimator = ValueAnimator.ofInt(startY, endY);
            mKeepAnimator.setDuration(200);
            mKeepAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mKeepAnimator.addUpdateListener(new AnimUpdateListener(startY));
            mKeepAnimator.start();
        }
    }

    private void startResetAnimator() {
        if (mTotalOffsetY != 0) {
            endAllRunningAnimator();
            int startY = mTotalOffsetY;
            mResetAnimator = ValueAnimator.ofInt(startY, 0);
            long duration = (long) (mCloseAnimDuration * ((float) Math.abs(mTotalOffsetY)
                    / mHeadHeight));
            mResetAnimator.setDuration(duration);
            mResetAnimator.setInterpolator(new DecelerateInterpolator());
            mResetAnimator.addUpdateListener(new AnimUpdateListener(startY));
            mResetAnimator.start();
        }
    }

    private void endAllRunningAnimator() {
        if (mAutoAnimator != null && mAutoAnimator.isRunning()) {
            mAutoAnimator.end();
        }
        if (mResetAnimator != null && mResetAnimator.isRunning()) {
            mResetAnimator.end();
        }
        if (mKeepAnimator != null && mKeepAnimator.isRunning()) {
            mKeepAnimator.end();
        }
    }

    public int getTotalOffsetY() {
        return mTotalOffsetY;
    }

    public Direction getDirection() {
        return mDirection;
    }

    public void finishRefresh(boolean isSuccess, int errorCode) {
        if (mStatusController.isAutoRefreshing()) {
            mStatusController.setAutoRefreshing(false);
        }
        if (isKeepHeadRefreshing) {
            hookKeepView();
        }
        mStatusController.finishRefresh();
        mTLRUiHandler.onFinish(tLRLinearLayout.getTouchView(), true, isSuccess, errorCode);
    }

    public void finishLoad(boolean isSuccess, int errorCode) {
        if (isKeepFootLoading) {
            hookKeepView();
        }
        mStatusController.finishLoad();
        mTLRUiHandler.onFinish(tLRLinearLayout.getTouchView(), false, isSuccess, errorCode);
    }

    private void hookKeepView() {
        if (mHooks.size() != 0) {
            for (TLRUIHandlerHook hook : mHooks) {
                hook.handlerHook();
            }
        } else {
            resetKeepView();
        }
    }

    private void resetKeepView() {
        startResetAnimator();
    }

    public void hook(TLRUIHandlerHook hook) {
        if (hook != null) {
            mHooks.add(hook);
        }
    }

    public void releaseHook(TLRUIHandlerHook hook) {
        if (hook != null) {
            mHooks.remove(hook);
        }
        if (mHooks.size() == 0) {
            resetKeepView();
        }
    }

    public float getRefreshThreshold() {
        return mRefreshThreshold;
    }

    public void setRefreshThreshold(float refreshThreshold) {
        mRefreshThreshold = refreshThreshold;
        setHeadViewHeight(mHeadHeight);
    }

    public float getLoadThreshold() {
        return mLoadThreshold;
    }

    public void setLoadThreshold(float loadThreshold) {
        mLoadThreshold = loadThreshold;
        setFootViewHeight(mFootHeight);
    }

    public int getRefreshMaxMoveDistance() {
        return mRefreshMaxMoveDistance;
    }

    public void setRefreshMaxMoveDistance(int refreshMaxMoveDistance) {
        mRefreshMaxMoveDistance = refreshMaxMoveDistance;
    }

    public int getLoadMaxMoveDistance() {
        return mLoadMaxMoveDistance;
    }

    public void setLoadMaxMoveDistance(int loadMaxMoveDistance) {
        mLoadMaxMoveDistance = loadMaxMoveDistance;
    }

    public float getResistance() {
        return mResistance;
    }

    public void setResistance(float resistance) {
        mResistance = resistance;
    }

    public void setCloseAnimDuration(int closeAnimDuration) {
        mCloseAnimDuration = closeAnimDuration;
    }

    public void setOpenAnimDuration(int openAnimDuration) {
        mOpenAnimDuration = openAnimDuration;
    }

    public boolean isKeepHeadRefreshing() {
        return isKeepHeadRefreshing;
    }

    public void setKeepHeadRefreshing(boolean keepHeadRefreshing) {
        isKeepHeadRefreshing = keepHeadRefreshing;
    }

    public boolean isKeepFootLoading() {
        return isKeepFootLoading;
    }

    public void setKeepFootLoading(boolean keepFootLoading) {
        isKeepFootLoading = keepFootLoading;
    }

    public boolean isReleaseRefresh() {
        return mStatusController.isReleaseRefresh();
    }

    public void setReleaseRefresh(boolean releaseRefresh) {
        mStatusController.setReleaseRefresh(releaseRefresh);
    }

    public boolean isReleaseLoad() {
        return mStatusController.isReleaseLoad();
    }

    public void setReleaseLoad(boolean releaseLoad) {
        mStatusController.setReleaseLoad(releaseLoad);
    }

    public boolean isAutoRefreshing() {
        return mStatusController.isAutoRefreshing();
    }

    public boolean isRefreshing() {
        return mStatusController.isRefreshing();
    }

    public boolean isLoading() {
        return mStatusController.isLoading();
    }

    public boolean isBackStatus() {
        return isBackStatus;
    }

    public boolean isRefresh() {
        return getTotalOffsetY() > 0;
    }

    public boolean isLoad() {
        return getTotalOffsetY() < 0;
    }

    private class AnimUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private int lastY;

        AnimUpdateListener(int startY) {
            this.lastY = startY;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int value = (int) animation.getAnimatedValue();
            moveOffsetY(value - lastY);
            lastY = value;
        }
    }

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        NONE
    }
}
