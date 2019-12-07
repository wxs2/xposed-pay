package com.zhiyi.ukafu.activitys;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.zhiyi.ukafu.AppConst;
import com.zhiyi.ukafu.R;
import com.zhiyi.ukafu.util.AppUtil;
import com.zhiyi.ukafu.util.DBManager;

public class SettingActivity extends AppCompatActivity {
    private AutoCompleteTextView _acTextAppid;
    private AutoCompleteTextView _acTextSecret;
    private AutoCompleteTextView _acTextNoticeServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = findViewById(R.id.setting_toolbar);
        _acTextNoticeServer = findViewById(R.id.ac_textView_notice_uri);
        _acTextAppid = findViewById(R.id.ac_text_appid);
        _acTextSecret = findViewById(R.id.ac_text_secret);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar bar = getSupportActionBar();
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
        }

        if (_acTextNoticeServer != null) {
            _acTextNoticeServer.setText(AppConst.NoticeUrl);
            AddRecord(_acTextNoticeServer,AppConst.HostUrl);
        }

        if(_acTextAppid!=null){
            _acTextAppid.setText(""+AppConst.NoticeAppId);
            AddRecord(_acTextAppid,Integer.toString(AppConst.AppId));
        }

        if(_acTextSecret !=null){
            _acTextSecret.setText(""+AppConst.NoticeSecret);
            AddRecord(_acTextSecret, AppConst.Secret);
        }
    }

    /**
     * 添加记录
     * @param view
     * @param record
     */
    private void AddRecord(AutoCompleteTextView view,String record){
        String[] nServer = new String[1];
        nServer[0] = record;
        ArrayAdapter<?> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, nServer);
        view.setThreshold(1);
        view.setAdapter(adapter);
    }

    public void copyAppid(){

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                boolean isFinished = OnSave();
                if(isFinished){
                    this.finish();
                    return true;
                }
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 保存状态
     */
    private boolean OnSave() {
        DBManager dbManager = new DBManager(this);
        if (_acTextNoticeServer != null) {
            String noticeUrl = _acTextNoticeServer.getText().toString();
            if(!TextUtils.isEmpty(noticeUrl)) {
                Boolean isUrl = AppUtil.IsUrl(noticeUrl);
                if(isUrl){
                    if (!noticeUrl.equals(AppConst.NoticeUrl)) {
                        if(!noticeUrl.endsWith("/")){
                            noticeUrl = noticeUrl.concat("/");
                        }
                        AppConst.NoticeUrl = noticeUrl;
                        dbManager.setConfig(AppConst.KeyNoticeUrl, noticeUrl);
                    }
                }
                else{
                    Toast.makeText(this, getString(R.string.msg_please_input_right_host_url),Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }

        boolean isDefaultHost = AppConst.HostUrl.equals(AppConst.NoticeUrl);

        if(isDefaultHost){
            AppConst.NoticeAppId = AppConst.AppId;
            AppConst.NoticeSecret = AppConst.Secret;
            dbManager.setConfig(AppConst.KeyNoticeAppId, Integer.toString(AppConst.AppId));
            dbManager.setConfig(AppConst.KeyNoticeSecret,  AppConst.Secret);
        }else{
            if (_acTextAppid != null) {
                String appIdStr = _acTextAppid.getText().toString();
                if(!TextUtils.isEmpty(appIdStr)){
                    int appId = Integer.parseInt(appIdStr);
                    if (appId != AppConst.NoticeAppId) {
                        AppConst.NoticeAppId = appId;
                        dbManager.setConfig(AppConst.KeyNoticeAppId, Integer.toString(appId));
                    }
                }
            }
            if (_acTextSecret != null) {
                String secretStr = _acTextSecret.getText().toString();
                if(!TextUtils.isEmpty(secretStr)){
                    if (secretStr != AppConst.NoticeSecret) {
                        AppConst.NoticeSecret = secretStr;
                        dbManager.setConfig(AppConst.KeyNoticeSecret, secretStr);
                    }
                }
            }
        }
        return true;
    }
}
