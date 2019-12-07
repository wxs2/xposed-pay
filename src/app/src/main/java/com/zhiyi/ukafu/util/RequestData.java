package com.zhiyi.ukafu.util;

import com.zhiyi.ukafu.AppConst;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2019/6/18.
 */

public class RequestData extends JSONObject{

    public static RequestData newInstance(String type){
        return new RequestData(type);
    }
    private RequestData(String type){
        try {
            this.put("_type",type);
            this.put("_appid", AppConst.AppId);
            this.put("_token", AppConst.Token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
