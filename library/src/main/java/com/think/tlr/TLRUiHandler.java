package com.think.tlr;

/**
 * Created by borney on 4/27/17.
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
