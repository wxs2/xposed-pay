package com.zhiyi.ukafu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Administrator on 2018/8/18.
 */

public class BootRecevier extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Log.i("ZYKJ", "Notification posted ");
            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }else if(intent.getAction().equals(AppConst.IntentAction)){
            Log.i("ZYKJ","receiver no"+intent.getData().toString());
        }
    }
}
