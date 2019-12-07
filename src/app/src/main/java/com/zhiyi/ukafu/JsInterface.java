package com.zhiyi.ukafu;

import android.app.Activity;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.zhiyi.ukafu.util.LogUtil;
import com.zhiyi.ukafu.util.SystemProgramUtils;

import org.json.JSONException;
import org.json.JSONObject;

public final class JsInterface {

    private WebView webView;
    private Activity activity;

    public JsInterface(WebView webview,Activity activity){
        this.webView = webview;
        this.activity = activity;
        callback = "alert";
    }

    @JavascriptInterface
    public String getToken(){
        return AppConst.AppId+"-"+AppConst.Token;
    }

    @JavascriptInterface
    public boolean isBoot(){
        return false;
    }
    @JavascriptInterface
    public void setBoot(boolean a){

    }


    @JavascriptInterface
    public void appendLog(int level,String log){
        if(level>1){
            LogUtil.e("js log=>"+log);
        }else{
            LogUtil.i("js log=>"+log);
        }
    }


    private String callback;
    @JavascriptInterface
    public void selectQrcode(String callback){
        this.callback = callback;
        SystemProgramUtils.zhaopian(activity);

    }


    public void onQrcodeload(Intent intent) {
        if(callback!=null){
            JSONObject jsonObject = new JSONObject();
            for (String key:intent.getExtras().keySet()) {
                try {
                    jsonObject.put(key,intent.getExtras().get(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            String jsCallback = "javascript:"+callback+"(" + jsonObject.toString() + ")";
            LogUtil.i("call js interface:"+jsCallback);
            webView.loadUrl(jsCallback);
        }
    }
}
