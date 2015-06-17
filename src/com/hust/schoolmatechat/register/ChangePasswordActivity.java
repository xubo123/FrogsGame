package com.hust.schoolmatechat.register;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.dao.ClassmateDao;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.entity.ContactsEntity;

public class ChangePasswordActivity extends Activity {
	protected static final String TAG = "ChangePasswordActivity";
	EditText phoneNum, checkCode_edit, password, password2;
	Button checkCode, changePassword;
	private Handler handler, changepasswordHandler;
	private HttpupLoad GetTask;
	private GetHandObj getContent;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = this;
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.changepassword);
		getContent = new GetHandObj();
		phoneNum = (EditText) findViewById(R.id.phoneNum_Edi);
		checkCode_edit = (EditText) findViewById(R.id.checkCode_change);
		password = (EditText) findViewById(R.id.password_edi_change);
		password2 = (EditText) findViewById(R.id.password_edi_change2);
		checkCode = (Button) findViewById(R.id.getRegisterCode_change);
		checkCode.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isMobileNO(phoneNum.getText().toString())) {

					checkCode.setEnabled(false);
					CountDownTimer mCountDownTimer = new CountDownTimer(
							60 * 1000, 1000) {// 两个参数，前一个指倒计时的总时间，后一个指多长时间倒数一下。
						@Override
						public void onTick(long millisUntilFinished) {
							// TODO Auto-generated method stub
							long s = millisUntilFinished / 1000;
							checkCode.setText(s + "s");
							checkCode.setBackgroundColor(R.drawable.button_style);
						}

						@Override
						public void onFinish() {
							// TODO Auto-generated method stub
							checkCode.setEnabled(true);
							checkCode.setText("获取验证码");
							checkCode.setBackgroundResource(R.drawable.menu_cell_background);
							this.cancel();
						}
					};
					mCountDownTimer.start();
					JSONObject phoneNumData = new JSONObject();
					JSONObject phoneNumorder = new JSONObject();
					try {
						phoneNumData.put("phoneNum", phoneNum.getText());
						phoneNumData.put("secretKey",
								APPBaseInfo.REGISTER_CODE_SECRET_KEY);
						phoneNumorder.put("content", phoneNumData);
						phoneNumorder.put("command",
								APPConstant.USER_PROFILE_GET_REGISTER_CODE);
						handler = new Handler() {
							@Override
							public void handleMessage(Message msg) {
								// TODO Auto-generated method stub
								switch (msg.what) {
								case 6:
									try {
										Toast.makeText(
												getApplicationContext(),
												getContent.getMessage(GetTask
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
						GetTask = new HttpupLoad(APPConstant.getUSERURL(),
								phoneNumorder, handler, 6,
								getApplicationContext());
						GetTask.execute();
						// CYLog.i(TAG,"数值————————>" + Register);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Toast.makeText(getApplicationContext(), "请输入正确的手机号",
							Toast.LENGTH_SHORT).show();
				}
			}

		});
		changePassword = (Button) findViewById(R.id.changePassword);
		changePassword.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (passwordCheck(password.getText().toString())) {
					if (password.getText().toString()
							.equals(password2.getText().toString())) {
						JSONObject changepasswordData = new JSONObject();
						JSONObject changepasswordOrder = new JSONObject();
						try {
							changepasswordData.put("phoneNum", phoneNum
									.getText().toString());
							changepasswordData.put("checkCode", checkCode_edit
									.getText().toString());
							changepasswordData.put("password", password
									.getText().toString());
							changepasswordOrder.put("content",
									changepasswordData);
							changepasswordOrder.put("command", "16");
							CYLog.i(TAG,"发送的数据_____>"
									+ changepasswordOrder);

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						changepasswordHandler = new Handler() {

							@Override
							public void handleMessage(Message msg) {
								switch (msg.what) {
								case 12:
									JSONObject json;
									try {
										json = new JSONObject(GetTask.getLoaddata()
												.getStrResult());
										boolean idString = json.getBoolean("success");
										// 上传成功,修改本地数据
										if (idString) {
											ClassmateDao classmateDao = new ClassmateDao(mContext);
											String phone = phoneNum
													.getText().toString();
											String passwd = password
													.getText().toString();
											ContactsEntity selfContactsEntity = classmateDao.getSelfContactsEntity(phone);
											selfContactsEntity.setPassword(passwd);
											classmateDao.updateSelfContactsEntity(selfContactsEntity);
											
											Toast.makeText(getApplicationContext(),
													"修改成功", Toast.LENGTH_SHORT).show();
											finish();
										} else {
										String result = getContent
												.getMessage(GetTask.getLoaddata()
														.getStrResult());
										Toast.makeText(getApplicationContext(),
												result, Toast.LENGTH_SHORT).show();
										}
									} catch (JSONException e) {
										Toast.makeText(getApplicationContext(),
												"系统错误，修改失败", Toast.LENGTH_SHORT).show();
										e.printStackTrace();
									}
									
									break;
								}
							}

						};

						GetTask = new HttpupLoad(APPConstant.getUSERURL(),
								changepasswordOrder, changepasswordHandler, 12,
								getApplicationContext());
						GetTask.execute();
					} else {
						Toast.makeText(getApplicationContext(), "请确认两次输入的密码一致",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(getApplicationContext(), "请输入6位以上密码",
							Toast.LENGTH_SHORT).show();
				}
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

	public static boolean isMobileNO(String mobiles) {
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
