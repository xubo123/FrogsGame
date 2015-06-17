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
	private List<String> joinedDepartmentList;// �Ѿ�����İ༶
	private ArrayList<String> joinedIdList;// �Ѿ�����İ༶id
	private Map<String, String> unjoinedDepartMap;// �»�ȡ�İ༶�б�
	private DepartmentDao departmentDao;
	private ListView joined_grade_list;
	private RelativeLayout gradelist_rl;
	private Spinner gradelist_sp;
	private Button addgrade, makeClassmateList;
	private Map<String, String> fullIdMap;// ����༶��������
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

	String students[] = { "����", "����", "�", "���", "������", "��־��", "������", "�ܷ�",
			"�����", "�β�", "������", "����", "�⿭", "ʷ��", "κ��ũ", "����", "�պ�", "����",
			"����", "�����", "��ΰ", "����", "����", "����ѩ", "����", "������", "�", "����",
			"��Ƽ", "����", "���", "��ٻ", "������", "�����", "������", "�����", "Ϳ��", "����",
			"л��", "��һ��", "л�ɻ�", "�½�", "�±���", "����ΰ", "��ΰ��", "����", "����", "����",
			"������", "����", "����", "����", "�Ž���", "��׿", "¥��", "������", "������", "������",
			"��ά��", "�����", "����", "����", "ʯ��÷", "������", "������", "Ԭ��", "����Ӣ", "���غ�",
			"������", "����Ⱥ", "Ф����", "������", "���Ա�", "���ܷ�", "�����", "������", "����ɺ",
			"�½���", "����", "���Ƶ�", "�󾧾�", "����", "��ƽ��", "�޹��", "����", "��Ϊ��", "��ΰ",
			"������", "�պ��", "���ƾ�", "֣����", "�º��", "�Ի���", "������", "������", "�첨",
			"�����", "�޹���", "��ȫ��", "����" };

	private int[] flags = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private int count = 0;
	boolean[] signal = { false, false, false };
	private ArrayList<String> namelist;
	private String userName;
	private Context mConext;
	
	
	//gson ʹ��
	HttpupLoad_gson httpupLoad_gson;
	String[] baseInfoId_gson=new String[1];
	String[] classmates_gson=new String[3];
	ArrayList<String> Three_classmates ;
	

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
//			case 3:
//				CYLog.i(TAG,"�޸Ľ��"
//						+ GetTask.getLoaddata().getStrResult());
//				try {
//					JSONObject json = new JSONObject(GetTask.getLoaddata()
//							.getStrResult());
//					boolean idString = json.getBoolean("success");
//					// �ϴ��ɹ�,�޸ı�������
//					if (idString) {
//						Toast.makeText(getApplicationContext(),
//								"��ϲ�㣬��ӳɹ�!�����µ�¼", Toast.LENGTH_SHORT).show();
//
//						ContactsEntity contactsEntity = dataCenterManagerService
//								.getUserSelfContactsEntity();
//						// StringBuffer baseInfoBuf = new StringBuffer();
//						// Set<String> baseInfoSet = new HashSet<String>();
//						//
//						// // ���ش洢��id��Ϣ
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
//						// // ���������ص�id��Ϣ
//						// JSONArray obj = json.getJSONArray("obj");
//						// if (obj != null && obj.length() > 0) {
//						// // ����û��id��Ϣ����Ϊ��һ����֤��Ӱ༶
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
//						// ��������û���Ϣ�����û����µ�¼���ӷ�������ȡ
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
//						// �˺ſ��Լ�ס��������գ��������
//						prefs.edit().putString("PASS", "").commit();
//
//						Intent intent_1 = new Intent();
//						intent_1.setClass(mConext, LoginActivity.class);
//						startActivity(intent_1);
//						finish();
//						break;
//					} else {
//						Toast.makeText(getApplicationContext(),
//								"���ʧ�ܣ������������ͬѧѡ���Ƿ���ȷ!", Toast.LENGTH_SHORT)
//								.show();
//
//						// ��Ϊ����id��Ϊ�գ����ʾҪ��Ӱ༶
//						if (fullID != null && !fullID.equals("")) {
//							// �ж��Ƿ�Ϊ����֤�û������ǣ������µ�¼�������
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
				CYLog.i(TAG,"�޸Ľ��"
						+ httpupLoad_gson.getLoaddata().getStrResult());
				try {
					JSONObject json = new JSONObject(httpupLoad_gson.getLoaddata()
							.getStrResult());
					boolean idString = json.getBoolean("success");
					// �ϴ��ɹ�,�޸ı�������
					if (idString) {
						Toast.makeText(getApplicationContext(),
								"��ϲ�㣬��ӳɹ�!�Ժ�Ӧ�ý�����", Toast.LENGTH_SHORT).show();

						ContactsEntity contactsEntity = dataCenterManagerService
								.getUserSelfContactsEntity();
						// StringBuffer baseInfoBuf = new StringBuffer();
						// Set<String> baseInfoSet = new HashSet<String>();
						//
						// // ���ش洢��id��Ϣ
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
						// // ���������ص�id��Ϣ
						// JSONArray obj = json.getJSONArray("obj");
						// if (obj != null && obj.length() > 0) {
						// // ����û��id��Ϣ����Ϊ��һ����֤��Ӱ༶
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
						// ��������û���Ϣ�����û����µ�¼���ӷ�������ȡ
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
						// �˺ſ��Լ�ס��������գ��������
						//prefs.edit().putString("PASS", "").commit();

						Intent intent_1 = new Intent();
						intent_1.setClass(mConext, LogoActivity.class);
						startActivity(intent_1);
						finish();
						break;
					} else {
						Toast.makeText(getApplicationContext(),
								"���ʧ�ܣ������������ͬѧѡ���Ƿ���ȷ!", Toast.LENGTH_SHORT)
								.show();

						// ��Ϊ����id��Ϊ�գ����ʾҪ��Ӱ༶
						if (fullID != null && !fullID.equals("")) {
							// �ж��Ƿ�Ϊ����֤�û������ǣ������µ�¼�������
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
//						// ��ȡʧ���˿����ٴλ�ȡ
//						addgrade.setEnabled(true);
//						return;
//					}
//					JSONObject returnJson = new JSONObject(returnStr);
//					boolean success = returnJson.getBoolean("success");
//					if (success == false) {
//						// ��ȡʧ���˿����ٴλ�ȡ
//						addgrade.setEnabled(true);
//						Toast.makeText(getApplicationContext(), "��ȡ�༶����ʧ�ܣ�������",
//								Toast.LENGTH_SHORT).show();
//						return;
//					}
//
//					// �Ѽ���İ༶id
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
//					// δ����İ༶id
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
//						//ȡ��web���ݹ����Ļ���ȫ��
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
//							//����֮ǰ�İ汾
//							if (msgs != null && msgs.length > i) {
//								departmentFullName = msgs[i];
//							}
//							if (departmentFullName == null || departmentFullName.equals("")) {
//								continue;
//							}
//							//���ݴ汾��
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
//						// ��ȡʧ���˿����ٴλ�ȡ
//						addgrade.setEnabled(true);
//						Toast.makeText(getApplicationContext(), "��ѯ���������ڵİ༶!",
//								Toast.LENGTH_SHORT).show();
//						return;
//					}
//
//					// ����ʾ�б�
//					if (unjoinedDepartmentList.size() == 0) {
//						addgrade.setVisibility(View.GONE);
//						Toast.makeText(getApplicationContext(), "���İ༶�Ѿ�ȫ�����!",
//								Toast.LENGTH_SHORT).show();
//						gradelist_rl.setVisibility(View.GONE);
//						textView1_addclass.setVisibility(View.GONE);
//						break;
//					} else {
//						textView1_addclass.setVisibility(View.VISIBLE);
//						gradelist_rl.setVisibility(View.VISIBLE);
//					}
//					// ������������������
//					ArrayAdapter<String> gradeAdapter = new ArrayAdapter<String>(
//							getApplicationContext(),
//							R.layout.simple_spinner_item,
//							unjoinedDepartmentList);
//					gradelist_sp.setAdapter(gradeAdapter);
//					// ���spinnger�¼�������
//					gradelist_sp
//							.setOnItemSelectedListener(new OnItemSelectedListener() {
//								@Override
//								public void onItemSelected(
//										AdapterView<?> parent, View view,
//										int position, long id) {
//									String aprtment_1 = parent
//											.getItemAtPosition(position)
//											.toString();
//									// ͨ��classmap�õ��༶��ȫ��,�����õ��༶��Ӧ��id,Ȼ���ٸ���map���鵽�û���baseinfoIds
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
//					// ��ȡʧ���˿����ٴλ�ȡ
//					addgrade.setEnabled(true);
//					Toast.makeText(getApplicationContext(), "��ȡ�༶����ʧ�ܣ�������",
//							Toast.LENGTH_SHORT).show();
//				}
//				break;
				
				
				// gson ��case 7 
			case 7:
				try {
					String returnStr = httpupLoad_gson.getLoaddata().getStrResult();
					if (returnStr == null) {
						// ��ȡʧ���˿����ٴλ�ȡ
						addgrade.setEnabled(true);
						return;
					}
					JSONObject returnJson = new JSONObject(returnStr);
					boolean success = returnJson.getBoolean("success");
					if (success == false) {
						// ��ȡʧ���˿����ٴλ�ȡ
						addgrade.setEnabled(true);
						Toast.makeText(getApplicationContext(), "��ȡ�༶����ʧ�ܣ�������",
								Toast.LENGTH_SHORT).show();
						return;
					}

					// �Ѽ���İ༶id
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

					// δ����İ༶id
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
						//ȡ��web���ݹ����Ļ���ȫ��
						try {
							msgs = classMsg.split("_");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					// �ѻ�õĻ�������ȫ�����
					
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
							//����֮ǰ�İ汾
							if (msgs != null && msgs.length > i) {
								departmentFullName = msgs[i];
							}
							if (departmentFullName == null || departmentFullName.equals("")) {
								continue;
							}
							//���ݴ汾��
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
						// ��ȡʧ���˿����ٴλ�ȡ
						addgrade.setEnabled(true);
						Toast.makeText(getApplicationContext(), "��ѯ���������ڵİ༶!",
								Toast.LENGTH_SHORT).show();
						return;
					}

					// ����ʾ�б�
					if (unjoinedDepartmentList.size() == 0) {
						addgrade.setVisibility(View.GONE);
						Toast.makeText(getApplicationContext(), "���İ༶�Ѿ�ȫ�����!",
								Toast.LENGTH_SHORT).show();
						gradelist_rl.setVisibility(View.GONE);
						textView1_addclass.setVisibility(View.GONE);
						break;
					} else {
						textView1_addclass.setVisibility(View.VISIBLE);
						gradelist_rl.setVisibility(View.VISIBLE);
					}
					// ������������������
					ArrayAdapter<String> gradeAdapter = new ArrayAdapter<String>(
							getApplicationContext(),
							R.layout.simple_spinner_item,
							unjoinedDepartmentList);
					gradelist_sp.setAdapter(gradeAdapter);
					// ���spinnger�¼�������
					gradelist_sp
							.setOnItemSelectedListener(new OnItemSelectedListener() {
								@Override
								public void onItemSelected(
										AdapterView<?> parent, View view,
										int position, long id) {
									String aprtment_1 = parent
											.getItemAtPosition(position)
											.toString();
									// ͨ��classmap�õ��༶��ȫ��,�����õ��༶��Ӧ��id,Ȼ���ٸ���map���鵽�û���baseinfoIds
									fullID = unjoinedDepartMap.get(aprtment_1);
								}

								@Override
								public void onNothingSelected(
										AdapterView<?> parent) {
								}
							});
				} catch (Exception e) {
					e.printStackTrace();
					
					// ��ȡʧ���˿����ٴλ�ȡ
					addgrade.setEnabled(true);
					Toast.makeText(getApplicationContext(), "��ȡ�༶����ʧ�ܣ�������",
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

					// �����л��棬ֱ��ʹ�û��棬û�л�����������������ж�
					String idString = null;
					if (!fullIdMap.containsKey(fullID)) {
						JSONObject json;
						try {
							json = new JSONObject(GetTask.getLoaddata()
									.getStrResult());
							boolean success = json.getBoolean("success");
							if (!success) {
								Toast.makeText(getApplicationContext(),
										"��ȡ�༶����ʧ�ܣ�������", Toast.LENGTH_SHORT)
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
						// �������ϻ�ȡ�İ༶����
						sss = new JSONObject(GetTask.getLoaddata()
								.getStrResult());
						idString = sss.getString("obj");
						makeClassmateList.setText("��һ������");

						fullIdMap.put(fullID, idString);
					} else {
						idString = fullIdMap.get(fullID);
						System.out
								.println("makeClassmateList using memory cache : "
										+ idString);
					}

					// �ɹ���ȡ�༶��������
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
					//���԰汾����ʾ3���༶ͬѧ������
					if (APPConstant.DEBUG_MODE) {
						CYLog.i(TAG,namelist.toString());
						Toast.makeText(getApplicationContext(),
								"�༶ͬѧ " + namelist.toString(),
								Toast.LENGTH_SHORT).show();
					}

					// ��ȡ����6��������
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

					// ����ԭ����˳��
					Collections.sort(namelist, new Comparator<String>() {
						public int compare(String arg0, String arg1) {
							return arg0.compareTo(arg1);
						}
					});

					// ��ʾ������
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

	/** �Խ��������ķ��� */
	private DataCenterManagerService dataCenterManagerService;
	ServiceConnection dataCenterManagerIntentConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			dataCenterManagerService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// ����һ��MsgService����
			dataCenterManagerService = ((DataCenterManagerBiner) service)
					.getService();

			initStudyExActivity();
		}
	};

	@Override
	public void onDestroy() {
		// add ȡ���� ibind
		unbindService(dataCenterManagerIntentConn);
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * �����������Ĺ������
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
			bar.setTitle("ѧϰ����");
		}else{
			bar.setTitle("�ҵ�ѧϰ����");
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

		// �Ѽ���İ༶id
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
				// �Ѿ�����İ༶
//				List<String> apartment = new ArrayList<String>();
				//+++lqg+++ δ�����ͬ��key������
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
					map.put("title", list.length>1?list[1]:className);//��ֹ����������
					map.put("apartmen", list.length>3?list[3]:className);
					listItem.add(map);
				}

				SimpleAdapter mSchedule = new SimpleAdapter(this, // ûʲô����
						listItem,// ������Դ
						R.layout.my_listitem,// ListItem��XMLʵ��
						// ��̬������ListItem��Ӧ������
						new String[] { "title", "apartmen" },
						// ListItem��XML�ļ����������TextView ID
						new int[] { R.id.ItemTitle, R.id.Itemcontent });
				// ��Ӳ�����ʾ
				joined_grade_list.setAdapter(mSchedule);
			}
		}

		// ȷ�����
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
										"�����ϴ����Ե�...", Toast.LENGTH_SHORT).show();
								check_adddata.setEnabled(false);

								JSONArray baseInfoId = new JSONArray();
								JSONObject addClassData = new JSONObject();
								JSONObject addClassOrder = new JSONObject();
								ContactsEntity selfContactsEntity = dataCenterManagerService
										.getUserSelfContactsEntity();
								baseInfoId.put(fullID);
								// gsonʹ�õ�baseInfoId  baseInfoId_gson
								baseInfoId_gson[0]=baseInfoId.getString(0);
								
								addClassData.put("baseInfoId", baseInfoId);
								addClassData.put("accountNum",
										selfContactsEntity.getAccountNum());
								addClassData.put("password",
										selfContactsEntity.getPassword());
								// 3������ͬѧ
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

								// ��Ϊ����id��Ϊ�գ����ʾҪ��Ӱ༶
								if (fullID != null && !fullID.equals("")) {
									try {
										// ������������˳�
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
								
								// gson ��ʹ��
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
								CYLog.i(TAG,"���͵�����" + addClassOrder);
							}
							else{
								Toast.makeText(getApplicationContext(), "����ѡ��ͬѧ���ڸð༶",
										Toast.LENGTH_SHORT).show();
								for(int c=0;c<3;c++){
									signal[c]=false;
								}

								for (int i = 0; i < 9; ++i) {
									classmateCheckBoxArray[i].setChecked(false);
								}
								
							//  ѡ��ͬѧʧ��  ���µ��ø���ͬѧ����
								
								try {
									if (fullID == null) {
										CYLog.e(TAG,
												"add setOnClickListener onClick fullID is null");
										return;
									}

								

									// ������ػ����а༶���ݣ��Ͳ����ٴδ������ȡ
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
										// ֱ��ʹ�ñ��ػ���
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
						Toast.makeText(getApplicationContext(), "ֻ��ѡ����ͬѧ",
								Toast.LENGTH_SHORT).show();
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// ��Ӱ༶
		addgrade = (Button) findViewById(R.id.addgrade);
		if(userName.equals("")){
			addgrade.setVisibility(View.GONE);
		}
		addgrade.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// ���ɹ�����ֻ�����һ��
				addgrade.setEnabled(false);

				JSONObject testarray = new JSONObject();
				JSONObject result = new JSONObject();

				try {
					testarray.put("name", userName);
					// testarray.put("name", "���");
					testarray.put("schoolNum", APPBaseInfo.SCHOOL_ID_NUMBER);
					result.put("command",
							APPConstant.USER_PROFILE_GET_USER_BASE_INFO_ID_LIST);
					result.put("content", testarray);
					CYLog.i(TAG,"���͵�����------>" + result);
					
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
					//���ͳ����쳣
					Message msg = new Message();
					msg.what = 7;
					handler.sendMessage(msg);
				}
			}

			private Context getActivity() {
				return null;
			}
		});

		// ��һ��༶����
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

					// ������ػ����а༶���ݣ��Ͳ����ٴδ������ȡ
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
						// ֱ��ʹ�ñ��ػ���
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
