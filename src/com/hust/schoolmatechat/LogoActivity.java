package com.hust.schoolmatechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hust.schoolmatechat.channelselect.ChannelSelectActivity;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.register.GetHandObj;
import com.hust.schoolmatechat.register.HttpupLoad;
import com.hust.schoolmatechat.utils.ImageUtils;
import com.hust.schoolmatechat.utils.StreamUtils;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class LogoActivity extends Activity {
	SharedPreferences prefs;
	PackageInfo info;
	private String TAG = "LogoActivity";
	private GetHandObj getContent;
	private HttpupLoad GetTask;
	private boolean isGetSchoolListSuccess = true;
	private Spinner provincelist_sp;
	private Spinner schoollist_sp;
	private Button ensureSchool;
	private TextView logo;
	private ProgressBar mProgressBar;
	List<String> pList = new ArrayList<String>();
	List<String> sList = new ArrayList<String>();
	List<String> idList = new ArrayList<String>();
	Dialog alertDialog;
	int schoolID=0;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 19:
				try {
					CYLog.i(TAG, "��ѯ���" + GetTask.getLoaddata().getStrResult());
					if (getContent.getIfsuccess(GetTask.getLoaddata()
							.getStrResult())) {
						pList.clear();
						sList.clear();

						final List<Map<String, String>> dataList = getContent
								.getSchoolList(GetTask.getLoaddata()
										.getStrResult());
						for (int i = 0; i < dataList.size(); i++) {
							int j = 0;
							for (j = 0; j < pList.size(); j++) {
								if (pList.get(j).equals(
										dataList.get(i).get("province")))
									break;
							}
							if (j == pList.size()) {
								pList.add(dataList.get(i).get("province"));
							}
						}
						if (pList.size() == 0) {
							pList.add("��ѡ��ʡ��");
						}
						final View view = LayoutInflater
								.from(LogoActivity.this).inflate(
										R.layout.myschool_layout, null);
						provincelist_sp = (Spinner) view
								.findViewById(R.id.province_sp);
						schoollist_sp = (Spinner) view
								.findViewById(R.id.school_sp);
						ensureSchool = (Button) view
								.findViewById(R.id.ensure_school);
						// ������������������
						ArrayAdapter<String> pAdapter = new ArrayAdapter<String>(
								getApplicationContext(),
								R.layout.simple_spinner_item, pList);
						ArrayAdapter<String> sAdapter = new ArrayAdapter<String>(
								getApplicationContext(),
								R.layout.simple_spinner_item, sList);
						schoollist_sp.setAdapter(sAdapter);
						provincelist_sp.setAdapter(pAdapter);
						// ���spinnger�¼�������
						provincelist_sp
								.setOnItemSelectedListener(new OnItemSelectedListener() {
									@Override
									public void onItemSelected(
											AdapterView<?> parent, View view,
											int position, long id) {

										for (int i = 0; i < dataList.size(); i++) {
											if (dataList
													.get(i)
													.get("province")
													.equals(pList.get(position))) {
												sList.add(dataList.get(i).get(
														"schoolName"));
												idList.add(dataList.get(i).get(
														"baseId"));
											}
										}

										ArrayAdapter<String> sAdapter = new ArrayAdapter<String>(
												getApplicationContext(),
												R.layout.simple_spinner_item,
												sList);
										schoollist_sp.setAdapter(sAdapter);

									}

									@Override
									public void onNothingSelected(
											AdapterView<?> parent) {
									}
								});
						schoollist_sp
						.setOnItemSelectedListener(new OnItemSelectedListener() {
							@Override
							public void onItemSelected(
									AdapterView<?> parent, View view,
									int position, long id) {
								schoolID = position;
//								Toast.makeText(LogoActivity.this,
//										"����ѧУ��"+sList.get(schoolID), Toast.LENGTH_SHORT)
//										.show();

							}

							@Override
							public void onNothingSelected(
									AdapterView<?> parent) {
							}
						});
						ensureSchool.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
//								Toast.makeText(LogoActivity.this,
//										"����ѧУ��"+sList.get(schoolID)+"", Toast.LENGTH_SHORT)
//										.show();
								mProgressBar.setVisibility(View.VISIBLE);
								logo.setText("���ݼ�����...");
								alertDialog.dismiss();
								try {
									JSONObject jsonObject = new JSONObject();
									
									jsonObject.put("baseId", idList.get(schoolID));
									JSONObject json_1 = jsonObject;
									JSONObject json_2 = new JSONObject();
									json_2.put("content", json_1);
									json_2.put("command", APPConstant.USER_PROFILE_GET_SCHOOL_CONFIGS);
									CYLog.i(TAG,"---------->" + json_2);

									GetTask = new HttpupLoad(APPConstant.BASEINFOURL, json_2, handler,
											20, getApplicationContext());
									GetTask.execute();
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								
								
							}
						});
						  alertDialog = new AlertDialog.Builder(LogoActivity.this)
						.setTitle("��ѡ������ѧУ")
						.setIcon(R.drawable.ic_launcher)
						.setView(view)
						.setCancelable(false)
						.show();

					} else {
						isGetSchoolListSuccess = false;
						Toast.makeText(LogoActivity.this,
								"��ʱδ�ܻ�ȡ��֧�ֵ�ѧУ��Ϣ������������Ӻ�����", Toast.LENGTH_SHORT)
								.show();
						finish();
					}
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(LogoActivity.this,
							"��ʱδ�ܻ�ȡ��֧�ֵ�ѧУ��Ϣ������������Ӻ�����"+e, Toast.LENGTH_SHORT)
							.show();
				}
				break;
			case 20:
				try {
					CYLog.i(TAG, "��ѯ���" + GetTask.getLoaddata().getStrResult());
					if (getContent.getIfsuccess(GetTask.getLoaddata()
							.getStrResult())) {
						JSONObject sss = new JSONObject(GetTask.getLoaddata()
								.getStrResult());
						String schoolString = sss.getString("obj");
						JSONArray schoollist = new JSONArray(schoolString);
							   
						prefs.edit().putString("baseId", schoollist.getJSONObject(0).getString("baseId")).commit();
						prefs.edit().putString("chatServer", schoollist.getJSONObject(0).getString("chatServer")).commit();
						prefs.edit().putString("chatServerDomain", schoollist.getJSONObject(0).getString("chatServerDomain")).commit();
						prefs.edit().putString("chatServerPort", schoollist.getJSONObject(0).getString("chatServerPort")).commit();
						prefs.edit().putString("city", schoollist.getJSONObject(0).getString("city")).commit();
						prefs.edit().putString("codeSecretKey", schoollist.getJSONObject(0).getString("codeSecretKey")).commit();
						prefs.edit().putString("fileServer", schoollist.getJSONObject(0).getString("fileServer")).commit();
						prefs.edit().putString("fileServerPort", schoollist.getJSONObject(0).getString("fileServerPort")).commit();
						prefs.edit().putString("province", schoollist.getJSONObject(0).getString("province")).commit();
						prefs.edit().putString("pushServer", schoollist.getJSONObject(0).getString("pushServer")).commit();
						prefs.edit().putString("pushServerAccount", schoollist.getJSONObject(0).getString("pushServerAccount")).commit();
						prefs.edit().putString("pushServerPassword", schoollist.getJSONObject(0).getString("pushServerPassword")).commit();
						prefs.edit().putString("pushServerPort", schoollist.getJSONObject(0).getString("pushServerPort")).commit();
						prefs.edit().putString("schoolName", schoollist.getJSONObject(0).getString("schoolName")).commit();
						prefs.edit().putString("telphone", schoollist.getJSONObject(0).getString("telephone")).commit();
						prefs.edit().putString("webServer", schoollist.getJSONObject(0).getString("webServer")).commit();
						prefs.edit().putString("webServerPort", schoollist.getJSONObject(0).getString("webServerPort")).commit();
						prefs.edit().putString("welcomePicture", schoollist.getJSONObject(0).getString("welcomePicture")).commit();
						Toast.makeText(LogoActivity.this,
								"���ݼ������", Toast.LENGTH_SHORT)
								.show();
						mProgressBar.setVisibility(View.GONE);
						logo.setText("���ݼ������");
						setData();
						welcome();
						
					}else {
						isGetSchoolListSuccess = false;
						Toast.makeText(LogoActivity.this,
								"��ʱδ�ܻ�ȡ��Ӧ�ü����������ݣ�����������ӣ��Ժ�����", Toast.LENGTH_SHORT)
								.show();
						finish();
					}
				} catch (Exception e) {
					e.printStackTrace();
					CYLog.e(TAG,""+e);
				}
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.activity_logo);
		String logoimage = prefs.getString("welcomePicture", "no");
		ImageUtils.setWelcomeLogo((ImageView) findViewById(R.id.logoimage),logoimage,handler);
		
		
		mProgressBar = (ProgressBar) findViewById(R.id.progressBarlogo);
		logo = (TextView) findViewById(R.id.textViewlogo);
		
		String auto = prefs.getString("AUTO", "no");
		String schoolID = prefs.getString("baseId", "0");
		if (schoolID.equals("0")) {
			new Handler().postDelayed(rget, 1000);
			
		} else if (auto.equals("auto") || auto.equals("reset")) {
			new Handler().postDelayed(runAuto, 1500);// 2���رգ�����ת����½ҳ��
		} else {
			new Handler().postDelayed(r, 2000);// 2���رգ�����ת����½ҳ��
		}
	}

	Runnable rget = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			getContent = new GetHandObj();
			try {

				JSONObject json_2 = new JSONObject();
				json_2.put("command",
						APPConstant.USER_PROFILE_GET_SUPPORTED_SCHOOLS);
				CYLog.i(TAG, "---------->" + json_2);

				GetTask = new HttpupLoad(APPConstant.BASEINFOURL,
						json_2, handler, 19, getApplicationContext());
				GetTask.execute();
			} catch (Exception e) {
				e.printStackTrace();
				CYLog.e(TAG, "---------->" + e);
			}
		}
	};
	Runnable r = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			setData();
			welcome();
		}
	};
	Runnable runAuto = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			setData();
			Intent intent = new Intent();
			intent.setClass(LogoActivity.this, LoginActivity.class);
			intent.putExtra("USERNAME", prefs.getString("USERNAME", "username"));
			intent.putExtra("PASS", prefs.getString("PASS", "pass"));
			startActivity(intent);
			finish();
		}
	};

	public void setData() {
		CYLog.d(TAG, APPBaseInfo.URL);
		APPBaseInfo.FLAG = null;
		StreamUtils.checkBaseInfo(prefs);		
		CYLog.d(TAG, APPBaseInfo.URL);
		
	}
	public void welcome() {
		try {
			info = getPackageManager().getPackageInfo(
					"com.hust.schoolmatechat", 0);
			int currentVersion = info.versionCode;

			int lastVersion = prefs.getInt("VERSION_KEY", 0);

			if (currentVersion > lastVersion) {
				// �����ǰ�汾�����ϴΰ汾���ð汾���ڵ�һ������
				Intent intent = new Intent();
				intent.setClass(LogoActivity.this, IntroductionActivity.class);
				intent.putExtra("comefrom", "logo");
				startActivity(intent);
				// ����ǰ�汾д��preference�У����´�������ʱ�򣬾ݴ��жϣ�����Ϊ�״�����
				prefs.edit().putInt("VERSION_KEY", currentVersion).commit();
				prefs.edit().putInt("DATA", 1).commit();
			} else {
				Intent intent = new Intent();
				intent.setClass(LogoActivity.this, LoginActivity.class);
				startActivity(intent);
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finish();
	}

}
