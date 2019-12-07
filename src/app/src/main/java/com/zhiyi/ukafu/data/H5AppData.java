package com.zhiyi.ukafu.data;

import com.zhiyi.ukafu.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class H5AppData {
    public String url;
    public String name;
    public String image;

    public H5AppData(JSONObject config){
        try {
            url = config.getString("url");
            name = config.getString("name");
            if(config.has("image")){
                image = config.getString("image");
            }
        } catch (JSONException e) {
            LogUtil.e("json no data,url,name,image");
        }
    }
}
