package com.hust.schoolmatechat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.utils.ImageUtils;

public class ViewGroupChatDetailedInfoActivity extends Activity {
	private final String TAG = "ViewGroupChatDetailedInfoActivity";

	TextView mTextView = null;
	// ListView
	private ListView mListView = null;
	// ListView适配器
	// private SimpleAdapter mListViewAdapter;
	private MyAdapter mListViewAdapter;
	private List<Map<String, Object>> mDataSource;
	private List<ContactsEntity> mOccupantsList;

	// 标记此页面是从哪个页面跳转过来的
	private String mFromPage = null;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				CYLog.i(TAG, "已经获取了聊天室详细信息!正在更新UI!");
				mListViewAdapter.notifyDataSetChanged();
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

			initViewGroupChatDetailedInfoActivity();
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
		// startService(dataCenterManagerIntent);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
				Context.BIND_ABOVE_CLIENT);
	}

	private void initViewGroupChatDetailedInfoActivity() {
		// 设置自定义ActionBar
		ActionBar bar = getActionBar();
		bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE);
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setDisplayShowHomeEnabled(false);
		LayoutParams customActionbarParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		bar.setTitle("群组信息");

		setContentView(R.layout.activity_group_chat_room_info);

		mTextView = (TextView) findViewById(R.id.groupchat_room_name);
		mListView = (ListView) findViewById(R.id.groupchat_room_detailed_info);
		mDataSource = new ArrayList<Map<String, Object>>();

		String groupId = getIntent().getStringExtra("roomId");
		String roomName = getIntent().getStringExtra("roomName");
		mFromPage = getIntent().getStringExtra("fromPage");
		CYLog.i(TAG, "此聊天室id=" + groupId);
		mOccupantsList = dataCenterManagerService
				.getAllOccupantsOfRoom(groupId);
		if (mOccupantsList == null) {
			mOccupantsList = new ArrayList<ContactsEntity>();
		}
		mOccupantsList.add(this.dataCenterManagerService
				.getUserSelfContactsEntity());
		mTextView.setText(roomName);

		CYLog.i(TAG, "此聊天室中成员人数=" + mOccupantsList.size());

		// for(ContactsEntity occupant:occupantsList){
		// Map<String,Object> columnItem = new HashMap<String,Object>();
		// columnItem.put("item0", "头像暂无");
		// columnItem.put("item1", occupant.getAccountNum());
		// columnItem.put("item2", occupant.getName());
		// }

		// mListViewAdapter = new SimpleAdapter(this, mDataSource,
		// R.layout.groupchat_room_detailed_info_item,
		// new String[] { "item0", "item1", "item2"}, new int[] {
		// R.id.groupchat_room_detailed_info_column0,
		// R.id.groupchat_room_detailed_info_column1,
		// R.id.groupchat_room_detailed_info_column2});
		mListViewAdapter = new MyAdapter();
		mListView.setAdapter(mListViewAdapter);
		mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

			}
		});

	}

	private class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return mOccupantsList.size();
		}

		@Override
		public Object getItem(int position) {
			return mOccupantsList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ContactsEntity currentItem = mOccupantsList.get(position);
			LayoutInflater inflater = getLayoutInflater();

			View view = inflater.inflate(
					R.layout.groupchat_room_detailed_info_item, null);
			TextView tvContactName = (TextView) view
					.findViewById(R.id.textView_groupm_nickname);
			TextView tvSignature = (TextView) view
					.findViewById(R.id.textView_groupm_signature);

			tvContactName.setText(currentItem.getName());
			tvSignature.setText(currentItem.getSign());

			ImageView ivAuthorized = (ImageView) view
					.findViewById(R.id.imageView_groupm_authorized);
			if (currentItem.getAuthenticated().equals("0")) {
				ivAuthorized.setImageResource(R.drawable.flag_unregistered);
			} else if (currentItem.getAuthenticated().equals("1")) {
				ivAuthorized.setImageResource(R.drawable.flag_registered);
			}

			ImageView ivGM = (ImageView) view
					.findViewById(R.id.iv_groupm_contacticon);
			String icon = currentItem.getPicture();
			if (icon != null) {
				ImageUtils.setUserHeadIcon(ivGM, icon, handler);
			}

			return view;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			Intent intent = null;
			if (mFromPage.equals("ContactsFragment")) {
				intent = new Intent(this, MainActivity.class);
			} else if (mFromPage.equals("ChatActivity")) {
				String roomName = getIntent().getStringExtra("roomName");
				String roomId = getIntent().getStringExtra("roomId");
				List<ChatItem> chatItems = dataCenterManagerService
						.getChatItems();
				ChatItem chatItem = null;
				for (ChatItem chatitem : chatItems) {
					if (roomId != null && chatitem != null
							&& chatitem.getOwner() != null
							&& roomId.equals(chatitem.getOwner())) {
						chatitem.setUnread(0);
						chatItem = chatitem;
						break;
					}
				}
				if (chatItem == null) {
					CYLog.e(TAG, "error can't find groupitem groupId : "
							+ roomId + " name : " + roomName);
					ChatItem ci = new ChatItem();
					ci.setOwner(roomId);
					ci.setUserAccount(dataCenterManagerService
							.getUserSelfContactsEntity().getUserAccount());
					ci.setFriendAccount("");
					ci.setName(roomName);
					ci.setTime(new Date());
					ci.setLatestMessage("new group msg come");
					ci.setUnread(0);
					ci.setType(ChatItem.GROUPCHATITEM);
					dataCenterManagerService.getChatItems().add(ci);
					chatItem = ci;
					// return;
				}

				// 打开Chat页面
				intent = new Intent(this, ChatActivity.class);
				Bundle mBundle = new Bundle();
				mBundle.putSerializable(ChatItem.SER_KEY, chatItem);
				mBundle.putString("roomId", roomId);
				mBundle.putString("roomName", roomName);
				intent.putExtras(mBundle);
				// CYLog.i(TAG, "chat owner : " + chatItem.getOwner());
				startActivity(intent);
			}

			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
