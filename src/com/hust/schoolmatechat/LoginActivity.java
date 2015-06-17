package com.hust.schoolmatechat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.schoolmatechat.ChatMsgservice.ChatMsgService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.FaceInput.FaceConversionUtil;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.AppEngine;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.login.CheckNetworkState;
import com.hust.schoolmatechat.register.ChangePasswordActivity;
import com.hust.schoolmatechat.register_2.ProvinceChooseActivity;
import com.hust.schoolmatechat.register_2.RegisteActivity_2;
import com.hust.schoolmatechat.register.RegisterActivity;

public class LoginActivity extends Activity {
	private final static String TAG = "LofinActivity";
	private EditText etAccount = null;
	private EditText etPassword = null;
	private TextView loginQuestion = null;
	private TextView school = null;
	private ProgressBar progressBar = null;
	private Button btLogin = null;
	private Button btRegister = null;
	private Button schoolElect = null;
	SharedPreferences prefs;
	private Bundle bundle2;
	private Handler handler2;

	private boolean flag = false;
	Intent intent0;

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

			AutoLogin();
		}
	};

	@Override
	public void onDestroy() {
		// add 取消绑定 ibind
		unbindService(dataCenterManagerIntentConn);
		super.onDestroy();
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Bundle bundle = msg.getData();
				String resultMsg = bundle.getString("resultMsg");
				if (!flag) {
					Toast.makeText(LoginActivity.this, resultMsg,
							Toast.LENGTH_SHORT).show();
					flag = false;
					if (progressBar != null)
						progressBar.setVisibility(View.GONE);
				}

				break;

			case 1:// 账号密码错误
				Toast.makeText(LoginActivity.this, "登陆失败,账号或者密码错误",
						Toast.LENGTH_SHORT).show();
				if (progressBar != null)
					progressBar.setVisibility(View.GONE);
				prefs.edit().putString("AUTO", "no").commit();
				prefs.edit().putString("USERNAME", "").commit();
				prefs.edit().putString("PASS", "").commit();
				initLoginActivity();
				break;
			case 2:// 没有网络
				Toast.makeText(LoginActivity.this, "网络未连接", Toast.LENGTH_SHORT)
						.show();
				if (progressBar != null)
					progressBar.setVisibility(View.GONE);
				prefs.edit().putString("AUTO", "no").commit();
				prefs.edit().putString("USERNAME", "").commit();
				prefs.edit().putString("PASS", "").commit();
				initLoginActivity();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}

	};

	// +++lqg+++全局初始化数据，放到static代码段，加快访问速度
	static {
		SchoolMateChat schoolMateChat = SchoolMateChat.getInstance();
		// 每次用户登录都检查一次数据表是否都创建好
		if (!schoolMateChat.checkExistLocalDBTables()) {
			Toast.makeText(schoolMateChat, "您的软件配置被毁坏，软件已不可用，请重新安装软件!",
					Toast.LENGTH_SHORT).show();
		}

		// // 载入测试数据
		// if (APPConstant.DEBUG_MODE) {
		// new Thread() {
		// @Override
		// public void run() {
		// try {
		// SchoolMateChat schoolMateChat = SchoolMateChat
		// .getInstance();
		// schoolMateChat.testMemoryProperties("245");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// }.start();
		// }

		// 表情输入初始化
		FaceConversionUtil.getInstace().getFileText(schoolMateChat);

		schoolMateChat = null;// 释放内存
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*
		 * 连接数据中心管理服务
		 */
		final Intent dataCenterManagerIntent = new Intent(this,
				DataCenterManagerService.class);
	    intent0=getIntent();
		this.startService(dataCenterManagerIntent);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
				Context.BIND_ABOVE_CLIENT);

		super.onCreate(savedInstanceState);
	}

	private void AutoLogin() {

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Intent parentIntent = getIntent();
		String username = parentIntent.getStringExtra("USERNAME");
		String pass = parentIntent.getStringExtra("PASS");
		String auto = prefs.getString("AUTO", "no");
		// 用户没有点击过退出按钮
		if ((auto.equals("auto") || auto.equals("reset")) && username != null
				&& !username.equals("")
				&& !dataCenterManagerService.isUserClickedQuitButton()) {
			flag = true;
			CYLog.d(TAG, "LoginActivity auto login");
			loginLoaded(username, pass);
			/*
			 * Resources res = getResources(); Drawable drawable =
			 * res.getDrawable(R.drawable.loading);
			 * this.getWindow().setBackgroundDrawable(drawable);
			 */
		} else {
			initLoginActivity();
		}

	}

	private void initLoginActivity() {
		CYLog.d(TAG, "LoginActivity initLoginActivity");
		setContentView(R.layout.login_main);

		etAccount = (EditText) this.findViewById(R.id.telphone_login);
		// 记住上次登录的账号
		schoolElect = (Button) this.findViewById(R.id.choose_school);
		schoolElect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("Activity","Login");
				intent.setClass(getApplicationContext(),
						ProvinceChooseActivity.class);
				startActivity(intent);
			}
		});
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String lastAccount = prefs.getString("PHONENUM", "");
		if (lastAccount != null) {
			etAccount.setText(lastAccount);
		}
		school=(TextView) findViewById(R.id.school_text);
		try {
	        if(intent0.getExtras().getString("schoolname")!=null){
	        	String SchoolName=intent0.getExtras().getString("schoolname");
	        	school.setText(SchoolName);
	        }
		} catch (NullPointerException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		etPassword = (EditText) this.findViewById(R.id.password_login);
		progressBar = (ProgressBar) this.findViewById(R.id.progressBar1);
		btLogin = (Button) this.findViewById(R.id.school_login);
		// TextView changepassword = (TextView)
		// findViewById(R.id.changepassword_login);
		// changepassword.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Intent change = new Intent();
		// change.setClass(getApplicationContext(),
		// ChangePasswordActivity.class);
		// startActivity(change);
		//
		// }
		// });

		btRegister = (Button) this.findViewById(R.id.register);
		btRegister.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 点击登录时禁止服务连接清除
				dataCenterManagerService.setUserClickedQuitButton(false);
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, RegisteActivity_2.class);
				startActivity(intent);

			}
		});
		String a=APPConstant.getUSERURL();
		Toast.makeText(getApplicationContext(), a, Toast.LENGTH_SHORT).show();
		// private void initLoginActivity() {
		// CYLog.d(TAG, "LoginActivity initLoginActivity");
		// setContentView(R.layout.login_main);
		//
		// etAccount = (EditText)
		// this.findViewById(R.id.editText_login_account);
		// // 记住上次登录的账号
		// prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// String lastAccount = prefs.getString("PHONENUM", "");
		// if (lastAccount != null) {
		// etAccount.setText(lastAccount);
		// }
		//
		// etPassword = (EditText) this.findViewById(R.id.password_login);
		// progressBar = (ProgressBar) this.findViewById(R.id.progressBar1);
		// btLogin = (Button) this.findViewById(R.id.school_login);
		// // TextView changepassword = (TextView)
		// findViewById(R.id.changepassword_login);
		// changepassword.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Intent change = new Intent();
		// change.setClass(getApplicationContext(),
		// ChangePasswordActivity.class);
		// startActivity(change);
		//
		// }
		// });
		//
		// btRegister = (Button) this.findViewById(R.id.button_login_register);
		// btRegister.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // 点击登录时禁止服务连接清除
		// dataCenterManagerService.setUserClickedQuitButton(false);
		//
		// Intent intent = new Intent();
		// intent.setClass(LoginActivity.this, RegisterActivity.class);
		// startActivity(intent);
		//
		// }
		// });

		// loginQuestion = (TextView) this.findViewById(R.id.textView3);
		// // loginQuestion.setVisibility(View.GONE);
		// loginQuestion.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Dialog dialog = new AlertDialog.Builder(LoginActivity.this)
		// .setTitle("登陆遇到问题？")
		// .setMessage(
		// "        非常抱歉!\n        若您确认账号密码无误仍无法登陆，或忘记密码，请联系"+APPBaseInfo.SCHOOLNAME+"校友总会："+APPBaseInfo.SCHOOLPHONE)
		// .setPositiveButton("拨号",
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int which) {
		// // TODO Auto-generated method stub
		// Intent intent = new Intent(
		// Intent.ACTION_DIAL,
		// Uri.parse("tel:"+APPBaseInfo.SCHOOLPHONE));
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// startActivity(intent);
		// }
		// })
		// .setNegativeButton("返回",
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int which) {
		// // TODO Auto-generated method stub
		// }
		// }).create();
		// dialog.show();
		// }
		// });
	}

	public void OnLoginClicked(View view) {
		// 修改注册流程以后，用户输入的为手机号，需要认证成功以后切换为账号
		String account = etAccount.getText().toString();
		String password = etPassword.getText().toString();
		progressBar.setVisibility(View.VISIBLE);

		loginLoaded(account, password);
	}

	/*
	 * 尝试登陆服务器
	 */
	private void loginLoaded(final String account, final String password) {
		// 本地存有数据，直接登录
		if (dataCenterManagerService.isSelfContactsEntityExisted(account)) {
			ContactsEntity contactsEntity = dataCenterManagerService
					.getSelfContactsEntity(account);
			// 认证过
			String auth = contactsEntity.getAuthenticated();
			if (auth != null && auth.equals("1")) {
				// 账号或者手机号都可以登录
				if (contactsEntity != null
						&& account != null
						&& (account.equals(contactsEntity.getUserAccount()) || account
								.equals(contactsEntity.getPhoneNum()))
						&& password != null
						&& password.equals(contactsEntity.getPassword())) {
					// 认证成功的提示
					Message msg = Message.obtain();
					msg.what = 0;
					Bundle bundle = new Bundle();
					msg.setData(bundle);
					bundle.putString("resultMsg", "登陆成功");
					handler.sendMessage(msg);

					// 切换出账号
					String phoneNum = contactsEntity.getPhoneNum();
					String userAccount = contactsEntity.getUserAccount();
					startWork(userAccount, password, phoneNum);
					return;
				}
			}
		}

		// 主线程不允许直接进行网络操作，需要新启一个线程
		new Thread() {

			@Override
			public void run() {
				try {
					// 登陆认证
					int ret = dataCenterManagerService.loginAuthenticate(
							account, password);
					if (ret == 1) {// 首次登陆认证
						// 认证成功的提示
						Message msg = Message.obtain();
						msg.what = 0;
						Bundle bundle = new Bundle();
						msg.setData(bundle);
						bundle.putString("resultMsg", "登陆成功");
						handler.sendMessage(msg);

						// 切换出账号
						String userAccount = dataCenterManagerService
								.getTigaseAccount();
						startWork(userAccount, password, account);
					} else if (ret == 2) {// 已经认证过，自动登陆，恢复主界面
						// 启动MainActivity
						startIntents();
					} else {
						// 登陆失败
						// 判断是否网络原因
						if (!CheckNetworkState.getInstance(
								getApplicationContext()).getNetworkState()) {// 如果没有网络
							handler.sendEmptyMessage(2);
						} else {
							handler.sendEmptyMessage(1);// 账号密码错误
						}
					}
				} catch (Exception e) {
					handler.sendEmptyMessage(0);
					e.printStackTrace();
				}
				super.run();
			}
		}.start();
	}

	/**
	 * 初始化界面，登陆聊天服务器，启动MainActivity
	 */
	private void startWork(final String userAccount, final String password,
			String phoneNum) {
		try {
			CYLog.i(TAG, "useraccount : " + userAccount + " password : "
					+ password);
			// 为实现自动登陆准备
			prefs.edit().putString("AUTO", "auto").commit();
			prefs.edit().putString("USERNAME", userAccount).commit();
			prefs.edit().putString("PASS", password).commit();
			prefs.edit().putString("PHONENUM", phoneNum).commit();

			// 数据初始化
			dataCenterManagerService
					.initialiseDataCenter(userAccount, password);

			// 登陆聊天服务器
			new Thread() {
				@Override
				public void run() {
					// 加入上次服务结束的判断，重新绑定
					Intent chatMsgServiceIntent = new Intent(
							LoginActivity.this, ChatMsgService.class);
					if (AppEngine.getInstance(getBaseContext())
							.isChatMsgServiceWorked()) {
						CYLog.i(TAG,
								"oncreate AppEngine.getInstance(getBaseContext()).isChatMsgServiceWorked()");
						// 上次的聊天服务没有完全退出，先退出
						stopService(chatMsgServiceIntent);
					}

					startService(chatMsgServiceIntent);
				}
			}.start();

			// 启动MainActivity
			startIntents();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startIntents() {
		Intent mainActivity = new Intent(this, MainActivity.class);
		startActivity(mainActivity);
		finish();
	}
}
