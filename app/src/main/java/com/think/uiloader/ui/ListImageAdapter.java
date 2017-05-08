package com.think.uiloader.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.think.uiloader.R;
import com.think.uiloader.data.entity.ImageEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by borney on 5/8/17.
 */
public class ListImageAdapter extends BaseAdapter {
    private final List<ImageEntity.Image> mList = new ArrayList<>();

    public ListImageAdapter() {
    }

    public void notifyImages(List<ImageEntity.Image> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public ImageEntity.Image getItem(int position) {
        if (mList == null) {
            return null;
        }
        if (position < 0 || position >= getCount()) {
            return null;
        }
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        }
        ImageEntity.Image item = getItem(position);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
        Glide.with(parent.getContext()).load(item.getThumbnailUrl()).into(imageView);
        TextView textView = (TextView) convertView.findViewById(R.id.text);
        textView.setText(item.getDesc());
        return convertView;
    }
}