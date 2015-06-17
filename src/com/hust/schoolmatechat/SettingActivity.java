package com.hust.schoolmatechat;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.hust.schoolmatechat.ChatMsgservice.ChatMsgService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.engine.AppEngine;
import com.hust.schoolmatechat.pushedmsgservice.PushedMsgService;

/**
 * Created by hongliang on 2014/7/30.
 */
public class SettingActivity extends Activity {
	private RelativeLayout person_newsInfor_rl, person_common_rl,
			person_permisson_rl, person_finish_rl, person_about_rl;
	private Switch person_vibrate_sw,person_sound_sw;
	Intent intent = new Intent();
	Context mContext;

	/** 用广播的方式，在退出的时候通知数据中心服务，聊天服务，推送服务结束 */
	private Intent processQuitIntent = null;

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
			initSettingActivity();
		}
	};

	private void initSettingActivity() {
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.setting);
		person_newsInfor_rl = (RelativeLayout) findViewById(R.id.person_newsInfor_rl);
		person_newsInfor_rl.setOnClickListener(listener);
		person_permisson_rl = (RelativeLayout) findViewById(R.id.person_permisson_rl);
		person_permisson_rl.setOnClickListener(listener);
		person_permisson_rl.setVisibility(View.GONE);
		person_common_rl = (RelativeLayout) findViewById(R.id.person_common_rl);
		person_common_rl.setOnClickListener(listener);
		person_common_rl.setVisibility(View.GONE);
		person_finish_rl = (RelativeLayout) findViewById(R.id.person_finish_rl);
		person_finish_rl.setOnClickListener(listener);
		person_about_rl = (RelativeLayout) findViewById(R.id.person_about_rl);
		person_about_rl.setOnClickListener(listener);
		person_vibrate_sw = (Switch) findViewById(R.id.person_vibrate_sw);
		person_vibrate_sw.setOnCheckedChangeListener(listener1);
		person_sound_sw = (Switch) findViewById(R.id.person_sound_sw);
		person_sound_sw.setOnCheckedChangeListener(listener1);
		mContext = this;
		String uid = dataCenterManagerService.getUserSelfContactsEntity().getUserAccount();
		AppEngine.getInstance(getBaseContext()).setUID(uid);
		person_vibrate_sw.setChecked(AppEngine.getInstance(getBaseContext())
				.getShaketoRemind());
		person_sound_sw.setChecked(AppEngine.getInstance(getBaseContext())
				.getVoicetoRemind());
		processQuitIntent = new Intent("com.schoolmatechat.processQuitIntent");
	}
	private CompoundButton.OnCheckedChangeListener listener1 = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
			boolean flag;
			switch (compoundButton.getId()) {
			case R.id.person_sound_sw:
				if (b) {
					flag = AppEngine.getInstance(getBaseContext())
							.setVoicetoRemind(true);
				} else {
					flag = AppEngine.getInstance(getBaseContext())
							.setVoicetoRemind(false);
				}
				break;
			case R.id.person_vibrate_sw:
				if (b) {
					flag = AppEngine.getInstance(getBaseContext())
							.setShaketoRemind(true);

				} else {
					flag = AppEngine.getInstance(getBaseContext())
							.setShaketoRemind(false);
				}
				break;
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*
		 * 连接数据中心管理服务
		 */
		final Intent dataCenterManagerIntent = new Intent(this,
				DataCenterManagerService.class);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
				Context.BIND_ABOVE_CLIENT);
		super.onCreate(savedInstanceState);
	}

	public OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			switch (view.getId()) {
			case R.id.person_newsInfor_rl:
				intent.setClass(SettingActivity.this, NewsInforActivity.class);
				startActivity(intent);
				break;
			case R.id.person_common_rl:
				Toast.makeText(SettingActivity.this, "通用设置尚未推出",
						Toast.LENGTH_SHORT).show();
				break;
				
			case R.id.person_finish_rl:
				Dialog dialog = new AlertDialog.Builder(SettingActivity.this)
						.setTitle("退出窗友")
						.setMessage("确认退出窗友吗？")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent it = new Intent();
										setResult(1, it);
										SharedPreferences prefs = PreferenceManager
												.getDefaultSharedPreferences(SettingActivity.this);
										prefs.edit().putString("AUTO", "no")
												.commit();
										//账号可以记住，不必清空
										prefs.edit().putString("PASS", "")
												.commit();

										// 用户退出，底层服务禁止自动连接用户账号
										dataCenterManagerService
												.setUserClickedQuitButton(true);

										try {
											// 聊天服务正常退出
											dataCenterManagerService
													.quitLastTigaseConnection();
										} catch (Exception e) {
											e.printStackTrace();
										}

										// //发广播通知服务结束
										// try {
										// mContext.sendBroadcast(processQuitIntent);
										// } catch (Exception e) {
										// e.printStackTrace();
										// }

										try {
											// 停止数据中心服务
											final Intent intent = new Intent(
													mContext,
													DataCenterManagerService.class);
											stopService(intent);
										} catch (Exception e) {
											e.printStackTrace();
										}

										try {
											// 关闭聊天服务
											Intent intent = new Intent(
													mContext,
													ChatMsgService.class);
											stopService(intent);
										} catch (Exception e) {
											e.printStackTrace();
										}

										try {
											Intent intent = new Intent(
													mContext,
													PushedMsgService.class);
											stopService(intent);
										} catch (Exception e) {
											e.printStackTrace();
										}

										try {
											finish();// 只能结束当前的Activity,会调用onDestroy
										} catch (Exception e) {
											e.printStackTrace();
										}
										// 在后台进程中结束掉整个进程，否则界面会闪
										// 当前处理，聊天服务退出就结束掉整个进程
										// System.exit(0);// 结束整个进程
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).create();
				dialog.show();

				break;
			case R.id.person_about_rl:
				intent.setClass(SettingActivity.this, AboutActivity.class);
				startActivity(intent);
				break;

			}

		}
	};

	@Override
	public void onDestroy() {
		// add 取消绑定 ibind
		unbindService(dataCenterManagerIntentConn);
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
