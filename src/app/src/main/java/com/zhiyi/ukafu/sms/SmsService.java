package com.zhiyi.ukafu.sms;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.zhiyi.ukafu.consts.ActionName;
import com.zhiyi.ukafu.consts.AppType;
import com.zhiyi.ukafu.util.AppUtil;
import com.zhiyi.ukafu.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by pc_mg on 2019/1/15.
 */

public class SmsService implements Handler.Callback{
    private static final Uri CONTENT_URI = Uri.parse("content://sms");
    private SMSObserver mObserver;
    private Service service;

    public void SmsService() {
    }

    public void registerSMSObserver(Service svc){
        service = svc;
        smsHandler = new Handler();
        //注册监听器
        ContentResolver resolver = svc.getContentResolver();
        //获取观察者对象
        SMSObserver mObserver = new SMSObserver(resolver, this.smsHandler);
        mObserver.test();
        //注册
        resolver.registerContentObserver(CONTENT_URI, true, mObserver);
    }

    private Handler smsHandler;

    private void holdphonemsg(SMSItem item) {
        try {
            String content = item.body;
            String date = item.date;
            if(System.currentTimeMillis()-Long.parseLong(date)>30000){
                return;
            }
            String tailnum = "";
            String money = "";
            String money2 = "";
            String tailnum2 = "";
            String paytype = AppType.BANK;
            Pattern p=Pattern.compile("(\\d+\\.\\d+\\d)");
            Matcher m=p.matcher(content);
            if(m.find()){
                money2 = m.group(1);
            }
            p=Pattern.compile("(\\d+\\d+\\d+\\d)");
            m=p.matcher(content);
            if(m.find()){
                tailnum2 = m.group(1);
            }
            String mark = "false";
            if (content.contains("[兴业银行]")) {
                mark = "兴业银行";
                tailnum = StringUtils.getTextCenter(content, "账户*", "*网联");
                money = StringUtils.getTextCenter(content, "收入", "元");
            }else
            if (content.contains("【华夏银行】")) {
                mark = "华夏银行";
                tailnum = StringUtils.getTextCenter(content, "（**", "），");
                money = StringUtils.getTextCenter(content, "到账人民币", "元");
            }else
            if (content.contains("【中国农业银行】")) {
                mark = "中国农业银行";
                tailnum = StringUtils.getTextCenter(content, "尾号", "账户");
                money = StringUtils.getTextCenter(content, "人民币", "，");
            }else
            if (content.contains("【工商银行】")) {
                mark = "中国工商银行";
                tailnum = StringUtils.getTextCenter(content, "尾号", "卡");
                money = StringUtils.getTextCenter(content, "支付宝)", "元");
            }else
            if (content.contains("【民生银行】")) {
                mark = "中国民生银行";
                tailnum = StringUtils.getTextCenter(content, "账户*", "于");
                money = StringUtils.getTextCenter(content, "存入￥", "元，");
            }else
            if (content.contains("【平安银行】")) {
                mark = "平安银行";
                tailnum = StringUtils.getTextCenter(content, "尾号", "的");
                money = StringUtils.getTextCenter(content, "转入人民币", "元,");
            }else
            if (content.contains("【厦门银行】")) {
                mark = "厦门银行";
                tailnum = StringUtils.getTextCenter(content, "尾数为", "的");
                money = StringUtils.getTextCenter(content, "转入", "元,");
            }else
            if (content.contains("【泉州银行】")) {
                mark = "泉州银行";
                tailnum = StringUtils.getTextCenter(content, "尾号", "的");
                money = StringUtils.getTextCenter(content, "入账人民币", "元，");
            }else if (content.contains("[建设银行]")) {
                if(content.contains("您注册的商户")){
                    tailnum="0000";
                    paytype = AppType.LEFSMS;
                    Pattern py=Pattern.compile("(\\d+\\.\\d+\\d)元");
                    Matcher my=py.matcher(content);
                    if(my.find()){
                        money = m.group(1);
                    }
                    mark = StringUtils.getTextCenter(content, "商户名称：", "，");
                }else{
                    mark = "中国建设银行";
                    tailnum = StringUtils.getTextCenter(content, "尾号", "的");
                    money = StringUtils.getTextCenter(content, "收入人民币", "元");
                }
            }else if (content.contains("[招商银行]")) {
                mark = "招商银行";
                tailnum = StringUtils.getTextCenter(content, "账户", "于");
                money = StringUtils.getTextCenter(content, "收款人民币", "，");
            }else if (content.contains("【中信银行】")) {
                mark = "中信银行";
                tailnum = StringUtils.getTextCenter(content, "尾号", "的");
                money = StringUtils.getTextCenter(content, "存入人民币", "元，");
            }else{
                tailnum = tailnum2;
                money = money2;
            }

            try {
                float f = Float.parseFloat(money);
                money = ""+f;
            }catch (Exception e){
                money = money2;
            }
            String no = AppUtil.toMD5("message"+date+tailnum+money);
            Intent broadCastIntent = new Intent();
            broadCastIntent.putExtra("bill_no", no);
            broadCastIntent.putExtra("bill_money", money);
            broadCastIntent.putExtra("bill_mark", mark);
            broadCastIntent.putExtra("bill_wh",tailnum);
            broadCastIntent.putExtra("bill_type", paytype);
            broadCastIntent.putExtra("bill_dt", date);
            broadCastIntent.setAction(ActionName.ONSMSRED);
            service.sendBroadcast(broadCastIntent);
            Log.i("yyk ","no"+no+"date"+date +"tailnum "+tailnum + "money "+money);
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        holdphonemsg((SMSItem)msg.obj);
        return true;
    }
}
