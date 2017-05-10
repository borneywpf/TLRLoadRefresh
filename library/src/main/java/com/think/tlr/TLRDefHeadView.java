package com.think.tlr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by borney on 5/9/17.
 */
public class TLRDefHeadView extends LinearLayout implements TLRUiHandler {
    private ImageView mImageView;
    private TextView mTextView;

    public TLRDefHeadView(Context context) {
        this(context, null);
    }

    public TLRDefHeadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TLRDefHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.tlr_def_head_layout, this);
        setWillNotDraw(true);
        mImageView = (ImageView) findViewById(R.id.tlr_def_icon);
        mTextView = (TextView) findViewById(R.id.tlr_def_text);
    }

    @Override
    public void onRefreshStatusChanged(TLRLinearLayout.RefreshStatus status) {
        switch (status) {
            case PULL_DOWN:
                mTextView.setText(R.string.tlr_def_head_pull_down);
                break;
            case RELEASE_REFRESH:
                mTextView.setText(R.string.tlr_def_head_release_refresh);
                break;
            case REFRESHING:
                mTextView.setText(R.string.tlr_def_head_refreshing);
                break;
        }
    }

    public void setTextView(CharSequence sequence) {
        if (mTextView != null) {
            mTextView.setText(sequence);
        }
    }

    public void setImageResource(int id) {
        if (mImageView != null) {
            mImageView.setImageResource(id);
        }
    }

    public void setImageDrawable(Drawable drawalbe) {
        if (mImageView != null) {
            mImageView.setImageDrawable(drawalbe);
        }
    }

    @Override
    public void onLoadStatusChanged(TLRLinearLayout.LoadStatus status) {

    }

    @Override
    public void onOffsetChanged(boolean isRefresh, int totalOffsetY, int totalThresholdY, int offsetY, float threshOffset) {
        mImageView.setRotation(threshOffset * 360);
        mImageView.invalidate();
    }

    @Override
    public void onFinish(boolean isRefresh, boolean isSuccess, int errorCode) {
        mTextView.setText(R.string.tlr_def_head_refresh_complete);
    }
}
