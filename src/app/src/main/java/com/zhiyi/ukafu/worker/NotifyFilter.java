package com.zhiyi.ukafu.worker;

import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class NotifyFilter {

    private HashMap<String,ArrayList<NotifyFilterItem>> map;
    private HashMap<String,String> empty;

    public HashMap<String,String> test(Bundle bundle, String pkgName) {
        if(map!=null){
            if(map.containsKey(pkgName)){
                String title = bundle.getString("android.title");
                String text = bundle.getString("android.text");
                ArrayList<NotifyFilterItem> list = map.get(pkgName);
                for(Iterator<NotifyFilterItem> item=list.iterator();item.hasNext();){
                    NotifyFilterItem filter = item.next();
                    HashMap<String,String> data = filter.test(title,text);
                    if(data!=null){
                        return data;
                    }
                }
            }
            return empty;
        }
        return null;
    }

    public void initData(String data) throws JSONException {
        map = new HashMap<>();
        empty = new HashMap<>();
        JSONObject json = new JSONObject(data);
        JSONArray list = json.getJSONArray("data");
        for(int i=0;i<list.length();i++){
            JSONObject fd = list.getJSONObject(i);
            /*
            {
            paytype:"ALIPAY",
            pattern:"(\S*)通过扫码向你付款([\d\.]+)元",
            pkgName:"com.xxx",
            groupname:["payer","money"],
            name:"支付宝",
            id:"1"
            }
            */
            String pkgName = fd.getString("pkgName");
            ArrayList<NotifyFilterItem> flist;
            if(map.containsKey(pkgName)){
                flist = map.get(pkgName);
            }else{
                flist = new ArrayList<>();
                map.put(pkgName,flist);
            }
            NotifyFilterItem item = new NotifyFilterItem(fd);
            flist.add(item);
        }
    }
}
