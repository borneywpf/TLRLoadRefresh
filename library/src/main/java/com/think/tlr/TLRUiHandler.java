package com.think.tlr;

/**
 * Created by borney on 4/27/17.
 */
public interface TLRUiHandler {
    void onRefreshStatusChanged(TLRLinearLayout.RefreshStatus status);

    void onLoadStatusChanged(TLRLinearLayout.LoadStatus status);

    void onOffsetChanged(int totalOffsetY, int totalThresholdY, int offsetY, float threshOffset);
}
