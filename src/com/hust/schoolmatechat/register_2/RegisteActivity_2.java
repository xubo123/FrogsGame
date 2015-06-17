package com.hust.schoolmatechat.register_2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.hust.schoolmatechat.GetHandObj;
import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.dao.ClassmateDao;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.postClass.HttpCommand;
import com.hust.schoolmatechat.postClass.HttpupLoad_gson;
import com.hust.schoolmatechat.postClass.RegisterData;
import com.hust.schoolmatechat.postClass.SecretKey;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class RegisteActivity_2 extends Activity {
	private Button SchoolChoose_btn;
	private Button complete_registe, getCode;
	private EditText edit_school, name_2, phoneNum_2, registeCode_2,
			password_2, passwordcheck_2;
	HttpupLoad_gson httpupLoad_gson;
	private GetHandObj getContent;
	protected static final String TAG = "RegisteActivity_2";
	private Context mContext;
	boolean flag = false;
	String SchoolName;
	String SchoolId;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			boolean isRe = false;
			switch (msg.what) {
			case 6:
				try {
					String s = httpupLoad_gson.getLoaddata().getStrResult();
					Toast.makeText(
							getApplicationContext(),
							getContent.getMessage(httpupLoad_gson.getLoaddata()
									.getStrResult()), Toast.LENGTH_SHORT)
							.show();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			case 0:
				isRe = getContent.getIfsuccess(httpupLoad_gson.getLoaddata()
						.getStrResult());
				if (isRe) {
					Toast.makeText(getApplicationContext(), "注册成功",
							Toast.LENGTH_SHORT).show();

					// 注册信息本地存储
					try {
						JSONObject sss = new JSONObject(httpupLoad_gson
								.getLoaddata().getStrResult());
						if (sss != null && sss.has("accountNum")) {
							String accountNum = sss.getString("accountNum");
							ClassmateDao classmateDao = new ClassmateDao(
									mContext);
							ContactsEntity contactsEntity = new ContactsEntity();
							contactsEntity.setUserAccount(accountNum);
							contactsEntity.setAccountNum(accountNum);
							contactsEntity.setName(name_2.getText().toString());
							contactsEntity.setPhoneNum(phoneNum_2.getText()
									.toString());
							contactsEntity.setPassword(password_2.getText()
									.toString());
							if (classmateDao
									.isSelfContactsEntityExisted(accountNum)) {
								classmateDao
										.updateContacsEntity(contactsEntity);
							} else {
								classmateDao.addContactsEntity(contactsEntity);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					Intent goNextPage = new Intent();
					goNextPage.putExtra("name_2", name_2.getText().toString());
					goNextPage.putExtra("phoneNum_2", phoneNum_2.getText()
							.toString());
					goNextPage.putExtra("registeCode_2", registeCode_2
							.getText().toString());
					goNextPage.putExtra("password_2", password_2.getText()
							.toString());

					goNextPage.setClass(RegisteActivity_2.this,
							Complete_registeActivity.class);
					startActivity(goNextPage);
					finish();
				} else {
					Toast.makeText(
							getApplicationContext(),
							getContent.getMessage(httpupLoad_gson.getLoaddata()
									.getStrResult()), Toast.LENGTH_SHORT)
							.show();
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registe2);
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			flag = bundle.getBoolean("flag");
			SchoolName = bundle.getString("schoolname");
			SchoolId = bundle.getString("schoolId");
		}

		mContext = this;
		getContent = new GetHandObj();
		edit_school = (EditText) findViewById(R.id.edit_school);
		edit_school.setEnabled(false);
		if (flag) {
			edit_school.setText(SchoolName);
			Toast.makeText(getApplicationContext(), "" + SchoolId,
					Toast.LENGTH_SHORT).show();
		}
		name_2 = (EditText) findViewById(R.id.name_2);
		phoneNum_2 = (EditText) findViewById(R.id.phoneNum_2);
		registeCode_2 = (EditText) findViewById(R.id.registeCode_2);
		password_2 = (EditText) findViewById(R.id.password_2);
		passwordcheck_2 = (EditText) findViewById(R.id.passwordcheck_2);

		getCode = (Button) findViewById(R.id.getCode);
		SchoolChoose_btn = (Button) findViewById(R.id.choose_school_btn);
		complete_registe = (Button) findViewById(R.id.complete_registe);
		getCode.setOnClickListener(listener);
		complete_registe.setOnClickListener(listener);
		SchoolChoose_btn.setOnClickListener(listener);
	}

	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.choose_school_btn:
				Intent intent = new Intent();
				intent.putExtra("Activity","Register");
				intent.setClass(getApplicationContext(),
						ProvinceChooseActivity.class);
				startActivity(intent);
				break;
			case R.id.complete_registe:
				// Intent intent_1 = new Intent();
				// intent_1.setClass(getApplicationContext(),
				// Complete_registeActivity.class);
				// startActivity(intent_1);
				if (flag) {

				JSONObject Registerdata = new JSONObject();
				JSONObject RegisterOrder = new JSONObject();
				if (isMobileNO(phoneNum_2.getText().toString())) {
					if (passwordCheck(password_2.getText().toString())) {
						if (password_2.getText().toString()
								.equals(passwordcheck_2.getText().toString())) {
							// gson 使用

							RegisterData registerData = new RegisterData(name_2
									.getText().toString(), phoneNum_2.getText()
									.toString(), registeCode_2.getText()
									.toString(), password_2.getText()
									.toString());
							HttpCommand httpCommand = new HttpCommand(
									APPConstant.USER_PROFILE_REGISTER,
									registerData);
							String uploadJson = httpCommand.getJsonStr();
							httpupLoad_gson = new HttpupLoad_gson(
									APPConstant.getUSERURL(), uploadJson,
									handler, 0, getApplicationContext());
							httpupLoad_gson.execute();
							CYLog.i(TAG, "发送的数据" + uploadJson);
							// gson 使用

						} else {
							Toast.makeText(getApplicationContext(),
									"请确认两次输入的密码一致", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getApplicationContext(), "请输入6位以上密码",
								Toast.LENGTH_SHORT).show();

					}
				} else {
					Toast.makeText(getApplicationContext(), "请输入正确手机号",
							Toast.LENGTH_SHORT).show();
				}
				}
				else {
					Toast.makeText(getApplicationContext(), "请先选择学校",
							Toast.LENGTH_SHORT).show();
				}
				break;

			case R.id.getCode:
				
				if (flag) {
				if (isMobileNO(phoneNum_2.getText().toString())) {
					getCode.setEnabled(false);

					CountDownTimer mCountDownTimer = new CountDownTimer(
							60 * 1000, 1000) {// 两个参数，前一个指倒计时的总时间，后一个指多长时间倒数一下。
						@Override
						public void onTick(long millisUntilFinished) {
							// TODO Auto-generated method stub
							long s = millisUntilFinished / 1000;
							getCode.setText(s + "s");
							getCode.setBackgroundColor(R.drawable.button_style);
						}

						@Override
						public void onFinish() {
							// TODO Auto-generated method stub
							getCode.setEnabled(true);
							getCode.setText("获取验证码");
							getCode.setBackgroundResource(R.drawable.menu_cell_background);
							this.cancel();
						}
					};
					mCountDownTimer.start();
					Gson gson = new Gson();
					SecretKey secretKey = new SecretKey(phoneNum_2.getText()
							.toString(), APPBaseInfo.REGISTER_CODE_SECRET_KEY);
					HttpCommand httpCommand = new HttpCommand(
							APPConstant.USER_PROFILE_GET_REGISTER_CODE,
							secretKey);
					String uploadJson = httpCommand.getJsonStr();
					CYLog.i(TAG, "------>url" + APPConstant.getUSERURL());
					httpupLoad_gson = new HttpupLoad_gson(
							APPConstant.getUSERURL(), uploadJson, handler, 6,
							getApplicationContext());
					httpupLoad_gson.execute();
				} else {
					Toast.makeText(getApplicationContext(), "请输入正确手机号",
							Toast.LENGTH_SHORT).show();
				}
				}
				else {
					Toast.makeText(getApplicationContext(), "请先选择学校",
							Toast.LENGTH_SHORT).show();
				}
				break;

			default:
				break;
			}

		}
	};

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.school_choose, menu);
//		return true;
//	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}
		return true;
	}

	public static boolean isMobileNO(String mobiles) {
		// CYLog.d(TAG, "注册手机号"+mobiles);
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
