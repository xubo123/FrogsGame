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
	// 以后改为list，多个群聊室多个监听器
	// private MultiChatMessageListener multiChatMessageListener;
	// private MultiUserChat groupChat;
	private PubSubManager pubSubManager;

	// ping 检测监听器
	private TigasePingFailedListener pingFailedListener;
	private PingManager tigasePingManager;

	int connectCount = 0;
	private ContactsEntity userSelfContactsEntity;
	private boolean hasLogined;// 是否已经登录聊天服务器
	private boolean tigaseServiceContinue;// 维护连接的线程是否继续
	private boolean hasAddConnectionPacketAndFilterListener;// 连接是否加入了包过滤器，用于接收好友添加的消息
	private boolean hasAddToClassChatRoom;// 是否已经加入了班级群聊室
	TimeBroadcastReceiver receiverTime;
	WakeupBroadcastReceiver receiverW;

	private int chatServerLoginState;// 登录状态

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
		if (auto != null && auto.equals("auto")) {// 自动登录才能重启动
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

		// 登陆状态为自动登陆，聊天服务连接断开，登陆状态非登录成功则重新登录聊天服务器
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String auto = prefs.getString("AUTO", "no");
		if (auto != null && auto.equals("auto") && !tigaseServiceContinue
				&& chatServerLoginState != APPConstant.LOGIN_TIGASE_SUCC) {
			EventbusCMD mEventbusCMD = new EventbusCMD();
			mEventbusCMD.setCMD(EventbusCMD.LOGIN_TIGASE);
			EventBus.getDefault().post(mEventbusCMD);// 不传递对象的调用直接用int指令代替，0为登陆指令
		}

		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	/**
	 * 登录聊天服务器
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

		// 启动维护tigase连接的线程
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
	 * Message Message用于表示一个消息包(可以用调试工具看到发送包和接收包的具体内容)。它有以下多种类型。
	 * Message.Type.NORMAL -- （默认）文本消息（比如邮件） Message.Type.CHAT --
	 * 典型的短消息，如QQ聊天的一行一行显示的消息 Message.Type.GROUP_CHAT -- 群聊消息
	 * Message.Type.HEADLINE -- 滚动显示的消息 Message.TYPE.ERROR -- 错误的消息
	 * Message有两个内部类： Message.Body -- 表示消息体 Message.Type -- 表示消息类型
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

				// 登陆成功后获取离线消息
				try {
					// 注册聊天消息监听器
					chatManager = ChatManager.getInstanceFor(connection);
					if (chatManager == null) {
						CYLog.e(TAG, "chatManager is null");

						// 连接成功后登陆造成的失败，需要发离线消息告诉聊天服务器
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

					// 连接成功后登陆造成的失败，需要发离线消息告诉聊天服务器
					if (connection != null) {
						this.sendOfflinePresence();
					}
					return false;
				}

				// 监听了所有人的心跳数据包，效率太低，耗流量
				if (!hasAddConnectionPacketAndFilterListener) {
					hasAddConnectionPacketAndFilterListener = addConnectionPacketAndFilterListener(connection);
				}
			} catch (Exception e) {
				e.printStackTrace();
				CYLog.e(TAG, e.toString());

				// 连接成功后登陆造成的失败，需要发离线消息告诉聊天服务器
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

				// 连接成功后登陆造成的失败，需要发离线消息告诉聊天服务器
				if (connection != null) {
					this.sendOfflinePresence();
				}
				return false;
			}

			// 调用后会发通知UI
			this.setRoster(roster);

			/*
			 * 别的用户可以使用一个订阅请求(相当于QQ加好友)尝试订阅目的用户。可以使用枚举类型Roster.
			 * SubscriptionMode的值处理这些请求： accept_all: 接收所有订阅请求
			 * reject_all：拒绝所有订阅请求 manual： 手工处理订阅请求
			 */
			// roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
			roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
			tigaseRosterListener = new TigaseRosterListener(this);
			roster.addRosterListener(tigaseRosterListener);

			// 两种处理离线消息的方式，一，在这里统一使用消息监听器，二采用MyChatManagerListener方式，代码保留
			// // 添加单聊的消息监听器，可以接收到离线消息
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

			// connection监听的 listener需要定义为成员，disconnect要移除
			// 两种处理离线消息的方式，一，在这里统一使用消息监听器，二采用MyChatManagerListener方式，代码保留
			// // 添加群聊的消息监听器，可以接收到离线消息
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
			// // 群聊消息入库
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

			// 注册聊天室邀请监听器
			// groupchatInvitationHandler =
			// new GroupChatInvitationHandler(dataCenterManagerService);
			// 设置群聊室邀请加入监听器
			// try {
			// MultiUserChat.addInvitationListener(this.connection,
			// new InvitationListener() {
			// @Override
			// public void invitationReceived(XMPPConnection conn,
			// String room, String inviter, String reason,
			// String password, Message msg) {
			// CYLog.i(TAG, "有人邀请你加入聊天室!");
			//
			// android.os.Message msg1 = dataCenterManagerService
			// .getGroupChatInvitationHandler()
			// .obtainMessage();
			// // 根据邀请人账号查询其真实名字
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
			// // 连接成功后登陆造成的失败，需要发离线消息告诉聊天服务器
			// if (connection != null) {
			// this.sendOfflinePresence();
			// }
			// return false;
			// }

			hasAddToClassChatRoom = true;
			// // 加入聊天室
			// if (!hasAddToClassChatRoom) {
			// hasAddToClassChatRoom = addSelfToClassChatRoom();
			// }
			if (connection != null) {
				String pubSubAddress = "pubsub." + connection.getServiceName();
				pubSubManager = new PubSubManager(connection, pubSubAddress);
			} else {
				return false;
			}
			// 获取所订阅的群组，添加消息监听器，+++lqg+++,当前统一通过单聊消息的监听器处理
			// Map<LeafNode, GroupNodeSubItemEventListener> leafNodeListenerMap
			// = new
			// HashMap<LeafNode, GroupNodeSubItemEventListener>();
			// GroupNodeSubItemEventListener groupNodeSubItemEventListener =
			// new GroupNodeSubItemEventListener();
			// roomNode.addItemEventListener(groupNodeSubItemEventListener);
			// //加入map，便于清理
			// leafNodeListenerMap.put(roomNode, groupNodeSubItemEventListener);

			String sign = null;
			try {
				// 在线
				sign = this.getUserSelfContactsEntity().getSign();

				if (sign == null || sign.equals("")) {
					sign = "online";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (sendOnlinePresence(sign)) {
				// 成功登陆聊天服务器，检查是否有遗留的通知消息未发送出去
				checkNotifyMsgDirectory();
				// 发送聊天服务器已连上EventBus消息
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
	 * 检查是否有遗留的通知消息未发送出去，发送，并删除序列化文件
	 */
	private void checkNotifyMsgDirectory() {
		try {
			String dir = SearchSuggestionProvider.pathStr + File.separator
					+ "notifymsg";
			File notifyFilePath = new File(dir);
			if (!notifyFilePath.exists()) {
				return;// 没有目录，直接返回
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
						file.delete();// 删除文件
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

	// // 计时判断，群聊消息，用户登录后的一分钟内的群聊消息，先缓存起来，之后批量更新
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
	// // 没发送完之前，不能清空
	// // groupChatMessageList.clear();
	// // multiChatMessageListener
	// // .setGroupChatMessageList(groupChatMessageList);
	// }
	// timer.cancel(); // 退出计时器
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
	// // 班级聊天室
	// try {
	// GroupChatRoom groupChatRoom = new GroupChatRoom();
	// String groupName = this.getUserSelfContactsEntity().getGroupName();
	// String[] names = groupName.split(",");
	// if (names.length > 0 && names[0] != null && !names[0].equals("")) {
	// // 创建或者加入聊天室
	// groupChat = GroupChat.createGroupChatRoom(connection, names[0],
	// this.getUserAccount());
	//
	// // 添加群聊消息监听器,如果监听器添加了多次则会收到多个消息
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
	// // 启动定时器
	// timer = new Timer(true);
	// timer.schedule(task, 10 * 1000, 10000); // 延时10 *
	// // 1000ms后执行，1000ms执行一次
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
	 * 目前只支持一个群聊室
	 * 
	 * @param roomName
	 * @return
	 */
	// public MultiUserChat addSelfToClassChatRoom2(String roomName) {
	// // 班级聊天室
	// try {
	// GroupChatRoom groupChatRoom = new GroupChatRoom();
	//
	// // 创建或者加入聊天室
	// groupChat = GroupChat.createGroupChatRoom(connection, roomName,
	// this.getUserAccount());
	// // 添加群聊消息监听器,如果监听器添加了多次则会收到多个消息
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
	 * Presence 表示XMPP状态的packet。每个presence packet都有一个状态。用枚举类型Presence.Type的值表示：
	 * available -- （默认）用户空闲状态 unavailable -- 用户没空看消息 subscribe --
	 * 请求订阅别人，即请求加对方为好友 subscribed -- 统一被别人订阅，也就是确认被对方加为好友 unsubscribe --
	 * 他取消订阅别人，请求删除某好友 unsubscribed -- 拒绝被别人订阅，即拒绝对方的添加请求 error -- 当前状态packet有错误
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
	 * 收到好友添加请求
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
	 * 连接聊天服务器
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
			configuration.setCompressionEnabled(true);// 支持数据压缩
			configuration.setSendPresence(false);// 状态设置为离线，这样可以在登录的时候首先获取到离线消息
			configuration.setSecurityMode(SecurityMode.disabled);
			// 不要自动重连，我们自己有重连 机制，自动重连，导致断网后app无法再进行聊天服务
			configuration.setReconnectionAllowed(false);

			connection = new XMPPTCPConnection(configuration);

			// 加入ping检测
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

			// 设置等待时间,默认设置，否则影响离线消息接收
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
			// 添加连接监听器
			tigaseConnectionListener = new TigaseConnectionListener(this);
			connection.addConnectionListener(tigaseConnectionListener);
			return true;
		} else {
			connectCount++;
			return false;
		}
	}

	// ibind 暴露出去
	public synchronized boolean sendChatMessage(String accountNum,
			String message) {
		CYLog.i(TAG, "sendMessage " + message);
		try {
			boolean ret = sendChatMessageInner(accountNum, message);
			return ret;
		} catch (Exception e) {
			// 发送失败的信息入库，下次reconnect的时候发送
			// dataCenterManagerService
			e.printStackTrace();
			CYLog.i(TAG, "to " + accountNum + " failed : " + message);
			CYLog.e(TAG, e.toString());
			return false;
		}
	}

	/**
	 * 处理发送消息
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
	 * 发送通知消息
	 * 
	 * @param mSendChatMessage
	 */
	private void sendNotifyMessage(final ChatMessageSendEntity mSendChatMessage) {
		// 通知消息需要可靠性保证
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
					// 通知消息保存本地，下次检测到网络或者登陆成功聊天服务器时再取消息发送
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

		// 发送加好友请求
		if (cmdType.equals(APPConstant.CMD_PREFIX_FRIEND_ADD_REQUEST)) {
			if (!isMyFriend(toAccountNum)) {
				StringBuffer cmdmsg = new StringBuffer();
				cmdmsg.append(cmdType).append(this.getUserAccount())
						.append("_").append(friendAddEntity.getName())
						.append("_").append(friendAddEntity.getClassName());
				flag = this.sendChatMessage(toAccountNum, cmdmsg.toString());
			}
		}

		// 创建好友
		if (cmdType.equals(APPConstant.CMD_PREFIX_FRIEND_ADD_AGREE)) {
			if (!isMyFriend(toAccountNum)) {
				StringBuffer cmdmsg = new StringBuffer();
				cmdmsg.append(cmdType).append(this.getUserAccount())
						.append("_").append(friendAddEntity.getName())
						.append("_").append(friendAddEntity.getClassName());
				flag = this.sendChatMessage(toAccountNum, cmdmsg.toString());
			}

			if (flag) {
				flag = addEntry(name, toAccountNum, "我的好友");
			}
		}

		// 删除好友
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
	 * 群组的创建、删除、加入、踢人、查询
	 * 
	 * @param room
	 */
	public void onEventBackgroundThread(GroupChatRoomEntity groupChatRoomEntity) {
		try {
			LeafNode roomNode = null;
			String groupNodeId = groupChatRoomEntity.getGroupId();
			GroupChatFuncStatusEntity returnStatus = new GroupChatFuncStatusEntity();
			// 无论成功失败都带回群组
			returnStatus.setGroupChatRoomEntity(groupChatRoomEntity);

			// 创建群组
			if (groupChatRoomEntity.getFunctionType() == GroupChatRoomEntity.FUNCTYPE_ADD) {
				try {
					// 对节点进行配置
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
					// 服务器之前已经创建过该节点，节点与用户账号是绑定的，只有该用户会用到
					try {
						roomNode = pubSubManager.getNode(groupNodeId);
					} catch (Exception e2) {
						CYLog.i(TAG, "createNode is in exception2!");
						e2.printStackTrace();
					}
				}

				// 可能服务器上有这个节点
				if (roomNode == null) {
					try {
						roomNode = pubSubManager.getNode(groupNodeId);
						// 节点中还有订阅者
						if (roomNode.getSubscriptions().size() > 1) {
							roomNode = null;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				boolean ret = false;
				if (roomNode != null) {
					// 订阅群组所在的节点数据
					ret = subscribeGroupNode(roomNode);
				}

				if (ret) {
					// 发送添加聊天室操作已完成的消息
					returnStatus
							.setFuncStatus(GroupChatFuncStatusEntity.GROUPCHAT_ADDROOM_SUCCESS);
					EventBus.getDefault().post(returnStatus);
				} else {
					// 发送添加聊天室操作失败的消息
					returnStatus
							.setFuncStatus(GroupChatFuncStatusEntity.GROUPCHAT_ADDROOM_FAIL);
					EventBus.getDefault().post(returnStatus);
				}
			} else if (groupChatRoomEntity.getFunctionType() == GroupChatRoomEntity.FUNCTYPE_DEL) {

				// 先取消订阅，群主才有权限，删除节点
				try {
					// 先取消订阅
					roomNode = pubSubManager.getNode(groupNodeId);
					if (roomNode != null) {
						roomNode.unsubscribe(groupNodeId);
					}

					Integer flag = groupChatRoomEntity.getOccupantsMap().get(
							this.getUserAccount());
					// 测试模式，任何人都可以删除群组
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
				// 取消订阅
				try {
					// 先取消订阅
					roomNode = pubSubManager.getNode(groupNodeId);
					if (roomNode != null) {
						StringBuffer buf = new StringBuffer();
						buf.append(this.getUserAccount()).append("@")
								.append(APPBaseInfo.TIGASE_SERVER_DOMAIN);
						// connection.getUser();//后面带有资源Smack
						roomNode.unsubscribe(buf.toString());

						// 群组已经没有人订阅了,删除节点
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
				// 加入聊天室
				CYLog.i(TAG, getUserAccount() + "加入聊天室! " + groupNodeId);
				boolean ret = false;
				try {
					roomNode = pubSubManager.getNode(groupNodeId);
				} catch (Exception e) {
					CYLog.i(TAG, "getNode is in exception!");
					e.printStackTrace();
				}

				if (roomNode != null) {
					ret = subscribeGroupNode(roomNode);
					CYLog.i(TAG, "tigase服务器上存在这样的节点!");
				} else {
					CYLog.i(TAG, "tigase服务器上不存在这样的节点!");
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

	// //发送群组邀请相关信息，有三种：邀请消息，接受邀请信息，拒绝邀请信息
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
	// mSendChatMessage.setMessageType("notifyall");// 消息类型标记
	// EventBus.getDefault().post(mSendChatMessage);// 发消息，等待返回
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * 邀请某人加入某群
	 * 
	 * @param friendAccount
	 * @param userAccount
	 * @param groupNodeId
	 */
	public void inviteFriendJoinGroup(String friendAccount, String userAccount,
			String groupNodeId) {
		try {
			// 过滤自己收到的邀请信息
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
	 * 订阅群组所在的节点 roomNode
	 * 
	 * @param roomNode
	 */
	private boolean subscribeGroupNode(LeafNode roomNode) {
		// 自己加入群组
		try {
			SubscribeForm subscriptionForm = new SubscribeForm(FormType.submit);
			subscriptionForm.setDeliverOn(true);
			subscriptionForm.setDigestFrequency(5000);
			subscriptionForm.setDigestOn(true);
			subscriptionForm.setIncludeBody(true);

			// jid订阅
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
			// 发送失败的信息入库，下次reconnect的时候发送
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

		this.setTigaseServiceContinue(false);// 线程退出
		// add 取消绑定 ibind
		// unbindService(dataCenterManagerIntentConn);
		EventBus.getDefault().unregister(this);
		super.onDestroy();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String auto = prefs.getString("AUTO", "no");
		if (auto == null || auto.equals("auto")) {// 如果是用户主动退出的话就不需要重启服务了
			CYLog.i(TAG, "unnormal onDestroy,restart");
			restartService();
		}
	}

	// /**
	// * 服务重启 不可用，断网后无法自动重连
	// */
	// public void restartChatMsgService() {
	// try {
	// // 线程退出
	// this.setTigaseServiceContinue(false);
	// // 清除数据
	// clearLastConnection();
	// // 重新登录
	// loginOnTigase(dataCenterManagerService.getUserSelfContactsEntity().getUserAccount(),
	// dataCenterManagerService.getUserSelfContactsEntity().getPassword());
	// } catch (Exception e) {
	// e.printStackTrace();
	// CYLog.e(TAG, "restartChatMsgService " + e.toString());
	// }
	// }

	/** 清除上一次与聊天服务器的连接 */
	public void clearLastConnection() {
		CYLog.i(TAG, "clearLastConnection");
		// 离线
		sendOfflinePresence();

		if (tigaseRosterListener != null) {
			tigaseRosterListener = null;
		}

		// 在myChatManagerListener之前清除，影响离线消息接收
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

		// 群聊
		if (groupChatMap != null) {
			groupChatMap.clear();
			groupChatMap = null;
		}

		// 在multiChatMessageListener之前清除
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

		// 清空chat map
		if (chatMap != null) {
			for (String key : chatMap.keySet()) {
				chatMap.get(key).close();
			}
			chatMap.clear();
			chatMap = null;
		}

		this.setHasAddConnectionPacketAndFilterListener(false);

		// 在所有的listener清除之后释放连接
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

			// 清除listenter的引用,影响离线消息
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
	 * 解析接收到的单聊消息，并存储
	 * 
	 * @param msg
	 */
	public void resolveChatMessage(Message msg) {
		try {
			// 如果是聊天室消息，则解析
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
					// 丢弃掉自己发送出去的消息
					if (groupMsg.startsWith(this.getUserAccount())) {
						return;
					}
					// 说明是群聊命令消息
					GroupChatMessage mGroupChatMessage = new GroupChatMessage();
					mGroupChatMessage.setGroupId(groupId);
					mGroupChatMessage.setMessage(groupMsg);
					EventBus.getDefault().post(mGroupChatMessage);
				}
				return;
			}

			// 丢弃掉自己发送出去的消息
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

	// 群聊消息模板
	// <message id='181' to='230@hust' from='pubsub.hust'>
	// <thread>
	// d067fdb3-59d7-4012-ab60-c5081ddc02a0
	// </thread>
	// <event xmlns='http://jabber.org/protocol/pubsub#event'>
	// <items node='发语音'>
	// <item id='240/缪新杰/放寒假'/>
	// </items>
	// </event>
	// </message>
	/**
	 * 解析群聊xml消息,返回groupId和msg内容
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
				// 先查看本地是否有个人信息，如果有就直接从本地取，个人信息入库
				if (classmateDao.isSelfContactsEntityExisted(accountNum)) {
					userSelfContactsEntity = classmateDao
							.getSelfContactsEntity(accountNum);
				}
			}

			if (userSelfContactsEntity != null) {
				String authenticated = userSelfContactsEntity
						.getAuthenticated();
				if (authenticated == null || authenticated.equals("0")) {
					// 没有认证，不登陆聊天服务器
					return;
				}
			}

			// 从本地数据库获取登陆账号登陆聊天服务器，登陆的控制使用prefs的auto选项
			final String userAccount = userSelfContactsEntity.getUserAccount();
			final String password = userSelfContactsEntity.getPassword();
			new Thread() {
				@Override
				public void run() {
					// 清除上一次的连接
					setTigaseServiceContinue(false);
					clearLastConnection();
					setChatServerLoginState(APPConstant.LOGIN_NOT_EXCUTE);
					// 聊天连接维护线程已经退出
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
			// 聊天服务连接维护线程退出
			this.setTigaseServiceContinue(false);
			this.clearLastConnection();
			this.setChatServerLoginState(APPConstant.LOGIN_ADOPT);
		}
	}

	public int getChatServerLoginState() {
		return chatServerLoginState;
	}

	/** 设置登录状态 */
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
		// 通知有roser更新
		EventBus.getDefault().post(roster);
		// if (dataCenterManagerService != null) {
		// // 先更新内存和本地联系人数据, 然后更新UI
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
	 * 登陆的时候自动加入所有的聊天室
	 * 
	 * 参考addSelfToClassChatRoom函数代码修改后才能使用
	 */
	// public void autoJoinAllGroupChatRoom(String groupName) {
	// CYLog.i(TAG, "groupName=" + groupName);
	// String[] roomNames = groupName.split(",");
	// CYLog.i(TAG, "自动加入的聊天室个数=" + roomNames.length);
	// groupChatMap = new HashMap<String, MultiUserChat>();
	// MultiUserChat groupChat = null;
	// for (int i = 0; i < roomNames.length; i++) {
	// CYLog.i(TAG, "自动加入聊天室" + roomNames[i]);
	// groupChat = new MultiUserChat(connection, roomNames[i]);
	// // 生成本账号jid
	// StringBuffer buf = new StringBuffer();
	// buf.append(
	// dataCenterManagerService.getUserSelfContactsEntity()
	// .getUserAccount()).append("@")
	// .append(APPConstant.TIGASE_SERVER_DOMAIN);
	// String tmp_jid = buf.toString();
	// GroupChat.joinMultiUserChat(groupChat, tmp_jid);
	// // 添加群聊消息监听器
	// groupChat.addMessageListener(new MultiChatMessageListener(
	// dataCenterManagerService, roomNames[i]));
	// groupChatMap.put(roomNames[i], groupChat);
	//
	// CYLog.i(TAG, "自动加入聊天室" + roomNames[i]);
	// }
	// }

	/*
	 * 向聊天室发送消息
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
	 * 向聊天室发送消息
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
	 * // 是否需要添加后缀domain msg.setFrom(getUserAccount());
	 * msg.setBody(messageBody); groupChat.sendMessage(msg); CYLog.i(TAG,
	 * "group msg from " + getUserAccount() + " : " + messageBody); }
	 * 
	 * return true; } catch (Exception e) { e.printStackTrace(); CYLog.e(TAG,
	 * "joinMultiUserChat " + e.toString()); return false; } }
	 */

	public boolean sendGroupChatMessageInnerEx(String groupId,
			String messageBody) {
		try {
			// +++lqg+++ 后期考虑对node本地做缓存
			LeafNode roomNode = null;
			try {
				roomNode = pubSubManager.getNode(groupId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 如果节点不存在试着重新创建，保证消息发送，如果断网，创建节点也是会失败的
			if (roomNode == null) {
				roomNode = this.pubSubManager.createNode(groupId);
				if (roomNode != null) {
					// 给群组内的所有人发送强制订阅命令
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
	// // 生成本账号jid
	// StringBuffer buf = new StringBuffer();
	// buf.append(
	// dataCenterManagerService.getUserSelfContactsEntity()
	// .getUserAccount()).append("@")
	// .append(APPConstant.TIGASE_SERVER_DOMAIN);
	// GroupChat.joinMultiUserChat(groupChat, buf.toString());
	// // 添加群聊消息监听器
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
	 * 对方添加好友响应
	 * 
	 * @param friendAccount
	 */
	public void onEventBackgroundThread(FriendPresence friendPresence) {
		try {
			Presence presence = new Presence(friendPresence.getSubscribed());
			String friendAccount = friendPresence.getFriendAccount();
			if (!friendAccount.endsWith("@" + APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
				presence.setTo(friendAccount + "@"
						+ APPBaseInfo.TIGASE_SERVER_DOMAIN);// 接收方jid
			} else {
				presence.setTo(friendAccount);
			}
			// 生成本账号jid
			StringBuffer buf = new StringBuffer();
			buf.append(this.getUserAccount()).append("@")
					.append(APPBaseInfo.TIGASE_SERVER_DOMAIN);
			presence.setFrom(buf.toString());// 发送方jid
			connection.sendPacket(presence);// connection是你自己的XMPPConnection链接
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// public void sendAgreeAddEntry(String friendAccount) {
	// try {
	// Presence presence = new Presence(Presence.Type.subscribed);// 同意是
	// presence.setTo(friendAccount + "@"
	// + APPConstant.TIGASE_SERVER_DOMAIN);// 接收方jid
	// // 生成本账号jid
	// StringBuffer buf = new StringBuffer();
	// buf.append(getUserAccount()).append("@")
	// .append(APPConstant.TIGASE_SERVER_DOMAIN);
	// presence.setFrom(buf.toString());// 发送方jid
	// connection.sendPacket(presence);// connection是你自己的XMPPConnection链接
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// /**
	// * 拒绝对方添加好友
	// *
	// * @param friendAccount
	// */
	// public void sendDisagreeAddEntry(String friendAccount) {
	// try {
	// Presence presence = new Presence(Presence.Type.unsubscribed);//
	// 拒绝被别人订阅，即拒绝对放的添加请求
	// presence.setTo(friendAccount + "@"
	// + APPConstant.TIGASE_SERVER_DOMAIN);// 接收方jid
	// // 生成本账号jid
	// StringBuffer buf = new StringBuffer();
	// buf.append(getUserAccount()).append("@")
	// .append(APPConstant.TIGASE_SERVER_DOMAIN);
	// presence.setFrom(buf.toString());// 发送方jid
	// connection.sendPacket(presence);// connection是你自己的XMPPConnection链接
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * 添加好友
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
			// // 通知UI,并入库
			this.setRoster(roster);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "addEntry " + e.toString());
			return false;
		}
	}

	/**
	 * 是否是好友
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
	 * 删除某人
	 * 
	 * @param jid
	 *            = 账号 + @ + APPConstant.TIGASE_SERVER_DOMAIN
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
						// 通知UI,并入库
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
