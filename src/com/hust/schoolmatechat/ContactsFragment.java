package com.hust.schoolmatechat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.hust.schoolmatechat.ChatActivity.ChatMessageBroadCastReceiver;
import com.hust.schoolmatechat.ChatMsgservice.GroupChatRoom;
import com.hust.schoolmatechat.ChatMsgservice.GroupChatRoomEntity;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.AppEngine;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.engine.GroupManagementItem;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.login.CheckNetworkState;
import com.hust.schoolmatechat.utils.ImageUtils;

/**
 * ���Ѱ�׿app���
 * 
 * 
 * @author luoguangzhen
 */
public class ContactsFragment extends Fragment implements Observer {
	private static final String TAG = "ContactsFragment";
	// public MainActivity mContext;
	public RelativeLayout mLayout;
	private LayoutInflater inflater = null;
	private ListView lvGM = null;
	private List<GroupManagementItem> groupManagementItems = new ArrayList<GroupManagementItem>();
	private List<GroupManagementItem> sourceList = null;
	private MyAdapter adapter = null;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				adapter.notifyDataSetChanged();
				lvGM.setSelection(lvGM.getBottom());
				break;
			}
		}
	};

	/** �Խ��������ķ��� */
	private DataCenterManagerService dataCenterManagerService = null;

	public ContactsFragment() {
	}

	/*
	 * public ContactsFragment(DataCenterManagerService
	 * dataCenterManagerService, MainActivity mContext2) { CYLog.i(TAG,
	 * "ContactsFragment"); this.dataCenterManagerService =
	 * dataCenterManagerService; this.mContext = mContext2;
	 * 
	 * // ע��������Ϣreceiver IntentFilter intentFilter = new IntentFilter(
	 * "com.schoolmatechat.message"); ChatMessageBroadCastReceiver
	 * chatMessageReceiver = new ChatMessageBroadCastReceiver();
	 * this.mContext.registerReceiver(chatMessageReceiver, intentFilter);
	 * this.mContext.addBroadcastReceiverToList(chatMessageReceiver);
	 * 
	 * //����������磬�������ҿ���ִ�и��²�������� if
	 * (!dataCenterManagerService.isUpdateUnAuthenticatedContactsStarted() &&
	 * CheckNetworkState.getInstance(mContext).getNetworkState()) {
	 * dataCenterManagerService.updateUnAuthenticatedContacts(); } }
	 */
	// public static ContactsFragment newInstance(
	// DataCenterManagerService dataCenterManagerService) {
	//
	// ContactsFragment cf = new ContactsFragment();
	// Bundle args = new Bundle();
	// args.putParcelable("dataCenterManagerService",
	// dataCenterManagerService);
	// cf.setArguments(args);
	//
	// return cf;
	//
	// }

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		// Bundle args = getArguments();
		// if (args != null) {
		// this.dataCenterManagerService = (DataCenterManagerService)
		// args.getParcelable("dataCenterManagerService");
		if (getActivity() instanceof MainActivity)
			this.dataCenterManagerService = ((MainActivity) getActivity())
					.getDataCenterManagerService();
		// ע��������Ϣreceiver
		IntentFilter intentFilter = new IntentFilter(
				"com.schoolmatechat.message");
		ChatMessageBroadCastReceiver chatMessageReceiver = new ChatMessageBroadCastReceiver();
		getActivity().registerReceiver(chatMessageReceiver, intentFilter);
		if (getActivity() instanceof MainActivity)
			((MainActivity) getActivity())
					.addBroadcastReceiverToList(chatMessageReceiver);

		// ����������磬�������ҿ���ִ�и��²��������
		if (dataCenterManagerService!=null&&!dataCenterManagerService.isUpdateUnAuthenticatedContactsStarted()
				&& CheckNetworkState.getInstance(getActivity())
						.getNetworkState()) {
			dataCenterManagerService.updateUnAuthenticatedContacts();
		}
		// }

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		CYLog.i(TAG, "onCreateView");

		return inflater
				.inflate(R.layout.groupmanagement_main, container, false);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// ifgetActivity()xt == null) {
		// mContext = (MainActivity) this.getActivity();
		// }
		if (dataCenterManagerService == null) {
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), LogoActivity.class);
			startActivity(intent);
			this.getActivity().finish();
			return;
		}
		initContactsFragment();

		IntentFilter intentFilter = new IntentFilter(
				"com.schoolmatechat.onRosterChanged");
		RosterChangedBroadCastReceiver chatReceiver = new RosterChangedBroadCastReceiver();
		getActivity().registerReceiver(chatReceiver, intentFilter);
		if (getActivity() instanceof MainActivity)
			((MainActivity) getActivity())
					.addBroadcastReceiverToList(chatReceiver);

		CYLog.i(TAG, "onActivityCreated");
	}

	private void initContactsFragment() {
		// if (mContext == null) {
		// mContext = (MainActivity) getActivity();
		// }
		AppEngine.getInstance(getActivity()).addObserver(this);
		mLayout = (RelativeLayout) getActivity().findViewById(
				R.id.layout_groupm);
		if (dataCenterManagerService != null) {
			sourceList = new ArrayList<GroupManagementItem>();

			updateSourcelist();
			filterVisibleList(sourceList, groupManagementItems);
		}
		lvGM = (ListView) getActivity().findViewById(R.id.listView_groupm_list);
		adapter = new MyAdapter();
		inflater = (LayoutInflater) getActivity().getSystemService(
				getActivity().LAYOUT_INFLATER_SERVICE);
		if (lvGM == null || adapter == null) {
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), LogoActivity.class);
			startActivity(intent);
			this.getActivity().finish();
			return;
		} else {
			lvGM.setAdapter(adapter);
			lvGM.setOnItemClickListener(new GMItemClickListener());
			registerForContextMenu(lvGM);
		}
	}

	/*
	 * ����������Ϣ���µĹ㲥
	 */
	class ChatMessageBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Bundle bundle = intent.getExtras();
				if(bundle != null && bundle.containsKey("from") 
						&& bundle.getString("from").equals("updateUI")) {
					// ����UI
					adapter.notifyDataSetChanged();
					CYLog.i(TAG, "update UI msg");
				}
			} catch (Exception e) {
				e.printStackTrace();
				CYLog.e(TAG, "notifyDataSetChanged : " + e.toString());
			}
		}

	}

	/**
	 * ������ϵ�б�
	 * 
	 * @author Administrator
	 * 
	 */
	class RosterChangedBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Bundle bundle = intent.getExtras();
			// android.os.Message msg = new android.os.Message();
			// msg.obj = bundle.getString("from");
			// msg.what = 0;
			// handler.sendMessage(msg);
			CYLog.i(TAG, "�յ�����ContactsFragment�㲥��Ϣ!");
			updateSourcelist();
			filterVisibleList(sourceList, groupManagementItems);
			adapter.notifyDataSetChanged();
			// lvGM.setSelection(lvGM.getBottom());
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		CYLog.i(TAG, "��ʱ������item id=" + view.getId());
		AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
		GroupManagementItem item = groupManagementItems
				.get(adapterMenuInfo.position);
		if (item.getType() == GroupManagementItem.MULTI_CHAT_ROOM) {
			// menu.add(0,1,0,"ɾ��Ⱥ");
			// menu.add(0,2,0,"�鿴��ϸ��Ϣ");
			// menu.add(0,3,0,"����");
			// menu.add(0,4,0,"����");
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.group_chat_room_manage_menu, menu);

			// ���������ʱ�����쳣����Ҫ��isAdminOfGroupChatRoom
			// ���˺Ų���Ⱥ��������ɾ��Ⱥ
			if (!dataCenterManagerService.isCreaterOfGroupChatRoom(item
					.getGmid())) {
				menu.removeItem(R.id.room_delete);
			}
			// ���˺Ų��ǳ�Ⱥ�������һ�����Ա����������
			if (!dataCenterManagerService
					.isAdminOfGroupChatRoom(item.getGmid())) {
				menu.removeItem(R.id.room_kick_person);
			}
			// �����Ⱥ��Ĭ�ϰ༶Ⱥ����ֻ�ܲ鿴Ⱥ��Ϣ
			if (dataCenterManagerService.isDefaultGroupChatRoom(item.getGmid())) {
				menu.removeItem(R.id.room_add_person);
				menu.removeItem(R.id.room_exit);
				menu.removeItem(R.id.room_delete);
				menu.removeItem(R.id.room_kick_person);
			}
		}else if(item.getType() == GroupManagementItem.CONTACT) {
			String fatherGmid = item.getGroupId();
			for(GroupManagementItem i : groupManagementItems) {
				if(i.getGmid().equals(fatherGmid) && 
						i.getName().equals("�ҵĺ���")) {
					MenuInflater inflater = getActivity().getMenuInflater();
					inflater.inflate(R.menu.contact_manage_menu, menu);
					break;
				}
			}
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		final GroupManagementItem manageItem = groupManagementItems
				.get(menuInfo.position);
		TextView tvContactName = (TextView) menuInfo.targetView
				.findViewById(R.id.textView_groupm_nickname);

		switch (item.getItemId()) {
		case R.id.room_delete: {
			CYLog.i(TAG, "����Ĳ˵�������=" + tvContactName.getText());
			final Map<String, Integer> targets = new HashMap<String, Integer>();
			targets.put(dataCenterManagerService
					.getCreaterOfGroupChatRoom(manageItem.getGmid()), 1);

			Dialog dialog = new AlertDialog.Builder(getActivity())
					.setTitle("ɾȺ����")
					.setMessage("��ȷ��Ҫɾ��������" + manageItem.getName() + "��?")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									dataCenterManagerService.kickByUserId(
											manageItem.getGmid(), targets);
								}
							})
					.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub

								}
							}).create();
			dialog.show();
		}
			break;
		case R.id.room_detailed_info: {
			Intent intent = new Intent();
			Bundle mBundle = new Bundle();
			mBundle.putString("roomId", manageItem.getGmid());
			mBundle.putString("roomName", manageItem.getName());
			mBundle.putString("fromPage", "ContactsFragment");
			intent.putExtras(mBundle);
			intent.setClass(getActivity(),
					ViewGroupChatDetailedInfoActivity.class);
			getActivity().startActivity(intent);
		}
			break;
		case R.id.room_add_person: {
			Intent intent = new Intent();
			Bundle mBundle = new Bundle();
			mBundle.putString("roomId", manageItem.getGmid());
			mBundle.putString("roomName", manageItem.getName());
			intent.putExtras(mBundle);
			intent.setClass(getActivity(), GroupChatJoinActivity.class);
			getActivity().startActivity(intent);
		}
			break;
		case R.id.room_kick_person: {
			Intent intent = new Intent();
			Bundle mBundle = new Bundle();
			mBundle.putString("roomId", manageItem.getGmid());
			mBundle.putString("roomName", manageItem.getName());
			intent.putExtras(mBundle);
			intent.setClass(getActivity(), GroupChatKickActivity.class);
			getActivity().startActivity(intent);
		}
			break;
		case R.id.room_exit: {
			final Map<String, Integer> targets = new HashMap<String, Integer>();
			if (dataCenterManagerService.isAdminOfGroupChatRoom(manageItem
					.getGmid()))
				targets.put(dataCenterManagerService.getTigaseAccount(), 1);
			else
				targets.put(dataCenterManagerService.getTigaseAccount(), 0);

			Dialog dialog = new AlertDialog.Builder(getActivity())
					.setTitle("��Ⱥ����")
					.setMessage("��ȷ��Ҫ�˳�������" + manageItem.getName() + "��?")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									dataCenterManagerService.kickByUserId(
											manageItem.getGmid(), targets);
								}
							})
					.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub

								}
							}).create();
			dialog.show();
		}
			break;
		case R.id.contact_delete:
		{
			String contactId = manageItem.getGmid().replace("contact_", "");
			this.dataCenterManagerService.sendFriendAddDecline(contactId, 1);
		}
			break;
		}
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (adapter == null) {
			CYLog.e(TAG, "dataCenterManagerService == null,restartapp");
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), LogoActivity.class);
			startActivity(intent);
			this.getActivity().finish();
			return;
			/*
			 * // if(dataCenterManagerService==null){ //
			 * dataCenterManagerService
			 * =getActivity().getDataCenterManagerService(); // }
			 * initContactsFragment(); adapter.notifyDataSetChanged();
			 */
		} else {
			// ifgetActivity()xt == null) {
			// getActivity() = (MainActivity) this.getActivity();
			// }
			adapter.notifyDataSetChanged();
		}
	}

	public void setSkin() {
		int index = Integer.parseInt(AppEngine.getInstance(getActivity())
				.getTheme());
		mLayout.setBackgroundResource(APPConstant.CONTACTSFRAGMENT_BG_COLOR_IDS[index]);
	}

	@Override
	public void update(Observable observable, Object data) {
		try {
			setSkin();
			updateSourcelist();
			// ++++lqg+++ ֮ǰ��ֱ��new������ط���UI���Ӻã���Ӧ��ֱ��new��������
			if (adapter == null) {
				adapter = new MyAdapter();
				lvGM.setAdapter(adapter);
			}
			adapter.notifyDataSetChanged();
			// lvGM.setSelection(lvGM.getBottom());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return groupManagementItems.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return groupManagementItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			GroupManagementItem currentItem = groupManagementItems
					.get(position);

			View view = null;

			switch (currentItem.getType()) {
			case GroupManagementItem.GROUP:
			case GroupManagementItem.MULTI_CHAT_GROUP:
				view = inflater.inflate(R.layout.groupmanagement_group, null);
				TextView tvGroupName = (TextView) view
						.findViewById(R.id.textView_groupname);

				tvGroupName.setText(currentItem.getName());

				ImageView ivArrow = (ImageView) view
						.findViewById(R.id.imageView_group_arrow);
				if (currentItem.isExtended()) {
					ivArrow.setImageResource(R.drawable.group_item_arrow_down);
				} else {
					ivArrow.setImageDrawable(getResources().getDrawable(
							R.drawable.group_item_arrow_right));
				}

				break;

			case GroupManagementItem.CONTACT:
				view = inflater.inflate(R.layout.groupmanagement_contact, null);
				TextView tvContactName = (TextView) view
						.findViewById(R.id.textView_groupm_nickname);
				TextView tvSignature = (TextView) view
						.findViewById(R.id.textView_groupm_signature);

				tvContactName.setText(currentItem.getName());
				tvSignature.setText(currentItem.getSignature());

				ImageView ivAuthorized = (ImageView) view
						.findViewById(R.id.imageView_groupm_authorized);
				if (currentItem.getAuthorized() == 0) {
					ivAuthorized.setImageResource(R.drawable.flag_unregistered);
				} else if (currentItem.getAuthorized() == 1) {
					ivAuthorized.setImageResource(R.drawable.flag_registered);
				}

				ImageView ivGM = (ImageView) view
						.findViewById(R.id.iv_groupm_contacticon);
				// ��ϵ����Ϣ
				ContactsEntity friendContactsEntity = dataCenterManagerService
						.getFriendInfoByAccount(currentItem.getUserId());
				if (friendContactsEntity != null) {
					String icon = friendContactsEntity.getPicture();
					if (icon != null) {
						ImageUtils.setUserHeadIcon(ivGM, icon, handler);
					} else {
						if (currentItem.getIcon() != null) {
							// �ӷ�������ͼƬ
							ImageUtils.setUserHeadIcon(ivGM,
									currentItem.getIcon(), handler);
						}
					}
				} else {
					if (currentItem.getIcon() != null) {
						// �ӷ�������ͼƬ
						ImageUtils.setUserHeadIcon(ivGM, currentItem.getIcon(),
								handler);
					}
				}

				break;

			case GroupManagementItem.MULTI_CHAT_ROOM:
				view = inflater.inflate(R.layout.groupmanagement_contact, null);
				TextView tvRoomName = (TextView) view
						.findViewById(R.id.textView_groupm_nickname);
				TextView tvRoomSignature = (TextView) view
						.findViewById(R.id.textView_groupm_signature);

				tvRoomName.setText(currentItem.getName());
				tvRoomSignature.setText(currentItem.getSignature());

				ImageView ivRoomAuthorized = (ImageView) view
						.findViewById(R.id.imageView_groupm_authorized);
				if (currentItem.getAuthorized() == 0) {
					ivRoomAuthorized
							.setImageResource(R.drawable.flag_unregistered);
				} else if (currentItem.getAuthorized() == 1) {
					ivRoomAuthorized
							.setImageResource(R.drawable.flag_registered);
				}

				ImageView ivRoomGM = (ImageView) view
						.findViewById(R.id.iv_groupm_contacticon);
				if (currentItem.getIcon() != null) {
					// ����Ⱥͼ��
					ImageUtils.setUserHeadIcon(ivRoomGM, currentItem.getIcon(),
							handler);
				}

				break;

			default:
				break;
			}
			return view;
		}
	}

	private class GMItemClickListener implements
			AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (dataCenterManagerService == null) {
				CYLog.e(TAG, "onItemClick dataCenterManagerService");
				return;
			}

			GroupManagementItem currentItem = (GroupManagementItem) adapter
					.getItem(position);

			if (GroupManagementItem.GROUP == currentItem.getType()
					|| GroupManagementItem.MULTI_CHAT_GROUP == currentItem
							.getType()) {
				if (currentItem.isExtended()) {
					currentItem.setExtended(false);
				} else {
					currentItem.setExtended(true);
				}

				/*
				 * ImageView ivArrow=(ImageView)
				 * view.findViewById(R.id.imageView_group_arrow);
				 * if(currentItem.isExtended()){
				 * ivArrow.setImageResource(R.drawable.group_item_arrow_right);
				 * }else{
				 * ivArrow.setImageDrawable(getResources().getDrawable(R.drawable
				 * .group_item_arrow_right)); }
				 */
				if (sourceList != null) {
					for (GroupManagementItem item : sourceList) {
						if (item.getGroupId().equals(currentItem.getGroupId())
								&& (GroupManagementItem.CONTACT == item
										.getType() || GroupManagementItem.MULTI_CHAT_ROOM == item
										.getType())) {
							if (item.isVisible()) {
								item.setVisible(false);

							} else {
								item.setVisible(true);
							}
						}
					}
					filterVisibleList(sourceList, groupManagementItems);
					adapter.notifyDataSetChanged();
					// lvGM.setSelection(lvGM.getBottom());
				}
			}
			if (GroupManagementItem.CONTACT == currentItem.getType()) {
				if (currentItem.getAuthorized() == 1) {
					// ��֤�û���������ҳ��
					dealContactGroupManagementItem(currentItem);
				} else {
					// ����������磬�������ҿ���ִ�и��²��������
					if (!dataCenterManagerService
							.isUpdateUnAuthenticatedContactsStarted()
							&& CheckNetworkState.getInstance(getActivity())
									.getNetworkState()) {
						dataCenterManagerService
								.updateUnAuthenticatedContacts();
					}

					// δע���û�����ʾ
					Toast.makeText(getActivity(), "���û���δע������Ѿ�ע�ᵫδ��֤", Toast.LENGTH_SHORT)
							.show();
				}
			} else if (GroupManagementItem.MULTI_CHAT_ROOM == currentItem
					.getType()) {

				// CYLog.i(TAG, " ��ʼ����Ⱥ��!");
				// Intent groupchatActivity = new Intent(getActivity()
				// .getApplicationContext(), GroupChatActivity.class);
				// groupchatActivity.putExtra("groupchatRoomId",
				// currentItem.getName());
				// startActivity(groupchatActivity);

				String name = currentItem.getName();
				String groupId = currentItem.getGmid();
				List<ChatItem> chatItems = dataCenterManagerService
						.getChatItems();
				ChatItem chatItem = null;
				for (ChatItem item : chatItems) {
					if (groupId.equals(item.getOwner())) {
						item.setUnread(0);
						chatItem = item;
						break;
					}
				}
				if (chatItem == null) {
					CYLog.e(TAG, "error can't find groupitem groupId : "
							+ groupId + " name : " + name);
					ChatItem ci = new ChatItem();
					ci.setOwner(groupId);
					ci.setUserAccount(dataCenterManagerService
							.getUserSelfContactsEntity().getUserAccount());
					ci.setFriendAccount("");
					ci.setName(name);
					ci.setTime(new Date());
					ci.setLatestMessage("new group msg come");
					ci.setUnread(0);
					ci.setType(ChatItem.GROUPCHATITEM);
					dataCenterManagerService.getChatItems().add(ci);
					chatItem = ci;
					// return;
				}

				Intent chatActivity = new Intent(getActivity(),
						ChatActivity.class);

				// ��Chatҳ��
				Intent intent = new Intent(getActivity(), ChatActivity.class);
				Bundle mBundle = new Bundle();
				mBundle.putSerializable(ChatItem.SER_KEY, chatItem);
				mBundle.putString("roomId", currentItem.getGmid());
				mBundle.putString("roomName", currentItem.getName());
				intent.putExtras(mBundle);
				// CYLog.i(TAG, "chat owner : " + chatItem.getOwner());
				startActivity(intent);
			}
			// Toast.makeText(GroupManagementActivity.this, "groupclicked",
			// 1).show();
		}

	}

	public void filterVisibleList(List<GroupManagementItem> sourceList,
			List<GroupManagementItem> targetList) {
		targetList.clear();
		targetList.addAll(sourceList);
		for (Iterator<GroupManagementItem> iterator = targetList.iterator(); iterator
				.hasNext();) {
			GroupManagementItem item = iterator.next();
			if (!item.isVisible()) {

				iterator.remove();
			}
		}

	}

	/**
	 * ��һ������ҳ��
	 * 
	 * @param currentItem
	 */
	public void dealContactGroupManagementItem(GroupManagementItem currentItem) {
		try {
			String jid = currentItem.getGmid();// �˴�Ӧȡ�����������ƣ�������
			List<ChatItem> chatItems = dataCenterManagerService.getChatItems();
			for (ChatItem item : chatItems) {
				if (jid.equals(item.getOwner())) {
					item.setUnread(0);
				}
			}
			Intent chatActivity = new Intent(getActivity()
					.getApplicationContext(), ChatActivity.class);
			String jid2 = jid.replace("contact_", "");
			if (jid2.endsWith("@" + APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
				jid2 = jid2.substring(0, jid2.lastIndexOf("@"
						+ APPBaseInfo.TIGASE_SERVER_DOMAIN));
			}

			ChatItem clickedItem = new ChatItem();
			clickedItem.setOwner(jid2);
			clickedItem.setType(ChatItem.PRIVATECHATITEM);
			clickedItem.setUserAccount(dataCenterManagerService
					.getUserSelfContactsEntity().getUserAccount());
			// clickedItem.setFriendAccount(friendAccount);
			// clickedItem.setIcon(icon);
			clickedItem.setName(currentItem.getName());
			// clickedItem.setTime(time);
			// clickedItem.setUnread(unread);
			// clickedItem.setLatestMessage(latestMessage);
			// ��Chatҳ��
			Intent intent = new Intent(getActivity(), ChatActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putSerializable(ChatItem.SER_KEY, clickedItem);
			intent.putExtras(mBundle);
			CYLog.i(TAG, "chat owner : " + clickedItem.getOwner());
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ���·����б�
	 * 
	 * ����GroupManagementItem��gmid����Ϊ���¸�ʽ: (1)һ���Group:"group"+"_"+����
	 * (2)���������ҵ�Group:"Ⱥ��" (3)��ϵ��:"contact"+"_"+RosterEntry.getUser()
	 * (4)������:"groupchat_room_"+"_"+getJid()
	 * (5)δ��֤��ϵ��:"unauthorized"+"_"+getUserId()
	 */
	public void updateSourcelist() {
		if (dataCenterManagerService == null) {
			CYLog.e(TAG, "updateSourcelist dataCenterManagerService");
			return;
		}

		if (sourceList != null) {
			sourceList.clear();// ÿ�ζ�����װ��
		} else {
			sourceList = new ArrayList<GroupManagementItem>();
		}

		// ��˵�Ϊ Ⱥ����
		int item_gmid = 1; // ��������ÿ��GroupManagementItem������,��Ҫȷ��ÿһ�����ж�һ�޶�������

		// ����Ⱥ���ҷ���
		GroupManagementItem groupchat_rooms = new GroupManagementItem();
		groupchat_rooms.setGmid("groupchat_group_" + item_gmid);
		groupchat_rooms.setType(GroupManagementItem.MULTI_CHAT_GROUP);
		groupchat_rooms.setName("�ҵ�Ⱥ��");
		groupchat_rooms.setGroupId(groupchat_rooms.getGmid());
		sourceList.add(groupchat_rooms);
		item_gmid++;
		// ������Ӵ��˺ż����ÿ��Ⱥ����
		List<GroupChatRoomEntity> groupchatRoomsList = dataCenterManagerService
				.getAllGroupChatRoomEntityList();
		if (groupchatRoomsList != null) {
			for (int i = 0; i < groupchatRoomsList.size(); i++) {

				GroupManagementItem groupchat_room = new GroupManagementItem();
				groupchat_room.setGmid(groupchatRoomsList.get(i).getGroupId());
				groupchat_room.setType(GroupManagementItem.MULTI_CHAT_ROOM);
				// ����ϵ��ҳ���chatActiviyҳ��Խӣ���Ҫ��ȷ����Ⱥ�������ƣ�����id����Ҫ��ʱ��ת��Ϊ����
				groupchat_room
						.setName(groupchatRoomsList.get(i).getGroupName());
				groupchat_room.setGroupId(groupchat_rooms.getGmid());
				if (groupchatRoomsList.get(i).getSubject() != null) {
					groupchat_room.setSignature(groupchatRoomsList.get(i)
							.getSubject());
				} else {
					groupchat_room.setSignature("����");
				}
				// ������Ӵ���ȺͼƬʱ���޸�
				groupchat_room.setIcon(Integer.toString(20));
				groupchat_room.setVisible(false);
				sourceList.add(groupchat_room);

				// �����ÿ��Ⱥ�����������ϵ�� add ����
			}
		} else {
			CYLog.e(TAG, "groupchatRoomsList is null");
		}

		// ��ȡ�ڴ��е���ϵ���б�
		Map<String, List<ContactsEntity>> classMap = dataCenterManagerService
				.getContactsEntityMap();
		if (classMap == null || classMap.size() == 0) {
			// ��ȡ�������ݿ��е���ϵ���б�
			classMap = dataCenterManagerService.getLocalDbContactsEntityMap();
			if (classMap == null || classMap.size() == 0) {
				classMap = null;
			}
		}

		if (classMap != null) {
			for (Entry<String, List<ContactsEntity>> entry : classMap
					.entrySet()) {
				// ������е��鵽��ʾҳ��
				GroupManagementItem groupItem = new GroupManagementItem();
				groupItem.setGmid("group" + "_" + item_gmid);
				groupItem.setType(GroupManagementItem.GROUP);
				groupItem.setName(entry.getKey());
				groupItem.setGroupId(groupItem.getGmid());
				if (sourceList != null)
					sourceList.add(groupItem);
				item_gmid++;

				// ���������������е���֤����ϵ�˵���ʾҳ��ǰ��
				List<ContactsEntity> cmrs = entry.getValue();
				for (ContactsEntity cmr : cmrs) {
					GroupManagementItem contactItem = new GroupManagementItem();

					if (cmr.getAuthenticated().equals("1")) {// ��֤
						contactItem.setGmid("contact" + "_"
								+ cmr.getAccountNum());
						contactItem.setAuthorized(1);
					} else {
						continue;
					}

					contactItem.setUserId(cmr.getBaseInfoId());
					contactItem.setType(GroupManagementItem.CONTACT);
					contactItem.setName(cmr.getName());
					// ���ͼƬ��ַ
					contactItem.setIcon(cmr.getPicture());
					contactItem.setSignature(cmr.getSign());
					contactItem.setGroupId(groupItem.getGmid());
					contactItem.setVisible(false);
					sourceList.add(contactItem);
				}

				// ���������������е�δ��֤����ϵ�˵���ʾҳ�����
				for (ContactsEntity cmr : cmrs) {
					GroupManagementItem contactItem = new GroupManagementItem();

					// �ȷ�1Ҳ��0 ���ݸ�ʽ���󣬴�log��δ��֤����
					if (cmr.getAuthenticated().equals("0")
							|| !cmr.getAuthenticated().equals("1")) {// δ��֤
						contactItem.setGmid("unauthorized" + "_"
								+ cmr.getBaseInfoId());
						contactItem.setAuthorized(0);
					} else {
						continue;
					}

					contactItem.setUserId(cmr.getBaseInfoId());
					contactItem.setType(GroupManagementItem.CONTACT);
					contactItem.setName(cmr.getName());
					contactItem.setSignature(cmr.getSign());
					contactItem.setGroupId(groupItem.getGmid());
					contactItem.setVisible(false);
					sourceList.add(contactItem);
				}
			}
		} else {
			CYLog.e(TAG, "classMap is null system error no data sql or web");
		}
	}

	@Override
	public void onDestroy() {
		CYLog.i(TAG, "onDestroy");
		// if (mContext == null) {
		// mContext = (MainActivity) this.getActivity();
		// }
		if (getActivity() instanceof MainActivity)
			((MainActivity) getActivity()).onStopForFragmentDestory();

		super.onDestroy();
	}

	@Override
	public void onStop() {
		CYLog.i(TAG, "onStop");
		super.onStop();
	}

}
