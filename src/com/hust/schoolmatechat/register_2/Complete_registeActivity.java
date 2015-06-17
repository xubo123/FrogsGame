package com.hust.schoolmatechat.register_2;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONObject;

import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.postClass.GetIdsFromName;
import com.hust.schoolmatechat.postClass.HttpCommand;
import com.hust.schoolmatechat.postClass.HttpupLoad_gson;
import com.hust.schoolmatechat.register.GetHandObj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Complete_registeActivity extends Activity{
 
	private static final String TAG = "Complete_registeActivity";
	Button attention,login_btn;
	Bundle bundle;
	String name,password,phoneNum;
	boolean isreturn;
	private GetHandObj getContent;
	HttpupLoad_gson httpupLoad_gson;
	private String fullID;
	private Map<String, String> map;
	private ArrayList<String> cutID;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 7:
				isreturn = getContent.getIfsuccess(httpupLoad_gson
						.getLoaddata().getStrResult());
				if (isreturn) {
					fullID = httpupLoad_gson.getLoaddata().getStrResult();
					try {
						map = getContent.getApartIDmap(fullID);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					CYLog.i(TAG, "输出map" + map);
					CYLog.i(TAG, "输出full" + fullID);
					try {
						cutID = getContent.getcutApartID(httpupLoad_gson
								.getLoaddata().getStrResult());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					CYLog.i(TAG, "输出full" + cutID);
					Intent inte = new Intent();
//					inte.putExtra("phoneNum", bundle
//							.get("phoneNum").toString());
//					inte.putExtra("checkCode",
//							bundle.get("checkCode").toString());
//					inte.putExtra("password", bundle
//							.get("password").toString());
					
					inte.putExtra("IDList", cutID);
					inte.putExtra("fullid", fullID);
					inte.putExtra("name", name);
					inte.putExtra("password", password);
					inte.putExtra("phoneNum", phoneNum);
//					inte.putExtra("name", name);

					// 使用web附带的机构数据
					try {
						JSONObject jsonRetStr = new JSONObject(
								httpupLoad_gson.getLoaddata()
										.getStrResult());
						String fullNameList = jsonRetStr
								.getString("msg");
						inte.putExtra("fullNameList", fullNameList);
					} catch (Exception e) {
						e.printStackTrace();
					}

					inte.setClass(getApplicationContext(),
							ClassChooseActivity.class);
					startActivity(inte);
					finish();
				} else {
					Toast.makeText(getApplicationContext(),
							"该姓名查询不到", Toast.LENGTH_SHORT).show();
				}
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.complete_registe2);
		getContent=new GetHandObj();
		attention=(Button) findViewById(R.id.attention);
		login_btn=(Button) findViewById(R.id.login_btn);
		attention.setOnClickListener(listener);
		bundle=getIntent().getExtras();
		if(bundle!=null){
			name=bundle.getString("name_2");
			password=bundle.getString("password_2");
			phoneNum=bundle.getString("phoneNum_2");
		}
		
	}
 private OnClickListener listener=new OnClickListener() {
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.attention:
//			CYLog.i(TAG,
//					"姓名------->" + name + bundle.get("phoneNum_2").toString()
//							+ bundle.get("registeCode_2").toString()
//							+ bundle.get("password_2").toString());
			
			//gson的使用

			GetIdsFromName getIdsFromName=new GetIdsFromName(name,APPBaseInfo.SCHOOL_ID_NUMBER);
			 HttpCommand httpCommand=new HttpCommand(APPConstant.USER_PROFILE_GET_USER_BASE_INFO_ID_LIST, 
					 getIdsFromName);
			 String uploadJson=httpCommand.getJsonStr();
			 httpupLoad_gson=new HttpupLoad_gson(APPConstant.getUSERURL(),
					 uploadJson,
					 handler,
					 7,
					 getApplicationContext());
				httpupLoad_gson.execute();
				Toast.makeText(getApplicationContext(), "继续完善班级信息",
						Toast.LENGTH_SHORT).show();
				//gson的使用
		
			
			break;
		case R.id.login_btn:
			break;
		}
	}
};
}
