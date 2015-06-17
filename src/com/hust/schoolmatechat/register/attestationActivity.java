package com.hust.schoolmatechat.register;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.schoolmatechat.postClass.AuthenticateData;
import com.hust.schoolmatechat.postClass.GetClassmatesById;
import com.hust.schoolmatechat.postClass.HttpCommand;
import com.hust.schoolmatechat.postClass.HttpupLoad_gson;
import com.hust.schoolmatechat.LoginActivity;
import com.hust.schoolmatechat.NewsExploreActivitiy;
import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.dao.DepartmentDao;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;

public class attestationActivity extends Activity {

	private static final String TAG = "attestationActivity";
	private Button register, selectClass;
	private Spinner gradeSpinner;
	private TextView textView1, words, text_words;
	private LinearLayout select_1;
	private EditText classmateone, classmatetwo, classmatethree;
	private CheckBox classmateCheckBoxArray[];
	private int[] classmate_add_ids = {R.id.classmate1, R.id.classmate2, R.id.classmate3
			,R.id.classmate4, R.id.classmate5, R.id.classmate6 
			,R.id.classmate7, R.id.classmate8, R.id.classmate9 };
	private int count = 0;
	private int[] flags = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private Bundle bundle;
	private HttpupLoad GetTask;
	private GetHandObj getContent;
	private ArrayList<String> content;
	private String fullJson;
	private String fullID;
//	private ArrayList<String> apartment_1;
	private Map<String, String> map;
	private Map<String, String> departmentMap;

	private String[] baseInfoId;
	private List<String> departmentList;
	private DepartmentDao departmentDao;
	private String[] intrestTypelist = APPConstant.INTERESTLIST;
	private String[] channellist = APPConstant.CHANNELLIST;
	public JSONObject second;
	private JSONObject first;
//	private String classId;
	String students[]={"刘华","宋洋","杨凡","舒慧" ,"胡明亮","钟志威","胡少文","熊峰","杨璐鲜","何波","韩佳霖","王震","吴凯","史俊","魏禹农",
			"张皓初", "颜浩","王哲" ,"刘娜","刘书辉","吴伟","常林","张艳","李若雪","刘漫", "段向武","李凡","王琦", "刘萍","熊舒","李浩",
			"刘倩","刘善芹","张亦弛","刘玉周","丁雨葵","涂君","吕明","谢刚","甘一鸣","谢成辉","陈杰","郝兵杰","万正伟","黄伟坚","郭婷","黄柳",
			"苏奇","刘继沐","林阳","王益","刘冬","张江鹏","杨卓","楼谊", "程子易", "张新文","王铁林","聂维芬","吴进文","黎明","卫国","石晓梅",
			"向艳稳","吴晓光","袁晖","陈林英","程载和","黄勇涛","陈轶群","肖少坡","白重任","王以兵","龙杰锋","李冠男","李宁嘉","罗丽珊","陈建龙",
			"张洋","彭善德","殷晶晶","王蕾","戎平辽","罗贵锋","焦云","樊为民","黄伟","陈智行", "苏恒迪","易善军","郑明娟","陈宏光","赵会明","韩爱明",
			"胡安徽","徐波","李红亮","罗广镇","李全刚","段涛"};
	
	
	
	//gson 使用
	JSONArray FullID ;
		HttpupLoad_gson httpupLoad_gson;
		String[] ids=new String[1];
		String[] classm;
		
		ArrayList<String> Three_classmates ;
		boolean[] signal = { false, false, false };
		
		
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
//			case 11:
//				register.setClickable(true);
//				try {
//					Toast.makeText(
//							getApplicationContext(),
//							getContent.getMessage(GetTask
//									.getLoaddata()
//									.getStrResult()),
//							Toast.LENGTH_SHORT).show();
//					if (getContent.getIfsuccess(GetTask
//							.getLoaddata().getStrResult())) {
////						userprofile = new UserProfile(
////								getApplicationContext());
////						auser = new aUserProfile();
////						auser.setPicture(first
////								.getString("picture"));
////						auser.setUID(first
////								.getString("accountNum"));
////						auser.setPassword(first
////								.getString("password"));
////						auser.setName(first
////								.getString("name"));
////						auser.setPhoneNum(first
////								.getString("phoneNum"));
////						auser.setSex(first.getString("sex"));
////						auser.setAddress(first
////								.getString("address"));
////						auser.setSign(first
////								.getString("sign"));
////
////						auser.setIntrestType(intrestTypelist);
////						auser.setChannels(channellist);
////						auser.setEmail("");
////						// auser.setIdNumber(first.getString("idNumber"));
////						auser.setBaseInfoId(baseInfoId);
////						boolean it = userprofile
////								.addUserProfile(auser);
//						
//						
//						// CYLog.i(TAG,"成功？------>"
//						// + it + "baseInfoID"
//						// + auser.getBaseInfoId());
//						Intent itResult = new Intent();
//						setResult(1, itResult);
//						Intent intent = new Intent();
//						intent.setClass(
//								getApplicationContext(),
//								LoginActivity.class);
//						startActivity(intent);
//						finish();
//					}
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				break;
				// gson的使用
			case 11:
				register.setClickable(true);
				try {
					Toast.makeText(
							getApplicationContext(),
							getContent.getMessage(httpupLoad_gson
									.getLoaddata()
									.getStrResult()),
							Toast.LENGTH_SHORT).show();
					if (getContent.getIfsuccess(httpupLoad_gson
							.getLoaddata().getStrResult())) {
//						userprofile = new UserProfile(
//								getApplicationContext());
//						auser = new aUserProfile();
//						auser.setPicture(first
//								.getString("picture"));
//						auser.setUID(first
//								.getString("accountNum"));
//						auser.setPassword(first
//								.getString("password"));
//						auser.setName(first
//								.getString("name"));
//						auser.setPhoneNum(first
//								.getString("phoneNum"));
//						auser.setSex(first.getString("sex"));
//						auser.setAddress(first
//								.getString("address"));
//						auser.setSign(first
//								.getString("sign"));
//
//						auser.setIntrestType(intrestTypelist);
//						auser.setChannels(channellist);
//						auser.setEmail("");
//						// auser.setIdNumber(first.getString("idNumber"));
//						auser.setBaseInfoId(baseInfoId);
//						boolean it = userprofile
//								.addUserProfile(auser);
						
						
						// CYLog.i(TAG,"成功？------>"
						// + it + "baseInfoID"
						// + auser.getBaseInfoId());
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
						for(int l=0;l<classmateCheckBoxArray.length;l++){
							classmateCheckBoxArray[l].setChecked(false);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
				
				
				
//			case 20:
//				selectClass.setClickable(true);
//				textView1.setVisibility(View.VISIBLE);
//				select_1.setVisibility(View.VISIBLE);
//				register.setVisibility(View.VISIBLE);
//				words.setVisibility(View.VISIBLE);
//				text_words.setVisibility(View.VISIBLE);
//				
//				JSONObject json;
//				try {
//					json = new JSONObject(GetTask.getLoaddata()
//							.getStrResult());
//					boolean success = json.getBoolean("success");
//					if (!success) {
//						Toast.makeText(getApplicationContext(), "获取班级数据失败，请重试",
//								Toast.LENGTH_SHORT).show();
//						break;
//					}
//				} catch (JSONException e1) {
//					e1.printStackTrace();
//					break;
//				}						
//				
//				JSONObject sss;
//				try {
//					String name = (String) bundle.get("name");
//					//从网络上获取的班级名单
//					sss = new JSONObject(GetTask.getLoaddata()
//							.getStrResult());
//					String idString = sss.getString("obj");
//					JSONArray getObj = new JSONArray(idString);
//					JSONObject message = new JSONObject();
//					ArrayList<String> namelist = new ArrayList<String>();
//					Random random = new Random();
//					int size = getObj.length();
//					
//					Set<String> nameSet = new HashSet<String>();
//					nameSet.add(name);
//					while (namelist.size() != 3) {
//						int x = random.nextInt() % size;
//						if (x < 0) {
//							x = -x;
//						}
//						message = (JSONObject) getObj.get(x);
//						String userName = message.getString("userName");
//						if (nameSet.contains(userName)) {
//							continue;
//						}
//						namelist.add(userName);
//						nameSet.add(userName);
//					}
//					
//					//测试版本，显示3个班级同学的名单
//					if (APPConstant.DEBUG_MODE) {
//						CYLog.i(TAG,namelist.toString());
//						Toast.makeText(getApplicationContext(),
//								"班级同学 " + namelist.toString(),
//								Toast.LENGTH_SHORT).show();
//					}
//					
//					//获取另外6个人名单
//					while (namelist.size() != 9){
//						int x = random.nextInt() % 100;
//						if (x < 0) {
//							x = -x;
//						}
//						String name2 = students[x];
//						if (nameSet.contains(name2)) {
//							continue;
//						}
//						namelist.add(name2);
//						nameSet.add(name2);
//					}
//					
//					//打乱原来的顺序
//					Collections.sort(namelist, new Comparator<String>() {
//				        public int compare(String arg0, String arg1) {
//				            return arg0.compareTo(arg1);
//				        }
//				    });
//					
//					//显示到界面	
//					for (int i = 0; i < 9 ; ++i) {
//						classmateCheckBoxArray[i].setText(namelist.get(i));
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//
//				break;
				
				
				//修改gson后的处理
			case 20:
				selectClass.setClickable(true);
				textView1.setVisibility(View.VISIBLE);
				select_1.setVisibility(View.VISIBLE);
				register.setVisibility(View.VISIBLE);
				words.setVisibility(View.VISIBLE);
				text_words.setVisibility(View.VISIBLE);
				
				JSONObject json;
				Three_classmates = new ArrayList<String>();
				try {
					json = new JSONObject(httpupLoad_gson.getLoaddata()
							.getStrResult());
					boolean success = json.getBoolean("success");
					if (!success) {
						Toast.makeText(getApplicationContext(), "获取班级数据失败，请重试",
								Toast.LENGTH_SHORT).show();
						break;
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
					break;
				}						
				
				JSONObject sss;
				try {
					String name = (String) bundle.get("name");
					//从网络上获取的班级名单
					sss = new JSONObject(httpupLoad_gson.getLoaddata()
							.getStrResult());
					String idString = sss.getString("obj");
					JSONArray getObj = new JSONArray(idString);
					JSONObject message = new JSONObject();
					ArrayList<String> namelist = new ArrayList<String>();
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
					//测试版本，显示3个班级同学的名单
					if (APPConstant.DEBUG_MODE) {
						CYLog.i(TAG,namelist.toString());
						Toast.makeText(getApplicationContext(),
								"班级同学 " + namelist.toString(),
								Toast.LENGTH_SHORT).show();
					}
					
					//获取另外6个人名单
					while (namelist.size() != 9){
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
					
					//打乱原来的顺序
					Collections.sort(namelist, new Comparator<String>() {
				        public int compare(String arg0, String arg1) {
				            return arg0.compareTo(arg1);
				        }
				    });
					
					//显示到界面	
					for (int i = 0; i < 9 ; ++i) {
						classmateCheckBoxArray[i].setText(namelist.get(i));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				break;
				
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.attestation);

		getContent = new GetHandObj();
		Intent intent = getIntent();
		bundle = intent.getExtras();
		content = new ArrayList<String>();
		content = (ArrayList<String>) bundle.get("IDList");

		fullJson = (String) bundle.get("fullid");
		String fullNameArray[] = null;
		try {
			String fullNameList = (String) bundle.get("fullNameList");
			if (fullNameList != null && !fullNameList.equals("")) {
				fullNameArray = fullNameList.split("_");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		map = new HashMap<String, String>();
		map = getContent.getApartIDmap(fullJson);
		// CYLog.i(TAG,"输出map" + map);

		// CYLog.i(TAG,"得到的数据" + content);
		// CYLog.i(TAG,"fullid" + fullJson);

		textView1 = (TextView) findViewById(R.id.textView1);
		textView1.setVisibility(View.GONE);
		select_1 = (LinearLayout) findViewById(R.id.select_1);
		select_1.setVisibility(View.GONE);
		words = (TextView) findViewById(R.id.words);
		words.setVisibility(View.GONE);
		text_words = (TextView) findViewById(R.id.text_words);
		text_words.setVisibility(View.GONE);
		text_words.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						NewsExploreActivitiy.class);
				intent.putExtra("newsUrl", "file:///android_asset/cy.htm");
				intent.putExtra("userName", "窗友软件许可及服务协议");
				startActivity(intent);
			}
		});
		gradeSpinner = (Spinner) findViewById(R.id.academe);
		departmentDao = new DepartmentDao(this.getApplicationContext());
		departmentList = new ArrayList<String>();
		departmentMap = new HashMap<String, String>();
		for (int i = 0; i < content.size(); i++) {
			String classBaseId = content.get(i).substring(0, 16);
			String grade = departmentDao.getDepartmentFullName(classBaseId);
			if (grade == null || grade.equals("")) {
				//存本地
				if (fullNameArray != null && fullNameArray.length > i) {
					grade = fullNameArray[i];
				}
				
				if (grade != null && !grade.equals("")) {
					departmentDao.addDepartment(classBaseId, grade);
				} else {
					grade = "k,未知的院系,k,未知的班级";
				}
			}
			//选择的字符串与id之间的反向映射
			String gradeSeg[] = grade.split(",");
			StringBuffer buf = new StringBuffer();
			buf.append(gradeSeg[1]).append(" ").append(gradeSeg[3]);
			departmentMap.put(buf.toString(), content.get(i));
			departmentList.add(buf.toString());
		}
		
		ArrayAdapter<String> gradeAdapter = new ArrayAdapter<String>(
				attestationActivity.this, R.layout.simple_spinner_item,
				departmentList);
		gradeSpinner.setAdapter(gradeAdapter);
		gradeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String aprtment = parent.getItemAtPosition(position).toString();
				fullID = departmentMap.get(aprtment).substring(0, 16);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				// CYLog.i(TAG,"你选的班级对应的id------->"+fullID);

			}
		});
		// String classId=fullID.subSequence(0, 16);
		selectClass = (Button) findViewById(R.id.selectClass);
		selectClass.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Namelist=new ArrayList<String>();
				for(int l=0;l<classmateCheckBoxArray.length;l++){
					classmateCheckBoxArray[l].setChecked(false);
				}
				selectClass.setClickable(false);

//				JSONObject classID = new JSONObject();
//				JSONObject classgetStr = new JSONObject();
//				try {
//					classID.put("classId", fullID);
//					classgetStr.put("command",
//							APPConstant.USER_PROFILE_GET_CLASSMATES_INFO_LIST);
//					classgetStr.put("content", classID);
//					// CYLog.i(TAG,"classgetStr-------->"+classgetStr);
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//
//				GetTask = new HttpupLoad(APPConstant.getUSERURL(), classgetStr,
//						handler, 20, getApplicationContext());
//				GetTask.execute();
				
				//gson的使用
				GetClassmatesById getClassmatesById=new GetClassmatesById(fullID);
				 HttpCommand httpCommand=new HttpCommand(APPConstant.USER_PROFILE_GET_CLASSMATES_INFO_LIST, 
						 getClassmatesById);
				 String uploadJson=httpCommand.getJsonStr();
				System.out.println("发送的数据"+uploadJson);
				 httpupLoad_gson=new HttpupLoad_gson(APPConstant.getUSERURL(),
						 uploadJson,
						 handler,
						 20,
						 getApplicationContext());
					httpupLoad_gson.execute();
					//gson的使用
			}
		});
		
		classmateCheckBoxArray = new CheckBox[9];
		for (int i = 0; i < 9; ++i) {
			classmateCheckBoxArray[i] = (CheckBox) findViewById(classmate_add_ids[i]);
			final int index = i;
			classmateCheckBoxArray[i].setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (isChecked) {
						flags[index] = 1;
					} else {
						flags[index] = 0;
					}
				}
			});
		}

		classmateone = (EditText) findViewById(R.id.classmateone);
		classmatetwo = (EditText) findViewById(R.id.classmatetwo);
		classmatethree = (EditText) findViewById(R.id.classmatethree);

		register = (Button) findViewById(R.id.register);
		register.setVisibility(View.GONE);
		register.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				count = flags[0] + flags[1] + flags[2] + flags[3] + flags[4]
						+ flags[5] + flags[6] + flags[7] + flags[8];
				JSONArray Classmates = new JSONArray();
				 classm=new String[3];
				if (count == 3) {
					for (int i = 0; i < 9; ++i) {
						if (flags[i] == 1) {
							Classmates.put(classmateCheckBoxArray[i].getText());
							
						}
					}
					for(int ss=0;ss<3;ss++){
						try {
							classm[ss]=Classmates.getString(ss);
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
                System.out.println("选择的学生名单"+classm);
					register.setClickable(false);
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

						JSONArray intrestType = arraytoJSon(intrestTypelist);
						// JSONArray intrestType =null;
						// intrestType=new JSONArray();
						// for(int i=0;i<intrestTypelist.length;i++){
						// intrestType.put(intrestTypelist[i]);
						// }
						JSONArray channel = arraytoJSon(channellist);
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
					AuthenticateData authenticateData=new AuthenticateData(bundle.get("password").toString(),
							bundle.get("name").toString(), 
							bundle.get("phoneNum").toString(), classm, ids);
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
					else{
						Toast.makeText(getApplicationContext(), "您所选的同学不在该班级",
								Toast.LENGTH_SHORT).show();
						register.setClickable(true);
						for(int c=0;c<3;c++){
							signal[c]=false;
						}
						//   选择同学失败  重新调用更换同学名单
						for(int l=0;l<classmateCheckBoxArray.length;l++){
							classmateCheckBoxArray[l].setChecked(false);
						}
						

//						JSONObject classID = new JSONObject();
//						JSONObject classgetStr = new JSONObject();
//						try {
//							classID.put("classId", fullID);
//							classgetStr.put("command",
//									APPConstant.USER_PROFILE_GET_CLASSMATES_INFO_LIST);
//							classgetStr.put("content", classID);
//							// CYLog.i(TAG,"classgetStr-------->"+classgetStr);
//						} catch (JSONException e) {
//							e.printStackTrace();
//						}
		//
//						GetTask = new HttpupLoad(APPConstant.getUSERURL(), classgetStr,
//								handler, 20, getApplicationContext());
//						GetTask.execute();
						
						//gson的使用
						GetClassmatesById getClassmatesById=new GetClassmatesById(fullID);
						 HttpCommand httpCommand=new HttpCommand(APPConstant.USER_PROFILE_GET_CLASSMATES_INFO_LIST, 
								 getClassmatesById);
						 String uploadJson=httpCommand.getJsonStr();
						System.out.println("发送的数据"+uploadJson);
						 httpupLoad_gson=new HttpupLoad_gson(APPConstant.getUSERURL(),
								 uploadJson,
								 handler,
								 20,
								 getApplicationContext());
							httpupLoad_gson.execute();
							//gson的使用
					}
					
				} else {
					Toast.makeText(getApplicationContext(), "只能选择三位同学姓名",
							Toast.LENGTH_SHORT).show();
				}

			}

		});

	}

	public JSONObject creatJsonString() {
		try {
			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("accountNum", bundle.get("accountNum"));
			jsonObject.put("name", bundle.get("name"));
			
//			jsonObject.put("sex", bundle.get("sex"));
//			jsonObject.put("sex","0");
			// jsonObject.put("name", "刘晓红");
			jsonObject.put("phoneNum", bundle.get("phoneNum"));
			
//			jsonObject.put("idNumber", bundle.get("idNumber"));
//			jsonObject.put("idNumber", "");
			
			// jsonObject.put("phoneNum", "13317159192");
			jsonObject.put("password", bundle.get("password"));
//			jsonObject.put("picture", bundle.get("picture"));
			
//			jsonObject.put("address", bundle.get("address"));
//			jsonObject.put("address", "");
			
//			jsonObject.put("sign", "");
			jsonObject.put("checkCode", bundle.get("checkCode"));

			// jsonObject.put("email", "1432123433@qq.com");
			// jsonObject.put("classmates", "classmates");
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}
	}

	public JSONArray arraytoJSon(String[] list) {
		try {
			JSONArray jsonarray = new JSONArray();

			for (int i = 0; i < list.length; i++) {
				jsonarray.put(i, list[i]);
			}
			return jsonarray;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}
	}

	public JSONArray getClassmates() {
		try {
			JSONArray jsonarray = new JSONArray();
			jsonarray.put(0, classmateone.getText());
			jsonarray.put(1, classmatetwo.getText());
			jsonarray.put(2, classmatethree.getText());

			// jsonarray.put(0, "刘志华");
			// jsonarray.put(1, "彭亚荣");
			// jsonarray.put(2, "谢飞龙");

			return jsonarray;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}

	}

	public Boolean isThreeStu(String string1, String string2, String string3) {
		int num1 = 0, num2 = 0, num3 = 0, sum;

		if (!string1.equals(""))
			num1 = 1;
		if (!string2.equals(""))
			num2 = 1;
		if (!string3.equals(""))
			num3 = 1;
		sum = num1 + num2 + num3;
		if (sum > 2)
			return true;
		else
			return false;

	}

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
