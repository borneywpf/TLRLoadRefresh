package com.think.uiloader.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.think.tlr.TLRLinearLayout;
import com.think.tlr.TLRUiHandlerAdapter;
import com.think.uiloader.R;

/**
 * Created by borney on 4/28/17.
 */
public class ViewGroupActivity extends AppCompatActivity {
    private TLRLinearLayout mTLRLinearLayout;
    private TextView mTextView;
    private int refreshCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tlrviewgroup);
        mTLRLinearLayout = (TLRLinearLayout) findViewById(R.id.tlrlayout);
        mTLRLinearLayout.addTLRUiHandler(new TLRUiHandlerAdapter() {
            @Override
            public void onRefreshStatusChanged(TLRLinearLayout.RefreshStatus status) {
                if (status == TLRLinearLayout.RefreshStatus.REFRESHING) {
                    refreshCount += 1;
                    mTextView.setText("刷新了 " + refreshCount + " 次.");
                }
            }
        });
        mTextView = (TextView) findViewById(R.id.text);
    }
}
