package com.hust.schoolmatechat.ChatMsgservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.NodeType;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.SubscribeForm;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;
import org.xmlpull.v1.XmlPullParser;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Xml;
import android.widget.Toast;

import com.hust.schoolmatechat.SearchSuggestionProvider;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.dao.ChatItemDao;
import com.hust.schoolmatechat.dao.ChatMessageDao;
import com.hust.schoolmatechat.dao.ClassmateDao;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.AppEngine;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.engine.ChatMessage;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.pushedmsgservice.PushedMsgService;
import com.hust.schoolmatechat.utils.StreamUtils;

import de.greenrobot.event.EventBus;

public class ChatMsgService extends Service {
	private static final String TAG = "ChatMsgService";

	private ChatManager chatManager;
	private Map<String, Chat> chatMap;
	private Map<String, MultiUserChat> groupChatMap;
	private XMPPConnection connection;
	private TigaseConnectionListener tigaseConnectionListener;
	private Roster roster;
	private TigaseRosterListener tigaseRosterListener;
	private MyChatManagerListener myChatManagerListener;
	// �Ժ��Ϊlist�����Ⱥ���Ҷ��������
	// private MultiChatMessageListener multiChatMessageListener;
	// private MultiUserChat groupChat;
	private PubSubManager pubSubManager;

	// ping ��������
	private TigasePingFailedListener pingFailedListener;
	private PingManager tigasePingManager;

	int connectCount = 0;
	private ContactsEntity userSelfContactsEntity;
	private boolean hasLogined;// �Ƿ��Ѿ���¼���������
	private boolean tigaseServiceContinue;// ά�����ӵ��߳��Ƿ����
	private boolean hasAddConnectionPacketAndFilterListener;// �����Ƿ�����˰������������ڽ��պ�����ӵ���Ϣ
	private boolean hasAddToClassChatRoom;// �Ƿ��Ѿ������˰༶Ⱥ����
	TimeBroadcastReceiver receiverTime;
	WakeupBroadcastReceiver receiverW;

	private int chatServerLoginState;// ��¼״̬

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	class WakeupBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_USER_PRESENT)) {
				CYLog.i(TAG,
						"====WakeupBroadcastReceiver"
								+ System.currentTimeMillis());
				restartService();

			}
		}
	}

	class TimeBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_TIME_TICK)) {
				CYLog.i(TAG,
						"++++ACTION_TIME_TICK" + System.currentTimeMillis());
				restartService();
			}
		}
	}

	public void restartService() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		StreamUtils.checkBaseInfo(prefs);
		String auto = prefs.getString("AUTO", "no");
		if (auto != null && auto.equals("auto")) {// �Զ���¼����������
			if (!AppEngine.getInstance(getBaseContext()).isPushServiceWorked()) {
				Intent PushService = new Intent(ChatMsgService.this,
						PushedMsgService.class);
				CYLog.i(TAG, "PushService restart");
				startService(PushService);
			}
			if (!AppEngine.getInstance(getBaseContext()).isDataCenterWorked()) {
				Intent dataCenterManagerIntent = new Intent(
						ChatMsgService.this, DataCenterManagerService.class);
				CYLog.i(TAG, "dataCenterManagerService  restart");

				startService(dataCenterManagerIntent);
			}
			if (!AppEngine.getInstance(getBaseContext())
					.isChatMsgServiceWorked()) {
				Intent chatIntent = new Intent(ChatMsgService.this,
						ChatMsgService.class);
				CYLog.i(TAG, "ChatMsgService  restart");

				startService(chatIntent);
			}
		}
	}

	private ChatMsgPacketListener chatMsgPacketListener;

	public void initChatMsgService() {
		hasLogined = false;
		tigaseServiceContinue = false;
		hasAddConnectionPacketAndFilterListener = false;
	}

	@Override
	public void onCreate() {
		CYLog.i(TAG, "ChatMsgService created");

		// final Intent dataCenterManagerIntent = new Intent(this,
		// DataCenterManagerService.class);
		// bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
		// Context.BIND_ABOVE_CLIENT);
		EventBus.getDefault().register(this);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		StreamUtils.checkBaseInfo(prefs);
		initChatMsgService();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		CYLog.i(TAG, "ChatMsgService started");

		receiverTime = new TimeBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		registerReceiver(receiverTime, intentFilter);
		receiverW = new WakeupBroadcastReceiver();
		IntentFilter intentFilter1 = new IntentFilter(
				Intent.ACTION_USER_PRESENT);
		registerReceiver(receiverW, intentFilter1);

		// ��½״̬Ϊ�Զ���½������������ӶϿ�����½״̬�ǵ�¼�ɹ������µ�¼���������
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String auto = prefs.getString("AUTO", "no");
		if (auto != null && auto.equals("auto") && !tigaseServiceContinue
				&& chatServerLoginState != APPConstant.LOGIN_TIGASE_SUCC) {
			EventbusCMD mEventbusCMD = new EventbusCMD();
			mEventbusCMD.setCMD(EventbusCMD.LOGIN_TIGASE);
			EventBus.getDefault().post(mEventbusCMD);// �����ݶ���ĵ���ֱ����intָ����棬0Ϊ��½ָ��
		}

		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	/**
	 * ��¼���������
	 */
	public boolean loginOnTigase(String accountNum, String passwd) {
		if (!this.TryConnectToTigase()) {
			CYLog.e(TAG, "TryConnectToTigase failed");
			setChatServerLoginState(APPConstant.LOGIN_DISCONNECTED);
			return false;
		}

		if (!this.TryLoginOnTigase(accountNum, passwd)) {
			CYLog.e(TAG, "TryLoginOnTigase failed");
			setChatServerLoginState(APPConstant.LOGIN_FAILED);
			return false;
		}

		// ����ά��tigase���ӵ��߳�
		this.setTigaseServiceContinue(true);
		TigaseConnectionMaintenanceThread TigaseConnectionMaintenanceThread = new TigaseConnectionMaintenanceThread(
				this);
		TigaseConnectionMaintenanceThread.start();
		setChatServerLoginState(APPConstant.LOGIN_TIGASE_SUCC);
		// this.setAccountNum(accountNum);
		// this.setPassword(passwd);
		return true;
	}

	/**
	 * Message Message���ڱ�ʾһ����Ϣ��(�����õ��Թ��߿������Ͱ��ͽ��հ��ľ�������)���������¶������͡�
	 * Message.Type.NORMAL -- ��Ĭ�ϣ��ı���Ϣ�������ʼ��� Message.Type.CHAT --
	 * ���͵Ķ���Ϣ����QQ�����һ��һ����ʾ����Ϣ Message.Type.GROUP_CHAT -- Ⱥ����Ϣ
	 * Message.Type.HEADLINE -- ������ʾ����Ϣ Message.TYPE.ERROR -- �������Ϣ
	 * Message�������ڲ��ࣺ Message.Body -- ��ʾ��Ϣ�� Message.Type -- ��ʾ��Ϣ����
	 * 
	 * @param accountNum
	 * @param passwd
	 * @return
	 */
	public boolean TryLoginOnTigase(String accountNum, String passwd) {
		CYLog.i(TAG, "TryLoginOnTigase");
		if (!this.isHasLogined()) {
			try {
				if (connection == null) {
					if (!this.TryConnectToTigase()) {
						CYLog.e(TAG, "TryConnectToTigase failed");
						setChatServerLoginState(APPConstant.LOGIN_DISCONNECTED);
						return false;
					}
				}
				connection.login(accountNum, passwd);

				// ��½�ɹ����ȡ������Ϣ
				try {
					// ע��������Ϣ������
					chatManager = ChatManager.getInstanceFor(connection);
					if (chatManager == null) {
						CYLog.e(TAG, "chatManager is null");

						// ���ӳɹ����½��ɵ�ʧ�ܣ���Ҫ��������Ϣ�������������
						if (connection != null) {
							this.sendOfflinePresence();
						}
						return false;
					}

					myChatManagerListener = new MyChatManagerListener();
					chatManager.addChatListener(myChatManagerListener);
				} catch (Exception e) {
					e.printStackTrace();
					CYLog.e(TAG, e.toString());

					// ���ӳɹ����½��ɵ�ʧ�ܣ���Ҫ��������Ϣ�������������
					if (connection != null) {
						this.sendOfflinePresence();
					}
					return false;
				}

				// �����������˵��������ݰ���Ч��̫�ͣ�������
				if (!hasAddConnectionPacketAndFilterListener) {
					hasAddConnectionPacketAndFilterListener = addConnectionPacketAndFilterListener(connection);
				}
			} catch (Exception e) {
				e.printStackTrace();
				CYLog.e(TAG, e.toString());

				// ���ӳɹ����½��ɵ�ʧ�ܣ���Ҫ��������Ϣ�������������
				if (connection != null) {
					this.sendOfflinePresence();
				}
				return false;
			}
			if (connection == null) {
				CYLog.e(TAG, "TryLoginOnTigase connection is null");
				return false;
			}
			Roster roster = connection.getRoster();

			if (roster == null) {
				CYLog.e(TAG, "roster is null");

				// ���ӳɹ����½��ɵ�ʧ�ܣ���Ҫ��������Ϣ�������������
				if (connection != null) {
					this.sendOfflinePresence();
				}
				return false;
			}

			// ���ú�֪ᷢͨUI
			this.setRoster(roster);

			/*
			 * ����û�����ʹ��һ����������(�൱��QQ�Ӻ���)���Զ���Ŀ���û�������ʹ��ö������Roster.
			 * SubscriptionMode��ֵ������Щ���� accept_all: �������ж�������
			 * reject_all���ܾ����ж������� manual�� �ֹ�����������
			 */
			// roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
			roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
			tigaseRosterListener = new TigaseRosterListener(this);
			roster.addRosterListener(tigaseRosterListener);

			// ���ִ���������Ϣ�ķ�ʽ��һ��������ͳһʹ����Ϣ��������������MyChatManagerListener��ʽ�����뱣��
			// // ��ӵ��ĵ���Ϣ�����������Խ��յ�������Ϣ
			// PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
			// connection.addPacketListener(new PacketListener() {
			// public void processPacket(Packet packet) {
			// try {
			// Message message = (Message) packet;
			// if (message.getBody() != null) {
			// String fromName = StringUtils
			// .parseBareAddress(message.getFrom());
			// CYLog.d(TAG,
			// "XMPPClient" + "Got text ["
			// + message.getBody() + "] from ["
			// + fromName + "]");
			// resolveChatMessage(message);
			// }
			// } catch (Exception e) {
			// e.printStackTrace();
			// CYLog.d(TAG, "single chat msg : " + e.toString());
			// }
			// }
			//
			// }, filter);

			// connection������ listener��Ҫ����Ϊ��Ա��disconnectҪ�Ƴ�
			// ���ִ���������Ϣ�ķ�ʽ��һ��������ͳһʹ����Ϣ��������������MyChatManagerListener��ʽ�����뱣��
			// // ���Ⱥ�ĵ���Ϣ�����������Խ��յ�������Ϣ
			// PacketFilter gfilter = new
			// MessageTypeFilter(Message.Type.groupchat);
			// connection.addPacketListener(new PacketListener() {
			// public void processPacket(Packet packet) {
			// try {
			// Message message = (Message) packet;
			// if (message.getBody() != null) {
			// String fromName = StringUtils
			// .parseBareAddress(message.getFrom());
			// String groupChatRoomName = fromName.substring(0,
			// fromName.indexOf("@muc"));
			// CYLog.d(TAG,
			// "XMPPClient" + "Got text ["
			// + message.getBody() + "] from ["
			// + fromName + "]" + "[room name : "
			// + groupChatRoomName + "]");
			// // Ⱥ����Ϣ���
			// if (dataCenterManagerService != null) {
			// dataCenterManagerService.pushDataToGroupChat(
			// groupChatRoomName, message);
			// } else {
			// CYLog.e(TAG,
			// "processPacket dataCenterManagerService is null");
			// }
			// }
			// } catch (Exception e) {
			// e.printStackTrace();
			// CYLog.d(TAG, "group room listener : " + e.toString());
			// }
			// }
			//
			// }, gfilter);

			// ע�����������������
			// groupchatInvitationHandler =
			// new GroupChatInvitationHandler(dataCenterManagerService);
			// ����Ⱥ����������������
			// try {
			// MultiUserChat.addInvitationListener(this.connection,
			// new InvitationListener() {
			// @Override
			// public void invitationReceived(XMPPConnection conn,
			// String room, String inviter, String reason,
			// String password, Message msg) {
			// CYLog.i(TAG, "�������������������!");
			//
			// android.os.Message msg1 = dataCenterManagerService
			// .getGroupChatInvitationHandler()
			// .obtainMessage();
			// // �����������˺Ų�ѯ����ʵ����
			// ContactsEntity inviterEntity = dataCenterManagerService
			// .getFriendInfoByAccount(inviter
			// .substring(0, inviter
			// .lastIndexOf("@")));
			// String[] inviter_room = new String[] {
			// inviterEntity.getName(),
			// room.substring(0, room.lastIndexOf("@")) };
			// msg1.what = 0;
			// msg1.obj = inviter_room;
			// dataCenterManagerService
			// .getGroupChatInvitationHandler()
			// .sendMessage(msg1);
			// }
			//
			// });
			// } catch (Exception e) {
			// e.printStackTrace();
			// CYLog.e(TAG, e.toString());
			//
			// // ���ӳɹ����½��ɵ�ʧ�ܣ���Ҫ��������Ϣ�������������
			// if (connection != null) {
			// this.sendOfflinePresence();
			// }
			// return false;
			// }

			hasAddToClassChatRoom = true;
			// // ����������
			// if (!hasAddToClassChatRoom) {
			// hasAddToClassChatRoom = addSelfToClassChatRoom();
			// }
			if (connection != null) {
				String pubSubAddress = "pubsub." + connection.getServiceName();
				pubSubManager = new PubSubManager(connection, pubSubAddress);
			} else {
				return false;
			}
			// ��ȡ�����ĵ�Ⱥ�飬�����Ϣ��������+++lqg+++,��ǰͳһͨ��������Ϣ�ļ���������
			// Map<LeafNode, GroupNodeSubItemEventListener> leafNodeListenerMap
			// = new
			// HashMap<LeafNode, GroupNodeSubItemEventListener>();
			// GroupNodeSubItemEventListener groupNodeSubItemEventListener =
			// new GroupNodeSubItemEventListener();
			// roomNode.addItemEventListener(groupNodeSubItemEventListener);
			// //����map����������
			// leafNodeListenerMap.put(roomNode, groupNodeSubItemEventListener);

			String sign = null;
			try {
				// ����
				sign = this.getUserSelfContactsEntity().getSign();

				if (sign == null || sign.equals("")) {
					sign = "online";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (sendOnlinePresence(sign)) {
				// �ɹ���½���������������Ƿ���������֪ͨ��Ϣδ���ͳ�ȥ
				checkNotifyMsgDirectory();
				// �������������������EventBus��Ϣ
				TigaseConnectionStatusEntity returnStatus = new TigaseConnectionStatusEntity();
				returnStatus
						.setConnStatus(TigaseConnectionStatusEntity.TIGASE_CONNECTED);
				EventBus.getDefault().post(returnStatus);
			}
		} else {
			CYLog.d(TAG, "has logined");
			return false;
		}
		this.setHasLogined(true);
		return true;
	}

	/**
	 * ����Ƿ���������֪ͨ��Ϣδ���ͳ�ȥ�����ͣ���ɾ�����л��ļ�
	 */
	private void checkNotifyMsgDirectory() {
		try {
			String dir = SearchSuggestionProvider.pathStr + File.separator
					+ "notifymsg";
			File notifyFilePath = new File(dir);
			if (!notifyFilePath.exists()) {
				return;// û��Ŀ¼��ֱ�ӷ���
			}

			File[] files = notifyFilePath.listFiles();
			for (File file : files) {
				if (!file.isDirectory()) {
					try {
						FileInputStream fs = new FileInputStream(file);
						ObjectInputStream os = new ObjectInputStream(fs);
						ChatMessageSendEntity mSendChatMessage = (ChatMessageSendEntity) os
								.readObject();
						os.close();
						file.delete();// ɾ���ļ�
						this.sendNotifyMessage(mSendChatMessage);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					file.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "checkNotifyMsgDirectory failed : " + e);
		}
	}

	// // ��ʱ�жϣ�Ⱥ����Ϣ���û���¼���һ�����ڵ�Ⱥ����Ϣ���Ȼ���������֮����������
	// private Timer timer;
	// final Handler handler = new Handler() {
	// public void handleMessage(android.os.Message msg) {
	// switch (msg.what) {
	// case 1:
	// if (multiChatMessageListener != null) {
	// multiChatMessageListener.setSendStart(true);
	// List<GroupChatMessage> groupChatMessageList = multiChatMessageListener
	// .getGroupChatMessageList();
	// CYLog.i(TAG, "#### multi chat msg size : "
	// + groupChatMessageList.size());
	// if (groupChatMessageList.size() != 0) {
	// GroupChatMessageList groupChatMessageList2 = new GroupChatMessageList();
	// groupChatMessageList2
	// .setGroupChatMessageList(groupChatMessageList);
	// EventBus.getDefault().post(groupChatMessageList2);
	// // û������֮ǰ���������
	// // groupChatMessageList.clear();
	// // multiChatMessageListener
	// // .setGroupChatMessageList(groupChatMessageList);
	// }
	// timer.cancel(); // �˳���ʱ��
	// }
	// break;
	// }
	// super.handleMessage(msg);
	// }
	// };
	//
	// TimerTask task = new TimerTask() {
	// public void run() {
	// android.os.Message message = new android.os.Message();
	// message.what = 1;
	// handler.sendMessage(message);
	// }
	// };

	// public boolean addSelfToClassChatRoom() {
	// // �༶������
	// try {
	// GroupChatRoom groupChatRoom = new GroupChatRoom();
	// String groupName = this.getUserSelfContactsEntity().getGroupName();
	// String[] names = groupName.split(",");
	// if (names.length > 0 && names[0] != null && !names[0].equals("")) {
	// // �������߼���������
	// groupChat = GroupChat.createGroupChatRoom(connection, names[0],
	// this.getUserAccount());
	//
	// // ���Ⱥ����Ϣ������,�������������˶������յ������Ϣ
	// multiChatMessageListener = new MultiChatMessageListener(
	// names[0]);
	// groupChat.addMessageListener(multiChatMessageListener);
	//
	// if (groupChatMap == null) {
	// groupChatMap = new HashMap<String, MultiUserChat>();
	// }
	//
	// if (groupChatMap.containsKey(names[0])) {
	// groupChatMap.remove(names[0]);
	// }
	// groupChatMap.put(names[0], groupChat);
	//
	// // ������ʱ��
	// timer = new Timer(true);
	// timer.schedule(task, 10 * 1000, 10000); // ��ʱ10 *
	// // 1000ms��ִ�У�1000msִ��һ��
	// return true;
	// } else {
	// return false;
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	/**
	 * Ŀǰֻ֧��һ��Ⱥ����
	 * 
	 * @param roomName
	 * @return
	 */
	// public MultiUserChat addSelfToClassChatRoom2(String roomName) {
	// // �༶������
	// try {
	// GroupChatRoom groupChatRoom = new GroupChatRoom();
	//
	// // �������߼���������
	// groupChat = GroupChat.createGroupChatRoom(connection, roomName,
	// this.getUserAccount());
	// // ���Ⱥ����Ϣ������,�������������˶������յ������Ϣ
	// multiChatMessageListener = new MultiChatMessageListener(roomName);
	// groupChat.addMessageListener(multiChatMessageListener);
	//
	// if (groupChatMap == null) {
	// groupChatMap = new HashMap<String, MultiUserChat>();
	// }
	//
	// if (groupChatMap.containsKey(roomName)) {
	// groupChatMap.remove(roomName);
	// }
	// groupChatMap.put(roomName, groupChat);
	// return groupChat;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return null;
	// }
	// }

	public boolean addConnectionPacketAndFilterListener(
			XMPPConnection connection2) {
		try {
			AndFilter presence_sub_filter = new AndFilter(new PacketTypeFilter(
					Presence.class), new ChatMsgPacketFilter(this));

			if (connection == null) {
				CYLog.i(TAG,
						"TryLoginOnTigase addPacketListener connection null");
				return false;
			}

			chatMsgPacketListener = new ChatMsgPacketListener(this);
			connection.addPacketListener(chatMsgPacketListener,
					presence_sub_filter);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Presence ��ʾXMPP״̬��packet��ÿ��presence packet����һ��״̬����ö������Presence.Type��ֵ��ʾ��
	 * available -- ��Ĭ�ϣ��û�����״̬ unavailable -- �û�û�տ���Ϣ subscribe --
	 * �����ı��ˣ�������ӶԷ�Ϊ���� subscribed -- ͳһ�����˶��ģ�Ҳ����ȷ�ϱ��Է���Ϊ���� unsubscribe --
	 * ��ȡ�����ı��ˣ�����ɾ��ĳ���� unsubscribed -- �ܾ������˶��ģ����ܾ��Է���������� error -- ��ǰ״̬packet�д���
	 * 
	 * @param sign
	 * @return
	 */
	public boolean sendOnlinePresence(String sign) {
		try {
			Presence presence = new Presence(Presence.Type.available);
			presence.setStatus(sign);
			presence.setMode(Presence.Mode.available);

			if (connection != null && connection.isConnected()) {
				connection.sendPacket(presence);
				// CYLog.i(TAG, "sendOnlinePresence succ");
				return true;
			} else {
				CYLog.i(TAG, "sendOnlinePresence connection not connected");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return false;
		}
	}

	public void sendOfflinePresence() {
		try {
			Presence presence = new Presence(Presence.Type.unavailable);
			presence.setMode(Presence.Mode.dnd);

			if (connection != null && connection.isConnected()) {
				connection.sendPacket(presence);
			} else {
				// CYLog.e(TAG, "sendOfflinePresence connection not connected");
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
		}
	}

	/**
	 * �յ������������
	 * 
	 * @param friendAccount
	 */
	public void sendFriendAddBroadcast(String friendAccount) {
		try {
			// if (friendAccount.endsWith("@" +
			// APPConstant.TIGASE_SERVER_DOMAIN)) {
			// friendAccount = friendAccount.substring(
			// 0,
			// friendAccount.lastIndexOf("@"
			// + APPConstant.TIGASE_SERVER_DOMAIN));
			// }
			// FriendAddEntity mAddFriend = new FriendAddEntity();
			// mAddFriend.setFriendAccount(friendAccount);
			// mAddFriend.setAddType("receive");
			// EventBus.getDefault().post(mAddFriend);
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "sendFriendAddBroadcast failed");
		}
	}

	/**
	 * �������������
	 * 
	 * @return
	 */
	public boolean TryConnectToTigase() {
		CYLog.i(TAG, "TryConnectToTigase");
		CYLog.d(TAG, APPBaseInfo.TigaseIP);

		try {
			ConnectionConfiguration configuration = new ConnectionConfiguration(
					APPBaseInfo.TigaseIP, APPBaseInfo.TIGASE_SERVER_PORT,
					APPBaseInfo.TIGASE_SERVER_DOMAIN);
			configuration.setCompressionEnabled(true);// ֧������ѹ��
			configuration.setSendPresence(false);// ״̬����Ϊ���ߣ����������ڵ�¼��ʱ�����Ȼ�ȡ��������Ϣ
			configuration.setSecurityMode(SecurityMode.disabled);
			// ��Ҫ�Զ������������Լ������� ���ƣ��Զ����������¶�����app�޷��ٽ����������
			configuration.setReconnectionAllowed(false);

			connection = new XMPPTCPConnection(configuration);

			// ����ping���
			try {
				if (pingFailedListener == null) {
					pingFailedListener = new TigasePingFailedListener(this);
				}

				if (tigasePingManager == null) {
					tigasePingManager = PingManager.getInstanceFor(connection);
					CYLog.i(TAG, "---default ping interval is : "
							+ tigasePingManager.getPingInterval());
				}
				tigasePingManager
						.unregisterPingFailedListener(pingFailedListener);
				tigasePingManager
						.registerPingFailedListener(pingFailedListener);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// ���õȴ�ʱ��,Ĭ�����ã�����Ӱ��������Ϣ����
			// connection.setPacketReplyTimeout(2000);
			connection.connect();

			// Mechanisms mechanisms = connection.getFeature(Mechanisms.ELEMENT,
			// Mechanisms.NAMESPACE);
			// if (mechanisms == null) {
			// CYLog.i(TAG,"Server did not report any SASL mechanisms");
			//
			// }
			// CYLog.i(TAG,mechanisms.getMechanisms());
			//
			// CYLog.i(TAG,SASLAuthentication.getRegisterdSASLMechanisms()
			// .toString());
		} catch (Exception e2) {
			e2.printStackTrace();
			CYLog.e(TAG, "ChatUtils.connect2Tigase error" + e2);
			connection = null;
			connectCount++;
			return false;
		}

		if (connection != null && connection.isConnected()) {
			this.setHasLogined(false);
		} else {
			connection = null;
			connectCount++;
			return false;
		}

		if (connection != null) {
			// ������Ӽ�����
			tigaseConnectionListener = new TigaseConnectionListener(this);
			connection.addConnectionListener(tigaseConnectionListener);
			return true;
		} else {
			connectCount++;
			return false;
		}
	}

	// ibind ��¶��ȥ
	public synchronized boolean sendChatMessage(String accountNum,
			String message) {
		CYLog.i(TAG, "sendMessage " + message);
		try {
			boolean ret = sendChatMessageInner(accountNum, message);
			return ret;
		} catch (Exception e) {
			// ����ʧ�ܵ���Ϣ��⣬�´�reconnect��ʱ����
			// dataCenterManagerService
			e.printStackTrace();
			CYLog.i(TAG, "to " + accountNum + " failed : " + message);
			CYLog.e(TAG, e.toString());
			return false;
		}
	}

	/**
	 * ��������Ϣ
	 * 
	 * @param mSendChatMessage
	 */
	public void onEventBackgroundThread(ChatMessageSendEntity mSendChatMessage) {
		if (mSendChatMessage.getMessageType().equals("single")) {

			boolean flag = sendChatMessage(mSendChatMessage.getReceiver(),
					mSendChatMessage.getMessage());
			StatusSendEntity mSendStatus = new StatusSendEntity();
			mSendStatus.setSendStatus(flag);
			CYLog.d(TAG, "mSendStatus.setSendStatus flag=" + flag);
			EventBus.getDefault().post(mSendStatus);
		} else if (mSendChatMessage.getMessageType().equals("group")) {
			boolean flag = sendGroupChatMessage(mSendChatMessage.getReceiver(),
					mSendChatMessage.getMessage());
			StatusSendEntity mSendStatus = new StatusSendEntity();
			mSendStatus.setSendStatus(flag);
			EventBus.getDefault().post(mSendStatus);
		} else if (mSendChatMessage.getMessageType().equals("notifyall")) {
			sendNotifyMessage(mSendChatMessage);
		}

	}

	/**
	 * ����֪ͨ��Ϣ
	 * 
	 * @param mSendChatMessage
	 */
	private void sendNotifyMessage(final ChatMessageSendEntity mSendChatMessage) {
		// ֪ͨ��Ϣ��Ҫ�ɿ��Ա�֤
		new Thread() {
			@Override
			public void run() {
				int count = 0;
				while (!sendChatMessage(mSendChatMessage.getReceiver(),
						mSendChatMessage.getMessage())) {
					try {
						++count;
						if (count > 20)
							break;
						sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (count > 20) {
					// ֪ͨ��Ϣ���汾�أ��´μ�⵽������ߵ�½�ɹ����������ʱ��ȡ��Ϣ����
					String dir = SearchSuggestionProvider.pathStr
							+ File.separator + "notifymsg";
					File notifyFilePath = new File(dir);
					if (!notifyFilePath.exists()) {
						notifyFilePath.mkdir();
					}

					File notifyFile = new File(dir
							+ java.util.UUID.randomUUID());
					FileOutputStream fs;
					try {
						fs = new FileOutputStream(notifyFile);
						ObjectOutputStream os = new ObjectOutputStream(fs);
						os.writeObject(mSendChatMessage);
						os.close();
					} catch (Exception e) {
						e.printStackTrace();
						CYLog.e(TAG, "notify msg save failed to : "
								+ mSendChatMessage.getReceiver() + " msg : "
								+ mSendChatMessage.getMessage());
					}
				}
			}
		}.start();
	}

	public void onEventBackgroundThread(FriendAddSendEntity friendAddEntity) {
		String toAccountNum = friendAddEntity.getAccountNum();
		String cmdType = friendAddEntity.getCmdType();

		boolean flag = true;
		String name = friendAddEntity.getName();

		// ���ͼӺ�������
		if (cmdType.equals(APPConstant.CMD_PREFIX_FRIEND_ADD_REQUEST)) {
			if (!isMyFriend(toAccountNum)) {
				StringBuffer cmdmsg = new StringBuffer();
				cmdmsg.append(cmdType).append(this.getUserAccount())
						.append("_").append(friendAddEntity.getName())
						.append("_").append(friendAddEntity.getClassName());
				flag = this.sendChatMessage(toAccountNum, cmdmsg.toString());
			}
		}

		// ��������
		if (cmdType.equals(APPConstant.CMD_PREFIX_FRIEND_ADD_AGREE)) {
			if (!isMyFriend(toAccountNum)) {
				StringBuffer cmdmsg = new StringBuffer();
				cmdmsg.append(cmdType).append(this.getUserAccount())
						.append("_").append(friendAddEntity.getName())
						.append("_").append(friendAddEntity.getClassName());
				flag = this.sendChatMessage(toAccountNum, cmdmsg.toString());
			}

			if (flag) {
				flag = addEntry(name, toAccountNum, "�ҵĺ���");
			}
		}

		// ɾ������
		if (cmdType.equals(APPConstant.CMD_PREFIX_FRIEND_ADD_DECLINE)) {
			StringBuffer jid = new StringBuffer();
			jid.append(toAccountNum).append("@")
					.append(APPBaseInfo.TIGASE_SERVER_DOMAIN);
			flag = removeContactsEntity(jid.toString());
		}

		StatusSendEntity mSendStatus = new StatusSendEntity();
		mSendStatus.setSendStatus(flag);
		EventBus.getDefault().post(mSendStatus);
	}

	/**
	 * Ⱥ��Ĵ�����ɾ�������롢���ˡ���ѯ
	 * 
	 * @param room
	 */
	public void onEventBackgroundThread(GroupChatRoomEntity groupChatRoomEntity) {
		try {
			LeafNode roomNode = null;
			String groupNodeId = groupChatRoomEntity.getGroupId();
			GroupChatFuncStatusEntity returnStatus = new GroupChatFuncStatusEntity();
			// ���۳ɹ�ʧ�ܶ�����Ⱥ��
			returnStatus.setGroupChatRoomEntity(groupChatRoomEntity);

			// ����Ⱥ��
			if (groupChatRoomEntity.getFunctionType() == GroupChatRoomEntity.FUNCTYPE_ADD) {
				try {
					// �Խڵ��������
					ConfigureForm form = new ConfigureForm(FormType.submit);
					form.setNodeType(NodeType.leaf);
					form.setAccessModel(AccessModel.open);
					form.setPublishModel(PublishModel.open);
					form.setPersistentItems(true);
					form.setNotifyRetract(true);
					form.setMaxItems(65535);

					roomNode = pubSubManager.createNode(groupNodeId);

					roomNode.sendConfigurationForm(form);
					CYLog.i(TAG, "createNode is called!");
				} catch (Exception e) {
					CYLog.i(TAG, "createNode is in exception!");
					e.printStackTrace();
					// ������֮ǰ�Ѿ��������ýڵ㣬�ڵ����û��˺��ǰ󶨵ģ�ֻ�и��û����õ�
					try {
						roomNode = pubSubManager.getNode(groupNodeId);
					} catch (Exception e2) {
						CYLog.i(TAG, "createNode is in exception2!");
						e2.printStackTrace();
					}
				}

				// ���ܷ�������������ڵ�
				if (roomNode == null) {
					try {
						roomNode = pubSubManager.getNode(groupNodeId);
						// �ڵ��л��ж�����
						if (roomNode.getSubscriptions().size() > 1) {
							roomNode = null;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				boolean ret = false;
				if (roomNode != null) {
					// ����Ⱥ�����ڵĽڵ�����
					ret = subscribeGroupNode(roomNode);
				}

				if (ret) {
					// ������������Ҳ�������ɵ���Ϣ
					returnStatus
							.setFuncStatus(GroupChatFuncStatusEntity.GROUPCHAT_ADDROOM_SUCCESS);
					EventBus.getDefault().post(returnStatus);
				} else {
					// ������������Ҳ���ʧ�ܵ���Ϣ
					returnStatus
							.setFuncStatus(GroupChatFuncStatusEntity.GROUPCHAT_ADDROOM_FAIL);
					EventBus.getDefault().post(returnStatus);
				}
			} else if (groupChatRoomEntity.getFunctionType() == GroupChatRoomEntity.FUNCTYPE_DEL) {

				// ��ȡ�����ģ�Ⱥ������Ȩ�ޣ�ɾ���ڵ�
				try {
					// ��ȡ������
					roomNode = pubSubManager.getNode(groupNodeId);
					if (roomNode != null) {
						roomNode.unsubscribe(groupNodeId);
					}

					Integer flag = groupChatRoomEntity.getOccupantsMap().get(
							this.getUserAccount());
					// ����ģʽ���κ��˶�����ɾ��Ⱥ��
					if (APPConstant.DEBUG_MODE) {
						flag = 1;
					}

					if (flag == 1) {
						pubSubManager.deleteNode(groupNodeId);
					}

					returnStatus
							.setFuncStatus(GroupChatFuncStatusEntity.GROUPCHAT_DEL_SUCCESS);
					EventBus.getDefault().post(returnStatus);
				} catch (Exception e) {
					e.printStackTrace();
					returnStatus
							.setFuncStatus(GroupChatFuncStatusEntity.GROUPCHAT_DEL_FAIL);
					EventBus.getDefault().post(returnStatus);
				}
			} else if (groupChatRoomEntity.getFunctionType() == GroupChatRoomEntity.FUNCTION_UBSUBSSRIBE) {
				// ȡ������
				try {
					// ��ȡ������
					roomNode = pubSubManager.getNode(groupNodeId);
					if (roomNode != null) {
						StringBuffer buf = new StringBuffer();
						buf.append(this.getUserAccount()).append("@")
								.append(APPBaseInfo.TIGASE_SERVER_DOMAIN);
						// connection.getUser();//���������ԴSmack
						roomNode.unsubscribe(buf.toString());

						// Ⱥ���Ѿ�û���˶�����,ɾ���ڵ�
						List<Subscription> subscriptionerList = roomNode
								.getSubscriptions();
						if (subscriptionerList != null
								&& subscriptionerList.size() <= 1) {
							pubSubManager.deleteNode(groupNodeId);

						}
					}

					returnStatus
							.setFuncStatus(GroupChatFuncStatusEntity.GROUPCHAT_UBSUBSSRIBE_SUCCESS);
					EventBus.getDefault().post(returnStatus);
				} catch (Exception e) {
					CYLog.e(TAG, e.getMessage());
					returnStatus
							.setFuncStatus(GroupChatFuncStatusEntity.GROUPCHAT_UBSUBSSRIBE_FAIL);
					EventBus.getDefault().post(returnStatus);
				}
			} else if (groupChatRoomEntity.getFunctionType() == GroupChatRoomEntity.FUNCTYPE_JOIN) {
				// ����������
				CYLog.i(TAG, getUserAccount() + "����������! " + groupNodeId);
				boolean ret = false;
				try {
					roomNode = pubSubManager.getNode(groupNodeId);
				} catch (Exception e) {
					CYLog.i(TAG, "getNode is in exception!");
					e.printStackTrace();
				}

				if (roomNode != null) {
					ret = subscribeGroupNode(roomNode);
					CYLog.i(TAG, "tigase�������ϴ��������Ľڵ�!");
				} else {
					CYLog.i(TAG, "tigase�������ϲ����������Ľڵ�!");
				}

				if (ret) {
					returnStatus
							.setFuncStatus(GroupChatFuncStatusEntity.GROUPCHAT_JOINROOM_SUCCESS);
					EventBus.getDefault().post(returnStatus);
				} else {
					returnStatus
							.setFuncStatus(GroupChatFuncStatusEntity.GROUPCHAT_JOINROOM_FAIL);
					EventBus.getDefault().post(returnStatus);
				}
			} /*
			 * else if (groupChatRoomEntity.getFunctionType() ==
			 * GroupChatRoomEntity.FUNCTION_KICK) {
			 * 
			 * } else if (groupChatRoomEntity.getFunctionType() ==
			 * GroupChatRoomEntity.FUNCTION_CONSULT) {
			 * 
			 * }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// //����Ⱥ�����������Ϣ�������֣�������Ϣ������������Ϣ���ܾ�������Ϣ
	// public void sendGroupChatInviteMessage(String accountNum, String message)
	// {
	// try {
	// String receiver = null;
	// if (accountNum.endsWith("@" + APPConstant.TIGASE_SERVER_DOMAIN)) {
	// receiver = accountNum.substring(0,
	// accountNum.lastIndexOf("@" + APPConstant.TIGASE_SERVER_DOMAIN));
	// } else {
	// receiver = accountNum;
	// }
	//
	// ChatMessageSendEntity mSendChatMessage = new ChatMessageSendEntity();
	// mSendChatMessage.setReceiver(receiver);
	// mSendChatMessage.setMessage(message);
	// mSendChatMessage.setMessageType("notifyall");// ��Ϣ���ͱ��
	// EventBus.getDefault().post(mSendChatMessage);// ����Ϣ���ȴ�����
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * ����ĳ�˼���ĳȺ
	 * 
	 * @param friendAccount
	 * @param userAccount
	 * @param groupNodeId
	 */
	public void inviteFriendJoinGroup(String friendAccount, String userAccount,
			String groupNodeId) {
		try {
			// �����Լ��յ���������Ϣ
			if (friendAccount.equals(userAccount)) {
				return;
			}
			StringBuffer message = new StringBuffer();
			message.append(APPConstant.CMD_PREFIX_GROUPCHAT_INVITE)
					.append(userAccount).append("/").append(groupNodeId);
			this.sendChatMessage(friendAccount, message.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����Ⱥ�����ڵĽڵ� roomNode
	 * 
	 * @param roomNode
	 */
	private boolean subscribeGroupNode(LeafNode roomNode) {
		// �Լ�����Ⱥ��
		try {
			SubscribeForm subscriptionForm = new SubscribeForm(FormType.submit);
			subscriptionForm.setDeliverOn(true);
			subscriptionForm.setDigestFrequency(5000);
			subscriptionForm.setDigestOn(true);
			subscriptionForm.setIncludeBody(true);

			// jid����
			roomNode.subscribe(connection.getUser(), subscriptionForm);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean sendChatMessageInner(String accountNum, String message) {
		CYLog.i(TAG, "sendMessage");
		try {
			StringBuffer buf = new StringBuffer();
			buf.append(accountNum);
			if (!accountNum.endsWith("@" + APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
				buf.append("@").append(APPBaseInfo.TIGASE_SERVER_DOMAIN);
			}

			Chat chat = null;
			if (chatMap == null) {
				chatMap = new HashMap<String, Chat>();
				this.setChatMap(chatMap);
			}
			if (chatMap.containsKey(buf.toString())) {
				chat = chatMap.get(buf.toString());
				if (chat == null) {
					chatMap.remove(buf.toString());
					chat = chatManager.createChat(buf.toString(), null);
					chatMap.put(buf.toString(), chat);
				}
			} else {
				chat = chatManager.createChat(buf.toString(), null);
				chatMap.put(buf.toString(), chat);
			}
			chat.sendMessage(message);
			// chat.close();
			CYLog.i(TAG, "msg to " + accountNum + " : " + message.toString());
			return true;
		} catch (Exception e) {
			// ����ʧ�ܵ���Ϣ��⣬�´�reconnect��ʱ����
			// dataCenterManagerService
			e.printStackTrace();
			CYLog.i(TAG, "to " + accountNum + " failed : " + message);
			CYLog.e(TAG, e.toString());
			return false;
		}
	}

	@Override
	public void onDestroy() {
		CYLog.i(TAG, "onDestroy");
		// clearLastConnection();
		if (receiverTime != null)
			unregisterReceiver(receiverTime);
		if (receiverW != null)
			unregisterReceiver(receiverW);

		this.setTigaseServiceContinue(false);// �߳��˳�
		// add ȡ���� ibind
		// unbindService(dataCenterManagerIntentConn);
		EventBus.getDefault().unregister(this);
		super.onDestroy();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String auto = prefs.getString("AUTO", "no");
		if (auto == null || auto.equals("auto")) {// ������û������˳��Ļ��Ͳ���Ҫ����������
			CYLog.i(TAG, "unnormal onDestroy,restart");
			restartService();
		}
	}

	// /**
	// * �������� �����ã��������޷��Զ�����
	// */
	// public void restartChatMsgService() {
	// try {
	// // �߳��˳�
	// this.setTigaseServiceContinue(false);
	// // �������
	// clearLastConnection();
	// // ���µ�¼
	// loginOnTigase(dataCenterManagerService.getUserSelfContactsEntity().getUserAccount(),
	// dataCenterManagerService.getUserSelfContactsEntity().getPassword());
	// } catch (Exception e) {
	// e.printStackTrace();
	// CYLog.e(TAG, "restartChatMsgService " + e.toString());
	// }
	// }

	/** �����һ������������������� */
	public void clearLastConnection() {
		CYLog.i(TAG, "clearLastConnection");
		// ����
		sendOfflinePresence();

		if (tigaseRosterListener != null) {
			tigaseRosterListener = null;
		}

		// ��myChatManagerListener֮ǰ�����Ӱ��������Ϣ����
		if (chatManager != null) {
			if (myChatManagerListener != null) {
				chatManager.removeChatListener(myChatManagerListener);
			}
			chatManager = null;
		}

		if (myChatManagerListener != null) {
			myChatManagerListener.clearAll();
			myChatManagerListener = null;
		}

		// Ⱥ��
		if (groupChatMap != null) {
			groupChatMap.clear();
			groupChatMap = null;
		}

		// ��multiChatMessageListener֮ǰ���
		// if (groupChat != null) {
		// if (multiChatMessageListener != null) {
		// groupChat.removeMessageListener(multiChatMessageListener);
		// }
		// groupChat = null;
		// }
		//
		// if (multiChatMessageListener != null) {
		// multiChatMessageListener = null;
		// }
		if (pubSubManager != null) {
			pubSubManager = null;
		}

		hasLogined = false;

		// ���chat map
		if (chatMap != null) {
			for (String key : chatMap.keySet()) {
				chatMap.get(key).close();
			}
			chatMap.clear();
			chatMap = null;
		}

		this.setHasAddConnectionPacketAndFilterListener(false);

		// �����е�listener���֮���ͷ�����
		try {
			if (connection != null) {
				connection.removePacketListener(this.chatMsgPacketListener);
				chatMsgPacketListener = null;
				connection.removeConnectionListener(tigaseConnectionListener);
				tigaseConnectionListener = null;
				if (connection.isConnected()) {
					connection.disconnect();
				}
				connection = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			connection = null;
		}
	}

	class MyChatManagerListener implements ChatManagerListener {
		Map<Chat, MyMessageListener> myMessageListenerChatMap;

		@Override
		public void chatCreated(Chat chat, boolean arg1) {
			MyMessageListener myMessageListener = new MyMessageListener();
			chat.addMessageListener(myMessageListener);
			if (myMessageListenerChatMap == null) {
				myMessageListenerChatMap = new HashMap<Chat, MyMessageListener>();
			}
			if (!myMessageListenerChatMap.containsKey(chat)) {
				myMessageListenerChatMap.put(chat, myMessageListener);
			}
		}

		public void clearAll() {
			if (myMessageListenerChatMap == null) {
				return;
			}

			// ���listenter������,Ӱ��������Ϣ
			for (Chat key : myMessageListenerChatMap.keySet()) {
				key.removeMessageListener(myMessageListenerChatMap.get(key));
			}
			myMessageListenerChatMap.clear();
			myMessageListenerChatMap = null;
		}
	}

	class MyMessageListener implements MessageListener {
		@Override
		public void processMessage(Chat arg0, Message msg) {
			resolveChatMessage(msg);
		}
	}

	/***
	 * �������յ��ĵ�����Ϣ�����洢
	 * 
	 * @param msg
	 */
	public void resolveChatMessage(Message msg) {
		try {
			// �������������Ϣ�������
			CYLog.i(TAG, "MyMessageListener msg's from=" + msg.getFrom());
			CYLog.i(TAG, "MyMessageListener msg's to=" + msg.getTo());
			CYLog.i(TAG, "MyMessageListener msg =" + msg.getBody());
			CYLog.i(TAG, "MyMessageListener msg =" + msg.toXML());
			if (msg.getFrom().endsWith(
					"pubsub." + APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
				Map<String, String> actualMsgMap = resoloveGroupXMLMsg(msg
						.toXML().toString());
				String groupId = null;
				String groupMsg = null;
				for (String key : actualMsgMap.keySet()) {
					groupId = key;
					groupMsg = actualMsgMap.get(key);
					break;
				}

				if (groupMsg != null) {
					// �������Լ����ͳ�ȥ����Ϣ
					if (groupMsg.startsWith(this.getUserAccount())) {
						return;
					}
					// ˵����Ⱥ��������Ϣ
					GroupChatMessage mGroupChatMessage = new GroupChatMessage();
					mGroupChatMessage.setGroupId(groupId);
					mGroupChatMessage.setMessage(groupMsg);
					EventBus.getDefault().post(mGroupChatMessage);
				}
				return;
			}

			// �������Լ����ͳ�ȥ����Ϣ
			if (msg.getFrom().startsWith(this.getUserAccount())) {
				return;
			}

			ChatMessage chatMessage = handleSmackMessage2ChatMessage(msg);
			CYLog.i(TAG, "msg from " + chatMessage.getOwner() + " : "
					+ chatMessage.getMessageContent() + " ---- time : "
					+ chatMessage.getTime());

			EventBus.getDefault().post(msg);

		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "resolveChatMessage " + e.toString());
		}
	}

	// Ⱥ����Ϣģ��
	// <message id='181' to='230@hust' from='pubsub.hust'>
	// <thread>
	// d067fdb3-59d7-4012-ab60-c5081ddc02a0
	// </thread>
	// <event xmlns='http://jabber.org/protocol/pubsub#event'>
	// <items node='������'>
	// <item id='240/���½�/�ź���'/>
	// </items>
	// </event>
	// </message>
	/**
	 * ����Ⱥ��xml��Ϣ,����groupId��msg����
	 * 
	 * @param xmlStr
	 * @return
	 */
	private Map<String, String> resoloveGroupXMLMsg(String xmlStr) {
		try {
			String groupId = null;
			String friendAccount = null;
			String msg = null;
			String name = null;
			boolean flag = true;
			Map<String, String> id_msg = new HashMap<String, String>();

			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(new StringReader(xmlStr));
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				switch (event) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if ("message".equals(parser.getName())) {
						// int count = parser.getAttributeCount();
						// for (int i = 0; i < count; i++) {
						// String key = parser.getAttributeName(i);
						// if ("from".equals(key)) {
						// isNeedUpdate=
						// "true".equals(parser.getAttributeValue(i));
						// }
						// }
					} else if ("items".equals(parser.getName())) {
						int count = parser.getAttributeCount();
						for (int i = 0; i < count; i++) {
							String key = parser.getAttributeName(i);
							if ("node".equals(key)) {
								groupId = parser.getAttributeValue(i);
							}
						}
					} else if ("item".equals(parser.getName())) {
						int count = parser.getAttributeCount();
						for (int i = 0; i < count; i++) {
							String key = parser.getAttributeName(i);
							if ("id".equals(key)) {
								msg = parser.getAttributeValue(i);
							}
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				event = parser.next();
			}

			id_msg.put(groupId, msg);
			return id_msg;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ChatMessage handleSmackMessage2ChatMessage(Message msg) {
		ChatMessage chatMessage = new ChatMessage();
		String owner = msg.getFrom().substring(0, msg.getFrom().indexOf("/"));
		chatMessage.setOwner(owner);
		chatMessage.setUserAccount(this.getUserAccount());
		chatMessage.setSenderAccount(owner);
		chatMessage.setRecvAccount(this.getUserAccount());
		chatMessage.setMessageContent(msg.getBody());
		chatMessage.setMid(UUID.randomUUID().toString());
		chatMessage.setType(ChatMessage.PRIVATECHATMESSAGE);
		chatMessage.setTime(new Date(System.currentTimeMillis()));

		return chatMessage;

	}

	public void onEventBackgroundThread(EventbusCMD mEventbusCMD) {
		int cmd = mEventbusCMD.getCMD();
		if (cmd == EventbusCMD.LOGIN_TIGASE) {
			if (userSelfContactsEntity == null) {
				ClassmateDao classmateDao = new ClassmateDao(this);
				String accountNum = getUserAccount();
				// �Ȳ鿴�����Ƿ��и�����Ϣ������о�ֱ�Ӵӱ���ȡ��������Ϣ���
				if (classmateDao.isSelfContactsEntityExisted(accountNum)) {
					userSelfContactsEntity = classmateDao
							.getSelfContactsEntity(accountNum);
				}
			}

			if (userSelfContactsEntity != null) {
				String authenticated = userSelfContactsEntity
						.getAuthenticated();
				if (authenticated == null || authenticated.equals("0")) {
					// û����֤������½���������
					return;
				}
			}

			// �ӱ������ݿ��ȡ��½�˺ŵ�½�������������½�Ŀ���ʹ��prefs��autoѡ��
			final String userAccount = userSelfContactsEntity.getUserAccount();
			final String password = userSelfContactsEntity.getPassword();
			new Thread() {
				@Override
				public void run() {
					// �����һ�ε�����
					setTigaseServiceContinue(false);
					clearLastConnection();
					setChatServerLoginState(APPConstant.LOGIN_NOT_EXCUTE);
					// ��������ά���߳��Ѿ��˳�
					while (!tigaseServiceContinue
							&& getChatServerLoginState() != APPConstant.LOGIN_TIGASE_SUCC
							&& getChatServerLoginState() != APPConstant.LOGIN_ADOPT) {
						try {
							CYLog.i(TAG,
									"-----try loginOnTigase : useraccount "
											+ userAccount + " password : "
											+ password);
							String u = userAccount;
							String p = password;
							if (u == null || u.equals("")) {
								u = getUserAccount();
							}
							if (p == null || p.equals("")) {
								p = getPassword();
							}
							loginOnTigase(u, p);
							sleep(APPConstant.CONNECTION_CHECK_INTERVAL / 2);
						} catch (InterruptedException e) {
							e.printStackTrace();
							CYLog.e(TAG, "loginOnTigase failed");
						}
					}
				}
			}.start();
		} else if (cmd == EventbusCMD.SEND_ROSTER) {
			EventBus.getDefault().post(roster);
		} else if (cmd == EventbusCMD.QUIT_TIGASE) {
			// �����������ά���߳��˳�
			this.setTigaseServiceContinue(false);
			this.clearLastConnection();
			this.setChatServerLoginState(APPConstant.LOGIN_ADOPT);
		}
	}

	public int getChatServerLoginState() {
		return chatServerLoginState;
	}

	/** ���õ�¼״̬ */
	public void setChatServerLoginState(int chatServerLoginState) {
		this.chatServerLoginState = chatServerLoginState;
	}

	public ChatManager getChatManager() {
		return chatManager;
	}

	public void setChatManager(ChatManager chatManager) {
		this.chatManager = chatManager;
	}

	public Map<String, Chat> getChatMap() {
		return chatMap;
	}

	public void setChatMap(Map<String, Chat> chatMap) {
		this.chatMap = chatMap;
	}

	public String getUserAccount() {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getString("USERNAME", "username");
	}

	// public void setAccountNum(String accountNum) {
	// this.accountNum = accountNum;
	// StringBuffer buf = new StringBuffer();
	// buf.append(accountNum).append("@")
	// .append(APPConstant.TIGASE_SERVER_DOMAIN);
	// jid = buf.toString();
	// }

	// public String getJid() {
	// return jid;
	// }
	//
	// public void setJid(String jid) {
	// this.jid = jid;
	// }
	//
	public String getPassword() {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getString("PASS", "pass");
	}

	//
	// public void setPassword(String password) {
	// this.password = password;
	// }

	public XMPPConnection getConnection() {
		return connection;
	}

	public void clearConnection() {
		connection = null;
	}

	public Roster getRoster() {
		return roster;
	}

	public void setRoster(Roster roster) {
		this.roster = roster;
		// ֪ͨ��roser����
		EventBus.getDefault().post(roster);
		// if (dataCenterManagerService != null) {
		// // �ȸ����ڴ�ͱ�����ϵ������, Ȼ�����UI
		// dataCenterManagerService.sendRosterIntentBroadcast();
		// } else {
		// CYLog.i(TAG, "setRoster dataCenterManagerService is null");
		// }
	}

	public void onEventBackgroundThread(ContactsEntity userSelfContactsEntity) {
		this.userSelfContactsEntity = userSelfContactsEntity;
	}

	public ContactsEntity getUserSelfContactsEntity() {
		if (userSelfContactsEntity != null) {
			return userSelfContactsEntity;
		} else {
			ClassmateDao classmateDao = new ClassmateDao(
					this.getApplicationContext());
			userSelfContactsEntity = classmateDao
					.getSelfContactsEntity(getUserAccount());
			return userSelfContactsEntity;
		}
	}

	public boolean isHasLogined() {
		return hasLogined;
	}

	public void setHasLogined(boolean hasLogined) {
		this.hasLogined = hasLogined;
	}

	public boolean isTigaseServiceContinue() {
		return tigaseServiceContinue;
	}

	public void setTigaseServiceContinue(boolean tigaseServiceContinue) {
		this.tigaseServiceContinue = tigaseServiceContinue;
	}

	/**
	 * ��½��ʱ���Զ��������е�������
	 * 
	 * �ο�addSelfToClassChatRoom���������޸ĺ����ʹ��
	 */
	// public void autoJoinAllGroupChatRoom(String groupName) {
	// CYLog.i(TAG, "groupName=" + groupName);
	// String[] roomNames = groupName.split(",");
	// CYLog.i(TAG, "�Զ�����������Ҹ���=" + roomNames.length);
	// groupChatMap = new HashMap<String, MultiUserChat>();
	// MultiUserChat groupChat = null;
	// for (int i = 0; i < roomNames.length; i++) {
	// CYLog.i(TAG, "�Զ�����������" + roomNames[i]);
	// groupChat = new MultiUserChat(connection, roomNames[i]);
	// // ���ɱ��˺�jid
	// StringBuffer buf = new StringBuffer();
	// buf.append(
	// dataCenterManagerService.getUserSelfContactsEntity()
	// .getUserAccount()).append("@")
	// .append(APPConstant.TIGASE_SERVER_DOMAIN);
	// String tmp_jid = buf.toString();
	// GroupChat.joinMultiUserChat(groupChat, tmp_jid);
	// // ���Ⱥ����Ϣ������
	// groupChat.addMessageListener(new MultiChatMessageListener(
	// dataCenterManagerService, roomNames[i]));
	// groupChatMap.put(roomNames[i], groupChat);
	//
	// CYLog.i(TAG, "�Զ�����������" + roomNames[i]);
	// }
	// }

	/*
	 * �������ҷ�����Ϣ
	 */
	public boolean sendGroupChatMessage(String groupChatRoomName,
			String messageBody) {
		try {
			boolean ret = sendGroupChatMessageInnerEx(groupChatRoomName,
					messageBody);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "sendGroupChatMessage failed!");
			return false;
		}
	}

	/*
	 * �������ҷ�����Ϣ
	 */
	/*
	 * public boolean sendGroupChatMessageInner(String groupChatRoomName, String
	 * messageBody) { try { if (groupChatRoomName == null ||
	 * groupChatRoomName.equals("")) { CYLog.e(TAG,
	 * "joinMultiUserChat groupChatRoomName is null"); return false; } String
	 * jid = null; if (groupChatRoomName.endsWith("@muc." +
	 * APPConstant.TIGASE_SERVER_DOMAIN)) { jid = groupChatRoomName; } else {
	 * jid = groupChatRoomName + "@muc." + APPConstant.TIGASE_SERVER_DOMAIN; }
	 * 
	 * MultiUserChat groupChat = null; if (groupChatMap == null) { groupChatMap
	 * = new HashMap<String, MultiUserChat>(); } if
	 * (groupChatMap.containsKey(groupChatRoomName)) { groupChat =
	 * groupChatMap.get(groupChatRoomName); } else { groupChat =
	 * this.addSelfToClassChatRoom2(groupChatRoomName); }
	 * 
	 * if (groupChat == null) { CYLog.e(TAG, "group msg send failed : " +
	 * getUserAccount() + " : " + messageBody); return false; } else {
	 * hasAddToClassChatRoom = true; }
	 * 
	 * if (messageBody != null && !messageBody.equals("")) { Message msg =
	 * groupChat.createMessage();
	 * 
	 * // �Ƿ���Ҫ��Ӻ�׺domain msg.setFrom(getUserAccount());
	 * msg.setBody(messageBody); groupChat.sendMessage(msg); CYLog.i(TAG,
	 * "group msg from " + getUserAccount() + " : " + messageBody); }
	 * 
	 * return true; } catch (Exception e) { e.printStackTrace(); CYLog.e(TAG,
	 * "joinMultiUserChat " + e.toString()); return false; } }
	 */

	public boolean sendGroupChatMessageInnerEx(String groupId,
			String messageBody) {
		try {
			// +++lqg+++ ���ڿ��Ƕ�node����������
			LeafNode roomNode = null;
			try {
				roomNode = pubSubManager.getNode(groupId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// ����ڵ㲻�����������´�������֤��Ϣ���ͣ���������������ڵ�Ҳ�ǻ�ʧ�ܵ�
			if (roomNode == null) {
				roomNode = this.pubSubManager.createNode(groupId);
				if (roomNode != null) {
					// ��Ⱥ���ڵ������˷���ǿ�ƶ�������
					GroupChatRoomEntity groupChatRoomEntity = new GroupChatRoomEntity();
					groupChatRoomEntity.setGroupId(groupId);
					GroupChatFuncStatusEntity returnStatus = new GroupChatFuncStatusEntity();
					returnStatus
							.setFuncStatus(GroupChatFuncStatusEntity.GROUPCHAT_FORCE_SUBSCRIBE);
					EventBus.getDefault().post(returnStatus);
				}

				return false;
			}
			roomNode.publish(new Item(messageBody));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// private void setGroupChatMap(Map<String, MultiUserChat> groupChatMap) {
	// this.groupChatMap = groupChatMap;
	// }
	//
	// public Map<String, MultiUserChat> getGroupChatMap() {
	// return groupChatMap;
	// }

	// public boolean addGroupChatOfRoomName(String rooName) {
	// try {
	// MultiUserChat groupChat = null;
	// if (groupChatMap == null) {
	// groupChatMap = new HashMap<String, MultiUserChat>();
	// }
	// if (!groupChatMap.containsKey(rooName)) {
	// groupChat = new MultiUserChat(connection, rooName);
	// // ���ɱ��˺�jid
	// StringBuffer buf = new StringBuffer();
	// buf.append(
	// dataCenterManagerService.getUserSelfContactsEntity()
	// .getUserAccount()).append("@")
	// .append(APPConstant.TIGASE_SERVER_DOMAIN);
	// GroupChat.joinMultiUserChat(groupChat, buf.toString());
	// // ���Ⱥ����Ϣ������
	// groupChat.addMessageListener(new MultiChatMessageListener(
	// dataCenterManagerService, rooName));
	// groupChatMap.put(rooName, groupChat);
	// CYLog.i(TAG, "create group chat room " + rooName);
	// }
	// return true;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	public boolean isHasAddToClassChatRoom() {
		return hasAddToClassChatRoom;
	}

	public void setHasAddToClassChatRoom(boolean hasAddToClassChatRoom) {
		this.hasAddToClassChatRoom = hasAddToClassChatRoom;
	}

	public boolean isHasAddConnectionPacketAndFilterListener() {
		return hasAddConnectionPacketAndFilterListener;
	}

	public void setHasAddConnectionPacketAndFilterListener(
			boolean hasAddConnectionPacketAndFilterListener) {
		this.hasAddConnectionPacketAndFilterListener = hasAddConnectionPacketAndFilterListener;
	}

	/**
	 * �Է���Ӻ�����Ӧ
	 * 
	 * @param friendAccount
	 */
	public void onEventBackgroundThread(FriendPresence friendPresence) {
		try {
			Presence presence = new Presence(friendPresence.getSubscribed());
			String friendAccount = friendPresence.getFriendAccount();
			if (!friendAccount.endsWith("@" + APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
				presence.setTo(friendAccount + "@"
						+ APPBaseInfo.TIGASE_SERVER_DOMAIN);// ���շ�jid
			} else {
				presence.setTo(friendAccount);
			}
			// ���ɱ��˺�jid
			StringBuffer buf = new StringBuffer();
			buf.append(this.getUserAccount()).append("@")
					.append(APPBaseInfo.TIGASE_SERVER_DOMAIN);
			presence.setFrom(buf.toString());// ���ͷ�jid
			connection.sendPacket(presence);// connection�����Լ���XMPPConnection����
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// public void sendAgreeAddEntry(String friendAccount) {
	// try {
	// Presence presence = new Presence(Presence.Type.subscribed);// ͬ����
	// presence.setTo(friendAccount + "@"
	// + APPConstant.TIGASE_SERVER_DOMAIN);// ���շ�jid
	// // ���ɱ��˺�jid
	// StringBuffer buf = new StringBuffer();
	// buf.append(getUserAccount()).append("@")
	// .append(APPConstant.TIGASE_SERVER_DOMAIN);
	// presence.setFrom(buf.toString());// ���ͷ�jid
	// connection.sendPacket(presence);// connection�����Լ���XMPPConnection����
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// /**
	// * �ܾ��Է���Ӻ���
	// *
	// * @param friendAccount
	// */
	// public void sendDisagreeAddEntry(String friendAccount) {
	// try {
	// Presence presence = new Presence(Presence.Type.unsubscribed);//
	// �ܾ������˶��ģ����ܾ��Էŵ��������
	// presence.setTo(friendAccount + "@"
	// + APPConstant.TIGASE_SERVER_DOMAIN);// ���շ�jid
	// // ���ɱ��˺�jid
	// StringBuffer buf = new StringBuffer();
	// buf.append(getUserAccount()).append("@")
	// .append(APPConstant.TIGASE_SERVER_DOMAIN);
	// presence.setFrom(buf.toString());// ���ͷ�jid
	// connection.sendPacket(presence);// connection�����Լ���XMPPConnection����
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * ��Ӻ���
	 * 
	 * @param nickname
	 * @param friendAccount
	 * @param className
	 * @return
	 */
	public boolean addEntry(String nickname, String friendAccount,
			String className) {
		try {
			if (roster == null) {
				CYLog.e(TAG, "addEntry roster null");
				return false;
			}

			String jid = null;
			if (friendAccount.endsWith("@" + APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
				jid = friendAccount;
			} else {
				jid = friendAccount + "@" + APPBaseInfo.TIGASE_SERVER_DOMAIN;
			}

			if (isMyFriend(friendAccount)) {
				CYLog.i(TAG, friendAccount + " " + nickname
						+ "is friend alerady");
				return true;
			}

			RosterGroup gMyFriend = roster.getGroup(className);
			if (gMyFriend == null) {
				gMyFriend = roster.createGroup(className);
			}

			if (gMyFriend == null) {
				CYLog.e(TAG, "addEntry createGroup failed");
				return false;
			}

			roster.createEntry(friendAccount + "@"
					+ APPBaseInfo.TIGASE_SERVER_DOMAIN, nickname,
					new String[] { className });
			// // ֪ͨUI,�����
			this.setRoster(roster);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "addEntry " + e.toString());
			return false;
		}
	}

	/**
	 * �Ƿ��Ǻ���
	 * 
	 * @param friendAccount
	 * @return
	 */
	public boolean isMyFriend(String friendAccount) {
		try {
			String jid = null;
			if (friendAccount.endsWith("@" + APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
				jid = friendAccount;
			} else {
				jid = friendAccount + "@" + APPBaseInfo.TIGASE_SERVER_DOMAIN;
			}
			return roster.contains(jid);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * ɾ��ĳ��
	 * 
	 * @param jid
	 *            = �˺� + @ + APPConstant.TIGASE_SERVER_DOMAIN
	 */
	public boolean removeContactsEntity(String jid) {
		try {
			if (!jid.endsWith("@" + APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
				jid = jid + "@" + APPBaseInfo.TIGASE_SERVER_DOMAIN;
			}

			RosterEntry rosterEntry = roster.getEntry(jid);
			if (rosterEntry != null) {
				roster.removeEntry(rosterEntry);
			}

			if (roster != null) {
				new Thread() {
					public void run() {
						// ֪ͨUI,�����
						setRoster(roster);
					}
				}.start();
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
