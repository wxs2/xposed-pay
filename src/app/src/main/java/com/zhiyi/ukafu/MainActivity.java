/**
 * 个人收款 https://gitee.com/DaLianZhiYiKeJi/xpay
 * 大连致一科技有限公司
 */

package com.zhiyi.ukafu;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.zhiyi.ukafu.activitys.SettingActivity;
import com.zhiyi.ukafu.activitys.WebViewActivity;
import com.zhiyi.ukafu.consts.ActionName;
import com.zhiyi.ukafu.data.H5AppData;
import com.zhiyi.ukafu.util.DBManager;
import com.zhiyi.ukafu.util.RequestData;
import com.zhiyi.ukafu.util.RequestUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final static String TRUE = "true";
    private final static String FALSE = "false";
    private final static String LogTag = "ZYKJ";

    private Switch swt_fuwu;
    private Switch swt_service;
    private Switch swt_smsservice;
    private Switch swt_mute;
//    private Button btn_qrcode;
    private TextView textView;
    private DBManager dbm;

    private Handler handler;


//    private NotificationChannel mNotificationChannel;

    private MainService service;
    private IMessageHander msgHander = new IMessageHander() {
        @Override
        public void handMessage(Message msg) {
            Log.i(LogTag, msg.obj.toString());
        }
    };
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            MainService.MyBinder myBinder = (MainService.MyBinder) binder;
            service = myBinder.getService();
            service.setMessageHander(msgHander);
            Log.i(LogTag, "MainActive - onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(LogTag, "MainActive - onServiceDisconnected");
        }
    };

    //不知到什么原因.广播收不到
    private BroadcastReceiver receiver = new BootRecevier();


    public MainActivity() {
    }

    private void mHandMessage(Message msg) {
        if (msg.what == 1) {
            String code = msg.obj.toString();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("获取绑定码");
            builder.setMessage("您的绑定码为: " + code + " ,请通过商户后台添加绑定,在绑定成功之前.请勿关闭");
            builder.setIcon(R.drawable.icon);
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    RequestData post = RequestData.newInstance(AppConst.NetTypeUnBindCode);
                    RequestUtils.post(AppConst.NoticeUrl, post, new IHttpResponse() {

                        @Override
                        public void OnHttpData(String data) {

                        }

                        @Override
                        public void OnHttpDataError(IOException e) {

                        }
                    });
                }
            }).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ZYKJ", "mainactivity");
        setContentView(R.layout.activity_main);
        swt_fuwu = (Switch) findViewById(R.id.p1);
        swt_service = (Switch) findViewById(R.id.service);
        swt_smsservice = (Switch) findViewById(R.id.smsservice);
        swt_mute = (Switch) findViewById(R.id.mute);

//        btn_qrcode = (Button) findViewById(R.id.btn_qrcode);

        swt_service.setChecked(false);
        swt_smsservice.setChecked(false);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                mHandMessage(msg);
            }
        };

        textView = (TextView) findViewById(R.id.textView_Help);
        dbm = new DBManager(this);
        //
        swt_mute.setChecked(!AppConst.PlaySounds);
        //
        TextView textv = (TextView) findViewById(R.id.textView_version);
        if(BuildConfig.BUILD_TYPE == "VIP"){
            View v = findViewById(R.id.textView_Help);
            ViewGroup vg = (ViewGroup)v.getParent();
            vg.removeView(v);
        }
        try {
            AppConst.version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            // 显示版本号
            textv.setText(textv.getText() + "" + AppConst.version);
            //
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        IntentFilter filter = new IntentFilter(AppConst.IntentAction);
        registerReceiver(receiver, filter);

        swt_fuwu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked != enabedPrivileges) {
                    openNotificationListenSettings();
                }
            }
        });

        swt_service.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkStatus();
                }
            }
        });

        swt_smsservice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean isChecked = swt_smsservice.isEnabled();
                if (isChecked) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, 1);
                        return true;
                    } else {
                        sendBroadcast(new Intent(ActionName.StartSMS));
                    }
                } else {
                    sendBroadcast(new Intent(ActionName.StopSMS));
                }
                return false;
            }
        });

//        btn_qrcode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                openQrcode();
//            }
//        });
//        btn_web.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
//                intent.putExtra(AppConst.ACTION_URL,AppConst.WebUrl);
//                startActivity(intent);
////                Intent intent = new Intent(MainActivity.this, LogActivity.class);
////                startActivity(intent);
//            }
//        });

        LinearLayout btn_container = findViewById(R.id.btn_container);
        for (int i = 0; i < AppConst.h5Apps.size(); i++) {
            H5AppData appData = AppConst.h5Apps.get(i);
            Button btn = getLayoutInflater().inflate(R.layout.btn_layout,btn_container).findViewById(R.id.btn_default);
            btn.setText(appData.name);
            btn.setTag(appData.url);
            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String url = v.getTag().toString();
                    Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                    intent.putExtra(AppConst.ACTION_URL, url);
                    startActivity(intent);
                }
            });
        }


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setData(Uri.parse("https://www.ukafu.com/help.html"));//Url 就是你要打开的网址
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent); //启动浏览器
            }
        });


        // 静音
        swt_mute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeMutestate(isChecked);
            }
        });

        Intent intent = new Intent(this, MainService.class);
        intent.putExtra("from", "MainActive");
        bindService(intent, conn, BIND_AUTO_CREATE);
        // 手动关闭服务之后 需要重新绑定服务 所以在onCreate处调用
        toggleNotificationListenerService();
        //
        checkStatus();
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_bindcode:
//                ToastUtil.show(this,"开源版本已去除服务器支持.请直接填写自己的服务器地址和参数");
//                break;
//                不在绑定服务器.
//                RequestData post = RequestData.newInstance(AppConst.NetTypeBindCode);
//                RequestUtils.post(AppConst.NoticeUrl, post, new HttpJsonResponse() {
//
//                    @Override
//                    protected void onJsonResponse(JSONObject data) {
//                        String code = null;
//                        try {
//                            code = "" + data.getInt("bind_code");
//                        } catch (JSONException e) {
//                        }
//                        Message msg = new Message();
//                        msg.what = 1;
//                        msg.obj = code;
//                        MainActivity.this.handler.sendMessage(msg);
//                    }
//
//                });
//                break;
//            case R.id.action_setting:
//                OpenSetting();
//                break;
            case R.id.action_exitapp:
                exit();
                break;
            case R.id.action_runtime:
                Intent intent = new Intent(MainActivity.this, RunTimeActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    /**
     * 设置界面
     * */
    private void OpenSetting() {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    /** 退出 */
    private void exit() {
        unbindService(conn);
        disableNotificationService();
    }

    /** 退出調用
     * 功能 Disable掉 NotificationService 直接退出App
     * */
    private void disableNotificationService() {
        // 先disable 服务
        PackageManager localPackageManager = getPackageManager();
        localPackageManager.setComponentEnabledSetting(new ComponentName(this, NotificationMonitorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);// 最后一个参数 DONT_KILL_APP或者0。 0说明杀死包含该组件的app
        //
    }

    private boolean enabedPrivileges;

    private void checkStatus() {
        //权限开启.才能启动服务
        boolean enabled = isEnabled();
        enabedPrivileges = enabled;
        swt_fuwu.setChecked(enabled);
        if (!enabled) {
            swt_service.setEnabled(false);
            return;
        }
        swt_service.setEnabled(true);
        //开启服务
        ComponentName name = startService(new Intent(this, NotificationMonitorService.class));
        if (name == null) {
            swt_service.setChecked(false);
            Toast.makeText(getApplicationContext(), "服务开启失败", Toast.LENGTH_LONG).show();
            return;
        }
        // 手动关闭服务之后 需要重新设置服务 所以在onCreate处调用
        // toggleNotificationListenerService();
        swt_service.setChecked(true);

        //微信支付宝开启

    }

    private void changeMutestate(boolean ischecked) {
        AppConst.PlaySounds = !ischecked;
        dbm.setConfig(AppConst.KeyMute, "" + AppConst.PlaySounds);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            keyCode = KeyEvent.KEYCODE_HOME;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isEnabled() {
        String str = getPackageName();
        String localObject = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(localObject)) {
            String[] strArr = (localObject).split(":");
            int i = 0;
            while (i < strArr.length) {
                ComponentName localComponentName = ComponentName.unflattenFromString(strArr[i]);
                if ((localComponentName != null) && (TextUtils.equals(str, localComponentName.getPackageName())))
                    return true;
                i += 1;
            }
        }
        return false;
    }

    // 判断短信是否可以读取
    private boolean checkSMS() {
        Uri uriSMS = Uri.parse("content://sms");
        Cursor c = this.getApplicationContext().getContentResolver().query(uriSMS, null, "read = 0",
                null, null);

        if (c != null && c.getCount() >= 0) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    private void openQrcode() {
        Intent intent = new Intent(MainActivity.this, QrcodeActivity.class);
        startActivity(intent);
    }

//    private void sendNotice(){
//        Log.i("ZYKJ","MainActivity Send Notice");
//        NotificationManager mNM =(NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            mNotificationChannel = new NotificationChannel(AppConst.CHANNEL_Test, "pxapy", NotificationManager.IMPORTANCE_DEFAULT);
//            mNotificationChannel.setDescription("个人支付的监控");
//            mNM.createNotificationChannel(mNotificationChannel);
//            Log.i("ZYKJ","MainActivity create mNotificationChannel");
//        }
//        Notification notification = new NotificationCompat.Builder(this,AppConst.CHANNEL_Test)
//                .setContentTitle("通知监控").setContentText("测试号通过扫码向你付款0.01元").setSmallIcon(R.mipmap.ic_launcher)
//                .setWhen(System.currentTimeMillis()).setChannelId(AppConst.CHANNEL_Test).build();
//
//        /**
//         * 注意,我们使用出来。incoming_message ID 通知。它可以是任何整数,但我们使用 资源id字符串相关
//         * 通知。它将永远是一个独特的号码在你的 应用程序。
//         */
//        mNM.notify(2,notification);
//        Log.i("ZYKJ","MainActivity Send Notice Comp");
//    }

    private void toggleNotificationListenerService() {
        PackageManager localPackageManager = getPackageManager();
        localPackageManager.setComponentEnabledSetting(new ComponentName(this, NotificationMonitorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        localPackageManager.setComponentEnabledSetting(new ComponentName(this, NotificationMonitorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
//
//    private void openPowerSetting(){
//        Intent powerUsageIntent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
//        ResolveInfo resolveInfo = getPackageManager().resolveActivity(powerUsageIntent, 0);
//        if(resolveInfo != null){
//            startActivity(powerUsageIntent);
//        }
//    }

    /**
     * 打开通知权限设置.一般手机根本找不到哪里设置
     */
    private void openNotificationListenSettings() {

        try {
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            } else {
                intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            }
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
