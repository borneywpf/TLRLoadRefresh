package com.think.uiloader.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.think.tlr.TLRLinearLayout;
import com.think.tlr.TLRUIHandlerAdapter;
import com.think.uiloader.App;
import com.think.uiloader.R;
import com.think.uiloader.data.entity.ImageEntity;
import com.think.uiloader.ui.di.components.ActivityComponent;
import com.think.uiloader.ui.di.components.DaggerActivityComponent;
import com.think.uiloader.ui.mvp.contract.ImageContract;
import com.think.uiloader.ui.mvp.presenter.ImagePresenter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by borney on 5/11/17.
 */
public class RGridViewActivity extends AppCompatActivity implements ImageContract.View {
    private TLRLinearLayout mTLRLinearLayout;
    private GridView mGridView;
    private Handler mHandler = new Handler();
    private List<ImageEntity.Image> mImageList = new ArrayList<>();
    private MyAdapter mAdapter;
    private App mApp;
    private int curIndex = 0;

    @Inject
    ImagePresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (App) getApplication();
        initActivityComponent();
        setContentView(R.layout.activity_tlrgridview);
        mTLRLinearLayout = (TLRLinearLayout) findViewById(R.id.tlrlayout);
        mTLRLinearLayout.addTLRUiHandler(new TLRUIHandlerAdapter() {
            @Override
            public void onRefreshStatusChanged(View target, TLRLinearLayout.RefreshStatus status) {
                if (status == TLRLinearLayout.RefreshStatus.REFRESHING) {
                    mPresenter.images(curIndex, 10);
                }
            }
        });
        mGridView = (GridView) findViewById(R.id.content);
        mAdapter = new MyAdapter();
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(RGridViewActivity.this, "onclick " + position, Toast.LENGTH_SHORT).show();
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

    private class MyAdapter extends BaseAdapter {
        private final List<ImageEntity.Image> mList = new ArrayList<>();

        public void notifyImages(List<ImageEntity.Image> list) {
            mList.clear();
            mList.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public ImageEntity.Image getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
            }
            ImageView imageView = (ImageView) view;
            Glide.with(parent.getContext()).load(getItem(position).getThumbnailUrl()).into(imageView);
            return imageView;
        }
    }
}
