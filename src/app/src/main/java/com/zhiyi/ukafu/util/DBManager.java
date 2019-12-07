package com.zhiyi.ukafu.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zhiyi.ukafu.data.LogItem;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * @ClassName: DBManager
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @date 2018年6月23日 下午1:27:22
 */
public class DBManager {
    private SQLiteDatabase db;
    private DBHelper helper;
    private final String tableaName="ukafu";

    public DBManager(Context context) {
        helper = DBHelper.getInstance(context);
        db = helper.getWritableDatabase();
    }

    public String getConfig(String name) {
        Cursor c = db.query(tableaName,new String[]{"value_m"},"key_m=?",new String[]{name},null,null,null);
        String rs = "";
        while (c.moveToNext()) {
            rs = c.getString(c.getColumnIndex("value_m"));
            break;
        }
        c.close();
        return rs;
    }

    public boolean hasConfig(String name){
        Cursor c = db.query(tableaName,new String[]{"value_m"},"key_m=?",new String[]{name},null,null,null);
        return c.moveToNext();
    }

    public boolean setConfig(String name,String value){
        ContentValues values = new ContentValues();
        values.put("key_m",name);
        values.put("value_m",value);
        long n;
        if(hasConfig(name)){
            n = db.update(tableaName,values,"key_m=?",new String[]{name});
        }else{
            n = db.insert(tableaName,null,values);
        }
        return n>0;
    }

    public long addLog(String log,int type){
        ContentValues values = new ContentValues();
        values.put("log_value",log);
        values.put("log_type",type);
        SimpleDateFormat sd = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
        values.put("create_dt",sd.format(new Date(System.currentTimeMillis())));
        return db.insert(tableaName+"_log",null,values);
    }

    public Cursor getLog(int page,int type){
        if(page<1){
            page = 1;
        }
        int from = (page-1)*25;
        String pageStr = from+","+25;
        Cursor c;
        if(type>0){
            c = db.query(tableaName+"_log",new String[]{"*"},"log_type=?",new String[]{type+""},null,null,"id desc",pageStr);
        }else{
            c = db.query(tableaName+"_log",new String[]{"*"},null,null,null,null,"id desc",pageStr);
        }
        return c;
    }

    public ArrayList<LogItem> getLogList(int page, int type){
        ArrayList<LogItem> list = new ArrayList<>();
        Cursor c = getLog(page,type);
        int idIdx = c.getColumnIndex("id");
        int valueIdx = c.getColumnIndex("log_value");
        int typeIdx = c.getColumnIndex("log_type");
        int dateIdx = c.getColumnIndex("create_dt");
        while(c.moveToNext()){
            LogItem item = new LogItem();
            item.id  = c.getInt(idIdx);
            item.log_value = c.getString(valueIdx);
            item.log_type = c.getInt(typeIdx);
            item.create_dt = c.getString(dateIdx);
            list.add(item);
        }
        return list;
    }

    public String getUnid(){
        Cursor c = db.query(tableaName+"_mt",new String[]{"mtid"},"",new String[]{},null,null,null);
        String rs = "";
        while (c.moveToNext()) {
            rs = c.getString(c.getColumnIndex("mtid"));
            break;
        }
        c.close();
        return rs;
    }

    public long addUnid(String mtid){
        ContentValues values = new ContentValues();
        values.put("mtid",mtid);
        return db.insert(tableaName+"_mt",null,values);
    }

    public String getCookie(String url) {
        Cursor c = db.query(tableaName+"_cookie",new String[]{"cookies"},"url=?",new String[]{url},null,null,null);
        String rs = "";
        while (c.moveToNext()) {
            rs = c.getString(c.getColumnIndex("cookies"));
            break;
        }
        c.close();
        return rs;
    }
}
