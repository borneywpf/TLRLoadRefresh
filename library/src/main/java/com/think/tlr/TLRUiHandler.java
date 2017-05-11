package com.think.tlr;

/**
 * @author borney
 * @date 4/28/17
 */
public interface TLRUiHandler {
    /**
     * @param status
     */
    void onRefreshStatusChanged(TLRLinearLayout.RefreshStatus status);

    /**
     * @param status
     */
    void onLoadStatusChanged(TLRLinearLayout.LoadStatus status);

    /**
     * @param totalOffsetY
     * @param totalThresholdY
     * @param offsetY
     * @param threshOffset
     */
    void onOffsetChanged(boolean isRefresh, int totalOffsetY, int totalThresholdY, int offsetY, float threshOffset);

    /**
     * @param isRefresh
     * @param isSuccess
     * @param errorCode
     */
    void onFinish(boolean isRefresh, boolean isSuccess, int errorCode);
}
