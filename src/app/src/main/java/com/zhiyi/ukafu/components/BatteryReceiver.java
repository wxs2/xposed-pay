package com.zhiyi.ukafu.components;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zhiyi.ukafu.AppConst;


public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            //获取当前电量
            int level = intent.getIntExtra("level", 0);
            //电量的总刻度
            int scale = intent.getIntExtra("scale", 100);
            //把它转成百分比
            int rate = (level * 100) / scale;
            AppConst.Battery = rate;
        }
    }
}
