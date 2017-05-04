package com.think.uiloader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by borney on 4/28/17.
 */
public class TLRViewGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tlrviewgroup);
        findViewById(R.id.content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TLRViewGroupActivity.this, "click text content", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
