package com.hust.schoolmatechat.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.hust.schoolmatechat.R;

public final class AppEngine extends Observable {
	private static final String TAG = "AppEngine";
	private static AppEngine mInstance = new AppEngine();
	private static String UID = null;
	private static Context mActivityContext = null;
	private static SharedPreferences prefs;
	private boolean canNotify = true;

	private AppEngine() {
	}

	public static AppEngine getInstance(Context context) {
		setActivityContext(context);
		return mInstance;
	}

	/**
	 * 获取Activity的Context
	 * 
	 * @return
	 */
	public Context getActivityContext() {
		return mActivityContext;
	}

	public static void setActivityContext(Context context) {
		mActivityContext = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(mActivityContext);
	}

	public void setUID(String uid) {
		UID = uid;
	}

	/**
	 * 释放activity
	 * 
	 * @param context
	 */
	public void releaseActivityContext(Context context) {
		if (mActivityContext == context) {
			mActivityContext = null;
		}
	}

	public void onNewsCome() {
		if (canNotify) {
			Boolean indexVoice = AppEngine.getInstance(mActivityContext)
					.getVoicetoRemind();
			Boolean indexShock = AppEngine.getInstance(mActivityContext)
					.getShaketoRemind();
			AudioManager volMgr = (AudioManager) mActivityContext
					.getSystemService(Context.AUDIO_SERVICE);
			if (volMgr.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
				indexVoice = false;
			} else if (volMgr.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {

			} else {// AudioManager.RINGER_MODE_NORMAL//常规模式

			}

			if (indexVoice) {
				MediaPlayer mediaPlayer = new MediaPlayer();
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				/*
				 * Uri notification =
				 * RingtoneManager.getDefaultUri(RingtoneManager
				 * .TYPE_NOTIFICATION); Ringtone r =
				 * RingtoneManager.getRingtone(mContext, notification);
				 * r.play();
				 */
				AssetFileDescriptor file = mActivityContext.getResources()
						.openRawResourceFd(R.raw.msn);
				try {
					mediaPlayer.setDataSource(file.getFileDescriptor(),
							file.getStartOffset(), file.getLength());
					file.close();
					mediaPlayer.prepare();
					mediaPlayer.start();
				} catch (IOException ioe) {
					mediaPlayer = null;
				}

			}
			if (indexShock) {
				Vibrator vibrator = (Vibrator) mActivityContext
						.getSystemService(Service.VIBRATOR_SERVICE);
				vibrator.vibrate(800);
			}
			canNotify = false;
			 new Handler().postDelayed(new Runnable(){  
			     public void run() {  
			     //execute the task 
			    	 canNotify = true;
			     }  
			  }, 2000); 
		}
	}



	// public static void onfinish() {
	// Boolean index = AppEngine.getInstance(mActivityContext)
	// .getRemindofMessage();
	//
	// if (index) {
	// if(!isDataCenterManagerServiceWorked())
	// mActivityContext.startService(new
	// Intent(mActivityContext,DataCenterManagerService.class));
	// //mActivityContext.startService(new
	// Intent(mActivityContext,ChatService.class));
	// }else{
	// //mActivityContext.stopService(new
	// Intent(mActivityContext,MQTTService.class));
	// //mActivityContext.stopService(new
	// Intent(mActivityContext,ChatService.class));
	// mActivityContext.stopService(new
	// Intent(mActivityContext,DataCenterManagerService.class));
	// //mActivityContext.stopService(new
	// Intent(mActivityContext,ChatMsgService.class));
	// //mActivityContext.stopService(new
	// Intent(mActivityContext,PushedMsgService.class));
	//
	// }
	//
	// }

	/**
	 * 聊天服务是否还在
	 * 
	 * @return
	 */
	public boolean isChatMsgServiceWorked() {
		ActivityManager myManager = (ActivityManager) mActivityContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
				.getRunningServices(Integer.MAX_VALUE);
		for (int i = 0; i < runningService.size(); i++) {
			/*
			 * CYCYLog.d("===", runningService.get(i).service.getClassName()
			 * .toString());
			 */
			if (runningService.get(i).service
					.getClassName()
					.toString()
					.equals("com.hust.schoolmatechat.ChatMsgservice.ChatMsgService")) {
				CYLog.i(TAG, "isChatMsgServiceworked");
				return true;
			}
		}
		CYLog.i(TAG, "no ChatMsgService");
		return false;
	}

	/**
	 * 数据中心服务是否还在
	 * 
	 * @return
	 */
	public boolean isDataCenterWorked() {
		ActivityManager myManager = (ActivityManager) mActivityContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
				.getRunningServices(Integer.MAX_VALUE);
		for (int i = 0; i < runningService.size(); i++) {
			if (runningService.get(i).service
					.getClassName()
					.toString()
					.equals("com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService")) {
				CYLog.i(TAG, "isDataCenterWorked");
				return true;
			}
		}
		
		
		CYLog.i(TAG, "no DataCenterWorked");
		return false;
	}

	/**
	 * 推送服务是否还在
	 * 
	 * @return
	 */
	public boolean isPushServiceWorked() {
		ActivityManager myManager = (ActivityManager) mActivityContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
				.getRunningServices(Integer.MAX_VALUE);
		for (int i = 0; i < runningService.size(); i++) {
			if (runningService.get(i).service
					.getClassName()
					.toString()
					.equals("com.hust.schoolmatechat.pushedmsgservice.PushedMsgService")) {
				CYLog.i(TAG, "isPushServiceWorked");
				return true;
			}
		}
		CYLog.i(TAG, "no PushServiceWorked");
		return false;
	}

	/**
	 * 返回提示音类型
	 */
	public String getKindofVoice() {
		String type = prefs.getString(UID + "KindofVoice", "0");
		return type;
	}

	/**
	 * 设置提示音类型
	 * 
	 * @param voiceId
	 */
	public boolean setKindofVoice(String voiceId) {

		prefs.edit().putString(UID + "KindofVoice", voiceId).commit();
		return true;
	}

	/**
	 * 返回当前皮肤名称
	 */
	public String getTheme() {
		String skinID = prefs.getString(UID + "Theme", "0");
		return skinID;
	}

	/**
	 * 设置皮肤
	 * 
	 * @param skinId
	 */
	public boolean setTheme(String skinId) {
		prefs.edit().putString(UID + "Theme", skinId).commit();
		return true;
	}

	/**
	 * 返回夜间防骚扰模式
	 */
	public Boolean getNightAntiDisturb() {
		Boolean mode = prefs.getBoolean(UID + "NightAntiDisturb", false);
		return mode;
	}

	/**
	 * 设置夜间防骚扰模式
	 * 
	 * @param mode
	 */
	public boolean setNightAntiDisturb(boolean mode) {
		prefs.edit().putBoolean(UID + "NightAntiDisturb", mode).commit();
		return true;
	}

	/**
	 * 返回是否消息提醒
	 */
	public Boolean getRemindofMessage() {
		Boolean mode = prefs.getBoolean(UID + "RemindofMessage", false);
		return mode;
	}

	/**
	 * 设置消息提醒
	 * 
	 * @param mode
	 */
	public boolean setRemindofMessage(boolean mode) {
		prefs.edit().putBoolean(UID + "RemindofMessage", mode).commit();
		return true;
	}

	/**
	 * 返回是否消息提醒闪烁
	 */
	public Boolean getFlashtoRemind() {
		Boolean mode = prefs.getBoolean(UID + "FlashtoRemind", false);
		return mode;
	}

	/**
	 * 设置消息提醒闪烁
	 * 
	 * @param mode
	 */
	public boolean setFlashtoRemind(boolean mode) {
		prefs.edit().putBoolean(UID + "FlashtoRemind", mode).commit();
		return true;
	}

	/**
	 * 返回是否消息提醒
	 */
	public Boolean getShaketoRemind() {
		Boolean mode = prefs.getBoolean(UID + "ShaketoRemind", false);
		return mode;
	}

	/**
	 * 设置消息提醒
	 * 
	 * @param mode
	 */
	public boolean setShaketoRemind(boolean mode) {
		prefs.edit().putBoolean(UID + "ShaketoRemind", mode).commit();
		return true;
	}

	/**
	 * 返回是否声音提示
	 */
	public Boolean getVoicetoRemind() {
		Boolean mode = prefs.getBoolean(UID + "VoicetoRemind", true);
		return mode;
	}

	/**
	 * 设置声音提示
	 * 
	 * @param mode
	 */
	public boolean setVoicetoRemind(boolean mode) {
		prefs.edit().putBoolean(UID + "VoicetoRemind", mode).commit();
		return true;
	}

	/**
	 * 返回是否群消息提醒震动
	 */
	public Boolean getGroupShaketoRemind() {
		Boolean mode = prefs.getBoolean(UID + "GroupShaketoRemind", false);
		return mode;
	}

	/**
	 * 设置群消息提醒震动
	 * 
	 * @param mode
	 */
	public boolean setGroupShaketoRemind(boolean mode) {
		prefs.edit().putBoolean(UID + "GroupShaketoRemind", mode).commit();
		return true;
	}

	/**
	 * 返回是否群消息提醒声音
	 */
	public Boolean getGroupVoicetoRemind() {
		Boolean mode = prefs.getBoolean(UID + "GroupVoicetoRemind", false);
		return mode;
	}

	/**
	 * 设置群消息提醒声音
	 * 
	 * @param mode
	 */
	public boolean setGroupVoicetoRemind(boolean mode) {
		prefs.edit().putBoolean(UID + "GroupVoicetoRemind", mode).commit();
		return true;
	}

}
