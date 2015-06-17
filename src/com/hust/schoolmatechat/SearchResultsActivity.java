/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hust.schoolmatechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.dao.DepartmentDao;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;

import de.greenrobot.event.EventBus;

/**
 * Displays a word and its definition.
 */
public class SearchResultsActivity extends Activity {
	private static final String TAG = "SearchResultsActivity";
	// 搜索框显示相关数据结构
	private ListView mListView;
	private TextView mTextView;
	private SimpleAdapter mListViewAdapter;
	private List<Map<String, Object>> mSearchResults;

	// 从服务器取数据相关数据结构
	private static String mUrl = APPConstant.getURL()
			+ "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";
	public static final String USER_PROFILE_SEARCH_FOR_USERS = "9";
	private HttpupLoad GetTask;
	private Handler handler;
	private GetHandObj getContent;
	private String strJSON = null;
	private String nickname = null;
	private String accountNum = null;

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

			initSearchResultsActivity();
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
	}

	private void initSearchResultsActivity() {
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.search_results);
		mListView = (ListView) findViewById(R.id.search_results_list);
		mTextView = (TextView) findViewById(R.id.no_search_results);
		mSearchResults = new ArrayList<Map<String, Object>>();
		mListViewAdapter = new SimpleAdapter(this, mSearchResults,
				R.layout.search_results_item,
				new String[] { "item0", "item1", "item2"}, new int[] {
						R.id.search_results_column0,
						R.id.search_results_column1,
						R.id.search_results_column2});
		mListView.setAdapter(mListViewAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				LinearLayout row = (LinearLayout) ((SimpleAdapter) parent
						.getAdapter()).getView(position, null, null);
				nickname = (String) ((TextView) row
						.findViewById(R.id.search_results_column0)).getText();
				accountNum = (String) ((TextView) row
						.findViewById(R.id.search_results_column1)).getText();

				// Toast.makeText(getApplicationContext(),
				// nickname+"-"+accountNum,Toast.LENGTH_SHORT).show();
				AlertDialog.Builder builder = new AlertDialog.Builder(
						SearchResultsActivity.this);
				builder.setMessage("是否添加" + nickname + "为好友？")
						.setCancelable(false)
						.setPositiveButton("添加",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										//++++lqg++++ 待修改 让用户可以选择添加的分组，分组管理
										if (dataCenterManagerService.isMyFriend(accountNum)) {
											Toast.makeText(getApplicationContext(),
													nickname+"已经是您的好友，无需添加", Toast.LENGTH_SHORT)
													.show();
											return;
										}
										
										//发送添加请求
										dataCenterManagerService.sendFriendAddRequest(accountNum);
										Toast.makeText(getApplicationContext(),
													"已经发送了您的请求", Toast.LENGTH_SHORT)
													.show();									
									}
								})
						.setNegativeButton("取消",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();

				// roster.createGroup(arg0)
			}
		});
		getContent = new GetHandObj();
		handleIntent(getIntent());
	}

	/**
	 * 根据名字搜索用户
	 */
	private void getSearchResults(String query) {
		// 下面添加从服务器取数据的代码
		// 初始化JSONObject
		JSONObject account_content = new JSONObject();
		JSONObject account = new JSONObject();
		try {
			account_content.put("accountNum", dataCenterManagerService.getUserSelfContactsEntity().getUserAccount());
			account_content.put("password", dataCenterManagerService.getUserSelfContactsEntity().getPassword());
			account_content.put("name", query);
			account.put("command", USER_PROFILE_SEARCH_FOR_USERS);
			account.put("content", account_content);
		} catch (JSONException e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
		}
		connectToServer(mUrl, account);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		CYLog.i(TAG, "new Intent arrived!");
		// 点击了一条建议
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {

		}
		// 点击了搜索框
		else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			CYLog.i(TAG, "query=" + query);
			Toast.makeText(getApplicationContext(), "正在联网查询，请稍后...",
					Toast.LENGTH_SHORT).show();
			String auth = dataCenterManagerService.getUserSelfContactsEntity()
					.getAuthenticated();
			if (auth != null && auth.equals("1")) {
				// 在下面添加搜索代码
				getSearchResults(query);
			} else {
				Toast.makeText(getApplicationContext(), "您还未认证，无法使用搜索功能",
						Toast.LENGTH_SHORT).show();
			}
			
		}
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.main, menu);
//
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//			SearchView searchView = (SearchView) menu.findItem(
//					R.id.action_search).getActionView();
//			searchView.setSearchableInfo(searchManager
//					.getSearchableInfo(getComponentName()));
//			searchView.setIconifiedByDefault(false);
//		}
//
//		return true;
//	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
//		case R.id.action_search:
//			return true;
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return false;
		}
	}

	/**
	 * 连接服务器,并查询
	 */
	public void connectToServer(String url, JSONObject json) {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case 0:
					String result = GetTask.getLoaddata().getStrResult();
					boolean checkResult = false;
					if(getContent!=null&&result!=null)
					checkResult = getContent.getIfsuccess(result);
					else{
						checkResult = false;
					}
					CYLog.i(TAG, result);
					if (checkResult) {
						CYLog.i(TAG, "can get data from server!");
						strJSON = GetTask.getLoaddata().getStrResult();
						mSearchResults.clear();
						List<Map<String, Object>> classInfoList = transStrObjToAppData(strJSON);
						if(classInfoList != null){
							mSearchResults.addAll(classInfoList);
							mListViewAdapter.notifyDataSetChanged();
							mTextView.setVisibility(View.GONE);
							mListView.setVisibility(View.VISIBLE);
						}else{
							mListView.setVisibility(View.GONE);
							mTextView.setVisibility(View.VISIBLE);					
						}
						
						//mListView.setSelection(mListView.getBottom());

					} else {
						CYLog.i(TAG, "can't get data from server!");
						mSearchResults.clear();
						mListView.setVisibility(View.GONE);
						mListViewAdapter.notifyDataSetChanged();
						mTextView.setVisibility(View.VISIBLE);
						//mListView.setSelection(mListView.getBottom());
					}
					break;
				default:
					break;
				}
			}

		};
		GetTask = new HttpupLoad(url, json, handler);
		GetTask.execute();
	}

	/**
	 * 将从服务器得到String转换为所需的形式
	 */
	public List<Map<String, Object>> transStrObjToAppData(String objString) {
		try {
			CYLog.i(TAG, "transStrObjToAppData is called!");
			JSONObject jsonObject = new JSONObject(objString);
			String jsonArrayStr = jsonObject.getString("obj");
			JSONArray jsonArray = new JSONArray(jsonArrayStr);
			List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonPerson = jsonArray.getJSONObject(i);
				String name = jsonPerson.getString("name");
				String accountNum = jsonPerson.getString("accountNum");
				String baseinfoId = jsonPerson.getString("baseInfoId");
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("item0", name);
				map.put("item1", accountNum);
				//添加所在班级的列
				String classBaseId = baseinfoId.substring(0, 16);
				DepartmentDao departmentDao = new DepartmentDao(this);
				String fullName = fullName = jsonPerson.getString("departName");
				String departName = "";
				if (fullName == null || fullName.equals("")) {
					fullName = departmentDao.getDepartmentFullName(classBaseId);;
					CYLog.i(TAG, fullName);
				}else {
					//更新本地
					departmentDao.addDepartment(classBaseId, fullName.split("_")[0]);
				} 
				if (fullName == null || fullName.equals("")) {//网络和本地都没有
					departName = "未查询到您所在的班级";
				} else {
					String[] DapartmentInfo = fullName.split("_");
					CYLog.i(TAG, ""+DapartmentInfo.length);
					for(int j=0;j<DapartmentInfo.length;j++){
						String[] spitDapartmentInfo = DapartmentInfo[j].split(",");
						departName += spitDapartmentInfo[spitDapartmentInfo.length - 1];
						if(j!=DapartmentInfo.length-1){
							departName+="\n";
						}
					}
				}
				CYLog.i(TAG, departName);
				map.put("item2", departName);
				results.add(map);
			}
			return results;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.i(TAG, e.toString());
			return null;
		}
	}
}
