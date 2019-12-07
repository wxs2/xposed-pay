/**
 个人收款 https://gitee.com/DaLianZhiYiKeJi/xpay
 大连致一科技有限公司
 */

package com.zhiyi.ukafu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tencent.bugly.Bugly;
import com.zhiyi.ukafu.util.AppUtil;
import com.zhiyi.ukafu.util.DBManager;
import com.zhiyi.ukafu.util.RequestUtils;
import com.zhiyi.ukafu.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private final String TAG_ZYKJ="ZYKJ";


    // UI references.
    private EditText mAppIdView;
    private EditText mTokenView;
    private View mProgressView;
    private View mLoginFormView;
    private DBManager dbManager;
    private Handler handler;
    private Button copyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mAppIdView = findViewById(R.id.app_id);
        mTokenView = findViewById(R.id.token);
        mTokenView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button getToken = (Button)findViewById(R.id.btn_getToken);
        getToken.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                act_GetToken(v);
            }

        });

        Button mEmailSignInButton = (Button)findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        //腾讯的版本升级,日志,自己打包就去掉这个行
        Bugly.init(getApplicationContext(), BuildConst.BuglyId, false);

        dbManager = new DBManager(this);
        String appid = readData(AppConst.KeyAppId);
        if(!TextUtils.isEmpty(appid)){
            mAppIdView.setText(appid);
            AppConst.AppId = Integer.parseInt(appid);
            String token = readData(AppConst.KeyToken);
            if(!TextUtils.isEmpty(token) && !"123456".equals(token)){
                mTokenView.setText(token);
                attemptLogin();
            }
        }else{
            createAppId();
        }
        //
        getAppConfig();
        //
    }

    private void getAppConfig(){
        // 静音
        String mute = readData(AppConst.KeyMute);
        if(!TextUtils.isEmpty(mute)){
            AppConst.PlaySounds = Boolean.parseBoolean(mute);
        }else{
            dbManager.setConfig(AppConst.KeyMute,"true");
        }
    }

    private void createAppId() {
        String appUnid = AppUtil.getUniqueId(this);
        if(appUnid!=null){
            showProgress(true);

            String sign= AppUtil.toMD5("zhiyikeji"+appUnid+BuildConst.Secret);
            RequestUtils.getRequest(AppConst.HostUrl + "person/api/getAppIdV2/unid/" + appUnid + "/sign/" + sign, new IHttpResponse() {
                @Override
                public void OnHttpData(String rs) {
                    if(rs==null || rs.isEmpty()){
                        ToastUtil.show(LoginActivity.this, "获取失败,空返回值");
                        return;
                    }
                    try {
                        final JSONObject json = new JSONObject(rs);
                        if(json.getInt("code")==0){
                            JSONObject data = json.getJSONObject("data");
                            String appId = data.getString("appid");
                            if(data.has("token")){
                                AppConst.Token = data.getString("token");
                            }
                            AppConst.AppId = Integer.parseInt(appId);
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                dbManager.setConfig(AppConst.KeyAppId,AppConst.AppId+"");
                                mAppIdView.setText(""+AppConst.AppId);
                                if(AppConst.Token!=null && !AppConst.Token.isEmpty()){
                                    dbManager.setConfig(AppConst.KeyToken,AppConst.Token);
                                    mTokenView.setText(AppConst.Token);
                                    attemptLogin();
                                }
                                }
                            };
                            handler.post(runnable);

                        }else{
                            ToastUtil.show(LoginActivity.this, json.getString("msg"));
                        }
                    } catch (JSONException e) {
                        Log.w(TAG_ZYKJ,e.getMessage());
                        ToastUtil.show(LoginActivity.this, "获取失败,JSON解析失败");
                    }
                }

                @Override
                public void OnHttpDataError(IOException e) {

                }
            });
            showProgress(false);
        }
    }

    private String readData(String name){
        return dbManager.getConfig(name);
    }

    public void act_GetToken(View view) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(AppConst.HostUrl+"person/index/getToken/appid/"+AppConst.AppId));//Url 就是你要打开的网址
        intent.setAction(Intent.ACTION_VIEW);
        this.startActivity(intent); //启动浏览器
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mTokenView.setError(null);

        // Store values at the time of the login attempt.
        String token = mTokenView.getText().toString();
        AppConst.Token = token;
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(token)) {
            focusView = mTokenView;
            cancel = true;
        }
        String appid = ""+AppConst.AppId;

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            RequestUtils.getRequest(AppConst.HostUrl + "person/api/login/appid/" + appid + "/token/" + token, new IHttpResponse() {
                @Override
                public void OnHttpData(String rs) {
                    if(rs==null || rs.isEmpty()){
                        ToastUtil.show(LoginActivity.this, "获取失败,空返回值");
                        return;
                    }
                    try {
                        JSONObject json = new JSONObject(rs);
                        if(json.getInt("code")==0){
                            AppConst.Secret = json.getString("data");
                            dbManager.setConfig(AppConst.KeyToken,AppConst.Token);
                            dbManager.setConfig(AppConst.KeySecret,AppConst.Secret);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            // 登录成功之后finish
                            finish();
                        }else{
                            ToastUtil.show(LoginActivity.this, json.getString("msg"));
                        }
                    }catch (JSONException je){
                        ToastUtil.show(LoginActivity.this, je.getMessage());
                    }
                }

                @Override
                public void OnHttpDataError(IOException e) {

                }
            });
            showProgress(false);

        }
    }




    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

}

