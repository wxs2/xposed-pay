package com.zhiyi.ukafu.util;

import android.util.Log;

import com.zhiyi.ukafu.AppConst;
import com.zhiyi.ukafu.BuildConfig;

import java.text.DateFormat;
import java.util.Date;


public class LogUtil {
    public static boolean debug = BuildConfig.DEBUG;
    private static LogUtil instance = new LogUtil();
    private String[] logArr;
    private int index;
    private static DateFormat format = DateFormat.getTimeInstance();
    public static void e(String msg){
        Log.e(AppConst.TAG_LOG,msg);
        instance.appendLog(msg);
    }

    public static void e(String msg,Throwable e){
        Log.e(AppConst.TAG_LOG ,msg,e);
        instance.appendLog(msg);
    }

    public static void i(String msg){
        Log.i(AppConst.TAG_LOG,msg);
        if(debug){
            instance.appendLog(msg);
        }
    }

    public static void i(String msg,Throwable e){
        Log.i(AppConst.TAG_LOG ,msg,e);
        if(debug){
            instance.appendLog(msg);
        }
    }

    private LogUtil(){
        logArr = new String[512];
        index = 0;
        lastIdx = -1;
    }

    private void appendLog(String msg){
        String time = format.format(new Date());
        logArr[index] = time +":"+msg+"\n";
        index = (index+1)%512;
    }

    public static String getLog(){
        return instance.readLog();
    }
    private String lastLog;
    private int lastIdx;
    private String readLog(){
        if(lastIdx == index){
            return lastLog;
        }
        lastIdx = index;
        StringBuffer sb = new StringBuffer(10240);
        for(int i=1;i<=512;i++){
            int  idx = index-i;
            if(idx<0){
                idx += 512;
            }
            if(logArr[idx]==null){
                break;
            }
            sb.append(logArr[idx]);
        }
        lastLog = sb.toString();
        return lastLog;
    }

}
