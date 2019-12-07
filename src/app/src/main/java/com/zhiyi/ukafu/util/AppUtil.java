/**
 *  个人收款 https://gitee.com/DaLianZhiYiKeJi/xpay
 *  大连致一科技有限公司
 * */


package com.zhiyi.ukafu.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.zhiyi.ukafu.consts.ActionName;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;


public class AppUtil {
    /**
     * 虚拟机可能都一样的ID,必须随机
     *  */
    public static String getUniqueId(Context context) {
        DBManager dbManager = new DBManager(context);
        String id = dbManager.getUnid();
        if(id.length()>0){
            return id;
        }
        id = randString(32);
        dbManager.addUnid(id);
        return id;
    }


    public static String toMD5(String text){
        //获取摘要器 MessageDigest
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e("ZYKJ", "Md5Bug", e);
            return null;
        }

        //通过摘要器对字符串的二进制字节数组进行hash计算
        byte[] digest = messageDigest.digest(text.getBytes());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            //循环每个字符 将计算结果转化为正整数;
            int digestInt = digest[i] & 0xff;
            //将10进制转化为较短的16进制
            String hexString = Integer.toHexString(digestInt);
            //转化结果如果是个位数会省略0,因此判断并补0
            if (hexString.length() < 2) {
                sb.append(0);
            }
            //将循环结果添加到缓冲区
            sb.append(hexString);
        }
        //返回整个结果
        return sb.toString();
    }


    public static String randString(int len){
        StringBuilder sb = new StringBuilder(len);
        char[] seqs = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G','H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R','S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2','3', '4', '5', '6', '7', '8', '9'};
        int total = seqs.length;
        for (int i=0;i<len;i++){
            int rnd = (int) (Math.random()*total);
            sb.append(seqs[rnd]);
        }
        return  sb.toString();
    }

    public static String getVersionCode(Context mContext) {
        String versionCode = "1.0.0";
        try {
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 是不是url
     * @param urlStr
     * @return 是否为url
     */
    public  static boolean IsUrl(String urlStr) {
        Pattern httpPattern = Pattern.compile("^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~/])+$");
        return httpPattern.matcher(urlStr).matches();
    }


    public static Intent sendOrder(final String payType, final String money, final String username){
        Intent intent = new Intent(ActionName.ONORDER_REC);
        intent.putExtra("type",payType);
        intent.putExtra("money",money);
        intent.putExtra("username",username);
        return intent;
    }
    public static Intent sendOrder(final String payType, final String money, final String username,String dt, boolean dianYuan){
        Intent intent = sendOrder(payType,money,username);
        intent.putExtra("dianYuan",dianYuan);
        if(dt!=null){
            intent.putExtra("dt",dt);
        }
        return intent;
    }

    public static Intent sendOrder(final String payType, final String money, final String username,boolean dianYuan){
        return sendOrder(payType,money,username,null,dianYuan);
    }

}
