package com.zhiyi.ukafu;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zhiyi.ukafu.util.RequestUtils;
import com.zhiyi.ukafu.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MerchantActivity extends AppCompatActivity {
    private Button btn_copy;
    private Button btn_reset;
    private Button btn_unite;
    private TextView txt_Merchant;
    private TextView txt_Secret;
    private String merchantId;
    private String Secret;
    private Handler uiHandler;
    private TextView txt_uniteId;
    private Button btn_admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHandler = new Handler();
        setContentView(R.layout.activity_merchant);
        btn_copy = findViewById(R.id.button_copy);
        btn_reset = findViewById(R.id.button_reset);
        btn_unite = findViewById(R.id.button_unite);
        btn_admin = findViewById(R.id.button_admin);
        txt_Merchant = findViewById(R.id.merchant_id);
        txt_Secret = findViewById(R.id.merchant_secret);
        txt_uniteId = findViewById(R.id.text_unite);
        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copySecret();
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(MerchantActivity.this).setTitle("确定要重置?").setIcon(R.drawable.icon).
                        setPositiveButton(R.string.action_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RequestUtils.getRequest(AppConst.authUrl("person/merchant/resetSecret"), new IHttpResponse() {

                                    @Override
                                    public void OnHttpData(String data) {
                                        handleMessage(data);
                                    }

                                    @Override
                                    public void OnHttpDataError(IOException e) {

                                    }
                                });

                            }
                        }).show();
                dialog.setCancelable(true);
            }
        });
        btn_unite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String uniteId = txt_uniteId.getText().toString();
                if(merchantId == null){
                    ToastUtil.show(MerchantActivity.this, "请等待商户ID获取");
                    return;
                }
                if (merchantId.equals(uniteId)) {
                    ToastUtil.show(MerchantActivity.this, "目标商户ID是自己");
                    return;
                }
                AlertDialog dialog = new AlertDialog.Builder(MerchantActivity.this).setTitle("关联之后.本商户将注销,不再独立收款,确定要关联吗?").setIcon(R.drawable.icon).
                        setPositiveButton(R.string.action_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RequestUtils.getRequest(AppConst.authUrl("person/merchant/bind") + "&unite=" + uniteId, new IHttpResponse() {

                                    @Override
                                    public void OnHttpData(String data) {
                                        handleMessage(data);
                                    }

                                    @Override
                                    public void OnHttpDataError(IOException e) {

                                    }
                                });

                            }
                        }).show();
                dialog.setCancelable(true);
            }
        });

        btn_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setData(Uri.parse(AppConst.HostUrl + "app/start/index.html#/token=" + AppConst.Token + "/appid=" + AppConst.AppId));//Url 就是你要打开的网址
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent); //启动浏览器
            }
        });

        RequestUtils.getRequest(AppConst.authUrl("person/merchant/getMerchant"), new IHttpResponse() {

            @Override
            public void OnHttpData(String data) {
                handleMessage(data);
            }

            @Override
            public void OnHttpDataError(IOException e) {

            }
        });
    }

    private void copySecret() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", "商户ID: " + merchantId + " ,密钥: " + Secret);
        cm.setPrimaryClip(mClipData);
        Toast.makeText(this, "复制成功", Toast.LENGTH_LONG).show();
    }


    private void handleMessage(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        Log.i(AppConst.TAG_LOG, message);

        JSONObject json;
        try {
            json = new JSONObject(message);
            int code = json.getInt("code");
            if (code == 0) {
                json = json.getJSONObject("data");
                merchantId = json.getString("merchantid");
                Secret = json.getString("secret");
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        txt_Merchant.setText(merchantId);
                        txt_Secret.setText(Secret);
                    }
                };
                uiHandler.post(runnable);

            } else {
                final String emsg = json.getString("msg");
                Log.w(AppConst.TAG_LOG, emsg);
                ToastUtil.show(this, emsg);
                if (code == 99) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            btn_copy.setEnabled(false);
                            btn_reset.setEnabled(false);
                            btn_unite.setEnabled(false);
                            new AlertDialog.Builder(MerchantActivity.this)
                                    .setTitle("注意")
                                    .setIcon(R.drawable.icon)
                                    .setMessage(emsg).show();
                        }
                    };
                    uiHandler.post(runnable);
                }
            }

        } catch (JSONException e) {
            Log.w(AppConst.TAG_LOG, e);
        }
    }
}
