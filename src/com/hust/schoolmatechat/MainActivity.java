package com.hust.schoolmatechat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.FaceInput.FaceConversionUtil;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.AppEngine;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.UpdateApp;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.pushedmsgservice.PushedMsgService;

/**
 * ���Ѱ�׿app���
 * 
 * 
 * @author luoguangzhen
 */
public class MainActivity extends FragmentActivity {
	private static final String TAG = "MainActivity";

	/**
	 * ��Ϣ�����Fragment
	 */
	private ChatFragment mChatFragment;

	/**
	 * ��̬�����Fragment
	 */
	private NewsFragment mNewsFragment;

	/**
	 * ͨѶ¼�����Fragment
	 */
	private ContactsFragment mContactsFragment;
	/**
	 * �ҵĽ����Fragment
	 */
	private MyselfFragment mMyselfFragment;
	/**
	 * PagerSlidingTabStrip��ʵ��
	 */
	private PagerSlidingTabStrip mTabs;

	/**
	 * ��ȡ��ǰ��Ļ���ܶ�
	 */
	private DisplayMetrics dm;

	private MainActivity mContext;

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
			CYLog.e(TAG, "dataCenterManagerService onServiceConnected ");
			// ��ȡ����������ʵ���Ժ����ʹ�ã��û�û�е���˳���ť
			if (dataCenterManagerService != null
					&& !dataCenterManagerService.isUserClickedQuitButton()) {
				initMainActivity();
			}
		}
	};

	private Handler mainActivityHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case APPConstant.GROUPCHAT_INV:// Ⱥ��������
				CYLog.i(TAG, "receive group invite");
				try {
					String[] inviter_room = (String[]) msg.obj;
					// �˺�+����+Ⱥ��id+Ⱥ����
					final String inviterAccount = inviter_room[0];
					final String name = inviter_room[1];
					final String groupId = inviter_room[2];
					final String groupName = inviter_room[3];

					CYLog.i(TAG, "inviterAccount=" + inviterAccount + "   "
							+ "name=" + name + "   " + "groupId=" + groupId
							+ "   groupName=" + groupName);

					Dialog dialog = new AlertDialog.Builder(MainActivity.this)
							.setTitle("Ⱥ��������")
							.setMessage(
									name + "���������������" + groupName + ",���Ƿ����?")
							.setPositiveButton("����",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											// ����������
											dataCenterManagerService
													.joinGroupChatRoom(groupId);
										}
									})
							.setNegativeButton("�ܾ�",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											try {
												// dataCenterManagerService
												// .declineJoinChatRoom(room,
												// inviter, null);

											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}).create();
					// dialog.getWindow().setType(
					// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					dialog.show();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				break;
			case APPConstant.GROUPCHAT_EXIT:// ��Ⱥ֪ͨ��Ϣ
				String[] id_room = (String[]) msg.obj;
				final String roomId = id_room[0];
				final String roomName = id_room[1];
				Dialog dialog = new AlertDialog.Builder(getApplicationContext())
						.setTitle("��Ⱥ֪ͨ")
						.setMessage("���Ѿ����Ƴ�������" + roomName)
						.setPositiveButton("����",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										dataCenterManagerService
												.unsubscribeGroupChatRoom(roomId);
									}
								}).create();
				dialog.getWindow().setType(
						WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				dialog.show();
				break;

			case APPConstant.FRIEND_ADD:
				Bundle bundle = msg.getData();
				String cmdtype = bundle.getString("cmdtype");
				final String friendAccount = bundle.getString("friendAccount");
				String name = bundle.getString("name");
				String className = bundle.getString("className");

				// �ܾ���Ϣ
				if (APPConstant.CMD_PREFIX_FRIEND_ADD_DECLINE.equals(cmdtype)) {
					Dialog dialog1 = new AlertDialog.Builder(MainActivity.this)
							.setTitle("���󱻾ܾ�")
							.setMessage(name + "�ܾ������Ϊ����!")
							.setPositiveButton("ȷ��",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											try {
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}).create();
					// dialog.getWindow().setType(
					// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					dialog1.show();
					break;
				}
				// �Ѿ��Ǻ���,������Ϣ
				if (dataCenterManagerService.isMyFriend(friendAccount)) {
					break;
				}
				// ��������Ϣ
				if (!APPConstant.CMD_PREFIX_FRIEND_ADD_REQUEST.equals(cmdtype)) {
					break;
				}

				Dialog dialog1 = new AlertDialog.Builder(MainActivity.this)
						.setTitle("�������")
						.setMessage(name + "�������Ϊ����" + ",���Ƿ����?")
						.setPositiveButton("����",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										try {
											dataCenterManagerService
													.sendFriendAddAgree(friendAccount);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								})
						.setNegativeButton("�ܾ�",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										try {
											dataCenterManagerService
													.sendFriendAddDecline(friendAccount, 0);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}).create();
				// dialog.getWindow().setType(
				// WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				dialog1.show();
				break;
			case APPConstant.NETWORK_UN: // �������������״��֪ͨ ,�������δ֪
				break;
			case APPConstant.NETWORK_OK: // ������
				// Toast.makeText(MainActivity.this, "���������������",
				// Toast.LENGTH_SHORT).show();
				if (dataCenterManagerService != null)
					dataCenterManagerService
							.autoCreateOrJoinDefaultGroupChatRoom();

				break;
			case APPConstant.NETWORK_NO: // �����ѶϿ�
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	/** ���й���MainActivity��BroadcastReceiver,���ڼ������� */
	private List<BroadcastReceiver> broadcastReceiverList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		CYLog.i(TAG, "MainActivity onCreate");
		
		/*
		 * �����������Ĺ������
		 */
		final Intent dataCenterManagerIntent = new Intent(this,
				DataCenterManagerService.class);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
				Context.BIND_ABOVE_CLIENT);

		// setContentView(R.layout.activity_main);

		super.onCreate(savedInstanceState);
	}

	/**
	 * ���������������ĵ�Ⱥ���������Ĺ㲥
	 * 
	 * @author Administrator
	 * 
	 */
	class GroupInviteBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Bundle bundle = intent.getExtras();
				android.os.Message msg = new android.os.Message();
				if (bundle != null && bundle.containsKey("groupFilterMsg")) {
					String[] inviter_room = bundle.getString("groupFilterMsg")
							.split("_");
					msg.obj = inviter_room;
					msg.what = APPConstant.GROUPCHAT_INV;
					mainActivityHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * �����������ĵ��Ƴ���������Ϣ
	 */
	class ExitGroupChatRoomBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Bundle bundle = intent.getExtras();
				android.os.Message msg = new android.os.Message();
				if (bundle != null && bundle.containsKey("selfMoveOutOfGroup")) {
					String[] id_room = bundle.getString("selfMoveOutOfGroup")
							.split("_");
					msg.obj = id_room;
					msg.what = APPConstant.GROUPCHAT_EXIT;
					mainActivityHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * �����������ĵ�tigase����״��֪ͨ��Ϣ
	 */
	class TigaseConnStatusNotifyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Bundle bundle = intent.getExtras();
				android.os.Message msg = new android.os.Message();
				if (bundle != null
						&& bundle.containsKey("tigaseConnectionStatus")) {
					msg.what = bundle.getInt("tigaseConnectionStatus");
					mainActivityHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * ���������������ĵĺ�����ӵĹ㲥
	 * 
	 * @author Administrator
	 * 
	 */
	class FriendAddBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Bundle bundle = intent.getExtras();
				android.os.Message msg = new android.os.Message();
				if (bundle != null && bundle.containsKey("cmdtype")
						&& bundle.containsKey("friendAccount")
						&& bundle.containsKey("name")
						&& bundle.containsKey("className")) {
					msg.setData(bundle);
					msg.what = APPConstant.FRIEND_ADD;
					mainActivityHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void initMainActivity() {
		setContentView(R.layout.activity_main);

		mContext = this;
		broadcastReceiverList = new ArrayList<BroadcastReceiver>();

		// ��Ӻ��������dialog��ʾ ++++lqg++++ �����޸Ľ�������ڴ˴�
		IntentFilter intentFilter = new IntentFilter(
				"com.schoolmatechat.friendAddMessage");
		FriendAddBroadcastReceiver friendAddBroadcastReceiver = new FriendAddBroadcastReceiver();
		registerReceiver(friendAddBroadcastReceiver, intentFilter);
		this.addBroadcastReceiverToList(friendAddBroadcastReceiver);

		// �����������dialog��ʾ
		// groupchatInvitationHandler = new
		// GroupChatInvitationHandlerEx(dataCenterManagerService);
		// dataCenterManagerService.setGroupChatInvitationHandler(groupchatInvitationHandler);
		// �����������dialog��ʾ
		IntentFilter intentFilter2 = new IntentFilter(
				"com.schoolmatechat.groupInviteBroadcastReceiver");
		GroupInviteBroadcastReceiver groupInviteBroadcastReceiver = new GroupInviteBroadcastReceiver();
		registerReceiver(groupInviteBroadcastReceiver, intentFilter2);
		this.addBroadcastReceiverToList(groupInviteBroadcastReceiver);

		// �Ƴ���Ⱥ�㲥��Ϣ����
		IntentFilter intentFilter3 = new IntentFilter(
				"com.schoolmatechat.moveOutOfRoomBroadcastReceiver");
		ExitGroupChatRoomBroadcastReceiver exitGroupChatRoomBroadcastReceiver = new ExitGroupChatRoomBroadcastReceiver();
		registerReceiver(exitGroupChatRoomBroadcastReceiver, intentFilter3);
		this.addBroadcastReceiverToList(exitGroupChatRoomBroadcastReceiver);

		// �������������״���㲥��Ϣ����
		IntentFilter intentFilter4 = new IntentFilter(
				"com.schoolmatechat.tigaseConnectionStatusBroadcastReceiver");
		TigaseConnStatusNotifyBroadcastReceiver tigaseConnStatusNotifyBroadcastReceiver = new TigaseConnStatusNotifyBroadcastReceiver();
		registerReceiver(tigaseConnStatusNotifyBroadcastReceiver, intentFilter4);
		this.addBroadcastReceiverToList(tigaseConnStatusNotifyBroadcastReceiver);

		setOverflowShowingAlways();
		dm = getResources().getDisplayMetrics();
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
		mTabs.setViewPager(pager);
		setTabsValue();

		if (!AppEngine.getInstance(getBaseContext()).isPushServiceWorked()) {
			startService(new Intent(this, PushedMsgService.class));
		}

		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		int DATA = prefs.getInt("DATA", 0);

		String uid = prefs.getString("USERNAME", "");

		if (uid != null && !uid.equals("")) {
			AppEngine.getInstance(getBaseContext()).setUID(uid);
			if (DATA == 1) {

				prefs.edit().putInt("DATA", 0).commit();
				Intent intent1 = new Intent();
				// ��ʾ�����ھ�Ľ��
				intent1.setClass(MainActivity.this, ListViewAcitivity.class);
				startActivity(intent1);
			}
		} else {
			CYLog.e(TAG, "account should not null");
		}

		GetNewVersion mGetTask = new GetNewVersion();
		mGetTask.execute();
	}

	/*
	 * �鿴���ͷ����Ƿ��Ѿ�����
	 * 
	 * public boolean isWorked() { ActivityManager myManager = (ActivityManager)
	 * getBaseContext() .getSystemService(Context.ACTIVITY_SERVICE);
	 * ArrayList<RunningServiceInfo> runningService =
	 * (ArrayList<RunningServiceInfo>) myManager .getRunningServices(300); for
	 * (int i = 0; i < runningService.size(); i++) {
	 * 
	 * CYCYLog.d("===", runningService.get(i).service.getClassName()
	 * .toString());
	 * 
	 * if (runningService.get(i).service.getClassName().toString()
	 * .equals("com.hust.schoolmatechat.serverpush.MQTTService")) { return true;
	 * } } return false; }
	 */

	/**
	 * ��PagerSlidingTabStrip�ĸ������Խ��и�ֵ��
	 */
	private void setTabsValue() {
		// ����Tab���Զ��������Ļ��
		mTabs.setShouldExpand(true);
		// ����Tab�ķָ�����͸����
		mTabs.setDividerColor(Color.TRANSPARENT);
		// ����Tab�ײ��ߵĸ߶�
		mTabs.setUnderlineHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 1, dm));
		// ����Tab Indicator�ĸ߶�
		mTabs.setIndicatorHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, dm));
		// ����Tab�������ֵĴ�С
		mTabs.setTextSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 16, dm));

		// ����Tab Indicator����ɫ
		mTabs.setIndicatorColor(getResources().getColor(R.color.fragment_click));
		// ����ѡ��Tab���ֵ���ɫ
		mTabs.setSelectedTextColor(getResources().getColor(
				R.color.fragment_click));
		// ȡ�����Tabʱ�ı���ɫ
		mTabs.setTabBackground(0);
	}

	public class MyPagerAdapter extends FragmentPagerAdapter {

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		private final String[] titles = { getString(R.string.chat_fragment),
				getString(R.string.contacts_fragment),
				getString(R.string.news_fragment),
				getString(R.string.myself_fragment) };

		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public Fragment getItem(int position) {
			if (dataCenterManagerService == null) {
				CYLog.e(TAG,
						"Fragment getItem dataCenterManagerService is null");
				return null;
			}
			switch (position) {
			case 0:
				if (mChatFragment == null) {
					mChatFragment = new ChatFragment();// .newInstance(dataCenterManagerService);
				}
				return mChatFragment;
			case 1:
				if (mContactsFragment == null) {
					mContactsFragment = new ContactsFragment();// .newInstance(dataCenterManagerService);
				}
				return mContactsFragment;
			case 2:
				if (mNewsFragment == null) {
					mNewsFragment = new NewsFragment();// .newInstance(dataCenterManagerService);
				}
				return mNewsFragment;
			case 3:
				if (mMyselfFragment == null) {
					mMyselfFragment = new MyselfFragment();// .newInstance(dataCenterManagerService);
				}
				return mMyselfFragment;
			default:
				return null;
			}
		}

	}

	public DataCenterManagerService getDataCenterManagerService() {
		return dataCenterManagerService;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
				.getActionView();
		if (searchView != null) {
			searchView.setSearchableInfo(searchManager
					.getSearchableInfo(getComponentName()));
			searchView.setIconifiedByDefault(false);
		}
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		CYLog.i(TAG, "onResume");
		if (dataCenterManagerService == null) {
			// Intent intent = new Intent();
			// intent.setClass(this, LogoActivity.class);
			// startActivity(intent);
			// finish();
			//

			/** ���������������Ĺ������ */

			CYLog.e(TAG, "dataCenterManagerService==null");
			final Intent dataCenterManagerIntent = new Intent(this,
					DataCenterManagerService.class);
			bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
					Context.BIND_ABOVE_CLIENT);

			setContentView(R.layout.activity_main);
		}

	}

	/*
	 * public DataCenterManagerService getDataCenterManagerService(){
	 * if(dataCenterManagerService!=null){ return dataCenterManagerService;
	 * }else{ CYLog.e(TAG,"getDataCenterManagerService,rebindService"); final
	 * Intent dataCenterManagerIntent = new Intent(this,
	 * DataCenterManagerService.class); bindService(dataCenterManagerIntent,
	 * dataCenterManagerIntentConn, Context.BIND_ABOVE_CLIENT);
	 * if(dataCenterManagerService==null){
	 * CYLog.e(TAG,"re bindService dataCenterManagerService==null");
	 * 
	 * } return dataCenterManagerService; }
	 * 
	 * }
	 */
	@Override
	public void onDestroy() {
		CYLog.i(TAG, "onDestroy");
		// �رշ���
		final Intent intent = new Intent(this, DataCenterManagerService.class);
		// add ȡ���� ibind
		unbindService(dataCenterManagerIntentConn);

		onStopForFragmentDestory();

		super.onDestroy();
	}

	/**
	 * fragment destory��ʱ����ã��ڴ�й¶
	 */
	public void onStopForFragmentDestory() {
		if (broadcastReceiverList == null) {
			return;
		}

		for (int i = 0; i < broadcastReceiverList.size(); ++i) {
			try {
				this.unregisterReceiver(broadcastReceiverList.get(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * �洢���еĹ���MainActivity�ϵ�BroadcastReceiver
	 */
	public void addBroadcastReceiverToList(BroadcastReceiver broadcastReceiver) {
		if (this.broadcastReceiverList == null) {
			broadcastReceiverList = new ArrayList<BroadcastReceiver>();
		}

		try {
			broadcastReceiverList.add(broadcastReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod(
							"setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}

	private void setOverflowShowingAlways() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����˵��¼���
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// �õ���ǰѡ�е�MenuItem��ID,
		int item_id = item.getItemId();
		switch (item_id) {
		// case R.id.action_plus:
		// //Toast.makeText(this, "����", Toast.LENGTH_SHORT).show();
		// break;
		case R.id.action_search:
			// Toast.makeText(this, "����", Toast.LENGTH_SHORT).show();
			break;
		case R.id.action_collection:
			Intent intent1 = new Intent();
			intent1.setClass(MainActivity.this, ListViewAcitivity.class);
			startActivity(intent1);
			break;
		case R.id.action_settings:
			Intent intent = new Intent();
			intent.setClass(this, SettingActivity.class);
			startActivityForResult(intent, 1);
			// Toast.makeText(this, "����", Toast.LENGTH_SHORT).show();
			break;
		case R.id.action_feed:
			// Toast.makeText(this, "�������", Toast.LENGTH_SHORT).show();
			Intent intent2 = new Intent(getApplicationContext(),
					NewsExploreActivitiy.class);
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			String uid = prefs.getString("USERNAME", "");
			intent2.putExtra("newsUrl", APPConstant.FEEDBACKURL + uid);
			intent2.putExtra("userName", "�������");
			startActivity(intent2);
			break;
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onResume();
		if (resultCode == 1) {
			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// moveTaskToBack(false);
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// ע��
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			return true;

		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		super.onStop();
	}

	UpdateApp update = new UpdateApp(MainActivity.this);

	public class GetNewVersion extends AsyncTask<Void, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			return update.doUpdateApp();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			if (success) {
				try {
					update.showUpdateDialog();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			super.onPostExecute(success);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}

}