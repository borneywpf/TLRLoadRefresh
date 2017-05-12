package com.think.tlr;

import android.view.View;

/**
 * @author borney
 * @date 4/28/17
 */
public class TLRUIHandlerAdapter implements TLRUIHandler {
    @Override
    public void onRefreshStatusChanged(View target, TLRLinearLayout.RefreshStatus status) {

    }

    @Override
    public void onLoadStatusChanged(View target, TLRLinearLayout.LoadStatus status) {

    }

    @Override
    public void onOffsetChanged(View target, boolean isRefresh, int totalOffsetY, int totalThresholdY, int offsetY, float threshOffset) {

    }

    @Override
    public void onFinish(View target, boolean isRefresh, boolean isSuccess, int errorCode) {

    }
}
