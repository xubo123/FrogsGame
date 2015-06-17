package com.hust.schoolmatechat;

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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.schoolmatechat.postClass.AddClass;
import com.hust.schoolmatechat.postClass.GetIdsFromName;
import com.hust.schoolmatechat.postClass.HttpCommand;
import com.hust.schoolmatechat.postClass.HttpupLoad_gson;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.dao.DepartmentDao;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.register.HttpupLoad;

public class StudyExActivity extends Activity {
	private static final String TAG = "StudyExActivity";
	private List<String> joinedDepartmentList;// 已经加入的班级
	private ArrayList<String> joinedIdList;// 已经加入的班级id
	private Map<String, String> unjoinedDepartMap;// 新获取的班级列表
	private DepartmentDao departmentDao;
	private ListView joined_grade_list;
	private RelativeLayout gradelist_rl;
	private Spinner gradelist_sp;
	private Button addgrade, makeClassmateList;
	private Map<String, String> fullIdMap;// 缓存班级名单数据
	private HttpupLoad GetTask;
	private String fullID = null;
	boolean isfriend = false;
	String friendBaseInfo;
	String[] ID;
	private CheckBox classmateCheckBoxArray[];
	private int[] classmate_add_ids = { R.id.classmate1_add,
			R.id.classmate2_add, R.id.classmate3_add, R.id.classmate4_add,
			R.id.classmate5_add, R.id.classmate6_add, R.id.classmate7_add,
			R.id.classmate8_add, R.id.classmate9_add };
	LinearLayout classmateList;
	TextView textView1_add,textView1_addclass;
	Button check_adddata;

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

	private int[] flags = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private int count = 0;
	boolean[] signal = { false, false, false };
	private ArrayList<String> namelist;
	private String userName;
	private Context mConext;
	
	
	//gson 使用
	HttpupLoad_gson httpupLoad_gson;
	String[] baseInfoId_gson=new String[1];
	String[] classmates_gson=new String[3];
	ArrayList<String> Three_classmates ;
	

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
//			case 3:
//				CYLog.i(TAG,"修改结果"
//						+ GetTask.getLoaddata().getStrResult());
//				try {
//					JSONObject json = new JSONObject(GetTask.getLoaddata()
//							.getStrResult());
//					boolean idString = json.getBoolean("success");
//					// 上传成功,修改本地数据
//					if (idString) {
//						Toast.makeText(getApplicationContext(),
//								"恭喜你，添加成功!请重新登录", Toast.LENGTH_SHORT).show();
//
//						ContactsEntity contactsEntity = dataCenterManagerService
//								.getUserSelfContactsEntity();
//						// StringBuffer baseInfoBuf = new StringBuffer();
//						// Set<String> baseInfoSet = new HashSet<String>();
//						//
//						// // 本地存储的id信息
//						// String baseInfoId = contactsEntity.getBaseInfoId();
//						// String[] baseInfoIds = null;
//						// if (baseInfoId != null && !baseInfoId.equals("")) {
//						// baseInfoIds = baseInfoId.split(",");
//						// if (baseInfoIds.length > 0) {
//						// baseInfoSet.add(baseInfoIds[0]);
//						// baseInfoBuf.append(baseInfoIds[0]);
//						// }
//						// for (int i = 1; i < baseInfoIds.length; ++i) {
//						// if (!baseInfoSet.contains(baseInfoIds[i])) {
//						// baseInfoSet.add(baseInfoIds[i]);
//						// baseInfoBuf.append(",").append(
//						// baseInfoIds[i]);
//						// }
//						// }
//						// }
//						//
//						// // 服务器返回的id信息
//						// JSONArray obj = json.getJSONArray("obj");
//						// if (obj != null && obj.length() > 0) {
//						// // 本地没有id信息，则为第一次认证添加班级
//						// if (baseInfoIds == null || baseInfoIds.length == 0) {
//						// baseInfoBuf.append((String) obj.get(0));
//						// } else {
//						// for (int i = 0; i < obj.length(); ++i) {
//						// if (!baseInfoSet.contains((String) obj
//						// .get(i))) {
//						// baseInfoSet.add((String) obj.get(i));
//						// baseInfoBuf.append(",").append(
//						// (String) obj.get(i));
//						// }
//						// }
//						// }
//						// }
//						// contactsEntity.setBaseInfoId(baseInfoBuf.toString());
//						// dataCenterManagerService
//						// .updateSelfContactsEntity(contactsEntity);
//						// 清除本地用户信息，让用户重新登录，从服务器获取
//						if (!dataCenterManagerService
//								.deleteSelfContactsEntity(contactsEntity
//										.getUserAccount())) {
//							dataCenterManagerService
//									.deleteSelfContactsEntity(contactsEntity
//											.getPhoneNum());
//						}
//
//						SharedPreferences prefs = PreferenceManager
//								.getDefaultSharedPreferences(StudyExActivity.this);
//						prefs.edit().putString("AUTO", "no").commit();
//						// 账号可以记住，不必清空，清空密码
//						prefs.edit().putString("PASS", "").commit();
//
//						Intent intent_1 = new Intent();
//						intent_1.setClass(mConext, LoginActivity.class);
//						startActivity(intent_1);
//						finish();
//						break;
//					} else {
//						Toast.makeText(getApplicationContext(),
//								"添加失败，请检查网络或检查同学选择是否正确!", Toast.LENGTH_SHORT)
//								.show();
//
//						// 若为基础id不为空，则表示要添加班级
//						if (fullID != null && !fullID.equals("")) {
//							// 判断是否为已认证用户，若是，则重新登录聊天服务
//							dataCenterManagerService.loginTigase();
//						}
//
//						Intent intent_1 = new Intent();
//						intent_1.setClass(mConext, AccountActivity.class);
//						startActivity(intent_1);
//						finish();
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//
//				break;
			
			case 3:
				CYLog.i(TAG,"修改结果"
						+ httpupLoad_gson.getLoaddata().getStrResult());
				try {
					JSONObject json = new JSONObject(httpupLoad_gson.getLoaddata()
							.getStrResult());
					boolean idString = json.getBoolean("success");
					// 上传成功,修改本地数据
					if (idString) {
						Toast.makeText(getApplicationContext(),
								"恭喜你，添加成功!稍后应用将重启", Toast.LENGTH_SHORT).show();

						ContactsEntity contactsEntity = dataCenterManagerService
								.getUserSelfContactsEntity();
						// StringBuffer baseInfoBuf = new StringBuffer();
						// Set<String> baseInfoSet = new HashSet<String>();
						//
						// // 本地存储的id信息
						// String baseInfoId = contactsEntity.getBaseInfoId();
						// String[] baseInfoIds = null;
						// if (baseInfoId != null && !baseInfoId.equals("")) {
						// baseInfoIds = baseInfoId.split(",");
						// if (baseInfoIds.length > 0) {
						// baseInfoSet.add(baseInfoIds[0]);
						// baseInfoBuf.append(baseInfoIds[0]);
						// }
						// for (int i = 1; i < baseInfoIds.length; ++i) {
						// if (!baseInfoSet.contains(baseInfoIds[i])) {
						// baseInfoSet.add(baseInfoIds[i]);
						// baseInfoBuf.append(",").append(
						// baseInfoIds[i]);
						// }
						// }
						// }
						//
						// // 服务器返回的id信息
						// JSONArray obj = json.getJSONArray("obj");
						// if (obj != null && obj.length() > 0) {
						// // 本地没有id信息，则为第一次认证添加班级
						// if (baseInfoIds == null || baseInfoIds.length == 0) {
						// baseInfoBuf.append((String) obj.get(0));
						// } else {
						// for (int i = 0; i < obj.length(); ++i) {
						// if (!baseInfoSet.contains((String) obj
						// .get(i))) {
						// baseInfoSet.add((String) obj.get(i));
						// baseInfoBuf.append(",").append(
						// (String) obj.get(i));
						// }
						// }
						// }
						// }
						// contactsEntity.setBaseInfoId(baseInfoBuf.toString());
						// dataCenterManagerService
						// .updateSelfContactsEntity(contactsEntity);
						// 清除本地用户信息，让用户重新登录，从服务器获取
						if (!dataCenterManagerService
								.deleteSelfContactsEntity(contactsEntity
										.getUserAccount())) {
							dataCenterManagerService
									.deleteSelfContactsEntity(contactsEntity
											.getPhoneNum());
						}

						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(StudyExActivity.this);
						prefs.edit().putString("AUTO", "reset").commit();
						// 账号可以记住，不必清空，清空密码
						//prefs.edit().putString("PASS", "").commit();

						Intent intent_1 = new Intent();
						intent_1.setClass(mConext, LogoActivity.class);
						startActivity(intent_1);
						finish();
						break;
					} else {
						Toast.makeText(getApplicationContext(),
								"添加失败，请检查网络或检查同学选择是否正确!", Toast.LENGTH_SHORT)
								.show();

						// 若为基础id不为空，则表示要添加班级
						if (fullID != null && !fullID.equals("")) {
							// 判断是否为已认证用户，若是，则重新登录聊天服务
							dataCenterManagerService.loginTigase();
						}

						Intent intent_1 = new Intent();
						intent_1.setClass(mConext, AccountActivity.class);
						startActivity(intent_1);
						finish();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

				break;

//			case 7:
//				try {
//					String returnStr = GetTask.getLoaddata().getStrResult();
//					if (returnStr == null) {
//						// 获取失败了可以再次获取
//						addgrade.setEnabled(true);
//						return;
//					}
//					JSONObject returnJson = new JSONObject(returnStr);
//					boolean success = returnJson.getBoolean("success");
//					if (success == false) {
//						// 获取失败了可以再次获取
//						addgrade.setEnabled(true);
//						Toast.makeText(getApplicationContext(), "获取班级数据失败，请重试",
//								Toast.LENGTH_SHORT).show();
//						return;
//					}
//
//					// 已加入的班级id
//					ContactsEntity contactsEntity = dataCenterManagerService
//							.getUserSelfContactsEntity();
//					String auth = contactsEntity.getAuthenticated();
//					String baseinfoId = contactsEntity.getBaseInfoId();
//					Set<String> idSet = new HashSet<String>();
//					if (auth != null && auth.equals("1")) {
//						if (baseinfoId != null && !baseinfoId.equals("")) {
//							String baseinfoIds[] = baseinfoId.split(",");
//							for (int i = 0; i < baseinfoIds.length; ++i) {
//								if (!idSet.contains(baseinfoIds[i])) {
//									idSet.add(baseinfoIds[i]);
//								}
//							}
//						}
//					}
//
//					// 未加入的班级id
//					String idString = returnJson.getString("obj");
//					JSONArray idList = new JSONArray(idString);
//					List<String> unjoinedIdList = new ArrayList<String>();
//					for (int i = 0; i < idList.length(); i++) {
//						if (!idSet.contains(idList.get(i))) {
//							idSet.add(idList.getString(i));
//							unjoinedIdList.add(idList.getString(i));
//						}
//					}
//
//					List<String> unjoinedDepartmentList = new ArrayList<String>();
//					String classMsg = returnJson.getString("msg");
//					String msgs[] = null;
//					if (classMsg != null && !classMsg.equals("")) {
//						//取出web传递过来的机构全称
//						try {
//							msgs = classMsg.split("_");
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//					for (int i = 0; i < unjoinedIdList.size(); i++) {
//						String baseInfoId = unjoinedIdList.get(i).substring(0, 16);
//						String departmentFullName = departmentDao.getDepartmentFullName(baseInfoId);
//						if (departmentFullName == null || departmentFullName.equals("")) {
//							//兼容之前的版本
//							if (msgs != null && msgs.length > i) {
//								departmentFullName = msgs[i];
//							}
//							if (departmentFullName == null || departmentFullName.equals("")) {
//								continue;
//							}
//							//数据存本地
//							departmentDao.addDepartment(baseInfoId, departmentFullName);
//						}
//						String departmentFullNames[] = departmentFullName.split(",");
//						if (departmentFullNames.length < 4) {
//							continue;
//						}
//						StringBuffer buf = new StringBuffer();
//						buf.append(departmentFullNames[1]).append(" ").append(departmentFullNames[3]);
//						unjoinedDepartmentList.add(buf.toString());
//						unjoinedDepartMap.put(buf.toString(), baseInfoId);
//					}
//					
//					if (unjoinedIdList.size() > 0 && (unjoinedDepartMap == null || unjoinedDepartMap.size() == 0)) {
//						// 获取失败了可以再次获取
//						addgrade.setEnabled(true);
//						Toast.makeText(getApplicationContext(), "查询不到您所在的班级!",
//								Toast.LENGTH_SHORT).show();
//						return;
//					}
//
//					// 不显示列表
//					if (unjoinedDepartmentList.size() == 0) {
//						addgrade.setVisibility(View.GONE);
//						Toast.makeText(getApplicationContext(), "您的班级已经全部添加!",
//								Toast.LENGTH_SHORT).show();
//						gradelist_rl.setVisibility(View.GONE);
//						textView1_addclass.setVisibility(View.GONE);
//						break;
//					} else {
//						textView1_addclass.setVisibility(View.VISIBLE);
//						gradelist_rl.setVisibility(View.VISIBLE);
//					}
//					// 将内容与适配器连接
//					ArrayAdapter<String> gradeAdapter = new ArrayAdapter<String>(
//							getApplicationContext(),
//							R.layout.simple_spinner_item,
//							unjoinedDepartmentList);
//					gradelist_sp.setAdapter(gradeAdapter);
//					// 添加spinnger事件监听器
//					gradelist_sp
//							.setOnItemSelectedListener(new OnItemSelectedListener() {
//								@Override
//								public void onItemSelected(
//										AdapterView<?> parent, View view,
//										int position, long id) {
//									String aprtment_1 = parent
//											.getItemAtPosition(position)
//											.toString();
//									// 通过classmap得到班级的全称,并查表得到班级对应的id,然后再根据map来查到用户的baseinfoIds
//									fullID = unjoinedDepartMap.get(aprtment_1);
//								}
//
//								@Override
//								public void onNothingSelected(
//										AdapterView<?> parent) {
//								}
//							});
//				} catch (Exception e) {
//					e.printStackTrace();
//					
//					// 获取失败了可以再次获取
//					addgrade.setEnabled(true);
//					Toast.makeText(getApplicationContext(), "获取班级数据失败，请重试",
//							Toast.LENGTH_SHORT).show();
//				}
//				break;
				
				
				// gson 的case 7 
			case 7:
				try {
					String returnStr = httpupLoad_gson.getLoaddata().getStrResult();
					if (returnStr == null) {
						// 获取失败了可以再次获取
						addgrade.setEnabled(true);
						return;
					}
					JSONObject returnJson = new JSONObject(returnStr);
					boolean success = returnJson.getBoolean("success");
					if (success == false) {
						// 获取失败了可以再次获取
						addgrade.setEnabled(true);
						Toast.makeText(getApplicationContext(), "获取班级数据失败，请重试",
								Toast.LENGTH_SHORT).show();
						return;
					}

					// 已加入的班级id
					ContactsEntity contactsEntity = dataCenterManagerService
							.getUserSelfContactsEntity();
					String auth = contactsEntity.getAuthenticated();
					String baseinfoId = contactsEntity.getBaseInfoId();
					Set<String> idSet = new HashSet<String>();
					if (auth != null && auth.equals("1")) {
						if (baseinfoId != null && !baseinfoId.equals("")) {
							String baseinfoIds[] = baseinfoId.split(",");
							for (int i = 0; i < baseinfoIds.length; ++i) {
								if (!idSet.contains(baseinfoIds[i])) {
									idSet.add(baseinfoIds[i]);
								}
							}
						}
					}

					// 未加入的班级id
					String idString = returnJson.getString("obj");
					JSONArray idList = new JSONArray(idString);
					List<String> unjoinedIdList = new ArrayList<String>();
					for (int i = 0; i < idList.length(); i++) {
						if (!idSet.contains(idList.get(i))) {
							idSet.add(idList.getString(i));
							unjoinedIdList.add(idList.getString(i));
						}
					}

							
					List<String> unjoinedDepartmentList = new ArrayList<String>();
					String classMsg = returnJson.getString("msg");
					String msgs[] = null;
					if (classMsg != null && !classMsg.equals("")) {
						//取出web传递过来的机构全称
						try {
							msgs = classMsg.split("_");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					// 把获得的机构数据全部入库
					
					for(int h=0;h<idList.length();h++){
						String ldlistFull=idList.getString(h);
						String ldlistCut=idList.getString(h).substring(0, 16);
						String departmentFullNam = departmentDao.getDepartmentFullName(ldlistCut);
						if (departmentFullNam == null || departmentFullNam.equals("")) {
							departmentDao.addDepartment(ldlistCut, msgs[h]);
						}
						else{
							continue;
						}
					}
					for (int i = 0; i < unjoinedIdList.size(); i++) {
						String baseInIdFull=unjoinedIdList.get(i);
						String baseInfoId = unjoinedIdList.get(i).substring(0, 16);
						String departmentFullName = departmentDao.getDepartmentFullName(baseInfoId);
						if (departmentFullName == null || departmentFullName.equals("")) {
							//兼容之前的版本
							if (msgs != null && msgs.length > i) {
								departmentFullName = msgs[i];
							}
							if (departmentFullName == null || departmentFullName.equals("")) {
								continue;
							}
							//数据存本地
							departmentDao.addDepartment(baseInfoId, departmentFullName);
						}
						String departmentFullNames[] = departmentFullName.split(",");
						if (departmentFullNames.length < 4) {
							continue;
						}
						StringBuffer buf = new StringBuffer();
						buf.append(departmentFullNames[1]).append(" ").append(departmentFullNames[3]);
						unjoinedDepartmentList.add(buf.toString());
						unjoinedDepartMap.put(buf.toString(), baseInIdFull);
					}
					
					if (unjoinedIdList.size() > 0 && (unjoinedDepartMap == null || unjoinedDepartMap.size() == 0)) {
						// 获取失败了可以再次获取
						addgrade.setEnabled(true);
						Toast.makeText(getApplicationContext(), "查询不到您所在的班级!",
								Toast.LENGTH_SHORT).show();
						return;
					}

					// 不显示列表
					if (unjoinedDepartmentList.size() == 0) {
						addgrade.setVisibility(View.GONE);
						Toast.makeText(getApplicationContext(), "您的班级已经全部添加!",
								Toast.LENGTH_SHORT).show();
						gradelist_rl.setVisibility(View.GONE);
						textView1_addclass.setVisibility(View.GONE);
						break;
					} else {
						textView1_addclass.setVisibility(View.VISIBLE);
						gradelist_rl.setVisibility(View.VISIBLE);
					}
					// 将内容与适配器连接
					ArrayAdapter<String> gradeAdapter = new ArrayAdapter<String>(
							getApplicationContext(),
							R.layout.simple_spinner_item,
							unjoinedDepartmentList);
					gradelist_sp.setAdapter(gradeAdapter);
					// 添加spinnger事件监听器
					gradelist_sp
							.setOnItemSelectedListener(new OnItemSelectedListener() {
								@Override
								public void onItemSelected(
										AdapterView<?> parent, View view,
										int position, long id) {
									String aprtment_1 = parent
											.getItemAtPosition(position)
											.toString();
									// 通过classmap得到班级的全称,并查表得到班级对应的id,然后再根据map来查到用户的baseinfoIds
									fullID = unjoinedDepartMap.get(aprtment_1);
								}

								@Override
								public void onNothingSelected(
										AdapterView<?> parent) {
								}
							});
				} catch (Exception e) {
					e.printStackTrace();
					
					// 获取失败了可以再次获取
					addgrade.setEnabled(true);
					Toast.makeText(getApplicationContext(), "获取班级数据失败，请重试",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case 20:
				makeClassmateList.setEnabled(true);
				JSONObject sss;
				try {
					if (fullIdMap == null) {
						fullIdMap = new HashMap<String, String>();
					}

					// 本地有缓存，直接使用缓存，没有缓存则对网络结果进行判断
					String idString = null;
					if (!fullIdMap.containsKey(fullID)) {
						JSONObject json;
						try {
							json = new JSONObject(GetTask.getLoaddata()
									.getStrResult());
							boolean success = json.getBoolean("success");
							if (!success) {
								Toast.makeText(getApplicationContext(),
										"获取班级数据失败，请重试", Toast.LENGTH_SHORT)
										.show();
								break;
							}
						} catch (JSONException e1) {
							e1.printStackTrace();
							break;
						}

						sss = new JSONObject(GetTask.getLoaddata()
								.getStrResult());
						// String name = (String) bundle.get("name");
						// 从网络上获取的班级名单
						sss = new JSONObject(GetTask.getLoaddata()
								.getStrResult());
						idString = sss.getString("obj");
						makeClassmateList.setText("换一组名单");

						fullIdMap.put(fullID, idString);
					} else {
						idString = fullIdMap.get(fullID);
						System.out
								.println("makeClassmateList using memory cache : "
										+ idString);
					}

					// 成功获取班级名单数据
					textView1_add.setVisibility(View.VISIBLE);
					classmateList.setVisibility(View.VISIBLE);
					check_adddata.setVisibility(View.VISIBLE);

					JSONArray getObj = new JSONArray(idString);
					JSONObject message = new JSONObject();
					namelist = new ArrayList<String>();
					Random random = new Random();
					int size = getObj.length();

					Set<String> nameSet = new HashSet<String>();
					Three_classmates = new ArrayList<String>();
					nameSet.add(userName);
					while (namelist.size() != 3) {
						int x = random.nextInt() % size;
						if (x < 0) {
							x = -x;
						}
						message = (JSONObject) getObj.get(x);
						String name1 = message.getString("userName");
						if (nameSet.contains(name1)) {
							continue;
						}
						namelist.add(name1);
						nameSet.add(name1);
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

					// 显示到界面
					for (int i = 0; i < 9; ++i) {
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

			initStudyExActivity();
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

		Intent parentIntent = this.getIntent();
		if (parentIntent.hasExtra("userName")) {
			userName = parentIntent.getStringExtra("userName");
		} else {
			userName = "";
		}
	}

	private void initStudyExActivity() {
		mConext = this;
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		if(userName.equals("")){
			bar.setTitle("学习经历");
		}else{
			bar.setTitle("我的学习经历");
		}
		setContentView(R.layout.studyex);
		textView1_add = (TextView) findViewById(R.id.textView1_add);
		textView1_add.setVisibility(View.GONE);
		classmateList = (LinearLayout) findViewById(R.id.classmateList);
		classmateList.setVisibility(View.GONE);
		check_adddata = (Button) findViewById(R.id.check_adddata);
		check_adddata.setVisibility(View.GONE);
		textView1_addclass = (TextView) findViewById(R.id.textView1_addclass);
		textView1_addclass.setVisibility(View.GONE);
		classmateCheckBoxArray = new CheckBox[9];
		for (int i = 0; i < 9; ++i) {
			classmateCheckBoxArray[i] = (CheckBox) findViewById(classmate_add_ids[i]);
			final int index = i;
			classmateCheckBoxArray[i]
					.setOnCheckedChangeListener( 
							new OnCheckedChangeListener() {
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

//		namelist = new ArrayList<String>();

		Intent intent = getIntent();
		Bundle bundle = new Bundle();
		bundle = intent.getExtras();
		if (bundle != null) {
			isfriend = bundle.getBoolean("isfriend");
			friendBaseInfo = bundle.getString("friendBaseInfo");
		}
//		Toast.makeText(getApplicationContext(),
//				"isfriend="+isfriend+",friendBaseInfo="+friendBaseInfo, Toast.LENGTH_SHORT).show();
		joined_grade_list = (ListView) findViewById(R.id.joined_grade_list);
		joined_grade_list.setEnabled(false);

		gradelist_rl = (RelativeLayout) findViewById(R.id.gradelist_rl);
		gradelist_rl.setVisibility(View.GONE);

		gradelist_sp = (Spinner) findViewById(R.id.gradelist_sp);

		departmentDao = new DepartmentDao(getApplicationContext());

		if (!isfriend) {
			ID = dataCenterManagerService.getUserSelfContactsEntity()
					.getBaseInfoId().split(",");
		} else {
			ID = friendBaseInfo.split(",");
		}

		unjoinedDepartMap = new HashMap<String, String>();

		// 已加入的班级id
		joinedIdList = new ArrayList<String>();
		joinedDepartmentList = new ArrayList<String>();
		ContactsEntity contactsEntity = dataCenterManagerService
				.getUserSelfContactsEntity();
		String auth = contactsEntity.getAuthenticated();
		String baseinfoId = contactsEntity.getBaseInfoId();
		if (isfriend) {
			baseinfoId = friendBaseInfo;
		}
		
		Set<String> idSet = new HashSet<String>();
		if (auth != null && auth.equals("1")) {
			if (baseinfoId != null && !baseinfoId.equals("")) {
				String baseinfoIds[] = baseinfoId.split(",");
				for (int i = 0; i < baseinfoIds.length; ++i) {
					if (!idSet.contains(baseinfoIds[i])) {
						idSet.add(baseinfoIds[i]);
						joinedIdList.add(baseinfoIds[i]);
					}
				}
				// 已经加入的班级
//				List<String> apartment = new ArrayList<String>();
				//+++lqg+++ 未解决相同的key的问题
				ArrayList<Map<String, Object>> listItem = new ArrayList<Map<String, Object>>();
				for (int i = 0; i < joinedIdList.size(); i++) {
					String baseId = joinedIdList.get(i).substring(0, 16);
					String grade = departmentDao.getDepartmentFullName(baseId);
//					String grade2 = grade.substring(grade.indexOf(",") + 1);
					joinedDepartmentList.add(grade);
					
					String className = departmentDao.getDepartmentFullName(baseId);
					CYLog.d(TAG,className);
					String[] list = className.split(",");
//					String title = list[0];
//					String is = list[1];
//					for (int j = 2; j < list.length; j++) {
//						is = is + list[j];
//					}
//					apartment.add(list[1]);
//					apartment.add(list[3]);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("title", list.length>1?list[1]:className);//防止机构不完整
					map.put("apartmen", list.length>3?list[3]:className);
					listItem.add(map);
				}

				SimpleAdapter mSchedule = new SimpleAdapter(this, // 没什么解释
						listItem,// 数据来源
						R.layout.my_listitem,// ListItem的XML实现
						// 动态数组与ListItem对应的子项
						new String[] { "title", "apartmen" },
						// ListItem的XML文件里面的两个TextView ID
						new int[] { R.id.ItemTitle, R.id.Itemcontent });
				// 添加并且显示
				joined_grade_list.setAdapter(mSchedule);
			}
		}

		// 确认添加
		check_adddata.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					count = flags[0] + flags[1] + flags[2] + flags[3]
							+ flags[4] + flags[5] + flags[6] + flags[7]
							+ flags[8];
					JSONArray Classmates = new JSONArray();
					if (count == 3) {
						for (int i = 0; i < 9; ++i) {
							if (flags[i] == 1) {
								Classmates.put(classmateCheckBoxArray[i]
										.getText());
							}
						}

						try {
							for (int i = 0; i < 3; i++) {
								for (int j = 0; j < Three_classmates.size(); j++) {
									if (Classmates.getString(i).equals(
											Three_classmates.get(j))) {
										signal[i] = true;
									}
								}
							}
							if(signal[0]&&signal[1]&&signal[2]){
								Toast.makeText(getApplicationContext(),
										"数据上传请稍等...", Toast.LENGTH_SHORT).show();
								check_adddata.setEnabled(false);

								JSONArray baseInfoId = new JSONArray();
								JSONObject addClassData = new JSONObject();
								JSONObject addClassOrder = new JSONObject();
								ContactsEntity selfContactsEntity = dataCenterManagerService
										.getUserSelfContactsEntity();
								baseInfoId.put(fullID);
								// gson使用的baseInfoId  baseInfoId_gson
								baseInfoId_gson[0]=baseInfoId.getString(0);
								
								addClassData.put("baseInfoId", baseInfoId);
								addClassData.put("accountNum",
										selfContactsEntity.getAccountNum());
								addClassData.put("password",
										selfContactsEntity.getPassword());
								// 3个本班同学
								JSONArray classmates = new JSONArray();
								classmates.put(Classmates.getString(0));
								classmates.put(Classmates.getString(1));
								classmates.put(Classmates.getString(2));
								classmates_gson[0]=Classmates.getString(0);
								classmates_gson[1]=Classmates.getString(1);
								classmates_gson[2]=Classmates.getString(2);
								addClassData.put("classmates", classmates);
								addClassData.put("phoneNum",
										selfContactsEntity.getPhoneNum());
								addClassData.put("name", userName);
								selfContactsEntity.setName(userName);

								addClassOrder
										.put("command",
												APPConstant.USER_PROFILE_UPDATE_USER_PROFILE);
								addClassOrder.put("content", addClassData);

								// 若为基础id不为空，则表示要添加班级
								if (fullID != null && !fullID.equals("")) {
									try {
										// 聊天服务正常退出
										dataCenterManagerService
												.quitLastTigaseConnection();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

//								GetTask = new HttpupLoad(APPConstant.getUSERURL(),
//										addClassOrder, handler, 3,
//										getApplicationContext());
//								GetTask.execute();
								
								// gson 的使用
								AddClass addClass=new AddClass(baseInfoId_gson, 
										selfContactsEntity.getAccountNum(), 
										selfContactsEntity.getPassword(), 
										classmates_gson, 
										selfContactsEntity.getPhoneNum(), 
										userName);
								HttpCommand httpCommand=new HttpCommand(APPConstant.USER_PROFILE_UPDATE_USER_PROFILE, 
										addClass);
								 String uploadJson=httpCommand.getJsonStr();
								 httpupLoad_gson=new HttpupLoad_gson(APPConstant.getUSERURL(),
										 uploadJson,
										 handler,
										 3,
										 getApplicationContext());
									httpupLoad_gson.execute();
								CYLog.i(TAG,"发送的数据" + addClassOrder);
							}
							else{
								Toast.makeText(getApplicationContext(), "您所选的同学不在该班级",
										Toast.LENGTH_SHORT).show();
								for(int c=0;c<3;c++){
									signal[c]=false;
								}

								for (int i = 0; i < 9; ++i) {
									classmateCheckBoxArray[i].setChecked(false);
								}
								
							//  选择同学失败  重新调用更换同学名单
								
								try {
									if (fullID == null) {
										CYLog.e(TAG,
												"add setOnClickListener onClick fullID is null");
										return;
									}

								

									// 如果本地缓存有班级数据，就不用再次从网络获取
									if (fullIdMap == null || !fullIdMap.containsKey(fullID)) {
										JSONObject classID = new JSONObject();
										JSONObject classgetStr = new JSONObject();
										try {
											classID.put("classId", fullID.substring(0, 16));
											classgetStr
													.put("command",
															APPConstant.USER_PROFILE_GET_CLASSMATES_INFO_LIST);
											classgetStr.put("content", classID);
											CYLog.i(TAG,"classgetStr-------->"
													+ classgetStr);
										} catch (JSONException e) {
											e.printStackTrace();
										}

										GetTask = new HttpupLoad(APPConstant.getUSERURL(),
												classgetStr, handler, 20,
												getApplicationContext());
										GetTask.execute();
									} else {
										// 直接使用本地缓存
										Message msg = new Message();
										msg.what = 20;
										handler.sendMessage(msg);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
								
								
					//  ********************************
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						Toast.makeText(getApplicationContext(), "只能选三个同学",
								Toast.LENGTH_SHORT).show();
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// 添加班级
		addgrade = (Button) findViewById(R.id.addgrade);
		if(userName.equals("")){
			addgrade.setVisibility(View.GONE);
		}
		addgrade.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 若成功了则只允许点一次
				addgrade.setEnabled(false);

				JSONObject testarray = new JSONObject();
				JSONObject result = new JSONObject();

				try {
					testarray.put("name", userName);
					// testarray.put("name", "吴刚");
					testarray.put("schoolNum", APPBaseInfo.SCHOOL_ID_NUMBER);
					result.put("command",
							APPConstant.USER_PROFILE_GET_USER_BASE_INFO_ID_LIST);
					result.put("content", testarray);
					CYLog.i(TAG,"发送的数据------>" + result);
					
//					GetTask = new HttpupLoad(APPConstant.getUSERURL(), result,
//							handler, 7, getActivity());
//					GetTask.execute();
					
					
					GetIdsFromName getIdsFromName=new GetIdsFromName(userName,APPBaseInfo.SCHOOL_ID_NUMBER);
					 HttpCommand httpCommand=new HttpCommand(APPConstant.USER_PROFILE_GET_USER_BASE_INFO_ID_LIST, 
							 getIdsFromName);
					 String uploadJson=httpCommand.getJsonStr();
					 httpupLoad_gson=new HttpupLoad_gson(APPConstant.getUSERURL(),
							 uploadJson,
							 handler,
							 7,
							 getApplicationContext());
						httpupLoad_gson.execute();
				} catch (JSONException e) {
					e.printStackTrace();
					//发送出现异常
					Message msg = new Message();
					msg.what = 7;
					handler.sendMessage(msg);
				}
			}

			private Context getActivity() {
				return null;
			}
		});

		// 换一组班级名单
		makeClassmateList = (Button) findViewById(R.id.makeClassmateList);
		makeClassmateList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (fullID == null) {
						CYLog.e(TAG,
								"add setOnClickListener onClick fullID is null");
						return;
					}
					for (int i = 0; i < 9; ++i) {
						classmateCheckBoxArray[i].setChecked(false);
					}
					makeClassmateList.setEnabled(false);

					// 如果本地缓存有班级数据，就不用再次从网络获取
					if (fullIdMap == null || !fullIdMap.containsKey(fullID)) {
						JSONObject classID = new JSONObject();
						JSONObject classgetStr = new JSONObject();
						try {
							classID.put("classId", fullID.substring(0, 16));
							classgetStr
									.put("command",
											APPConstant.USER_PROFILE_GET_CLASSMATES_INFO_LIST);
							classgetStr.put("content", classID);
							CYLog.i(TAG,"classgetStr-------->"
									+ classgetStr);
						} catch (JSONException e) {
							e.printStackTrace();
						}

						GetTask = new HttpupLoad(APPConstant.getUSERURL(),
								classgetStr, handler, 20,
								getApplicationContext());
						GetTask.execute();
					} else {
						// 直接使用本地缓存
						Message msg = new Message();
						msg.what = 20;
						handler.sendMessage(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

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
