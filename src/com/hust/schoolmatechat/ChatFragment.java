package com.hust.schoolmatechat;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.FaceInput.FaceConversionUtil;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.AppEngine;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.engine.ChatMessage;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.utils.DateFormatUtils;
import com.hust.schoolmatechat.utils.ImageUtils;
import com.hust.schoolmatechat.view.MsgListView;
import com.hust.schoolmatechat.view.MsgListView.OnRefreshListener;

/**
 * 窗友安卓app框架
 * 
 * 
 * @author luoguangzhen
 */
public class ChatFragment extends Fragment implements Observer {
	private static final String TAG = "ChatFragment";
	// public MainActivity mContext;
	public RelativeLayout mLayout;
	public Button mButtom;
	private MsgListView listView = null;
	private LayoutInflater inflater = null;
	private ChatFragmentAdapter adapter = null;
	List<ChatItem> chatItems;
	/** 对接数据中心服务 */
	private DataCenterManagerService dataCenterManagerService;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			ChatItem rcvItem = null;
			switch (msg.what) {

			case 0:

				updateChatFramentUI();
				// listView.setSelection(listView.getBottom());
				/*
				 * String[] objs = (String[]) msg.obj;
				 * 
				 * 
				 * for (ChatItem item : chatItems) { if
				 * (item.getCid().equals(objs[0])) {
				 * item.setUnread(item.getUnread() + 1);
				 * item.setLatestMessage(objs[1]); item.setTime(new
				 * Date(System.currentTimeMillis())); rcvItem = item;
				 * updateChatItem2Top(item);
				 * 
				 * } }
				 * 
				 * if (rcvItem == null) { rcvItem = new ChatItem();
				 * rcvItem.setCid(objs[0]); rcvItem.setNickName(objs[0]);
				 * rcvItem.setLatestMessage(objs[1]);
				 * rcvItem.setType(ChatItem.PRIVATECHATITEM);
				 * rcvItem.setUnread(rcvItem.getUnread() + 1);
				 * rcvItem.setTime(new Date(System.currentTimeMillis()));
				 * addChatItem2List(rcvItem); }
				 */
				break;

			case 1:
				updateChatFramentUI();
				// listView.setSelection(listView.getBottom());
				// PushedMessage pushedMessage= (PushedMessage) msg.obj;

				/*
				 * for (ChatItem item : chatItems) { if
				 * (item.getCid().equals(pushedMessage.getChannelId())) {
				 * item.setUnread(item.getUnread() + 1);
				 * item.setLatestMessage(pushedMessage.getNewsSummary());
				 * item.setTime(pushedMessage.getTime()); rcvItem = item;
				 * updateChatItem2Top(item);
				 * 
				 * } }
				 * 
				 * if (rcvItem == null) { rcvItem = new ChatItem();
				 * rcvItem.setCid(pushedMessage.getChannelId());
				 * rcvItem.setNickName(pushedMessage.getcName());
				 * rcvItem.setLatestMessage(pushedMessage.getNewsSummary());
				 * rcvItem.setType(ChatItem.NEWSITEM);
				 * rcvItem.setUnread(rcvItem.getUnread() + 1);
				 * rcvItem.setTime(pushedMessage.getTime());
				 * addChatItem2List(rcvItem); }
				 */
				break;

			default:
				break;
			}

			super.handleMessage(msg);
		}

	};

	public ChatFragment() {
	}

	// public ChatFragment(DataCenterManagerService dataCenterManagerService,
	// MainActivity mContext2) {
	// this.dataCenterManagerService = dataCenterManagerService;
	// // this.mContext = mContext2;
	// }

	// public static ChatFragment newInstance(
	// DataCenterManagerService dataCenterManagerService) {
	//
	// ChatFragment cf = new ChatFragment();
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
		// }
		if (getActivity() instanceof MainActivity)
			this.dataCenterManagerService = ((MainActivity) getActivity())
					.getDataCenterManagerService();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.chaititemlist_main, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// if (mContext == null) {
		// mContext = (MainActivity) this.getActivity();
		// }
		// if(dataCenterManagerService==null){
		// dataCenterManagerService=mContext.getDataCenterManagerService();
		// }
		initChatFragment();
	}

	private void initChatFragment() {

		AppEngine.getInstance(getActivity()).addObserver(this);
		mLayout = (RelativeLayout) getActivity().findViewById(
				R.id.chaititemlist_mainlayout);
		if (dataCenterManagerService == null || mLayout == null) {
			CYLog.e(TAG, "initChatFragment dataCenterManagerService null");
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), LogoActivity.class);
			startActivity(intent);
			this.getActivity().finish();
			return;
		}
		// 注册聊天信息receiver
		IntentFilter intentFilter = new IntentFilter(
				"com.schoolmatechat.message");
		ChatMessageBroadCastReceiver chatMessageReceiver = new ChatMessageBroadCastReceiver(
				this);
		getActivity().registerReceiver(chatMessageReceiver, intentFilter);
		if (getActivity() instanceof MainActivity)
			((MainActivity) getActivity())
					.addBroadcastReceiverToList(chatMessageReceiver);

		// 注册推送信息receiver
		IntentFilter newsFilter = new IntentFilter(
				"com.schoolmatechat.newsadded2message");
		PushedMessageBroadCastReceiver pushedMsgReceiver = new PushedMessageBroadCastReceiver(
				this);
		getActivity().registerReceiver(pushedMsgReceiver, newsFilter);
		if (getActivity() instanceof MainActivity)
			((MainActivity) getActivity())
					.addBroadcastReceiverToList(pushedMsgReceiver);

		listView = (MsgListView) getActivity().findViewById(
				R.id.listView_chatitems);
		adapter = new ChatFragmentAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new MyItemClickListener());
		listView.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
			//	CYLog.d(TAG, "setonRefreshListener,onRefresh");
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try {
							Thread.sleep(500);
							CYLog.d(TAG, "Thread.sleep(500);");
						} catch (Exception e) {
							e.printStackTrace();
							CYLog.e(TAG, "Thread.sleep(500);"+e);
						}
						if (dataCenterManagerService != null){
							chatItems = dataCenterManagerService.getChatItems();
							CYLog.d(TAG, "dataCenterManagerService.getChatItems()");
							if(chatItems!=null)
								CYLog.d(TAG, "chatItems.size()="+chatItems.size());
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						CYLog.d(TAG, "onPostExecute"+result);
						adapter.notifyDataSetChanged();
						listView.onRefreshComplete();
					}
				}.execute();
			}
		},1);

		inflater = (LayoutInflater) getActivity().getSystemService(
				getActivity().LAYOUT_INFLATER_SERVICE);
		chatItems = dataCenterManagerService.getChatItems();
	}

	@Override
	public void onResume() {
		super.onResume();
		// setSkin();
		CYLog.d(TAG, APPBaseInfo.URL);
		if (adapter == null) {
			CYLog.e(TAG, "dataCenterManagerService == null,restartapp");
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), LogoActivity.class);
			startActivity(intent);
			this.getActivity().finish();
			return;
		} else {
			updateChatFramentUI();
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

	/**
	 * 通知adapter更新数据
	 */
	public synchronized void updateChatFramentUI() {
		// try {
		// android.os.Message msg = new android.os.Message();
		// msg.what = 0;
		// handler.sendMessage(msg);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		if (adapter == null && dataCenterManagerService != null) {
			// if (mContext == null) {
			// mContext = (MainActivity) this.getActivity();
			// }
			// if(dataCenterManagerService==null){
			// dataCenterManagerService=mContext.getDataCenterManagerService();
			// }
			initChatFragment();
		}
		try {
			if (dataCenterManagerService != null)
				chatItems = dataCenterManagerService.getChatItems();
			adapter.notifyDataSetChanged();
			// listView.setSelection(listView.getBottom());
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "updateChatFramentUI : " + e.toString());
		}
	}

	public void setSkin() {
		int index = Integer.parseInt(AppEngine.getInstance(getActivity())
				.getTheme());
		mLayout.setBackgroundResource(APPConstant.CHATFRAGMENT_BG_COLOR_IDS[index]);
	}

	@Override
	public void update(Observable observable, Object data) {
		setSkin();
	}

	private class ChatFragmentAdapter extends BaseAdapter {
		// private ChatFragment chatFragment;
		// private DataCenterManagerService dataCenterManagerService;
		//
		// ChatFragmentAdapter(ChatFragment chatFragment,
		// DataCenterManagerService dataCenterManagerService) {
		// this.chatFragment = chatFragment;
		// this.dataCenterManagerService = dataCenterManagerService;
		// }

		@Override
		public int getCount() {
			if (chatItems == null) {
				return 0;
			}
			//CYLog.d(TAG, "getCount ="+chatItems.size());
			return chatItems.size();
		}

		@Override
		public Object getItem(int position) {
			if (chatItems == null) {
				return null;
			}
			//CYLog.d(TAG, "getItem position ="+position);
			return chatItems.get(position);
		}

		@Override
		public long getItemId(int position) {
		//	CYLog.d(TAG, "getItemId ="+position+",chatItems.size() = "+chatItems.size());
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//CYLog.d(TAG, "getView ="+position);
			if (chatItems == null) {
				CYLog.e(TAG, "ChatFragmentAdapter getView chatItems is null");
				return null;
			}

			View view = getInflater().inflate(R.layout.chatitemlist_chatitem,
					null);
			ImageView ivChatIcon = (ImageView) view
					.findViewById(R.id.iv_chaticon);
			ImageView ivRedPoint = (ImageView) view
					.findViewById(R.id.iv_redpoint);
			TextView tvUnread = (TextView) view.findViewById(R.id.tv_unread);

			TextView tvNickName = (TextView) view
					.findViewById(R.id.textView_nickname);
			TextView tvTime = (TextView) view.findViewById(R.id.textView_time);
			TextView tvLatestMessage = (TextView) view
					.findViewById(R.id.textView_latestmessage);
			if (position < chatItems.size()) {
				ChatItem chatItem = chatItems.get(position);

				if (chatItem.getUnread() > 0) {
					ivRedPoint.setVisibility(View.VISIBLE);
					tvUnread.setVisibility(View.VISIBLE);
					tvUnread.setText(chatItem.getUnread() + "");
				}

				tvNickName.setText(chatItem.getName());

				if (chatItem.getTime() != null)
					tvTime.setText(DateFormatUtils.date2ChatItemTime(chatItem
							.getTime()));

				if (chatItem.getType() == ChatItem.PRIVATECHATITEM
						|| chatItem.getType() == ChatItem.GROUPCHATITEM) {
					SpannableString spannableString = FaceConversionUtil
							.getInstace().getExpressionString(getActivity(),
									chatItem.getLatestMessage());
					if (chatItem.getLatestMessage().startsWith(
							APPConstant.CMD_PREFIX_FILE_SEND)) {
						int start = chatItem.getLatestMessage()
								.lastIndexOf("/");// 成功发送的url中取文件类型
						if (start <= 0) {// 没发成功的直接前缀后面直接带的文件类型
							start = chatItem.getLatestMessage().length();
						}
						String type = chatItem.getLatestMessage().substring(
								start - 1, start);
						// 最近的一条消息如果是文件的话，显示[文件类型]
						if (type.equals("" + APPConstant.PICTURE)) {
							tvLatestMessage.setText(APPConstant.SPICTURE);
						} else if (type.equals("" + APPConstant.AUDIO)) {
							tvLatestMessage.setText(APPConstant.SAUDIO);
						} else if (type.equals("" + APPConstant.VIDEO)) {
							tvLatestMessage.setText(APPConstant.SVIDEO);
						} else if (type.equals("" + APPConstant.NORMAL_FILE)) {
							tvLatestMessage.setText(APPConstant.SNORMAL_FILE);
						}
					} else if (spannableString != null) {
						tvLatestMessage.setText(spannableString);
					}
					// 联系人信息 +++lqg+++ 群聊item没有图像
					if (chatItem.getType() == ChatItem.PRIVATECHATITEM) {
						ContactsEntity friendContactsEntity = dataCenterManagerService
								.getFriendInfoByAccount(chatItem.getOwner());
						if (friendContactsEntity != null) {
							String icon = friendContactsEntity.getPicture();
							if (icon != null) {
								// 从服务器拿图片
								ImageUtils.setUserHeadIcon(ivChatIcon, icon,
										handler);
							} else {
								if (chatItem.getIcon() != null) {
									// 从服务器拿图片
									ImageUtils.setUserHeadIcon(ivChatIcon,
											chatItem.getIcon(), handler);
								}
							}
						} else {
							//本地没有联系人信息，删除item
							dataCenterManagerService.deleteChatItem(chatItem.getOwner(), ChatItem.PRIVATECHATITEM);
							if (chatItem.getIcon() != null) {
								// 从服务器拿图片
								ImageUtils.setUserHeadIcon(ivChatIcon,
										chatItem.getIcon(), handler);
							}
						}
					} else if (chatItem.getType() == ChatItem.GROUPCHATITEM) {
						if (chatItem.getIcon() != null) {
							// 设置群图标
							String tmp = chatItem.getIcon();
							ImageUtils.setUserHeadIcon(ivChatIcon,
									chatItem.getIcon(), handler);
						}
					}

				} else {
					tvLatestMessage.setText(chatItem.getLatestMessage());
					if (chatItem.getIcon() != null) {
						// 从服务器拿图片
					//	CYLog.d(TAG, "" + chatItem.getIcon());
						ImageUtils.setIcon(ivChatIcon, chatItem.getIcon(),
								handler);
					}
				}
			} else {
				view.setVisibility(View.GONE);
			}
			return view;
		}
	}

	private class MyItemClickListener implements
			AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			if (chatItems == null) {
				CYLog.e(TAG,
						"MyItemClickListener onItemClick chatItems is null");
				return;
			}
			// Toast.makeText(mContext, "item"+position+"clicked",
			// Toast.LENGTH_SHORT).show();
			ChatItem clickedItem = chatItems.get( position>0?position-1:position);
			clickedItem.setUnread(0);// 未读消息数为0
			dataCenterManagerService.updateChatItemToDb(clickedItem);// 更新点击的消息

			// // 打开Chat页面
			// if (clickedItem.getType() != ChatItem.GROUPCHATITEM) {
			Intent intent = new Intent(getActivity(), ChatActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putSerializable(ChatItem.SER_KEY, clickedItem);
			if (clickedItem.getType() == ChatItem.GROUPCHATITEM) {
				mBundle.putString("roomId", clickedItem.getOwner());
				mBundle.putString("roomName", clickedItem.getName());
			}
			intent.putExtras(mBundle);
			// CYLog.i(TAG, "chat owner : " + clickedItem.getOwner());
			startActivity(intent);
			// } else {
			// Intent groupchatActivity = new Intent(mContext
			// .getApplicationContext(), GroupChatActivity.class);
			// groupchatActivity.putExtra("groupchatRoomId",
			// clickedItem.getOwner());
			// CYLog.i(TAG, "chat owner : " + clickedItem.getOwner());
			// startActivity(groupchatActivity);
			// }
		}
	}

	public LayoutInflater getInflater() {
		return this.inflater;
	}

	/*
	 * 接收聊天消息更新的广播
	 */
	class ChatMessageBroadCastReceiver extends BroadcastReceiver {
		private ChatFragment chatFragment;

		ChatMessageBroadCastReceiver(ChatFragment chatFragment) {
			this.chatFragment = chatFragment;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			CYLog.i(TAG, "收到更新ChatFragment广播消息!");
			chatFragment.updateChatFramentUI();
		}

	}

	/*
	 * 接收推送消息更新的广播
	 */
	class PushedMessageBroadCastReceiver extends BroadcastReceiver {
		private ChatFragment chatFragment;

		PushedMessageBroadCastReceiver(ChatFragment chatFragment) {
			this.chatFragment = chatFragment;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			chatFragment.updateChatFramentUI();
		}

	}

}
