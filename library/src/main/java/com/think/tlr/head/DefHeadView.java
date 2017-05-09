package com.think.tlr.head;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.think.tlr.TLRLinearLayout;
import com.think.tlr.TLRUiHandler;

/**
 * Created by borney on 5/9/17.
 */
public class DefHeadView extends LinearLayout implements TLRUiHandler {

    public DefHeadView(Context context) {
        this(context, null);
    }

    public DefHeadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.HORIZONTAL);
        setWillNotDraw(true);
    }

    @Override
    public void onRefreshStatusChanged(TLRLinearLayout.RefreshStatus status) {

    }

    @Override
    public void onLoadStatusChanged(TLRLinearLayout.LoadStatus status) {

    }

    @Override
    public void onOffsetChanged(int totalOffsetY, int totalThresholdY, int offsetY, float threshOffset) {

    }
}
