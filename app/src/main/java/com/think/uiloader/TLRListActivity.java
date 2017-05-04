package com.think.uiloader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.think.tlr.TLRLinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by borney on 4/28/17.
 */
public class TLRListActivity extends AppCompatActivity {
    private ListView mListView;
    private TLRLinearLayout mTLRLinearLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tlrlistview);
        mListView = (ListView) findViewById(R.id.content);
        mTLRLinearLayout = (TLRLinearLayout) findViewById(R.id.tlrlayout);
        mListView.setAdapter(new MyListAdapter(initList()));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(TLRListActivity.this, "onclick " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTLRLinearLayout.startAutoRefresh();
    }

    private List<String> initList() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("Item-" + i);
        }
        return list;
    }

    private class MyListAdapter extends BaseAdapter {
        private List<String> mList;

        public MyListAdapter(List<String> list) {
            mList = list;
        }

        @Override
        public int getCount() {
            return mList != null ? mList.size() : 0;
        }

        @Override
        public String getItem(int position) {
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
                convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(getItem(position));
            return convertView;
        }
    }
}
