package com.zhiyi.ukafu.worker;

import android.util.Log;

import com.zhiyi.ukafu.AppConst;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2019/5/5.
 */

public class NotifyFilterItem {

    private Pattern _pattern;
    private String paytype;//:"ALIPAY",
    private String name;//:"支付宝",
    private String id;
    private String[] groups;
    private String ext="";

    public NotifyFilterItem(JSONObject data) throws JSONException {
        this.paytype = data.getString("paytype");
        this.name = data.getString("name");
        this.id = data.getString("id");
        String pt = data.getString("pattern");
        _pattern = Pattern.compile(pt);
        groups = data.getString("groupname").split(",");
        if(data.has("ext")){
            ext = data.getString("ext");
        }
    }

    public HashMap<String,String> test(String title,String body) {
        Matcher mt = _pattern.matcher(body);
        if(mt.find()){
            int len = mt.groupCount();
            if(len!=groups.length){
                Log.e(AppConst.TAG_LOG,"正则匹配长度不对:"+len+"<"+groups.length+","+body);
            }
            HashMap<String,String> map= new HashMap<>();
            map.put("id",id);
            map.put("name",name);
            map.put("paytype",paytype);
            for(int i=0;i<len;i++){
                map.put(groups[i],mt.group(i+1));
            }
            return map;
        }
        return null;
    }
}
