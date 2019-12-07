/**
 * 个人收款 https://gitee.com/DaLianZhiYiKeJi/xpay
 * 大连致一科技有限公司
 */


package com.zhiyi.ukafu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.zhiyi.ukafu.components.BatteryReceiver;
import com.zhiyi.ukafu.consts.ActionName;
import com.zhiyi.ukafu.data.MapOrderData;
import com.zhiyi.ukafu.data.OrderData;
import com.zhiyi.ukafu.data.OrderDataBase;
import com.zhiyi.ukafu.sms.SmsService;
import com.zhiyi.ukafu.util.AppUtil;
import com.zhiyi.ukafu.util.DBManager;
import com.zhiyi.ukafu.util.RequestData;
import com.zhiyi.ukafu.util.RequestUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


/**
 * 后台进程.确保进入后台也在运行
 */
public class MainService extends Service implements Runnable, MediaPlayer.OnCompletionListener {
    private Handler handler;
    private IMessageHander msgHander;
    private MediaPlayer payComp;
    private MediaPlayer payNetWorkError;
    private MediaPlayer payRecv;
    private ArrayList<OrderDataBase> sendingList;
    private DBManager dbManager;
    private PowerManager.WakeLock wakeLock;
    private BroadcastReceiver aliReceiver;
    //    public static final  String CHANNEL_ID          = "zhi_yi_px_pay";
    private NotificationChannel mNotificationChannel;
    private SmsService smsService;

    @Override
    public void onCreate() {
        super.onCreate();
        AppConst.version = AppUtil.getVersionCode(this);
        sendingList = new ArrayList<>();
        Log.i("ZYKJ", "mainactivity");
        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msgHander != null) {
                    msgHander.handMessage(msg);
                }
            }
        };
        payRecv = MediaPlayer.create(this, R.raw.payrecv);
        payComp = MediaPlayer.create(this, R.raw.paycomp);
        payNetWorkError = MediaPlayer.create(this, R.raw.networkerror);
        dbManager = new DBManager(this);
        AppConst.InitParams(dbManager);
        aliReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receiveBroadcast(context, intent);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ActionName.ONORDER_REC);
        filter.addAction(ActionName.ONORDER_NOTIFY);
        filter.addAction(ActionName.StartSMS);
        registerReceiver(aliReceiver, filter);

        new Thread(this, "MainService").start();
        //保持黑屏状态执行
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MainService.class.getName());
        if (wakeLock != null) {
            wakeLock.acquire();
        } else {
            Log.w(AppConst.TAG_LOG, "wakeLock is null");
        }
        //声音播放也不成功
        payComp = MediaPlayer.create(this, R.raw.paycomp);
        payComp.setOnCompletionListener(this);
        //
        Log.i("ZYKJ", "MainService Start");
        NotificationManager mNM = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationChannel = new NotificationChannel(AppConst.CHANNEL_Front, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationChannel.setDescription(getString(R.string.app_desc));
            mNM.createNotificationChannel(mNotificationChannel);
        }
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, AppConst.CHANNEL_Front);//
        nb.setContentTitle(getString(R.string.app_desc)).setTicker(getString(R.string.app_desc2)).setSmallIcon(R.mipmap.ic_launcher);
        nb.setWhen(System.currentTimeMillis());
        Notification notification = nb.build();
        startForeground(1, notification);


        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        BatteryReceiver batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, intentFilter);

        smsService = new SmsService();
        Log.i("ZYKJ", "MainService Started");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
//        if(AppConst.ManualExit) return START_NOT_STICKY;
        return START_STICKY;
    }

    public void setMessageHander(IMessageHander hander) {
        msgHander = hander;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    public void receiveBroadcast(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case ActionName.StartSMS:
                smsService.registerSMSObserver(this);
                break;
            case ActionName.StopSMS:
                smsService.registerSMSObserver(this);
                break;
            case ActionName.ONORDER_REC:
                postMethod(intent, false);
                break;
            case ActionName.ONORDER_NOTIFY:
                postMethod(intent, true);
                break;
        }
    }

    class MyBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    @Override
    public void run() {
//       while(true) {
//           try {
//               Thread.sleep(30000);
//           } catch (InterruptedException e) {
//               Log.e("ZYKJ", "service thread", e);
//           }
//            Message msg = new Message();
//            msg.what = 1;
//            msg.obj = "time";
//            handler.sendMessage(msg);
        //发送在线通知,保持让系统时时刻刻直到app在线
//            RequestUtils.getRequest(AppConst.authUrl("person/active/app?version="+getApplicationContext().getApplicationInfo()),handler);
//       }

        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Log.e(AppConst.TAG_LOG, "service thread", e);
            }
            if (!sendingList.isEmpty()) {
                OrderDataBase data;
                synchronized (sendingList) {
                    data = sendingList.remove(0);
                }
                postMethod(data);
            }
            long now = System.currentTimeMillis();
            do {
                //10秒内有交互,取消
                if (now - lastResponseTime < 20000) {
                    break;
                }
                //发送在线通知,保持让系统时时刻刻直到app在线,5秒发送一次
                if (now - lastSendTime < 10000) {
                    Log.d(AppConst.TAG_LOG, "10秒内有交互");
                    break;
                }
                postState();
                //30秒,没消息了.提示网络异常
                if (now - lastResponseTime > 30000) {
                    playMedia(payNetWorkError);
                }
            } while (false);
        }

    }

    private void postMethod(final OrderDataBase data) {
        if (data == null) {
            return;
        }
        IHttpResponse response = new IHttpResponse() {
            @Override
            public void OnHttpData(String data) {
                dbManager.addLog(data, 200);
                handleMessage(data, 1);
            }

            @Override
            public void OnHttpDataError(IOException e) {
                dbManager.addLog("http error," + e.getMessage(), 500);
                sendingList.add(data);
            }
        };
        if (data.isPost()) {
            RequestUtils.post(data.getApiUrl(), data.getOrderData(), response);
        } else {
            RequestUtils.getRequest(data.getApiUrl(), response);
        }
    }


    /**
     * 获取道的支付通知发送到服务器
     */
    private void postMethod(Intent intent, boolean map) {
        OrderDataBase data;
        if (map) {
            data = new MapOrderData(intent);
        } else {
            data = new OrderData(intent);
        }
        dbManager.addLog(data.getLogString(), 101);
        playMedia(payRecv);
        postMethod(data);
    }


    public boolean handleMessage(String message, int arg1) {
        lastResponseTime = System.currentTimeMillis();
        if (message == null || message.isEmpty()) {
            return true;
        }
        String msg = message;
        Log.i(AppConst.TAG_LOG, msg);
        if (arg1 == 3) {
            return true;
        }
        playMedia(payComp);
        return true;
    }

    private long lastSendTime;
    private long lastResponseTime;

    /**
     * 发送错误信息到服务器
     */
    public void postState() {
        lastSendTime = System.currentTimeMillis();
        Log.d(AppConst.TAG_LOG, "发送在线信息");
        //取消发送在线消息
        RequestData post = RequestData.newInstance(AppConst.NetTypeOnline);
        RequestUtils.post(AppConst.NoticeUrl, post, new HttpJsonResponse() {
            @Override
            protected void onJsonResponse(JSONObject json) {
                lastResponseTime = System.currentTimeMillis();
                try {
                    if (json.has("time")) {
                        int time = json.getInt("time");
                        int dt = (int) (System.currentTimeMillis() / 1000) - time;
                        AppConst.DetaTime = (dt + AppConst.DetaTime * 9) / 10;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
//        lastResponseTime = System.currentTimeMillis();
    }


    /**
     * 支付通知发送成功的时候.播报声音.
     * 在听到 微信到账1元之后.听到支付已完成,就知道系统没毛病.如果不能同时听到2声音.表示又问题了..
     * 但是这个播放声音我的手机老是出毛病.重启后就好了..
     */
    public void payCompSounds() {
        if (AppConst.PlaySounds) {
            payComp.start();
        }
    }


    /**
     * 据说这样能提高存活率,貌似也不太稳定
     */
    @Override
    public void onDestroy() {
//        if(AppConst.ManualExit) return;
        if (payComp != null) {
            payComp.release();
            payComp = null;
        }
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        Intent localIntent = new Intent();
        localIntent.setClass(this, MainService.class);
        startService(localIntent);
    }


    private void playMedia(MediaPlayer media) {
        if (AppConst.PlaySounds) {
            media.start();
        }
    }
}

