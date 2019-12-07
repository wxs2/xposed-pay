package com.zhiyi.ukafu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.zhiyi.ukafu.util.LogUtil;

public class RunTimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_time);
        TextView textView = findViewById(R.id.text_log);
        textView.setText(LogUtil.getLog());
    }
}
