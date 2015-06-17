package com.hust.schoolmatechat.register_2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hust.schoolmatechat.LoginActivity;
import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.postClass.AuthenticateData;
import com.hust.schoolmatechat.postClass.HttpCommand;
import com.hust.schoolmatechat.postClass.HttpupLoad_gson;
import com.hust.schoolmatechat.register.GetHandObj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SelectClassmatesActivity extends Activity {
	private static final String TAG = "attestationActivity";
	Button button1, button2, button3, button4, button5, button6, button7,
			button8, button9, checkClassmates;
	boolean btn_flag_1 = false, btn_flag_2 = false, btn_flag_3 = false,
			btn_flag_4 = false, btn_flag_5 = false, btn_flag_6 = false,
			btn_flag_7 = false, btn_flag_8 = false, btn_flag_9 = false;
	boolean[] btn_flag = { false, false, false, false, false, false, false,
			false, false };
	private int[] flags = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	String classmatesStr;
	ArrayList<String> Three_classmates;
	JSONObject sss;
	String[] classm;
	ArrayList<String> namelist = new ArrayList<String>();
	boolean[] signal = { false, false, false };
	private JSONObject first;
	private String fullID;
	JSONArray FullID ;
	HttpupLoad_gson httpupLoad_gson;
	String[] ids=new String[1];
	private String[] baseInfoId;
	public JSONObject second;
	private GetHandObj getContent;
	String name,password,phoneNum;

	String students[] = { "刘华", "宋洋", "杨凡", "舒慧", "胡明亮", "钟志威", "胡少文", "熊峰",
			"杨璐鲜", "何波", "韩佳霖", "王震", "吴凯", "史俊", "魏禹农", "张皓初", "颜浩", "王哲",
			"刘娜", "刘书辉", "吴伟", "常林", "张艳", "李若雪", "刘漫", "段向武", "李凡", "王琦",
			"刘萍", "熊舒", "李浩", "刘倩", "刘善芹", "张亦弛", "刘玉周", "丁雨葵", "涂君", "吕明",
			"谢刚", "甘一鸣", "谢成辉", "陈杰", "郝兵杰", "万正伟", "黄伟坚", "郭婷", "黄柳", "苏奇",
			"刘继沐", "林阳", "王益", "刘冬", "张江鹏", "杨卓", "楼谊", "程子易", "张新文", "王铁林",
			"聂维芬", "吴进文", "黎明", "卫国", "石晓梅", "向艳稳", "吴晓光", "袁晖", "陈林英", "程载和",
			"黄勇涛", "陈轶群", "肖少坡", "白重任", "王以兵", "龙杰锋", "李冠男", "李宁嘉", "罗丽珊",
			"陈建龙", "张洋", "彭善德", "殷晶晶", "王蕾", "戎平辽", "罗贵锋", "焦云", "樊为民", "黄伟",
			"陈智行", "苏恒迪", "易善军", "郑明娟", "陈宏光", "赵会明", "韩爱明", "胡安徽", "徐波",
			"李红亮", "罗广镇", "李全刚", "段涛" };
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 11:  
				checkClassmates.setClickable(true);
				try {
					Toast.makeText(
							getApplicationContext(),
							getContent.getMessage(httpupLoad_gson
									.getLoaddata()
									.getStrResult()),
							Toast.LENGTH_SHORT).show();
					if (getContent.getIfsuccess(httpupLoad_gson
							.getLoaddata().getStrResult())) {

						Intent itResult = new Intent();
						setResult(1, itResult);
						Intent intent = new Intent();
						intent.setClass(
								getApplicationContext(),
								LoginActivity.class);
						startActivity(intent);
						finish();
					}
					else{
						Toast.makeText(getApplicationContext(), getContent.getMessage(httpupLoad_gson
							.getLoaddata().getStrResult()), Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
		};
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_classmates);
		getContent=new GetHandObj();
		button1 = (Button) findViewById(R.id.calssmate_btn1);
		button1.setOnClickListener(listener);
		button2 = (Button) findViewById(R.id.calssmate_btn2);
		button2.setOnClickListener(listener);
		button3 = (Button) findViewById(R.id.calssmate_btn3);
		button3.setOnClickListener(listener);
		button4 = (Button) findViewById(R.id.calssmate_btn4);
		button4.setOnClickListener(listener);
		button5 = (Button) findViewById(R.id.calssmate_btn5);
		button5.setOnClickListener(listener);
		button6 = (Button) findViewById(R.id.calssmate_btn6);
		button6.setOnClickListener(listener);
		button7 = (Button) findViewById(R.id.calssmate_btn7);
		button7.setOnClickListener(listener);
		button8 = (Button) findViewById(R.id.calssmate_btn8);
		button8.setOnClickListener(listener);
		button9 = (Button) findViewById(R.id.calssmate_btn9);
		button9.setOnClickListener(listener);
		checkClassmates = (Button) findViewById(R.id.checkClassmates);
		checkClassmates.setOnClickListener(listener);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			classmatesStr = bundle.getString("calssmatesStr");
			Toast.makeText(getApplicationContext(), classmatesStr,
					Toast.LENGTH_SHORT).show();
			name=bundle.getString("name");
			password=bundle.getString("password");
			phoneNum=bundle.getString("phoneNum");
			
		}

		// String name = (String) bundle.get("name");
	
		fullID=bundle.getString("baseInfo");
		// 从网络上获取的班级名单
		JSONObject sss;
		Three_classmates = new ArrayList<String>();
		try {
			sss = new JSONObject(classmatesStr);

			String idString = sss.getString("obj");
			JSONArray getObj = new JSONArray(idString);
			JSONObject message = new JSONObject();

			Random random = new Random();
			int size = getObj.length();

			Set<String> nameSet = new HashSet<String>();
			nameSet.add(name);
			while (namelist.size() != 3) {
				int x = random.nextInt() % size;
				if (x < 0) {
					x = -x;
				}
				message = (JSONObject) getObj.get(x);
				String userName = message.getString("userName");
				if (nameSet.contains(userName)) {
					continue;
				}
				namelist.add(userName);
				nameSet.add(userName);
			}

			Three_classmates.add(namelist.get(0));
			Three_classmates.add(namelist.get(1));
			Three_classmates.add(namelist.get(2));
			// 测试版本，显示3个班级同学的名单
			if (APPConstant.DEBUG_MODE) {
				CYLog.i(TAG, namelist.toString());
				Toast.makeText(getApplicationContext(),
						"班级同学 " + namelist.toString(), Toast.LENGTH_SHORT)
						.show();
			}

			// 获取另外6个人名单
			while (namelist.size() != 9) {
				int x = random.nextInt() % 100;
				if (x < 0) {
					x = -x;
				}
				String name2 = students[x];
				if (nameSet.contains(name2)) {
					continue;
				}
				namelist.add(name2);
				nameSet.add(name2);
			}

			// 打乱原来的顺序
			Collections.sort(namelist, new Comparator<String>() {
				public int compare(String arg0, String arg1) {
					return arg0.compareTo(arg1);
				}
			});
			button1.setText(namelist.get(0));
			button2.setText(namelist.get(1));
			button3.setText(namelist.get(2));
			button4.setText(namelist.get(3));
			button5.setText(namelist.get(4));
			button6.setText(namelist.get(5));
			button7.setText(namelist.get(6));
			button8.setText(namelist.get(7));
			button9.setText(namelist.get(8));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.calssmate_btn1:
				if (!btn_flag[0]) {
					btn_flag[0] = true;
					button1.setBackgroundColor(R.drawable.checked_corner);
				} else {
					btn_flag[0] = false;
					button1.setBackgroundColor(0);
				}
				break;
			case R.id.calssmate_btn2:
				if (!btn_flag[1]) {
					btn_flag[1] = true;
					button2.setBackgroundColor(R.drawable.checked_corner);
				} else {
					btn_flag[1] = false;
					button2.setBackgroundColor(0);
				}
				break;
			case R.id.calssmate_btn3:
				if (!btn_flag[2]) {
					btn_flag[2] = true;
					button3.setBackgroundColor(R.drawable.checked_corner);
				} else {
					btn_flag[2] = false;
					button3.setBackgroundColor(0);
				}
				break;
			case R.id.calssmate_btn4:
				if (!btn_flag[3]) {
					btn_flag[3] = true;
					button4.setBackgroundColor(R.drawable.checked_corner);
				} else {
					btn_flag[3] = false;
					button4.setBackgroundColor(0);
				}
				break;
			case R.id.calssmate_btn5:
				if (!btn_flag[4]) {
					btn_flag[4] = true;
					button5.setBackgroundColor(R.drawable.checked_corner);
				} else {
					btn_flag[4] = false;
					button5.setBackgroundColor(0);
				}
				break;
			case R.id.calssmate_btn6:
				if (!btn_flag[5]) {
					btn_flag[5] = true;
					button6.setBackgroundColor(R.drawable.checked_corner);
				} else {
					btn_flag[5] = false;
					button6.setBackgroundColor(0);
				}
				break;
			case R.id.calssmate_btn7:
				if (!btn_flag[6]) {
					btn_flag[6] = true;
					button7.setBackgroundColor(R.drawable.checked_corner);
				} else {
					btn_flag[6] = false;
					button7.setBackgroundColor(0);
				}
				break;
			case R.id.calssmate_btn8:
				if (!btn_flag[7]) {
					btn_flag[7] = true;
					button8.setBackgroundColor(R.drawable.checked_corner);
				} else {
					btn_flag[7] = false;
					button8.setBackgroundColor(0);
				}
				break;
			case R.id.calssmate_btn9:
				if (!btn_flag[8]) {
					btn_flag[8] = true;
					button9.setBackgroundColor(R.drawable.checked_corner);
				} else {
					btn_flag[8] = false;
					button9.setBackgroundColor(0);
				}
				break;
			case R.id.checkClassmates:
				for (int j = 0; j < btn_flag.length; j++) {
					if (btn_flag[j]) {
						flags[j] = 1;
					} else {
						flags[j] = 0;
					}
				}
				int count = flags[0] + flags[1] + flags[2] + flags[3]
						+ flags[4] + flags[5] + flags[6] + flags[7] + flags[8];
				classm = new String[3];
				JSONArray Classmates = new JSONArray();
				if (count == 3) {
					for (int j = 0; j < btn_flag.length; j++) {
						if (btn_flag[j]) {
							Classmates.put(namelist.get(j));
						}
					}
					
					for (int ss = 0; ss < 3; ss++) {
						try {
							classm[ss] = Classmates.getString(ss);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					for (int i = 0; i < 3; i++) {
						for (int j = 0; j < Three_classmates.size(); j++) {
							try {
								if (Classmates.getString(i).equals(
										Three_classmates.get(j))) {
									signal[i] = true;
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				  System.out.println("选择的学生名单"+classm);
				  checkClassmates.setClickable(false);
					if(signal[0]&&signal[1]&&signal[2]){
					try {

						first = creatJsonString();
						// JSONArray Classmates = getClassmates();
						 FullID = new JSONArray();
						FullID.put(fullID);
						int length=FullID.length();
						ids[0]=fullID;
					
						int u = FullID.length();
						baseInfoId = new String[u];

						for (int h = 0; h < FullID.length(); h++) {
							// CYLog.i(TAG,"12312" +
							// FullID.getString(0));
							String x = FullID.getString(h);
							baseInfoId[h] = x;
						}

//						JSONArray intrestType = arraytoJSon(intrestTypelist);
						// JSONArray intrestType =null;
						// intrestType=new JSONArray();
						// for(int i=0;i<intrestTypelist.length;i++){
						// intrestType.put(intrestTypelist[i]);
						// }
//						JSONArray channel = arraytoJSon(channellist);
						// JSONArray channel = null;
						// channel=new JSONArray();
						// for(int i=0;i<channellist.length;i++){
						// channel.put(channellist[i]);
						// }
						first.put("classmates", Classmates);
						first.put("baseInfoId", FullID);
//						first.put("intrestType", intrestType);
//						first.put("channels", channel);
						second = new JSONObject();

//						second.put("command", APPConstant.USER_PROFILE_REGISTER);
						second.put("command", APPConstant.USER_PROFILE_AUTHENTICATED);
						second.put("content", first);
						//CYLog.i(TAG,"发送的字符:-------》"+second);
						// CYLog.i(TAG,"json " + second);
						
						Toast.makeText(
								getApplicationContext(),
								"数据上传，请稍候...",
								Toast.LENGTH_SHORT).show();
						
//						GetTask = new HttpupLoad(APPConstant.getUSERURL(),
//								second, handler, 11, getApplicationContext());
//						GetTask.execute();

					} catch (JSONException e) {
						e.printStackTrace();
					}
				
						
					
					//gson 使用
//					AuthenticateData authenticateData=new AuthenticateData(bundle.get("password").toString(),
//							bundle.get("name").toString(), 
//							bundle.get("phoneNum").toString(), classm, ids);
					AuthenticateData authenticateData=new AuthenticateData(password,
							name, 
							phoneNum, classm, ids);
					 HttpCommand httpCommand=new HttpCommand(APPConstant.USER_PROFILE_AUTHENTICATED, 
							 authenticateData);
					 String uploadJson=httpCommand.getJsonStr();
					 System.out.println("发送的数据"+uploadJson);
					 httpupLoad_gson=new HttpupLoad_gson(APPConstant.getUSERURL(),
							 uploadJson,
							 handler,
							 11,
							 getApplicationContext());
						httpupLoad_gson.execute();
						//gson的使用
					}
				break;
			default:
				break;
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	public JSONObject creatJsonString() {
		try {
			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("name", bundle.get("name"));
			jsonObject.put("name","李全刚");
			

//			jsonObject.put("phoneNum", bundle.get("phoneNum"));
			jsonObject.put("phoneNum", "13545022349");

//			jsonObject.put("password", bundle.get("password"));
			jsonObject.put("password", "123456");
		
			

			
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}
	}
}
