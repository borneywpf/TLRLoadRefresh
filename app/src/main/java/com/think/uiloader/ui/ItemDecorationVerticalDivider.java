package com.think.uiloader.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by borney on 8/16/16.
 */
public class ItemDecorationVerticalDivider extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private final int[] ATTRS = new int[]{
        android.R.attr.listDivider
    };

    public ItemDecorationVerticalDivider(Context context) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //super.getItemOffsets(outRect, view, parent, state);

//            if (parent.getChildAdapterPosition(view) == 0) {
//                return;
//            }

        outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int dividerLeft = 0;
        int dividerRight = parent.getWidth();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(dividerLeft, top, dividerRight, bottom);
            mDivider.draw(c);
        }
    }
}
