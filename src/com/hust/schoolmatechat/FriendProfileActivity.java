package com.hust.schoolmatechat;    //把包名改下

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.dao.DepartmentDao;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.postClass.FriendProfile;
import com.hust.schoolmatechat.postClass.HttpCommand;
import com.hust.schoolmatechat.postClass.HttpupLoad_gson;
import com.hust.schoolmatechat.register.GetHandObj;
import com.hust.schoolmatechat.register.HttpupLoad;
import com.hust.schoolmatechat.utils.ImageUtils;

public class FriendProfileActivity extends Activity {
	private TextView title, accountNum_tv, phoneNum_tv, name_tv, grade_tv,
			sex_tv, address_tv, email_tv, sign_tv;
	private ImageView person_photo_iv;
	private RelativeLayout phoneNum_rl,grade_rl;
	private Button post;
	String friendAccount;
	ContactsEntity  friend;
	
	// gson的使用
	HttpupLoad_gson httpupLoad_gson;
	private GetHandObj getContent;
	private static final String TAG = "FriendProfileActivity";
	String FriendIdList;
	String FriendID=null;
	private DepartmentDao departmentDao;
	
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 21:
				getContent=new GetHandObj();
				
				CYLog.i(TAG, "修改结果" + httpupLoad_gson.getLoaddata().getStrResult());
				if (getContent.getIfsuccess(httpupLoad_gson.getLoaddata()
						.getStrResult())){
					try {
						JSONObject sss = new JSONObject(httpupLoad_gson
								.getLoaddata().getStrResult());
						 FriendIdList=sss.getString("obj");
						 String Department=sss.getString("msg");
						 String[] Department_list=Department.split("_");
//			             for(int j=0;j<Department_list.length;j++){
//			            	
//			            	 departmentDao.addDepartment(baseInfoId, departmentFullName);
//			             }
						 JSONArray idList = new JSONArray(FriendIdList);
						 FriendID=idList.getString(0);
						 String[] cutFriendI;
						 
						 for(int p=1;p<idList.length();p++){
							
							 FriendID=FriendID+","+idList.getString(p);
							 
						 }
						 cutFriendI=FriendID.split(",");
						 
						 for(int leng=0;leng<cutFriendI.length;leng++){
							 cutFriendI[leng]=cutFriendI[leng].substring(0, 16);
						 }
						 for(int m=0;m<cutFriendI.length;m++){
							 String baseInfoId=cutFriendI[m];
							 String departmentFullName = departmentDao.getDepartmentFullName(baseInfoId);
							 if(departmentFullName!=null){
								 continue;
							 }
							 else{
								 departmentDao.addDepartment(cutFriendI[m], Department_list[m]);
							 }
						 }
						 
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			
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

			initAccountActivity();
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
		 * 启动数据中心管理服务
		 */
		final Intent dataCenterManagerIntent = new Intent(this,
				DataCenterManagerService.class);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
				Context.BIND_ABOVE_CLIENT);
	}
	
	
	private void initAccountActivity() {
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setDisplayShowHomeEnabled(false);
		bar.setTitle("好友信息");
		setContentView(R.layout.account_layout);
		init();
		
		phoneNum_rl.setVisibility(View.GONE);
		post.setVisibility(View.GONE);
		
		Intent parent=getIntent();
		friendAccount=parent.getStringExtra("friendAccount");
	    friend=dataCenterManagerService.getFriendInfoByAccount(friendAccount);
	    if(friend==null){
	    	Toast.makeText(this,"暂时无法查看好友信息",Toast.LENGTH_SHORT).show();
	    	finish();
	    	return;
	    }
		accountNum_tv.setText(friend.getAccountNum());
		ImageUtils.setUserHeadIcon(person_photo_iv,friend.getPicture(), new Handler());
		name_tv.setText(friend.getName());
		if (friend.getSex().equals("0")) {
			sex_tv.setText("男");
		}
		if (friend.getSex().equals("1")) {
			sex_tv.setText("女");
		}
		address_tv.setText(friend.getAddress());
		email_tv.setText(friend.getEmail());
		sign_tv.setText(friend.getSign());
		
		// 获取完整好友基础id

		departmentDao = new DepartmentDao(getApplicationContext());
		
		FriendProfile friendProfile=new FriendProfile(friend.getAccountNum(),APPBaseInfo.SCHOOL_ID_NUMBER);
		HttpCommand httpCommand=new HttpCommand(APPConstant.USER_PROFILE_GET_USER_BASE_INFO_ID_LIST, friendProfile);
		String mapJson_1=httpCommand.getJsonStr();
		CYLog.i("好友id获取json",mapJson_1);
		httpupLoad_gson=new HttpupLoad_gson(APPConstant.getUSERURL(), mapJson_1, handler, 21,getApplicationContext());
		httpupLoad_gson.execute();
		
	
		
		
		grade_tv.setText("查看学习经历");
		grade_rl.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("isfriend", true);
				if(FriendID==null||FriendID.equals("")){
				intent.putExtra("friendBaseInfo",friend.getBaseInfoId());
				}
				else{
					intent.putExtra("friendBaseInfo",FriendID);
				}
				
				intent.setClass(getApplicationContext(), StudyExActivity.class);
				startActivity(intent);
				
			}
		});
	}

	private void init() {
		accountNum_tv = (TextView) findViewById(R.id.accountNum_tv);
		phoneNum_tv = (TextView) findViewById(R.id.phoneNum_tv);
		name_tv = (TextView) findViewById(R.id.person_name_tv);
		name_tv.setVisibility(View.VISIBLE);
		grade_tv = (TextView) findViewById(R.id.grade_tv);
		sex_tv = (TextView) findViewById(R.id.person_sex_tv);
		address_tv = (TextView) findViewById(R.id.address_tv);
		email_tv = (TextView) findViewById(R.id.email_tv);
		sign_tv = (TextView) findViewById(R.id.person_sign_tv);
		person_photo_iv = (ImageView) findViewById(R.id.person_photo_iv);
		post = (Button) findViewById(R.id.post);
		grade_rl=(RelativeLayout) findViewById(R.id.grade);
		phoneNum_rl=(RelativeLayout) findViewById(R.id.phoneNum);
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
