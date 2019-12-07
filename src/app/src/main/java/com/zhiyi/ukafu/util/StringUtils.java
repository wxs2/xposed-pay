package com.zhiyi.ukafu.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class StringUtils {

	public static boolean isEmpty(String args) {
		if(args == null){
			return true;
		}
		if(args.trim().length()<1){
			return true;
		}
		return false;
	}

	/**
	 * 截取两字符之间的字符串，str 和 start不能为null或""
	 */
	public static String getCutOutString(String str, String start, String endwith) {
		if (TextUtils.isEmpty(str)) {
			return "";
		}
		String result = "";
		if (str.contains(start)) {
			String s1 = str.split(start)[1];
			if (endwith == null || "".equals(endwith)) {
				result = s1;
			} else {
				String s2[] = s1.split(endwith);
				if (s2 != null) {
					result = s2[0];
				}
			}
		}
		return result;

	}

	public static byte[] getBytes(String filePath) {
		byte[] buffer = null;
		try {
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer;
	}

	public static void getFile(byte[] bfile, String filePath, String fileName) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		File file = null;
		try {
			File dir = new File(filePath);
			if (!dir.exists() && !dir.isDirectory()) {// �ж��ļ�Ŀ¼�Ƿ����?
				dir.mkdirs();
			}
			file = new File(filePath + "\\" + fileName);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bfile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}


	public static String getphonenum(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return "";
		}
		String phonenum = tm.getLine1Number();
		return phonenum;
	}

	public static String getTextCenter(String text,String begin,String end){
		try {
			int b=text.indexOf(begin)+begin.length();
			int e=text.indexOf(end,b);
			return text.substring(b,e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
	}
}