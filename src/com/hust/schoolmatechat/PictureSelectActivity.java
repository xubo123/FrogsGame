package com.hust.schoolmatechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.register.HttpupLoad;

public class PictureSelectActivity extends Activity {
	private static final String TAG = "PictureSelectActivity";
	GridView gridView;
	Button btn_confirm;// 确认按钮
	int thePicture = 0;
	View lastView = null;
	String partofUrl;
	boolean ismale = true;
	String accountNum;
	String password;
	private HttpupLoad GetTask;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 3:
				CYLog.i(TAG,"修改结果"
						+ GetTask.getLoaddata().getStrResult());
				try {
					JSONObject json = new JSONObject(GetTask.getLoaddata().getStrResult());
					boolean idString = json.getBoolean("success");
					//上传成功,修改本地数据
					if (idString) {
						ContactsEntity contactsEntity = dataCenterManagerService
								.getUserSelfContactsEntity();
						if (Url != null && !Url.equals("")) {
							contactsEntity.setPicture(Url);
						}
						dataCenterManagerService.updateSelfContactsEntity(contactsEntity, true);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				Intent intent_1 = new Intent();
				intent_1.setClass(getApplicationContext(),
						AccountActivity.class);
				startActivity(intent_1);
				finish();
				break;
			default:
				break;
			}
		}

	};
	
	String Url;
	boolean[] clickCheck = new boolean[] { false, false, false, false, false,
			false, false, false, false, false };
	int[] maleIds = new int[] { R.drawable.picture_0, R.drawable.picture_1,
			R.drawable.picture_2, R.drawable.picture_3, R.drawable.picture_4,
			R.drawable.picture_5, R.drawable.picture_6, R.drawable.picture_7,
			R.drawable.picture_8, R.drawable.picture_9

	};
	int[] femaleIds = new int[] { R.drawable.picture_10, R.drawable.picture_11,
			R.drawable.picture_12, R.drawable.picture_13,
			R.drawable.picture_14, R.drawable.picture_15,
			R.drawable.picture_16, R.drawable.picture_17,
			R.drawable.picture_18, R.drawable.picture_19

	};

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

			initPictureSelectActivity();
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
		/*
		 * 连接数据中心管理服务
		 */
		final Intent dataCenterManagerIntent = new Intent(this,
				DataCenterManagerService.class);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
				Context.BIND_ABOVE_CLIENT);

		super.onCreate(savedInstanceState);

	}

	public void initPictureSelectActivity() {
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.photoselect);
		Intent intent = getIntent();
		Bundle bundle = new Bundle();
		bundle = intent.getExtras();
//		int num = bundle.getInt("pictureNum");
		boolean sex_type=bundle.getBoolean("sex");
		partofUrl = bundle.getString("partofUrl");
		accountNum = bundle.getString("accountNum");
		password = bundle.getString("password");
		
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
//		if (num < 10) {
		if (sex_type) {
			ismale = true;
			for (int i = 0; i < maleIds.length; i++) {
				Map<String, Object> listItem = new HashMap<String, Object>();
				listItem.put("image", maleIds[i]);
				listItems.add(listItem);
			}
		} else {
			ismale = false;
			for (int i = 0; i < femaleIds.length; i++) {
				Map<String, Object> listItem = new HashMap<String, Object>();
				listItem.put("image", femaleIds[i]);
				listItems.add(listItem);
			}
		}
		
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems,
				R.layout.cell, new String[] { "image" },
				new int[] { R.id.iamge1 });
		gridView = (GridView) findViewById(R.id.grid01);
		gridView.setAdapter(simpleAdapter);
		
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				thePicture = position;
				if (!(lastView == null)) {
					lastView.setBackgroundColor(Color.parseColor("#ffffff"));
				}
				lastView = view;

				view.setBackgroundColor(Color.parseColor("#F5F5DC"));
			}
		});
		
		btn_confirm = (Button) findViewById(R.id.btn_confirm);
		btn_confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ismale) {
					Url = "" + thePicture;// 使用默认图像，只保存图像标号即可
				} else {
					int ThePicture = thePicture + 10;
					Url = "" + ThePicture;
				}
				
				JSONObject url = new JSONObject();
				JSONObject json = new JSONObject();
				try {
					url.put("picture", Url);
					url.put("accountNum", accountNum);
					url.put("password", password);
					json.put("content", url);
					json.put("command",
							APPConstant.USER_PROFILE_UPDATE_USER_PROFILE);
					CYLog.i(TAG,"图片修改数据" + json);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				GetTask = new HttpupLoad(APPConstant.getUSERURL(), json,
						handler, 3, getApplicationContext());
				GetTask.execute();
			}
		});
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
