package com.zhiyi.ukafu;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

//import com.qcloud.image.ImageClient;
//import com.qcloud.image.request.GeneralOcrRequest;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.zhiyi.ukafu.qrcode.QrCli;
import com.zhiyi.ukafu.util.LogUtil;
import com.zhiyi.ukafu.util.RequestData;
import com.zhiyi.ukafu.util.RequestUtils;
import com.zhiyi.ukafu.util.StringUtils;
import com.zhiyi.ukafu.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


public class QrcodeUploadActivity extends AppCompatActivity {
    private EditText eMoney;
    private EditText eName;
    private EditText txt_url;
    private Button upButton;
    private String qrcode;
    private String currentPhotoString;
    private Handler handler;
    private boolean forUpload;
    //    private ImageClient imageClient;
//    private QrCodeData qrData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        setContentView(R.layout.activity_qrcode_upload);
        Intent intent = getIntent();
        currentPhotoString = intent.getStringExtra("file");
        forUpload = intent.hasExtra("forupload")?intent.getBooleanExtra("forupload",false):true;
        txt_url = findViewById(R.id.txt_url);
        ImageView img = findViewById(R.id.image_view);
        img.setImageURI(Uri.fromFile(new File(currentPhotoString)));
        eMoney = findViewById(R.id.txt_money);
        eName = findViewById(R.id.txt_name);
        upButton = findViewById(R.id.btn_upload);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toUpload();
            }
        });
        loadImage();
    }

    private void toUpload(){
        String money = eMoney.getText().toString();
        String name = eName.getText().toString();
        if(qrcode == null || qrcode.isEmpty()){
            ToastUtil.show(QrcodeUploadActivity.this,"二维码不支持");
            return;
        }
        if(money == null || money.isEmpty()){
            ToastUtil.show(QrcodeUploadActivity.this,"金额必须填写,如果金额不固定,请填写0");
            return;
        }
        if(name == null || name.isEmpty()){
            ToastUtil.show(QrcodeUploadActivity.this,"名称必须填写");
            return;
        }
        try{
            Float.parseFloat(money);
        }catch (NumberFormatException e){
            LogUtil.e("数字格式不对,"+money);
            return;
        }
        RequestData post = RequestData.newInstance(AppConst.NetTypeQrcodeUpload);
        try {
            if(!StringUtils.isEmpty(name)){
                post.put("name",name);
            }
            post.put("money",money);
            post.put("code",qrcode);
        } catch (JSONException e) {
        }
        if(forUpload){
            RequestUtils.post(AppConst.NoticeUrl, post, new HttpJsonResponse() {
                @Override
                protected void onJsonResponse(JSONObject data) {
                    Intent intent = new Intent();
                    //把返回数据存入Intent
                    intent.putExtra("result", data.toString());
                    //设置返回数据
                    setResult(RESULT_OK, intent);
                    //关闭Activity
                    finish();
                }
            });
        }else{
            Intent intent = new Intent();

            //把返回数据存入Intent
            intent.putExtra("code", qrcode);
            intent.putExtra("money", money);
            intent.putExtra("name", name);
            //设置返回数据
            setResult(RESULT_OK, intent);
            //关闭Activity
            finish();
        }

    }

    private void loadImage(){
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoString);
        if (bitmap == null) {
            LogUtil.e("bitmap is null" + currentPhotoString);
            String[] mPermissionList = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, mPermissionList, 100);
            return;
        }
//        LuminanceSource source = new BufferedImageLuminanceSource(bitmap);
//        Map<DecodeHintType,Object> hints = new LinkedHashMap<>();
//        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
//        hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
//
//        // 设置继续的字符编码格式为UTF8
//        hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
//
//        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
//        Result rs = null;
//        try {
//            rs = new QRCodeReader().decode(binaryBitmap,hints);
//        } catch (NotFoundException e) {
//            return;
//        } catch (FormatException e) {
//        } catch (ChecksumException e) {
//        }
//        qrcode = "";
//        if (rs != null) {
//            String txt = rs.getText();
//            Log.w(LOG_TAG, txt);
//            if (txt != null) {
//                readTxt(currentPhotoString, txt);
//            }else{
//                ToastUtil.show(this,"二维码解析失败");
//            }
//        }else{
//            Log.w(LOG_TAG, "decoder result is null");
//            ToastUtil.show(this,"二维码解析失败");
//        }
        qrcode = "";
        CodeUtils.analyzeBitmap(currentPhotoString, new CodeUtils.AnalyzeCallback() {
            @Override
            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                readTxt(result);
            }

            @Override
            public void onAnalyzeFailed() {
                QrCli.upload("https://upload.api.cli.im/upload.php?kid=cliim", new File(currentPhotoString),
                        new HttpJsonResponse("status",1,"info","info"){
                    @Override
                    protected void onJsonResponse(JSONObject data) {
                        try {
                            readTxt(data.getJSONArray("data").getString(0));
                        } catch (JSONException e) {
                           LogUtil.e("json error",e);
                        }
                    }
                });

                LogUtil.e("二维码解析失败:"+currentPhotoString);
            }
        });

    }

    private void readTxt(final String url) {
//服务器识别类型.客户端获取二维码足以
//        if (url.toUpperCase().startsWith("WXP://")) {
//            type = QrCodeData.TYPE_WX;
//        } else if (url.toUpperCase().startsWith("HTTPS://QR.ALIPAY.COM")) {
//            type = QrCodeData.TYPE_ALI;
//        }else if(url.toLowerCase().startsWith("https://nxt.nongxinyin.com")){
//            type = QrCodeData.TYPE_JXYMF;
//        } else {
//            Log.w(LOG_TAG, "qrcode is not enable");
//            Toast.makeText(this, "不支持选择的二维码,请选择支付宝/微信收款码", Toast.LENGTH_SHORT).show();
//            return;
//        }
        qrcode = url;
        if(StringUtils.isEmpty(url)){
            Toast.makeText(this, "无法识别二维码", Toast.LENGTH_SHORT).show();
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                txt_url.setText(qrcode);
            }
        };
        handler.post(runnable);
        //文字识别,取消,没钱,API要钱
        // Android 4.0 之后不能在主线程中请求HTTP请求
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                qrData = new QrCodeData();
//                qrData.money = "0";
//                qrData.name = "";
//                do {
//                    String bucketName = "qrcode-1252008836";
//                    if (imageClient == null) {
//                        String appId = "1252008836";
//                        String secretId = "AKIDUJ5ZBdw7MivIfx7C82mLFe9aEiJksLxX";
//                        String secretKey = "BB0ra9oOEwcOaeXdbTAg4LRMXouqgT6Y";
//                        imageClient = new ImageClient(appId, secretId, secretKey, ImageClient.NEW_DOMAIN_recognition_image_myqcloud_com);
//                    }
//                    File tagImage;
//                    try {
//                        tagImage = new File(file);
//                    } catch (Exception ex) {
//                        Log.w(LOG_TAG, ex);
//                        break;
//                    }
//
//                    GeneralOcrRequest tagReq = new GeneralOcrRequest(bucketName, tagImage);
//
//                    String ret = imageClient.generalOcr(tagReq);
//                    try {
//                        JSONObject jsonObject = new JSONObject(ret);
//                        int code = jsonObject.getInt("code");
//                        if (code != 0) {
//                            Log.w(LOG_TAG, "image ORC code is " + code);
//                            Log.w(LOG_TAG, ret);
//                            break;
//                        }
//                        if (!jsonObject.has("data")) {
//                            Log.w(LOG_TAG, "image ORC no data ");
//                            break;
//                        }
//                        JSONArray array = jsonObject.getJSONObject("data").getJSONArray("items");
//                        if (array == null) {
//                            Log.w(LOG_TAG, "image ORC no items ");
//                            break;
//                        }
//                        Pattern pMoney = Pattern.compile("￥([\\d\\.]+)");
//                        if (type == QrCodeData.TYPE_WX) {
//                            Pattern pWeixinNick = Pattern.compile("(\\S+)\\(\\*");
//                            for (int i = 0; i < array.length(); i++) {
//                                JSONObject item = array.getJSONObject(i);
//                                if (item.has("itemstring")) {
//                                    String value = item.getString("itemstring");
//                                    Matcher m = pMoney.matcher(value);
//                                    if (m.find()) {
//                                        qrData.money = m.group(1);
//                                        continue;
//                                    }
//                                    m = pWeixinNick.matcher(value);
//                                    if (m.find()) {
//                                        qrData.name = m.group(1);
//                                        continue;
//                                    }
//                                }
//                            }
//                        } else if (type == QrCodeData.TYPE_ALI) {
//                            Pattern aliNick = Pattern.compile("支付宝|LIPAY");
//                            for (int i = 0; i < array.length(); i++) {
//                                JSONObject item = array.getJSONObject(i);
//                                if (item.has("itemstring")) {
//                                    String value = item.getString("itemstring");
//                                    Matcher m = pMoney.matcher(value);
//                                    if (m.find()) {
//                                        qrData.money = m.group(1);
//                                        continue;
//                                    }
//                                    m = aliNick.matcher(value);
//                                    if (m.find()) {
//                                        continue;
//                                    } else {
//                                        qrData.name = value;
//                                    }
//                                }
//                            }
//                        }else if(type == QrCodeData.TYPE_JXYMF){
//                            Pattern aliNick = Pattern.compile("订单金额");
//                            for (int i = 0; i < array.length(); i++) {
//                                JSONObject item = array.getJSONObject(i);
//                                if (item.has("itemstring")) {
//                                    String value = item.getString("itemstring");
//                                    Matcher m = pMoney.matcher(value);
//                                    if (m.find()) {
//                                        qrData.money = m.group(1);
//                                        continue;
//                                    }
//                                    m = aliNick.matcher(value);
//                                    if (m.find()) {
//                                        continue;
//                                    } else {
//                                        qrData.name = value;
//                                    }
//                                }
//                            }
//                        }
//                        if (!qrData.name.matches("^\\S+$")) {
//                            qrData.name = "";
//                        }
//                        qrData.type = type;
//                    } catch (JSONException e) {
//                        Log.w(LOG_TAG, e);
//                    }
//                    Log.i(LOG_TAG, qrData.money + qrData.name);
//                }while (false);
//                Runnable runnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        eMoney.setText(qrData.money);
//                        if(!qrData.name.isEmpty()){
//                            eName.setText(qrData.name);
//                        }
//                    }
//                };
//                handler.post(runnable);
//            }
//        }).start();
    }
}
