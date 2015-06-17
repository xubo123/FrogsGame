package com.hust.schoolmatechat.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hust.schoolmatechat.postClass.HttpCommand;
import com.hust.schoolmatechat.postClass.HttpupLoad_gson;
import com.hust.schoolmatechat.postClass.RegisterData;
import com.hust.schoolmatechat.postClass.SecretKey;
import com.hust.schoolmatechat.NewsExploreActivitiy;
import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.dao.ClassmateDao;
import com.hust.schoolmatechat.dao.DepartmentDao;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.entity.ContactsEntity;

public class RegisterActivity extends Activity {
	protected static final String TAG = "RegisterActivity";
	// private TextView set_academe,set_profession,set_grade;
	private Button nextstep;
	private EditText account, Name, phoneNum, password, password2,
			RegisterCode, IDlastNum, address;
	private Spinner sexspinner;
	private Intent intent;
	private int sexpositon;
	private Button finishRegi;
	private Button getRegisterCode;
	private Handler handler;

	private postName postname;
	private ArrayList<String> cutID;
	private String fullID;
	private Map<String, String> map;

	private HttpupLoad GetTask;
	private GetHandObj getContent;
	private boolean isreturn = false;
	private boolean checkResult = false;
	private boolean ischeck = false;
	private boolean ischeckCode = false;
	private boolean keyIsempty = false;
//	SQLiteDatabase database;
//	OpenDatabase open;
	private DepartmentDao getGradeList;

	private TextView textWords;
	private Handler handerReg;
	private Context mContext;
	
	//gson ʹ��
	HttpupLoad_gson httpupLoad_gson;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.register);
		init();

		getContent = new GetHandObj();
		getGradeList = new DepartmentDao(this.getApplicationContext());
		postname = new postName();

//		DbOpenHelper HELPER = new DbOpenHelper(getApplicationContext());
//		HELPER.getWritableDatabase();

//		// �����û�id��ѯ�û�Ժϵ��Ϣ
//		open = new OpenDatabase();
//		database = open.openDatabase(this.getApplicationContext());
//		String a = "000150";
//		String s = getGradeList.getDepartment(a);
		// String s = getApartment(database, a);
		// CYLog.i(TAG,"the apartment" + s);
		textWords = (TextView) findViewById(R.id.text_words);
		textWords.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						NewsExploreActivitiy.class);
				intent.putExtra("newsUrl", "file:///android_asset/cy.htm");
				intent.putExtra("userName", "���������ɼ�����Э��");
				startActivity(intent);
			}
		});
//		check = (Button) findViewById(R.id.checkaccount);
//
//		check.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				check.setEnabled(false);
//				JSONObject accountNum = new JSONObject();
//				ischeck = true;
//				JSONObject result = new JSONObject();
//				try {
//					accountNum.put("accountNum", account.getText());
//					result.put("command",
//							APPConstant.USER_PROFILE_CHECK_ACCOUNT_NUM);
//					result.put("content", accountNum);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				// CYLog.i(TAG,"----->" + result);
//				handler = new Handler() {
//
//					@Override
//					public void handleMessage(Message msg) {
//						// TODO Auto-generated method stub
//						switch (msg.what) {
//						case 5:
//							// CYLog.i(TAG,"�˺��Ƿ����"
//							// + GetTask.getLoaddata().getStrResult());
//							check.setEnabled(true);
//							try {
//								checkResult = getContent.getIfsuccess(GetTask
//										.getLoaddata().getStrResult());
//								if (checkResult) {
//
//									// CYLog.i(TAG,"�˺��Ƿ����" +
//									// checkResult);
//									Toast.makeText(getApplicationContext(),
//											"���˺ſ���", Toast.LENGTH_SHORT).show();
//								} else {
//
//									// CYLog.i(TAG,"�˺��Ƿ����" +
//									// checkResult);
//									Toast.makeText(getApplicationContext(),
//											"���˺Ų�����", Toast.LENGTH_SHORT)
//											.show();
//								}
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							break;
//						default:
//							break;
//						}
//					}
//
//				};
//				GetTask = new HttpupLoad(APPConstant.getUSERURL(), result,
//						handler, 5, getApplicationContext());
//				GetTask.execute();
//			}
//
//		});

		// phoneNum.addTextChangedListener(mTextWatcher);
		// phoneNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		//
		// @Override
		// public void onFocusChange(View view, boolean hasFocus) {
		//
		// if (hasFocus) {// ��ý���
		// // ��������ԶԻ�ý�����д���
		// } else {// ʧȥ����
		// // ��������Զ�������ı����ݽ�����Ч����֤
		// EditText num = (EditText) view;
		//
		// if (num.length() < 11) {
		// Toast.makeText(RegisterActivity.this,
		// num.getText() + "����д��ȷ�ֻ���", Toast.LENGTH_SHORT)
		// .show();
		// }
		//
		// }
		// }
		// });

		sexspinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				sexpositon = position;
				// CYLog.i(TAG,"sex" + position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}

		});

		getRegisterCode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isMobileNO(phoneNum.getText().toString())) {
					getRegisterCode.setEnabled(false);
					CountDownTimer mCountDownTimer = new CountDownTimer(
							60 * 1000, 1000) {// ����������ǰһ��ָ����ʱ����ʱ�䣬��һ��ָ�೤ʱ�䵹��һ�¡�
						@Override
						public void onTick(long millisUntilFinished) {
							// TODO Auto-generated method stub
							long s = millisUntilFinished / 1000;
							getRegisterCode.setText(s + "s");
							getRegisterCode
									.setBackgroundColor(R.drawable.button_style);
						}

						@Override
						public void onFinish() {
							// TODO Auto-generated method stub
							getRegisterCode.setEnabled(true);
							getRegisterCode.setText("��ȡ��֤��");
							getRegisterCode
									.setBackgroundResource(R.drawable.menu_cell_background);
							this.cancel();
						}
					};
					mCountDownTimer.start();
					
//					JSONObject content = new JSONObject();
//					JSONObject Register = new JSONObject();
//					try {
//						content.put("phoneNum", phoneNum.getText());
//						content.put("secretKey",
//								APPConstant.REGISTER_CODE_SECRET_KEY);
//						Register.put("content", content);
//						Register.put("command",
//								APPConstant.USER_PROFILE_GET_REGISTER_CODE);
//						handler = new Handler() {
//							@Override
//							public void handleMessage(Message msg) {
//								// TODO Auto-generated method stub
//								switch (msg.what) {
//								case 6:
//									try {
//										Toast.makeText(
//												getApplicationContext(),
//												getContent.getMessage(GetTask
//														.getLoaddata()
//														.getStrResult()),
//												Toast.LENGTH_SHORT).show();
//									} catch (Exception e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
//									break;
//								default:
//									break;
//								}
//							}
//						};
//						GetTask = new HttpupLoad(APPConstant.getUSERURL(),
//								Register, handler, 6, getApplicationContext());
//						GetTask.execute();
//						// CYLog.i(TAG,"��ֵ����������������>" + Register);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}

					
					//gson ʹ��
					handler = new Handler() {
						@Override
						public void handleMessage(Message msg) {
							// TODO Auto-generated method stub
							switch (msg.what) {
							case 6:
								try {
									Toast.makeText(
											getApplicationContext(),
											getContent.getMessage(httpupLoad_gson
													.getLoaddata()
													.getStrResult()),
											Toast.LENGTH_SHORT).show();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
							default:
								break;
							}
						}
					};
					 Gson gson=new Gson();
					 SecretKey secretKey=new SecretKey(phoneNum.getText().toString(), 
							 APPBaseInfo.REGISTER_CODE_SECRET_KEY);
					 HttpCommand httpCommand=new HttpCommand(APPConstant.USER_PROFILE_GET_REGISTER_CODE, 
							 secretKey);
					 String uploadJson=httpCommand.getJsonStr();
					 
					 httpupLoad_gson=new HttpupLoad_gson(APPConstant.getUSERURL(),
							 uploadJson,
							 handler,
							 6,
							 getApplicationContext());
						httpupLoad_gson.execute();
					//gson 
				} else {
					Toast.makeText(getApplicationContext(), "��������ȷ�ֻ���",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		nextstep.setOnClickListener(new OnClickListener() {
			// �ڵ����һ����ʱ�򣬶�ÿ���ؼ������ݽ����жϣ����Ϊ�գ����ؼ�����д�������Ϊ�գ��������һ��
			@Override
			public void onClick(View v) {
				nextstep.setEnabled(false);
				if (!password.getText().toString().equals("")
						&& password.getText().toString().length() > 5) {
					keyIsempty = true;
				} else {
					keyIsempty = false;
				}
				cutID = new ArrayList<String>();
				map = new HashMap<String, String>();
				JSONObject testarray = new JSONObject();
				JSONObject result = new JSONObject();
				ischeckCode = RegisterCode.getText().toString().equals("");
				ischeckCode = !ischeckCode;
				// CYLog.i(TAG,"��֤���Ƿ���д" + ischeckCode);
				try {
					testarray.put("name", Name.getText());
					testarray.put("schoolNum", APPBaseInfo.SCHOOL_ID_NUMBER);
					result.put("command",
							APPConstant.USER_PROFILE_GET_USER_BASE_INFO_ID_LIST);
					result.put("content", testarray);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// CYLog.i(TAG,"----->" + result);
				handler = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						// TODO Auto-generated method stub
						switch (msg.what) {
						case 7:
							nextstep.setEnabled(true);
							// CYLog.i(TAG,"����result"
							// + GetTask.getLoaddata().getStrResult());
							try {
								isreturn = getContent.getIfsuccess(GetTask
										.getLoaddata().getStrResult());
								// CYLog.i(TAG,"������Ϣ" + isreturn);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (isreturn && ischeckCode && keyIsempty) {

								try {
									// CYLog.i(TAG,"����"
									// + getContent.getcutApartID(GetTask
									// .getLoaddata()
									// .getStrResult()));
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								fullID = GetTask.getLoaddata().getStrResult();
								try {
									map = getContent.getApartIDmap(fullID);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								// CYLog.i(TAG,"���map" + map);
								// CYLog.i(TAG,"���full" + fullID);
								try {
									cutID = getContent.getcutApartID(GetTask
											.getLoaddata().getStrResult());
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								// CYLog.i(TAG,"����" + cutID);
								intent = new Intent();
								intent.putExtra("accountNum", account.getText());
								intent.putExtra("name", Name.getText());
								intent.putExtra("sex", sexpositon);
								intent.putExtra("phoneNum", phoneNum.getText());
								intent.putExtra("idNumber", IDlastNum.getText());
								intent.putExtra("address", address.getText());
								intent.putExtra("password", password.getText());
								Random pictureNum = new Random();
								int numPicture = pictureNum.nextInt(19);
								String numStr = String.valueOf(numPicture);
								// String
								// picture="http://219.140.177.108:8088/face_image/"+numStr+".png";
								intent.putExtra("picture", numStr);
								intent.putExtra("checkCode",
										RegisterCode.getText());

								intent.putExtra("IDList", cutID);
								intent.putExtra("fullid", fullID);
								intent.setClass(RegisterActivity.this,
										attestationActivity.class);
								startActivityForResult(intent, 1);
							} else {

								// if (!checkResult) {
								// Toast.makeText(getApplicationContext(),
								// "��ȷ���˺ſ���", Toast.LENGTH_SHORT)
								// .show();
								// }
								// else {
								if (!isreturn) {
									Toast.makeText(getApplicationContext(),
											"��ѯ����������", Toast.LENGTH_SHORT)
											.show();
								} else {

									if (!ischeckCode) {
										Toast.makeText(getApplicationContext(),
												"���ȡ����д��֤��", Toast.LENGTH_SHORT)
												.show();
									}
									if (!keyIsempty) {
										Toast.makeText(getApplicationContext(),
												"����д��������λ������",
												Toast.LENGTH_SHORT).show();
									}
								}
								// }
							}
							break;
						default:
							break;
						}
					}

				};
				GetTask = new HttpupLoad(APPConstant.getUSERURL(), result,
						handler, 7, getApplicationContext());
				GetTask.execute();

				// CYLog.i(TAG,"--->" + GetTask.getResult());

			}

		});
		finishRegi = (Button) findViewById(R.id.finishRegi);
		finishRegi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				JSONObject Registerdata = new JSONObject();
				JSONObject RegisterOrder = new JSONObject();
				if (isMobileNO(phoneNum.getText().toString())) {
					if (passwordCheck(password.getText().toString())) {
						if (password.getText().toString()
								.equals(password2.getText().toString())) {
							// gson ʹ�� 
							
							 
							
								
								handerReg = new Handler() {

									@Override
									public void handleMessage(Message msg) {
										// TODO Auto-generated method stub
										boolean isRe = false;
										switch (msg.what) {
										case 0:
											isRe = getContent.getIfsuccess(httpupLoad_gson
													.getLoaddata().getStrResult());
											if (isRe) {
												Toast.makeText(
														getApplicationContext(),
														"ע��ɹ�", Toast.LENGTH_SHORT)
														.show();
												
												// ע����Ϣ���ش洢
												try {
													JSONObject sss = new JSONObject(
															httpupLoad_gson.getLoaddata()
																	.getStrResult());
													if (sss != null
															&& sss.has("accountNum")) {
														String accountNum = sss
																.getString("accountNum");										
														ClassmateDao classmateDao = new ClassmateDao(
																mContext);
														ContactsEntity contactsEntity = new ContactsEntity();
														contactsEntity
																.setUserAccount(accountNum);
														contactsEntity
																.setAccountNum(accountNum);
														contactsEntity.setName(Name
																.getText()
																.toString());
														contactsEntity
																.setPhoneNum(phoneNum
																		.getText()
																		.toString());
														contactsEntity
																.setPassword(password
																		.getText()
																		.toString());
														if (classmateDao
																.isSelfContactsEntityExisted(accountNum)) {
															classmateDao.updateContacsEntity(contactsEntity);
														} else {
															classmateDao.addContactsEntity(contactsEntity);
														}
													}
												} catch (Exception e) {
													e.printStackTrace();
												}

												Intent goNextPage = new Intent();
												goNextPage.putExtra("name",
														Name.getText());
												goNextPage.putExtra("phoneNum",
														phoneNum.getText());
												goNextPage.putExtra("checkCode",
														RegisterCode.getText());
												goNextPage.putExtra("password",
														password.getText());

												goNextPage
														.setClass(
																RegisterActivity.this,
																ChooseAttentionActivity.class);
												startActivity(goNextPage);
												finish();
											} else {
												Toast.makeText(
														getApplicationContext(),
														getContent
																.getMessage(httpupLoad_gson
																		.getLoaddata()
																		.getStrResult()),
														Toast.LENGTH_SHORT).show();
											}
											break;
										}
									}

								};
								RegisterData registerData=new RegisterData(Name.getText().toString(), phoneNum.getText().toString(),
										RegisterCode.getText().toString(), password.getText().toString());
								 HttpCommand httpCommand=new HttpCommand(APPConstant.USER_PROFILE_REGISTER, 
										 registerData);
								 String uploadJson=httpCommand.getJsonStr();
								 httpupLoad_gson=new HttpupLoad_gson(APPConstant.getUSERURL(),
										 uploadJson,
										 handerReg,
										 0,
										 getApplicationContext());
									httpupLoad_gson.execute();
									CYLog.i(TAG,"���͵�����" + uploadJson);
							//gson ʹ��
								
							
//							Registerdata = getRegisterData();
//							RegisterOrder = new JSONObject();
//							try {
//								RegisterOrder.put("command",
//										APPConstant.USER_PROFILE_REGISTER);
//								RegisterOrder.put("content", Registerdata);
//							} catch (JSONException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							CYLog.i(TAG,"���͵�����" + RegisterOrder);
//							// ����ע�ᣬ���ע��ɹ������ݷ��͵���һ��ҳ��
//
//							handerReg = new Handler() {
//
//								@Override
//								public void handleMessage(Message msg) {
//									// TODO Auto-generated method stub
//									boolean isRe = false;
//									switch (msg.what) {
//									case 0:
//										isRe = getContent.getIfsuccess(GetTask
//												.getLoaddata().getStrResult());
//										if (isRe) {
//											Toast.makeText(
//													getApplicationContext(),
//													"ע��ɹ�", Toast.LENGTH_SHORT)
//													.show();
//											
//											// ע����Ϣ���ش洢
//											try {
//												JSONObject sss = new JSONObject(
//														GetTask.getLoaddata()
//																.getStrResult());
//												if (sss != null
//														&& sss.has("accountNum")) {
//													String accountNum = sss
//															.getString("accountNum");										
//													ClassmateDao classmateDao = new ClassmateDao(
//															mContext);
//													ContactsEntity contactsEntity = new ContactsEntity();
//													contactsEntity
//															.setUserAccount(accountNum);
//													contactsEntity
//															.setAccountNum(accountNum);
//													contactsEntity.setName(Name
//															.getText()
//															.toString());
//													contactsEntity
//															.setPhoneNum(phoneNum
//																	.getText()
//																	.toString());
//													contactsEntity
//															.setPassword(password
//																	.getText()
//																	.toString());
//													if (classmateDao
//															.isSelfContactsEntityExisted(accountNum)) {
//														classmateDao.updateContacsEntity(contactsEntity);
//													} else {
//														classmateDao.addContactsEntity(contactsEntity);
//													}
//												}
//											} catch (Exception e) {
//												e.printStackTrace();
//											}
//
//											Intent goNextPage = new Intent();
//											goNextPage.putExtra("name",
//													Name.getText());
//											goNextPage.putExtra("phoneNum",
//													phoneNum.getText());
//											goNextPage.putExtra("checkCode",
//													RegisterCode.getText());
//											goNextPage.putExtra("password",
//													password.getText());
//
//											goNextPage
//													.setClass(
//															RegisterActivity.this,
//															ChooseAttentionActivity.class);
//											startActivity(goNextPage);
//											finish();
//										} else {
//											Toast.makeText(
//													getApplicationContext(),
//													getContent
//															.getMessage(GetTask
//																	.getLoaddata()
//																	.getStrResult()),
//													Toast.LENGTH_SHORT).show();
//										}
//										break;
//									}
//								}
//
//							};
//
//							GetTask = new HttpupLoad(APPConstant.getUSERURL(),
//									RegisterOrder, handerReg, 0,
//									getApplicationContext());
//							GetTask.execute();
//
//							CYLog.i(TAG,"ע�����ݡ�����������������" + RegisterOrder);
							// Toast.makeText(getApplicationContext(), "���ע��",
							// Toast.LENGTH_SHORT).show();
							//
						} else {
							Toast.makeText(getApplicationContext(),
									"��ȷ���������������һ��", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getApplicationContext(), "������6λ��������",
								Toast.LENGTH_SHORT).show();

					}
				} else {
					Toast.makeText(getApplicationContext(), "��������ȷ�ֻ���",
							Toast.LENGTH_SHORT).show();
				}

			}

		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onResume();
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 1) {
			// ע��ɹ�
			finish();
		}
	}

	TextWatcher mTextWatcher = new TextWatcher() {
		private CharSequence temp;
		private int editStart;
		private int editEnd;

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			temp = s;

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			// mTextView.setText(s);//�����������ʵʱ��ʾ
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			editStart = phoneNum.getSelectionStart();
			editEnd = phoneNum.getSelectionEnd();
			// mTextView.setText("��������" + temp.length() + "���ַ�");
			if (temp.length() > 11) {
				Toast.makeText(RegisterActivity.this, "������������Ѿ����������ƣ�",
						Toast.LENGTH_SHORT).show();
				s.delete(editStart - 1, editEnd);
				int tempSelection = editStart;
				// mEditText.setText(s);
				// mEditText.setSelection(tempSelection);
			}
		}

	};

	private JSONObject getRegisterData() {
		// TODO Auto-generated method stub
		JSONObject json = new JSONObject();
		try {
			json.put("name", Name.getText());
			json.put("phoneNum", phoneNum.getText());
			json.put("checkCode", RegisterCode.getText());
			json.put("password", password.getText());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	public void init() {
		nextstep = (Button) findViewById(R.id.nextstep);
		account = (EditText) findViewById(R.id.account);
		Name = (EditText) findViewById(R.id.Name);
		phoneNum = (EditText) findViewById(R.id.phoneNum);
		password = (EditText) findViewById(R.id.password1);
		password2 = (EditText) findViewById(R.id.password2);
		sexspinner = (Spinner) findViewById(R.id.sexspinner);
		RegisterCode = (EditText) findViewById(R.id.RegisterCode);
		IDlastNum = (EditText) findViewById(R.id.IDlastNum);
		address = (EditText) findViewById(R.id.address);
		getRegisterCode = (Button) findViewById(R.id.getRegisterCode);

	}

	// private String getApartment(SQLiteDatabase db, String id) {
	// String ID = "'" + id + "'";
	//
	// Cursor c = db.rawQuery("SELECT * FROM cy_dept WHERE dept_id = " + ID,
	// null);
	// String name = null;
	// while (c.moveToNext()) {
	// String _id = c.getString(c.getColumnIndex("dept_id"));
	// name = c.getString(c.getColumnIndex("full_name"));
	//
	// CYLog.i("db", "_id=>" + _id + ", name=>" + name);
	// }
	// c.close();
	// return name;
	//
	// }
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

	public static boolean isMobileNO(String mobiles) {
//		CYLog.d(TAG, "ע���ֻ���"+mobiles);
		if (mobiles == null) {
			return false;
		}
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9])|(14[5,7])|(17[0-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	public boolean passwordCheck(String password) {
		boolean is = false;
		char[] passwordChar = password.toCharArray();
		if (passwordChar.length > 5) {
			is = true;
		}
		return is;
	}

}
