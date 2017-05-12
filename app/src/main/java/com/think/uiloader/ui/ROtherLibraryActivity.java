package com.think.uiloader.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.think.tlr.TLRLinearLayout;
import com.think.tlr.TLRUIHandlerAdapter;
import com.think.uiloader.App;
import com.think.uiloader.R;
import com.think.uiloader.data.entity.ImageEntity;
import com.think.uiloader.ui.di.components.ActivityComponent;
import com.think.uiloader.ui.di.components.DaggerActivityComponent;
import com.think.uiloader.ui.mvp.contract.ImageContract;
import com.think.uiloader.ui.mvp.presenter.ImagePresenter;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by borney on 5/11/17.
 */
public class ROtherLibraryActivity extends AppCompatActivity implements ImageContract.View {
    private Banner mBanner;
    private ListView mListView;
    private TLRLinearLayout mTLRLinearLayout;
    private ListImageAdapter mAdapter;
    private List<ImageEntity.Image> mImageList = new ArrayList<>();
    private App mApp;
    private int curIndex = 0;
    private Handler mHandler = new Handler();

    @Inject
    ImagePresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (App) getApplication();
        initActivityComponent();
        setContentView(R.layout.activity_tlrotherlibrary);
        initTlr();
        initListView();
        initBanner();
    }

    private void initBanner() {
        mBanner = (Banner) findViewById(R.id.banner);
        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE);
        //设置图片加载器
        mBanner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        List<String> images = new ArrayList<String>() {
            {
                add("http://img4.imgtn.bdimg.com/it/u=1105037253,1131367531&fm=23&gp=0.jpg");
                add("http://img3.imgtn.bdimg.com/it/u=3633691784,3186862163&fm=23&gp=0.jpg");
                add("http://img0.imgtn.bdimg.com/it/u=464052833,4104593507&fm=23&gp=0.jpg");
            }
        };
        mBanner.setImages(images);
        //设置banner动画效果
        mBanner.setBannerAnimation(Transformer.DepthPage);
        //设置标题集合（当banner样式有显示title时）
        List<String> titles = new ArrayList<String>() {
            {
                add("banner1");
                add("banner2");
                add("banner3");
            }
        };
        mBanner.setBannerTitles(titles);
        //设置自动轮播，默认为true
        mBanner.isAutoPlay(true);
        //设置轮播时间
        mBanner.setDelayTime(1500);
        //设置指示器位置（当banner模式中有指示器时）
        mBanner.setIndicatorGravity(BannerConfig.CENTER);
        //banner设置方法全部调用完毕时最后调用
        mBanner.start();
    }

    private void initTlr() {
        mTLRLinearLayout = (TLRLinearLayout) findViewById(R.id.tlrlayout);
        mTLRLinearLayout.addTLRUiHandler(new TLRUIHandlerAdapter() {
            @Override
            public void onRefreshStatusChanged(View target, TLRLinearLayout.RefreshStatus status) {
                if (status == TLRLinearLayout.RefreshStatus.REFRESHING) {
                    mPresenter.images(curIndex, 10);
                }
            }
        });
    }

    private void initListView() {
        mListView = (ListView) findViewById(R.id.content);
        mAdapter = new ListImageAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ROtherLibraryActivity.this, "onclick " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initActivityComponent() {
        ActivityComponent component = DaggerActivityComponent.builder().applicationComponent(
                mApp.getApplicationComponent()).build();
        component.inject(this);
        mPresenter.setView(this);
    }

    @Override
    public void startImages() {

    }

    @Override
    public void imagesSuccess(final List<ImageEntity.Image> images) {
        if (images != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mImageList.addAll(0, images);
                    curIndex += images.size();
                    mAdapter.notifyImages(mImageList);
                    mTLRLinearLayout.finishRefresh(true);
                }
            }, 1500);
        }
    }

    @Override
    public void endImages() {

    }

    @Override
    public void error(int errorCode) {

    }
}
