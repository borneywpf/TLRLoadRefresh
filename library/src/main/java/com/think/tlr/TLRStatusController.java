package com.think.tlr;

import com.think.tlr.TLRLinearLayout.LoadStatus;
import com.think.tlr.TLRLinearLayout.RefreshStatus;

/**
 * Created by borney on 5/5/17.
 */
class TLRStatusController {
    private TLRCalculator mCalculator;
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
     * 是否释放刷新/加载
     */
    private boolean isReleaseRefresh = true, isReleaseLoad = true;

    /**
     * 是否是自动刷新
     */
    private boolean isAutoRefresh = false;

    private TLRUiHandler mTLRUiHandler;

    TLRStatusController(TLRCalculator calculator) {
        mCalculator = calculator;
    }

    /**
     * calculate refresh view status
     *
     * @param down view is move down now
     */
    public void calculateMoveRefreshStatus(boolean down) {
        int totalOffsetY = mCalculator.getTotalOffsetY();
        //y 方向移动正值,切Load为初始状态
        if (totalOffsetY > 0 && mLoadStatus == LoadStatus.IDLE) {
            if (down) {//view向下运动
                if (mRefreshStatus == RefreshStatus.IDLE) {
                    notifyRefreshStatusChanged(RefreshStatus.PULL_DOWN);
                }
                if (totalOffsetY >= mRefreshThresholdHeight && mRefreshStatus == RefreshStatus.PULL_DOWN) {
                    notifyRefreshStatusChanged(RefreshStatus.RELEASE_REFRESH);
                    if (isAutoRefresh) {
                        notifyRefreshStatusChanged(RefreshStatus.REFRESHING);
                        notifyRefreshStatusChanged(RefreshStatus.IDLE);
                        isAutoRefresh = false;
                    }
                }
            } else {//view向上运动
                if (totalOffsetY < mRefreshThresholdHeight && mRefreshStatus == RefreshStatus.RELEASE_REFRESH) {
                    notifyRefreshStatusChanged(RefreshStatus.PULL_DOWN);
                }
                if (mRefreshStatus == RefreshStatus.PULL_DOWN) {
                    notifyRefreshStatusChanged(RefreshStatus.IDLE);
                }
            }
        }
    }

    /**
     * calculate load view status
     *
     * @param up view is move up now
     */
    public void calculateMoveLoadStatus(boolean up) {
        int totalOffsetY = mCalculator.getTotalOffsetY();
        if (totalOffsetY < 0 && mRefreshStatus == RefreshStatus.IDLE) {
            if (up) {//view向上运动
                if (totalOffsetY < 0 && mLoadStatus == LoadStatus.IDLE) {
                    notifyLoadStatusChanged(LoadStatus.PULL_UP);
                }
                if (Math.abs(totalOffsetY) >= mLoadThresholdHeight && mLoadStatus == LoadStatus.PULL_UP) {
                    notifyLoadStatusChanged(LoadStatus.RELEASE_LOAD);
                }
            } else {//view向下运动
                if (Math.abs(totalOffsetY) < mLoadThresholdHeight && mLoadStatus == LoadStatus.RELEASE_LOAD) {
                    notifyLoadStatusChanged(LoadStatus.PULL_UP);
                }
                if (mLoadStatus == LoadStatus.PULL_UP) {
                    notifyLoadStatusChanged(LoadStatus.IDLE);
                }
            }
        }
    }

    /**
     * calculate load view status when up motionevent
     */
    public void calculatorUpLoadStatus() {
        if (mLoadStatus == LoadStatus.RELEASE_LOAD) {
            if (isReleaseLoad) {
                notifyLoadStatusChanged(LoadStatus.LOADING);
                notifyLoadStatusChanged(LoadStatus.IDLE);
            }
        } else if (mLoadStatus != LoadStatus.IDLE) {
            notifyLoadStatusChanged(LoadStatus.IDLE);
        }
    }

    /**
     * calculate refresh view status when up motionevent
     */
    public void calculatorUpRefreshStatus() {
        if (mRefreshStatus == RefreshStatus.RELEASE_REFRESH) {
            if (isReleaseRefresh) {
                notifyRefreshStatusChanged(RefreshStatus.REFRESHING);
                notifyRefreshStatusChanged(RefreshStatus.IDLE);
            }
        } else if (mRefreshStatus != RefreshStatus.IDLE) {
            notifyRefreshStatusChanged(RefreshStatus.IDLE);
        }
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

    public void setReleaseRefresh(boolean refresh) {
        isReleaseRefresh = refresh;
    }

    public void setReleaseLoad(boolean load) {
        isReleaseLoad = load;
    }

    public void setTLRUiHandler(TLRUiHandler uiHandler) {
        mTLRUiHandler = uiHandler;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        isAutoRefresh = autoRefresh;
    }

    public void setLoadThresholdHeight(int height) {
        mLoadThresholdHeight = height;
    }

    public void setRefreshThresholdHeight(int height) {
        mRefreshThresholdHeight = height;
    }

    public LoadStatus getLoadStatus() {
        return mLoadStatus;
    }

    public RefreshStatus getRefreshStatus() {
        return mRefreshStatus;
    }
}
