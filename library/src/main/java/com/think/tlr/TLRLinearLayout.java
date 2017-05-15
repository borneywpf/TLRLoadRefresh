package com.think.tlr;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author borney
 * @date 4/28/17
 * @see android.view.View
 * @see android.view.ViewGroup
 */
public class TLRLinearLayout extends ViewGroup {
    private static final boolean DEBUG = false;
    public static final int LABEL_HEAD = 1;
    public static final int LABEL_CONTENT = 2;
    public static final int LABEL_FOOT = 3;
    /**
     * 上拉加载功能是否可用
     */
    private boolean isEnableLoad = false;
    /**
     * 下拉刷新功能是否可用
     */
    private boolean isEnableRefresh = false;
    /**
     * 操作过程中是否保持contentLayout不移动
     */
    private boolean isKeepContentLayout = false;

    /**
     * TLR 是否可以移动head
     */
    private boolean canMoveHeadByTLR = true;

    /**
     * TLR 是否可以移动foot
     */
    private boolean canMoveFootByTLR = true;

    private View mHeaderView;
    /**
     * flag is {@link TLRLinearLayout#LABEL_CONTENT} view,
     * 只有标记为content的view才能触发刷新或加载操作
     */
    private List<View> mContentViews;
    /**
     * 需要向ContentLayout中添加的子view
     */
    private List<View> mContentChilds;
    private LinearLayout mContentLayout;
    private View mFooterView;
    private TLRCalculator mCalculator;
    private TLRUiHandlerWrapper mUiHandlerWrapper;
    private boolean isAddViewSelf = false;
    private View mTouchView;

    /**
     * 刷新状态
     */
    public enum RefreshStatus {
        IDLE, PULL_DOWN, RELEASE_REFRESH, REFRESHING
    }

    /**
     * 加载状态
     */
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
            TLRLog.e("initAttrs array is null");
            return;
        }
        try {
            final int N = array.getIndexCount();
            for (int i = 0; i < N; i++) {
                int index = array.getIndex(i);
                if (index == R.styleable.TLRLinearLayout_enableLoad) {
                    isEnableLoad = array.getBoolean(index, false);
                    TLRLog.i("isEnableLoad = " + isEnableLoad);
                } else if (index == R.styleable.TLRLinearLayout_enableRefresh) {
                    isEnableRefresh = array.getBoolean(index, false);
                    TLRLog.i("isEnableRefresh = " + isEnableRefresh);
                } else if (index == R.styleable.TLRLinearLayout_keepContentLayout) {
                    isKeepContentLayout = array.getBoolean(index, false);
                    TLRLog.i("isKeepContentLayout = " + isKeepContentLayout);
                } else if (index == R.styleable.TLRLinearLayout_canMoveHeadByTLR) {
                    canMoveHeadByTLR = array.getBoolean(index, canMoveHeadByTLR);
                    TLRLog.i("canMoveHeadByTLR = " + canMoveHeadByTLR);
                } else if (index == R.styleable.TLRLinearLayout_canMoveFootByTLR) {
                    canMoveFootByTLR = array.getBoolean(index, canMoveFootByTLR);
                    TLRLog.i("canMoveFootByTLR = " + canMoveFootByTLR);
                }
            }
        } finally {
            array.recycle();
        }
    }

    private void addSelfView(View child, int index, ViewGroup.LayoutParams params) {
        isAddViewSelf = true;
        addView(child, index, params);
        isAddViewSelf = false;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (index == 0 && !isAddViewSelf) {
            TLRLog.e("can't add view index 0!!!");
            return;
        }
        super.addView(child, index, params);

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
            TLRLog.d("child = " + child.getClass().getSimpleName() + " params.label = " + params.label);
            if (params.label == LABEL_HEAD) {
                if (i != 0) {
                    throw new RuntimeException("head must in first");
                }
                setHeaderView(true, child);
            } else if (params.label == LABEL_FOOT) {
                if (i != count - 1) {
                    throw new RuntimeException("foot must in last!!!");
                }
                setFooterView(true, child);
            } else if (params.label == LABEL_CONTENT) {
                mContentViews.add(child);
                mContentChilds.add(child);
            } else {
                mContentChilds.add(child);
            }
        }

        if (mHeaderView == null) {
            TLRLog.e("has not header view!!!");
            if (isEnableRefresh) {
                TLRLog.v("use default head view!!!");
                setHeaderView(false, new TLRDefHeadView(getContext()));
            }
        }

        if (mFooterView == null) {
            TLRLog.e("has not footer view!");
            if (isEnableLoad()) {
                TLRLog.v("use default foot view!!!");
                setFooterView(false, new TLRDefFootView(getContext()));
            }
        }

        if (mContentChilds.size() == 0) {
            throw new RuntimeException("must have content view !!!");
        }

        for (View view : mContentChilds) {
            removeView(view);
            ViewGroup.MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(params);
            mContentLayout.addView(view, llp);
        }
        addSelfView(mContentLayout, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        TLRLog.i("ContentLayout count:" + mContentLayout.getChildCount());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return dispatchTouchEvent(false, ev);
    }

    boolean dispatchTouchEvent(boolean isNested, MotionEvent ev) {
        if (isNested) {
            return super.dispatchTouchEvent(ev);
        }
        if (!isEnableLoad() && !isEnableRefresh()) {
            return super.dispatchTouchEvent(ev);
        }
        if (mCalculator.hasAnyAnimatorRunning()) {
            return super.dispatchTouchEvent(ev);
        }

        int action = ev.getAction();
        float x = ev.getX();
        float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!isRefreshing() && !isLoading()) {
                    setTouchView(x, y);
                }
                mCalculator.eventDown(x, y);
                super.dispatchTouchEvent(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                mCalculator.eventMove(x, y);
                if (!mCalculator.isBackStatus()) {
                    mCalculator.touchMoveLayoutView();
                } else if (mCalculator.canCalculatorV()) {
                    if (isTouchMoveRefresh(x, y) || isTouchMoveLoad(x, y)) {
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
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
                if (DEBUG) {
                    TLRLog.d(child.getClass().getSimpleName() + " mw:" + child.getMeasuredWidth() + " mh:" + child.getMeasuredHeight());
                }
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int totalOffsetY = mCalculator.getTotalOffsetY();

        int parentLeft = getPaddingLeft();
        int parentTop = getPaddingTop();
        int parentBottom = parentTop + getMeasuredHeight();

        if (mContentLayout != null && mContentLayout.getVisibility() != GONE) {
            LayoutParams lp = (LayoutParams) mContentLayout.getLayoutParams();
            int left = parentLeft + lp.leftMargin;
            int top = parentTop + lp.topMargin;
            if (!isKeepContentLayout) {
                top += totalOffsetY;
            }
            int right = left + mContentLayout.getMeasuredWidth();
            int bottom = top + mContentLayout.getMeasuredHeight();

            if (DEBUG) {
                TLRLog.i("ContentLayout left:" + left + " right:" + right + " top:" + top + " bottom:" + bottom);
            }

            mContentLayout.layout(left, top, right, bottom);
        }

        if (mHeaderView != null && mHeaderView.getVisibility() != GONE) {
            LayoutParams lp = (LayoutParams) mHeaderView.getLayoutParams();
            int left = parentLeft + lp.leftMargin;
            int bottom = parentTop - lp.bottomMargin;//layout head calculator bottom first
            if (isEnableRefresh && canMoveHeadByTLR) { //refresh is enabled
                bottom += totalOffsetY;
            }
            int right = left + mHeaderView.getMeasuredWidth();
            int top = bottom - mHeaderView.getMeasuredHeight();

            if (DEBUG) {
                TLRLog.i("HeaderView left:" + left + " right:" + right + " top:" + top + " bottom:" + bottom);
            }

            mHeaderView.layout(left, top, right, bottom);
        }

        if (mFooterView != null && mFooterView.getVisibility() != GONE) {
            LayoutParams lp = (LayoutParams) mFooterView.getLayoutParams();
            int left = parentLeft + lp.leftMargin;
            int top = parentBottom + lp.topMargin;//layout foot calculator top first
            if (isEnableLoad && canMoveFootByTLR) { //load is enabled
                top += totalOffsetY;
            }
            int right = left + mFooterView.getMeasuredWidth();
            int bottom = top + mFooterView.getMeasuredHeight();

            if (DEBUG) {
                TLRLog.i("FooterView left:" + left + " right:" + right + " top:" + top + " bottom:" + bottom);
            }

            mFooterView.layout(left, top, right, bottom);
        }
    }

    void move(int y) {
        if (isEnableRefresh() && isCanMoveHeadByTLR() && mHeaderView != null) {
            mHeaderView.offsetTopAndBottom(y);
        }
        if (isEnableLoad() && isCanMoveFootByTLR() && mFooterView != null) {
            mFooterView.offsetTopAndBottom(y);
        }
        if (!isKeepContentLayout()) {
            mContentLayout.offsetTopAndBottom(y);
        }
    }

    /**
     * call by child
     *
     * @return
     */
    TLRCalculator calculator() {
        return mCalculator;
    }

    private void setTouchView(float downx, float downy) {
        for (View view : mContentViews) {
            if (inView(view, downx, downy)) {
                setTouchView(view);
                break;
            }
        }
    }

    void setTouchView(View touchView) {
        if (mTouchView != touchView) {
            mTouchView = touchView;
        }
    }

    View getTouchView() {
        return mTouchView;
    }

    private boolean isTouchMoveRefresh(float x, float y) {
        boolean refresh = false;
        for (View view : mContentViews) {
            if (isTouchViewRefresh(view, x, y)) {
                refresh = true;
                break;
            }
        }
        refresh &= isEnableRefresh();
        if (DEBUG) {
            TLRLog.i("isTouchMoveRefresh refresh:" + refresh);
        }
        return refresh;
    }

    private boolean isTouchMoveLoad(float x, float y) {
        boolean load = false;
        for (View view : mContentViews) {
            if (isTouchViewLoad(view, x, y)) {
                load = true;
                break;
            }
        }
        load &= isEnableLoad();
        if (DEBUG) {
            TLRLog.i("isTouchMoveLoad load:" + load);
        }
        return load;
    }

    private boolean inView(View view, float x, float y) {
        final int scrollY = getScrollY();
        return !(y < view.getTop() - scrollY
                || y >= view.getBottom() - scrollY
                || x < view.getLeft()
                || x >= view.getRight());
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

    /**
     * start auto refresh
     */
    public void autoRefresh() {
        mCalculator.startAutoRefresh();
    }

    /**
     * finish refresh success or not
     *
     * @param isSuccess
     */
    public void finishRefresh(boolean isSuccess) {
        finishRefresh(isSuccess, -1);
    }

    /**
     * finish refresh success or not, cotain errorCode
     *
     * @param isSuccess
     */
    public void finishRefresh(boolean isSuccess, int errorCode) {
        mCalculator.finishRefresh(isSuccess, errorCode);
    }

    /**
     * finish load success or not
     *
     * @param isSuccess
     */
    public void finishLoad(boolean isSuccess) {
        finishLoad(isSuccess, -1);
    }

    /**
     * finish load success or not, cotain errorCode
     *
     * @param isSuccess
     */
    public void finishLoad(boolean isSuccess, int errorCode) {
        mCalculator.finishLoad(isSuccess, errorCode);
    }

    /**
     * add {@link TLRUIHandler} callback
     *
     * @param handler
     */
    public void addTLRUiHandler(TLRUIHandler handler) {
        mUiHandlerWrapper.addTLRUiHandler(handler);
    }

    /**
     * remove {@link TLRUIHandler} callback
     *
     * @param handler
     */
    public void removeTLRUiHandler(TLRUIHandler handler) {
        mUiHandlerWrapper.removeTLRUiHandler(handler);
    }

    /**
     * add hook when ui ready to reset
     *
     * @param hook
     */
    public void hook(TLRUIHandlerHook hook) {
        mCalculator.hook(hook);
    }

    /**
     * remove hook, must call if you set hook
     *
     * @param hook
     */
    public void releaseHook(TLRUIHandlerHook hook) {
        mCalculator.releaseHook(hook);
    }

    /**
     * return load is enable or not
     *
     * @return
     */
    public boolean isEnableLoad() {
        return isEnableLoad;
    }

    /**
     * set load is enable or not
     *
     * @param enableLoad
     */
    public void setEnableLoad(boolean enableLoad) {
        isEnableLoad = enableLoad;
    }

    /**
     * return refresh is enable or not
     *
     * @return
     */
    public boolean isEnableRefresh() {
        return isEnableRefresh;
    }

    /**
     * set refresh is enable or not
     *
     * @param enableRefresh
     */
    public void setEnableRefresh(boolean enableRefresh) {
        isEnableRefresh = enableRefresh;
    }

    /**
     * return contentLayout can not move on refresh or load
     *
     * @return
     */
    public boolean isKeepContentLayout() {
        return isKeepContentLayout;
    }

    /**
     * set contentLayout can not move on refresh or load
     *
     * @param keepContentLayout
     */
    public void setKeepContentLayout(boolean keepContentLayout) {
        isKeepContentLayout = keepContentLayout;
    }

    /**
     * get {@link TLRLinearLayout} can move head view
     */
    public boolean isCanMoveHeadByTLR() {
        return canMoveHeadByTLR;
    }

    /**
     * set {@link TLRLinearLayout} can move head view
     *
     * @param canMoveHeadByTLR
     */
    public void setCanMoveHeadByTLR(boolean canMoveHeadByTLR) {
        this.canMoveHeadByTLR = canMoveHeadByTLR;
    }

    /**
     * get {@link TLRLinearLayout} can move foot view
     */
    public boolean isCanMoveFootByTLR() {
        return canMoveFootByTLR;
    }

    /**
     * set {@link TLRLinearLayout} can move foot view
     *
     * @param canMoveFootByTLR
     */
    public void setCanMoveFootByTLR(boolean canMoveFootByTLR) {
        this.canMoveFootByTLR = canMoveFootByTLR;
    }

    /**
     * get head view
     *
     * @return
     */
    public View getHeaderView() {
        return mHeaderView;
    }

    /**
     * set head view
     *
     * @param headerView
     */
    public void setHeaderView(View headerView) {
        setHeaderView(false, headerView);
    }

    private void setHeaderView(boolean isAdded, View headerView) {
        if (headerView == null || mHeaderView == headerView) {
            return;
        }
        if (isAdded) {
            mHeaderView = headerView;
        } else {
            mHeaderView = headerView;
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.label = LABEL_HEAD;
            addView(mHeaderView, params);
        }
        TLRLog.d("setHeaderView:" + mHeaderView.getClass().getSimpleName());
        if (mHeaderView instanceof TLRUIHandler) {
            addTLRUiHandler((TLRUIHandler) mHeaderView);
        }
    }

    /**
     * set foot view
     *
     * @param footerView
     */
    public void setFooterView(View footerView) {
        setFooterView(false, footerView);
    }

    private void setFooterView(boolean isAdded, View footerView) {
        if (footerView == null || mFooterView == footerView) {
            return;
        }
        if (isAdded) {
            mFooterView = footerView;
        } else {
            mFooterView = footerView;
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.label = LABEL_FOOT;
            addView(mFooterView, params);
        }
        TLRLog.d("setFooterView:" + mFooterView.getClass().getSimpleName());
        if (mFooterView instanceof TLRUIHandler) {
            addTLRUiHandler((TLRUIHandler) mFooterView);
        }
    }

    /**
     * get foot view
     *
     * @return
     */
    public View getFooterView() {
        return mFooterView;
    }

    /**
     * get the refresh factor
     *
     * @return
     */
    public float getRefreshThreshold() {
        return mCalculator.getRefreshThreshold();
    }

    /**
     * Set the refresh factor
     */
    public void setRefreshThreshold(float refreshThreshold) {
        mCalculator.setRefreshThreshold(refreshThreshold);
    }

    /**
     * get the load factor
     *
     * @return
     */
    public float getLoadThreshold() {
        return mCalculator.getLoadThreshold();
    }

    /**
     * Set the load factor
     */
    public void setLoadThreshold(float loadThreshold) {
        mCalculator.setLoadThreshold(loadThreshold);
    }

    /**
     * get the maximum travel distance to refresh
     */
    public int getRefreshMaxMoveDistance() {
        return mCalculator.getRefreshMaxMoveDistance();
    }

    /**
     * Set the maximum travel distance to refresh
     *
     * @param refreshMaxMoveDistance
     */
    public void setRefreshMaxMoveDistance(int refreshMaxMoveDistance) {
        mCalculator.setRefreshMaxMoveDistance(refreshMaxMoveDistance);
    }

    /**
     * get the maximum travel distance to load
     */
    public int getLoadMaxMoveDistance() {
        return mCalculator.getLoadMaxMoveDistance();
    }

    /**
     * Set the maximum travel distance to load
     *
     * @param loadMaxMoveDistance
     */
    public void setLoadMaxMoveDistance(int loadMaxMoveDistance) {
        mCalculator.setLoadMaxMoveDistance(loadMaxMoveDistance);
    }

    /**
     * get the damping coefficient
     *
     * @return
     */
    public float getResistance() {
        return mCalculator.getResistance();
    }

    /**
     * set the damping coefficient
     */
    public void setResistance(float resistance) {
        mCalculator.setResistance(resistance);
    }

    /**
     * Set the reset the animation duration
     *
     * @param closeAnimDuration
     */
    public void setCloseAnimDuration(int closeAnimDuration) {
        mCalculator.setCloseAnimDuration(closeAnimDuration);
    }

    /**
     * Set the auto refresh to open the animation duration
     *
     * @param openAnimDuration
     */
    public void setOpenAnimDuration(int openAnimDuration) {
        mCalculator.setOpenAnimDuration(openAnimDuration);
    }

    /**
     * return keep head view when refreshing
     *
     * @return
     */
    public boolean isKeepHeadRefreshing() {
        return mCalculator.isKeepHeadRefreshing();
    }

    /**
     * When refresh, whether or not to stay head view
     *
     * @param keepHeadRefreshing
     */
    public void setKeepHeadRefreshing(boolean keepHeadRefreshing) {
        mCalculator.setKeepHeadRefreshing(keepHeadRefreshing);
    }

    /**
     * return keep foot view when loading
     *
     * @return
     */
    public boolean isKeepFootLoading() {
        return mCalculator.isKeepFootLoading();
    }

    /**
     * When loaded, whether to stay foot view
     *
     * @param keepFootLoading
     */
    public void setKeepFootLoading(boolean keepFootLoading) {
        mCalculator.setKeepFootLoading(keepFootLoading);
    }

    /**
     * Whether to release the refresh
     *
     * @return
     */
    public boolean isReleaseRefresh() {
        return mCalculator.isReleaseRefresh();
    }

    /**
     * set whether to release the refresh
     *
     * @param releaseRefresh
     */
    public void setReleaseRefresh(boolean releaseRefresh) {
        mCalculator.setReleaseRefresh(releaseRefresh);
    }

    /**
     * Whether to release the load
     *
     * @return
     */
    public boolean isReleaseLoad() {
        return mCalculator.isReleaseLoad();
    }

    /**
     * set whether to release the load
     *
     * @param releaseLoad
     */
    public void setReleaseLoad(boolean releaseLoad) {
        mCalculator.setReleaseLoad(releaseLoad);
    }

    /**
     * return has any animation is running
     *
     * @return
     */
    public boolean hasAnyAnimatorRunning() {
        return mCalculator.hasAnyAnimatorRunning();
    }

    /**
     * Returns whether it is refreshing
     *
     * @return
     */
    public boolean isRefreshing() {
        return mCalculator.isRefreshing();
    }

    /**
     * Returns whether it is loading
     *
     * @return
     */
    public boolean isLoading() {
        return mCalculator.isLoading();
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

    private static class TLRUiHandlerWrapper implements TLRUIHandler {
        private final List<TLRUIHandler> mTLRUiHandlers = new ArrayList<>();

        public void addTLRUiHandler(TLRUIHandler handler) {
            if (handler != null) {
                mTLRUiHandlers.add(handler);
            }
        }

        public void removeTLRUiHandler(TLRUIHandler handler) {
            if (handler != null) {
                for (TLRUIHandler uiHandler : mTLRUiHandlers) {
                    if (uiHandler.equals(handler)) {
                        mTLRUiHandlers.remove(uiHandler);
                        break;
                    }
                }
            }
        }

        @Override
        public void onRefreshStatusChanged(View target, RefreshStatus status) {
            if (DEBUG) {
                String name = target == null ? null : target.getClass().getSimpleName();
                TLRLog.d("onRefreshStatusChanged target:" + name + " status:" + status + " size:" + mTLRUiHandlers.size());
            }
            for (TLRUIHandler handler : mTLRUiHandlers) {
                handler.onRefreshStatusChanged(target, status);
            }
        }

        @Override
        public void onLoadStatusChanged(View target, LoadStatus status) {
            if (DEBUG) {
                String name = target == null ? null : target.getClass().getSimpleName();
                TLRLog.i("onLoadStatusChanged target:" + name + " status:" + status + " size:" + mTLRUiHandlers.size());
            }
            for (TLRUIHandler handler : mTLRUiHandlers) {
                handler.onLoadStatusChanged(target, status);
            }
        }

        @Override
        public void onOffsetChanged(View target, boolean isRefresh, int totalOffsetY, int totalThresholdY, int offsetY, float threshOffset) {
            if (DEBUG) {
                String name = target == null ? null : target.getClass().getSimpleName();
                TLRLog.v("onOffsetChanged target:" + name + " isRefresh:" + isRefresh + " totalOffsetY:" + totalOffsetY + " totalThresholdY:" + totalThresholdY + " offsetY:" + offsetY + " threshOffset:" + threshOffset);
            }
            for (TLRUIHandler handler : mTLRUiHandlers) {
                handler.onOffsetChanged(target, isRefresh, totalOffsetY, totalThresholdY, offsetY, threshOffset);
            }
        }

        @Override
        public void onFinish(View target, boolean isRefresh, boolean isSuccess, int errorCode) {
            if (DEBUG) {
                String name = target == null ? null : target.getClass().getSimpleName();
                TLRLog.i("onFinish target:" + name + " isRefresh:" + isRefresh + " isSuccess:" + isSuccess + " errorCode:" + errorCode);
            }
            for (TLRUIHandler handler : mTLRUiHandlers) {
                handler.onFinish(target, isRefresh, isSuccess, errorCode);
            }
        }
    }
}
