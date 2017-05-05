package com.think.tlr;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by borney on 4/28/17.
 */
public final class TLRLinearLayout extends ViewGroup {
    public static final int FLAG_LABEL_HEAD = 0x01;
    public static final int FLAG_LABEL_CONTENT = 0x02;
    public static final int FLAG_LABEL_FOOT = 0x04;
    private boolean isEnableLoad = false;
    private boolean isEnableRefresh = false;
    private boolean isKeepContentLayout = false;
    private View mHeaderView;
    private List<View> mContentViews;
    private List<View> mContentChilds;
    private LinearLayout mContentLayout;
    private View mFooterView;
    private TLRCalculator mCalculator;
    private TLRUiHandlerWrapper mUiHandlerWrapper;

    public enum RefreshStatus {
        IDLE, PULL_DOWN, RELEASE_REFRESH, REFRESHING
    }

    public enum LoadStatus {
        IDLE, PULL_UP, RELEASE_LOAD, LOADING
    }

    public TLRLinearLayout(Context context) {
        this(context, null);
    }

    public TLRLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TLRLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        setWillNotDraw(false);
        mUiHandlerWrapper = new TLRUiHandlerWrapper();
        mCalculator = new TLRCalculator(this, attrs);
        mCalculator.setTLRUiHandler(mUiHandlerWrapper);
        mContentViews = new ArrayList<>();
        mContentChilds = new ArrayList<>();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TLRLinearLayout);
        if (array == null) {
            Log.e("initAttrs array is null");
            return;
        }
        try {
            final int N = array.getIndexCount();
            for (int i = 0; i < N; i++) {
                int index = array.getIndex(i);
                if (index == R.styleable.TLRLinearLayout_enableLoad) {
                    isEnableLoad = array.getBoolean(index, false);
                    Log.i("isEnableLoad = " + isEnableLoad);
                } else if (index == R.styleable.TLRLinearLayout_enableRefresh) {
                    isEnableRefresh = array.getBoolean(index, false);
                    Log.i("isEnableRefresh = " + isEnableRefresh);
                } else if (index == R.styleable.TLRLinearLayout_keepContentLayout) {
                    isKeepContentLayout = array.getBoolean(index, false);
                    Log.i("isKeepContentLayout = " + isKeepContentLayout);
                }
            }
        } finally {
            array.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        mContentLayout = new LinearLayout(getContext());
        mContentLayout.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams l = getLayoutParams();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            Log.d("child = " + child.getClass().getSimpleName() + " params.label = " + params.label);
            if (params.label == FLAG_LABEL_HEAD) {
                if (i != 0) {
                    throw new RuntimeException("head must in first");
                }
                mHeaderView = child;
            } else if (params.label == FLAG_LABEL_FOOT) {
                if (i != count - 1) {
                    throw new RuntimeException("foot must in last!!!");
                }
                mFooterView = child;
            } else if (params.label == FLAG_LABEL_CONTENT) {
                mContentViews.add(child);
                mContentChilds.add(child);
            } else {
                mContentChilds.add(child);
            }
        }

        if (mHeaderView == null) {
            Log.d("has not header view!");
        }

        if (mFooterView == null) {
            Log.d("has not footer view!");
        }

        for (View view : mContentChilds) {
            removeView(view);
            mContentLayout.addView(view);
        }
        addView(mContentLayout, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        Log.i("ContentLayout count:" + mContentLayout.getChildCount());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isEnableLoad && !isEnableRefresh) {
            return superdispatchTouchEvent(ev);
        }
        if (mCalculator.hasAnimatorRunning()) {
            return superdispatchTouchEvent(ev);
        }
        int action = ev.getAction();
        float x = ev.getX();
        float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mCalculator.eventDown(x, y);
                superdispatchTouchEvent(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                mCalculator.eventMove(x, y);
                if (mCalculator.canCalculatorV()) {
                    boolean refresh = false;
                    boolean load = false;
                    for (View view : mContentViews) {
                        if (isTouchViewRefresh(view, x, y)) {
                            refresh = true;
                        }
                        if (isTouchViewLoad(view, x, y)) {
                            load = true;
                        }
                    }

                    refresh &= isEnableRefresh;
                    load &= isEnableLoad;

                    if (refresh || load) {
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        superdispatchTouchEvent(ev);
                        mCalculator.touchMoveLayoutView();
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mCalculator.eventUp(x, y);
                break;
        }
        return superdispatchTouchEvent(ev);
    }

    private boolean superdispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                // Measure the child.
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                if (child.equals(mContentLayout)) {
                    width = child.getMeasuredWidth();
                    height = child.getMeasuredHeight();
                } else if (child.equals(mHeaderView)) {
                    mCalculator.setHeadViewHeight(child.getMeasuredHeight());
                } else if (child.equals(mFooterView)) {
                    mCalculator.setFootViewHeight(child.getMeasuredHeight());
                }
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int parentLeft = getPaddingLeft();
        int parentTop = getPaddingTop();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            int left = parentLeft;
            int right = left + width;
            if (child.equals(mHeaderView)) {
                int bottom = parentTop;
                int top = bottom - height;
                child.layout(left, top, right, bottom);
            } else if (child.equals(mFooterView)) {
                int top = parentTop + getMeasuredHeight();
                int bottom = top + height;
                child.layout(left, top, right, bottom);
            } else {
                child.layout(left, parentTop, right, parentTop + height);
            }
        }
    }

    private boolean inView(View view, float x, float y) {
        final int scrollY = getScrollY();
        return !(y < view.getTop() - scrollY
                || y >= view.getBottom() - scrollY
                || x < view.getLeft()
                || x >= view.getRight());
    }

    public void startAutoRefresh() {
        mCalculator.startAutoRefresh();
    }

    public void resetKeepView() {
        mCalculator.resetKeepView();
    }

    void move(int y) {
        if (!isKeepContentLayout) {
            mContentLayout.offsetTopAndBottom(y);
        }
        mHeaderView.offsetTopAndBottom(y);
        mFooterView.offsetTopAndBottom(y);

        invalidate();
    }

    private boolean isTouchViewRefresh(View target, float x, float y) {
        boolean inView = inView(target, x, y);
        if (inView && mCalculator.getDirection() == TLRCalculator.Direction.DOWN) {
            return isViewRefresh(target);
        }
        return false;
    }

    private boolean isTouchViewLoad(View target, float x, float y) {
        boolean inView = inView(target, x, y);
        if (inView && mCalculator.getDirection() == TLRCalculator.Direction.UP) {
            return isViewLoad(target);
        }
        return false;
    }

    private boolean isViewRefresh(View target) {
        return !ViewCompat.canScrollVertically(target, -1);
    }

    private boolean isViewLoad(View target) {
        return !ViewCompat.canScrollVertically(target, 1);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(getContext());
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return generateDefaultLayoutParams();
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        int label = 0;

        public LayoutParams(Context c) {
            this(c, null);
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray array = c.obtainStyledAttributes(attrs, R.styleable.TLRLinearLayout_Layout);

            try {
                final int N = array.getIndexCount();
                for (int i = 0; i < N; i++) {
                    int index = array.getIndex(i);
                    if (index == R.styleable.TLRLinearLayout_Layout_label) {
                        label = array.getInt(R.styleable.TLRLinearLayout_Layout_label, 0);
                    }
                }
            } finally {
                array.recycle();
            }
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public int getLabel() {
            return label;
        }
    }

    private static class TLRUiHandlerWrapper implements TLRUiHandler {
        private final List<WeakReference<TLRUiHandler>> mTLRUiHandlers = new CopyOnWriteArrayList<>();

        @Override
        public void onRefreshStatusChanged(RefreshStatus status) {
            Log.d("onRefreshStatusChanged status:" + status);
            Iterator<WeakReference<TLRUiHandler>> iterator = mTLRUiHandlers.iterator();
            while (iterator.hasNext()) {
                WeakReference<TLRUiHandler> wefUiHandler = iterator.next();
                TLRUiHandler handler = wefUiHandler.get();
                if (handler != null) {
                    handler.onRefreshStatusChanged(status);
                } else {
                    iterator.remove();
                }
            }
        }

        @Override
        public void onLoadStatusChanged(LoadStatus status) {
            Log.i("onLoadStatusChanged status:" + status);
            Iterator<WeakReference<TLRUiHandler>> iterator = mTLRUiHandlers.iterator();
            while (iterator.hasNext()) {
                WeakReference<TLRUiHandler> wefUiHandler = iterator.next();
                TLRUiHandler handler = wefUiHandler.get();
                if (handler != null) {
                    handler.onLoadStatusChanged(status);
                } else {
                    iterator.remove();
                }
            }
        }

        @Override
        public void onOffsetChanged(int totalOffsetY, int totalThresholdY, int offsetY, float threshOffset) {
            //Log.v("onOffsetChanged totalOffsetY:" + totalOffsetY + " totalThresholdY:" + totalThresholdY + " offsetY:" + offsetY + " threshOffset:" + threshOffset);
            Iterator<WeakReference<TLRUiHandler>> iterator = mTLRUiHandlers.iterator();
            while (iterator.hasNext()) {
                WeakReference<TLRUiHandler> wefUiHandler = iterator.next();
                TLRUiHandler handler = wefUiHandler.get();
                if (handler != null) {
                    handler.onOffsetChanged(totalOffsetY, totalThresholdY, offsetY, threshOffset);
                } else {
                    iterator.remove();
                }
            }
        }
    }
}
