package com.hust.schoolmatechat.DataCenterManagerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.LocalActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.hust.schoolmatechat.ChatActivity;
import com.hust.schoolmatechat.ContactsFragment;
import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.SchoolMateChat;
import com.hust.schoolmatechat.SearchSuggestionProvider;
import com.hust.schoolmatechat.BackgroundJob.AddGroupChatRoomJob;
import com.hust.schoolmatechat.BackgroundJob.HttpJobStatus;
import com.hust.schoolmatechat.BackgroundJob.JoinGroupChatRoomJob;
import com.hust.schoolmatechat.BackgroundJob.KickGroupChatRoomMemberJob;
import com.hust.schoolmatechat.BackgroundJob.ReceiveInviteeAcceptReplyJob;
import com.hust.schoolmatechat.BackgroundJob.ResetDefaultGroupChatRoomJob;
import com.hust.schoolmatechat.BackgroundJob.ResetGroupChatRoomInfoJob;
import com.hust.schoolmatechat.ChatMsgservice.FriendAddRecvEntity;
import com.hust.schoolmatechat.ChatMsgservice.FriendAddSendEntity;
import com.hust.schoolmatechat.ChatMsgservice.ChatMsgService;
import com.hust.schoolmatechat.ChatMsgservice.EventbusCMD;
import com.hust.schoolmatechat.ChatMsgservice.FriendPresence;
import com.hust.schoolmatechat.ChatMsgservice.GroupChatFuncStatusEntity;
import com.hust.schoolmatechat.ChatMsgservice.GroupChatInvitationHandler;
import com.hust.schoolmatechat.ChatMsgservice.GroupChatMessage;
import com.hust.schoolmatechat.ChatMsgservice.GroupChatMessageList;
import com.hust.schoolmatechat.ChatMsgservice.GroupChatRoom;
import com.hust.schoolmatechat.ChatMsgservice.ChatMessageSendEntity;
import com.hust.schoolmatechat.ChatMsgservice.GroupChatRoomEntity;
import com.hust.schoolmatechat.ChatMsgservice.GroupChatRoomList;
import com.hust.schoolmatechat.ChatMsgservice.StatusSendEntity;
import com.hust.schoolmatechat.ChatMsgservice.TigaseConnectionStatusEntity;
import com.hust.schoolmatechat.dao.ChannelDao;
import com.hust.schoolmatechat.dao.ChatItemDao;
import com.hust.schoolmatechat.dao.ChatMessageDao;
import com.hust.schoolmatechat.dao.ClassmateDao;
import com.hust.schoolmatechat.dao.DepartmentDao;
import com.hust.schoolmatechat.dao.GroupChatDao;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.AppEngine;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.engine.ChatMessage;
import com.hust.schoolmatechat.engine.CrashHandler;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.login.CheckNetworkState;
import com.hust.schoolmatechat.login.LoginUtils;
import com.hust.schoolmatechat.pushedmsgservice.Channel;
import com.hust.schoolmatechat.pushedmsgservice.PushedMessage;
import com.hust.schoolmatechat.pushedmsgservice.PushedMsgService;
import com.hust.schoolmatechat.pushedmsgservice.SingleNewsMessage;
import com.hust.schoolmatechat.utils.StreamUtils;
import com.hust.schoolmatechat.utils.UUIDUtils;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.path.android.jobqueue.JobManager;

import de.greenrobot.event.EventBus;

public class DataCenterManagerService extends Service {
	private static final String TAG = "DataCenterManagerService";

	/** 消息选项卡列表 */
	private List<ChatItem> chatItems;// 待优化
	private List<Channel> channels;// 待优化，采用map

	private Map<String, List<ContactsEntity>> mContactsEntityMap;// 联系人列表
	/** 登陆用户自己的个人信息 包含了账号、密码、通道、群组、图片地址、姓名等 */
	private ContactsEntity userSelfContactsEntity;

	/** 用户所在的聊天室列表 groupId为key */
	private Map<String, GroupChatRoomEntity> groupChatRoomMap;

	/** 入库出库 */
	private ChannelDao channelDao;
	private ChatMessageDao chatMessageDao;
	private ChatItemDao chatItemDao;
	private ClassmateDao classmateDao;// 联系人表
	private DepartmentDao departmentDao;// 机构表
	private GroupChatDao groupChatDao; // 聊天室表
	// private Map<String, List<ChatMessage>> unreadNewsMap = null;
	// private Map<String, List<ChatMessage>> readedNewsMap = null;
	private Map<String, List<ChatMessage>> unreadChatMessageMap = null;
	private Map<String, List<ChatMessage>> readedChatMessageMap = null;
	private Map<String, ContactsEntity> friendsInfoMap;// 存储本地的联系人，账号为key
	// 未读新闻的数目
	// private Map<String, Integer> unreadNewsCountMap = null;
	private Roster roster;
	ContentValues values = null;

	boolean sendSucc = true;// 是否发送成功
	boolean sendStatus = false;// 是否有发送成功与否的状态返回

	GroupChatFuncStatusEntity groupChatFuncStatus = null; // 表示群聊操作的各种状态
	private DownloadFileBroadCastReceiver downloadFileReceiver = null;
	private Context context;

	private JobManager jobManager = SchoolMateChat.getInstance()
			.getJobManager();;

	int numflag = 0;

	// HttpManagerThread线程消息接收器
	Handler httpManagerHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HttpJobStatus.HTTP_SUCCESS:
				CYLog.i(TAG, "Http操作成功!");
				break;
			case HttpJobStatus.HTTP_FAIL:
				CYLog.i(TAG, "Http操作失败!");
				break;
			}
		}
	};

	// 文件上传下载
	Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case APPConstant.DOWNLOAD_STARTED:
				CYLog.i(TAG, "开始接收");
				break;

			case APPConstant.UPDATE_DOWNLOAD_PROGRESS:
				int percentage = msg.arg1;
				CYLog.i(TAG, "接收进度" + percentage);
				break;

			case APPConstant.DOWNLOAD_FINISHED:
				CYLog.d(TAG, "文件下载完成" + msg.obj);
				// 下载完成，通知上层更新UI显示
				Bundle bundle = new Bundle();
				// 消息页面只要有消息都要更新，
				bundle.putString("from", "updateUI");
				messageIntent.putExtras(bundle);
				sendBroadcast(messageIntent);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	class DownloadFileBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Bundle bundle = intent.getExtras();

				if (bundle != null && bundle.containsKey("URL")) {
					String url = bundle.getString("URL");
					new DownloadFileTask(url, null, null, handler).execute("");// 接收到文件消息
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 群组信息
	Handler groupChatHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			// GroupChatRoomEntity结构进行传递，不采用全局变量的方式
			GroupChatRoomEntity groupChatRoomEntity = (GroupChatRoomEntity) msg.obj;
			if (groupChatRoomEntity == null) {
				CYLog.i(TAG, "groupChatRoomEntity is null in handler");
				return;
			}
			String groupId = groupChatRoomEntity.getGroupId();
			if (groupId == null || groupId.equals("")) {
				CYLog.i(TAG, "groupId is null in handler");
				return;
			}

			switch (msg.what) {
			case GroupChatFuncStatusEntity.GROUPCHAT_ADDROOM_SUCCESS: {
				CYLog.i(TAG, "在tigase服务器上创建聊天室成功!");
				jobManager.addJobInBackground(new AddGroupChatRoomJob(
						DataCenterManagerService.this, groupChatRoomEntity));
			}
				break;

			case GroupChatFuncStatusEntity.GROUPCHAT_ADDROOM_FAIL:
				CYLog.i(TAG, "在tigase服务器上创建聊天室失败!");
				break;

			case GroupChatFuncStatusEntity.GROUPCHAT_JOINROOM_SUCCESS: {
				CYLog.i(TAG, "订阅聊天室节点成功!");
				// 要加入的群的信息已下载到本地，且群结点也已经订阅成功，
				// 然后向聊天室里面发送自己同意加入的命令消息 前缀+用户账号_用户姓名_图片地址
				ContactsEntity userSelfContactsEntity = getUserSelfContactsEntity();
				String name = userSelfContactsEntity.getName();
				String picture = userSelfContactsEntity.getPicture();
				String userAccount = userSelfContactsEntity.getUserAccount();
				StringBuffer buf = new StringBuffer();
				buf.append(APPConstant.CMD_PREFIX_GROUPCHAT_ACCEPT_INVITE)
						.append(userAccount).append("_").append(name)
						.append("_").append(picture);
				sendGroupChatMessage(groupId, buf.toString(), true);
			}
				break;

			case GroupChatFuncStatusEntity.GROUPCHAT_JOINROOM_FAIL:
				CYLog.i(TAG, "加入群组失败! " + msg.obj);
				// +++lqg+++ 在发送群聊消息失败的时候检查处理，补救

				break;

			case GroupChatFuncStatusEntity.GROUPCHAT_UBSUBSSRIBE_SUCCESS: {
				CYLog.i(TAG, "此账号退订聊天室" + groupChatRoomEntity.getGroupName()
						+ "成功!");
				// 删除本地群历史信息并更新UI
				deleteLocalGroupChatData(groupId);
				// 如果当前Activity是群聊聊天界面，则销毁此Activity，回到MainActivity
				ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				String cn = am.getRunningTasks(1).get(0).topActivity
						.getClassName();
				CYLog.i(TAG, "当前Activity=" + cn);
				if (cn.equals("com.hust.schoolmatechat.ChatActivity"))
					ChatActivity.instance.finish();
			}
				break;

			case GroupChatFuncStatusEntity.GROUPCHAT_UBSUBSSRIBE_FAIL:
				CYLog.i(TAG, "取消订阅群结点失败!");
				break;

			case GroupChatFuncStatusEntity.GROUPCHAT_FORCE_SUBSCRIBE:
				CYLog.i(TAG, "要求群成员重新订阅群组! " + msg.obj);
				// String userAccount2 = userSelfContactsEntity
				// .getUserAccount();
				// String name2 = userSelfContactsEntity.getName();
				String groupId2 = groupChatRoomEntity.getGroupId();
				StringBuffer buf2 = new StringBuffer();
				buf2.append(APPConstant.CMD_PREFIX_FORCE_SUBSCRIBE).append(
						groupId2);
				sendGroupNotifyCmdMessage(groupChatRoomEntity, buf2.toString());
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	/********** 聊天服务结构移植过来 **************/
	private Intent messageIntent = null;
	private Intent rosterIntent = null;
	private Intent friendAddIntent = null;
	private Intent groupInviteIntent = null;
	private Intent moveOutOfRoomIntent = null;
	private Intent tigaseConnectionStatusIntent = null;
	/********* 用户信息 ****************/
	private String jid;// jid = userSelfContactsEntity.getAccountNum() + @ +
						// domain
	private Intent newsIntent = null;
	private Intent channelIntent = null;

	private int authorizedState;// 用户是否认证成功0没有认证成功，1认证成功
	private boolean userClickedQuitButton;// 用户是否点击退出

	/********* ------------------- **********/

	/** 时间格式 */
	DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// /** 定时刷新ui */
	// Timer timer = new Timer();
	TimeBroadcastReceiver receiverTime;
	WakeupBroadcastReceiver receiverW;
	private boolean isMessageCome = false;

	// private String groupChatRoomName;

	private void onCreateProxy() {
		context = this;

		authorizedState = 0;

		channels = new Vector<Channel>();

		channelDao = new ChannelDao(this);
		chatMessageDao = new ChatMessageDao(this);
		chatItemDao = new ChatItemDao(this);
		classmateDao = new ClassmateDao(this);
		departmentDao = new DepartmentDao(this);
		groupChatDao = new GroupChatDao(this);

		// unreadNewsMap = new HashMap<String, List<ChatMessage>>();
		// readedNewsMap = new HashMap<String, List<ChatMessage>>();
		unreadChatMessageMap = new HashMap<String, List<ChatMessage>>();
		readedChatMessageMap = new HashMap<String, List<ChatMessage>>();
		friendsInfoMap = new HashMap<String, ContactsEntity>();

		messageIntent = new Intent("com.schoolmatechat.message");
		rosterIntent = new Intent("com.schoolmatechat.onRosterChanged");
		friendAddIntent = new Intent("com.schoolmatechat.friendAddMessage");
		groupInviteIntent = new Intent(
				"com.schoolmatechat.groupInviteBroadcastReceiver");
		moveOutOfRoomIntent = new Intent(
				"com.schoolmatechat.moveOutOfRoomBroadcastReceiver");
		tigaseConnectionStatusIntent = new Intent(
				"com.schoolmatechat.tigaseConnectionStatusBroadcastReceiver");

		newsIntent = new Intent("com.schoolmatechat.newsadded2message");
		channelIntent = new Intent("channelistreceived");

		IntentFilter intentFilter = new IntentFilter(
				"com.schoolmatechat.downloadFile");
		downloadFileReceiver = new DownloadFileBroadCastReceiver();
		registerReceiver(downloadFileReceiver, intentFilter);

		// setTimerTask();
	}

	/**
	 * 群组通知的命令消息发给所有成员
	 * 
	 * @param groupChatRoomEntity
	 * @param cmd
	 */
	public synchronized void sendGroupNotifyCmdMessage(
			GroupChatRoomEntity groupChatRoomEntity, String notifyMsg) {
		try {
			for (String key : groupChatRoomEntity.getTargetOccupantsMap()
					.keySet()) {
				sendNotifyAllChatMessage(key, notifyMsg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 程序退出时调用，这样就可以正确判断自动登陆
	 */
	public void resetAuthorizedState() {
		authorizedState = 0;
		// 清空内存数据,重新读取
		chatItems.clear();
		chatItems = null;
		channels.clear();
		channels = null;
		mContactsEntityMap.clear();
		mContactsEntityMap = null;

		// unreadNewsMap = new HashMap<String, List<ChatMessage>>();
		// readedNewsMap = new HashMap<String, List<ChatMessage>>();
		unreadChatMessageMap.clear();
		unreadChatMessageMap = null;
		readedChatMessageMap.clear();
		readedChatMessageMap = null;
		// 联系人缓存列表不用清空
		// unreadNewsCountMap.clear();
	}

	/**
	 * 登陆认证, account为手机号或者账号
	 */
	public int loginAuthenticate(String account, String password) {
		// 尝试登陆web服务器
		try {
			if (authorizedState == 0) {
				String loginJResult = LoginUtils.loginOnMainServer(account,
						password);
				JSONTokener jsonTokener = new JSONTokener(loginJResult);
				JSONObject resultJO = (JSONObject) jsonTokener.nextValue();
				boolean mainServerSuccess = resultJO.getBoolean("success");
				if (mainServerSuccess) {
					// 解析返回的结果数据，并获取登陆用户的信息
					boolean wholeLoginSuccess = handleJResult(loginJResult,
							account, password);
					if (wholeLoginSuccess) {
						authorizedState = 1;// 检验成功
					} else {
						CYLog.d(TAG, "login authenticate failed");
						return 0;
					}
				} else {
					return 0;
				}

				return 1;// 首次认证
			} else {
				CYLog.d(TAG, "has loginAuthenticate");
				return 2;// 已经认证过
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 初始化数据中心，启动线程完成，加快登陆速度 userAccount password
	 */
	public void initialiseDataCenter(final String userAccount,
			final String password) {
		new Thread() {
			@Override
			public void run() {
				// 在认证成功之后，从本地数据库重置各个界面的历史数据
				resetDataFromDb(userAccount);

				// 初始化联系人信息表
				if (!resetContactsEntityMap(userAccount, password)) {
					CYLog.e(TAG, "resetContactsEntityMap failed");
				}
				// 初始化聊天室信息表，包括班级聊天室
				String groupIds = userSelfContactsEntity.getGroupName();
				String[] classNames = userSelfContactsEntity.getClassName()
						.split(",");
				if (groupIds == null) {
					groupIds = "";
				}
				for (int i = 0; i < classNames.length; ++i) {
					String classId = departmentDao
							.getDepartmentId(classNames[i]);
					if (classId == null) {
						classId = "";
					}
					if (!groupIds.contains(classId)) {
						groupIds += "," + classId;
					}
				}
				if (groupIds != null && !groupIds.equals("")) {
					jobManager
							.addJobInBackground(new ResetGroupChatRoomInfoJob(
									DataCenterManagerService.this, groupIds));
				}
			}
		}.start();
	}

	/**
	 * 初始化联系人信息表
	 * 
	 * @param userAccount
	 * @param password
	 * @return
	 */
	public boolean resetContactsEntityMap(final String userAccount,
			final String password) {
		CYLog.i(TAG, "resetContactsEntityMap is called!");
		try {
			boolean ret = true;
			// 先查看本地是否已经完整的获取到班级联系人信息，如果有就直接从本地取，联系人信息入库
			if (classmateDao.hasAllClassmates(userAccount) == 1) {
				mContactsEntityMap = classmateDao
						.getAllcontactsEntity(userAccount);
			} else {
				CYLog.i(TAG, "else branch is called!");
				// 没有认证，则不用去获取其信息
				String auth = userSelfContactsEntity.getAuthenticated();
				if (auth != null && auth.equals("1")) {
					CYLog.i(TAG, "通过认证!");
					new Thread() {
						@Override
						public void run() {
							try {
								// 从web服务器获取联系人信息，并更新数据库
								while (!updateContatsFromWeb(
										userSelfContactsEntity.getBaseInfoId(),
										userAccount, password)) {
									sleep(APPConstant.CONNECTION_CHECK_INTERVAL / 5);
									if (isUserClickedQuitButton()) {
										return;
									}

									CYLog.d(TAG,
											"resetContactsEntityMap updateContatsFromWeb----");
								}
								classmateDao.updateHasAllClassmates(
										userAccount, 1);// 获取成功更新到数据库
								userSelfContactsEntity.setHasAllClassmates(1);
								CYLog.d(TAG,
										"resetContactsEntityMap updateContatsFromWeb successful");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}.start();
				}
			}
			if (mContactsEntityMap == null) {
				// 防止程序应为联系人初始化失败而挂掉
				mContactsEntityMap = new HashMap<String, List<ContactsEntity>>();
				ret = false;
			}

			CYLog.d(TAG, "resetContactsEntityMap result : "
					+ (ret ? "successful" : "failed"));
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 从http服务器批量获取聊天群组非好友账号详细信息,并入库 使用Ion库的版本
	 */
	public void updateGroupChatMembersFromWeb(String userAccount,
			String password, String accountNums) throws Exception {
		JsonObject jStr = new JsonObject();
		jStr.addProperty("command",
				APPConstant.USER_PROFILE_GET_GROUP_MEMBERS_INFO);

		JsonObject jContent = new JsonObject();
		jContent.addProperty("accountNum", userAccount);
		jContent.addProperty("password", password);
		jContent.addProperty("accountNums", accountNums);
		jStr.add("content", jContent);

		String path = APPConstant.getURL()
				+ "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";
		String paramJson = URLEncoder.encode(jStr.toString());

		JsonObject result = Ion.with(context)
				.load(path + "?jsonStr=" + paramJson).asJsonObject().get();

		boolean success = result.get("success").getAsBoolean();
		if (success) {
			JsonArray jsonArray = result.getAsJsonArray("obj");

			List<ContactsEntity> contactsEntityList = new ArrayList<ContactsEntity>();
			for (int i = 0; i < jsonArray.size(); ++i) {
				JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
				ContactsEntity contactsEntity = LoginUtils
						.jsonObjectToContactsEntity(jsonObject, userAccount);
				if (contactsEntity != null) {
					contactsEntity.setHasAllClassmates(1);// 联系人有详细信息
					contactsEntityList.add(contactsEntity);
				}
			}
			// 非好友联系人入库
			updateGroupChatMembersToDB(contactsEntityList);
		} else {
			CYLog.i(TAG, "从web服务器上上获取群成员失败!");
		}
	}

	/**
	 * 从http服务器批量获取聊天群组非好友账号详细信息,并入库 使用httpClient库的版本
	 */
	// public void updateGroupChatMembersFromWeb(String userAccount, String
	// password, String accountNums) {
	// try {
	// JSONObject jStr = new JSONObject();
	// jStr.put("command", APPConstant.USER_PROFILE_GET_GROUP_MEMBERS_INFO);
	// JSONObject jContent = new JSONObject();
	// jContent.put("accountNum", userAccount);
	// jContent.put("password", password);
	// jContent.put("accountNums", accountNums);
	// jStr.put("content", jContent);
	//
	// String path = APPConstant.getURL()
	// +
	// "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";
	// String paramJson = URLEncoder.encode(jStr.toString());
	//
	// HttpClient client = new DefaultHttpClient();
	// HttpGet httpGet = new HttpGet(path + "?jsonStr=" + paramJson);
	// HttpResponse response = client.execute(httpGet);
	//
	// InputStream in = response.getEntity().getContent();
	// byte[] resultBytes = StreamUtils.getBytes(in);
	// String resultJson = new String(resultBytes);
	//
	// JSONTokener jsonTokener = new JSONTokener(resultJson);
	// JSONObject resultJO = (JSONObject) jsonTokener.nextValue();
	// boolean success = resultJO.getBoolean("success");
	// if (success) {
	// JSONArray jsonArray = resultJO.getJSONArray("obj");
	// List<ContactsEntity> contactsEntityList = new
	// ArrayList<ContactsEntity>();
	// for (int i = 0; i < jsonArray.length(); ++i) {
	// JSONObject jsonObject = jsonArray.getJSONObject(i);
	// ContactsEntity contactsEntity =
	// LoginUtils.jsonObjectToContactsEntity(jsonObject, userAccount);
	// if (contactsEntity != null) {
	// contactsEntity.setHasAllClassmates(1);// 联系人有详细信息
	// contactsEntityList.add(contactsEntity);
	// }
	// }
	// updateGroupChatMembersToDB(contactsEntityList);
	// }
	//
	// } catch(Exception e){
	// try {
	// throw e;
	// } catch (Exception e1) {
	// e1.printStackTrace();
	// }
	// }
	// }

	/**
	 * 从http服务器批量获取聊天群组，并入库 使用Ion库的版本
	 */
	public void updateGroupChatRoomsFromWeb(String userAccount,
			String password, String groupIds) throws Exception {
		JsonObject jsonStr = new JsonObject();
		JsonObject jsonContentStr = new JsonObject();
		jsonContentStr.addProperty("accountNum", userAccount);
		jsonContentStr.addProperty("password", password);
		jsonContentStr.addProperty("groupId", groupIds);
		jsonStr.addProperty("command", APPConstant.USER_PROFILE_GET_GROUP_INFO);
		jsonStr.add("content", jsonContentStr);

		String path = APPConstant.getURL()
				+ "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";
		String paramJson = URLEncoder.encode(jsonStr.toString());

		JsonObject result = Ion.with(context)
				.load(path + "?jsonStr=" + paramJson).asJsonObject().get();

		boolean success = result.get("success").getAsBoolean();
		if (success) {
			List<GroupChatRoomEntity> roomList = new ArrayList<GroupChatRoomEntity>();

			JsonArray jsonArray = result.getAsJsonArray("obj");
			for (int i = 0; i < jsonArray.size(); ++i) {
				JsonObject jsonObjectT = jsonArray.get(i).getAsJsonObject();
				GroupChatRoomEntity groupChatRoom = LoginUtils
						.jsonObject2GroupChatRoomEntity(jsonObjectT,
								userAccount, password);
				if (groupChatRoom != null) {
					roomList.add(groupChatRoom);
				}
			}
			// 聊天群组入库
			if (roomList.size() > 0) {
				updateGroupChatRoomToDB(roomList, userAccount);
			}
		}
	}

	/**
	 * 从http服务器批量获取聊天群组，并入库 使用httpClient库的版本
	 */
	// public void updateGroupChatRoomsFromWeb(String accountNum, String
	// password, String groupIds) {
	// try {
	// JSONObject jsonStr = new JSONObject();
	// JSONObject jsonContentStr = new JSONObject();
	// jsonContentStr.put("accountNum", accountNum);
	// jsonContentStr.put("password", password);
	// jsonContentStr.put("groupId", groupIds);
	// jsonStr.put("command", APPConstant.USER_PROFILE_GET_GROUP_INFO);
	// jsonStr.put("content", jsonContentStr);
	//
	// String path = APPConstant.getURL()
	// +
	// "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";
	// String paramJson = URLEncoder.encode(jsonStr.toString());
	//
	// HttpClient client = new DefaultHttpClient();
	// HttpGet httpGet = new HttpGet(path + "?jsonStr=" + paramJson);
	// HttpResponse response = client.execute(httpGet);
	//
	// InputStream in = response.getEntity().getContent();
	// byte[] resultBytes = StreamUtils.getBytes(in);
	// String resultJson = new String(resultBytes);
	//
	// List<GroupChatRoomEntity> roomList = new
	// ArrayList<GroupChatRoomEntity>();
	//
	// // 根据将查询结果字符串转换为GroupChatRoomEntity
	// JSONObject jsonObject = new JSONObject(resultJson);
	// boolean success = jsonObject.getBoolean("success");
	// if (success) {
	// JSONArray jsonArray = jsonObject.getJSONArray("obj");
	// for (int i = 0; i < jsonArray.length(); ++i) {
	// JSONObject jsonObjectT = jsonArray.getJSONObject(i);
	// GroupChatRoomEntity groupChatRoom =
	// LoginUtils.jsonObject2GroupChatRoomEntity(
	// jsonObjectT, accountNum, password);
	// if (groupChatRoom != null) {
	// roomList.add(groupChatRoom);
	// }
	// }
	// }
	// //聊天群组入库
	// if (roomList.size() > 0) {
	// updateGroupChatRoomToDB(roomList, accountNum);
	// }
	// } catch(Exception e) {
	// try {
	// throw e;
	// } catch (Exception e1) {
	// e1.printStackTrace();
	// }
	// }
	// }

	/**
	 * 群组列表信息更新到本地
	 */
	private synchronized void updateGroupChatRoomToDB(
			List<GroupChatRoomEntity> groupChatRoomEntityList,
			String userAccount) {
		try {
			CYLog.i(TAG, "updateGroupChatRoomToDB is called!");

			int size = groupChatRoomEntityList.size();
			// 依次处理从web服务器上取的聊天室记录
			for (int i = 0; i < size; ++i) {
				GroupChatRoomEntity groupChatRoomEntity = groupChatRoomEntityList
						.get(i);
				if (!groupChatDao.isGroupChatRoomEntityExisted(userAccount,
						groupChatRoomEntity.getGroupId())) { // 此聊天室是新添加的,则添加到本地数据库
					groupChatDao.addGroupChatEntity(groupChatRoomEntity);
				} else { // 此聊天室已存在于本地，更新本地数据库
					groupChatDao.updateGroupChatEntity(groupChatRoomEntity);
				}
			}

			if (size > 0) {
				// 更新完数据库后，更新内存
				groupChatRoomMap = null;
				this.checkGroupChatRoomMap();
				// +++lqg+++ 被多线程调用，可能出现频繁更新UI的情况
				sendBroadcast(rosterIntent);// 通知UI
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 联系人信息入库
	 */
	private synchronized void updateGroupChatMembersToDB(
			List<ContactsEntity> contactsEntityList) {
		for (int j = 0; j < contactsEntityList.size(); ++j) {
			ContactsEntity contactsEntity = contactsEntityList.get(j);
			if (!groupChatDao.isContactsEntityExisted(
					contactsEntity.getUserAccount(),
					contactsEntity.getAccountNum())) {
				groupChatDao.addContactsEntity(contactsEntity);
			} else {
				groupChatDao.updateContacsEntity(contactsEntity);
			}
		}
	}

	/**
	 * 解析登陆web服务器认证后的结果数据
	 * 
	 * @param resultJson
	 * @param accountNum
	 * @param password
	 * @return
	 */
	private boolean handleJResult(String resultJson, String accountNum,
			String password) {
		try {
			boolean flag = false;// 标记是否有此人信息
			// 先查看本地是否有个人信息，如果有就直接从本地取，个人信息入库
			if (classmateDao.isSelfContactsEntityExisted(accountNum)) {
				flag = true;// 本地有此人信息
				userSelfContactsEntity = classmateDao
						.getSelfContactsEntity(accountNum);
				String auth = userSelfContactsEntity.getAuthenticated();
				// 认证过
				if (auth != null && auth.equals("1")) {
					EventBus.getDefault().post(userSelfContactsEntity);
					return true;
				}
			}
			ContactsEntity userSelfContactsEntityTemp = userSelfContactsEntity;
			// 注意，web获取的个人信息中没有分组信息，在tigase服务器的roster里面
			userSelfContactsEntity = LoginUtils.jsonToContacsEntity(resultJson,
					accountNum, password);
			// 防止手机号作为账号写入
			userSelfContactsEntity.setUserAccount(userSelfContactsEntity
					.getAccountNum());
			userSelfContactsEntity.setPassword(password);

			if (userSelfContactsEntity.getChannels().equals("")
					&& userSelfContactsEntityTemp != null) {
				userSelfContactsEntity.setChannels(userSelfContactsEntityTemp
						.getChannels());
			}
			if (userSelfContactsEntity.getIntrestType().equals("")
					&& userSelfContactsEntityTemp != null) {
				userSelfContactsEntity
						.setIntrestType(userSelfContactsEntityTemp
								.getIntrestType());
			}
			String auth = userSelfContactsEntity.getAuthenticated();
			// 认证过
			if (auth != null && auth.equals("1")) {
				String baseInfoIds = userSelfContactsEntity.getBaseInfoId();
				if (baseInfoIds != null && !baseInfoIds.equals("")) {
					// 分组信息默认设置为用户所在的班级信息
					String ids[] = baseInfoIds.split(",");
					if (ids.length != 0) {
						// 本地没有机构数据，则使用web数据
						String deptsArray[] = null;
						try {
							JSONTokener jsonTokener = new JSONTokener(
									resultJson);
							JSONObject resultJO = (JSONObject) jsonTokener
									.nextValue();
							JSONObject jsonObject = resultJO
									.getJSONObject("obj");
							CYLog.d(TAG, resultJO.toString());
							String depts = jsonObject.getString("departName");
							deptsArray = depts.split("_");
						} catch (Exception e) {
							e.printStackTrace();
						}

						StringBuffer buf = new StringBuffer();
						if (ids.length > 0) {
							String dept = null;
							String baseInfoId = ids[0].substring(0, 16);
							if (ids[0] != null && !ids[0].equals("")) {
								dept = departmentDao
										.getDepartmentFullName(baseInfoId);
							}

							if (dept == null || dept.equals("")) {
								CYLog.i(TAG, "local getDepartment failed");
								if (deptsArray != null && deptsArray.length > 0) {
									dept = deptsArray[0];// 使用web数据
								}
								if (dept != null && !dept.equals("")) {
									departmentDao.addDepartment(baseInfoId,
											dept);
								}
							}

							if (dept == null || dept.equals("")) {
								CYLog.i(TAG, "local getDepartment failed");
							} else {
								String groupName = dept.substring(
										dept.lastIndexOf(",") + 1,
										dept.length());
								// CYLog.i(TAG, "groupName : " + groupName);
								buf.append(groupName);
							}
						}
						for (int i = 1; i < ids.length; ++i) {
							String dept = null;
							String baseInfoId = ids[i].substring(0, 16);
							if (ids[i] != null && !ids[i].equals("")) {
								dept = departmentDao
										.getDepartmentFullName(baseInfoId);
							}

							if (dept == null || dept.equals("")) {
								CYLog.i(TAG, "local getDepartment failed");
								if (deptsArray != null && deptsArray.length > i) {
									dept = deptsArray[i];// 使用web数据
								}

								if (dept != null && !dept.equals("")) {
									departmentDao.addDepartment(baseInfoId,
											dept);
								}
							}

							if (dept == null || dept.equals("")) {
								CYLog.i(TAG, "local getDepartment failed");
							} else {
								String groupName = dept.substring(
										dept.lastIndexOf(",") + 1,
										dept.length());
								// CYLog.i(TAG, "groupName : " + groupName);
								buf.append(",").append(groupName);
							}
						}

						String className = buf.toString();
						if (className.length() != 0) {
							userSelfContactsEntity.setClassName(className);
							// userSelfContactsEntity.setGroupName(className);//
							// 目前只有班级群聊室这一个群聊室
							userSelfContactsEntity.setHasAllClassmates(0);// 本地还没有从web获取到班级联系人数据
							EventBus.getDefault().post(userSelfContactsEntity);
							if (flag) {
								classmateDao
										.updateSelfContactsEntity(userSelfContactsEntity);
							} else {
								// 本地入库
								classmateDao
										.addSelfContactsEntity(userSelfContactsEntity);
								// 启动本人认证的通知线程，用户退出则退出
								selfAuthenticatedNotifyThread();
							}
						} else {
							CYLog.e(TAG, "local getDepartment failed");
							return false;
						}
					} else {
						CYLog.e(TAG, "userSelfContactsEntity no BaseInfoId");
						return false;
					}
				}
			} else {
				if (flag) {
					classmateDao
							.updateSelfContactsEntity(userSelfContactsEntity);
				} else {
					// 本地入库
					classmateDao.addSelfContactsEntity(userSelfContactsEntity);
					// 启动本人认证的通知线程，用户退出则退出
					selfAuthenticatedNotifyThread();
				}
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return false;
		}
	}

	/**
	 * 第一次登陆的时候启动线程，通知所有已经认证的好友更新本人的信息
	 */
	private void selfAuthenticatedNotifyThread() {
		try {
			new Thread() {
				@Override
				public void run() {
					try {
						while (true) {
							if (isUserClickedQuitButton()) {
								break;
							}

							if (userSelfContactsEntity != null) {
								int ret = userSelfContactsEntity
										.getHasAllClassmates();
								if (ret == 1) {
									updateSelfContactsEntity(
											userSelfContactsEntity, true);
									break;
								}
							}

							sleep(APPConstant.CONNECTION_CHECK_INTERVAL / 2);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从web服务器获取联系人信息
	 */
	private boolean updateContatsFromWeb(String baseInfo, String accountNum,
			String password) {
		try {
			CYLog.i(TAG, "updateContatsFromWeb is called!");
			Map<String, List<ContactsEntity>> tmpMap = new HashMap<String, List<ContactsEntity>>();

			String[] baseInfoIds = baseInfo.split(",");
			for (int i = 0; i < baseInfoIds.length; i++) {
				String baseId = baseInfoIds[i];
				baseId = baseId.substring(0, 16);
				String apartment = departmentDao.getDepartmentFullName(baseId);
				if (apartment == null || apartment.equals("")) {
					apartment = LoginUtils.getDepartFullNameFromWeb(baseId);

					if (apartment == null || apartment.equals("")) {
						CYLog.e(TAG, "can not get full_name with baseInfoId "
								+ baseId);
						continue;
					}
				}
				String groupName = apartment.substring(
						apartment.lastIndexOf(",") + 1, apartment.length());

				// 获取web服务器上的班级同学
				String classmatesResult = LoginUtils.getClassMates(baseId,
						accountNum, password);
				List<ContactsEntity> classmates = LoginUtils
						.handleClassmatesResult(classmatesResult, accountNum,
								password, groupName);
				if (classmates != null) {
					tmpMap.put(groupName, classmates);
				} else {
					CYLog.e(TAG, "LoginUtils.handleClassmatesResult ret null");
					return false;
				}
			}

			updateClassmateToDB(tmpMap, accountNum);
			// CYLog.i(TAG, "get class and mates");
			return true;
		} catch (Exception e) {
			CYLog.i(TAG, "updateContatsFromWeb执行出现异常!");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 本地是否有登陆用户的信息
	 * 
	 * @param userAccount
	 * @return
	 */
	public boolean isSelfContactsEntityExisted(String userAccount) {
		try {
			if (classmateDao == null) {
				classmateDao = new ClassmateDao(this);
			}
			return classmateDao.isSelfContactsEntityExisted(userAccount);
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "" + e.toString());
			return false;
		}
	}

	/**
	 * 根据基础信息id获取好友信息，若为未认证用户，则从网络获取
	 * 
	 * @param baseInfoId
	 * @return
	 */
	public ContactsEntity getFriendInfoByBaseId(String baseInfoId) {
		try {
			if (classmateDao == null) {
				classmateDao = new ClassmateDao(this);
			}

			String userAccount = this.getTigaseAccount();
			ContactsEntity contactsEntity = classmateDao
					.getFriendInfoByBaseInfoIds(baseInfoId, userAccount);

			return contactsEntity;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "" + e.toString());
			return null;
		}
	}

	private boolean updateUnAuthenticatedContactsStarted = false;

	/**
	 * 更新未认证用户的信息
	 */
	public void updateUnAuthenticatedContacts() {
		try {
			if (classmateDao == null) {
				classmateDao = new ClassmateDao(this);
			}
			// 已经更新过或者用户已经退出
			if (updateUnAuthenticatedContactsStarted
					|| isUserClickedQuitButton()) {
				return;
			}
			updateUnAuthenticatedContactsStarted = true;

			final String userAccount = this.getTigaseAccount();
			final String password = this.getTigasePassword();
			List<ContactsEntity> contactsEntityList = classmateDao
					.getAllContactsEntityList(userAccount);
			for (int i = 0; i < contactsEntityList.size(); ++i) {
				ContactsEntity contactsEntity = contactsEntityList.get(i);

				// 非认证用户，启动线程更新信息
				newThreadUpdateUnAuthenticatedUserInfo(contactsEntity,
						userAccount, password);
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "" + e.toString());
		}
	}

	/**
	 * 更新未认证用户的信息
	 * 
	 * @param contactsEntity
	 * @param userAccount
	 * @param password
	 */
	private synchronized void newThreadUpdateUnAuthenticatedUserInfo(
			ContactsEntity contactsEntity, final String userAccount,
			final String password) {
		try {
			String auth = contactsEntity.getAuthenticated();
			if (auth == null || !auth.equals("1")) {
				final String baseinfId = contactsEntity.getBaseInfoId();
				final String className = contactsEntity.getClassName();
				// 网络服务启动线程单独进行
				new Thread() {
					@Override
					public void run() {
						try {
							// web上获取的好友信息没有带分组,需另外设置,此处使用基础id
							ContactsEntity contactsEntity2 = LoginUtils
									.getFriendInfoByBaseInfoId(userAccount,
											password, baseinfId);
							// 用户已经退出
							if (isUserClickedQuitButton()) {
								return;
							}

							CYLog.i(TAG, userAccount
									+ " get friend info by baseid " + baseinfId);
							if (contactsEntity2 == null) {
								return;
							}
							CYLog.i(TAG,
									userAccount
											+ " get "
											+ baseinfId
											+ " "
											+ contactsEntity2.getName()
											+ " auth = "
											+ contactsEntity2
													.getAuthenticated() + " "
											+ contactsEntity2.getAccountNum());

							// 认证用户，更新本地信息
							String auth = contactsEntity2.getAuthenticated();
							if (auth != null && auth.equals("1")) {
								String nickname = contactsEntity2.getName();
								String friendAccount = contactsEntity2
										.getAccountNum();
								// // 不是好友，加为好友，班级好友默认自动添加
								// if (!isMyFriend(friendAccount)) {
								// sendFriendAddAgree(friendAccount, className);
								// return;
								// }

								contactsEntity2.setClassName(className);
								contactsEntity2.setUserAccount(userAccount);
								classmateDao
										.updateContacsEntity(contactsEntity2);

								// +++lqg+++ 后期改为handler处理
								// 更新完数据库后，更新内存
								mContactsEntityMap = classmateDao
										.getAllcontactsEntity(userAccount);
								sendBroadcast(rosterIntent);// 通知UI
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * true 不启动更新未认证的人，不执行操作，false更新认证的人，执行操作
	 * 
	 * @param updateUnAuthenticatedContactsStarted
	 */
	public void setUpdateUnAuthenticatedContactsStarted(
			boolean updateUnAuthenticatedContactsStarted) {
		this.updateUnAuthenticatedContactsStarted = updateUnAuthenticatedContactsStarted;
	}

	public boolean isUpdateUnAuthenticatedContactsStarted() {
		return updateUnAuthenticatedContactsStarted;
	}

	/**
	 * 查找个人信息,指此时登陆app的人
	 * 
	 * @param userAccount
	 */
	public ContactsEntity getSelfContactsEntity(String userAccount) {
		try {
			if (classmateDao == null) {
				classmateDao = new ClassmateDao(this);
			}
			return classmateDao.getSelfContactsEntity(userAccount);
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "" + e.toString());
			return null;
		}
	}

	/**
	 * 获取联系人的信息
	 * 
	 * @param friendAccount
	 */
	public ContactsEntity getFriendInfoByAccount(String friendAccount) {
		if (friendsInfoMap == null) {
			friendsInfoMap = new HashMap<String, ContactsEntity>();
		}

		ContactsEntity contactsEntity = null;
		if (friendsInfoMap.containsKey(friendAccount)) {
			contactsEntity = friendsInfoMap.get(friendAccount);
		} else {
			String userAccount = this.getTigaseAccount();
			contactsEntity = classmateDao.getFriendInfoByAccount(friendAccount,
					userAccount);

			// 非好友，看是否为群内的非好友
			if (contactsEntity == null) {
				if (groupChatDao == null) {
					groupChatDao = new GroupChatDao(this);
				}
				contactsEntity = groupChatDao.getFriendInfoByAccount(
						userAccount, friendAccount);
			}

			if (contactsEntity != null) {
				friendsInfoMap.put(friendAccount, contactsEntity);// 缓存好友信息
			}
		}

		return contactsEntity;
	}

	/**
	 * 更新本地的联系人信息, 加锁访问
	 */
	private synchronized void updateClassmateToDB(
			Map<String, List<ContactsEntity>> classmateMap, String userAccount) {
		CYLog.i(TAG, "updateClassmateToDB is called!");
		ContactsEntity classmateResult;
		if (mContactsEntityMap == null) {
			mContactsEntityMap = classmateDao.getAllcontactsEntity(userAccount);
		}

		// 检查有无新的分组出现
		Set<String> classSet = new HashSet<String>();
		ContactsEntity selfContactsEntity = this.getUserSelfContactsEntity();
		if (selfContactsEntity != null) {
			// 获取组信息
			String className = selfContactsEntity.getClassName();
			if (className != null && !className.equals("")) {
				String classItems[] = className.split(",");
				for (int i = 0; i < classItems.length; ++i) {
					if (classItems[i] != null && !classItems[i].equals("")) {
						classSet.add(classItems[i]);
					}
				}
			}
		}

		for (String key : classmateMap.keySet()) {
			// 如果出现新的分组，则需要修改本地的个人信息
			if (selfContactsEntity != null && !classSet.contains(key)) {
				CYLog.i(TAG, "#########new class name : " + key);
				String className = selfContactsEntity.getClassName();
				selfContactsEntity.setClassName(className + "," + key);
				this.updateSelfContactsEntity(selfContactsEntity, false);
			}

			for (int i = 0; i < classmateMap.get(key).size(); i++) {
				classmateResult = classmateMap.get(key).get(i);
				String baseInfoIds = classmateResult.getBaseInfoId();
				// 本地有这个人,判断标准为基础信息id而给用户账号
				// baseInfoIds内id有可能用逗号隔开了
				if (baseInfoIds == null || baseInfoIds.equals("")) {
					continue;
				}

				if (classmateDao.isContacsEntityExisted(userAccount,
						baseInfoIds)) {
					// 从本地取出这个人的信息
					ContactsEntity friend = null;
					if (baseInfoIds.length() > 19) {
						String baseIds[] = baseInfoIds.split(",");
						for (int j = 0; j < baseIds.length; ++j) {
							// 错误的基础id
							if (baseIds[j].length() != 19) {
								continue;
							}

							friend = classmateDao.getFriendInfoByBaseInfoIds(
									baseIds[j], userAccount);

							if (friend != null) {
								classmateResult.setBaseInfoId(baseIds[j]);
								break;
							}
						}
					} else {
						friend = classmateDao.getFriendInfoByBaseInfoIds(
								baseInfoIds, userAccount);
					}

					if (friend == null) {
						continue;
					}
					updateLocalContactsEntity(classmateResult, friend);
				} else {
					String friendAccount = classmateResult.getAccountNum();
					ContactsEntity temp = null;
					if (friendAccount != null && !friendAccount.equals("")) {
						temp = classmateDao.getFriendInfoByAccount(
								friendAccount, userAccount);
					}

					if (temp == null) {
						classmateDao.addContactsEntity(classmateResult);
					} else {
						classmateDao.updateContacsEntity(classmateResult);
					}
					CYLog.i(TAG,
							"add contact : account = "
									+ classmateResult.getAccountNum()
									+ " name = " + classmateResult.getName());
				}
			}
		}

		// 更新完数据库后，更新内存
		mContactsEntityMap = classmateDao.getAllcontactsEntity(userAccount);
		sendBroadcast(rosterIntent);// 通知UI
	}

	/**
	 * 将friendNew的数据合并到friend，并更新到本地
	 * 
	 * @param friendNew
	 * @param friend
	 */
	private synchronized void updateLocalContactsEntity(
			ContactsEntity friendNew, ContactsEntity friend) {
		// CYLog.d(TAG,"friend.getChannels()="+friend.getChannels()+",friend.getAccountNum()="+friend.getAccountNum());
		// CYLog.e(TAG,"friendNew.getChannels()="+friendNew.getChannels()+",friendNew.getAccountNum()="+friendNew.getAccountNum());
		try {
			if (!friendNew.getAccountNum().equals("")
					&& (friend.getAccountNum() == null || !friend
							.getAccountNum().equals(friendNew.getAccountNum()))) {
				friend.setAccountNum(friendNew.getAccountNum());
			}

			if (!friendNew.getAddress().equals("")
					&& (friend.getAddress() == null || !friend.getAddress()
							.equals(friendNew.getAddress()))) {
				friend.setAddress(friendNew.getAddress());
			}

			if (!friendNew.getAuthenticated().equals("0")
					&& !friendNew.getAuthenticated().equals("")
					&& (friend.getAuthenticated() == null
							|| friend.getAuthenticated().equals("") || friend
							.getAuthenticated().equals("0"))) {
				friend.setAuthenticated(friendNew.getAuthenticated());
			}

			if (!friendNew.getBaseInfoId().equals("")
					&& (friend.getBaseInfoId() == null || !friend
							.getBaseInfoId().equals(friendNew.getBaseInfoId()))) {
				friend.setBaseInfoId(friendNew.getBaseInfoId());
			}

			if (!friendNew.getChannels().equals("")
					&& (friend.getChannels() == null || !friend.getChannels()
							.equals(friendNew.getChannels()))) {
				friend.setChannels(friendNew.getChannels());
			}

			if (!friendNew.getClassName().equals("")
					&& (friend.getClassName() == null || !friend.getClassName()
							.equals(friendNew.getClassName()))) {
				friend.setClassName(friendNew.getClassName());
			}

			if (!friendNew.getEmail().equals("")
					&& (friend.getEmail() == null || !friend.getEmail().equals(
							friendNew.getEmail()))) {
				friend.setEmail(friendNew.getEmail());
			}

			if (!friendNew.getGroupName().equals("")
					&& (friend.getGroupName() == null || !friend.getGroupName()
							.equals(friendNew.getGroupName()))) {
				friend.setGroupName(friendNew.getGroupName());
			}

			// if (!friendNew.getId().equals("")
			// && (friend.getId() == null || !friend.getId().equals(
			// friendNew.getId()))) {
			// friend.setId(friendNew.getId());
			// }

			if (!friendNew.getIntrestType().equals("")
					&& (friend.getIntrestType() == null || !friend
							.getIntrestType()
							.equals(friendNew.getIntrestType()))) {
				friend.setIntrestType(friendNew.getIntrestType());
			}

			if (!friendNew.getName().equals("")
					&& (friend.getName() == null || !friend.getName().equals(
							friendNew.getName()))) {
				friend.setName(friendNew.getName());
			}

			if (!friendNew.getPassword().equals("")
					&& (friend.getPassword() == null || !friend.getPassword()
							.equals(friendNew.getPassword()))) {
				friend.setPassword(friendNew.getPassword());
			}

			if (!friendNew.getPhoneNum().equals("")
					&& (friend.getPhoneNum() == null || !friend.getPhoneNum()
							.equals(friendNew.getPhoneNum()))) {
				friend.setPhoneNum(friendNew.getPhoneNum());
			}

			if (!friendNew.getPicture().equals("")
					&& (friend.getPicture() == null || !friend.getPicture()
							.equals(friendNew.getPicture()))) {
				friend.setPicture(friendNew.getPicture());
			}

			if (!friendNew.getSex().equals("")
					&& (friend.getSex() == null || !friend.getSex().equals(
							friendNew.getSex()))) {
				friend.setSex(friendNew.getSex());
			}

			if (!friendNew.getSign().equals("")
					&& (friend.getSign() == null || !friend.getSign().equals(
							friendNew.getSign()))) {
				friend.setSign(friendNew.getSign());
			}

			if (friend.getHasAllClassmates() == 0) {
				friend.setHasAllClassmates(friendNew.getHasAllClassmates());
			}

			if (classmateDao == null) {
				classmateDao = new ClassmateDao(this);
			}
			classmateDao.updateContacsEntity(friend);
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "updateLocalContactsEntity : " + e);
		}
	}

	/**
	 * 更新用户订阅信息
	 * 
	 * @param mychannels
	 * @param myinterest
	 */
	public void updateChannelInterest(String mychannels, String myinterest) {
		ContactsEntity mContactsEntity = this.getUserSelfContactsEntity();
		mContactsEntity.setChannels(mychannels);
		mContactsEntity.setIntrestType(myinterest);
		if (classmateDao == null) {
			classmateDao = new ClassmateDao(this);
		}
		classmateDao.updateSelfContactsEntity(mContactsEntity);
		userSelfContactsEntity = mContactsEntity;
		EventBus.getDefault().post(userSelfContactsEntity);
	}

	/**
	 * 在认证成功之后，从本地数据库重置各个界面的历史数据
	 */
	public void resetDataFromDb(String accountNum) {// 带上账号，异常退出再次登录
		try {
			CYLog.i(TAG, "resetDataFromDb");
			// 异常判断处理 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ++++lqg++++ 待修改
			if (channels == null || channelDao == null
					|| chatMessageDao == null || chatItemDao == null
					|| classmateDao == null || departmentDao == null
					|| unreadChatMessageMap == null
					|| readedChatMessageMap == null || messageIntent == null
					|| rosterIntent == null || newsIntent == null) {// 很有可能由于报异常导致内存已经被清理了

				// 全部重新初始化
				this.onCreateProxy();
				this.setAuthorizedState(1);
			}

			// 初始化通道信息
			if (channels.size() == 0) {
				// CYLog.d(TAG, "channels.size() == 0");
				List<Channel> dbAllChannels = channelDao.getAllChanenels();
				if (dbAllChannels.size() > 1) {
					// CYLog.d(TAG, "dbAllChannels.size() > 1=" +
					// dbAllChannels.size());
					channels.addAll(dbAllChannels);
				} else {
					// CYLog.d(TAG,
					// "dbAllChannels.size() <= 1=" + dbAllChannels.size());
					channelDao.initChannel();
					dbAllChannels = channelDao.getAllChanenels();
					channels.addAll(dbAllChannels);
					// CYLog.d(TAG, "dbAllChannels.size()=" +
					// dbAllChannels.size());
				}
			} else {
				CYLog.d(TAG, "channels.size() = " + channels.size());
			}

			/**
			 * 重置登陆app的信息
			 */
			userSelfContactsEntity = classmateDao
					.getSelfContactsEntity(accountNum);
			EventBus.getDefault().post(userSelfContactsEntity);

			if (chatItemDao == null) {
				chatItemDao = new ChatItemDao(this);
			}
			// 初始化chatItem
			chatItems = chatItemDao.getAllChatItem(this.userSelfContactsEntity
					.getAccountNum());
			if (chatItems != null) {
				// CYLog.d(TAG, "chatItems.size()=" + chatItems.size());

				// 初始化每一个条目里面的信息内容
				for (ChatItem chatItem : chatItems) {
					List<ChatMessage> unReadList = null;
					if (chatItem.getType() == ChatItem.GROUPCHATITEM) {// 初始化未读群聊消息
						unReadList = chatMessageDao.getUnreadChatMessage(
								// 这个方法才是读取未读的
								chatItem.getOwner(),
								userSelfContactsEntity.getAccountNum(),
								ChatMessage.GROUPCHATMESSAGE);
						// CYLog.d(TAG, "GROUPCHATITEM unReadList.size()="
						// + unReadList.size());
					} else if (chatItem.getType() == ChatItem.PRIVATECHATITEM) {// 初始化未读的单聊消息
						unReadList = chatMessageDao.getUnreadChatMessage(
								chatItem.getOwner(),
								this.userSelfContactsEntity.getAccountNum(),
								ChatMessage.PRIVATECHATMESSAGE);
						// CYLog.d(TAG, "GROUPCHATITEM unReadList.size()="
						// + unReadList.size());
					} else if (chatItem.getType() == ChatItem.NEWSITEM) {// 初始化未读的新闻消息
						unReadList = chatMessageDao.getUnreadChatMessage(
								chatItem.getOwner(),
								this.userSelfContactsEntity.getAccountNum(),
								ChatMessage.NEWSMESSAGE);
						// CYLog.d(TAG, "GROUPCHATITEM unReadList.size()="
						// + unReadList.size());
					}
					if (chatItem != null) {
						CYLog.d(TAG, "item cid :" + chatItem.getOwner()
								+ " type : " + chatItem.getType() + " name : "
								+ chatItem.getName());
					} else {
						CYLog.e(TAG, "chatItems == null");
					}

					if (unReadList != null) {
						unreadChatMessageMap.put(chatItem.getOwner(),
								unReadList);
					} else {
						CYLog.e(TAG, "unReadList == null");
					}
				}
			}

			if (chatMessageDao == null) {
				chatMessageDao = new ChatMessageDao(this);
				classmateDao = new ClassmateDao(this);
				departmentDao = new DepartmentDao(this);

				chatMessageDao = new ChatMessageDao(this);
				// unreadChatMessageMap = new HashMap<String,
				// List<ChatMessage>>();
			} else {
				// 初始化校友帮帮忙信息
				// ++++这里其实不需要初始化了，getchatdata()方法里面有了
				// List<ChatMessage> scHelperMessages = chatMessageDao
				// .getSchoolHelperMessages(ChatMessage.SCHOOLHELPER);
				// unreadChatMessageMap.put(
				// APPConstant.SCHOOL_HELPER_CHANNEL_NAME,
				// scHelperMessages);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// // 第一次登陆的时候主动获取新闻
	// public void getPushedServerNewsActively() {
	//
	//
	// if (chatItems != null && chatItems.size() < 2) {//
	// 若消息列表小于两条，判断为没有新闻，主动取一次
	// this.pushedMsgService.getPushedServerNewsActively();
	// // CYLog.d(TAG, "chatItems.size()=" + chatItems.size());
	// }
	//
	//
	// }

	/**
	 * roster发生变化导致联系人列表发生变化
	 */
	public void updateContactsOnRoster() {
		try {
			// 聊天服务器上的联系人信息到来，更新认证信息或者分组信息
			Roster roster = this.getRoster();
			if (roster != null) {
				Collection<RosterGroup> groups = roster.getGroups();
				Map<String, List<ContactsEntity>> tmpContactsMap = new HashMap<String, List<ContactsEntity>>();
				for (RosterGroup groupEntry : groups) {
					System.out.print("group : " + groupEntry.getName());
					Collection<RosterEntry> rosters = groupEntry.getEntries();
					List<ContactsEntity> tmpContactsList = new ArrayList<ContactsEntity>();
					for (RosterEntry rosterEntry : rosters) {
						String jid = rosterEntry.getUser();
						String friendAccount = "";
						if (jid.endsWith("@" + APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
							try {
								// 到web服务器上获取好友的信息
								friendAccount = jid
										.substring(
												0,
												jid.lastIndexOf("@"
														+ APPBaseInfo.TIGASE_SERVER_DOMAIN));
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							// CYLog.i(TAG, "jid " + jid + " friend account : "
							// + friendAccount);
							continue;
						}
						// CYLog.i(TAG, "jid " + jid + " friend account : "
						// + friendAccount);

						// 本地信息不全
						if (!classmateDao.isContacsEntityInfoComplete(
								this.getTigaseAccount(),// 直接调用本地的方法，可以防止userSelfContactsEntity被回收后拿不到账号
								friendAccount)) {
							// web上获取的好友信息没有带分组,需另外设置
							ContactsEntity contactsEntity = LoginUtils
									.getFriendInfo(this.getTigaseAccount(),
											this.getTigasePassword(),
											friendAccount);
							if (contactsEntity != null) {
								contactsEntity.setClassName(groupEntry
										.getName());// 设置其所在分组信息
								contactsEntity.setAuthenticated("1");// 已经认证过
								tmpContactsList.add(contactsEntity);
							}
						}
					}
					tmpContactsMap.put(groupEntry.getName(), tmpContactsList);
				}
				if (tmpContactsMap.size() != 0) {
					// 更新到本地数据库
					updateClassmateToDB(tmpContactsMap, this.getTigaseAccount());
				}

				// // 成功登陆聊天服务器，检查本地是否有群组信息未同步
				// checkGroupInfoSyncDirectory();
			} else {
				CYLog.e(TAG, "system error on roster null func called");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate() {
		CYLog.i(TAG, "DataCenterManagerService created");
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		StreamUtils.checkBaseInfo(prefs);
		onCreateProxy();

		// bindService(chatMsgServiceIntent, chatMsgServiceConn,
		// Context.BIND_ABOVE_CLIENT);
		EventBus.getDefault().register(this);
		// if (pushedMsgService != null) {
		// this.stopPushedMsgService();
		// }
		// final Intent pushedMsgServiceIntent = new Intent(this,
		// PushedMsgService.class);
		// this.startService(pushedMsgServiceIntent);
		// bindService(pushedMsgServiceIntent, pushedMsgServiceConn,
		// Context.BIND_ABOVE_CLIENT);

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		CYLog.i(TAG, "onStartCommand");
		// onCreateProxy();
		// initMemeryData();
		receiverTime = new TimeBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		registerReceiver(receiverTime, intentFilter);
		receiverW = new WakeupBroadcastReceiver();
		IntentFilter intentFilter1 = new IntentFilter(Intent.ACTION_TIME_TICK);
		registerReceiver(receiverW, intentFilter1);
		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
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
		if (auto == null || auto.equals("auto")) {// 自动登陆才能重新启动
			if (!AppEngine.getInstance(getBaseContext()).isPushServiceWorked()) {
				Intent PushService = new Intent(this, PushedMsgService.class);
				CYLog.i(TAG, "PushService restart");
				startService(PushService);
			}
			if (!AppEngine.getInstance(getBaseContext()).isDataCenterWorked()) {
				Intent dataCenterManagerIntent = new Intent(this,
						DataCenterManagerService.class);
				CYLog.i(TAG, "dataCenterManagerService restart");

				startService(dataCenterManagerIntent);
			}
			if (!AppEngine.getInstance(getBaseContext())
					.isChatMsgServiceWorked()) {
				Intent chatIntent = new Intent(this, ChatMsgService.class);
				CYLog.i(TAG, "ChatMsgService  restart");

				startService(chatIntent);
			}
		}
	}

	@Override
	public void onDestroy() {
		CYLog.i(TAG, "onDestroy");

		if (receiverTime != null)
			unregisterReceiver(receiverTime);
		if (receiverW != null)
			unregisterReceiver(receiverW);

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

	/** ibind 绑定 必须在其他服务绑定自己之前初始化 */
	private DataCenterManagerBiner dataCenterManagerBinder = new DataCenterManagerBiner();

	@Override
	public IBinder onBind(Intent intent) {
		dataCenterManagerBinder = new DataCenterManagerBiner();
		return dataCenterManagerBinder;
	}

	public class DataCenterManagerBiner extends Binder {
		// 获取当前Service的实例
		public DataCenterManagerService getService() {
			return DataCenterManagerService.this;
		}
	}

	/**
	 * 将每一批推送来的新闻更新到内存，同时入库
	 * 
	 * @param pushedMessage
	 */

	public void onEventBackgroundThread(PushedMessage pushedMessage) {
		CYLog.d(TAG, "pushDataToChannel");
		Channel foundChannel = null;
		if (channels == null || (channels != null && channels.size() == 0)) {
			resetDataFromDb(getTigaseAccount());
		}
		for (Channel channel : channels) {
			CYLog.d(TAG, "channel.getcName() = " + channel.getcName()
					+ ",pushedMessage.getcName() = " + pushedMessage.getcName());
			if (pushedMessage.getcName().equals(channel.getcName())) {
				foundChannel = channel;
			}
		}
		if (foundChannel == null) {
			CYLog.d(TAG, "foundChannel == null");
			return;
		}

		ChatItem rcvItem = null;
		for (ChatItem item : chatItems) {
			String owner = item.getOwner();

			if (owner != null && owner.equals(pushedMessage.getcName())) {
				item.setUnread(item.getUnread() + 1);
				item.setLatestMessage(pushedMessage.getNewsSummary());
				item.setTime(pushedMessage.getTime());
				item.setIcon(pushedMessage.getIcon());
				item.setName(pushedMessage.getcName());
				rcvItem = item;
			}
		}

		if (rcvItem == null) {
			rcvItem = new ChatItem();
			rcvItem.setOwner(pushedMessage.getcName());
			rcvItem.setUserAccount(this.getTigaseAccount());
			rcvItem.setIcon(pushedMessage.getIcon());
			rcvItem.setName(pushedMessage.getcName());
			rcvItem.setLatestMessage(pushedMessage.getNewsSummary());
			rcvItem.setType(ChatItem.NEWSITEM);
			rcvItem.setUnread(rcvItem.getUnread() + 1);
			rcvItem.setTime(pushedMessage.getTime());
			chatItems.add(0, rcvItem);
			// 插入一条chatitem记录
			chatItemDao.addChatItem(rcvItem);
		} else {
			chatItems.remove(rcvItem);
			chatItems.add(0, rcvItem);
			chatItemDao.updateChatItem(rcvItem);
		}

		ChatMessage rcvMessage = new ChatMessage();
		rcvMessage.setIcon(pushedMessage.getIcon());
		rcvMessage.setIsRead(0);
		rcvMessage.setOwner(pushedMessage.getcName());
		rcvMessage.setMessageContent(pushedMessage.getNewsSummary());
		// ++++lqg++++有问题 待修改 罗广镇
		rcvMessage.setMid(UUIDUtils.getUUID());
		for (SingleNewsMessage newsMessage : pushedMessage.getNewsList()) {
			newsMessage.setPMId(rcvMessage.getMid());
		}
		rcvMessage.setNewsList(pushedMessage.getNewsList());
		// rcvMessage.setRecvAccount(recvAccount);
		// rcvMessage.setSenderAccount(senderAccount);
		// rcvMessage.setSendSucc(sendSucc);
		rcvMessage.setTime(pushedMessage.getTime());
		rcvMessage.setType(ChatMessage.NEWSMESSAGE);
		rcvMessage.setUserAccount(this.getTigaseAccount());

		// CYLog.i(TAG,"in what.1 newslistsize:"+pushedMessage.getNewsList().size());
		for (SingleNewsMessage newsMessage : pushedMessage.getNewsList()) {
			CYLog.i(TAG, "pmid:" + newsMessage.getPMId());
			CYLog.i(TAG, "summary:" + newsMessage.getSummary());
		}

		List<ChatMessage> foundList = null;
		foundList = unreadChatMessageMap.get(rcvItem.getOwner());
		if (foundList == null) {
			foundList = new ArrayList<ChatMessage>();
			unreadChatMessageMap.put(rcvItem.getOwner(), foundList);
		}
		foundList.add(0, rcvMessage);// 将每次接收来的新闻信息放在最前面
		chatMessageDao.addChatMessage(rcvMessage);

		CYLog.d(TAG, "信道" + pushedMessage.getChannelId() + "的未读新闻信息数是"
				+ unreadChatMessageMap.get(rcvItem.getOwner()).size());
		// 消息通知
		notifyMainUIMsgCome(pushedMessage.getNewsList().get(0).getTitle(),
				pushedMessage.getNewsList().get(0).getSummary());

		// 带上channleId
		newsIntent.putExtra("channelId", rcvItem.getOwner());
		sendBroadcast(newsIntent);
	}

	/**
	 * 取新闻列表
	 * 
	 * @param
	 */
	public List<ChatItem> getNewsItems() {
		try {
			List<ChatItem> newsItems = new ArrayList<ChatItem>();
			// 给每一个频道设置一个对应的item
			for (Channel channel : channels) {
				ChatItem newsItem = new ChatItem();
				newsItem.setOwner(channel.getcName());
				newsItem.setName(channel.getcName());
				newsItem.setIcon(channel.getIcon());
				newsItem.setUserAccount(this.getTigaseAccount());
				newsItem.setLatestMessage(channel.getChannelRemark());
				// 统一用owner作为判断标准
				if (newsItem.getOwner().equals(
						APPConstant.SCHOOL_HELPER_CHANNEL_NAME)) {
					newsItem.setType(ChatItem.SCHOOLHELPERITEM);// 主界面校友帮帮忙消息条目
				} else {
					newsItem.setType(ChatItem.NEWSITEM);
				}
				newsItems.add(newsItem);
			}
			for (ChatItem chatItem : chatItems) {
				if (chatItem.getType() == ChatItem.NEWSITEM) {
					for (ChatItem newsItem : newsItems) {
						if (newsItem.getOwner().equals(chatItem.getOwner())) {
							newsItem.setLatestMessage(chatItem
									.getLatestMessage());
							newsItem.setTime(chatItem.getTime());
							newsItem.setUnread(chatItem.getUnread());
							newsItem.setOwner(chatItem.getOwner());
						}
					}
				}
			}

			return newsItems;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "getNewsItems " + e.toString());
			return null;
		}
	}

	/**
	 * 将一批通道信息更新到内存并更新数据库
	 * 
	 * @param channelList
	 */

	public void onEventBackgroundThread(List<Channel> recvChannels) {
		if (recvChannels != null && recvChannels.size() > 0) {
			// 内存里无通道信息
			CYLog.d(TAG,
					"channel data update channels.size()=" + channels.size());
			if (channels.size() == 0) {
				channelDao.deleteAndSave(recvChannels);
				channels.addAll(recvChannels);
			} else {
				channelDao.deleteAndSave(recvChannels);
				channels.removeAll(channels);
				channels.addAll(channelDao.getAllChanenels());
			}
			sendBroadcast(channelIntent);

			// AppEngine.getInstance(getBaseContext()).onNewsCome();
		}
	}

	/**
	 * 获取所有通道信息，先去检查内存中有无数据，有就直接返回，无就去数据库取
	 * 
	 * @return
	 */

	// public List<Channel> getAllChannel() {
	// CYLog.i(TAG, "通道信息大小为：" + channels.size());
	// return channels;
	// }
	//
	// public List<ChatItem> getAllChatItem() {
	// CYLog.i(TAG, "ITEM信息大小为：" + chatItems.size());
	// return chatItems;
	// }

	/**
	 * 读取新闻
	 * 
	 * @param channelId
	 * @return
	 */

	public List<ChatMessage> getChannelData(String channelId) {
		try {
			List<ChatMessage> newsList = new Vector<ChatMessage>();
			List<ChatMessage> unreadNewsList = unreadChatMessageMap
					.get(channelId);
			if (unreadNewsList == null) {
				unreadNewsList = new Vector<ChatMessage>();
			}

			// 手机显示限制，只能显示100条记录
			if (unreadNewsList.size() >= 100) {
				// 返回的新闻数据
				newsList.addAll(0, unreadNewsList.subList(0, 100));
				// 将数据库中返回的这批数据更新为已读
				chatMessageDao.update(newsList);
				// 将返回的新闻数据移出未读队列
				unreadNewsList.removeAll(newsList);
				// 更新map
				unreadChatMessageMap.put(channelId, unreadNewsList);
			} else {
				// 如果未读消息不足100,取数据库已读消息补齐
				// 将未读消息放队列前面
				newsList.addAll(0, unreadNewsList);
				List<ChatMessage> readChatMessages = chatMessageDao
						.getReadChatMessage(this.getTigaseAccount(),
								ChatMessage.NEWSMESSAGE, 0,
								100 - unreadNewsList.size());
				newsList.addAll(readChatMessages);
				// 更新库里数据为已读
				chatMessageDao.update(unreadNewsList);
				// 将返回的新闻数据移出未读队列
				unreadNewsList.removeAll(unreadNewsList);
				// 更新map
				unreadChatMessageMap.put(channelId, unreadNewsList);
			}
			return newsList;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "getChannelData " + e.toString());
			return null;
		}
	}

	private void addUnreadNewsToDB(List<ChatMessage> list) {
		ChatMessage chatMessage = null;
		for (int i = 0; i < list.size(); i++) {
			// 将未读的信息转化为已读，并将其存到ChatMessage_Table中
			chatMessage = list.get(i);
			values = new ContentValues();
			values.put("Mid", chatMessage.getMid());
			values.put("Icon", chatMessage.getIcon());
			values.put("Type", ChatMessage.NEWSMESSAGE);

			Date mTime = chatMessage.getTime();
			String time = mTime.toString();
			try {
				mTime = format.parse(time);
				values.put("Time", "" + mTime);
			} catch (ParseException e) {
				CYLog.e(TAG, e.toString());
			}
			// db_chatMessage.addChatMessage(values);

			values = null;
			List<SingleNewsMessage> singleNewsMessageList = chatMessage
					.getNewsList();
			SingleNewsMessage singleNewsMessage = null;
			for (int j = 0; j < singleNewsMessageList.size(); j++) {
				values = new ContentValues();
				singleNewsMessage = new SingleNewsMessage();
				singleNewsMessage = singleNewsMessageList.get(j);

				values.put("ChannelID", singleNewsMessage.getChannelId());
				values.put("ISbreaking", "" + singleNewsMessage.isBreaking());
				values.put("PMId", singleNewsMessage.getPMId());
				values.put("NewsUrl", singleNewsMessage.getNewsUrl());
				values.put("Nid", singleNewsMessage.getNid());
				values.put("Title", singleNewsMessage.getTitle());
				values.put("Summary", singleNewsMessage.getSummary());
				values.put("Icon", singleNewsMessage.getIcon());

				time = singleNewsMessage.getTime().toString();
				try {
					mTime = format.parse(time);
					values.put("Time", "" + mTime);
				} catch (ParseException e) {
					CYLog.e(TAG, e.toString());
				}
				// db_getSingleNewsMessage.addSingleNewsMessage(values);
				values = null;
			}

		}
	}

	public List<ChatMessage> getLastNewsChannelData(String channelId) {
		try {
			List<ChatMessage> unreadNewsList = unreadChatMessageMap
					.get(channelId);
			List<ChatMessage> readedNewsList = readedChatMessageMap
					.get(channelId);
			// List<ChatMessage>lastNewsList=unreadNewsList.subList(0,
			// unreadNewsList.size());
			List<ChatMessage> lastNewsList = new ArrayList<ChatMessage>();
			if (unreadNewsList == null) {
				unreadNewsList = new ArrayList<ChatMessage>();
			}
			if (readedNewsList == null) {
				readedNewsList = new ArrayList<ChatMessage>();
			}
			lastNewsList.addAll(0, unreadNewsList);

			// (unreadChatMessageMap) {
			readedNewsList.addAll(0, unreadNewsList);
			unreadChatMessageMap.put(channelId, readedNewsList);
			// }
			// (unreadNewsList) {
			addUnreadNewsToDB(unreadNewsList);
			unreadChatMessageMap.remove(channelId);
			unreadNewsList.clear();
			// unreadNewsList.removeAll(unreadNewsList);
			// }
			CYLog.i(TAG, "信道" + channelId + "的未读条数为0");
			return lastNewsList;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "getLastNewsChannelData " + e.toString());
			return null;
		}
	}

	public Map<String, Integer> getUnreadChatItemCount() {
		try {
			// 返回所有的未读取的聊天信息的cid和对应的未读取条目。
			Map<String, Integer> map = new HashMap<String, Integer>();
			Set<String> set = unreadChatMessageMap.keySet();
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String key = it.next();
				map.put(key, unreadChatMessageMap.get(key).size());
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "getUnreadChatItemCount " + e.toString());
			return null;
		}
	}

	/**
	 * 获取所有未读新闻的条数
	 * 
	 * @return
	 */

	public Map<String, Integer> GetUnreadChannleCount() {
		try {
			// 返回所有的未读取的聊天信息的cid和对应的未读取条目。
			Map<String, Integer> map = new HashMap<String, Integer>();
			Set<String> set = unreadChatMessageMap.keySet();
			Iterator<String> it = set.iterator();
			while (it.hasNext()) {
				String key = it.next();
				map.put(key, unreadChatMessageMap.get(key).size());
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "GetUnreadChannleCount " + e.toString());
			return null;
		}
	}

	public List<ChatMessage> getChatData(String from) {
		try {
			List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
			List<ChatMessage> unreadChatMessageList = unreadChatMessageMap
					.get(from);
			List<ChatMessage> readedChatMessageList = readedChatMessageMap
					.get(from);
			if (unreadChatMessageMap.get(from) == null) {
				unreadChatMessageList = new ArrayList<ChatMessage>();
			}
			if (readedChatMessageMap.get(from) == null) {
				readedChatMessageList = new ArrayList<ChatMessage>();
			}
			// CYLog.i(TAG,unreadChatMessageList.size());
			if (unreadChatMessageList.size() >= 100) {
				// (unreadChatMessageList) {
				addUnreadChatMessageToDB(unreadChatMessageList.subList(0, 100));
				for (int i = 0; i < 100; i++) {
					chatMessageList.add(unreadChatMessageList.get(0));
					unreadChatMessageList.remove(0);
					// }
				}

				// (readedChatMessageMap) {
				readedChatMessageList.addAll(0, chatMessageList);

				readedChatMessageMap.put(from, readedChatMessageList);
				// }

			} else if (unreadChatMessageList.size()
					+ readedChatMessageList.size() >= 100) {
				chatMessageList.addAll(0, unreadChatMessageList);
				chatMessageList.addAll(chatMessageList.size(),
						readedChatMessageList.subList(0,
								100 - unreadChatMessageList.size()));

				// (readedChatMessageMap) {
				readedChatMessageList.addAll(0, chatMessageList);

				readedChatMessageMap.put(from, readedChatMessageList);
				// }
				// (unreadChatMessageMap) {
				addUnreadChatMessageToDB(unreadChatMessageList);
				unreadChatMessageMap.remove(from);
				unreadChatMessageList.clear();
				// }
			} else {
				chatMessageList.addAll(0, unreadChatMessageList);
				chatMessageList.addAll(chatMessageList.size(),
						readedChatMessageList);

				CYLog.i(TAG, "chatMessageList.size()" + chatMessageList.size());

				// (readedChatMessageMap) {
				readedChatMessageList.addAll(0, chatMessageList);

				readedChatMessageMap.put(from, readedChatMessageList);
				// }
				// (unreadChatMessageMap) {
				addUnreadChatMessageToDB(unreadChatMessageList);
				unreadChatMessageMap.remove(from);
				unreadChatMessageList.clear();
				// }

			}
			return chatMessageList;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "getChatData " + e.toString());
			return null;
		}
	}

	public void checkDataFromDB(String owner, String userAccount,
			int messageType) {
		chatMessageDao.checkDataFromDB(owner, userAccount, messageType);
	}

	/**
	 * 取聊天信息
	 * 
	 * @param from
	 * @return
	 */
	public List<ChatMessage> getChatData(String from, int type, int count) {
		try {
			List<ChatMessage> chatMessageList = new Vector<ChatMessage>();
			List<ChatMessage> unreadChatMessageList = unreadChatMessageMap
					.get(from);

			if (unreadChatMessageList == null
					|| unreadChatMessageList.size() == 0) { // 这个from里面根本没有未读消息，
															// 直接读取数据库

				List<ChatMessage> readedChatMessageList = new ArrayList<ChatMessage>();

				// if (from.equals("母校新闻") || from.equals("总会快递")) {

				readedChatMessageList = chatMessageDao.getReadChatMessage(from,
						this.getTigaseAccount(), type, 0, count);
				CYLog.d(TAG, ",readedChatMessageList.size()="
						+ readedChatMessageList.size());
				// } else {
				// readedChatMessageList = chatMessageDao.getReadChatMessage(
				// from, this.getTigaseAccount(), 3, 0, count);
				// }
				chatMessageList.addAll(readedChatMessageList);
			} else { // 这里面list存在，肯定更有未读信息
				chatMessageList.addAll(0, unreadChatMessageList); // 读取所有的未读信息

				CYLog.d(TAG, ",unreadChatMessageList.size()="
						+ unreadChatMessageList.size());

				unreadChatMessageList.removeAll(chatMessageList);
				// 更新map
				unreadChatMessageMap.put(from, unreadChatMessageList); // 可能可以不需要
				List<ChatMessage> readedChatMessageList = null;
				if (chatMessageList.size() < count) {
					readedChatMessageList = chatMessageDao.getReadChatMessage(
							from, this.getTigaseAccount(), type, 0, count
									- chatMessageList.size());
					CYLog.d(TAG,
							",chatMessageList.size()=" + chatMessageList.size()
									+ ",readedChatMessageList.size()="
									+ readedChatMessageList.size());
				}
				// 将数据库中返回的这批数据更新为已读 要先取了已读数据之后才能更新数据库，不然就有两条重复了
				chatMessageDao.update(chatMessageList);
				if (readedChatMessageList != null) {
					chatMessageList.addAll(readedChatMessageList);
				}

			}
			return chatMessageList;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "getChatData " + e.toString());
			return null;
		}
	}

	// public List<ChatMessage> getChatData(String from, int count) {
	// try {
	// List<ChatMessage> chatMessageList = new Vector<ChatMessage>();
	// List<ChatMessage> unreadChatMessageList = unreadChatMessageMap
	// .get(from);
	// if (unreadChatMessageList == null) {
	// unreadChatMessageList = new Vector<ChatMessage>();
	// }
	// // 手机显示限制，只能显示100条记录
	// if (unreadChatMessageList.size() >= 100) {
	// // 返回的聊天数据
	// chatMessageList
	// .addAll(0, unreadChatMessageList.subList(0, 100));
	// // 将数据库中返回的这批数据更新为已读
	// chatMessageDao.update(chatMessageList);
	// // 将返回的新闻数据移出未读队列
	// unreadChatMessageList.removeAll(chatMessageList);
	// // 更新map
	// unreadChatMessageMap.put(from, unreadChatMessageList);
	// } else {
	// // 如果未读消息不足100,取数据库已读消息补齐
	// // 将未读消息放队列前面
	// chatMessageList.addAll(0, unreadChatMessageList);
	// List<ChatMessage> readedChatMessageList = chatMessageDao
	// .getChatMessage(this.getTigaseAccount(), 0,
	// 100 * (count - 1),
	// 100 - unreadChatMessageList.size());
	// chatMessageList.addAll(readedChatMessageList);
	// // 更新库里数据为已读
	// chatMessageDao.update(unreadChatMessageList);
	// // 将返回的新闻数据移出未读队列
	// unreadChatMessageList.removeAll(unreadChatMessageList);
	// // 更新map
	// unreadChatMessageMap.put(from, unreadChatMessageList);
	// }
	// return chatMessageList;
	// } catch (Exception e) {
	// e.printStackTrace();
	// CYLog.e(TAG, "getChatData " + e.toString());
	// return null;
	// }
	// }

	/**
	 * 聊天信息从未读变成已读，将未读信息入库
	 * 
	 * @param list
	 */
	private void addUnreadChatMessageToDB(List<ChatMessage> list) {
		// +++lqg+++ 有错误待修改
		ChatMessage chatMessage = null;
		for (int i = 0; i < list.size(); i++) {
			chatMessage = list.get(i);
			values = new ContentValues();
			values.put("From", chatMessage.getOwner());
			values.put("MessageContent", chatMessage.getMessageContent());
			values.put("Mid", chatMessage.getMid());
			values.put("NickName", chatMessage.getOwner());
			values.put("Type", ChatMessage.PRIVATECHATMESSAGE);
			Date mTime = chatMessage.getTime();
			String time = mTime.toString();
			try {
				mTime = format.parse(time);
				values.put("Time", "" + mTime);
			} catch (ParseException e) {
				CYLog.e(TAG, e.toString());
			}
			// db_chatMessage.addChatMessage(values);
		}
	}

	/*
	 * // 得到某个jid下的数据库的更多聊天历史信息。读取超过100条的数据， ，< 采用内存对象进行数据管理> public
	 * List<ChatMessage> getMoreChatData(String from) { try { List<ChatMessage>
	 * chatMessageList = new ArrayList<ChatMessage>();
	 * 
	 * List<ChatMessage> unreadChatMessageList=unreadChatMessageMap.get(from);
	 * List<ChatMessage> readedChatMessageList=readedChatMessageMap.get(from);
	 * if(unreadChatMessageMap.get(from)==null){ unreadChatMessageList=new
	 * ArrayList<ChatMessage>(); } if(readedChatMessageMap.get(from)==null){
	 * readedChatMessageList=new ArrayList<ChatMessage>(); }
	 * chatMessageList.addAll(0, unreadChatMessageList);
	 * chatMessageList.addAll(chatMessageList.size(), readedChatMessageList);
	 * (readedChatMessageMap){ readedChatMessageList.addAll(0, chatMessageList);
	 * readedChatMessageMap.put(from, readedChatMessageList); }
	 * (unreadChatMessageList){ unreadChatMessageMap.remove(from);
	 * unreadChatMessageList.clear(); }
	 * 
	 * String[] args = { String.valueOf(from) }; List<Map<String, String>>
	 * listMap = db_chatMessage .listChatMessageMaps("NickName=?", args, ""); if
	 * (listMap == null) { return null; } ChatMessage chatMessage = null;
	 * Map<String, String> map = null; for (int i = 0; i < listMap.size(); i++)
	 * { chatMessage = new ChatMessage(); map = listMap.get(i);
	 * chatMessage.setFrom(map.get("NickName"));
	 * chatMessage.setIcon(map.get("Icon"));
	 * chatMessage.setMessageContent(map.get("MessageContent"));
	 * chatMessage.setMid(map.get("Mid"));
	 * chatMessage.setTime(format.parse(map.get("Time")));
	 * chatMessage.setNickName(map.get("NickName"));
	 * chatMessage.setType(Integer.parseInt(map.get("Type")));
	 * chatMessageList.add(chatMessage); } return chatMessageList; } catch
	 * (Exception e) { e.printStackTrace(); CYLog.i(TAG, e.toString()); return
	 * null; } }
	 */

	// 退出时保存所有的未读信息入库

	public void saveAllUnreadChatMessage() {
		// +++lqg+++ 有错误待修改
		List<ChatMessage> chatMessageList = null;
		List<ChatMessage> unreadNewsList = null;
		ContentValues values = null;
		// 关闭客户端时把未读的聊天信息存入数据库中
		for (Entry<String, List<ChatMessage>> entry : unreadChatMessageMap
				.entrySet()) {
			chatMessageList = entry.getValue();
			if (chatMessageList == null) {
				continue;
			} else {
				for (int i = 0; i < chatMessageList.size(); i++) {
					values = new ContentValues();
					values.put("MessageContent", chatMessageList.get(i)
							.getMessageContent());
					values.put("Mid", UUID.randomUUID().toString());
					values.put("NickName", chatMessageList.get(i).getOwner());
					values.put("Type", ChatMessage.PRIVATECHATMESSAGE);
					try {
						Date mTime = format.parse(chatMessageList.get(i)
								.getTime().toString());
						values.put("Time", "" + mTime);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						CYLog.e("=====", "=====le name : " + e);
					}
					// unreadChatMessageDao.addChatMessage(values);
					values = null;
				}
			}
		}

		// 关闭客户端时把未读的新闻信息存入数据库中
		for (Entry<String, List<ChatMessage>> entry : unreadChatMessageMap
				.entrySet()) {
			unreadNewsList = entry.getValue();
			if (unreadNewsList == null) {
				continue;
			} else {
				for (int i = 0; i < unreadNewsList.size(); i++) {
					values = new ContentValues();
					values.put("Mid", unreadNewsList.get(i).getMid());
					values.put("Icon", unreadNewsList.get(i).getIcon());
					values.put("Type", ChatMessage.NEWSMESSAGE);

					try {
						Date mTime = format.parse(unreadNewsList.get(i)
								.getTime().toString());
						values.put("Time", "" + mTime);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						CYLog.e("=====", "=====le name : " + e);
					}
					// db_chatMessage.addChatMessage(values);

					values = null;
					List<SingleNewsMessage> singleNewsMessageList = unreadNewsList
							.get(i).getNewsList();
					SingleNewsMessage singleNewsMessage = null;
					for (int j = 0; j < singleNewsMessageList.size(); j++) {
						values = new ContentValues();
						singleNewsMessage = new SingleNewsMessage();
						singleNewsMessage = singleNewsMessageList.get(j);

						values.put("ChannelID",
								singleNewsMessage.getChannelId());
						values.put("ISbreaking",
								"" + singleNewsMessage.isBreaking());
						values.put("PMId", singleNewsMessage.getPMId());
						values.put("NewsUrl", singleNewsMessage.getNewsUrl());
						values.put("Nid", singleNewsMessage.getNid());
						values.put("Title", singleNewsMessage.getTitle());
						values.put("Summary", singleNewsMessage.getSummary());
						values.put("Icon", singleNewsMessage.getIcon());
						try {
							Date mTime = format.parse(singleNewsMessage
									.getTime().toString());
							values.put("Time", "" + mTime);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// db_getSingleNewsMessage.addSingleNewsMessage(values);
						values = null;
					}
				}
			}
		}
	}

	// 刚登录时获取从数据库中获取的未读信息

	/*
	 * public Map<String, List<ChatMessage>> getAllUnreadChatMessage() {
	 * Map<String, List<ChatMessage>> initUnreadChatMessageMap = null;
	 * List<Map<String, String>> list = unreadChatMessageDao
	 * .listAllChatMessageMaps(); Map<String, String> chatMessageMap = null;
	 * initUnreadChatMessageMap = new HashMap<String, List<ChatMessage>>();
	 * ChatMessage chatMessage = new ChatMessage(); for (int i = 0; i <
	 * list.size(); i++) { chatMessageMap = list.get(i); //
	 * 判断消息类别，如果是新闻信息则返回循环，如果是聊天消息则进行下面的步骤 if
	 * (Integer.parseInt(chatMessageMap.get("Type")) == 1) continue;
	 * chatMessage.setFrom(chatMessageMap.get("NickName"));
	 * chatMessage.setIcon(chatMessageMap.get("Icon"));
	 * chatMessage.setMessageContent(chatMessageMap.get("MessageContent"));
	 * chatMessage.setMid(chatMessageMap.get("Mid")); try {
	 * chatMessage.setTime(format.parse(chatMessageMap.get("Time"))); } catch
	 * (ParseException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * chatMessage.setNickName(chatMessageMap.get("NickName"));
	 * chatMessage.setType(Integer.parseInt(chatMessageMap.get("Type")));
	 * List<ChatMessage> foundList = null; foundList =
	 * initUnreadChatMessageMap.get(chatMessage.getFrom()); if (foundList ==
	 * null) { foundList = new ArrayList<ChatMessage>();
	 * foundList.add(chatMessage);
	 * initUnreadChatMessageMap.put(chatMessage.getFrom(), foundList); } else {
	 * foundList.add(chatMessage); }
	 * 
	 * } return initUnreadChatMessageMap; }
	 */

	/*
	 * public Map<String, List<ChatMessage>> getAllUnreadNewsChatMessage() {
	 * Map<String, List<ChatMessage>> initUnreadNewsMap = null; List<Map<String,
	 * String>> list = unreadChatMessageDao .listAllChatMessageMaps();
	 * Map<String, String> chatMessageMap = null; initUnreadNewsMap = new
	 * HashMap<String, List<ChatMessage>>(); ChatMessage chatMessage = new
	 * ChatMessage(); for (int i = 0; i < list.size(); i++) { chatMessageMap =
	 * list.get(i); // 判断消息类别，如果是新闻信息则进行下面的内容，否则聊天信息的话就返回继续循环 if
	 * (Integer.parseInt(chatMessageMap.get("Type")) != 1) continue;
	 * chatMessage.setIcon(chatMessageMap.get("Icon")); //
	 * chatMessage.setMessageContent(chatMessageMap.get("MessageContent"));
	 * chatMessage.setMid(chatMessageMap.get("Mid")); try {
	 * chatMessage.setTime(format.parse(chatMessageMap.get("Time"))); } catch
	 * (ParseException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } //
	 * chatMessage.setNickName(chatMessageMap.get("NickName"));
	 * chatMessage.setType(Integer.parseInt(chatMessageMap.get("Type")));
	 * String[] args = { String.valueOf(chatMessageMap.get("Mid")) };
	 * List<Map<String, String>> listSingleNewsMessageMap =
	 * unreadSingleNewsMessageDao .listAllSingleNewsMessageMaps("PMId=?", args);
	 * 
	 * List<SingleNewsMessage> singleNewsMessageList = new
	 * ArrayList<SingleNewsMessage>(); String channelId = ""; for (int num = 0;
	 * num < listSingleNewsMessageMap.size(); num++) { Map<String, String>
	 * singleNewsMessageMap = listSingleNewsMessageMap .get(i); channelId =
	 * singleNewsMessageMap.get("ChannelID"); SingleNewsMessage
	 * singleNewsMessage = new SingleNewsMessage();
	 * singleNewsMessage.setChannelId(channelId);
	 * singleNewsMessage.setBreaking(Boolean
	 * .parseBoolean(singleNewsMessageMap.get("ISBreaking")));
	 * singleNewsMessage.setPMId(singleNewsMessageMap.get("PMId"));
	 * singleNewsMessage .setNewsUrl(singleNewsMessageMap.get("NewUrl"));
	 * singleNewsMessage.setNid(Integer.parseInt(singleNewsMessageMap
	 * .get("Nid")));
	 * singleNewsMessage.setTitle(singleNewsMessageMap.get("Title")); try {
	 * singleNewsMessage.setTime(format.parse(singleNewsMessageMap
	 * .get("Time"))); } catch (ParseException e) { // TODO Auto-generated catch
	 * block e.printStackTrace(); }
	 * singleNewsMessage.setSummary(singleNewsMessageMap .get("Summary"));
	 * singleNewsMessage.setIcon(singleNewsMessageMap.get("Icon"));
	 * singleNewsMessageList.add(singleNewsMessage);
	 * 
	 * } List<ChatMessage> foundList = null; foundList =
	 * initUnreadNewsMap.get(channelId); if (foundList == null) { foundList =
	 * new ArrayList<ChatMessage>(); foundList.add(chatMessage);
	 * initUnreadNewsMap.put(channelId, foundList); } else {
	 * foundList.add(chatMessage); }
	 * 
	 * } return initUnreadNewsMap; }
	 */

	/**
	 * 单聊,发送聊天消息
	 */

	public synchronized boolean sendChatMessage(String cid, String message) {
		// CYLog.i(TAG, "sendChatMessage");
		if (message == null || message.equals("")) {
			return false;
		}
		String receiver = null;
		if (cid.endsWith("@" + APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
			receiver = cid.substring(0,
					cid.lastIndexOf("@" + APPBaseInfo.TIGASE_SERVER_DOMAIN));
		} else {
			receiver = cid;
		}
		sendSucc = false;
		sendStatus = false;
		ChatMessageSendEntity mSendChatMessage = new ChatMessageSendEntity();
		mSendChatMessage.setReceiver(receiver);
		mSendChatMessage.setMessage(message);
		mSendChatMessage.setMessageType("single");
		EventBus.getDefault().post(mSendChatMessage);// 发消息，等待返回
		new Handler().postDelayed(new Runnable() {
			public void run() {
				// execute the task
				if (!sendStatus) {
					sendStatus = true;
				}
			}
		}, 2000);
		while (!sendStatus) {
			// CYLog.i(TAG, "sendSucc=" + sendSucc);
		}
		// if (chatMsgService == null
		// || !chatMsgService.sendChatMessage(, )) {
		// // 信息发送失败，不入库，但要标记显示
		// // CYLog.i(TAG, "to " + receiver + " failed : " + message);
		// sendSucc = false;
		// }

		// 将文本消息封装成包含发送此消息账号、发送时间、id、类型等的ChatMessage对象
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setMessageContent(message);
		chatMessage.setMid(UUID.randomUUID().toString());
		chatMessage.setOwner(receiver);// 属主为对方
		chatMessage.setUserAccount(this.getTigaseAccount());
		chatMessage.setSenderAccount(this.getTigaseAccount());
		chatMessage.setRecvAccount(receiver);
		// 设置此消息类型为自己发送出去的单聊消息
		chatMessage.setType(ChatMessage.PRIVATECHATMESSAGE);
		chatMessage.setTime(new Date(System.currentTimeMillis()));
		chatMessage.setIsRead(0);// 设置为未读消息
		chatMessage.setSendSucc(sendSucc);// 消息发送成功与否
		// 添加到内存列表
		addChatMessageToQueue(chatMessage, sendSucc);

		// 通知上层UI
		Bundle bundle = new Bundle();
		bundle.putString("from", receiver);
		// bundle.putString("body", msg.getBody());
		messageIntent.putExtras(bundle);
		sendBroadcast(messageIntent);

		return sendSucc;
	}

	public void onEventBackgroundThread(StatusSendEntity mSendStatus) {
		CYLog.i(TAG, "send status rec..");
		this.sendSucc = mSendStatus.getSendStatus();
		this.sendStatus = true;
	}

	public void onEventBackgroundThread(GroupChatFuncStatusEntity funcStatus) {
		groupChatFuncStatus = funcStatus;
	}

	public void onEventBackgroundThread(TigaseConnectionStatusEntity connStatus) {
		Bundle bundle = new Bundle();
		bundle.putInt("tigaseConnectionStatus", connStatus.getConnStatus());
		tigaseConnectionStatusIntent.putExtras(bundle);
		sendBroadcast(tigaseConnectionStatusIntent);
	}

	/**
	 * 将聊天信息存放在内存中，并且存库
	 * 
	 * @param msg
	 */
	public void onEventBackgroundThread(Message msg) {
		try {
			String from = msg.getFrom().substring(
					0,
					msg.getFrom().lastIndexOf(
							"@" + APPBaseInfo.TIGASE_SERVER_DOMAIN));

			String msgBody = msg.getBody();
			boolean isCmdMsg = onCmdMessageCome(msgBody);
			if (isCmdMsg) {
				return;// 命令消息
			}
			if (msgBody.startsWith(APPConstant.CMD_PREFIX_FILE_SEND)) {
				String url = msgBody.replaceAll(
						APPConstant.CMD_PREFIX_FILE_SEND, "");
				new DownloadFileTask(url, null, null, handler).execute("");// 接收到文件消息
			}

			// 本地没有联系人数据消息屏蔽
			boolean mark = true;
			if (this.getFriendInfoByAccount(from) == null) {
				if (this.isMyFriend(from)) {
					this.checkFriendInfoLocalExisted(from);
					if (this.getFriendInfoByAccount(from) == null) {
						mark = false;
					}
				} else {
					// 是否添加此人为好友
					mark = false;

					// web上获取的好友信息没有带分组,需另外设置
					ContactsEntity contactsEntity = LoginUtils.getFriendInfo(
							this.getTigaseAccount(), this.getTigasePassword(),
							from);
					if (contactsEntity != null) {
						try {
							FriendAddRecvEntity friendAddRecvEntity = new FriendAddRecvEntity();
							friendAddRecvEntity
									.setCmdType(APPConstant.CMD_PREFIX_FRIEND_ADD_REQUEST);
							friendAddRecvEntity.setAccountNum(from);
							friendAddRecvEntity.setName(contactsEntity
									.getName());
							String classname = this.departmentDao
									.getDepartmentFullName(contactsEntity
											.getBaseInfoId().substring(0, 16));
							if (classname == null || classname.equals("")) {
								classname = "未知班级";
							}
							friendAddRecvEntity.setClassName(classname);
							EventBus.getDefault().post(friendAddRecvEntity);

							// 显示消息
							mark = true;
							contactsEntity.setClassName("我的好友");
							this.friendsInfoMap.put(from, contactsEntity);
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}
					}
				}
			}

			if (!mark) {
				CYLog.e(TAG, "---- msg from : " + from + " " + msgBody);
				return;
			}

			ChatMessage rcvMessage = new ChatMessage();
			rcvMessage.setOwner(from);
			rcvMessage.setUserAccount(this.getTigaseAccount());
			rcvMessage.setSenderAccount(from);
			rcvMessage.setRecvAccount(this.getTigaseAccount());
			rcvMessage.setMessageContent(msgBody);
			rcvMessage.setMid(UUID.randomUUID().toString());
			rcvMessage.setType(ChatMessage.PRIVATECHATMESSAGE);
			Date mTime = new Date(System.currentTimeMillis());
			rcvMessage.setTime(mTime);
			rcvMessage.setIsRead(0);
			rcvMessage.setSendSucc(true);// 接收的消息始终是成功的
			addChatMessageToQueue(rcvMessage, true);

			// isMessageCome = true;
			// 消息通知
			if (friendsInfoMap == null) {
				friendsInfoMap = new HashMap<String, ContactsEntity>();
			}
			String name = from;
			if (friendsInfoMap.containsKey(from)) {
				name = friendsInfoMap.get(from).getName();
			} else {
				ContactsEntity friend = classmateDao.getFriendInfoByAccount(
						from, this.getTigaseAccount());
				name = friend.getName();
				if (name == null || name.equals("")) {
					name = from;
				}
				friendsInfoMap.put(from, friend);// 缓存好友信息
			}

			notifyMainUIMsgCome(name, msg.getBody());

			// 通知上层UI
			Bundle bundle = new Bundle();
			bundle.putString("from", from);
			// bundle.putString("body", msg.getBody());
			messageIntent.putExtras(bundle);
			sendBroadcast(messageIntent);
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "pushDataToChat : " + e.toString());
		}
	}

	/**
	 * 处理命令消息，返回是否为命令消息
	 * 
	 * @param msgBody
	 * @return
	 */
	private boolean onCmdMessageCome(String msgBody) {
		try {
			// 判断是否为特殊命令消息
			if (!msgBody.startsWith(APPConstant.CMD_PREFIX)) {
				return false;
			} else {
				if (msgBody
						.startsWith(APPConstant.CMD_PREFIX_UPDATE_FRIEND_INFO)) {
					final String friendAccount = msgBody
							.substring(APPConstant.CMD_PREFIX_UPDATE_FRIEND_INFO
									.length());
					CYLog.i(TAG, "update friend info from web : "
							+ friendAccount);
					// 要更新不是自己的的信息
					final String userAccount = this.getTigaseAccount();
					final String password = this.getTigasePassword();
					if (!userAccount.equals(friendAccount)) {
						// 启动更新线程，后期改为异步处理
						new Thread() {
							@Override
							public void run() {
								try {
									// 获取web好友的信息
									ContactsEntity friendNew = LoginUtils
											.getFriendInfo(userAccount,
													password, friendAccount);
									while (friendNew == null) {
										sleep(5000);
										if (isUserClickedQuitButton()) {
											return;
										}

										friendNew = LoginUtils.getFriendInfo(
												userAccount, password,
												friendAccount);
									}
									if (friendNew != null) {
										friendNew.setAuthenticated("1");// 已经认证过
									}

									// 从本地取出这个人的信息
									if (classmateDao == null) {
										classmateDao = new ClassmateDao(null);
									}
									ContactsEntity friend = classmateDao
											.getFriendInfoByAccount(
													friendAccount, userAccount);
									// 没有账号则通过基础id获取
									if (friend == null) {
										String friendBaseInfoId = friendNew
												.getBaseInfoId();
										// 错误的基础id
										if (friendBaseInfoId.length() < 19) {
											return;
										}
										friend = classmateDao
												.getFriendInfoByBaseInfoIds(
														friendBaseInfoId,
														userAccount);
										// 仍然为空 +++lqg+++ 可能会引起联系人重复的错误
										if (friend == null) {
											// 掰开基础id，按姓名基础id获取，找到记录后删除掉，添加新的记录进去
											String baseInfoIdBak = null;
											String name = friendNew.getName();
											String baseIds[] = friendBaseInfoId
													.split(",");
											for (int i = 0; i < baseIds.length; ++i) {
												if (baseIds[i] != null
														&& !baseIds[i]
																.equals("")) {
													// 错误的基础id
													if (baseIds[i].length() != 19) {
														continue;
													}
													friend = classmateDao
															.getFriendInfoByBaseInfoIds(
																	baseIds[i],
																	userAccount);
													if (friend == null) {
														continue;
													}

													if (friend.getName()
															.equals(name)) {
														baseInfoIdBak = baseIds[i];
														break;
													} else {
														friend = null;
													}
												}
											}

											if (friend == null) {
												return;
											}

											classmateDao
													.deleteContactsEntityByIdAndName(
															userAccount,
															baseInfoIdBak, name);

											// 检验是否删除成功
											ContactsEntity temp = classmateDao
													.getFriendInfoByBaseInfoIds(
															baseInfoIdBak,
															userAccount);
											if (temp == null) {
												temp = classmateDao
														.getFriendInfoByAccount(
																friendAccount,
																userAccount);
												if (temp == null) {
													classmateDao
															.addContactsEntity(friendNew);
												} else {
													friendNew
															.setAccountNum(friendAccount);
													classmateDao
															.updateContacsEntity(friendNew);
												}
											} else {
												return;
											}
										} else {
											updateLocalContactsEntity(
													friendNew, friend);
										}
									} else {
										updateLocalContactsEntity(friendNew,
												friend);
									}

									// 重新获取联系人列表信息
									mContactsEntityMap = classmateDao
											.getAllcontactsEntity(userAccount);
									// 更新内存中好友信息
									if (friendsInfoMap != null
											&& friendsInfoMap
													.containsKey(friendAccount)) {
										friendsInfoMap.remove(friendAccount);
										friendsInfoMap.put(friendAccount,
												friend);
									}

									// 通知上层UI
									Bundle bundle = new Bundle();
									// 消息页面只要有消息都要更新，但是联系人页面只有用户图像更新时才需要更新
									bundle.putString("from", "updateUI");// 只做UI图片更新
									messageIntent.putExtras(bundle);
									sendBroadcast(messageIntent);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}.start();

						return true;
					} else {
						return true;// 不是自己的好友更新头像就跟自己没关系（有时候自己更新头像也会走到这里来）
					}
				} else if (msgBody
						.startsWith(APPConstant.CMD_PREFIX_GROUPCHAT_INVITE)) {
					CYLog.i(TAG, "收到群聊室邀请消息!");
					String groupFilterMsg = msgBody
							.substring(APPConstant.CMD_PREFIX_GROUPCHAT_INVITE
									.length());

					// 通知上层UI，数据中心与UI的交互一律采用broadcast的方式，保持代码结构一致
					Bundle bundle = new Bundle();
					bundle.putString("groupFilterMsg", groupFilterMsg);
					groupInviteIntent.putExtras(bundle);
					sendBroadcast(groupInviteIntent);
					return true;
				} else if (msgBody
						.startsWith(APPConstant.CMD_PREFIX_FORCE_SUBSCRIBE)) {
					CYLog.i(TAG, "收到群聊室强制订阅消息!");
					String groupId = msgBody
							.substring(APPConstant.CMD_PREFIX_FORCE_SUBSCRIBE
									.length());

					// 重新订阅节点
					joinGroupChatRoom(groupId);
					return true;
				} else if (msgBody
						.startsWith(APPConstant.CMD_PREFIX_FRIEND_ADD_REQUEST)) {
					CYLog.i(TAG, "收到好友添加请求!");
					String msg = msgBody
							.substring(APPConstant.CMD_PREFIX_FRIEND_ADD_REQUEST
									.length());
					String msgArray[] = msg.split("_");
					if (msgArray == null || msgArray.length < 3) {
						return false;
					}
					FriendAddRecvEntity friendAddRecvEntity = new FriendAddRecvEntity();
					friendAddRecvEntity
							.setCmdType(APPConstant.CMD_PREFIX_FRIEND_ADD_REQUEST);
					friendAddRecvEntity.setAccountNum(msgArray[0]);
					friendAddRecvEntity.setName(msgArray[1]);
					friendAddRecvEntity.setClassName(msgArray[2]);
					EventBus.getDefault().post(friendAddRecvEntity);
					return true;
				} else if (msgBody
						.startsWith(APPConstant.CMD_PREFIX_FRIEND_ADD_DECLINE)) {
					CYLog.i(TAG, "收到好友添加拒绝!");
					String msg = msgBody
							.substring(APPConstant.CMD_PREFIX_FRIEND_ADD_DECLINE
									.length());
					String msgArray[] = msg.split("_");
					if (msgArray == null || msgArray.length < 3) {
						return false;
					}
					FriendAddRecvEntity friendAddRecvEntity = new FriendAddRecvEntity();
					friendAddRecvEntity
							.setCmdType(APPConstant.CMD_PREFIX_FRIEND_ADD_DECLINE);
					// 不是好友，丢弃接收到的删除消息
					if (!this.isMyFriend(msgArray[0])) {
						return true;
					}
					friendAddRecvEntity.setAccountNum(msgArray[0]);
					friendAddRecvEntity.setName(msgArray[1]);
					friendAddRecvEntity.setClassName(msgArray[2]);
					EventBus.getDefault().post(friendAddRecvEntity);
					return true;
				} else if (msgBody
						.startsWith(APPConstant.CMD_PREFIX_FRIEND_ADD_AGREE)) {
					CYLog.i(TAG, "收到好友添加同意!");
					String msg = msgBody
							.substring(APPConstant.CMD_PREFIX_FRIEND_ADD_AGREE
									.length());
					String msgArray[] = msg.split("_");
					if (msgArray == null || msgArray.length < 3) {
						return false;
					}
					FriendAddRecvEntity friendAddRecvEntity = new FriendAddRecvEntity();
					friendAddRecvEntity
							.setCmdType(APPConstant.CMD_PREFIX_FRIEND_ADD_AGREE);
					friendAddRecvEntity.setAccountNum(msgArray[0]);
					friendAddRecvEntity.setName(msgArray[1]);
					friendAddRecvEntity.setClassName(msgArray[2]);
					EventBus.getDefault().post(friendAddRecvEntity);
					return true;
				}
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 通知消息到达
	 */
	private void notifyMainUIMsgCome(String from, String msg) {
		CYLog.i(TAG, "==========收到服务器消息  From===========" + from);
		CYLog.i(TAG, "==========收到服务器消息  Body===========" + msg);
		// CYLog.d(TAG, "setIsChatingWithWho = "+isChatingWithWho);
		if (!isChatingWithWho.equals(from)) {
			AppEngine.getInstance(getBaseContext()).onNewsCome();
			if (msg.startsWith(APPConstant.CMD_PREFIX_FILE_SEND)) {
				int start = msg.lastIndexOf("/");
				String type = msg.substring(start - 1, start);
				// CYLog.d(TAG,"=======   "+type+"   =====");
				if (type.equals("" + APPConstant.PICTURE)) {// 发送文件类型提示
					msg = APPConstant.SPICTURE;
				} else if (type.equals("" + APPConstant.AUDIO)) {
					msg = APPConstant.SAUDIO;
				} else if (type.equals("" + APPConstant.VIDEO)) {
					msg = APPConstant.SVIDEO;
				} else if (type.equals("" + APPConstant.NORMAL_FILE)) {
					msg = APPConstant.SNORMAL_FILE;
				}
			}
			numflag++;
			final NotificationManager notificationManager = (NotificationManager) getBaseContext()
					.getSystemService(Context.NOTIFICATION_SERVICE);
			String me = from + "#" + msg;
			if (numflag > 1) {
				me = me.split("#")[0] + "#您有" + numflag + "条窗友新消息！";
			}
			if (me != null) {
				final Notification notification = new Notification(

				R.drawable.ic_launcher, from + "：" + msg,
						System.currentTimeMillis());
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				final Intent intent1 = new Intent(getBaseContext(),
						ChatActivity.class);
				final PendingIntent activity = PendingIntent.getActivity(
						getBaseContext(), 0, intent1, 0);

				notification.setLatestEventInfo(getBaseContext(),
						me.split("#")[0], me.split("#")[1], activity);
				// CYLog.d(TAG, "from ==== " +
				// StreamUtils.stringToAsciiInt(from));
				notificationManager.notify(0, notification);
			}
		}
	}

	private String isChatingWithWho = "";

	public void setIsChatingWithWho(String who) {
		isChatingWithWho = who;
		// CYLog.d(TAG, "setIsChatingWithWho = "+isChatingWithWho);
	}

	/**
	 * 发送的单聊消息存本地消息列表，存数据库
	 * 
	 */
	private synchronized void addChatMessageToQueue(ChatMessage chatMessage,
			boolean saveToDatabase) {
		try {
			// 是否存数据库
			if (saveToDatabase) {
				// 添加此消息到数据库中
				chatMessageDao.addChatMessage(chatMessage);
			}
			// 将新聊天消息插入到本地消息列表，按帐号存放
			List<ChatMessage> chatMessageList = null;
			List<ChatMessage> chatMessageListtemp = new ArrayList<ChatMessage>();
			if (unreadChatMessageMap.containsKey(chatMessage.getOwner())) {
				chatMessageList = unreadChatMessageMap.get(chatMessage
						.getOwner());
			} else {
				chatMessageList = new ArrayList<ChatMessage>();
			}
			chatMessageListtemp.addAll(chatMessageList);
			chatMessageList.clear();
			chatMessageList.add(chatMessage);
			chatMessageList.addAll(chatMessageListtemp);// 未读消息顺序应该是新的在最上面，而不是最后面，因为是倒序取的

			unreadChatMessageMap.put(chatMessage.getOwner(), chatMessageList);// update
			if (saveToDatabase) {
				// 从消息选项 卡查询，若此单聊条目存在，则设置为最新聊天条目，否则，将此单聊条目添加到消息选项卡列表中
				// 用来更新chatFragment
				ChatItem rcvItem = null;
				String owner = chatMessage.getOwner();
				if (chatItems == null) {
					chatItems = chatItemDao
							.getAllChatItem(this.userSelfContactsEntity
									.getAccountNum());
				}
				for (ChatItem item : chatItems) {
					// 判断新来的item在已有的item中是否存在
					if (item.getOwner().equals(owner)) {
						rcvItem = item;
						break;
					}
				}
				int unreadnum = 0;
				if (rcvItem != null) {
					unreadnum = rcvItem.getUnread();
					chatItems.remove(rcvItem);
				}
				// 如果不存在，创建一个新的item//全部新建，不然会有数据项为空
				rcvItem = new ChatItem();
				rcvItem.setOwner(owner);
				rcvItem.setUserAccount(this.getTigaseAccount());
				rcvItem.setFriendAccount(chatMessage.getOwner());

				if (friendsInfoMap == null) {
					friendsInfoMap = new HashMap<String, ContactsEntity>();
				}
				ContactsEntity contactsEntity = null;
				if (friendsInfoMap.containsKey(owner)) {
					contactsEntity = friendsInfoMap.get(owner);
				} else {
					contactsEntity = classmateDao.getFriendInfoByAccount(owner,
							this.getTigaseAccount());
					friendsInfoMap.put(owner, contactsEntity);// 缓存好友信息
				}

				if (contactsEntity != null) {
					rcvItem.setName(contactsEntity.getName());
					// 聊天消息的图片通过账号获取联系人的信息获取
					// rcvItem.setIcon(contactsEntity.getPicture());
				} else {
					rcvItem.setName(chatMessage.getOwner());
					// rcvItem.setIcon("");// 避免空指针错误
				}

				rcvItem.setType(ChatItem.PRIVATECHATITEM);
				rcvItem.setLatestMessage(chatMessage.getMessageContent());
				rcvItem.setTime(chatMessage.getTime());
				// 未读消息+1
				// (rcvItem) {
				// 不是自己发送出去的消息
				if (!this.getTigaseAccount().equals(
						chatMessage.getSenderAccount())) {
					rcvItem.setUnread(unreadnum + 1);
					// CYLog.e(TAG, "rcvItem.setUnread= " +
					// rcvItem.getUnread());
				}
				// }

				// 将新来的item放在队列最前面，手机显示，就是列表的顶部
				chatItems.add(0, rcvItem);
				// 将chatItem入库

				chatItemDao.deleteAndSave(rcvItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "addChatMessageToQueue " + e.toString());
		}
	}

	// // /**
	// // * 收到消息每秒通知一次ui
	// // */
	// // private void setTimerTask() {
	// // timer.schedule(new TimerTask() {
	// // @Override
	// // public void run() {
	// // // TODO Auto-generated method stub
	// // if (isMessageCome) {
	// // Bundle bundle = new Bundle();
	// // bundle.putString("from", groupChatRoomName);
	// // messageIntent.putExtras(bundle);
	// // sendBroadcast(messageIntent);
	// // isMessageCome = false;
	// // }
	// // // CYLog.d(TAG,"=====1s=====");
	// // }
	// //
	// // }, 0, 1000);
	// // }
	//

	/**
	 * 更新数据库中的一条消息
	 */
	public void updateChatItemToDb(ChatItem chatItem) {
		try {
			chatItemDao.updateChatItem(chatItem);

			// 将某一个chatitem置顶
			// chatItems.remove(chatItem);
			// chatItems.add(0, chatItem);
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
		}
	}

	/**
	 * 有roster更新，通知UI重新获取联系人
	 */
	public void sendRosterIntentBroadcast() {
		// 先更新联系人列表，再通知UI
		updateContactsOnRoster();
	}

	public void onEventBackgroundThread(Roster roster) {
		this.roster = roster;
		sendRosterIntentBroadcast();
	}

	public void onEventBackgroundThread(FriendAddRecvEntity friendAddRecvEntity) {
		try {
			String userAccount = this.getTigaseAccount();
			// 收到好友添加请求
			if (APPConstant.CMD_PREFIX_FRIEND_ADD_REQUEST
					.equals(friendAddRecvEntity.getCmdType())) {

				// 已经是好友,忽略消息
				String friendAccount = friendAddRecvEntity.getAccountNum();
				if (isMyFriend(friendAccount)) {
					return;
				}

				friendAddIntent.putExtra("cmdtype",
						APPConstant.CMD_PREFIX_FRIEND_ADD_REQUEST);
				friendAddIntent.putExtra("friendAccount", friendAccount);
				friendAddIntent.putExtra("name", friendAddRecvEntity.getName());
				friendAddIntent.putExtra("className",
						friendAddRecvEntity.getClassName());
				this.sendBroadcast(friendAddIntent);
			} else if (APPConstant.CMD_PREFIX_FRIEND_ADD_DECLINE
					.equals(friendAddRecvEntity.getCmdType())) {
				final String friendAccount = friendAddRecvEntity
						.getAccountNum();
				// 拒绝好友添加请求
				if (!isMyFriend(friendAccount)) {
					friendAddIntent.putExtra("cmdtype",
							APPConstant.CMD_PREFIX_FRIEND_ADD_DECLINE);
					friendAddIntent.putExtra("friendAccount", friendAccount);
					friendAddIntent.putExtra("name",
							friendAddRecvEntity.getName());
					friendAddIntent.putExtra("className",
							friendAddRecvEntity.getClassName());
					this.sendBroadcast(friendAddIntent);
					return;
				} else {
					// 好友删除
					ContactsEntity contactsEntity = this
							.getFriendInfoByAccount(friendAccount);
					if (contactsEntity == null) {
						// 刷新页面
						if (this.friendsInfoMap != null
								&& friendsInfoMap.containsKey(friendAccount)) {
							friendsInfoMap.remove(friendAccount);
							this.sendBroadcast(rosterIntent);
						}
						return;
					}

					if ("我的好友".equals(contactsEntity.getClassName())) {
						new Thread() {
							public void run() {
								// 删除好友
								sendFriendAddDecline(friendAccount, 2);
							}
						}.start();
					}
				}
			} else if (APPConstant.CMD_PREFIX_FRIEND_ADD_AGREE
					.equals(friendAddRecvEntity.getCmdType())) {

				// 已经是好友,忽略消息
				final String friendAccount = friendAddRecvEntity
						.getAccountNum();
				if (isMyFriend(friendAccount)) {
					return;
				}

				// 创建好友
				final FriendAddSendEntity friendAddSendEntity = this
						.getFriendAddEntity(friendAccount);
				friendAddSendEntity
						.setCmdType(APPConstant.CMD_PREFIX_FRIEND_ADD_AGREE);
				new Thread() {
					public void run() {
						EventBus.getDefault().post(friendAddSendEntity);

						// 获取好友信息，更新UI
						checkFriendInfoLocalExisted(friendAccount);
					}
				}.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "sendFriendAddBroadcast failed");
		}
	}

	/**
	 * 发送广播之前，检查本地是否有联系人信息，若没有，则从网络获取，存入内存联系人表，陌生人不入库
	 * 
	 * @param friendAccount
	 */
	private void checkFriendInfoLocalExisted(final String friendAccount) {
		try {
			final String userAccount = this.getTigaseAccount();
			final String password = this.getTigasePassword();
			if (friendsInfoMap == null) {
				friendsInfoMap = new HashMap<String, ContactsEntity>();
			}

			if (!friendsInfoMap.containsKey(friendAccount)) {

				ContactsEntity contactsEntity = classmateDao
						.getFriendInfoByAccount(friendAccount, userAccount);
				if (contactsEntity == null) {// web上获取
					// web上获取的好友信息没有带分组,需另外设置
					contactsEntity = LoginUtils.getFriendInfo(userAccount,
							password, friendAccount);

					if (contactsEntity == null) {
						contactsEntity = new ContactsEntity();
						contactsEntity.setUserAccount(userAccount);
						contactsEntity.setAccountNum(friendAccount);
						contactsEntity.setAuthenticated("0");
						contactsEntity.setHasAllClassmates(0);
					} else {
						// 网络获取的联系人，属主不一定正确，没有班级信息
						contactsEntity.setUserAccount(userAccount);

						// 判断是否为班级同学，若是则更新本地信息
						String baseId = contactsEntity.getBaseInfoId();
						if (baseId != null && !baseId.equals("")) {
							String baseIds[] = baseId.split(",");
							int i;
							for (i = 0; i < baseIds.length; ++i) {
								ContactsEntity contactsEntityT = classmateDao
										.getFriendInfoByBaseInfoIds(baseIds[i],
												userAccount);
								if (contactsEntityT != null) {
									contactsEntity.setClassName(contactsEntityT
											.getClassName());
									classmateDao
											.updateContacsEntity(contactsEntity);

									// 更新完数据库后，更新内存
									mContactsEntityMap = classmateDao
											.getAllcontactsEntity(userAccount);
									sendBroadcast(rosterIntent);// 通知UI
									break;
								}
							}

							// 非班级同学
							if (i == baseIds.length) {
								ContactsEntity temp = classmateDao
										.getFriendInfoByAccount(friendAccount,
												userAccount);
								if (temp == null) {
									contactsEntity.setClassName("我的好友");
									classmateDao
											.addContactsEntity(contactsEntity);
								} else {
									classmateDao
											.updateContacsEntity(contactsEntity);
								}
							}
						} else {
							contactsEntity.setClassName("陌生人");
							contactsEntity.setAuthenticated("1");
							contactsEntity.setHasAllClassmates(1);
						}
					}
				}

				friendsInfoMap.put(friendAccount, contactsEntity);// 缓存好友信息
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<ChatItem> getChatItems() {
		return chatItems;
	}

	private void setChatItems(List<ChatItem> chatItems) {
		this.chatItems = chatItems;
	}

	public List<Channel> getChannels() {
		return channels;
	}

	private void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

	// public Map<String, Integer> getUnreadNewsCountMap() {
	// return unreadNewsCountMap;
	// }
	//
	// private void setUnreadNewsCountMap(Map<String, Integer>
	// unreadNewsCountMap) {
	// this.unreadNewsCountMap = unreadNewsCountMap;
	// }

	public ContentValues getValues() {
		return values;
	}

	public void setValues(ContentValues values) {
		this.values = values;
	}

	public int getNumflag() {
		return numflag;
	}

	public void setNumflag(int numflag) {
		this.numflag = numflag;
	}

	private void setMessageIntent(Intent messageIntent) {
		this.messageIntent = messageIntent;
	}

	private void setRosterIntent(Intent rosterIntent) {
		this.rosterIntent = rosterIntent;
	}

	public Map<String, List<ChatMessage>> getUnReadMessageListMap() {
		return unreadChatMessageMap;
	}

	private void setUnReadMessageListMap(
			Map<String, List<ChatMessage>> messageListMap) {
		this.unreadChatMessageMap = messageListMap;
	}

	public Map<String, List<ChatMessage>> getReadedMessageListMap() {
		return readedChatMessageMap;
	}

	private void setReadedMessageListMap(
			Map<String, List<ChatMessage>> messageListMap) {
		this.readedChatMessageMap = messageListMap;
	}

	public String getTigaseAccount() {
		try {
			if (userSelfContactsEntity == null) {
				userSelfContactsEntity = this.getUserSelfContactsEntity();
			}
			return userSelfContactsEntity.getUserAccount();
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "getTigaseAccount " + e.toString());

		}
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getString("USERNAME", "username");
	}

	public String getTigasePassword() {
		try {
			if (userSelfContactsEntity == null) {
				userSelfContactsEntity = this.getUserSelfContactsEntity();
			}
			return userSelfContactsEntity.getPassword();
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "getTigasePassword " + e.toString());
		}

		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getString("PASS", "pass");
	}

	// public String getBaseInfoIds() {
	// try {
	// return userSelfContactsEntity.getBaseInfoId();
	// } catch (Exception e) {
	// e.printStackTrace();
	// CYLog.e(TAG, "getBaseInfoIds " + e.toString());
	// return null;
	// }
	// }

	// public String getNumId() {
	// try {
	// return userSelfContactsEntity.getId();
	// } catch (Exception e) {
	// e.printStackTrace();
	// CYLog.e(TAG, "getNumId " + e.toString());
	// return null;
	// }
	// }
	//
	// public String getIntrestType() {
	// try {
	// return userSelfContactsEntity.getIntrestType();
	// } catch (Exception e) {
	// e.printStackTrace();
	// CYLog.e(TAG, "getIntrestType " + e.toString());
	// return null;
	// }
	// }

	public int getAuthorizedState() {
		return authorizedState;
	}

	public void setAuthorizedState(int authorizedState) {
		this.authorizedState = authorizedState;
	}

	// public String getName() {
	// try {
	// return userSelfContactsEntity.getName();
	// } catch (Exception e) {
	// e.printStackTrace();
	// CYLog.e(TAG, "getName " + e.toString());
	// return null;
	// }
	// }

	// public boolean getIsMQTTConnect() {
	// return isMQTTConnect;
	// }
	//
	// public void setIsMQTTConnect(boolean ismqttconnect) {
	// this.isMQTTConnect = ismqttconnect;
	// }

	// public int getChatServerLoginState() {
	// if (chatMsgService != null) {
	// return chatMsgService.getChatServerLoginState();
	// } else {
	// CYLog.i(TAG, "getChatServerLoginState chatMsgService is null");
	// return APPConstant.LOGIN_NOT_COMPLETE;
	// }
	// }

	/**
	 * 群聊修改以后，后面将不再对外提供获取roster的方法，tigase的东西它自己维护
	 * 
	 * @return
	 */
	public Roster getRoster() {
		if (roster == null) {
			EventbusCMD mEventbusCMD = new EventbusCMD();
			mEventbusCMD.setCMD(EventbusCMD.SEND_ROSTER);
			EventBus.getDefault().post(mEventbusCMD);// 取roster
		}
		CYLog.i(TAG, "roster=" + roster);
		return this.roster;
	}

	// public boolean addGroupChatOfRoomName(String name) {
	// return chatMsgService.addGroupChatOfRoomName(name);
	// }

	// public void setGroupChatInvitationHandler(
	// GroupChatInvitationHandlerEx handler) {
	// groupchatInvitationHandler = handler
	// }
	//
	// public GroupChatInvitationHandlerEx getGroupChatInvitationHandler() {
	// return groupchatInvitationHandler;
	// }

	public Map<String, List<ContactsEntity>> getContactsEntityMap() {
		return mContactsEntityMap;
	}

	private void setContactsEntityMap(
			Map<String, List<ContactsEntity>> mContactsEntityMap) {
		this.mContactsEntityMap = mContactsEntityMap;
	}

	// public List<GroupChatRoom> getGroupChatRoomList(Context mContext) {
	// try {
	// // 后面改
	// // return GroupChat.getGroupChatRoomsByUser(
	// // chatMsgService.getConnection(), mContext,
	// // this.userSelfContactsEntity.getAccountNum());
	// List<GroupChatRoom> groupChatRoomList = new ArrayList<GroupChatRoom>();
	// GroupChatRoom groupChatRoom = new GroupChatRoom();
	// String className = this.userSelfContactsEntity.getClassName();
	// String[] names = className.split(",");
	// if (names.length > 0 && names[0] != null && !names[0].equals("")) {
	// groupChatRoom.setName(names[0]);
	// groupChatRoomList.add(groupChatRoom);
	// return groupChatRoomList;
	// }
	// return null;
	// } catch (Exception e) {
	// e.printStackTrace();
	// return null;
	// }
	//
	// }

	// /**
	// * 获取用户sign
	// */
	// public String getSign() {
	// try {
	// return this.userSelfContactsEntity.getSign();
	// } catch (Exception e) {
	// e.printStackTrace();
	// CYLog.e(TAG, "getSign " + e.toString());
	// return null;
	// }
	// }

	// /**
	// * 拒绝加入聊天室
	// */
	// public void declineJoinChatRoom(String room, String inviter, String arg3)
	// {
	// try {
	// MultiUserChat.decline(this.getConnection(), room, inviter, arg3);
	// } catch (NotConnectedException e) {
	// e.printStackTrace();
	// }
	// }

	// /**
	// * 获取服务器上是否存在某个群聊室
	// */
	// public GroupChatRoom getGroupChatRoomByName(String inputRoomName) {
	// return GroupChat.getGroupChatRoomByName(chatMsgService.getConnection(),
	// inputRoomName);
	// }

	// /**
	// * 创建群聊室
	// */
	// public void createGroupChatRoom(String inputRoomName) {
	// GroupChat.createGroupChatRoom(chatMsgService.getConnection(),
	// inputRoomName);
	// }

	// public MultiUserChat NewMultiUserChat(String roomJid) {
	// return new MultiUserChat(chatMsgService.getConnection(), roomJid);
	// }

	/**
	 * 获取登陆用户所有未认证联系人的姓名
	 * 
	 * @return
	 */
	public String[] getUnAuthenticatedContactsNamesArray() {
		try {
			List<ContactsEntity> contactsList = classmateDao
					.getAllContactsEntityList(this.getTigaseAccount());

			if (contactsList != null) {
				String names[] = new String[contactsList.size()];
				int j = 0;
				for (int i = 0; i < contactsList.size(); ++i) {
					ContactsEntity tmp = contactsList.get(i);
					if (tmp.getAuthenticated().equals("0")) {
						names[j] = tmp.getName();
						++j;
					}
				}
				if (names.length != 0) {
					return names;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "" + e.toString());
			return null;
		}
	}

	/**
	 * 获取本地数据库中的联系人列表，已经分组好
	 * 
	 * @return
	 */
	public Map<String, List<ContactsEntity>> getLocalDbContactsEntityMap() {
		try {
			return classmateDao.getAllcontactsEntity(this.getTigaseAccount());
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "getLocalDbContactsEntityMap " + e.toString());
			return null;
		}
	}

	/**
	 * 是否是好友
	 * 
	 * @param friendAccount
	 * @return
	 */
	public boolean isMyFriend(final String friendAccount) {
		try {
			// 使用本地数据库判断
			if (classmateDao == null) {
				this.classmateDao = new ClassmateDao(this);
			}
			ContactsEntity contactsEntity = classmateDao
					.getFriendInfoByAccount(friendAccount,
							this.getTigaseAccount());
			if (contactsEntity != null) {
				// 本地有此人信息
				return true;
			}

			String jid = null;
			if (friendAccount.endsWith("@" + APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
				jid = friendAccount;
			} else {
				jid = friendAccount + "@" + APPBaseInfo.TIGASE_SERVER_DOMAIN;
			}
			if (roster != null) {
				boolean ret = roster.contains(jid);
				if (ret) {
					new Thread() {
						public void run() {
							checkFriendInfoLocalExisted(friendAccount);
						}
					}.start();

					return ret;
				}
			}

			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 发送添加好友的请求
	 * 
	 * @param friendAccount
	 */
	public void sendFriendAddRequest(String friendAccount) {
		try {
			FriendAddSendEntity friendAddEntity = getFriendAddEntity(friendAccount);
			friendAddEntity
					.setCmdType(APPConstant.CMD_PREFIX_FRIEND_ADD_REQUEST);
			EventBus.getDefault().post(friendAddEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拒绝添加好友的请求或者删除好友
	 * 
	 * @param friendAccount
	 * @cType 0 拒绝好友添加请求 1主动删除好友 2收到被动删除的命令
	 */
	public void sendFriendAddDecline(String friendAccount, int cType) {
		try {
			// 主动删除好友或者被动删除
			FriendAddSendEntity friendAddEntity = getFriendAddEntity(friendAccount);
			if (cType == 1 || cType == 2) {
				friendAddEntity
						.setCmdType(APPConstant.CMD_PREFIX_FRIEND_ADD_DECLINE);
				EventBus.getDefault().post(friendAddEntity);
			}

//			// 删除联系人
//			ClassmateDao classmateDao = new ClassmateDao(
//					this.getApplicationContext());
//			String userAccount = this.getTigaseAccount();
//			classmateDao.deleteContactsEntity(userAccount, friendAccount);
			
			// 删除聊天记录
			String userAccount = this.getTigaseAccount();
			ChatItemDao chatItemDao = new ChatItemDao(
					this.getApplicationContext());
			chatItemDao.deleteChatItem(friendAccount, userAccount,
					ChatItem.PRIVATECHATITEM);
			ChatMessageDao chatMessageDao = new ChatMessageDao(
					this.getApplicationContext());
			chatMessageDao.deleteChatMessages(friendAccount, userAccount,
					ChatMessage.PRIVATECHATMESSAGE);

			try {
				if (chatItems != null) {
					for (int i = 0; i < chatItems.size(); ++i) {
						if (chatItems.get(i).getOwner().equals(friendAccount)) {
							chatItems.remove(i);
						}
					}
				}

				// 删除联系人
				if (classmateDao == null) {
					classmateDao = new ClassmateDao(
						this.getApplicationContext());
				}
				classmateDao.deleteContactsEntity(userAccount, friendAccount);
				
				// 更新完数据库后，更新内存
				mContactsEntityMap = classmateDao
						.getAllcontactsEntity(userAccount);
				sendBroadcast(rosterIntent);// 通知UI 更新联系人页面
			} catch (Exception e) {
			}

			// 通知上层UI
			Bundle bundle = new Bundle();
			// 消息页面只要有消息都要更新，但是联系人页面只有用户图像更新时才需要更新
			bundle.putString("from", "updateUI");// 只做UI图片更新
			messageIntent.putExtras(bundle);
			sendBroadcast(messageIntent);// 通知UI更新,更新chatFragment

			// 拒绝添加或者主动删除好友
			if (cType == 0 || cType == 1) {
				StringBuffer cmdmsg = new StringBuffer();
				cmdmsg.append(APPConstant.CMD_PREFIX_FRIEND_ADD_DECLINE)
						.append(this.getTigaseAccount()).append("_")
						.append(friendAddEntity.getName()).append("_")
						.append(friendAddEntity.getClassName());
				this.sendNotifyAllChatMessage(friendAccount, cmdmsg.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 同意添加好友的请求
	 * 
	 * @param friendAccount
	 */
	public void sendFriendAddAgree(String friendAccount) {
		try {
			FriendAddSendEntity friendAddEntity = getFriendAddEntity(friendAccount);
			friendAddEntity.setCmdType(APPConstant.CMD_PREFIX_FRIEND_ADD_AGREE);
			EventBus.getDefault().post(friendAddEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private FriendAddSendEntity getFriendAddEntity(String friendAccount) {
		try {
			FriendAddSendEntity friendAddEntity = new FriendAddSendEntity();
			ContactsEntity selfContactsEntity = this
					.getUserSelfContactsEntity();
			friendAddEntity.setName(selfContactsEntity.getName());

			StringBuffer className = new StringBuffer();
			String baseId = selfContactsEntity.getBaseInfoId();
			if (this.departmentDao == null) {
				departmentDao = new DepartmentDao(this);
			}
			if (baseId != null && !baseId.equals("")) {
				String baseIds[] = baseId.split(",");
				for (int i = 0; i < baseIds.length; ++i) {
					String fullname = departmentDao
							.getDepartmentFullName(baseIds[i].substring(0, 16));
					String groupName = fullname.substring(
							fullname.lastIndexOf(",") + 1, fullname.length());
					className.append(groupName).append(",");
				}

				friendAddEntity.setClassName(className.substring(0,
						className.lastIndexOf(",")));
			}

			friendAddEntity.setAccountNum(friendAccount);
			return friendAddEntity;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 退出上一次与聊天服务器的连接
	 */
	public void quitLastTigaseConnection() {
		try {
			// 退出后，其他用户可以登录
			resetAuthorizedState();

			EventbusCMD cmd = new EventbusCMD();
			cmd.setCMD(EventbusCMD.QUIT_TIGASE);
			EventBus.getDefault().post(cmd);
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
		}
	}

	/**
	 * 主动退出聊天服务后，登录
	 */
	public void loginTigase() {
		try {
			EventbusCMD cmd = new EventbusCMD();
			cmd.setCMD(EventbusCMD.LOGIN_TIGASE);
			EventBus.getDefault().post(cmd);
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
		}
	}

	// /**
	// * 获取登陆用户的图片url信息
	// *
	// * @return
	// */
	// public String getMyselfPicure() {
	// try {
	// return this.userSelfContactsEntity.getPicture();
	// } catch (Exception e) {
	// e.printStackTrace();
	// CYLog.e(TAG, "getMyselfPicure " + e.toString());
	// return null;
	// }
	// }

	/**
	 * 登陆用户结构，不宜直接暴露出去，后面修改
	 */
	public ContactsEntity getUserSelfContactsEntity() {
		try {
			if (userSelfContactsEntity == null) {
				// 从本地数据库获取用户信息
				SharedPreferences prefs;
				prefs = PreferenceManager.getDefaultSharedPreferences(this
						.getApplicationContext());
				String accountNum = prefs.getString("USERNAME", "");
				if (accountNum != null && !accountNum.equals("")) {
					if (classmateDao == null) {
						classmateDao = new ClassmateDao(this);
					}
					userSelfContactsEntity = this.classmateDao
							.getSelfContactsEntity(accountNum);
				}
			}
			// CYLog.d(TAG, "userSelfContactsEntity.getIntrestType="
			// + userSelfContactsEntity.getIntrestType());
			return userSelfContactsEntity;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ContactsEntity getContactsEntityByName(String name) {
		ContactsEntity nContactsEntity = new ContactsEntity();
		try {
			if (name != null && !name.equals("")) {
				if (classmateDao == null) {
					classmateDao = new ClassmateDao(this);
				}
				nContactsEntity = this.classmateDao.getFriendIDByName(name);
			}
			// CYLog.d(TAG, "userSelfContactsEntity.getIntrestType="
			// + userSelfContactsEntity.getIntrestType());
			return nContactsEntity;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean isUserClickedQuitButton() {
		return userClickedQuitButton;
	}

	public void setUserClickedQuitButton(boolean userClickedQuitButton) {
		this.userClickedQuitButton = userClickedQuitButton;
	}

	// // 根据baseInfoId获取联系人班级信息
	// public String getDepartmentByBaseInfoId(String baseinfoId) {
	// String departmentInfo = departmentDao.getDepartment(baseinfoId);
	// String[] spitDapartmentInfo = departmentInfo.split(",");
	// return spitDapartmentInfo[spitDapartmentInfo.length - 1];
	// }

	/**
	 * 个人信息更新，并通知所有的好友
	 * 
	 * @param selfContactsEntity
	 *            notifyAllFriends 是否通知所有的好友，更新本人信息
	 */
	public synchronized void updateSelfContactsEntity(
			ContactsEntity selfContactsEntity, boolean notifyAllFriends) {
		try {
			if (classmateDao == null) {
				classmateDao = new ClassmateDao(this);
			}
			classmateDao.updateSelfContactsEntity(selfContactsEntity);

			if (notifyAllFriends) {
				// 通知所有认证过的好友
				final String userAccount = this.getTigaseAccount();
				final List<ContactsEntity> contactsEntityList = classmateDao
						.getAllContactsEntityList(userAccount);
				for (int i = 0; i < contactsEntityList.size(); ++i) {
					final String accountNum = contactsEntityList.get(i)
							.getAccountNum();
					String auth = contactsEntityList.get(i).getAuthenticated();
					if (auth == null || !auth.equals("1")) {
						continue;// 没有认证
					}
					if (accountNum == null || accountNum.equals("")) {
						continue;// 没有账号
					}
					StringBuilder message = new StringBuilder();
					message.append(APPConstant.CMD_PREFIX_UPDATE_FRIEND_INFO)
							.append(userAccount);
					sendNotifyAllChatMessage(accountNum, message.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "updateSelfContactsEntity : " + e);
		}
	}

	/**
	 * 给账号为accountNum的好友发送通知消息
	 * 
	 * @param accountNum
	 * @param message
	 */
	private void sendNotifyAllChatMessage(String accountNum, String message) {
		try {
			String receiver = null;
			if (accountNum.endsWith("@" + APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
				receiver = accountNum.substring(
						0,
						accountNum.lastIndexOf("@"
								+ APPBaseInfo.TIGASE_SERVER_DOMAIN));
			} else {
				receiver = accountNum;
			}

			ChatMessageSendEntity mSendChatMessage = new ChatMessageSendEntity();
			mSendChatMessage.setReceiver(receiver);
			mSendChatMessage.setMessage(message);
			mSendChatMessage.setMessageType("notifyall");// 消息类型标记
			EventBus.getDefault().post(mSendChatMessage);// 发消息，等待返回
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除登陆用户的本地记录信息，没有删联系人信息
	 * 
	 * @param userAccount
	 */
	public boolean deleteSelfContactsEntity(String userAccount) {
		try {
			if (classmateDao == null) {
				classmateDao = new ClassmateDao(this);
			}
			return classmateDao.deleteSelfContactsEntity(userAccount);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 更新本地个人信息表的groupName字段并将群组记录添加到本地
	 * 
	 * @param groupChatRoomEntity
	 */
	public void updateLocalGroupChatData(GroupChatRoomEntity groupChatRoomEntity) {
		if (groupChatDao == null) {
			groupChatDao = new GroupChatDao(this);
		}

		String groupId = groupChatRoomEntity.getGroupId();
		// 更新本地个人信息表
		updateSelfGroupInfoToUserProfile(groupId);

		// 更新本地群聊表
		if (!groupChatDao.isGroupChatRoomEntityExisted(
				groupChatRoomEntity.getUserAccount(), groupId)) {
			groupChatDao.addGroupChatEntity(groupChatRoomEntity);
		} else {
			groupChatDao.updateGroupChatEntity(groupChatRoomEntity);
		}

		groupChatRoomMap = null;
		this.checkGroupChatRoomMap();
		sendBroadcast(rosterIntent); // 通知UI更新
	}

	/**
	 * 删除本地与此聊天室相关的所有信息
	 */
	public synchronized void deleteLocalGroupChatData(String groupId) {
		if (groupChatDao == null) {
			groupChatDao = new GroupChatDao(this);
		}

		// 更新本地个人信息表
		deleteSelfGroupInfoToUserProfile(groupId);

		// 更新本地群聊表
		if (groupChatDao.isGroupChatRoomEntityExisted(getTigaseAccount(),
				groupId)) {
			groupChatDao.deleteGroupChatEntity(getTigaseAccount(), groupId);
		}

		groupChatRoomMap = null;
		this.checkGroupChatRoomMap();

		// 删除本聊天室相关的ChatItem,并更新内存
		chatItemDao.deleteChatItem(groupId, this.getTigaseAccount(),
				ChatItem.GROUPCHATITEM);
		chatItems = null;
		this.checkChatItemList();

		// 删除本聊天室的所有历史消息,并更新内存
		chatMessageDao.deleteChatMessages(groupId, this.getTigaseAccount(),
				ChatMessage.GROUPCHATMESSAGE);
		for (String key : unreadChatMessageMap.keySet()) {
			if (key.equals(groupId)) {
				if (unreadChatMessageMap.get(key) != null
						&& unreadChatMessageMap.get(key).size() > 0) {
					if (unreadChatMessageMap.get(key).get(0).getType() == ChatMessage.GROUPCHATMESSAGE) {
						unreadChatMessageMap.remove(key);
					}
				}
			}
		}
		for (String key : readedChatMessageMap.keySet()) {
			if (key.equals(groupId)) {
				if (readedChatMessageMap.get(key) != null
						&& readedChatMessageMap.get(key).size() > 0) {
					if (readedChatMessageMap.get(key).get(0).getType() == ChatMessage.GROUPCHATMESSAGE) {
						unreadChatMessageMap.remove(key);
					}
				}
			}
		}

		// 更新UI
		sendBroadcast(rosterIntent); // 通知UI更新,更新contactsFragment
		// 通知上层UI
		Bundle bundle = new Bundle();
		// 消息页面只要有消息都要更新，但是联系人页面只有用户图像更新时才需要更新
		bundle.putString("from", "updateUI");// 只做UI图片更新
		messageIntent.putExtras(bundle);
		sendBroadcast(messageIntent);// 通知UI更新,更新chatFragment
	}

	/*
	 * 本人创建聊天室,isDefaultGroupChatRoom为false表示创建的是一般聊天室，否则则是默认班级聊天室
	 */
	public void addGroupChatRoom(String roomName,
			Map<String, Integer> invitees, boolean isDefaultGroupChatRoom) {
		try {
			// 下一个可用的聊天室id
			String nextAvailableId = null;
			// 第一步：确定新建聊天室id
			if (isDefaultGroupChatRoom) {
				nextAvailableId = departmentDao.getDepartmentId(roomName);
			} else {
				nextAvailableId = getAvailableIdOnCreatingGroup();
			}

			GroupChatRoomEntity groupChatRoomEntity = new GroupChatRoomEntity();
			groupChatRoomEntity.setGroupId(nextAvailableId);
			String userAccount = getTigaseAccount();
			groupChatRoomEntity.setCreaterAccount(userAccount);
			groupChatRoomEntity.setTargetOccupantsMap(invitees);// 被邀请的人
			groupChatRoomEntity.setGroupName(roomName);
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put(userAccount, 1);// 自己作为管理员加入
			groupChatRoomEntity.setOccupantsMap(map);
			groupChatRoomEntity.setUserAccount(getTigaseAccount()); // 这个字段和Password字段是用来更新Web服务器失败后的恢复操作
			groupChatRoomEntity.setPassword(getTigasePassword());
			groupChatRoomEntity
					.setFunctionType(GroupChatRoomEntity.FUNCTYPE_ADD);
			groupChatRoomEntity
					.setSyncType(GroupChatRoomEntity.SYNCTYPE_ADD_ROOM);

			// 第二步：在tigase上创建聊天室节点,等待操作执行结果
			groupChatFuncStatus = new GroupChatFuncStatusEntity();
			EventBus.getDefault().post(groupChatRoomEntity);

			waitEventBusResultOfGroupFunc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 邀请人加入聊天室
	 */
	public void inviteGroupChatRoom(String roomId, Map<String, Integer> invitees) {
		// 发送邀请
		String userAccount1 = userSelfContactsEntity.getUserAccount();
		String name1 = userSelfContactsEntity.getName();
		GroupChatRoomEntity groupChatRoomEntity = groupChatDao
				.getGroupChatRoomByGroupId(getTigaseAccount(), roomId);
		groupChatRoomEntity.setTargetOccupantsMap(invitees);
		String groupId1 = groupChatRoomEntity.getGroupId();
		String groupName1 = groupChatRoomEntity.getGroupName();
		StringBuffer buf1 = new StringBuffer();
		buf1.append(APPConstant.CMD_PREFIX_GROUPCHAT_INVITE)
				.append(userAccount1).append("_").append(name1).append("_")
				.append(groupId1).append("_").append(groupName1);
		sendGroupNotifyCmdMessage(groupChatRoomEntity, buf1.toString());
	}

	/**
	 * 获取当前可以创建的群组id
	 * 
	 * @return
	 */
	private String getAvailableIdOnCreatingGroup() {
		StringBuffer buf = new StringBuffer();
		try {
			if (this.userSelfContactsEntity == null) {
				userSelfContactsEntity = this.getUserSelfContactsEntity();
			}

			String userAccount = userSelfContactsEntity.getUserAccount();
			buf.append(userAccount).append("-");
			String groupIds = userSelfContactsEntity.getGroupName();
			if (groupIds == null || groupIds.equals("")) {
				buf.append("0");
			} else {
				String groupArray[] = groupIds.split(",");
				List<String> idList = new ArrayList<String>();
				for (int i = 0; i < groupArray.length; ++i) {
					if (groupArray[i].startsWith(userAccount)) {
						idList.add(groupArray[i]);
					}
				}

				if (idList.size() == 0) {
					buf.append("0");
				} else {
					Collections.sort(idList);
					String str = idList.get(idList.size() - 1);
					int num = Integer
							.parseInt(str.substring(str.indexOf("-") + 1)); // 注：此分割符不要和群聊命令消息中的特殊字符冲突，如#,_
					++num;
					buf.append(num);
				}
			}
			return buf.toString();
		} catch (Exception e) {
			CYLog.e(TAG, "get available groupid error : " + e);
			e.printStackTrace();
			Random random = new Random();
			int ret = random.nextInt() % 1000;
			if (ret < 0) {
				ret = -ret;
			}
			ret += 1000;
			return buf.append(ret).toString();
		}
	}

	// public void deleteGroupChatRoomEx(String roomId) {
	// //发一个删除聊天室的广播
	//
	// }

	/**
	 * 获取聊天室里面除自己以外所有成员信息，包括id,姓名,图片
	 * 
	 * @param groupId
	 * @return
	 */
	public List<ContactsEntity> getAllOccupantsOfRoom(String groupId) {
		this.checkGroupChatRoomMap();
		try {
			List<ContactsEntity> contactsEntityList = new ArrayList<ContactsEntity>();
			GroupChatRoomEntity groupChatRoomEntity = this.groupChatRoomMap
					.get(groupId);
			Map<String, Integer> occupantsMap = groupChatRoomEntity
					.getOccupantsMap();
			String userAccount = this.getTigaseAccount();
			for (Entry<String, Integer> key : occupantsMap.entrySet()) {
				String friendAccount = key.getKey();
				ContactsEntity contactsEntity = null;
				// 过滤掉自己
				if (friendAccount.equals(userAccount)) {
					continue;
				}

				if (this.isMyFriend(friendAccount)) {
					// 好友信息
					contactsEntity = this.getFriendInfoByAccount(friendAccount);
				} else {
					// 陌生人信息
					contactsEntity = groupChatDao.getFriendInfoByAccount(
							userAccount, friendAccount);
				}

				if (contactsEntity != null && contactsEntity.getAuthenticated().equals("1")) {
					contactsEntityList.add(contactsEntity);
				}
			}

			return contactsEntityList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 收到邀请后同意加入聊天室
	 * 
	 * @param roomName
	 * @param participants
	 */
	public void joinGroupChatRoom(String groupId) {
		CYLog.i(TAG, "groupId=" + groupId);
		// 丢弃
		if (groupId == null || groupId.equals("")) {
			CYLog.i(TAG, "数据格式不正确!");
			return;
		}

		jobManager.addJobInBackground(new JoinGroupChatRoomJob(
				DataCenterManagerService.this, groupId));
	}

	/**
	 * 更新自己的group信息，返回是否更新成功
	 * 
	 * @param groupId
	 */
	public boolean updateSelfGroupInfoToUserProfile(String groupId) {
		try {
			final String userAccount = this.getTigaseAccount();
			if (userSelfContactsEntity == null) {
				userSelfContactsEntity = this
						.getSelfContactsEntity(userAccount);
			}
			String groupIdStr = userSelfContactsEntity.getGroupName();

			StringBuffer buf = new StringBuffer();
			Set<String> groupIdSet = new HashSet<String>();
			if (groupIdStr != null && !groupIdStr.equals("")) {
				String groupIds[] = groupIdStr.split(",");
				for (int i = 0; i < groupIds.length; ++i) {
					if (!groupIdSet.contains(groupIds[i])) {
						groupIdSet.add(groupIds[i]);
						buf.append(groupIds[i]).append(",");
					}
				}
			}

			if (groupIdSet.contains(groupId)) {
				return false;// 已经加入过了
			} else {
				buf.append(groupId);
			}

			userSelfContactsEntity.setGroupName(buf.toString());
			this.updateSelfContactsEntity(userSelfContactsEntity, false);

			// //group信息上传web +++lqg+++修改为统一有管理员通知服务器来同步
			// final String password = this.getTigasePassword();
			// final String groupIdResult = buf.toString();
			// new Thread() {
			// @Override
			// public void run() {
			// boolean flag =
			// LoginUtils.syncGroupInfoToUserProfile(groupIdResult, userAccount,
			// password);
			// while (!flag) {
			// sleep(APPConstant.CONNECTION_CHECK_INTERVAL / 5);
			// flag = LoginUtils.syncGroupInfoToUserProfile(groupIdResult,
			// userAccount, password);
			// CYLog.i(TAG, "syncGroupInfoToUserProfile--- : " + groupIdResult);
			// }
			// }
			// }.start();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// 聊天室踢人,退群，删群(群主退群即为删群)
	public void kickByUserId(String roomId, Map<String, Integer> targets) {
		// 群主发送踢人消息，里面包含要踢的人名单
		CYLog.i(TAG, "kickByUserId is called!");
		if (targets == null || targets.size() == 0) {
			CYLog.i(TAG, "数据格式不正确!");
			return;
		}

		StringBuffer cmdStrBuf = new StringBuffer();
		cmdStrBuf.append(APPConstant.CMD_PREFIX_GROUPCHAT_KICK);
		for (String key : targets.keySet()) {
			cmdStrBuf.append(key).append("_");
		}
		String cmdStr = cmdStrBuf.substring(0, cmdStrBuf.length() - 1)
				.toString();
		sendGroupChatMessage(roomId, cmdStr, true);
	}

	// 取消订阅聊天室
	public void unsubscribeGroupChatRoom(String roomId) {
		GroupChatRoomEntity roomEntity = new GroupChatRoomEntity();
		roomEntity.setGroupId(roomId);
		roomEntity.setFunctionType(GroupChatRoomEntity.FUNCTION_UBSUBSSRIBE);
		CYLog.i(TAG, "即将取消订阅聊天室!");

		// 发送取消订阅聊天室消息
		groupChatFuncStatus = new GroupChatFuncStatusEntity();
		EventBus.getDefault().post(roomEntity);

		// 等待结果
		waitEventBusResultOfGroupFunc();
	}

	/**
	 * 将groupId从本账号的groupName字段中删除
	 */
	private boolean deleteSelfGroupInfoToUserProfile(String groupId) {
		try {
			// 个人信息表的groupName现在为id列表
			String[] currentGroupId = userSelfContactsEntity.getGroupName()
					.split(",");
			Set<String> groupNameSet = new HashSet<String>(
					Arrays.asList(currentGroupId));
			if (groupNameSet.contains(groupId))
				groupNameSet.remove(groupId);
			StringBuffer newGroupNameBuf = new StringBuffer();
			for (String s : groupNameSet) {
				newGroupNameBuf.append(s).append(",");
			}
			if (newGroupNameBuf != null && newGroupNameBuf.length() != 0) {
				newGroupNameBuf.substring(0, newGroupNameBuf.length() - 1);
			}
			userSelfContactsEntity.setGroupName(newGroupNameBuf.toString());
			updateSelfContactsEntity(userSelfContactsEntity, false);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 处理创建或者加入群聊室后的数据同步问题
	 */
	public synchronized void waitEventBusResultOfGroupFunc() {
		while (groupChatFuncStatus.getFuncStatus() == GroupChatFuncStatusEntity.GROUPCHAT_UNRET) {
			CYLog.i(TAG, "正在等待聊天室函数执行返回结果!");
		}

		// 得到了函数执行的返回结果，丢到handler中处理
		android.os.Message msg = new android.os.Message();
		msg.what = groupChatFuncStatus.getFuncStatus();
		msg.obj = groupChatFuncStatus.getGroupChatRoomEntity();
		groupChatHandler.sendMessage(msg);
	}

	/**
	 * 群聊室信息同步到web服务器 使用Ion库版本
	 */
	public void executeGroupInfoSync(final GroupChatRoomEntity roomEntity)
			throws Exception {
		String accountNum = roomEntity.getUserAccount();
		String password = roomEntity.getPassword();

		JsonObject jsonStr = new JsonObject();
		JsonObject jsonContentStr = new JsonObject();
		jsonContentStr.addProperty("accountNum", accountNum);
		jsonContentStr.addProperty("password", password);

		// 如果为空则新建一条记录
		String groupId = roomEntity.getGroupId();
		if (groupId != null && !groupId.equals("")) {
			jsonContentStr.addProperty("groupId", groupId);
		}

		String createrAccount = roomEntity.getCreaterAccount();
		if (createrAccount != null && !createrAccount.equals("")) {
			jsonContentStr.addProperty("createrAccount", createrAccount);
		}

		jsonContentStr.addProperty("type", roomEntity.getSyncType());

		jsonContentStr.addProperty("groupName", roomEntity.getGroupName());
		jsonContentStr.addProperty("description", roomEntity.getDescription());
		jsonContentStr.addProperty("subject", roomEntity.getSubject());

		String adminsAccount = roomEntity.getAdministratersStr();
		if (adminsAccount != null) {
			jsonContentStr.addProperty("adminsAccount", adminsAccount);
		}

		String membersAccount = roomEntity.getNormalMembersStr();
		if (membersAccount != null) {
			jsonContentStr.addProperty("membersAccount", membersAccount);
		}
		jsonStr.addProperty("command",
				APPConstant.USER_PROFILE_UPDATE_GROUP_INFO);
		jsonStr.add("content", jsonContentStr);

		String path = APPConstant.getURL()
				+ "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";
		String paramJson = URLEncoder.encode(jsonStr.toString());

		JsonObject result = Ion.with(context)
				.load(path + "?jsonStr=" + paramJson).asJsonObject().get();

		boolean success = result.get("success").getAsBoolean();
		if (success) {
			CYLog.i(TAG, "更新聊天室到web服务器上成功!");
		} else {
			CYLog.i(TAG, "更新聊天室到web服务器上失败!");
		}
	}

	// /**
	// * 群聊室信息同步到web服务器
	// * 使用httpClent库版本
	// */
	// protected void executeGroupInfoSync(final GroupChatRoomEntity roomEntity)
	// {
	// String accountNum = roomEntity.getUserAccount();
	// String password = roomEntity.getPassword();
	//
	// try {
	// JSONObject jsonStr = new JSONObject();
	// JSONObject jsonContentStr = new JSONObject();
	// jsonContentStr.put("accountNum", accountNum);
	// jsonContentStr.put("password", password);
	//
	// // 如果为空则新建一条记录
	// String groupId = roomEntity.getGroupId();
	// if (groupId != null && !groupId.equals("")) {
	// jsonContentStr.put("groupId", groupId);
	// }
	//
	// String createrAccount = roomEntity.getCreaterAccount();
	// if (createrAccount != null && !createrAccount.equals("")){
	// jsonContentStr.put("createrAccount", createrAccount);
	// }
	//
	// jsonContentStr.put("type", roomEntity.getSyncType());
	//
	// jsonContentStr.put("groupName", roomEntity.getGroupName());
	// jsonContentStr.put("description",
	// roomEntity.getDescription());
	// jsonContentStr.put("subject", roomEntity.getSubject());
	//
	// String adminsAccount = roomEntity.getAdministratersStr();
	// if (adminsAccount != null) {
	// jsonContentStr.put("adminsAccount", adminsAccount);
	// }
	//
	// String membersAccount = roomEntity.getNormalMembersStr();
	// if (membersAccount != null) {
	// jsonContentStr.put("membersAccount", membersAccount);
	// }
	// jsonStr.put("command", APPConstant.USER_PROFILE_UPDATE_GROUP_INFO);
	// jsonStr.put("content", jsonContentStr);
	//
	// HttpClient client = new DefaultHttpClient();
	// CYLog.i(TAG, "ready to send GroupChatRoom to web! "
	// + roomEntity.getGroupName());
	//
	// String path = APPConstant.getURL()
	// +
	// "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";
	//
	// String paramJson = URLEncoder.encode(jsonStr.toString());
	//
	// HttpGet httpGet = new HttpGet(path + "?jsonStr=" + paramJson);
	// HttpResponse response = client.execute(httpGet);
	//
	// InputStream in = response.getEntity().getContent();
	// byte[] resultBytes = StreamUtils.getBytes(in);
	// String resultJson = new String(resultBytes);
	//
	// CYLog.i(TAG, resultJson);
	//
	// // 解析结果字符串
	// JSONObject jsonObject = new JSONObject(resultJson);
	// boolean success = jsonObject.getBoolean("success");
	// if (success) {
	// CYLog.i(TAG, "更新聊天室到web服务器上成功!");
	// } else {
	// CYLog.i(TAG, "更新聊天室到web服务器上失败!");
	// }
	// } catch (Exception e) {
	// CYLog.i(TAG, "更新聊天室到web服务器上失败!");
	// e.printStackTrace();
	// try {
	// throw e;
	// } catch (Exception e1) {
	// e1.printStackTrace();
	// }
	// }
	// }

	// /**
	// * 检查是否有遗留的群聊同步消息未执行成功，执行，并删除序列化文件
	// */
	// private void checkGroupInfoSyncDirectory() {
	// try {
	// String dir = SearchSuggestionProvider.pathStr + File.separator
	// + "update_group_info";
	// File notifyFilePath = new File(dir);
	// if (!notifyFilePath.exists()) {
	// return;// 没有目录，直接返回
	// }
	//
	// File[] files = notifyFilePath.listFiles();
	// for (File file : files) {
	// if (!file.isDirectory()) {
	// try {
	// FileInputStream fs = new FileInputStream(file);
	// ObjectInputStream os = new ObjectInputStream(fs);
	// GroupChatRoomEntity groupChatRoomEntity = (GroupChatRoomEntity) os
	// .readObject();
	// os.close();
	//
	// file.delete();// 删除文件
	// executeGroupInfoSync(groupChatRoomEntity);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// } else {
	// file.delete();
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// CYLog.e(TAG, "checkNotifyMsgDirectory failed : " + e);
	// }
	// }

	/**
	 * 获取本账号所有的群组信息，若没有则需启动线程从网络获取，handler回调
	 */
	public List<GroupChatRoomEntity> getAllGroupChatRoomEntityList() {
		if (groupChatDao == null) {
			groupChatDao = new GroupChatDao(this);
		}

		String userAccount = this.getTigaseAccount();
		List<GroupChatRoomEntity> groupChatRoomEntityList = groupChatDao
				.getAllGroupChatRoomEntityList(userAccount);

		if (groupChatRoomEntityList == null
				|| groupChatRoomEntityList.size() == 0) {
			if (classmateDao == null) {
				classmateDao = new ClassmateDao(context);
			}
			ContactsEntity selfContactsEntity = classmateDao
					.getSelfContactsEntity(userAccount);

			if (selfContactsEntity != null) {
				jobManager.addJobInBackground(new ResetGroupChatRoomInfoJob(
						DataCenterManagerService.this, selfContactsEntity
								.getGroupName()));
			}
		}
		return groupChatRoomEntityList;
	}

	/**
	 * 发送普通群聊消息 isCmd 是否为特殊的命令消息
	 */
	public boolean sendGroupChatMessage(String groupId, String messageBody,
			boolean isCmd) {
		if (messageBody == null || messageBody.equals("")) {
			return false;
		}

		sendSucc = false;
		sendStatus = false;
		ChatMessageSendEntity mSendChatMessage = new ChatMessageSendEntity();
		mSendChatMessage.setReceiver(groupId);

		String userAccount = this.getTigaseAccount();
		// 普通消息前面带上账号，姓名
		if (!isCmd) {
			// 业务逻辑在数据中心处理
			if (userSelfContactsEntity == null) {
				userSelfContactsEntity = this.getUserSelfContactsEntity();
			}
			String name = userSelfContactsEntity.getName();
			StringBuffer buf = new StringBuffer();
			buf.append(userAccount).append("_").append(name).append("_")
					.append(messageBody);
			mSendChatMessage.setMessage(buf.toString());
		} else {
			mSendChatMessage.setMessage(messageBody);
		}

		mSendChatMessage.setMessageType("group");
		EventBus.getDefault().post(mSendChatMessage);

		// 发消息，等待返回
		new Handler().postDelayed(new Runnable() {
			public void run() {
				// execute the task
				if (!sendStatus) {
					sendStatus = true;
				}
			}
		}, 2000);

		while (!sendStatus) {
			// CYLog.i(TAG, "sendSucc=" + sendSucc);
		}
		sendStatus = false;

		// 命令消息不显示也不入库
		if (!isCmd) {
			// 将文本消息封装成包含发送此消息账号、发送时间、id、类型等的ChatMessage对象
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setMessageContent(messageBody);
			chatMessage.setMid(UUID.randomUUID().toString());
			chatMessage.setOwner(groupId);
			chatMessage.setUserAccount(userAccount);
			chatMessage.setSenderAccount(userAccount);
			chatMessage.setRecvAccount(groupId);
			// 设置此消息类型为群聊消息
			chatMessage.setType(ChatMessage.GROUPCHATMESSAGE);
			chatMessage.setTime(new Date(System.currentTimeMillis()));
			chatMessage.setIsRead(0);// 设置为未读消息
			chatMessage.setSendSucc(sendSucc);
			// 本地不入库时，发送的群聊消息，不主动放本地内存消息列表，和服务器推送的消息一起处理
			addGroupChatMessageToQueue(chatMessage, sendSucc);

			// 通知上层UI
			Bundle bundle = new Bundle();
			bundle.putString("from", groupId);
			messageIntent.putExtras(bundle);
			sendBroadcast(messageIntent);
		}

		if (sendSucc == false) {
			if (groupChatDao == null) {
				groupChatDao = new GroupChatDao(this);
			}

			if (isCmd) {
				// 命令消息失败+++lqg+++ 后续添加补救措施
				CYLog.i(TAG, " group cmd msg failed");
			} else {
				// 普通群聊消息失败，本地没有群表信息
				CYLog.i(TAG, " group msg failed");
				if (!groupChatDao.isGroupChatRoomEntityExisted(userAccount,
						groupId)) {
					CYLog.i(TAG, " group no recorde local ");
				}
			}
		}

		return sendSucc;
	}

	/**
	 * 获取本地的群名
	 * 
	 * @param groupId
	 * @return
	 */
	private String getGroupChatRoomName(String groupId) {
		try {
			this.checkGroupChatRoomMap();
			GroupChatRoomEntity groupChatRoomEntity = this.groupChatRoomMap
					.get(groupId);
			return groupChatRoomEntity.getGroupName();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 处理获取到的群聊消息，包括一般群聊消息和群聊管理消息
	 */
	public void onEventBackgroundThread(GroupChatMessage groupChatMessage) {
		CYLog.i(TAG, "存储获取到的群聊消息!");
		checkGroupChatRoomMap();

		String groupId = groupChatMessage.getGroupId();
		String message = groupChatMessage.getMessage();
		CYLog.i(TAG, "groupId=" + groupId + "   message=" + message);

		boolean ret = dealGroupCmdMessage(groupId, message);
		if (ret) {
			return;// 命令消息
		}

		// 下面处理普通的群聊消息，要过滤掉发送给自己的普通群聊消息
		String userAccount = this.getTigaseAccount();
		String password = this.getTigasePassword();
		try {
			// 取出消息内容
			int index = message.indexOf("_");
			String from = message.substring(0, index);
			String message2 = message.substring(index + 1);
			int index2 = message2.indexOf("_");
			String name = message2.substring(0, index2);
			String messageBody = message2.substring(index2 + 1);

			// 过滤掉发给本人的普通群聊消息
			if (userAccount.equals(from)) {
				return;
			}

			if (messageBody.startsWith(APPConstant.CMD_PREFIX_FILE_SEND)) {
				String url = messageBody.replaceAll(
						APPConstant.CMD_PREFIX_FILE_SEND, "");
				new DownloadFileTask(url, null, null, handler).execute("");
			}

			// 本地没有此人信息
			if (getFriendInfoByAccount(from) == null) {
				// 在本地设置默认的信息
				ContactsEntity contactsEntity;
				contactsEntity = new ContactsEntity();
				contactsEntity.setUserAccount(userAccount);
				contactsEntity.setAccountNum(from);
				contactsEntity.setName(name);
				contactsEntity.setHasAllClassmates(0);
				contactsEntity.setPicture("");
				groupChatDao.addContactsEntity(contactsEntity);

				updateGroupChatMembersFromWeb(userAccount, password, from);
			}

			// 将文本消息封装成包含发送此消息账号、发送时间、id、类型等的ChatMessage对象
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setMessageContent(messageBody);
			chatMessage.setMid(UUID.randomUUID().toString());
			chatMessage.setOwner(groupId);
			chatMessage.setUserAccount(userAccount);
			chatMessage.setSenderAccount(from);
			chatMessage.setRecvAccount(userAccount);
			// 设置此消息类型为群聊消息
			chatMessage.setType(ChatMessage.GROUPCHATMESSAGE);
			chatMessage.setTime(new Date(System.currentTimeMillis()));
			chatMessage.setIsRead(0);// 设置为未读消息
			chatMessage.setSendSucc(true);// 接收到的群聊消息始终成功

			// 放入内存消息列表，入库
			addGroupChatMessageToQueue(chatMessage, true);

			isMessageCome = true;

			// 消息通知
			if (friendsInfoMap == null) {
				friendsInfoMap = new HashMap<String, ContactsEntity>();
			}
			String groupName = name;
			if (groupChatRoomMap.containsKey(groupId)
					&& groupChatRoomMap.get(groupId) != null) {
				groupName = groupChatRoomMap.get(groupId).getGroupName();
			} else {
				GroupChatRoomEntity friend = groupChatDao
						.getGroupChatRoomByGroupId(getTigaseAccount(), groupId);
				if (friend != null) {
					groupName = friend.getGroupName();
					if (groupName == null || groupName.equals("")) {
						groupName = name;
					}
					groupChatRoomMap.put(groupId, friend);
				}
			}
			// 消息通知
			notifyMainUIMsgCome(groupName, messageBody);

			// 通知上层UI
			Bundle bundle = new Bundle();
			bundle.putString("from", groupId);
			messageIntent.putExtras(bundle);
			sendBroadcast(messageIntent);
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "resolveGroupChatMsg " + e.toString());
		}
	}

	/**
	 * 处理群组命令消息, 返回是否为命令消息
	 * 
	 * @param groupId
	 * @param msg
	 */
	private boolean dealGroupCmdMessage(String groupId, String msg) {
		try {
			if (!msg.startsWith(APPConstant.CMD_PREFIX)) {
				return false;// 非命令消息
			}

			if (msg.startsWith(APPConstant.CMD_PREFIX_GROUPCHAT_ACCEPT_INVITE)) {
				CYLog.i(TAG, "收到被邀请者的答复!被邀请者同意加入聊天室");

				jobManager.addJobInBackground(new ReceiveInviteeAcceptReplyJob(
						DataCenterManagerService.this, groupId, msg));

				return true;

			} else if (msg
					.startsWith(APPConstant.CMD_PREFIX_GROUPCHAT_DECLINE_INVITE)) {
				CYLog.i(TAG, "收到被邀请者的答复!被邀请者拒绝加入聊天室");
				return true;

			} else if (msg.startsWith(APPConstant.CMD_PREFIX_GROUPCHAT_KICK)) {
				CYLog.i(TAG, "收到了踢人命令!");

				jobManager.addJobInBackground(new KickGroupChatRoomMemberJob(
						DataCenterManagerService.this, groupId, msg));

				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 检查群组表，自动重新从本地数据库导入
	 */
	public void checkGroupChatRoomMap() {
		if (groupChatRoomMap == null) {
			if (groupChatDao == null) {
				groupChatDao = new GroupChatDao(this);
			}
			// 本地重置群聊表
			groupChatRoomMap = groupChatDao.getAllGroupChatRoomEntityMap(this
					.getTigaseAccount());
			if (groupChatRoomMap == null) {
				groupChatRoomMap = new HashMap<String, GroupChatRoomEntity>();
			}
		}
	}

	/**
	 * 检查ChatItem表，自动重新从本地数据库导入
	 */
	private void checkChatItemList() {
		if (chatItems == null) {
			if (chatItemDao == null) {
				chatItemDao = new ChatItemDao(this);
			}
			// 重置内存中的chatItems
			chatItems = chatItemDao.getAllChatItem(this.userSelfContactsEntity
					.getAccountNum());
		}
	}

	/**
	 * 发送的群聊消息存本地消息列表，存数据库
	 */
	private synchronized void addGroupChatMessageToQueue(
			ChatMessage chatMessage, boolean saveToDatabase) {
		try {
			if (saveToDatabase) {
				// 添加此消息到数据库中
				chatMessageDao.addChatMessage(chatMessage);
			}

			// 将新聊天消息插入到本地消息列表
			List<ChatMessage> chatMessageList = null;
			List<ChatMessage> chatMessageListtemp = new ArrayList<ChatMessage>();
			String groupId = chatMessage.getOwner();
			if (unreadChatMessageMap.containsKey(groupId)) {
				chatMessageList = unreadChatMessageMap.get(groupId);
			} else {
				chatMessageList = new ArrayList<ChatMessage>();
			}
			chatMessageListtemp.addAll(chatMessageList);
			chatMessageList.clear();
			chatMessageList.add(chatMessage);
			chatMessageList.addAll(chatMessageListtemp);// 未读消息顺序应该是新的在最上面，而不是最后面，因为是倒序取的
			unreadChatMessageMap.put(groupId, chatMessageList);// update
			if (saveToDatabase) {
				// 从消息选项 卡查询，若此群聊条目存在，则设置为最新聊天条目，否则，将此群聊条目添加到消息选项卡列表中
				// 用来更新chatFragment
				ChatItem foundItem = null;
				if (chatItems == null) {
					chatItems = chatItemDao
							.getAllChatItem(this.userSelfContactsEntity
									.getAccountNum());
				}
				for (ChatItem item : chatItems) {
					if (groupId.equals(item.getOwner())) {
						foundItem = item;
						foundItem.setLatestMessage(chatMessage
								.getMessageContent());
						foundItem.setTime(chatMessage.getTime());
					}
				}

				if (foundItem != null) {
					chatItems.remove(foundItem);
				} else {
					// 如果不存在，创建一个新的item
					foundItem = new ChatItem();
					foundItem.setOwner(groupId);
					foundItem.setUserAccount(this.getTigaseAccount());
					foundItem.setFriendAccount(chatMessage.getSenderAccount());

					this.checkGroupChatRoomMap();
					GroupChatRoomEntity groupChatRoomEntity = null;
					if (groupChatRoomMap.containsKey(groupId)) {
						groupChatRoomEntity = groupChatRoomMap.get(groupId);
					} else {
						groupChatRoomEntity = this.groupChatDao
								.getGroupChatRoomByGroupId(
										this.getTigaseAccount(), groupId);
						groupChatRoomMap.put(groupId, groupChatRoomEntity);// 缓存好友信息
					}

					if (groupChatRoomEntity != null) {
						foundItem.setName(groupChatRoomEntity.getGroupName());
						foundItem.setIcon("");
					} else {
						foundItem.setName(groupId);
						foundItem.setIcon("");// 避免空指针错误
					}
					foundItem.setType(ChatItem.GROUPCHATITEM);
					foundItem.setIcon(Integer.toString(20)); // 发第一条群聊消息时，由于聊天室chatitem已经存在，所以执行不到这里，即放到这里无法设置群头像。此问题待解决
				}

				foundItem.setLatestMessage(chatMessage.getMessageContent());
				foundItem.setTime(chatMessage.getTime());
				foundItem.setIcon(Integer.toString(20));
				// 未读消息+1
				// (rcvItem) {
				// 不是自己发送出去的消息
				if (!chatMessage.getSenderAccount().equals(
						foundItem.getUserAccount())) {
					foundItem.setUnread(foundItem.getUnread() + 1);
					CYLog.d(TAG,
							"chatMessage.getSenderAccount()="
									+ chatMessage.getSenderAccount()
									+ ",chatMessage.getRecvAccount()="
									+ chatMessage.getRecvAccount());
				} else {
					CYLog.d(TAG, "groupchat self send");
				}
				// }
				// 将新来的item放在队列最前面，手机显示，就是列表的顶部
				chatItems.add(0, foundItem);
				// 将chatItem入库
				chatItemDao.deleteAndSave(foundItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "addGroupChatMessageToQueue " + e.toString());
		}
	}

	/**
	 * 判断登陆用户是否为管理员
	 * 
	 * @param groupId
	 * @return
	 */
	public boolean isAdminOfGroupChatRoom(String groupId) {
		try {
			this.checkGroupChatRoomMap();
			GroupChatRoomEntity groupChatRoomEntity = this.groupChatRoomMap
					.get(groupId);
			Integer ret = groupChatRoomEntity.getOccupantsMap().get(
					this.getTigaseAccount());
			if (ret == 1) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			CYLog.i(TAG, "isAdminOfGroupChatRoom函数执行出现异常!");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 得到聊天室的创建者，即群主
	 */
	public String getCreaterOfGroupChatRoom(String groupId) {
		this.checkGroupChatRoomMap();
		try {
			GroupChatRoomEntity room = this.groupChatRoomMap.get(groupId);
			return room.getCreaterAccount();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		// GroupChatRoomEntity room =
		// groupChatDao.getGroupChatRoomByGroupId(getTigaseAccount(), groupId);
		// if(room != null){
		// return room.getCreaterAccount()
		// } else{
		// return null;
		// }
	}

	/**
	 * 判断本用户是否是聊天室创建者，即群主
	 */
	public boolean isCreaterOfGroupChatRoom(String groupId) {
		try {
			checkGroupChatRoomMap();
			GroupChatRoomEntity groupChatRoomEntity = this.groupChatRoomMap
					.get(groupId);
			if (groupChatRoomEntity.getCreaterAccount().equals(
					getTigaseAccount())) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			CYLog.i(TAG, "isCreaterOfGroupChatRoom函数执行出现异常!");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 判断此聊天室是否是默认的班级聊天室 用户自己创建的聊天室id格式为：用户账号 + "-" +聊天室编号 默认班级聊天室的id则为班级id
	 */
	public boolean isDefaultGroupChatRoom(String groupId) {
		String groupIds = userSelfContactsEntity.getGroupName();
		if (!groupIds.contains(groupId)) {
			return false;
		}
		if (!groupId.contains("-")) {
			return true;
		}
		return false;
	}

	/**
	 * 自动创建或加入班级聊天室
	 */
	public void autoCreateOrJoinDefaultGroupChatRoom() {
		jobManager.addJobInBackground(new ResetDefaultGroupChatRoomJob(
				DataCenterManagerService.this));
	}

	public GroupChatDao getGroupChatDao() {
		return groupChatDao;
	}

	public void sendHttpStatusMessage(int status) {
		httpManagerHandler.sendEmptyMessage(status);
	}

	public Map<String, GroupChatRoomEntity> getGroupChatRoomMap() {
		return groupChatRoomMap;
	}

	public DepartmentDao getDepartmentDao() {
		return departmentDao;
	}

	public void setGroupChatFuncStatus(GroupChatFuncStatusEntity funcStatus) {
		groupChatFuncStatus = funcStatus;
	}

	/**
	 * 删除联系人的聊天项
	 * 
	 * @param owner
	 * @param ctype
	 */
	public void deleteChatItem(String owner, int ctype) {
		try {
			if (chatItemDao == null) {
				chatItemDao = new ChatItemDao(this.getApplicationContext());
			}
			this.chatItemDao.deleteChatItem(owner, this.getTigaseAccount(),
					ctype);
			
			try {
				if (chatItems != null) {
					for (int i = 0; i < chatItems.size(); ++i) {
						if (chatItems.get(i).getOwner().equals(owner)) {
							chatItems.remove(i);
						}
					}
				}
			} catch (Exception e) {
			}

			//延时更新页面
			new Thread() {
				public void run() {
					try {
						sleep(500);
						
						// 通知上层UI
						Bundle bundle = new Bundle();
						// 消息页面只要有消息都要更新，但是联系人页面只有用户图像更新时才需要更新
						bundle.putString("from", "updateUI");// 只做UI图片更新
						messageIntent.putExtras(bundle);
						sendBroadcast(messageIntent);// 通知UI更新,更新chatFragment
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<SQLiteDatabase> dbList = new LinkedList<SQLiteDatabase>();
}
