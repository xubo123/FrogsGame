package com.hust.schoolmatechat;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.engine.CYLog;

public class NotificationReceiver extends Activity {
	
	/** 对接数据中心服务 */
	private DataCenterManagerService dataCenterManagerService;
	ServiceConnection dataCenterManagerIntentConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			dataCenterManagerService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// 返回一个MsgService对象
			dataCenterManagerService = ((DataCenterManagerBiner) service)
					.getService();

			initNotificationReceiver();
		}
	};
	
	public void initNotificationReceiver() {
		dataCenterManagerService.setNumflag(0);
		
		if(isRunningForeground (this)){
			Intent intent = new Intent(this, MainActivity.class);  
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		}else{
			SharedPreferences prefs;
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
			Intent intent = new Intent(this, LoginActivity.class); 
			intent.putExtra("USERNAME", prefs.getString("USERNAME", "username"));
			intent.putExtra("PASS", prefs.getString("PASS", "pass"));
			startActivity(intent);
		}
		finish();
	}

	@Override
	public void onDestroy() {
		// add 取消绑定 ibind
		unbindService(dataCenterManagerIntentConn);
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*
		 * 连接数据中心管理服务
		 */
		final Intent dataCenterManagerIntent = new Intent(this,
				DataCenterManagerService.class);
		this.startService(dataCenterManagerIntent);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
				Context.BIND_ABOVE_CLIENT);
		
		super.onCreate(savedInstanceState);
	}
	
	private boolean isRunningForeground (Context context)  
	{  
	    ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);  
	    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;  
	    String currentPackageName = cn.getPackageName();  
	    if(!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(getPackageName()))  
	    {  
	    	CYLog.e("====","前台");
	        return true ;  
	    } else{ CYLog.e("====","后台");}
	   
	    return false ;  
	}  
}
