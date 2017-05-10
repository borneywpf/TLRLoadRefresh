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

/**
 * Created by borney on 5/3/17.
 */
class TLRCalculator {
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
     * 刷新阀值(高度), 加载阀值
     */
    private int mRefreshThresholdHeight, mLoadThresholdHeight;

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

    private TLRUiHandler mTLRUiHandler;

    private ValueAnimator mAutoAnimator, mResetAnimator, mKeepAnimator;

    private TLRLinearLayout mTLRLinearLayout;
    private int mTouchSlop;
    private Direction mDirection = Direction.NONE;

    public TLRCalculator(TLRLinearLayout layout, AttributeSet attrs) {
        mTLRLinearLayout = layout;
        Context context = layout.getContext();
        initAttrs(context, attrs);
        mStatusController = new TLRStatusController(this, context, attrs);
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
                } else if (index == R.styleable.TLRLinearLayout_closeAnimDuration) {
                    mCloseAnimDuration = array.getInt(index, mCloseAnimDuration);
                } else if (index == R.styleable.TLRLinearLayout_openAnimDuration) {
                    mOpenAnimDuration = array.getInt(index, mOpenAnimDuration);
                } else if (index == R.styleable.TLRLinearLayout_keepHeadRefreshing) {
                    isKeepHeadRefreshing = array.getBoolean(index, isKeepHeadRefreshing);
                } else if (index == R.styleable.TLRLinearLayout_keepFootLoading) {
                    isKeepFootLoading = array.getBoolean(index, isKeepFootLoading);
                } else if (index == R.styleable.TLRLinearLayout_refreshMaxMoveDistance) {
                    mRefreshMaxMoveDistance = array.getDimensionPixelOffset(index, mRefreshMaxMoveDistance);
                } else if (index == R.styleable.TLRLinearLayout_loadMaxMoveDistance) {
                    mLoadMaxMoveDistance = array.getDimensionPixelOffset(index, mLoadMaxMoveDistance);
                }
            }
        } finally {
            array.recycle();
        }
        Log.v("isKeepHeadRefreshing = " + isKeepHeadRefreshing);
        Log.v("isKeepFootLoading = " + isKeepFootLoading);
        Log.v("mRefreshMaxMoveDistance = " + mRefreshMaxMoveDistance);
        Log.v("mLoadMaxMoveDistance = " + mLoadMaxMoveDistance);
    }

    public void setTLRUiHandler(TLRUiHandler uiHandler) {
        mTLRUiHandler = uiHandler;
        mStatusController.setTLRUiHandler(mTLRUiHandler);
    }

    public void setHeadViewHeight(int height) {
        mHeadHeight = height;
        mRefreshThresholdHeight = (int) (mHeadHeight * mRefreshThreshold);
        mStatusController.setRefreshThresholdHeight(mRefreshThresholdHeight);
    }

    public void setFootViewHeight(int height) {
        mFootHeight = height;
        mLoadThresholdHeight = (int) (mFootHeight * mLoadThreshold);
        mStatusController.setLoadThresholdHeight(mLoadThresholdHeight);
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
        if (isKeepFootLoading && mTotalOffsetY <= -mLoadThresholdHeight) {
            startKeepAnimator();
        } else if (isKeepHeadRefreshing && mTotalOffsetY >= mRefreshThresholdHeight) {
            startKeepAnimator();
        } else {
            startResetAnimator();
        }
    }

    /**
     * eventMove distance must more than {@link ViewConfiguration#getScaledTouchSlop()}
     *
     * @return
     */
    public boolean canCalculatorV() {
        if (mDirection == Direction.DOWN || mDirection == Direction.UP) {
            return Math.abs(mLastY - mDownY) > mTouchSlop;
        }
        return false;
    }

    public void touchMoveLayoutView() {
        moveLayoutView((int) mOffsetY);
    }

    public void touchMoveLayoutView(int offsetY) {
        moveLayoutView(offsetY);
    }

    /**
     * call view {@link android.view.View#offsetTopAndBottom(int)} method must cast offset to int
     *
     * @return
     */
    private void moveLayoutView(int y) {
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

        //Log.d("mTotalOffsetY:" + mTotalOffsetY + " y:" + y);

        //move view
        mTotalOffsetY += y;

        mTLRLinearLayout.move(y);

        //calculate move status
        mStatusController.calculateMoveRefreshStatus(y > 0);
        mStatusController.calculateMoveLoadStatus(y < 0);

        //notify offset
        if (mTotalOffsetY > 0 && mStatusController.getLoadStatus() == LoadStatus.IDLE) {
            notifyPixOffset(mTotalOffsetY, mRefreshThresholdHeight, y);
        }
        if (mTotalOffsetY < 0 && mStatusController.getRefreshStatus() == RefreshStatus.IDLE) {
            notifyPixOffset(mTotalOffsetY, mLoadThresholdHeight, y);
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
        if (tempTotalOffsetY > 0 && mRefreshMaxMoveDistance > 0 && tempTotalOffsetY > mRefreshMaxMoveDistance) {
            y = mRefreshMaxMoveDistance - mTotalOffsetY;
        }

        // calculate load over max move distance
        if (tempTotalOffsetY < 0 && mLoadMaxMoveDistance > 0 && -tempTotalOffsetY > mLoadMaxMoveDistance) {
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

        mTLRUiHandler.onOffsetChanged(totalOffsetY, totalThresholdY, y, offset);
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
        Log.d("autoRefresh mHeadHeight:" + mHeadHeight);
        mStatusController.setAutoRefresh(true);
        if (mHeadHeight == 0) {
            mTLRLinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Log.d("autoRefresh onGlobalLayout mHeadHeight:" + mHeadHeight);
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
        if (mHeadHeight != 0) {
            if (mAutoAnimator != null && mAutoAnimator.isRunning()) {
                mAutoAnimator.end();
            }
            int startY = -mHeadHeight;
            mAutoAnimator = ValueAnimator.ofInt(startY, 0);
            mAutoAnimator.setDuration(mOpenAnimDuration);
            mAutoAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mAutoAnimator.addUpdateListener(new AnimUpdateListener(startY));
            mAutoAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.e("isKeepHeadRefreshing:" + isKeepHeadRefreshing);
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
        if (mKeepAnimator != null && mKeepAnimator.isRunning()) {
            mKeepAnimator.end();
        }
        int startY = mTotalOffsetY;
        int endY;
        if (mTotalOffsetY > 0) {
            endY = mRefreshThresholdHeight;
        } else {
            endY = -mLoadThresholdHeight;
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
            if (mResetAnimator != null && mResetAnimator.isRunning()) {
                mResetAnimator.end();
            }
            int startY = mTotalOffsetY;
            mResetAnimator = ValueAnimator.ofInt(startY, 0);
            mResetAnimator.setDuration(mCloseAnimDuration);
            mResetAnimator.setInterpolator(new DecelerateInterpolator());
            mResetAnimator.addUpdateListener(new AnimUpdateListener(startY));
            mResetAnimator.start();
        }
    }

    public int getTotalOffsetY() {
        return mTotalOffsetY;
    }

    public Direction getDirection() {
        return mDirection;
    }

    public void resetKeepView() {
        startResetAnimator();
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

    private class AnimUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private int lastY;

        AnimUpdateListener(int startY) {
            this.lastY = startY;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int value = (int) animation.getAnimatedValue();
            moveLayoutView(value - lastY);
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
