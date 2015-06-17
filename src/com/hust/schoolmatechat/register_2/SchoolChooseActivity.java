package com.hust.schoolmatechat.register_2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hust.schoolmatechat.LoginActivity;
import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.register.GetHandObj;
import com.hust.schoolmatechat.register.HttpupLoad;
import com.hust.schoolmatechat.register_2.RegisteActivity_2;
import com.hust.schoolmatechat.utils.StreamUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class SchoolChooseActivity extends Activity {
	protected static final String TAG = "SchoolChooseActivity";
	// private String[] SchoolList; // 学校名缓存数组
	private ListView list;
	List<String> sList = new ArrayList<String>();
	List<String> idList = new ArrayList<String>();
	ArrayList<String> Schoollist = new ArrayList<String>();
	ArrayList<String> IdList = new ArrayList<String>();
	View lastView;
	SimpleAdapter listItemAdapter;
	ArrayList<HashMap<String, Object>> listItem;
	private HttpupLoad GetTask;
	SharedPreferences prefs;
	private GetHandObj getContent;
	private ProgressBar mProgressBar;
	String schoolId;
	int choosePosition;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
		
			switch (msg.what) {
			case 20:
				try {
					CYLog.i(TAG, "查询结果" + GetTask.getLoaddata().getStrResult());
					if (getContent.getIfsuccess(GetTask.getLoaddata()
							.getStrResult())) {
						JSONObject sss = new JSONObject(GetTask.getLoaddata()
								.getStrResult());
						String schoolString = sss.getString("obj");
						JSONArray schoollist = new JSONArray(schoolString);

						prefs.edit()
								.putString(
										"baseId",
										schoollist.getJSONObject(0).getString(
												"baseId")).commit();
						prefs.edit()
								.putString(
										"chatServer",
										schoollist.getJSONObject(0).getString(
												"chatServer")).commit();
						prefs.edit()
								.putString(
										"chatServerDomain",
										schoollist.getJSONObject(0).getString(
												"chatServerDomain")).commit();
						prefs.edit()
								.putString(
										"chatServerPort",
										schoollist.getJSONObject(0).getString(
												"chatServerPort")).commit();
						prefs.edit()
								.putString(
										"city",
										schoollist.getJSONObject(0).getString(
												"city")).commit();
						prefs.edit()
								.putString(
										"codeSecretKey",
										schoollist.getJSONObject(0).getString(
												"codeSecretKey")).commit();
						prefs.edit()
								.putString(
										"fileServer",
										schoollist.getJSONObject(0).getString(
												"fileServer")).commit();
						prefs.edit()
								.putString(
										"fileServerPort",
										schoollist.getJSONObject(0).getString(
												"fileServerPort")).commit();
						prefs.edit()
								.putString(
										"province",
										schoollist.getJSONObject(0).getString(
												"province")).commit();
						prefs.edit()
								.putString(
										"pushServer",
										schoollist.getJSONObject(0).getString(
												"pushServer")).commit();
						prefs.edit()
								.putString(
										"pushServerAccount",
										schoollist.getJSONObject(0).getString(
												"pushServerAccount")).commit();
						prefs.edit()
								.putString(
										"pushServerPassword",
										schoollist.getJSONObject(0).getString(
												"pushServerPassword")).commit();
						prefs.edit()
								.putString(
										"pushServerPort",
										schoollist.getJSONObject(0).getString(
												"pushServerPort")).commit();
						prefs.edit()
								.putString(
										"schoolName",
										schoollist.getJSONObject(0).getString(
												"schoolName")).commit();
						prefs.edit()
								.putString(
										"telphone",
										schoollist.getJSONObject(0).getString(
												"telephone")).commit();
						prefs.edit()
								.putString(
										"webServer",
										schoollist.getJSONObject(0).getString(
												"webServer")).commit();
						prefs.edit()
								.putString(
										"webServerPort",
										schoollist.getJSONObject(0).getString(
												"webServerPort")).commit();
						prefs.edit()
								.putString(
										"welcomePicture",
										schoollist.getJSONObject(0).getString(
												"welcomePicture")).commit();
						Toast.makeText(SchoolChooseActivity.this, "数据加载完成",
								Toast.LENGTH_SHORT).show();
						mProgressBar.setVisibility(View.GONE);

						setData();
						Intent intent0=getIntent();
						Bundle bundle=intent0.getExtras();
						Intent intent  = new Intent();
						String from=bundle.getString("Activity");
						intent.putExtra("flag", true);
						intent.putExtra("schoolId", schoolId);
						intent.putExtra("schoolname", Schoollist.get(choosePosition));
						if(from.equals("Login")){
							intent.setClass(getApplicationContext(), LoginActivity.class);
						}
						else 
							intent.setClass(getApplicationContext(), RegisteActivity_2.class);
						startActivity(intent);

					} else {

						Toast.makeText(SchoolChooseActivity.this,
								"暂时未能获取到应用加载所需数据，请打开网络连接，稍后重试",
								Toast.LENGTH_SHORT).show();
						finish();
					}
				} catch (Exception e) {
					e.printStackTrace();
					CYLog.e(TAG, "" + e);
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
		setContentView(R.layout.school_choose);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		getContent = new GetHandObj();
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		list = (ListView) findViewById(R.id.school_list);
		mProgressBar = (ProgressBar) findViewById(R.id. school_progressBar);
		// SchoolList = new String[4];
		// SchoolList[0] = "学校一";
		// SchoolList[1] = "学校二";
		// SchoolList[2] = "学校三";
		// SchoolList[3] = "学校四";
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		CYLog.i("TAG", "" + bundle.size());

		// Schoollist = (ArrayList<String>)
		// bundle.getSerializable("schoolList");
		// IdList = (ArrayList<String>) bundle.getSerializable("idList");
		Schoollist = bundle.getStringArrayList("schoolList");

		IdList = bundle.getStringArrayList("idList");
		for (int s = 0; s < Schoollist.size(); s++) {
			CYLog.i("TAg", Schoollist.get(s) + "----->" + IdList.get(s));
		}

		listItem = new ArrayList<HashMap<String, Object>>();
		listItem.clear();

		if (Schoollist != null && Schoollist.size() != 0) {
			for (int i = 0; i < Schoollist.size(); i++) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("ItemTitle", Schoollist.get(i));
				map.put("ItemPicture", R.drawable.checked);
				listItem.add(map);
			}
		}

		listItemAdapter = new SimpleAdapter(this, listItem,// 数据源
				R.layout.school_choose_item,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "ItemTitle", "ItemPicture" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.schoolname, R.id.school_check });
		list.setAdapter(listItemAdapter);

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mProgressBar.setVisibility(View.VISIBLE);
				choosePosition = position;
				if (lastView != null) {
					lastView.findViewById(R.id.school_check).setVisibility(
							View.GONE);
				}
				lastView = view;
				view.findViewById(R.id.school_check)
						.setVisibility(View.VISIBLE);
				schoolId = IdList.get(position);
				Toast.makeText(getApplicationContext(), schoolId,
						Toast.LENGTH_SHORT).show();
				CYLog.i("TAG", "学校id-----》" + schoolId);

				JSONObject jsonObject = new JSONObject();
				JSONObject json_2 = new JSONObject();

				try {
					jsonObject.put("baseId", schoolId);
					JSONObject json_1 = jsonObject;

					json_2.put("content", json_1);
					json_2.put("command",
							APPConstant.USER_PROFILE_GET_SCHOOL_CONFIGS);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				CYLog.i(TAG, "---------->" + json_2);

				GetTask = new HttpupLoad(APPConstant.BASEINFOURL, json_2,
						handler, 20, getApplicationContext());
				GetTask.execute();

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.school_choose, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.school_choose_check_item:
			Toast.makeText(getApplicationContext(), "确定", Toast.LENGTH_SHORT).show();
			break;
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}
		return true;
	}

	public void setData() {
		CYLog.d(TAG, APPBaseInfo.URL);
		APPBaseInfo.FLAG = null;
		StreamUtils.checkBaseInfo(prefs);
		CYLog.d(TAG, APPBaseInfo.URL);

	}
}
