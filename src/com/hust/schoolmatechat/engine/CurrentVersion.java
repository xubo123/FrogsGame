package com.hust.schoolmatechat.engine;
import android.content.Context;

import com.hust.schoolmatechat.R;
public class CurrentVersion {
	private static final String TAG = "Config";
	public static final String appPackName = "com.hust.schoolmatechat";
	public static int getVerCode(Context context) {
		int verCode = -1;
		try{
			verCode = context.getPackageManager().getPackageInfo(appPackName, 0).versionCode;	
		}catch(Exception e){
			CYLog.e(TAG, e.getMessage());
		}
		return verCode;
	}
	public static String getVerName(Context context){
		String verName = "";
		try{
			verName = context.getPackageManager().getPackageInfo(appPackName, 0).versionName;
		}catch(Exception e){
			CYLog.e(TAG, e.getMessage());
		}
		return verName;
	}
	public static String getAppName(Context context){
		String appName = context.getResources().getText(R.string.app_name).toString();
		return appName;
		}
}
