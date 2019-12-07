package com.zhiyi.ukafu;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhiyi.ukafu.data.QrCodeData;
import com.zhiyi.ukafu.util.RequestData;
import com.zhiyi.ukafu.util.RequestUtils;
import com.zhiyi.ukafu.util.SystemProgramUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class QrcodeActivity extends AppCompatActivity {

    public final int ARG_TYPE_QRCODELIST = 1;
    public final int ARG_TYPE_ADD = 2;
    public final int ARG_TYPE_DEL = 3;


    private final int PICK_CODE = 2;
    private final int UPLOAD = 3;

    private static String LOG_TAG = "ZYKJ";

    private View mContentView;
    private LinearLayout container;


    private Handler handler;


    private View.OnClickListener readClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            readImg();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_qrcode);

        container = (LinearLayout)findViewById(R.id.container);
        mContentView = findViewById(R.id.fullscreen_content);
        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(readClick);
        handler = new Handler();

        findViewById(R.id.dummy_button).setOnClickListener(readClick);
        RequestData post = RequestData.newInstance(AppConst.NetTypeQrcodeList);
        RequestUtils.post(AppConst.NoticeUrl, post, new HttpJsonResponse() {
            @Override
            protected void onJsonResponse(JSONObject data) {
                handleMessage(data.toString(), ARG_TYPE_QRCODELIST);
            }
        });

    }


    public void readImg() {
//        Intent pic = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(pic, PICK_CODE);

//        Intent intent = new Intent("android.intent.action.GET_CONTENT");
//        intent.putExtra("scale", true);//设置可以缩放
//        intent.putExtra("crop", true);//设置可以裁剪
//        intent.setType("image/*");//设置需要从系统选择的内容：图片
//        //intent.putExtra(MediaStore.EXTRA_OUTPUT, this.imageUri);//设置输出位置
//        startActivityForResult(intent, PICK_CODE);
        SystemProgramUtils.zhaopian(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (intent == null) {
            return;
        }
        if (requestCode == PICK_CODE) {
                Uri uri = intent.getData();
                if (uri == null) {
                    Toast.makeText(this, "目标数据为空", Toast.LENGTH_LONG);
                    return;
                }
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor == null) {
                    Toast.makeText(this, "找不到数据", Toast.LENGTH_LONG);
                    return;
                }
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                if (idx < 0) {
                    Toast.makeText(this, "无法访问相册", Toast.LENGTH_LONG);
                    return;
                }
                String currentPhotoString = cursor.getString(idx);
                cursor.close();

                Intent qIntent = new Intent(QrcodeActivity.this,QrcodeUploadActivity.class);
                qIntent.putExtra("file",currentPhotoString);
                qIntent.putExtra("forupload",true);
                startActivityForResult(qIntent,UPLOAD);
//                Bitmap bitmap = resizePhono(currentPhotoString);
        }else if(requestCode == UPLOAD) {
            String msg = intent.getStringExtra("result");
            handleMessage(msg, ARG_TYPE_ADD);
        }
    }

    /**
     * 压缩图片
     */
//    private Bitmap resizePhono(String currentPhotoString) {
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;//仅仅加载图片
//        BitmapFactory.decodeFile(currentPhotoString, options);
//        if (options.outWidth < 1) {
//            Log.w(LOG_TAG, "image not read" + currentPhotoString);
//            return null;
//        }
//        double radio = Math.max(options.outWidth * 1.0d / 1024f, options.outHeight * 1.0d / 1024f);
//        options.inSampleSize = (int) Math.ceil(radio);
//        options.inJustDecodeBounds = false;
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        return BitmapFactory.decodeFile(currentPhotoString, options);
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                boolean writeExternalStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readExternalStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (grantResults.length > 0 && writeExternalStorage && readExternalStorage) {
                    readImg();
                } else {
                    Toast.makeText(this, "请允许访问相册的权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

//    private ImageClient imageClient;



    private HashMap<String, View> codeMap = new HashMap<>();

    public boolean handleMessage(final String msg, final int arg1) {
        if (msg == null || msg.isEmpty()) {
            return false;
        }
        Runnable runner = new Runnable() {
            @Override
            public void run() {
                try {
                    if (arg1 == ARG_TYPE_QRCODELIST) {
                        JSONObject obj = new JSONObject(msg);
                        JSONArray list = obj.getJSONArray("list");
                        int[] colors = new int[]{R.color.colorPrimary, R.color.colorPrimaryDark};
                        if (list != null && list.length() > 0) {
                            int flag = 0;
                            for (int i = 0; i < list.length(); i++) {
                                View view = addViewItem(list.getJSONObject(i));
                                if (view != null) {
                                    if (Build.VERSION.SDK_INT > 22) {
                                        view.setBackgroundColor(getResources().getColor(colors[++flag % 2], getTheme()));
                                    }
                                }
                            }
                        }
                    } else if (arg1 == ARG_TYPE_ADD) {
                        JSONObject obj = new JSONObject(msg);
                        updateView(obj);
                    }
                } catch (JSONException e) {
                    Log.w("ZYKJ",e.getMessage());
                }
                if (codeMap.size() > 0) {
                    mContentView.setVisibility(View.INVISIBLE);
                    LinearLayout p = (LinearLayout) mContentView.getParent();
                    if(p!=null) {
                        p.removeView(mContentView);
                    }
                }
            }
        };
        handler.post(runner);
        return false;
    }


    private View addViewItem(JSONObject data) {

        try {
            if (!data.has("money_round")) {
                return null;
            }
            Object o = data.get("money_round");
            String money = "" + o;
            if (codeMap.containsKey(money)) {
                updateView(data);
                return null;
            }
            View view = View.inflate(this, R.layout.qrcode, null);

            TextView btn_add = view.findViewById(R.id.qr_money);
            btn_add.setText(money);
            updateText(data, view);
            codeMap.put(money, view);
            container.addView(view);
            return view;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "获取money_round失败" + data.toString());
            Log.w(LOG_TAG, e);
            return null;
        }

    }

    public void updateView(JSONObject data) {
        try {
            if (!data.has("money_round")) {
                return;
            }
            Object o = data.get("money_round");
            String key = "" + o;
            if (data.has("add")) {
                int add = data.getInt("add");
                if (add == 0) {
                    return;
                }
            }
            if (codeMap.containsKey(key)) {
                View view = codeMap.get(key);
                updateText(data, view);
            } else {
                addViewItem(data);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "获取money_round失败" + data.toString());
            Log.w(LOG_TAG, e);
        }
    }

    private void updateText(JSONObject data, View view) throws JSONException {
        String type = data.getString("pay_type");
        TextView textView = view.findViewById(R.id.qr_zfb_num);
        TextView textQrtype = view.findViewById(R.id.qr_type);
        ImageView img = view.findViewById(R.id.img_zfb);
        if (QrCodeData.NAME_ALI.equals(type)) {

        } else if (QrCodeData.NAME_WX.equals(type)) {
            img.setImageResource(R.drawable.wx);
        }else{
            img.setImageResource(R.drawable.ukafu);
        }
        int num = 0;
        if (data.has("count")) {
            num = data.getInt("count");
        } else {
            String numStr = textView.getText().toString();
            num = Integer.parseInt(numStr) + 1;
        }
        textView.setText(num + "");
        textQrtype.setText(type);
    }
}
