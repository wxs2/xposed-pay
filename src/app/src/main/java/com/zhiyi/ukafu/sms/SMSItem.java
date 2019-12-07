package com.zhiyi.ukafu.sms;

/**
 * Created by pc_mg on 2019/1/15.
 */

public class SMSItem {
    public int id;
    public int type;
    public int protocol;
    public String date;
    public String phone;
    public String body;
    public String toString() {
        return "SMSItem{" +
                "id=" + id +
                ",type=" + type +'\'' +
                ",protocol='" + protocol +'\'' +
                ",date='" + date + '\''+
                ",body,"+body+'\''+
                "}";
    }
}
