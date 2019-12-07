/**
 *  个人收款 https://gitee.com/DaLianZhiYiKeJi/xpay
 *  大连致一科技有限公司
 * */

package com.zhiyi.ukafu.util;


import android.util.Log;


//import com.squareup.okhttp.Callback;
//import com.squareup.okhttp.MediaType;
//import com.squareup.okhttp.OkHttpClient;
//import com.squareup.okhttp.Request;
//import com.squareup.okhttp.RequestBody;
//import com.squareup.okhttp.Response;
import com.zhiyi.ukafu.IHttpResponse;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.util.EntityUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;



public class RequestUtils {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient client;

    public static void getRequest(final String url,final IHttpResponse callback) {
        if(client == null){
            client = new OkHttpClient.Builder()
                    .writeTimeout(5,TimeUnit.SECONDS)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build();
        }
        Log.i("ZYKJ", "request: " + url);
        Request request = new Request.Builder().url(url).tag(url).build();
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String rs = response.body().string();
                Log.i("ZYKJ", "response: " + rs);
                callback.OnHttpData(rs);
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ZYKJ", e.getMessage() + ":" + url);
                callback.OnHttpDataError(e);
            }
        });
    }


    public static void post(final String url, final RequestData data, final IHttpResponse callback){
        post(url,data.toString(),callback);
    }
    public static void post(final String url,final String data,final IHttpResponse callback){
        if(client == null){
            client = new OkHttpClient.Builder()
                    .writeTimeout(5,TimeUnit.SECONDS)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .build();
        }
        LogUtil.i(url+"=>"+data);
        RequestBody body = new RequestBody() {
            @Override
            public MediaType contentType() {
                return JSON;
            }

            @Override
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                bufferedSink.writeUtf8(data);
            }
        };
        Request request = new Request.Builder()
                .url(url).tag(url).post(body)
                .build();
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String rs = response.body().string();
                LogUtil.i("response: "+rs);
                callback.OnHttpData(rs);
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("ZYKJ",e.getMessage()+":"+url);
                callback.OnHttpDataError(e);
            }

        });
    }

}