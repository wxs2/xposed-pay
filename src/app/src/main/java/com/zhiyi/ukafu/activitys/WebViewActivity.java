package com.zhiyi.ukafu.activitys;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.zhiyi.ukafu.AppConst;
import com.zhiyi.ukafu.JsInterface;
import com.zhiyi.ukafu.QrcodeUploadActivity;
import com.zhiyi.ukafu.R;
import com.zhiyi.ukafu.components.ZyWebViewClient;
import com.zhiyi.ukafu.util.DBHelper;
import com.zhiyi.ukafu.util.DBManager;
import com.zhiyi.ukafu.util.LogUtil;
import com.zhiyi.ukafu.util.SystemProgramUtils;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private static String LOG_TAG = "ZYKJ";
    private JsInterface jsInterface;
    private DBManager dbm;
    private String Url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_web_view);
        webView = findViewById(R.id.web_view);
//        webView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        webView.setWebViewClient(new ZyWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        //webView.setWebContentsDebuggingEnabled(true);
        Intent intent = getIntent();
        Url = intent.getStringExtra(AppConst.ACTION_URL);
        if (Url == null) {
            Url = "https://www.ukafu.com/default_app_h5.html";
        }
        jsInterface = new JsInterface(webView,this);
        webView.addJavascriptInterface(jsInterface,"client");
        webView.loadUrl(Url);
        CookieManager ck = CookieManager.getInstance();
        ck.setAcceptCookie(true);
        ck.setAcceptThirdPartyCookies(webView,true);
        ck.getCookie(Url);
        dbm = new DBManager(this);
        String sck = dbm.getCookie(Url);
        ck.setCookie(Url,sck);

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (intent == null) {
            return;
        }
        if (requestCode == SystemProgramUtils.REQUEST_CODE_ZHAOPIAN) {
            Uri uri = intent.getData();
            if (uri == null) {
                Toast.makeText(this, "目标数据为空", Toast.LENGTH_LONG);
                return;
            }
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) {
                Toast.makeText(this, "找不到数据", Toast.LENGTH_LONG);
                return;
            }
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            if (idx < 0) {
                Toast.makeText(this, "无法访问相册", Toast.LENGTH_LONG);
                return;
            }
            String currentPhotoString = cursor.getString(idx);
            cursor.close();

            Intent qIntent = new Intent(WebViewActivity.this, QrcodeUploadActivity.class);
            qIntent.putExtra("file", currentPhotoString);
            qIntent.putExtra("forupload",false);
            startActivityForResult(qIntent, 101);
        }else if(requestCode == SystemProgramUtils.REQUEST_CODE_PERMISSION){
            LogUtil.i("请求权限结果:"+resultCode);
        }else if(requestCode == 101){
            jsInterface.onQrcodeload(intent);
        }
    }
}
