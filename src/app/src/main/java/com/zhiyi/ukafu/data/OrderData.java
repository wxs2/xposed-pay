package com.zhiyi.ukafu.data;

import android.content.Intent;

import com.zhiyi.ukafu.AppConst;
import com.zhiyi.ukafu.util.AppUtil;
import com.zhiyi.ukafu.util.RequestData;

import org.json.JSONException;


public class OrderData extends OrderDataBase {


    public String username;

    public int dianYuan;

    public OrderData(Intent intent){
        final boolean dianYuan = intent.getBooleanExtra("dianYuan",false);
        this.payType = intent.getStringExtra("type");
        this.username = intent.getStringExtra("username");
        this.money = intent.getStringExtra("money");
        this.dianYuan = dianYuan?1:0;
    }

    public boolean isPost(){
        return false;
    }

    public String getApiUrl(){
        return AppConst.NoticeUrl + "person/notify/pay?version="+AppConst.version+"&"+getOrderData();
    }

    public RequestData getOrderData(){
        String app_id = "" + AppConst.NoticeAppId;
        this.sign = AppUtil.toMD5(app_id + AppConst.NoticeSecret + time + AppConst.version + rndStr + payType + money + username);
//        return "type=" + payType
//                + "&money=" + money
//                + "&uname=" + username
//                + "&appid=" + AppConst.NoticeAppId
//                + "&rndstr=" + rndStr
//                + "&sign=" + sign
//                + "&time=" + time
//                + "&dianyuan=" + dianYuan;
        RequestData requestData = RequestData.newInstance(AppConst.NetTypeNotify);
        try {
            requestData.put("type",payType);
            requestData.put("money",money);
            requestData.put("uname",username);
            requestData.put("appid",AppConst.NoticeAppId);
            requestData.put("rndstr",rndStr);
            requestData.put("sign",sign);
            requestData.put("time",time);
            requestData.put("dianyuan",dianYuan);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestData;
    }


    @Override
    public String getLogString() {
        return "type="+payType+",money="+money+",user="+username;
    }

}
