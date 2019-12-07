package com.zhiyi.ukafu.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;

import java.io.File;

/**
 * 类：SystemProgramUtils 系统程序适配帮助类
 * 1. 拍照
 * 2. 相册
 * 3. 裁切
 * 4. apk安装
 * 作者： qxc
 * 日期：2018/2/23.
 */
public class SystemProgramUtils {
    public static final int REQUEST_CODE_PAIZHAO = 1;
    public static final int REQUEST_CODE_ZHAOPIAN = 2;
    public static final int REQUEST_CODE_CAIQIE = 3;
    public static final int REQUEST_CODE_PERMISSION = 4;

    /**
     * 打开相机拍照
     */
    public static void paizhao(Activity activity, File outputFile){
        Intent intent = new Intent();
        intent.setAction("android.media.action.IMAGE_CAPTURE");
        intent.addCategory("android.intent.category.DEFAULT");
        Uri uri = FileProviderUtils.uriFromFile(activity, outputFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        activity.startActivityForResult(intent, REQUEST_CODE_PAIZHAO);
    }

    /**
     * 打开相册
     */
    public static void zhaopian(Activity activity){
        boolean permission_readStorage  = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission_readStorage = PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        }else{
            PackageManager pm = activity.getPackageManager();
            String pkgName =  activity.getPackageName();
            permission_readStorage = (PackageManager.PERMISSION_GRANTED ==
                    pm.checkPermission("android.permission.READ_EXTERNAL_STORAGE",pkgName));
        }
        if(!permission_readStorage){
            String[] mPermissionList = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(mPermissionList,REQUEST_CODE_PERMISSION);
            }else{
                ActivityCompat.requestPermissions(activity, mPermissionList, REQUEST_CODE_PERMISSION);
            }
            return;
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.PICK");
        intent.addCategory("android.intent.category.DEFAULT");
        activity.startActivityForResult(intent, REQUEST_CODE_ZHAOPIAN);
    }

    /**
     * 打开图片裁切
     */
    public static void Caiqie(Activity activity, Uri uri, File outputFile) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        FileProviderUtils.setIntentDataAndType(activity, intent, "image/*", uri, true);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        //return-data为true时，直接返回bitmap，可能会很占内存，不建议，小米等个别机型会出异常！！！
        //所以适配小米等个别机型，裁切后的图片，不能直接使用data返回，应使用uri指向
        //裁切后保存的URI，不属于我们向外共享的，所以可以使用fill://类型的URI
        Uri outputUri = Uri.fromFile(outputFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        activity.startActivityForResult(intent, REQUEST_CODE_CAIQIE);
    }

}
