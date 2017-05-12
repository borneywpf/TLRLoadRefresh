package com.think.uiloader.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class RViewPagerActivity extends FragmentActivity {
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tlrviewpager);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mDemoCollectionPagerAdapter =
                new DemoCollectionPagerAdapter(
                        getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
    }

    public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {
        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new PagerFragment();
            return fragment;
        }

        @Override
        public int getCount() {
            return 100;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "PAGE " + (position + 1);
        }
    }

    public static class PagerFragment extends Fragment {
        private TLRLinearLayout mTLRLinearLayout;
        private ProgressBar mProgressBar;
        private WebView mWebView;
        private Handler mHandler = new Handler();


        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_pager_layout, container, false);
            mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
            mTLRLinearLayout = (TLRLinearLayout) rootView.findViewById(R.id.tlrlayout);
            mTLRLinearLayout.addTLRUiHandler(new TLRUIHandlerAdapter() {
                @Override
                public void onRefreshStatusChanged(View target, TLRLinearLayout.RefreshStatus status) {
                    if (status == TLRLinearLayout.RefreshStatus.REFRESHING) {
                        mWebView.loadUrl("https://github.com/borneywpf");
                    }
                }
            });
            mWebView = (WebView) rootView.findViewById(R.id.content);
            mWebView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    mTLRLinearLayout.finishRefresh(true);
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

            return rootView;
        }
    }
}

