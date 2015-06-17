package com.hust.schoolmatechat;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.engine.AppEngine;

/**
 * Created by hongliang on 2014/8/1.
 */
public class NewsInforActivity extends Activity {
	private Switch antiNoise_sw, contentShow_sw, person_vibrate_sw,
			person_sound_sw, stillReceive_sw;
	private boolean flag;
//	GetDataFromConfigurationTable getDataFromConfigurationTable = new Configuration(
//			NewsInforActivity.this);
	ContentValues values = new ContentValues();

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

			initNewsInfoActivity();
		}
	};

	@Override
	public void onDestroy() {
		// add 取消绑定 ibind
		unbindService(dataCenterManagerIntentConn);
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * 连接数据中心管理服务
		 */
		final Intent dataCenterManagerIntent = new Intent(this,
				DataCenterManagerService.class);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
				Context.BIND_ABOVE_CLIENT);
	}

	private void initNewsInfoActivity() {
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.newsinfor_layout);
		antiNoise_sw = (Switch) findViewById(R.id.person_antiNoise_sw);
		antiNoise_sw.setOnCheckedChangeListener(listener);
		contentShow_sw = (Switch) findViewById(R.id.person_contentShow_sw);
		contentShow_sw.setOnCheckedChangeListener(listener);
		person_vibrate_sw = (Switch) findViewById(R.id.person_vibrate_sw);
		person_vibrate_sw.setOnCheckedChangeListener(listener);
		person_sound_sw = (Switch) findViewById(R.id.person_sound_sw);
		person_sound_sw.setOnCheckedChangeListener(listener);
		stillReceive_sw = (Switch) findViewById(R.id.person_stillReceive_sw);
		stillReceive_sw.setOnCheckedChangeListener(listener);
		antiNoise_sw.setEnabled(false);
		contentShow_sw.setEnabled(false);
		String uid = dataCenterManagerService.getUserSelfContactsEntity().getUserAccount();
		AppEngine.getInstance(getBaseContext()).setUID(uid);
		antiNoise_sw.setChecked(AppEngine.getInstance(getBaseContext())
				.getNightAntiDisturb());
		stillReceive_sw.setChecked(AppEngine.getInstance(getBaseContext())
				.getRemindofMessage());
		person_vibrate_sw.setChecked(AppEngine.getInstance(getBaseContext())
				.getShaketoRemind());
		person_sound_sw.setChecked(AppEngine.getInstance(getBaseContext())
				.getVoicetoRemind());

	}

	private CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
			switch (compoundButton.getId()) {
			case R.id.person_antiNoise_sw:
				if (b) {
					flag = AppEngine.getInstance(getBaseContext())
							.setNightAntiDisturb(true);
					Toast.makeText(NewsInforActivity.this, "夜间勿扰模式尚未推出",
							Toast.LENGTH_SHORT).show();
				} else {
					flag = AppEngine.getInstance(getBaseContext())
							.setNightAntiDisturb(false);

					Toast.makeText(NewsInforActivity.this, "夜间勿扰模式尚未推出",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.person_stillReceive_sw:
				if (b) {
					flag = AppEngine.getInstance(getBaseContext())
							.setRemindofMessage(true);
					// Toast.makeText(NewsInforActivity.this,"退出仍接收开"+flag,Toast.LENGTH_SHORT).show();

				} else {
					flag = AppEngine.getInstance(getBaseContext())
							.setRemindofMessage(false);
					// Toast.makeText(NewsInforActivity.this,"退出仍接收关"+flag,Toast.LENGTH_SHORT).show();
				}
				break;
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
			case R.id.person_contentShow_sw:
				if (b) {

					Toast.makeText(NewsInforActivity.this, "通知不显示内容尚未推出",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(NewsInforActivity.this, "通知不显示内容尚未推出",
							Toast.LENGTH_SHORT).show();
				}
				break;
			}

		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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