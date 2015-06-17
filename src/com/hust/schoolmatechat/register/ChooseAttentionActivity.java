package com.hust.schoolmatechat.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.hust.schoolmatechat.postClass.GetIdsFromName;
import com.hust.schoolmatechat.postClass.HttpCommand;
import com.hust.schoolmatechat.postClass.HttpupLoad_gson;
import com.hust.schoolmatechat.LoginActivity;
import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ChooseAttentionActivity extends Activity {
	Button Btn_goAttention, Btn_gologin;
	private Bundle bundle;
	private ArrayList<String> cutID;
	private String fullID;
	private HttpupLoad GetTask;
	private Handler handler;
	private GetHandObj getContent;
	private boolean isreturn;
	private Map<String, String> map;
	private static final String TAG = "ChooseAttentionActivity";
	String name;
	
	//gson 使用
	HttpupLoad_gson httpupLoad_gson;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.choose_attention);
		Btn_goAttention = (Button) findViewById(R.id.go_attention);
		Btn_gologin = (Button) findViewById(R.id.go_login);
		cutID = new ArrayList<String>();
		map = new HashMap<String, String>();
		getContent = new GetHandObj();
		Btn_goAttention.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = getIntent();
				bundle = intent.getExtras();
				name = bundle.get("name").toString();
				CYLog.i(TAG,
						"姓名------->" + name + bundle.get("phoneNum").toString()
								+ bundle.get("checkCode").toString()
								+ bundle.get("password").toString());
//				JSONObject data = new JSONObject();
//				JSONObject order = new JSONObject();
//
//				try {
//
//					data.put("schoolNum", "000150");
//					data.put("name", name);
//					order.put("command",
//							APPConstant.USER_PROFILE_GET_USER_BASE_INFO_ID_LIST);
//					order.put("content", data);
//					CYLog.i(TAG, "发送的数据------->" + order);
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				handler = new Handler() {
//
//					@Override
//					public void handleMessage(Message msg) {
//						switch (msg.what) {
//						case 7:
//							isreturn = getContent.getIfsuccess(GetTask
//									.getLoaddata().getStrResult());
//							if (isreturn) {
//								fullID = GetTask.getLoaddata().getStrResult();
//								try {
//									map = getContent.getApartIDmap(fullID);
//								} catch (Exception e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//
//								CYLog.i(TAG, "输出map" + map);
//								CYLog.i(TAG, "输出full" + fullID);
//								try {
//									cutID = getContent.getcutApartID(GetTask
//											.getLoaddata().getStrResult());
//								} catch (Exception e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//								CYLog.i(TAG, "输出full" + cutID);
//								Intent inte = new Intent();
//								inte.putExtra("phoneNum", bundle
//										.get("phoneNum").toString());
//								inte.putExtra("checkCode",
//										bundle.get("checkCode").toString());
//								inte.putExtra("password", bundle
//										.get("password").toString());
//								inte.putExtra("IDList", cutID);
//								inte.putExtra("fullid", fullID);
//								inte.putExtra("name", name);
//
//								// 使用web附带的机构数据
//								try {
//									JSONObject jsonRetStr = new JSONObject(
//											GetTask.getLoaddata()
//													.getStrResult());
//									String fullNameList = jsonRetStr
//											.getString("msg");
//									inte.putExtra("fullNameList", fullNameList);
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//
//								inte.setClass(getApplicationContext(),
//										attestationActivity.class);
//								startActivity(inte);
//								finish();
//							} else {
//								Toast.makeText(getApplicationContext(),
//										"该姓名查询不到", Toast.LENGTH_SHORT).show();
//							}
//							break;
//						}
//					}
//				};
//				GetTask = new HttpupLoad(APPConstant.getUSERURL(), order,
//						handler, 7, getApplicationContext());
//				GetTask.execute();
//				Toast.makeText(getApplicationContext(), "继续完善班级信息",
//						Toast.LENGTH_SHORT).show();
				
				
				//gson的使用
				handler = new Handler() {

					@Override
					public void handleMessage(Message msg) {
						switch (msg.what) {
						case 7:
							isreturn = getContent.getIfsuccess(httpupLoad_gson
									.getLoaddata().getStrResult());
							if (isreturn) {
								fullID = httpupLoad_gson.getLoaddata().getStrResult();
								try {
									map = getContent.getApartIDmap(fullID);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								CYLog.i(TAG, "输出map" + map);
								CYLog.i(TAG, "输出full" + fullID);
								try {
									cutID = getContent.getcutApartID(httpupLoad_gson
											.getLoaddata().getStrResult());
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								CYLog.i(TAG, "输出full" + cutID);
								Intent inte = new Intent();
								inte.putExtra("phoneNum", bundle
										.get("phoneNum").toString());
								inte.putExtra("checkCode",
										bundle.get("checkCode").toString());
								inte.putExtra("password", bundle
										.get("password").toString());
								inte.putExtra("IDList", cutID);
								inte.putExtra("fullid", fullID);
								inte.putExtra("name", name);

								// 使用web附带的机构数据
								try {
									JSONObject jsonRetStr = new JSONObject(
											httpupLoad_gson.getLoaddata()
													.getStrResult());
									String fullNameList = jsonRetStr
											.getString("msg");
									inte.putExtra("fullNameList", fullNameList);
								} catch (Exception e) {
									e.printStackTrace();
								}

								inte.setClass(getApplicationContext(),
										attestationActivity.class);
								startActivity(inte);
								finish();
							} else {
								Toast.makeText(getApplicationContext(),
										"该姓名查询不到", Toast.LENGTH_SHORT).show();
							}
							break;
						}
					}
				};
				GetIdsFromName getIdsFromName=new GetIdsFromName(name,APPBaseInfo.SCHOOL_ID_NUMBER);
				 HttpCommand httpCommand=new HttpCommand(APPConstant.USER_PROFILE_GET_USER_BASE_INFO_ID_LIST, 
						 getIdsFromName);
				 String uploadJson=httpCommand.getJsonStr();
				 httpupLoad_gson=new HttpupLoad_gson(APPConstant.getUSERURL(),
						 uploadJson,
						 handler,
						 7,
						 getApplicationContext());
					httpupLoad_gson.execute();
					Toast.makeText(getApplicationContext(), "继续完善班级信息",
							Toast.LENGTH_SHORT).show();
					//gson的使用
			}
		});

		Btn_gologin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Intent intent = new Intent();
				// intent.setClass(
				// getApplicationContext(),
				// LoginActivity.class);
				// startActivity(intent);
				finish();
				Toast.makeText(getApplicationContext(), "欢迎您开始使用窗友",
						Toast.LENGTH_SHORT).show();

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
