package com.think.tlr;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author borney
 * @date 4/28/17
 * @see TLRUIHandler
 */
public class TLRDefHeadView extends LinearLayout implements TLRUIHandler {
    private boolean isMoveDown = false;
    private ImageView mImageView;
    private TextView mTextView;
    private ValueAnimator mReleaseAnimator;
    private AnimationDrawable mAnimationDrawable;

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
        initReleaseAnimator();
    }

    private void initReleaseAnimator() {
        mReleaseAnimator = ValueAnimator.ofFloat(0, 180);
        mReleaseAnimator.setDuration(210);
        mReleaseAnimator.setInterpolator(new LinearInterpolator());
        mReleaseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mImageView.setRotation(value);
            }
        });
    }

    @Override
    public void onRefreshStatusChanged(View target, TLRLinearLayout.RefreshStatus status) {
        switch (status) {
            case PULL_DOWN:
                if (isMoveDown) {
                    mTextView.setText(R.string.tlr_def_head_pull_down);
                }
                break;
            case RELEASE_REFRESH:
                mTextView.setText(R.string.tlr_def_head_release_refresh);
                mReleaseAnimator.start();
                break;
            case REFRESHING:
                if (mReleaseAnimator.isRunning()) {
                    mReleaseAnimator.end();
                }
                mImageView.setImageResource(R.drawable.tlr_def_refresh_loading);
                mAnimationDrawable = (AnimationDrawable) mImageView.getDrawable();
                mAnimationDrawable.start();
                mTextView.setText(R.string.tlr_def_head_refreshing);
                break;
            case IDLE:
                break;
        }
    }

    public void setTextView(CharSequence sequence) {
        if (mTextView != null) {
            mTextView.setText(sequence);
        }
    }

    @Override
    public void onLoadStatusChanged(View target, TLRLinearLayout.LoadStatus status) {

    }

    @Override
    public void onOffsetChanged(View target, boolean isRefresh, int totalOffsetY, int totalThresholdY, int offsetY, float threshOffset) {
        isMoveDown = offsetY > 0;
        if (threshOffset == 0) {
            mImageView.setRotation(0);
        }
    }

    @Override
    public void onFinish(View target, boolean isRefresh, boolean isSuccess, int errorCode) {
        mTextView.setText(R.string.tlr_def_head_refresh_complete);
        if (isRefresh) {
            mAnimationDrawable.stop();
            mImageView.setRotation(180);
            mImageView.setImageResource(R.drawable.tlr_def_refresh);
        }
    }
}
