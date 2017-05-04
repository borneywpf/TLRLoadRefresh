package com.think.tlr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
    private boolean isKeepHeadAnimator = true, isKeepFootAnimator = true;
    /**
     * 是否是自动刷新
     */
    private boolean isAutoRefresh = false;
    private ValueAnimator mValueAnimator;
    private TLRLinearLayout mTLRLinearLayout;
    private int mTouchSlop;
    private boolean isRefresh;
    private boolean isLoad;
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
                } else if (index == R.styleable.TLRLinearLayout_keepHeadAnimator) {
                    isKeepHeadAnimator = array.getBoolean(index, isKeepHeadAnimator);
                } else if (index == R.styleable.TLRLinearLayout_keepFootAnimator) {
                    isKeepFootAnimator = array.getBoolean(index, isKeepFootAnimator);
                }
            }
        } finally {
            array.recycle();
        }
    }

    public void setTLR(boolean refresh, boolean load) {
        if (mRefreshStatus == RefreshStatus.IDLE) {
            isRefresh = refresh;
        }
        if (mLoadStatus == LoadStatus.IDLE) {
            isLoad = load;
        }
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
    public boolean eventDown(float x, float y) {
        if (mRefreshStatus == RefreshStatus.IDLE && mLoadStatus == LoadStatus.IDLE) {
            reset();
            if (mValueAnimator != null && mValueAnimator.isRunning()) {
                mValueAnimator.end();
            }
            mLastX = mDownX = x;
            mLastY = mDownY = y;
            return true;
        } else {
            return false;
        }
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
        if (mRefreshStatus == RefreshStatus.RELEASE_REFRESH) {
            notifyRefreshStatusChanged(RefreshStatus.REFRESHING);
        } else if (mRefreshStatus != RefreshStatus.IDLE) {
            startResetAnimator();
        }
        if (mLoadStatus == LoadStatus.RELEASE_LOAD) {
            notifyLoadStatusChanged(LoadStatus.LOADING);
        } else if (mLoadStatus != LoadStatus.IDLE) {
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
        mTotalOffsetY += y;
        //Log.d("total:" + mTotalOffsetY + " mRefreshThresholdHeight:" + mRefreshThresholdHeight + " isRefresh:" + isRefresh + " isLoad:" + isLoad);
        calculateRefreshStatus();
        calculateLoadStatus();
        mTLRLinearLayout.move(y);
    }

    private void calculateRefreshStatus() {
        if (isRefresh && Math.abs(mTotalOffsetY) >= 0 && mRefreshStatus == RefreshStatus.IDLE) {
            notifyRefreshStatusChanged(RefreshStatus.PULL_DOWN);
        }
        if (Math.abs(mTotalOffsetY) >= mRefreshThresholdHeight && mRefreshStatus == RefreshStatus.PULL_DOWN) {
            notifyRefreshStatusChanged(RefreshStatus.RELEASE_REFRESH);
            if (isAutoRefresh) {
                notifyRefreshStatusChanged(RefreshStatus.REFRESHING);
            }
        }
    }

    private void calculateLoadStatus() {
        if (isLoad && Math.abs(mTotalOffsetY) >= 0 && mLoadStatus == LoadStatus.IDLE) {
            notifyLoadStatusChanged(LoadStatus.PULL_UP);
        }

        if (Math.abs(mTotalOffsetY) >= mLoadThresholdHeight && mLoadStatus == LoadStatus.PULL_UP) {
            notifyLoadStatusChanged(LoadStatus.RELEASE_LOAD);
        }
    }

    private void reset() {
        isRefresh = false;
        isLoad = false;
        isAutoRefresh = false;
        mLastX = mDownX = 0;
        mLastY = mDownY = 0;
        mDirection = Direction.NONE;
        setOffset(0, 0);
    }

    private void resetUI() {
        if (mTotalOffsetY != 0) {
            moveLayoutView(-mTotalOffsetY);
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
        isRefresh = true;
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
            startYAnimator(-mHeadHeight, 0, new AccelerateDecelerateInterpolator(), mOpenAnimDuration, null);
        }
    }

    private void startResetAnimator() {
        if (mTotalOffsetY != 0) {
            startYAnimator(mTotalOffsetY, 0, new DecelerateInterpolator(), mCloseAnimDuration, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    notifyRefreshStatusChanged(RefreshStatus.IDLE);
                    notifyLoadStatusChanged(LoadStatus.IDLE);
                    resetUI();
                }
            });
        }
    }

    private void startYAnimator(final int startY, int endY, TimeInterpolator interpolator, long duration, Animator.AnimatorListener animatorListener) {
        mValueAnimator = ValueAnimator.ofInt(startY, endY);
        mValueAnimator.setDuration(duration);
        mValueAnimator.setInterpolator(interpolator);
        if (animatorListener != null) {
            mValueAnimator.addListener(animatorListener);
        }
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

    public Direction touchDirection() {
        return mDirection;
    }

    private void notifyRefreshStatusChanged(RefreshStatus status) {
        mRefreshStatus = status;
        Log.i("mRefreshStatus=" + mRefreshStatus);
        if (!isKeepHeadAnimator && mRefreshStatus == RefreshStatus.REFRESHING) {
            startResetAnimator();
        }
    }

    private void notifyLoadStatusChanged(LoadStatus status) {
        mLoadStatus = status;
        Log.w("mLoadStatus=" + mLoadStatus);
        if (!isKeepFootAnimator && mLoadStatus == LoadStatus.LOADING) {
            startResetAnimator();
        }
    }

    public boolean isRefresh() {
        return mRefreshStatus != RefreshStatus.IDLE;
    }

    public boolean isLoad() {
        return mLoadStatus != LoadStatus.IDLE;
    }

    public void endRefresh() {
        if (mRefreshStatus == RefreshStatus.REFRESHING) {
            notifyRefreshStatusChanged(RefreshStatus.IDLE);
            startResetAnimator();
        }
    }

    public void endLoad() {
        if (mLoadStatus == LoadStatus.LOADING) {
            notifyLoadStatusChanged(LoadStatus.IDLE);
            startResetAnimator();
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
