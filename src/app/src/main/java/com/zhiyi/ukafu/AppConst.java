package com.zhiyi.ukafu;

import android.text.TextUtils;

import com.zhiyi.ukafu.data.H5AppData;
import com.zhiyi.ukafu.util.DBManager;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/8/6.
 */

public class AppConst {
    public static final String KeyAppId = "appid";
    public static final String KeyToken = "token";
    public static final String KeySecret = "secret";
    public static final String KeyWebAPP = "webapp";
    public static final String KeyIsCustom = "is_custom";
    public static final String KeyNoticeUrl = "notice_url";
    public static final String KeyNoticeAppId = "notice_appid";
    public static final String KeyNoticeSecret = "notice_secret";
    public static final String KeyUKFNoticeUrl = "uk_notice_url";
    public static final String KeyUKFNoticeAppId = "uk_notice_appid";
    public static final String KeyUKFNoticeToken = "uk_notice_token";
    public static final String KeyUKFNoticeSecret = "uk_notice_secret";
    public static final String KeyBoolLog = "b_set_log";
//    public static final String KeyBoolWx = "b_set_wx";
//    public static final String KeyBoolZfb = "b_set_zfb";

    public static boolean isOnline;
    /**
     * 音量
     */
    public static final String KeyMute = "mute";
    /**
     * 服务器地址
     */
//    public static final String HostUrl = "https://pxpay.ukafu.com/";
//    public static final String HostUrl = "http://test.ukafu.com/";
//    public static final String HostUrl = "http://192.168.1.129:89/";
//    public static final String HostUrl = "http://faka.ukafu.com/";

    public static final String HostUrl = "https://www.ukafu.com/";

    /**
     * 传输数据为URL的KEY
     */
    public static final String ACTION_URL = "a_url";

    public static String version;

    public static int Battery = 0;

    public static boolean PlaySounds = true;
    public static int AppId = 0;
    public static String Token = "";
    public static String Secret = "";
    public static String Online = "";
    /**
     * 通知url
     */
    public static String NoticeUrl = HostUrl;
    public static int NoticeAppId = 0;
    public static String NoticeSecret = "";

    public static int DetaTime = 0;//手机和服务器的时间差

    public static final String TAG_LOG = "ZYKJ";

    public static final String IntentAction = "com.zhiyikeji.Notification";

    public static final int MT_Net_Response = 1;
    public static final int MT_Net_Toast = 1;

    public static final String CHANNEL_ID = "zhi_yi_px_pay";
    public static final String CHANNEL_Front = "zhi_yi_px_pay_front";
    public static final String CHANNEL_Test = "zhi_yi_px_pay_test";

    public static final String TypeBANK = "";

    /**
     * 本地存储setting内容
     */
    public static final String SP_Setting = "setting_config";
    public static final String SP_Setting_KeyHost = "host_server";
    /**
     * 网络交互KEY
     *  */
    /**
     * 登录
     * */
    public static final String NetTypeLogin="login";
    /**
     * 通知,收到感兴趣的通知,发给服务器
     * */
    public static final String NetTypeNotify="notify";
    /**
     * 轮询,告诉服务器我在线
     * */
    public static final String NetTypeOnline="online";
    /**app绑定,获取绑定码*/
    public static final String NetTypeBindCode="bindCode";
    /**app绑定,清理绑定码*/
    public static final String NetTypeUnBindCode="unbindCode";
    //二维码上传,获取
    /**
     * 二维码列表
     * */
    public static final String NetTypeQrcodeList="qrList";
    /**
     * 上传二维码
     * */
    public static final String NetTypeQrcodeUpload="qrUpload";
    public static ArrayList<H5AppData> h5Apps = new ArrayList<>();

    public static final String authUrl(String api) {
        return HostUrl + api + "?appid=" + AppId + "&token=" + Token;
    }

    public static void InitParams(DBManager dbm){
        String custom = dbm.getConfig(AppConst.KeyIsCustom);
        if("custom".equals(custom)){
            //自定义选项
        }else{
            //通知url
            String noticeUrlStr = dbm.getConfig(AppConst.KeyUKFNoticeUrl);
            AppConst.NoticeUrl = TextUtils.isEmpty(noticeUrlStr) ? AppConst.HostUrl+"person/notify/pay" :noticeUrlStr;
            //通知appid
            String noticeAppIdStr = dbm.getConfig(AppConst.KeyUKFNoticeAppId);
            AppConst.AppId =TextUtils.isEmpty(noticeAppIdStr)? 0 : Integer.parseInt(noticeAppIdStr);
            //通知密钥
            String noticeSecretStr = dbm.getConfig(AppConst.KeyUKFNoticeSecret);
            AppConst.Secret = TextUtils.isEmpty(noticeSecretStr) ? "" : noticeSecretStr;
        }
    }

}
