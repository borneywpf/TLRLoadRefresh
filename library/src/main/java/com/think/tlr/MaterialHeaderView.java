package com.think.tlr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

/**
 * Created by borney on 5/12/17.
 */
public class MaterialHeaderView extends LinearLayout implements TLRUIHandler {
    private CircleImageView mCircleView;
    private MaterialProgressDrawable mProgress;
    // Default background for the progress spinner
    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;

    private static final int CIRCLE_DIAMETER = 40;

    private int mMediumAnimationDuration;

    private ValueAnimator mScaleAnimator;

    private TLRLinearLayout mTLRLinearLayout;

    private TLRUIHandlerHook mHook = new TLRUIHandlerHook() {
        @Override
        public void handlerHook() {

        }
    };

    public MaterialHeaderView(Context context) {
        this(context, null);
    }

    public MaterialHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.HORIZONTAL);
        setWillNotDraw(true);
        setGravity(Gravity.CENTER);

        mMediumAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        createProgressView();
        initUpScaleAnimator();
    }

    private void initUpScaleAnimator() {
        mScaleAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        mScaleAnimator.setDuration(mMediumAnimationDuration);
        mScaleAnimator.setInterpolator(new AccelerateInterpolator());
        mScaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                mCircleView.setScaleX(scale);
                mCircleView.setScaleY(scale);
                invalidate();
            }
        });
        mScaleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mTLRLinearLayout != null) {
                    mTLRLinearLayout.releaseHook(mHook);
                }
                mCircleView.setScaleX(1.0f);
                mCircleView.setScaleY(1.0f);
                invalidate();
                mCircleView.setVisibility(View.GONE);
            }
        });
    }

    private void createProgressView() {
        mCircleView = new CircleImageView(getContext(), CIRCLE_BG_LIGHT, CIRCLE_DIAMETER / 2);
        mProgress = new MaterialProgressDrawable(getContext(), this);
        mProgress.setBackgroundColor(CIRCLE_BG_LIGHT);
        mCircleView.setImageDrawable(mProgress);
        mCircleView.setVisibility(View.VISIBLE);
        mProgress.setAlpha(256);
        mProgress.setColorSchemeColors(getResources().getIntArray(R.array.google_colors));
        addView(mCircleView);
    }

    public void setTLRLinearLayout(TLRLinearLayout TLRLinearLayout) {
        mTLRLinearLayout = TLRLinearLayout;
    }

    @Override
    public void onRefreshStatusChanged(View target, TLRLinearLayout.RefreshStatus status) {
        if (status == TLRLinearLayout.RefreshStatus.REFRESHING) {
            if (mTLRLinearLayout != null) {
                mTLRLinearLayout.hook(mHook);
            }
            mProgress.setAlpha(255);
            mProgress.start();
        }
    }

    public void setColorSchemeColors(int[] colors) {
        mProgress.setColorSchemeColors(colors);
        invalidate();
    }

    @Override
    public void onLoadStatusChanged(View target, TLRLinearLayout.LoadStatus status) {

    }

    @Override
    public void onOffsetChanged(View target, boolean isRefresh, int totalOffsetY, int totalThresholdY, int offsetY, float threshOffset) {
        if (isRefresh && totalOffsetY == 0) {
            mCircleView.setVisibility(View.VISIBLE);
        }

        if (isRefresh && offsetY > 0) {
            if (threshOffset == 1) {
                threshOffset += (float) totalOffsetY / totalThresholdY;
            }
            mProgress.showArrow(true);
            mProgress.setArrowScale(Math.min(1.0f, threshOffset));
            mProgress.setProgressRotation((threshOffset));
            mProgress.setStartEndTrim(0, Math.min(.8f, 0.8f * threshOffset));
            invalidate();
        }
    }

    @Override
    public void onFinish(View target, boolean isRefresh, boolean isSuccess, int errorCode) {
        if (isRefresh) {
            mProgress.stop();
            mScaleAnimator.start();
        }
    }
}
