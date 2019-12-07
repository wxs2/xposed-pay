/**
 * 个人收款 https://gitee.com/DaLianZhiYiKeJi/xpay
 * 大连致一科技有限公司
 */

package com.zhiyi.ukafu;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zhiyi.ukafu.consts.ActionName;
import com.zhiyi.ukafu.util.AppUtil;
import com.zhiyi.ukafu.util.DBManager;
import com.zhiyi.ukafu.util.LogUtil;
import com.zhiyi.ukafu.util.RequestUtils;
import com.zhiyi.ukafu.worker.NotifyFilter;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationMonitorService extends NotificationListenerService {
    private static final String AliPay = "ALIPAY";
    private static final String WeixinPay = "WXPAY";
    private static final String JxYmf_Pay = "JXYMF";
    private static final String JxYmf_PKG = "com.buybal.buybalpay.nxy.jxymf";

//    public long lastTimePosted = System.currentTimeMillis();
    private Pattern pJxYmf_Nofity;
    private Pattern pAlipay;
    private Pattern pAlipay2;
    private Pattern pAlipayDianyuan;
    private Pattern pWeixin;
    private MediaPlayer payRecv;
    private DBManager dbManager;
    private PowerManager.WakeLock wakeLock;

    private NotifyFilter filter;


    public void onCreate() {
        super.onCreate();
        initFilter();
        Log.i(AppConst.TAG_LOG, "Notification posted ");
        Toast.makeText(getApplicationContext(), "启动服务", Toast.LENGTH_LONG).show();
        //支付宝
        String pattern = "(\\S*)通过扫码向你付款([\\d\\.]+)元";
        pAlipay = Pattern.compile(pattern);
        pattern = "成功收款([\\d\\.]+)元。享免费提现等更多专属服务，点击查看";
        pAlipay2 = Pattern.compile(pattern);
        pAlipayDianyuan = Pattern.compile("支付宝成功收款([\\d\\.]+)元。收钱码收钱提现免费，赶紧推荐顾客使用");
        pWeixin = Pattern.compile("微信支付收款([\\d\\.]+)元");
        pJxYmf_Nofity = Pattern.compile("一笔收款交易已完成，金额([\\d\\.]+)元");
        payRecv = MediaPlayer.create(this, R.raw.payrecv);
        dbManager = new DBManager(this);
        if (AppConst.AppId < 1) {
            String appid = dbManager.getConfig(AppConst.KeyAppId);
            if (!TextUtils.isEmpty(appid)) {
                AppConst.AppId = Integer.parseInt(appid);
                String token = dbManager.getConfig(AppConst.KeyToken);
                if (!TextUtils.isEmpty(token)) {
                    AppConst.Token = token;
                }
                String secret = dbManager.getConfig(AppConst.KeySecret);
                if (!TextUtils.isEmpty(secret)) {
                    AppConst.Secret = secret;
                }
            }
            // 推送前判断下playSounds
            String mute = dbManager.getConfig(AppConst.KeyMute);
            if(!TextUtils.isEmpty(mute)){
                AppConst.PlaySounds = Boolean.parseBoolean(mute);
            }
        }
        Log.i(AppConst.TAG_LOG, "Notification Monitor Service start");
        NotificationManager mNM = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mNM != null) {
            NotificationChannel mNotificationChannel = mNM.getNotificationChannel(AppConst.CHANNEL_ID);
            if (mNotificationChannel == null) {
                mNotificationChannel = new NotificationChannel(AppConst.CHANNEL_ID, "Ukafu", NotificationManager.IMPORTANCE_DEFAULT);
                mNotificationChannel.setDescription("监控专家");
                mNM.createNotificationChannel(mNotificationChannel);
            }
        }
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, AppConst.CHANNEL_ID);//

        nb.setContentTitle("Ukafu监控专家").setTicker("Ukafu监控专家").setSmallIcon(R.drawable.icon);
        nb.setContentText("监控专家运行中.请保持此通知一直存在");
        //nb.setContent(new RemoteViews(getPackageName(),R.layout.layout));
        nb.setWhen(System.currentTimeMillis());
        Notification notification = nb.build();
        startForeground(1, notification);


        Log.i(AppConst.TAG_LOG, "Notification Monitor Service started");
    }


    public void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        Intent localIntent = new Intent();
        localIntent.setClass(this, NotificationMonitorService.class);
        startService(localIntent);
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        Bundle bundle = sbn.getNotification().extras;
        String pkgName = sbn.getPackageName();
        if (getPackageName().equals(pkgName)) {
            //测试成功
            Log.i(AppConst.TAG_LOG, "测试成功");
            Intent intent = new Intent();
            intent.setAction(AppConst.IntentAction);
            Uri uri = new Uri.Builder().scheme("app").path("log").query("msg=测试成功").build();
            intent.setData(uri);
            sendBroadcast(intent);
            //payRecv.start();
            playMedia(payRecv);
            return;
        }
        String title = bundle.getString("android.title");
        String text = bundle.getString("android.text");
        LogUtil.i("Notification posted [" + pkgName + "]:" + title + " & " + text);
        if(text == null){
            //没有消息.垃圾
            return;
        }
        HashMap<String,String> fdata = filter.test(bundle,pkgName);
        if(fdata!=null){
            postMethod(fdata);
            return;
        }
//        this.lastTimePosted = System.currentTimeMillis();
        //支付宝com.eg.android.AlipayGphone
        //com.eg.android.AlipayGphone]:支付宝通知 & 新哥通过扫码向你付款0.01元
        if (pkgName.equals("com.eg.android.AlipayGphone")) {
            // 现在创建 matcher 对象
            do {
                Matcher m = pAlipay.matcher(text);
                if (m.find()) {
                    String uname = m.group(1);
                    String money = m.group(2);
                    postMethod(AliPay, money, uname, false);
                    break;
                }
                m = pAlipay2.matcher(text);
                if (m.find()) {
                    String money = m.group(1);
                    postMethod(AliPay, money, "支付宝用户", false);
                    break;
                }
                m = pAlipayDianyuan.matcher(text);
                if (m.find()) {
                    String money = m.group(1);
                    postMethod(AliPay, money, "支付宝-店员", true);
                    break;
                }
                Log.w(AppConst.TAG_LOG, "匹配失败" + text);
            } while (false);
        }
        //微信
        //com.tencent.mm]:微信支付 & 微信支付收款0.01元
        else if (pkgName.equals("com.tencent.mm")) {
            // 现在创建 matcher 对象
            Matcher m = pWeixin.matcher(text);
            if (m.find()) {
                String uname = "微信用户";
                String money = m.group(1);
                postMethod(WeixinPay, money, uname, false);
            }
        }else if(pkgName.equals(JxYmf_PKG)){
            Matcher m = pJxYmf_Nofity.matcher(text);
            if(m.find()){
                String uname = "一码付";
                String money = m.group(1);
                postMethod(JxYmf_Pay, money, uname, false);
            }
        }
    }



    public void onNotificationRemoved(StatusBarNotification paramStatusBarNotification) {
        if (Build.VERSION.SDK_INT >= 19) {
            Bundle localObject = paramStatusBarNotification.getNotification().extras;
            String pkgName = paramStatusBarNotification.getPackageName();
            String title = localObject.getString("android.title");
            String text = (localObject).getString("android.text");
            Log.i(AppConst.TAG_LOG, "Notification removed [" + pkgName + "]:" + title + " & " + text);
        }
    }

    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        return START_NOT_STICKY;
    }

    private void postMethod(final String payType, final String money, final String username, boolean dianYuan) {
        sendBroadcast(AppUtil.sendOrder(payType,money,username,dianYuan));
    }
    private void postMethod(HashMap<String,String> data) {
        if(data.isEmpty()){
            return;
        }
        Intent intent = new Intent(ActionName.ONORDER_NOTIFY);
        intent.putExtra("data",data);
        sendBroadcast(intent);
    }

    private void playMedia(MediaPlayer media) {
        if (AppConst.PlaySounds) {
            media.start();
        }
    }

    public void initFilter() {
        filter = new NotifyFilter();
        RequestUtils.post("https://www.ukafu.com/api.php/portal/filter/nfilter", "appid="+AppConst.AppId, new IHttpResponse() {
            @Override
            public void OnHttpData(String data) {
                try {
                    filter.initData(data);
                } catch (JSONException e) {
                    Log.e(AppConst.TAG_LOG,"filter init error",e);
                }
            }

            @Override
            public void OnHttpDataError(IOException e) {

            }
        });
    }

}