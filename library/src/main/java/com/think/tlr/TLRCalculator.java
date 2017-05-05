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
     * 刷新状态机
     */
    private RefreshStatus mRefreshStatus = RefreshStatus.IDLE;
    /**
     * 加载状态机
     */
    private LoadStatus mLoadStatus = LoadStatus.IDLE;
    /**
     * 是否保存刷新view/加载view
     */
    private boolean isKeepHeadRefreshing = true, isKeepFootLoading = true;
    /**
     * 是否是自动刷新
     */
    private boolean isAutoRefresh = false;

    /**
     * 是否释放刷新/加载
     */
    private boolean isReleaseRefresh = true, isReleaseLoad = true;

    /**
     * UI是否回归到初始状态
     */
    private boolean isBackStatus = true;

    private TLRUiHandler mTLRUiHandler;

    private ValueAnimator mAutoAnimator, mResetAnimator, mKeepAnimator;

    private TLRLinearLayout mTLRLinearLayout;
    private int mTouchSlop;
    private Direction mDirection = Direction.NONE;

    public TLRCalculator(TLRLinearLayout layout, AttributeSet attrs) {
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
                } else if (index == R.styleable.TLRLinearLayout_closeAnimDuration) {
                    mCloseAnimDuration = array.getInt(index, mCloseAnimDuration);
                } else if (index == R.styleable.TLRLinearLayout_openAnimDuration) {
                    mOpenAnimDuration = array.getInt(index, mOpenAnimDuration);
                } else if (index == R.styleable.TLRLinearLayout_keepHeadRefreshing) {
                    isKeepHeadRefreshing = array.getBoolean(index, isKeepHeadRefreshing);
                } else if (index == R.styleable.TLRLinearLayout_keepFootLoading) {
                    isKeepFootLoading = array.getBoolean(index, isKeepFootLoading);
                } else if (index == R.styleable.TLRLinearLayout_releaseRefresh) {
                    isReleaseRefresh = array.getBoolean(index, isReleaseRefresh);
                } else if (index == R.styleable.TLRLinearLayout_releaseLoad) {
                    isReleaseLoad = array.getBoolean(index, isReleaseLoad);
                }
            }
        } finally {
            array.recycle();
        }
        Log.v("isKeepHeadRefreshing = " + isKeepHeadRefreshing);
        Log.v("isKeepFootLoading = " + isKeepFootLoading);
        Log.v("isReleaseRefresh = " + isReleaseRefresh);
        Log.v("isReleaseLoad = " + isReleaseLoad);
    }

    public void setTLRUiHandler(TLRUiHandler uiHandler) {
        mTLRUiHandler = uiHandler;
    }

    public void setHeadViewHeight(int height) {
        mHeadHeight = height;
        mRefreshThresholdHeight = (int) (mHeadHeight * mRefreshThreshold);
    }

    public void setFootViewHeight(int height) {
        mFootHeight = height;
        mLoadThresholdHeight = (int) (mFootHeight * mLoadThreshold);
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
        calculatorUpRefreshStatus();
        calculatorUpLoadStatus();
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

        mTotalOffsetY += y;
        mTLRLinearLayout.move(y);
        if (mTotalOffsetY > 0 && mLoadStatus == LoadStatus.IDLE) {
            calculateMoveRefreshStatus(y > 0);
            notifyPixOffset(mTotalOffsetY, mRefreshThresholdHeight, y);
        }
        if (mTotalOffsetY < 0 && mRefreshStatus == RefreshStatus.IDLE) {
            calculateMoveLoadStatus(y < 0);
            notifyPixOffset(mTotalOffsetY, mLoadThresholdHeight, y);
        }

        //end ui move
        if (mTotalOffsetY == 0 && !isBackStatus) {
            isBackStatus = true;
            notifyPixOffset(0, 0, y);
        }
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
        if (mTLRUiHandler != null) {
            mTLRUiHandler.onOffsetChanged(totalOffsetY, totalThresholdY, y, offset);
        }
    }

    /**
     * calculate refresh view status
     *
     * @param down view is move down now
     */
    private void calculateMoveRefreshStatus(boolean down) {
        if (down) {//view向下运动
            if (mRefreshStatus == RefreshStatus.IDLE) {
                notifyRefreshStatusChanged(RefreshStatus.PULL_DOWN);
            }
            if (mTotalOffsetY >= mRefreshThresholdHeight && mRefreshStatus == RefreshStatus.PULL_DOWN) {
                notifyRefreshStatusChanged(RefreshStatus.RELEASE_REFRESH);
                if (isAutoRefresh) {
                    notifyRefreshStatusChanged(RefreshStatus.REFRESHING);
                    notifyRefreshStatusChanged(RefreshStatus.IDLE);
                    isAutoRefresh = false;
                }
            }
        } else {//view向上运动
            if (mTotalOffsetY < mRefreshThresholdHeight && mRefreshStatus == RefreshStatus.RELEASE_REFRESH) {
                notifyRefreshStatusChanged(RefreshStatus.PULL_DOWN);
            }
            if (mRefreshStatus == RefreshStatus.PULL_DOWN) {
                notifyRefreshStatusChanged(RefreshStatus.IDLE);
            }
        }
    }

    /**
     * calculate load view status
     *
     * @param up view is move up now
     */
    private void calculateMoveLoadStatus(boolean up) {
        if (up) {//view向上运动
            if (mTotalOffsetY < 0 && mLoadStatus == LoadStatus.IDLE) {
                notifyLoadStatusChanged(LoadStatus.PULL_UP);
            }
            if (Math.abs(mTotalOffsetY) >= mLoadThresholdHeight && mLoadStatus == LoadStatus.PULL_UP) {
                notifyLoadStatusChanged(LoadStatus.RELEASE_LOAD);
            }
        } else {//view向下运动
            if (Math.abs(mTotalOffsetY) < mLoadThresholdHeight && mLoadStatus == LoadStatus.RELEASE_LOAD) {
                notifyLoadStatusChanged(LoadStatus.PULL_UP);
            }
            if (mLoadStatus == LoadStatus.PULL_UP) {
                notifyLoadStatusChanged(LoadStatus.IDLE);
            }
        }
    }

    private void calculatorUpLoadStatus() {
        if (mLoadStatus == LoadStatus.RELEASE_LOAD) {
            if (isReleaseLoad) {
                notifyLoadStatusChanged(LoadStatus.LOADING);
                notifyLoadStatusChanged(LoadStatus.IDLE);
            }
        } else if (mLoadStatus != LoadStatus.IDLE) {
            notifyLoadStatusChanged(LoadStatus.IDLE);
        }
    }

    private void calculatorUpRefreshStatus() {
        if (mRefreshStatus == RefreshStatus.RELEASE_REFRESH) {
            if (isReleaseRefresh) {
                notifyRefreshStatusChanged(RefreshStatus.REFRESHING);
                notifyRefreshStatusChanged(RefreshStatus.IDLE);
            }
        } else if (mRefreshStatus != RefreshStatus.IDLE) {
            notifyRefreshStatusChanged(RefreshStatus.IDLE);
        }
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
        Log.d("startAutoRefresh mHeadHeight:" + mHeadHeight);
        isAutoRefresh = true;
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

    public boolean hasAnimatorRunning() {
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

    public Direction getDirection() {
        return mDirection;
    }

    private void notifyRefreshStatusChanged(RefreshStatus status) {
        if (mRefreshStatus == status) {
            return;
        }
        mRefreshStatus = status;
        if (mRefreshStatus == RefreshStatus.IDLE) {
            isAutoRefresh = false;
        }
        if (mTLRUiHandler != null) {
            mTLRUiHandler.onRefreshStatusChanged(mRefreshStatus);
        }
    }

    private void notifyLoadStatusChanged(LoadStatus status) {
        if (mLoadStatus == status) {
            return;
        }
        mLoadStatus = status;
        if (mTLRUiHandler != null) {
            mTLRUiHandler.onLoadStatusChanged(mLoadStatus);
        }
    }

    public void resetKeepView() {
        startResetAnimator();
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
