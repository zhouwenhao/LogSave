package com.timark.demo;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.timark.logsave.ExtractCall;
import com.timark.logsave.LogSaveManager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogSaveManager.getInstance().init(getApplicationContext());

        findViewById(R.id.record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 10000; ++i){
                    LogSaveManager.getInstance().record(Log.DEBUG, "ceshiTag", "ceshiMsg=" + i);
                }
            }
        });

        findViewById(R.id.zip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogSaveManager.getInstance().upload(new ExtractCall() {
                    @Override
                    public void zip(int type, boolean isEnd, String filePath) {
                        Log.d("ceshi", "type=" + type + "&&&isEnd=" + isEnd + "&&&filePath=" + filePath);
                    }
                });
            }
        });

    }
}
