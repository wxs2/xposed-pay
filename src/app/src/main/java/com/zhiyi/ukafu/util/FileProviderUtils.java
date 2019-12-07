package com.zhiyi.ukafu.util;

/**
 * Created by Administrator on 2018/10/6.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import java.io.File;

/**
 * 类：FileProviderUtils Uri适配帮助类
 * 从APP向外共享的文件URI时，必须使用该类进行适配，否则在7.0以上系统，会报错：FileUriExposedException(文件Uri暴露异常)
 * 作者： qxc
 * 日期：2018/2/23.
 */
public class FileProviderUtils {
    /**
     * 从文件获得URI
     * @param context 上下文
     * @param file 文件
     * @return 文件对应的URI
     */
    public static Uri uriFromFile(Context context, File file) {
        Uri fileUri;
        //7.0以上进行适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String p = context.getPackageName() + ".FileProvider";
            fileUri = FileProvider.getUriForFile(
                    context,
                    p,
                    file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }

    /**
     * 设置Intent的data和类型，并赋予目标程序临时的URI读写权限
     * @param context 上下文
     * @param intent 意图
     * @param type 类型
     * @param file 文件
     * @param writeAble 是否赋予可写URI的权限
     */
    public static void setIntentDataAndType(Activity context,
                                            Intent intent,
                                            String type,
                                            File file,
                                            boolean writeAble) {
        //7.0以上进行适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setDataAndType(uriFromFile(context, file), type);
            //临时赋予读写Uri的权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        } else {
            intent.setDataAndType(Uri.fromFile(file), type);
        }
    }

    /**
     * 设置Intent的data和类型，并赋予目标程序临时的URI读写权限
     * @param context 上下文
     * @param intent 意图
     * @param type 类型
     * @param fileUri 文件uri
     * @param writeAble 是否赋予可写URI的权限
     */
    public static void setIntentDataAndType(Context context,
                                            Intent intent,
                                            String type,
                                            Uri fileUri,
                                            boolean writeAble) {
        //7.0以上进行适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setDataAndType(fileUri, type);
            //临时赋予读写Uri的权限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        } else {
            intent.setDataAndType(fileUri, type);
        }
    }
}