package com.think.uiloader.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.think.tlr.TLRLinearLayout;
import com.think.tlr.TLRUIHandlerAdapter;
import com.think.uiloader.R;

/**
 * Created by borney on 5/11/17.
 */
public class RScrollViewActivity extends AppCompatActivity {
    private TLRLinearLayout mTLRLinearLayout;
    private TextView mTextView;
    private int refreshCount = 0;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tlrscrollview);
        mTLRLinearLayout = (TLRLinearLayout) findViewById(R.id.tlrlayout);
        mTLRLinearLayout.addTLRUiHandler(new TLRUIHandlerAdapter() {
            @Override
            public void onRefreshStatusChanged(View target, TLRLinearLayout.RefreshStatus status) {
                if (status == TLRLinearLayout.RefreshStatus.REFRESHING) {
                    refreshCount += 1;
                    mTLRLinearLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mTextView.setText("ScrollView中刷新了 " + refreshCount + " 次.");
                            mTLRLinearLayout.finishRefresh(true);
                        }
                    }, 1500);
                }
            }
        });
        mTextView = (TextView) findViewById(R.id.text);
    }
}
