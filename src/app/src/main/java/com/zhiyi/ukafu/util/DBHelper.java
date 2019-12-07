package com.zhiyi.ukafu.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 

* @ClassName: DBHelper

* @Description: TODO(这里用一句话描述这个类的作用)

* @date 2018年6月23日 下午1:27:16

*
 */
public class DBHelper extends SQLiteOpenHelper{
	private static DBHelper sInstance;
	public DBHelper(Context context) {  
        super(context, "zykj.db", null, 4);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
//		db.execSQL("CREATE TABLE IF NOT EXISTS ukafu(_id INTEGER PRIMARY KEY AUTOINCREMENT, money varchar, mark varchar, type varchar, payurl varchar, dt varchar)");
		db.execSQL("CREATE TABLE IF NOT EXISTS ukafu (key_m varchar primary key,value_m varchar)");
		String sql = "CREATE TABLE IF NOT EXISTS ukafu_log(id integer primary key autoincrement,log_value varchar(512),log_type int(11),create_dt varchar(20))";
		db.execSQL(sql);
		db.execSQL("CREATE TABLE IF NOT EXISTS ukafu_mt(mtid varchar(512))");
		db.execSQL("CREATE TABLE IF NOT EXISTS ukafu_cookie(url varchar(1024) primary key,cookies varchar(10240))");
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(newVersion==3){
			String sql = "CREATE TABLE IF NOT EXISTS ukafu_mt(mtid varchar(32))";
			db.execSQL(sql);
		}
		if(newVersion == 4){
			db.execSQL("CREATE TABLE IF NOT EXISTS ukafu_cookie(url varchar(1024) primary key,cookies varchar(10240))");
		}
	}

	public static DBHelper getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (sInstance == null) {
			sInstance = new DBHelper(context.getApplicationContext());
		}
		return sInstance;
	}
}
