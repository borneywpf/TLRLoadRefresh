package com.think.uiloader.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.think.uiloader.R;

import java.util.ArrayList;
import java.util.List;

public class NestedActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nested);
        mRecyclerView = getView(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new MyAdapter(initList()));
    }

    private List<String> initList() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list.add("Item-" + (i + 1));
        }
        return list;
    }

    private <T> T getView(int id) {
        return (T) findViewById(id);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyHolder> {
        private List<String> list;
        public MyAdapter(List<String> list) {
            this.list = list;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
        }

        @Override
        public void onBindViewHolder(MyHolder holder, final int position) {
            final String text = list.get(position);
            holder.textView.setText(text);
            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("LoaderView", "onClick item pos:" + position + " text = " + text);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private class MyHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public MyHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(android.R.id.text1);
        }
    }
}
