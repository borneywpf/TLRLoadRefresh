package com.think.tlr;

import android.view.View;

/**
 * @author borney
 * @date 4/28/17
 */
public interface TLRUIHandler {
    /**
     *
     * @param target
     * @param status
     */
    void onRefreshStatusChanged(View target, TLRLinearLayout.RefreshStatus status);

    /**
     * @param target
     * @param status
     */
    void onLoadStatusChanged(View target, TLRLinearLayout.LoadStatus status);

    /**
     * @param target
     * @param totalOffsetY
     * @param totalThresholdY
     * @param offsetY
     * @param threshOffset
     */
    void onOffsetChanged(View target, boolean isRefresh, int totalOffsetY, int totalThresholdY, int offsetY, float threshOffset);

    /**
     * @param target
     * @param isRefresh
     * @param isSuccess
     * @param errorCode
     */
    void onFinish(View target, boolean isRefresh, boolean isSuccess, int errorCode);
}
