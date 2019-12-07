package com.zhiyi.ukafu.qrcode;

import com.zhiyi.ukafu.IHttpResponse;
import com.zhiyi.ukafu.util.LogUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

public class QrCli {
    private static OkHttpClient client;
    public static void upload(final String url, final File data, final IHttpResponse callback){
        client = new OkHttpClient.Builder()
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();
        RequestBody body = MultipartBody.create(data, MediaType.parse("image/jpeg"));
        MultipartBody mbody = new MultipartBody.Builder()
                .setType(MediaType.parse("multipart/form-data"))
                .addFormDataPart("Filedata",data.getName(),body).build();
        Request request = new Request.Builder().url(url)
                .addHeader("Referer","https://cli.im/deqr")
                .addHeader("Origin","https://cli.im")
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36")
                .tag(url).post(mbody).build();
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String rs = response.body().string();
                LogUtil.i("response: "+rs);
                try {
                    JSONObject jdata = new JSONObject(rs);
                    if (jdata.has("status")) {
                        int value = jdata.getInt("status");
                        if (value == 1) {
                            String url = jdata.getJSONObject("data").getString("path");
                            decode("https://cli.im/apis/up/deqrimg", url, callback);
                        }
                    }
                }catch (JSONException je){
                    LogUtil.e("json parse error",je);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                LogUtil.e(e.getMessage()+":"+url);
                callback.OnHttpDataError(e);
            }

        });
    }
    public static void decode(final String url, final String img, final IHttpResponse callback){
        RequestBody body = new FormBody.Builder()
                .add("img",img)
                .build();
        Request request = new Request.Builder().url(url)
                .addHeader("Referer","https://cli.im/deqr")
                .addHeader("Origin","https://cli.im")
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36")
                .tag(url).post(body).build();
        client.newCall(request).enqueue(new Callback(){

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String rs = response.body().string();
                LogUtil.i(rs);
                callback.OnHttpData(rs);
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.OnHttpDataError(e);
            }
        });
    }

//
}