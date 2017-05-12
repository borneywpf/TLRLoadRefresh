package com.think.uiloader.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.think.tlr.TLRLinearLayout;
import com.think.tlr.TLRUIHandlerAdapter;
import com.think.uiloader.R;

/**
 * Created by borney on 5/11/17.
 */
public class RWebViewActivity extends AppCompatActivity {
    private TLRLinearLayout mTLRLinearLayout;
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private int refreshCount = 0;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tlrwebview);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mTLRLinearLayout = (TLRLinearLayout) findViewById(R.id.tlrlayout);
        mTLRLinearLayout.addTLRUiHandler(new TLRUIHandlerAdapter() {
            @Override
            public void onRefreshStatusChanged(View target, TLRLinearLayout.RefreshStatus status) {
                if (status == TLRLinearLayout.RefreshStatus.REFRESHING) {
                    if (refreshCount % 2 == 0) {
                        mWebView.loadUrl("https://developers.google.cn/");
                    } else {
                        mWebView.loadUrl("https://developer.android.google.cn/index.html");
                    }
                    refreshCount++;
                }
            }
        });
        mWebView = (WebView) findViewById(R.id.content);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mTLRLinearLayout.finishRefresh(true);
                    }
                }, 1500);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mTLRLinearLayout.finishRefresh(false);
                    }
                }, 1500);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 0 || newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                mProgressBar.setProgress(newProgress);
            }
        });
    }
}
