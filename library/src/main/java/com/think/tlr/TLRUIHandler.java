package com.think.tlr;

import android.view.View;

/**
 * @author borney
 * @date 4/28/17
 */
public interface TLRUIHandler {
    /**
     * Callback refresh state to the specified target
     */
    void onRefreshStatusChanged(View target, TLRLinearLayout.RefreshStatus status);

    /**
     * Callback load state to the specified target
     */
    void onLoadStatusChanged(View target, TLRLinearLayout.LoadStatus status);

    /**
     * Callback offset pix to the specified target
     * @param target
     * @param isRefresh
     * @param totalOffsetY
     * @param totalThresholdY
     * @param offsetY
     * @param threshOffset
     */
    void onOffsetChanged(View target, boolean isRefresh, int totalOffsetY, int totalThresholdY,
            int offsetY, float threshOffset);

    /**
     * Callback finish state for  refresh or load to the specified target
     */
    void onFinish(View target, boolean isRefresh, boolean isSuccess, int errorCode);
}
