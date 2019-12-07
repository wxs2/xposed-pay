package com.zhiyi.ukafu.sms;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;


/**
 * Created by pc_mg on 2019/1/15.
 */

public class SMSObserver extends ContentObserver {

    private ContentResolver mresolver;

    private Handler mhandler;

    public SMSObserver(ContentResolver contentResolver, Handler handler) {
        super(handler);
        mresolver = contentResolver;
        mhandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        query();
    }

    public void test(){
        String where = " date > " + String.valueOf(System.currentTimeMillis() - 500 * 1000);
        mresolver.query(Uri.parse("content://sms"), null, where, null, "date desc");
    }

    private void query() {
        String where = " date > " + String.valueOf(System.currentTimeMillis() - 500 * 1000);

        Cursor cursor = mresolver.query(Uri.parse("content://sms"), null, where, null, "date desc");

        int id, type, protocol;
        String phone, body, date;
        Message message;
        while (cursor.moveToNext()) {
            body = cursor.getString(cursor.getColumnIndex("body"));
            if ((body.contains("银行")||body.contains("账户")&&body.contains("尾号")) && (body.contains("收入")
                    || body.contains("到账") || body.contains("完成代付交易")
                    || body.contains("存入") || body.contains("转入")
                    || body.contains("入账") || body.contains("收款"))) {
                type = cursor.getInt(cursor.getColumnIndex("type"));
                id = cursor.getInt(0);
                date = cursor.getString(4);
                phone = cursor.getString(2);
                protocol = cursor.getInt(7);

                SMSItem item = new SMSItem();
                item.type = type;
                item.id = id;
                item.date = date;
                item.phone = phone;
                item.protocol = protocol;
                item.body = body;
                Message msg = new Message();
                msg.what = 123;
                msg.obj = item;
                mhandler.sendMessage(msg);
            }
        }
    }
}
