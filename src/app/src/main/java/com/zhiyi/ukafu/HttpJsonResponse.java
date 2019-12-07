package com.zhiyi.ukafu;

import com.zhiyi.ukafu.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Administrator on 2019/6/21.
 */

public abstract class HttpJsonResponse implements IHttpResponse {

    private String code = "code";
    private String message = "msg";
    private String dataName = "data";
    private int success=0;

    public HttpJsonResponse(){
        this("code",0);
    }
    public HttpJsonResponse(String code,int success){
        this(code,success,"msg","data");
    }
    public HttpJsonResponse(String code,int success,String msg,String data){
        this.code=code;
        this.success = success;
        message = msg;
        dataName = data;
    }
    @Override
    public void OnHttpData(String data) {
        if (data.charAt(0) == '{' || data.charAt(0) == '[') {
            try {
                JSONObject jdata = new JSONObject(data);
                if (jdata != null) {
                    if(jdata.has(code)){
                        int value = jdata.getInt(code);
                        if (value != success) {
                            String msg = jdata.has(message)?jdata.getString(message):"未提供消息属性:"+message;
                            onError(value,msg);
                        } else {
                            onJsonResponse(jdata.getJSONObject(dataName));
                        }
                    }else{
                        LogUtil.e("json not code "+data);
                    }
                } else {
                    LogUtil.e("is not json " + data);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onError(int code,String msg){
        LogUtil.e("json response error,code:"+code+",msg:"+msg);
    }

    protected abstract void onJsonResponse(JSONObject data);

    @Override
    public void OnHttpDataError(IOException e) {
        LogUtil.e(e.getMessage());
    }
}
