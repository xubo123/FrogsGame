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

	/** ��Ϣѡ��б� */
	private List<ChatItem> chatItems;// ���Ż�
	private List<Channel> channels;// ���Ż�������map

	private Map<String, List<ContactsEntity>> mContactsEntityMap;// ��ϵ���б�
	/** ��½�û��Լ��ĸ�����Ϣ �������˺š����롢ͨ����Ⱥ�顢ͼƬ��ַ�������� */
	private ContactsEntity userSelfContactsEntity;

	/** �û����ڵ��������б� groupIdΪkey */
	private Map<String, GroupChatRoomEntity> groupChatRoomMap;

	/** ������ */
	private ChannelDao channelDao;
	private ChatMessageDao chatMessageDao;
	private ChatItemDao chatItemDao;
	private ClassmateDao classmateDao;// ��ϵ�˱�
	private DepartmentDao departmentDao;// ������
	private GroupChatDao groupChatDao; // �����ұ�
	// private Map<String, List<ChatMessage>> unreadNewsMap = null;
	// private Map<String, List<ChatMessage>> readedNewsMap = null;
	private Map<String, List<ChatMessage>> unreadChatMessageMap = null;
	private Map<String, List<ChatMessage>> readedChatMessageMap = null;
	private Map<String, ContactsEntity> friendsInfoMap;// �洢���ص���ϵ�ˣ��˺�Ϊkey
	// δ�����ŵ���Ŀ
	// private Map<String, Integer> unreadNewsCountMap = null;
	private Roster roster;
	ContentValues values = null;

	boolean sendSucc = true;// �Ƿ��ͳɹ�
	boolean sendStatus = false;// �Ƿ��з��ͳɹ�����״̬����

	GroupChatFuncStatusEntity groupChatFuncStatus = null; // ��ʾȺ�Ĳ����ĸ���״̬
	private DownloadFileBroadCastReceiver downloadFileReceiver = null;
	private Context context;

	private JobManager jobManager = SchoolMateChat.getInstance()
			.getJobManager();;

	int numflag = 0;

	// HttpManagerThread�߳���Ϣ������
	Handler httpManagerHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HttpJobStatus.HTTP_SUCCESS:
				CYLog.i(TAG, "Http�����ɹ�!");
				break;
			case HttpJobStatus.HTTP_FAIL:
				CYLog.i(TAG, "Http����ʧ��!");
				break;
			}
		}
	};

	// �ļ��ϴ�����
	Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case APPConstant.DOWNLOAD_STARTED:
				CYLog.i(TAG, "��ʼ����");
				break;

			case APPConstant.UPDATE_DOWNLOAD_PROGRESS:
				int percentage = msg.arg1;
				CYLog.i(TAG, "���ս���" + percentage);
				break;

			case APPConstant.DOWNLOAD_FINISHED:
				CYLog.d(TAG, "�ļ��������" + msg.obj);
				// ������ɣ�֪ͨ�ϲ����UI��ʾ
				Bundle bundle = new Bundle();
				// ��Ϣҳ��ֻҪ����Ϣ��Ҫ���£�
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
					new DownloadFileTask(url, null, null, handler).execute("");// ���յ��ļ���Ϣ
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Ⱥ����Ϣ
	Handler groupChatHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			// GroupChatRoomEntity�ṹ���д��ݣ�������ȫ�ֱ����ķ�ʽ
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
				CYLog.i(TAG, "��tigase�������ϴ��������ҳɹ�!");
				jobManager.addJobInBackground(new AddGroupChatRoomJob(
						DataCenterManagerService.this, groupChatRoomEntity));
			}
				break;

			case GroupChatFuncStatusEntity.GROUPCHAT_ADDROOM_FAIL:
				CYLog.i(TAG, "��tigase�������ϴ���������ʧ��!");
				break;

			case GroupChatFuncStatusEntity.GROUPCHAT_JOINROOM_SUCCESS: {
				CYLog.i(TAG, "���������ҽڵ�ɹ�!");
				// Ҫ�����Ⱥ����Ϣ�����ص����أ���Ⱥ���Ҳ�Ѿ����ĳɹ���
				// Ȼ�������������淢���Լ�ͬ������������Ϣ ǰ׺+�û��˺�_�û�����_ͼƬ��ַ
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
				CYLog.i(TAG, "����Ⱥ��ʧ��! " + msg.obj);
				// +++lqg+++ �ڷ���Ⱥ����Ϣʧ�ܵ�ʱ���鴦������

				break;

			case GroupChatFuncStatusEntity.GROUPCHAT_UBSUBSSRIBE_SUCCESS: {
				CYLog.i(TAG, "���˺��˶�������" + groupChatRoomEntity.getGroupName()
						+ "�ɹ�!");
				// ɾ������Ⱥ��ʷ��Ϣ������UI
				deleteLocalGroupChatData(groupId);
				// �����ǰActivity��Ⱥ��������棬�����ٴ�Activity���ص�MainActivity
				ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				String cn = am.getRunningTasks(1).get(0).topActivity
						.getClassName();
				CYLog.i(TAG, "��ǰActivity=" + cn);
				if (cn.equals("com.hust.schoolmatechat.ChatActivity"))
					ChatActivity.instance.finish();
			}
				break;

			case GroupChatFuncStatusEntity.GROUPCHAT_UBSUBSSRIBE_FAIL:
				CYLog.i(TAG, "ȡ������Ⱥ���ʧ��!");
				break;

			case GroupChatFuncStatusEntity.GROUPCHAT_FORCE_SUBSCRIBE:
				CYLog.i(TAG, "Ҫ��Ⱥ��Ա���¶���Ⱥ��! " + msg.obj);
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

	/********** �������ṹ��ֲ���� **************/
	private Intent messageIntent = null;
	private Intent rosterIntent = null;
	private Intent friendAddIntent = null;
	private Intent groupInviteIntent = null;
	private Intent moveOutOfRoomIntent = null;
	private Intent tigaseConnectionStatusIntent = null;
	/********* �û���Ϣ ****************/
	private String jid;// jid = userSelfContactsEntity.getAccountNum() + @ +
						// domain
	private Intent newsIntent = null;
	private Intent channelIntent = null;

	private int authorizedState;// �û��Ƿ���֤�ɹ�0û����֤�ɹ���1��֤�ɹ�
	private boolean userClickedQuitButton;// �û��Ƿ����˳�

	/********* ------------------- **********/

	/** ʱ���ʽ */
	DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// /** ��ʱˢ��ui */
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
	 * Ⱥ��֪ͨ��������Ϣ�������г�Ա
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
	 * �����˳�ʱ���ã������Ϳ�����ȷ�ж��Զ���½
	 */
	public void resetAuthorizedState() {
		authorizedState = 0;
		// ����ڴ�����,���¶�ȡ
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
		// ��ϵ�˻����б������
		// unreadNewsCountMap.clear();
	}

	/**
	 * ��½��֤, accountΪ�ֻ��Ż����˺�
	 */
	public int loginAuthenticate(String account, String password) {
		// ���Ե�½web������
		try {
			if (authorizedState == 0) {
				String loginJResult = LoginUtils.loginOnMainServer(account,
						password);
				JSONTokener jsonTokener = new JSONTokener(loginJResult);
				JSONObject resultJO = (JSONObject) jsonTokener.nextValue();
				boolean mainServerSuccess = resultJO.getBoolean("success");
				if (mainServerSuccess) {
					// �������صĽ�����ݣ�����ȡ��½�û�����Ϣ
					boolean wholeLoginSuccess = handleJResult(loginJResult,
							account, password);
					if (wholeLoginSuccess) {
						authorizedState = 1;// ����ɹ�
					} else {
						CYLog.d(TAG, "login authenticate failed");
						return 0;
					}
				} else {
					return 0;
				}

				return 1;// �״���֤
			} else {
				CYLog.d(TAG, "has loginAuthenticate");
				return 2;// �Ѿ���֤��
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * ��ʼ���������ģ������߳���ɣ��ӿ��½�ٶ� userAccount password
	 */
	public void initialiseDataCenter(final String userAccount,
			final String password) {
		new Thread() {
			@Override
			public void run() {
				// ����֤�ɹ�֮�󣬴ӱ������ݿ����ø����������ʷ����
				resetDataFromDb(userAccount);

				// ��ʼ����ϵ����Ϣ��
				if (!resetContactsEntityMap(userAccount, password)) {
					CYLog.e(TAG, "resetContactsEntityMap failed");
				}
				// ��ʼ����������Ϣ�������༶������
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
	 * ��ʼ����ϵ����Ϣ��
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
			// �Ȳ鿴�����Ƿ��Ѿ������Ļ�ȡ���༶��ϵ����Ϣ������о�ֱ�Ӵӱ���ȡ����ϵ����Ϣ���
			if (classmateDao.hasAllClassmates(userAccount) == 1) {
				mContactsEntityMap = classmateDao
						.getAllcontactsEntity(userAccount);
			} else {
				CYLog.i(TAG, "else branch is called!");
				// û����֤������ȥ��ȡ����Ϣ
				String auth = userSelfContactsEntity.getAuthenticated();
				if (auth != null && auth.equals("1")) {
					CYLog.i(TAG, "ͨ����֤!");
					new Thread() {
						@Override
						public void run() {
							try {
								// ��web��������ȡ��ϵ����Ϣ�����������ݿ�
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
										userAccount, 1);// ��ȡ�ɹ����µ����ݿ�
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
				// ��ֹ����ӦΪ��ϵ�˳�ʼ��ʧ�ܶ��ҵ�
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
	 * ��http������������ȡ����Ⱥ��Ǻ����˺���ϸ��Ϣ,����� ʹ��Ion��İ汾
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
					contactsEntity.setHasAllClassmates(1);// ��ϵ������ϸ��Ϣ
					contactsEntityList.add(contactsEntity);
				}
			}
			// �Ǻ�����ϵ�����
			updateGroupChatMembersToDB(contactsEntityList);
		} else {
			CYLog.i(TAG, "��web���������ϻ�ȡȺ��Աʧ��!");
		}
	}

	/**
	 * ��http������������ȡ����Ⱥ��Ǻ����˺���ϸ��Ϣ,����� ʹ��httpClient��İ汾
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
	// contactsEntity.setHasAllClassmates(1);// ��ϵ������ϸ��Ϣ
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
	 * ��http������������ȡ����Ⱥ�飬����� ʹ��Ion��İ汾
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
			// ����Ⱥ�����
			if (roomList.size() > 0) {
				updateGroupChatRoomToDB(roomList, userAccount);
			}
		}
	}

	/**
	 * ��http������������ȡ����Ⱥ�飬����� ʹ��httpClient��İ汾
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
	// // ���ݽ���ѯ����ַ���ת��ΪGroupChatRoomEntity
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
	// //����Ⱥ�����
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
	 * Ⱥ���б���Ϣ���µ�����
	 */
	private synchronized void updateGroupChatRoomToDB(
			List<GroupChatRoomEntity> groupChatRoomEntityList,
			String userAccount) {
		try {
			CYLog.i(TAG, "updateGroupChatRoomToDB is called!");

			int size = groupChatRoomEntityList.size();
			// ���δ����web��������ȡ�������Ҽ�¼
			for (int i = 0; i < size; ++i) {
				GroupChatRoomEntity groupChatRoomEntity = groupChatRoomEntityList
						.get(i);
				if (!groupChatDao.isGroupChatRoomEntityExisted(userAccount,
						groupChatRoomEntity.getGroupId())) { // ��������������ӵ�,����ӵ��������ݿ�
					groupChatDao.addGroupChatEntity(groupChatRoomEntity);
				} else { // ���������Ѵ����ڱ��أ����±������ݿ�
					groupChatDao.updateGroupChatEntity(groupChatRoomEntity);
				}
			}

			if (size > 0) {
				// ���������ݿ�󣬸����ڴ�
				groupChatRoomMap = null;
				this.checkGroupChatRoomMap();
				// +++lqg+++ �����̵߳��ã����ܳ���Ƶ������UI�����
				sendBroadcast(rosterIntent);// ֪ͨUI
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ϵ����Ϣ���
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
	 * ������½web��������֤��Ľ������
	 * 
	 * @param resultJson
	 * @param accountNum
	 * @param password
	 * @return
	 */
	private boolean handleJResult(String resultJson, String accountNum,
			String password) {
		try {
			boolean flag = false;// ����Ƿ��д�����Ϣ
			// �Ȳ鿴�����Ƿ��и�����Ϣ������о�ֱ�Ӵӱ���ȡ��������Ϣ���
			if (classmateDao.isSelfContactsEntityExisted(accountNum)) {
				flag = true;// �����д�����Ϣ
				userSelfContactsEntity = classmateDao
						.getSelfContactsEntity(accountNum);
				String auth = userSelfContactsEntity.getAuthenticated();
				// ��֤��
				if (auth != null && auth.equals("1")) {
					EventBus.getDefault().post(userSelfContactsEntity);
					return true;
				}
			}
			ContactsEntity userSelfContactsEntityTemp = userSelfContactsEntity;
			// ע�⣬web��ȡ�ĸ�����Ϣ��û�з�����Ϣ����tigase��������roster����
			userSelfContactsEntity = LoginUtils.jsonToContacsEntity(resultJson,
					accountNum, password);
			// ��ֹ�ֻ�����Ϊ�˺�д��
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
			// ��֤��
			if (auth != null && auth.equals("1")) {
				String baseInfoIds = userSelfContactsEntity.getBaseInfoId();
				if (baseInfoIds != null && !baseInfoIds.equals("")) {
					// ������ϢĬ������Ϊ�û����ڵİ༶��Ϣ
					String ids[] = baseInfoIds.split(",");
					if (ids.length != 0) {
						// ����û�л������ݣ���ʹ��web����
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
									dept = deptsArray[0];// ʹ��web����
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
									dept = deptsArray[i];// ʹ��web����
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
							// Ŀǰֻ�а༶Ⱥ������һ��Ⱥ����
							userSelfContactsEntity.setHasAllClassmates(0);// ���ػ�û�д�web��ȡ���༶��ϵ������
							EventBus.getDefault().post(userSelfContactsEntity);
							if (flag) {
								classmateDao
										.updateSelfContactsEntity(userSelfContactsEntity);
							} else {
								// �������
								classmateDao
										.addSelfContactsEntity(userSelfContactsEntity);
								// ����������֤��֪ͨ�̣߳��û��˳����˳�
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
					// �������
					classmateDao.addSelfContactsEntity(userSelfContactsEntity);
					// ����������֤��֪ͨ�̣߳��û��˳����˳�
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
	 * ��һ�ε�½��ʱ�������̣߳�֪ͨ�����Ѿ���֤�ĺ��Ѹ��±��˵���Ϣ
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
	 * ��web��������ȡ��ϵ����Ϣ
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

				// ��ȡweb�������ϵİ༶ͬѧ
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
			CYLog.i(TAG, "updateContatsFromWebִ�г����쳣!");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * �����Ƿ��е�½�û�����Ϣ
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
	 * ���ݻ�����Ϣid��ȡ������Ϣ����Ϊδ��֤�û�����������ȡ
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
	 * ����δ��֤�û�����Ϣ
	 */
	public void updateUnAuthenticatedContacts() {
		try {
			if (classmateDao == null) {
				classmateDao = new ClassmateDao(this);
			}
			// �Ѿ����¹������û��Ѿ��˳�
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

				// ����֤�û��������̸߳�����Ϣ
				newThreadUpdateUnAuthenticatedUserInfo(contactsEntity,
						userAccount, password);
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "" + e.toString());
		}
	}

	/**
	 * ����δ��֤�û�����Ϣ
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
				// ������������̵߳�������
				new Thread() {
					@Override
					public void run() {
						try {
							// web�ϻ�ȡ�ĺ�����Ϣû�д�����,����������,�˴�ʹ�û���id
							ContactsEntity contactsEntity2 = LoginUtils
									.getFriendInfoByBaseInfoId(userAccount,
											password, baseinfId);
							// �û��Ѿ��˳�
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

							// ��֤�û������±�����Ϣ
							String auth = contactsEntity2.getAuthenticated();
							if (auth != null && auth.equals("1")) {
								String nickname = contactsEntity2.getName();
								String friendAccount = contactsEntity2
										.getAccountNum();
								// // ���Ǻ��ѣ���Ϊ���ѣ��༶����Ĭ���Զ����
								// if (!isMyFriend(friendAccount)) {
								// sendFriendAddAgree(friendAccount, className);
								// return;
								// }

								contactsEntity2.setClassName(className);
								contactsEntity2.setUserAccount(userAccount);
								classmateDao
										.updateContacsEntity(contactsEntity2);

								// +++lqg+++ ���ڸ�Ϊhandler����
								// ���������ݿ�󣬸����ڴ�
								mContactsEntityMap = classmateDao
										.getAllcontactsEntity(userAccount);
								sendBroadcast(rosterIntent);// ֪ͨUI
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
	 * true ����������δ��֤���ˣ���ִ�в�����false������֤���ˣ�ִ�в���
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
	 * ���Ҹ�����Ϣ,ָ��ʱ��½app����
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
	 * ��ȡ��ϵ�˵���Ϣ
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

			// �Ǻ��ѣ����Ƿ�ΪȺ�ڵķǺ���
			if (contactsEntity == null) {
				if (groupChatDao == null) {
					groupChatDao = new GroupChatDao(this);
				}
				contactsEntity = groupChatDao.getFriendInfoByAccount(
						userAccount, friendAccount);
			}

			if (contactsEntity != null) {
				friendsInfoMap.put(friendAccount, contactsEntity);// ���������Ϣ
			}
		}

		return contactsEntity;
	}

	/**
	 * ���±��ص���ϵ����Ϣ, ��������
	 */
	private synchronized void updateClassmateToDB(
			Map<String, List<ContactsEntity>> classmateMap, String userAccount) {
		CYLog.i(TAG, "updateClassmateToDB is called!");
		ContactsEntity classmateResult;
		if (mContactsEntityMap == null) {
			mContactsEntityMap = classmateDao.getAllcontactsEntity(userAccount);
		}

		// ��������µķ������
		Set<String> classSet = new HashSet<String>();
		ContactsEntity selfContactsEntity = this.getUserSelfContactsEntity();
		if (selfContactsEntity != null) {
			// ��ȡ����Ϣ
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
			// ��������µķ��飬����Ҫ�޸ı��صĸ�����Ϣ
			if (selfContactsEntity != null && !classSet.contains(key)) {
				CYLog.i(TAG, "#########new class name : " + key);
				String className = selfContactsEntity.getClassName();
				selfContactsEntity.setClassName(className + "," + key);
				this.updateSelfContactsEntity(selfContactsEntity, false);
			}

			for (int i = 0; i < classmateMap.get(key).size(); i++) {
				classmateResult = classmateMap.get(key).get(i);
				String baseInfoIds = classmateResult.getBaseInfoId();
				// �����������,�жϱ�׼Ϊ������Ϣid�����û��˺�
				// baseInfoIds��id�п����ö��Ÿ�����
				if (baseInfoIds == null || baseInfoIds.equals("")) {
					continue;
				}

				if (classmateDao.isContacsEntityExisted(userAccount,
						baseInfoIds)) {
					// �ӱ���ȡ������˵���Ϣ
					ContactsEntity friend = null;
					if (baseInfoIds.length() > 19) {
						String baseIds[] = baseInfoIds.split(",");
						for (int j = 0; j < baseIds.length; ++j) {
							// ����Ļ���id
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

		// ���������ݿ�󣬸����ڴ�
		mContactsEntityMap = classmateDao.getAllcontactsEntity(userAccount);
		sendBroadcast(rosterIntent);// ֪ͨUI
	}

	/**
	 * ��friendNew�����ݺϲ���friend�������µ�����
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
	 * �����û�������Ϣ
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
	 * ����֤�ɹ�֮�󣬴ӱ������ݿ����ø����������ʷ����
	 */
	public void resetDataFromDb(String accountNum) {// �����˺ţ��쳣�˳��ٴε�¼
		try {
			CYLog.i(TAG, "resetDataFromDb");
			// �쳣�жϴ��� !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ++++lqg++++ ���޸�
			if (channels == null || channelDao == null
					|| chatMessageDao == null || chatItemDao == null
					|| classmateDao == null || departmentDao == null
					|| unreadChatMessageMap == null
					|| readedChatMessageMap == null || messageIntent == null
					|| rosterIntent == null || newsIntent == null) {// ���п������ڱ��쳣�����ڴ��Ѿ���������

				// ȫ�����³�ʼ��
				this.onCreateProxy();
				this.setAuthorizedState(1);
			}

			// ��ʼ��ͨ����Ϣ
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
			 * ���õ�½app����Ϣ
			 */
			userSelfContactsEntity = classmateDao
					.getSelfContactsEntity(accountNum);
			EventBus.getDefault().post(userSelfContactsEntity);

			if (chatItemDao == null) {
				chatItemDao = new ChatItemDao(this);
			}
			// ��ʼ��chatItem
			chatItems = chatItemDao.getAllChatItem(this.userSelfContactsEntity
					.getAccountNum());
			if (chatItems != null) {
				// CYLog.d(TAG, "chatItems.size()=" + chatItems.size());

				// ��ʼ��ÿһ����Ŀ�������Ϣ����
				for (ChatItem chatItem : chatItems) {
					List<ChatMessage> unReadList = null;
					if (chatItem.getType() == ChatItem.GROUPCHATITEM) {// ��ʼ��δ��Ⱥ����Ϣ
						unReadList = chatMessageDao.getUnreadChatMessage(
								// ����������Ƕ�ȡδ����
								chatItem.getOwner(),
								userSelfContactsEntity.getAccountNum(),
								ChatMessage.GROUPCHATMESSAGE);
						// CYLog.d(TAG, "GROUPCHATITEM unReadList.size()="
						// + unReadList.size());
					} else if (chatItem.getType() == ChatItem.PRIVATECHATITEM) {// ��ʼ��δ���ĵ�����Ϣ
						unReadList = chatMessageDao.getUnreadChatMessage(
								chatItem.getOwner(),
								this.userSelfContactsEntity.getAccountNum(),
								ChatMessage.PRIVATECHATMESSAGE);
						// CYLog.d(TAG, "GROUPCHATITEM unReadList.size()="
						// + unReadList.size());
					} else if (chatItem.getType() == ChatItem.NEWSITEM) {// ��ʼ��δ����������Ϣ
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
				// ��ʼ��У�Ѱ��æ��Ϣ
				// ++++������ʵ����Ҫ��ʼ���ˣ�getchatdata()������������
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

	// // ��һ�ε�½��ʱ��������ȡ����
	// public void getPushedServerNewsActively() {
	//
	//
	// if (chatItems != null && chatItems.size() < 2) {//
	// ����Ϣ�б�С���������ж�Ϊû�����ţ�����ȡһ��
	// this.pushedMsgService.getPushedServerNewsActively();
	// // CYLog.d(TAG, "chatItems.size()=" + chatItems.size());
	// }
	//
	//
	// }

	/**
	 * roster�����仯������ϵ���б����仯
	 */
	public void updateContactsOnRoster() {
		try {
			// ����������ϵ���ϵ����Ϣ������������֤��Ϣ���߷�����Ϣ
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
								// ��web�������ϻ�ȡ���ѵ���Ϣ
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

						// ������Ϣ��ȫ
						if (!classmateDao.isContacsEntityInfoComplete(
								this.getTigaseAccount(),// ֱ�ӵ��ñ��صķ��������Է�ֹuserSelfContactsEntity�����պ��ò����˺�
								friendAccount)) {
							// web�ϻ�ȡ�ĺ�����Ϣû�д�����,����������
							ContactsEntity contactsEntity = LoginUtils
									.getFriendInfo(this.getTigaseAccount(),
											this.getTigasePassword(),
											friendAccount);
							if (contactsEntity != null) {
								contactsEntity.setClassName(groupEntry
										.getName());// ���������ڷ�����Ϣ
								contactsEntity.setAuthenticated("1");// �Ѿ���֤��
								tmpContactsList.add(contactsEntity);
							}
						}
					}
					tmpContactsMap.put(groupEntry.getName(), tmpContactsList);
				}
				if (tmpContactsMap.size() != 0) {
					// ���µ��������ݿ�
					updateClassmateToDB(tmpContactsMap, this.getTigaseAccount());
				}

				// // �ɹ���½�������������鱾���Ƿ���Ⱥ����Ϣδͬ��
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
		if (auto == null || auto.equals("auto")) {// �Զ���½������������
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
		if (auto == null || auto.equals("auto")) {// ������û������˳��Ļ��Ͳ���Ҫ����������
			CYLog.i(TAG, "unnormal onDestroy,restart");
			restartService();
		}
	}

	/** ibind �� ����������������Լ�֮ǰ��ʼ�� */
	private DataCenterManagerBiner dataCenterManagerBinder = new DataCenterManagerBiner();

	@Override
	public IBinder onBind(Intent intent) {
		dataCenterManagerBinder = new DataCenterManagerBiner();
		return dataCenterManagerBinder;
	}

	public class DataCenterManagerBiner extends Binder {
		// ��ȡ��ǰService��ʵ��
		public DataCenterManagerService getService() {
			return DataCenterManagerService.this;
		}
	}

	/**
	 * ��ÿһ�������������Ÿ��µ��ڴ棬ͬʱ���
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
			// ����һ��chatitem��¼
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
		// ++++lqg++++������ ���޸� �޹���
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
		foundList.add(0, rcvMessage);// ��ÿ�ν�������������Ϣ������ǰ��
		chatMessageDao.addChatMessage(rcvMessage);

		CYLog.d(TAG, "�ŵ�" + pushedMessage.getChannelId() + "��δ��������Ϣ����"
				+ unreadChatMessageMap.get(rcvItem.getOwner()).size());
		// ��Ϣ֪ͨ
		notifyMainUIMsgCome(pushedMessage.getNewsList().get(0).getTitle(),
				pushedMessage.getNewsList().get(0).getSummary());

		// ����channleId
		newsIntent.putExtra("channelId", rcvItem.getOwner());
		sendBroadcast(newsIntent);
	}

	/**
	 * ȡ�����б�
	 * 
	 * @param
	 */
	public List<ChatItem> getNewsItems() {
		try {
			List<ChatItem> newsItems = new ArrayList<ChatItem>();
			// ��ÿһ��Ƶ������һ����Ӧ��item
			for (Channel channel : channels) {
				ChatItem newsItem = new ChatItem();
				newsItem.setOwner(channel.getcName());
				newsItem.setName(channel.getcName());
				newsItem.setIcon(channel.getIcon());
				newsItem.setUserAccount(this.getTigaseAccount());
				newsItem.setLatestMessage(channel.getChannelRemark());
				// ͳһ��owner��Ϊ�жϱ�׼
				if (newsItem.getOwner().equals(
						APPConstant.SCHOOL_HELPER_CHANNEL_NAME)) {
					newsItem.setType(ChatItem.SCHOOLHELPERITEM);// ������У�Ѱ��æ��Ϣ��Ŀ
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
	 * ��һ��ͨ����Ϣ���µ��ڴ沢�������ݿ�
	 * 
	 * @param channelList
	 */

	public void onEventBackgroundThread(List<Channel> recvChannels) {
		if (recvChannels != null && recvChannels.size() > 0) {
			// �ڴ�����ͨ����Ϣ
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
	 * ��ȡ����ͨ����Ϣ����ȥ����ڴ����������ݣ��о�ֱ�ӷ��أ��޾�ȥ���ݿ�ȡ
	 * 
	 * @return
	 */

	// public List<Channel> getAllChannel() {
	// CYLog.i(TAG, "ͨ����Ϣ��СΪ��" + channels.size());
	// return channels;
	// }
	//
	// public List<ChatItem> getAllChatItem() {
	// CYLog.i(TAG, "ITEM��Ϣ��СΪ��" + chatItems.size());
	// return chatItems;
	// }

	/**
	 * ��ȡ����
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

			// �ֻ���ʾ���ƣ�ֻ����ʾ100����¼
			if (unreadNewsList.size() >= 100) {
				// ���ص���������
				newsList.addAll(0, unreadNewsList.subList(0, 100));
				// �����ݿ��з��ص��������ݸ���Ϊ�Ѷ�
				chatMessageDao.update(newsList);
				// �����ص����������Ƴ�δ������
				unreadNewsList.removeAll(newsList);
				// ����map
				unreadChatMessageMap.put(channelId, unreadNewsList);
			} else {
				// ���δ����Ϣ����100,ȡ���ݿ��Ѷ���Ϣ����
				// ��δ����Ϣ�Ŷ���ǰ��
				newsList.addAll(0, unreadNewsList);
				List<ChatMessage> readChatMessages = chatMessageDao
						.getReadChatMessage(this.getTigaseAccount(),
								ChatMessage.NEWSMESSAGE, 0,
								100 - unreadNewsList.size());
				newsList.addAll(readChatMessages);
				// ���¿�������Ϊ�Ѷ�
				chatMessageDao.update(unreadNewsList);
				// �����ص����������Ƴ�δ������
				unreadNewsList.removeAll(unreadNewsList);
				// ����map
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
			// ��δ������Ϣת��Ϊ�Ѷ���������浽ChatMessage_Table��
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
			CYLog.i(TAG, "�ŵ�" + channelId + "��δ������Ϊ0");
			return lastNewsList;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "getLastNewsChannelData " + e.toString());
			return null;
		}
	}

	public Map<String, Integer> getUnreadChatItemCount() {
		try {
			// �������е�δ��ȡ��������Ϣ��cid�Ͷ�Ӧ��δ��ȡ��Ŀ��
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
	 * ��ȡ����δ�����ŵ�����
	 * 
	 * @return
	 */

	public Map<String, Integer> GetUnreadChannleCount() {
		try {
			// �������е�δ��ȡ��������Ϣ��cid�Ͷ�Ӧ��δ��ȡ��Ŀ��
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
	 * ȡ������Ϣ
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
					|| unreadChatMessageList.size() == 0) { // ���from�������û��δ����Ϣ��
															// ֱ�Ӷ�ȡ���ݿ�

				List<ChatMessage> readedChatMessageList = new ArrayList<ChatMessage>();

				// if (from.equals("ĸУ����") || from.equals("�ܻ���")) {

				readedChatMessageList = chatMessageDao.getReadChatMessage(from,
						this.getTigaseAccount(), type, 0, count);
				CYLog.d(TAG, ",readedChatMessageList.size()="
						+ readedChatMessageList.size());
				// } else {
				// readedChatMessageList = chatMessageDao.getReadChatMessage(
				// from, this.getTigaseAccount(), 3, 0, count);
				// }
				chatMessageList.addAll(readedChatMessageList);
			} else { // ������list���ڣ��϶�����δ����Ϣ
				chatMessageList.addAll(0, unreadChatMessageList); // ��ȡ���е�δ����Ϣ

				CYLog.d(TAG, ",unreadChatMessageList.size()="
						+ unreadChatMessageList.size());

				unreadChatMessageList.removeAll(chatMessageList);
				// ����map
				unreadChatMessageMap.put(from, unreadChatMessageList); // ���ܿ��Բ���Ҫ
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
				// �����ݿ��з��ص��������ݸ���Ϊ�Ѷ� Ҫ��ȡ���Ѷ�����֮����ܸ������ݿ⣬��Ȼ���������ظ���
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
	// // �ֻ���ʾ���ƣ�ֻ����ʾ100����¼
	// if (unreadChatMessageList.size() >= 100) {
	// // ���ص���������
	// chatMessageList
	// .addAll(0, unreadChatMessageList.subList(0, 100));
	// // �����ݿ��з��ص��������ݸ���Ϊ�Ѷ�
	// chatMessageDao.update(chatMessageList);
	// // �����ص����������Ƴ�δ������
	// unreadChatMessageList.removeAll(chatMessageList);
	// // ����map
	// unreadChatMessageMap.put(from, unreadChatMessageList);
	// } else {
	// // ���δ����Ϣ����100,ȡ���ݿ��Ѷ���Ϣ����
	// // ��δ����Ϣ�Ŷ���ǰ��
	// chatMessageList.addAll(0, unreadChatMessageList);
	// List<ChatMessage> readedChatMessageList = chatMessageDao
	// .getChatMessage(this.getTigaseAccount(), 0,
	// 100 * (count - 1),
	// 100 - unreadChatMessageList.size());
	// chatMessageList.addAll(readedChatMessageList);
	// // ���¿�������Ϊ�Ѷ�
	// chatMessageDao.update(unreadChatMessageList);
	// // �����ص����������Ƴ�δ������
	// unreadChatMessageList.removeAll(unreadChatMessageList);
	// // ����map
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
	 * ������Ϣ��δ������Ѷ�����δ����Ϣ���
	 * 
	 * @param list
	 */
	private void addUnreadChatMessageToDB(List<ChatMessage> list) {
		// +++lqg+++ �д�����޸�
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
	 * // �õ�ĳ��jid�µ����ݿ�ĸ���������ʷ��Ϣ����ȡ����100�������ݣ� ��< �����ڴ����������ݹ���> public
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

	// �˳�ʱ�������е�δ����Ϣ���

	public void saveAllUnreadChatMessage() {
		// +++lqg+++ �д�����޸�
		List<ChatMessage> chatMessageList = null;
		List<ChatMessage> unreadNewsList = null;
		ContentValues values = null;
		// �رտͻ���ʱ��δ����������Ϣ�������ݿ���
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

		// �رտͻ���ʱ��δ����������Ϣ�������ݿ���
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

	// �յ�¼ʱ��ȡ�����ݿ��л�ȡ��δ����Ϣ

	/*
	 * public Map<String, List<ChatMessage>> getAllUnreadChatMessage() {
	 * Map<String, List<ChatMessage>> initUnreadChatMessageMap = null;
	 * List<Map<String, String>> list = unreadChatMessageDao
	 * .listAllChatMessageMaps(); Map<String, String> chatMessageMap = null;
	 * initUnreadChatMessageMap = new HashMap<String, List<ChatMessage>>();
	 * ChatMessage chatMessage = new ChatMessage(); for (int i = 0; i <
	 * list.size(); i++) { chatMessageMap = list.get(i); //
	 * �ж���Ϣ��������������Ϣ�򷵻�ѭ���������������Ϣ���������Ĳ��� if
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
	 * list.get(i); // �ж���Ϣ��������������Ϣ�������������ݣ�����������Ϣ�Ļ��ͷ��ؼ���ѭ�� if
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
	 * ����,����������Ϣ
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
		EventBus.getDefault().post(mSendChatMessage);// ����Ϣ���ȴ�����
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
		// // ��Ϣ����ʧ�ܣ�����⣬��Ҫ�����ʾ
		// // CYLog.i(TAG, "to " + receiver + " failed : " + message);
		// sendSucc = false;
		// }

		// ���ı���Ϣ��װ�ɰ������ʹ���Ϣ�˺š�����ʱ�䡢id�����͵ȵ�ChatMessage����
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setMessageContent(message);
		chatMessage.setMid(UUID.randomUUID().toString());
		chatMessage.setOwner(receiver);// ����Ϊ�Է�
		chatMessage.setUserAccount(this.getTigaseAccount());
		chatMessage.setSenderAccount(this.getTigaseAccount());
		chatMessage.setRecvAccount(receiver);
		// ���ô���Ϣ����Ϊ�Լ����ͳ�ȥ�ĵ�����Ϣ
		chatMessage.setType(ChatMessage.PRIVATECHATMESSAGE);
		chatMessage.setTime(new Date(System.currentTimeMillis()));
		chatMessage.setIsRead(0);// ����Ϊδ����Ϣ
		chatMessage.setSendSucc(sendSucc);// ��Ϣ���ͳɹ����
		// ��ӵ��ڴ��б�
		addChatMessageToQueue(chatMessage, sendSucc);

		// ֪ͨ�ϲ�UI
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
	 * ��������Ϣ������ڴ��У����Ҵ��
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
				return;// ������Ϣ
			}
			if (msgBody.startsWith(APPConstant.CMD_PREFIX_FILE_SEND)) {
				String url = msgBody.replaceAll(
						APPConstant.CMD_PREFIX_FILE_SEND, "");
				new DownloadFileTask(url, null, null, handler).execute("");// ���յ��ļ���Ϣ
			}

			// ����û����ϵ��������Ϣ����
			boolean mark = true;
			if (this.getFriendInfoByAccount(from) == null) {
				if (this.isMyFriend(from)) {
					this.checkFriendInfoLocalExisted(from);
					if (this.getFriendInfoByAccount(from) == null) {
						mark = false;
					}
				} else {
					// �Ƿ���Ӵ���Ϊ����
					mark = false;

					// web�ϻ�ȡ�ĺ�����Ϣû�д�����,����������
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
								classname = "δ֪�༶";
							}
							friendAddRecvEntity.setClassName(classname);
							EventBus.getDefault().post(friendAddRecvEntity);

							// ��ʾ��Ϣ
							mark = true;
							contactsEntity.setClassName("�ҵĺ���");
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
			rcvMessage.setSendSucc(true);// ���յ���Ϣʼ���ǳɹ���
			addChatMessageToQueue(rcvMessage, true);

			// isMessageCome = true;
			// ��Ϣ֪ͨ
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
				friendsInfoMap.put(from, friend);// ���������Ϣ
			}

			notifyMainUIMsgCome(name, msg.getBody());

			// ֪ͨ�ϲ�UI
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
	 * ����������Ϣ�������Ƿ�Ϊ������Ϣ
	 * 
	 * @param msgBody
	 * @return
	 */
	private boolean onCmdMessageCome(String msgBody) {
		try {
			// �ж��Ƿ�Ϊ����������Ϣ
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
					// Ҫ���²����Լ��ĵ���Ϣ
					final String userAccount = this.getTigaseAccount();
					final String password = this.getTigasePassword();
					if (!userAccount.equals(friendAccount)) {
						// ���������̣߳����ڸ�Ϊ�첽����
						new Thread() {
							@Override
							public void run() {
								try {
									// ��ȡweb���ѵ���Ϣ
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
										friendNew.setAuthenticated("1");// �Ѿ���֤��
									}

									// �ӱ���ȡ������˵���Ϣ
									if (classmateDao == null) {
										classmateDao = new ClassmateDao(null);
									}
									ContactsEntity friend = classmateDao
											.getFriendInfoByAccount(
													friendAccount, userAccount);
									// û���˺���ͨ������id��ȡ
									if (friend == null) {
										String friendBaseInfoId = friendNew
												.getBaseInfoId();
										// ����Ļ���id
										if (friendBaseInfoId.length() < 19) {
											return;
										}
										friend = classmateDao
												.getFriendInfoByBaseInfoIds(
														friendBaseInfoId,
														userAccount);
										// ��ȻΪ�� +++lqg+++ ���ܻ�������ϵ���ظ��Ĵ���
										if (friend == null) {
											// ��������id������������id��ȡ���ҵ���¼��ɾ����������µļ�¼��ȥ
											String baseInfoIdBak = null;
											String name = friendNew.getName();
											String baseIds[] = friendBaseInfoId
													.split(",");
											for (int i = 0; i < baseIds.length; ++i) {
												if (baseIds[i] != null
														&& !baseIds[i]
																.equals("")) {
													// ����Ļ���id
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

											// �����Ƿ�ɾ���ɹ�
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

									// ���»�ȡ��ϵ���б���Ϣ
									mContactsEntityMap = classmateDao
											.getAllcontactsEntity(userAccount);
									// �����ڴ��к�����Ϣ
									if (friendsInfoMap != null
											&& friendsInfoMap
													.containsKey(friendAccount)) {
										friendsInfoMap.remove(friendAccount);
										friendsInfoMap.put(friendAccount,
												friend);
									}

									// ֪ͨ�ϲ�UI
									Bundle bundle = new Bundle();
									// ��Ϣҳ��ֻҪ����Ϣ��Ҫ���£�������ϵ��ҳ��ֻ���û�ͼ�����ʱ����Ҫ����
									bundle.putString("from", "updateUI");// ֻ��UIͼƬ����
									messageIntent.putExtras(bundle);
									sendBroadcast(messageIntent);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}.start();

						return true;
					} else {
						return true;// �����Լ��ĺ��Ѹ���ͷ��͸��Լ�û��ϵ����ʱ���Լ�����ͷ��Ҳ���ߵ���������
					}
				} else if (msgBody
						.startsWith(APPConstant.CMD_PREFIX_GROUPCHAT_INVITE)) {
					CYLog.i(TAG, "�յ�Ⱥ����������Ϣ!");
					String groupFilterMsg = msgBody
							.substring(APPConstant.CMD_PREFIX_GROUPCHAT_INVITE
									.length());

					// ֪ͨ�ϲ�UI������������UI�Ľ���һ�ɲ���broadcast�ķ�ʽ�����ִ���ṹһ��
					Bundle bundle = new Bundle();
					bundle.putString("groupFilterMsg", groupFilterMsg);
					groupInviteIntent.putExtras(bundle);
					sendBroadcast(groupInviteIntent);
					return true;
				} else if (msgBody
						.startsWith(APPConstant.CMD_PREFIX_FORCE_SUBSCRIBE)) {
					CYLog.i(TAG, "�յ�Ⱥ����ǿ�ƶ�����Ϣ!");
					String groupId = msgBody
							.substring(APPConstant.CMD_PREFIX_FORCE_SUBSCRIBE
									.length());

					// ���¶��Ľڵ�
					joinGroupChatRoom(groupId);
					return true;
				} else if (msgBody
						.startsWith(APPConstant.CMD_PREFIX_FRIEND_ADD_REQUEST)) {
					CYLog.i(TAG, "�յ������������!");
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
					CYLog.i(TAG, "�յ�������Ӿܾ�!");
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
					// ���Ǻ��ѣ��������յ���ɾ����Ϣ
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
					CYLog.i(TAG, "�յ��������ͬ��!");
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
	 * ֪ͨ��Ϣ����
	 */
	private void notifyMainUIMsgCome(String from, String msg) {
		CYLog.i(TAG, "==========�յ���������Ϣ  From===========" + from);
		CYLog.i(TAG, "==========�յ���������Ϣ  Body===========" + msg);
		// CYLog.d(TAG, "setIsChatingWithWho = "+isChatingWithWho);
		if (!isChatingWithWho.equals(from)) {
			AppEngine.getInstance(getBaseContext()).onNewsCome();
			if (msg.startsWith(APPConstant.CMD_PREFIX_FILE_SEND)) {
				int start = msg.lastIndexOf("/");
				String type = msg.substring(start - 1, start);
				// CYLog.d(TAG,"=======   "+type+"   =====");
				if (type.equals("" + APPConstant.PICTURE)) {// �����ļ�������ʾ
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
				me = me.split("#")[0] + "#����" + numflag + "����������Ϣ��";
			}
			if (me != null) {
				final Notification notification = new Notification(

				R.drawable.ic_launcher, from + "��" + msg,
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
	 * ���͵ĵ�����Ϣ�汾����Ϣ�б������ݿ�
	 * 
	 */
	private synchronized void addChatMessageToQueue(ChatMessage chatMessage,
			boolean saveToDatabase) {
		try {
			// �Ƿ�����ݿ�
			if (saveToDatabase) {
				// ��Ӵ���Ϣ�����ݿ���
				chatMessageDao.addChatMessage(chatMessage);
			}
			// ����������Ϣ���뵽������Ϣ�б����ʺŴ��
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
			chatMessageList.addAll(chatMessageListtemp);// δ����Ϣ˳��Ӧ�����µ��������棬����������棬��Ϊ�ǵ���ȡ��

			unreadChatMessageMap.put(chatMessage.getOwner(), chatMessageList);// update
			if (saveToDatabase) {
				// ����Ϣѡ�� ����ѯ�����˵�����Ŀ���ڣ�������Ϊ����������Ŀ�����򣬽��˵�����Ŀ��ӵ���Ϣѡ��б���
				// ��������chatFragment
				ChatItem rcvItem = null;
				String owner = chatMessage.getOwner();
				if (chatItems == null) {
					chatItems = chatItemDao
							.getAllChatItem(this.userSelfContactsEntity
									.getAccountNum());
				}
				for (ChatItem item : chatItems) {
					// �ж�������item�����е�item���Ƿ����
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
				// ��������ڣ�����һ���µ�item//ȫ���½�����Ȼ����������Ϊ��
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
					friendsInfoMap.put(owner, contactsEntity);// ���������Ϣ
				}

				if (contactsEntity != null) {
					rcvItem.setName(contactsEntity.getName());
					// ������Ϣ��ͼƬͨ���˺Ż�ȡ��ϵ�˵���Ϣ��ȡ
					// rcvItem.setIcon(contactsEntity.getPicture());
				} else {
					rcvItem.setName(chatMessage.getOwner());
					// rcvItem.setIcon("");// �����ָ�����
				}

				rcvItem.setType(ChatItem.PRIVATECHATITEM);
				rcvItem.setLatestMessage(chatMessage.getMessageContent());
				rcvItem.setTime(chatMessage.getTime());
				// δ����Ϣ+1
				// (rcvItem) {
				// �����Լ����ͳ�ȥ����Ϣ
				if (!this.getTigaseAccount().equals(
						chatMessage.getSenderAccount())) {
					rcvItem.setUnread(unreadnum + 1);
					// CYLog.e(TAG, "rcvItem.setUnread= " +
					// rcvItem.getUnread());
				}
				// }

				// ��������item���ڶ�����ǰ�棬�ֻ���ʾ�������б�Ķ���
				chatItems.add(0, rcvItem);
				// ��chatItem���

				chatItemDao.deleteAndSave(rcvItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "addChatMessageToQueue " + e.toString());
		}
	}

	// // /**
	// // * �յ���Ϣÿ��֪ͨһ��ui
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
	 * �������ݿ��е�һ����Ϣ
	 */
	public void updateChatItemToDb(ChatItem chatItem) {
		try {
			chatItemDao.updateChatItem(chatItem);

			// ��ĳһ��chatitem�ö�
			// chatItems.remove(chatItem);
			// chatItems.add(0, chatItem);
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
		}
	}

	/**
	 * ��roster���£�֪ͨUI���»�ȡ��ϵ��
	 */
	public void sendRosterIntentBroadcast() {
		// �ȸ�����ϵ���б���֪ͨUI
		updateContactsOnRoster();
	}

	public void onEventBackgroundThread(Roster roster) {
		this.roster = roster;
		sendRosterIntentBroadcast();
	}

	public void onEventBackgroundThread(FriendAddRecvEntity friendAddRecvEntity) {
		try {
			String userAccount = this.getTigaseAccount();
			// �յ������������
			if (APPConstant.CMD_PREFIX_FRIEND_ADD_REQUEST
					.equals(friendAddRecvEntity.getCmdType())) {

				// �Ѿ��Ǻ���,������Ϣ
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
				// �ܾ������������
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
					// ����ɾ��
					ContactsEntity contactsEntity = this
							.getFriendInfoByAccount(friendAccount);
					if (contactsEntity == null) {
						// ˢ��ҳ��
						if (this.friendsInfoMap != null
								&& friendsInfoMap.containsKey(friendAccount)) {
							friendsInfoMap.remove(friendAccount);
							this.sendBroadcast(rosterIntent);
						}
						return;
					}

					if ("�ҵĺ���".equals(contactsEntity.getClassName())) {
						new Thread() {
							public void run() {
								// ɾ������
								sendFriendAddDecline(friendAccount, 2);
							}
						}.start();
					}
				}
			} else if (APPConstant.CMD_PREFIX_FRIEND_ADD_AGREE
					.equals(friendAddRecvEntity.getCmdType())) {

				// �Ѿ��Ǻ���,������Ϣ
				final String friendAccount = friendAddRecvEntity
						.getAccountNum();
				if (isMyFriend(friendAccount)) {
					return;
				}

				// ��������
				final FriendAddSendEntity friendAddSendEntity = this
						.getFriendAddEntity(friendAccount);
				friendAddSendEntity
						.setCmdType(APPConstant.CMD_PREFIX_FRIEND_ADD_AGREE);
				new Thread() {
					public void run() {
						EventBus.getDefault().post(friendAddSendEntity);

						// ��ȡ������Ϣ������UI
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
	 * ���͹㲥֮ǰ����鱾���Ƿ�����ϵ����Ϣ����û�У���������ȡ�������ڴ���ϵ�˱�İ���˲����
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
				if (contactsEntity == null) {// web�ϻ�ȡ
					// web�ϻ�ȡ�ĺ�����Ϣû�д�����,����������
					contactsEntity = LoginUtils.getFriendInfo(userAccount,
							password, friendAccount);

					if (contactsEntity == null) {
						contactsEntity = new ContactsEntity();
						contactsEntity.setUserAccount(userAccount);
						contactsEntity.setAccountNum(friendAccount);
						contactsEntity.setAuthenticated("0");
						contactsEntity.setHasAllClassmates(0);
					} else {
						// �����ȡ����ϵ�ˣ�������һ����ȷ��û�а༶��Ϣ
						contactsEntity.setUserAccount(userAccount);

						// �ж��Ƿ�Ϊ�༶ͬѧ����������±�����Ϣ
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

									// ���������ݿ�󣬸����ڴ�
									mContactsEntityMap = classmateDao
											.getAllcontactsEntity(userAccount);
									sendBroadcast(rosterIntent);// ֪ͨUI
									break;
								}
							}

							// �ǰ༶ͬѧ
							if (i == baseIds.length) {
								ContactsEntity temp = classmateDao
										.getFriendInfoByAccount(friendAccount,
												userAccount);
								if (temp == null) {
									contactsEntity.setClassName("�ҵĺ���");
									classmateDao
											.addContactsEntity(contactsEntity);
								} else {
									classmateDao
											.updateContacsEntity(contactsEntity);
								}
							}
						} else {
							contactsEntity.setClassName("İ����");
							contactsEntity.setAuthenticated("1");
							contactsEntity.setHasAllClassmates(1);
						}
					}
				}

				friendsInfoMap.put(friendAccount, contactsEntity);// ���������Ϣ
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
	 * Ⱥ���޸��Ժ󣬺��潫���ٶ����ṩ��ȡroster�ķ�����tigase�Ķ������Լ�ά��
	 * 
	 * @return
	 */
	public Roster getRoster() {
		if (roster == null) {
			EventbusCMD mEventbusCMD = new EventbusCMD();
			mEventbusCMD.setCMD(EventbusCMD.SEND_ROSTER);
			EventBus.getDefault().post(mEventbusCMD);// ȡroster
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
	// // �����
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
	// * ��ȡ�û�sign
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
	// * �ܾ�����������
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
	// * ��ȡ���������Ƿ����ĳ��Ⱥ����
	// */
	// public GroupChatRoom getGroupChatRoomByName(String inputRoomName) {
	// return GroupChat.getGroupChatRoomByName(chatMsgService.getConnection(),
	// inputRoomName);
	// }

	// /**
	// * ����Ⱥ����
	// */
	// public void createGroupChatRoom(String inputRoomName) {
	// GroupChat.createGroupChatRoom(chatMsgService.getConnection(),
	// inputRoomName);
	// }

	// public MultiUserChat NewMultiUserChat(String roomJid) {
	// return new MultiUserChat(chatMsgService.getConnection(), roomJid);
	// }

	/**
	 * ��ȡ��½�û�����δ��֤��ϵ�˵�����
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
	 * ��ȡ�������ݿ��е���ϵ���б��Ѿ������
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
	 * �Ƿ��Ǻ���
	 * 
	 * @param friendAccount
	 * @return
	 */
	public boolean isMyFriend(final String friendAccount) {
		try {
			// ʹ�ñ������ݿ��ж�
			if (classmateDao == null) {
				this.classmateDao = new ClassmateDao(this);
			}
			ContactsEntity contactsEntity = classmateDao
					.getFriendInfoByAccount(friendAccount,
							this.getTigaseAccount());
			if (contactsEntity != null) {
				// �����д�����Ϣ
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
	 * ������Ӻ��ѵ�����
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
	 * �ܾ���Ӻ��ѵ��������ɾ������
	 * 
	 * @param friendAccount
	 * @cType 0 �ܾ������������ 1����ɾ������ 2�յ�����ɾ��������
	 */
	public void sendFriendAddDecline(String friendAccount, int cType) {
		try {
			// ����ɾ�����ѻ��߱���ɾ��
			FriendAddSendEntity friendAddEntity = getFriendAddEntity(friendAccount);
			if (cType == 1 || cType == 2) {
				friendAddEntity
						.setCmdType(APPConstant.CMD_PREFIX_FRIEND_ADD_DECLINE);
				EventBus.getDefault().post(friendAddEntity);
			}

//			// ɾ����ϵ��
//			ClassmateDao classmateDao = new ClassmateDao(
//					this.getApplicationContext());
//			String userAccount = this.getTigaseAccount();
//			classmateDao.deleteContactsEntity(userAccount, friendAccount);
			
			// ɾ�������¼
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

				// ɾ����ϵ��
				if (classmateDao == null) {
					classmateDao = new ClassmateDao(
						this.getApplicationContext());
				}
				classmateDao.deleteContactsEntity(userAccount, friendAccount);
				
				// ���������ݿ�󣬸����ڴ�
				mContactsEntityMap = classmateDao
						.getAllcontactsEntity(userAccount);
				sendBroadcast(rosterIntent);// ֪ͨUI ������ϵ��ҳ��
			} catch (Exception e) {
			}

			// ֪ͨ�ϲ�UI
			Bundle bundle = new Bundle();
			// ��Ϣҳ��ֻҪ����Ϣ��Ҫ���£�������ϵ��ҳ��ֻ���û�ͼ�����ʱ����Ҫ����
			bundle.putString("from", "updateUI");// ֻ��UIͼƬ����
			messageIntent.putExtras(bundle);
			sendBroadcast(messageIntent);// ֪ͨUI����,����chatFragment

			// �ܾ���ӻ�������ɾ������
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
	 * ͬ����Ӻ��ѵ�����
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
	 * �˳���һ�������������������
	 */
	public void quitLastTigaseConnection() {
		try {
			// �˳��������û����Ե�¼
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
	 * �����˳��������󣬵�¼
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
	// * ��ȡ��½�û���ͼƬurl��Ϣ
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
	 * ��½�û��ṹ������ֱ�ӱ�¶��ȥ�������޸�
	 */
	public ContactsEntity getUserSelfContactsEntity() {
		try {
			if (userSelfContactsEntity == null) {
				// �ӱ������ݿ��ȡ�û���Ϣ
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

	// // ����baseInfoId��ȡ��ϵ�˰༶��Ϣ
	// public String getDepartmentByBaseInfoId(String baseinfoId) {
	// String departmentInfo = departmentDao.getDepartment(baseinfoId);
	// String[] spitDapartmentInfo = departmentInfo.split(",");
	// return spitDapartmentInfo[spitDapartmentInfo.length - 1];
	// }

	/**
	 * ������Ϣ���£���֪ͨ���еĺ���
	 * 
	 * @param selfContactsEntity
	 *            notifyAllFriends �Ƿ�֪ͨ���еĺ��ѣ����±�����Ϣ
	 */
	public synchronized void updateSelfContactsEntity(
			ContactsEntity selfContactsEntity, boolean notifyAllFriends) {
		try {
			if (classmateDao == null) {
				classmateDao = new ClassmateDao(this);
			}
			classmateDao.updateSelfContactsEntity(selfContactsEntity);

			if (notifyAllFriends) {
				// ֪ͨ������֤���ĺ���
				final String userAccount = this.getTigaseAccount();
				final List<ContactsEntity> contactsEntityList = classmateDao
						.getAllContactsEntityList(userAccount);
				for (int i = 0; i < contactsEntityList.size(); ++i) {
					final String accountNum = contactsEntityList.get(i)
							.getAccountNum();
					String auth = contactsEntityList.get(i).getAuthenticated();
					if (auth == null || !auth.equals("1")) {
						continue;// û����֤
					}
					if (accountNum == null || accountNum.equals("")) {
						continue;// û���˺�
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
	 * ���˺�ΪaccountNum�ĺ��ѷ���֪ͨ��Ϣ
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
			mSendChatMessage.setMessageType("notifyall");// ��Ϣ���ͱ��
			EventBus.getDefault().post(mSendChatMessage);// ����Ϣ���ȴ�����
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ɾ����½�û��ı��ؼ�¼��Ϣ��û��ɾ��ϵ����Ϣ
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
	 * ���±��ظ�����Ϣ���groupName�ֶβ���Ⱥ���¼��ӵ�����
	 * 
	 * @param groupChatRoomEntity
	 */
	public void updateLocalGroupChatData(GroupChatRoomEntity groupChatRoomEntity) {
		if (groupChatDao == null) {
			groupChatDao = new GroupChatDao(this);
		}

		String groupId = groupChatRoomEntity.getGroupId();
		// ���±��ظ�����Ϣ��
		updateSelfGroupInfoToUserProfile(groupId);

		// ���±���Ⱥ�ı�
		if (!groupChatDao.isGroupChatRoomEntityExisted(
				groupChatRoomEntity.getUserAccount(), groupId)) {
			groupChatDao.addGroupChatEntity(groupChatRoomEntity);
		} else {
			groupChatDao.updateGroupChatEntity(groupChatRoomEntity);
		}

		groupChatRoomMap = null;
		this.checkGroupChatRoomMap();
		sendBroadcast(rosterIntent); // ֪ͨUI����
	}

	/**
	 * ɾ�����������������ص�������Ϣ
	 */
	public synchronized void deleteLocalGroupChatData(String groupId) {
		if (groupChatDao == null) {
			groupChatDao = new GroupChatDao(this);
		}

		// ���±��ظ�����Ϣ��
		deleteSelfGroupInfoToUserProfile(groupId);

		// ���±���Ⱥ�ı�
		if (groupChatDao.isGroupChatRoomEntityExisted(getTigaseAccount(),
				groupId)) {
			groupChatDao.deleteGroupChatEntity(getTigaseAccount(), groupId);
		}

		groupChatRoomMap = null;
		this.checkGroupChatRoomMap();

		// ɾ������������ص�ChatItem,�������ڴ�
		chatItemDao.deleteChatItem(groupId, this.getTigaseAccount(),
				ChatItem.GROUPCHATITEM);
		chatItems = null;
		this.checkChatItemList();

		// ɾ���������ҵ�������ʷ��Ϣ,�������ڴ�
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

		// ����UI
		sendBroadcast(rosterIntent); // ֪ͨUI����,����contactsFragment
		// ֪ͨ�ϲ�UI
		Bundle bundle = new Bundle();
		// ��Ϣҳ��ֻҪ����Ϣ��Ҫ���£�������ϵ��ҳ��ֻ���û�ͼ�����ʱ����Ҫ����
		bundle.putString("from", "updateUI");// ֻ��UIͼƬ����
		messageIntent.putExtras(bundle);
		sendBroadcast(messageIntent);// ֪ͨUI����,����chatFragment
	}

	/*
	 * ���˴���������,isDefaultGroupChatRoomΪfalse��ʾ��������һ�������ң���������Ĭ�ϰ༶������
	 */
	public void addGroupChatRoom(String roomName,
			Map<String, Integer> invitees, boolean isDefaultGroupChatRoom) {
		try {
			// ��һ�����õ�������id
			String nextAvailableId = null;
			// ��һ����ȷ���½�������id
			if (isDefaultGroupChatRoom) {
				nextAvailableId = departmentDao.getDepartmentId(roomName);
			} else {
				nextAvailableId = getAvailableIdOnCreatingGroup();
			}

			GroupChatRoomEntity groupChatRoomEntity = new GroupChatRoomEntity();
			groupChatRoomEntity.setGroupId(nextAvailableId);
			String userAccount = getTigaseAccount();
			groupChatRoomEntity.setCreaterAccount(userAccount);
			groupChatRoomEntity.setTargetOccupantsMap(invitees);// ���������
			groupChatRoomEntity.setGroupName(roomName);
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put(userAccount, 1);// �Լ���Ϊ����Ա����
			groupChatRoomEntity.setOccupantsMap(map);
			groupChatRoomEntity.setUserAccount(getTigaseAccount()); // ����ֶκ�Password�ֶ�����������Web������ʧ�ܺ�Ļָ�����
			groupChatRoomEntity.setPassword(getTigasePassword());
			groupChatRoomEntity
					.setFunctionType(GroupChatRoomEntity.FUNCTYPE_ADD);
			groupChatRoomEntity
					.setSyncType(GroupChatRoomEntity.SYNCTYPE_ADD_ROOM);

			// �ڶ�������tigase�ϴ��������ҽڵ�,�ȴ�����ִ�н��
			groupChatFuncStatus = new GroupChatFuncStatusEntity();
			EventBus.getDefault().post(groupChatRoomEntity);

			waitEventBusResultOfGroupFunc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �����˼���������
	 */
	public void inviteGroupChatRoom(String roomId, Map<String, Integer> invitees) {
		// ��������
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
	 * ��ȡ��ǰ���Դ�����Ⱥ��id
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
							.parseInt(str.substring(str.indexOf("-") + 1)); // ע���˷ָ����Ҫ��Ⱥ��������Ϣ�е������ַ���ͻ����#,_
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
	// //��һ��ɾ�������ҵĹ㲥
	//
	// }

	/**
	 * ��ȡ������������Լ��������г�Ա��Ϣ������id,����,ͼƬ
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
				// ���˵��Լ�
				if (friendAccount.equals(userAccount)) {
					continue;
				}

				if (this.isMyFriend(friendAccount)) {
					// ������Ϣ
					contactsEntity = this.getFriendInfoByAccount(friendAccount);
				} else {
					// İ������Ϣ
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
	 * �յ������ͬ�����������
	 * 
	 * @param roomName
	 * @param participants
	 */
	public void joinGroupChatRoom(String groupId) {
		CYLog.i(TAG, "groupId=" + groupId);
		// ����
		if (groupId == null || groupId.equals("")) {
			CYLog.i(TAG, "���ݸ�ʽ����ȷ!");
			return;
		}

		jobManager.addJobInBackground(new JoinGroupChatRoomJob(
				DataCenterManagerService.this, groupId));
	}

	/**
	 * �����Լ���group��Ϣ�������Ƿ���³ɹ�
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
				return false;// �Ѿ��������
			} else {
				buf.append(groupId);
			}

			userSelfContactsEntity.setGroupName(buf.toString());
			this.updateSelfContactsEntity(userSelfContactsEntity, false);

			// //group��Ϣ�ϴ�web +++lqg+++�޸�Ϊͳһ�й���Ա֪ͨ��������ͬ��
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

	// ����������,��Ⱥ��ɾȺ(Ⱥ����Ⱥ��ΪɾȺ)
	public void kickByUserId(String roomId, Map<String, Integer> targets) {
		// Ⱥ������������Ϣ���������Ҫ�ߵ�������
		CYLog.i(TAG, "kickByUserId is called!");
		if (targets == null || targets.size() == 0) {
			CYLog.i(TAG, "���ݸ�ʽ����ȷ!");
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

	// ȡ������������
	public void unsubscribeGroupChatRoom(String roomId) {
		GroupChatRoomEntity roomEntity = new GroupChatRoomEntity();
		roomEntity.setGroupId(roomId);
		roomEntity.setFunctionType(GroupChatRoomEntity.FUNCTION_UBSUBSSRIBE);
		CYLog.i(TAG, "����ȡ������������!");

		// ����ȡ��������������Ϣ
		groupChatFuncStatus = new GroupChatFuncStatusEntity();
		EventBus.getDefault().post(roomEntity);

		// �ȴ����
		waitEventBusResultOfGroupFunc();
	}

	/**
	 * ��groupId�ӱ��˺ŵ�groupName�ֶ���ɾ��
	 */
	private boolean deleteSelfGroupInfoToUserProfile(String groupId) {
		try {
			// ������Ϣ���groupName����Ϊid�б�
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
	 * ���������߼���Ⱥ���Һ������ͬ������
	 */
	public synchronized void waitEventBusResultOfGroupFunc() {
		while (groupChatFuncStatus.getFuncStatus() == GroupChatFuncStatusEntity.GROUPCHAT_UNRET) {
			CYLog.i(TAG, "���ڵȴ������Һ���ִ�з��ؽ��!");
		}

		// �õ��˺���ִ�еķ��ؽ��������handler�д���
		android.os.Message msg = new android.os.Message();
		msg.what = groupChatFuncStatus.getFuncStatus();
		msg.obj = groupChatFuncStatus.getGroupChatRoomEntity();
		groupChatHandler.sendMessage(msg);
	}

	/**
	 * Ⱥ������Ϣͬ����web������ ʹ��Ion��汾
	 */
	public void executeGroupInfoSync(final GroupChatRoomEntity roomEntity)
			throws Exception {
		String accountNum = roomEntity.getUserAccount();
		String password = roomEntity.getPassword();

		JsonObject jsonStr = new JsonObject();
		JsonObject jsonContentStr = new JsonObject();
		jsonContentStr.addProperty("accountNum", accountNum);
		jsonContentStr.addProperty("password", password);

		// ���Ϊ�����½�һ����¼
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
			CYLog.i(TAG, "���������ҵ�web�������ϳɹ�!");
		} else {
			CYLog.i(TAG, "���������ҵ�web��������ʧ��!");
		}
	}

	// /**
	// * Ⱥ������Ϣͬ����web������
	// * ʹ��httpClent��汾
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
	// // ���Ϊ�����½�һ����¼
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
	// // ��������ַ���
	// JSONObject jsonObject = new JSONObject(resultJson);
	// boolean success = jsonObject.getBoolean("success");
	// if (success) {
	// CYLog.i(TAG, "���������ҵ�web�������ϳɹ�!");
	// } else {
	// CYLog.i(TAG, "���������ҵ�web��������ʧ��!");
	// }
	// } catch (Exception e) {
	// CYLog.i(TAG, "���������ҵ�web��������ʧ��!");
	// e.printStackTrace();
	// try {
	// throw e;
	// } catch (Exception e1) {
	// e1.printStackTrace();
	// }
	// }
	// }

	// /**
	// * ����Ƿ���������Ⱥ��ͬ����Ϣδִ�гɹ���ִ�У���ɾ�����л��ļ�
	// */
	// private void checkGroupInfoSyncDirectory() {
	// try {
	// String dir = SearchSuggestionProvider.pathStr + File.separator
	// + "update_group_info";
	// File notifyFilePath = new File(dir);
	// if (!notifyFilePath.exists()) {
	// return;// û��Ŀ¼��ֱ�ӷ���
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
	// file.delete();// ɾ���ļ�
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
	 * ��ȡ���˺����е�Ⱥ����Ϣ����û�����������̴߳������ȡ��handler�ص�
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
	 * ������ͨȺ����Ϣ isCmd �Ƿ�Ϊ�����������Ϣ
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
		// ��ͨ��Ϣǰ������˺ţ�����
		if (!isCmd) {
			// ҵ���߼����������Ĵ���
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

		// ����Ϣ���ȴ�����
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

		// ������Ϣ����ʾҲ�����
		if (!isCmd) {
			// ���ı���Ϣ��װ�ɰ������ʹ���Ϣ�˺š�����ʱ�䡢id�����͵ȵ�ChatMessage����
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setMessageContent(messageBody);
			chatMessage.setMid(UUID.randomUUID().toString());
			chatMessage.setOwner(groupId);
			chatMessage.setUserAccount(userAccount);
			chatMessage.setSenderAccount(userAccount);
			chatMessage.setRecvAccount(groupId);
			// ���ô���Ϣ����ΪȺ����Ϣ
			chatMessage.setType(ChatMessage.GROUPCHATMESSAGE);
			chatMessage.setTime(new Date(System.currentTimeMillis()));
			chatMessage.setIsRead(0);// ����Ϊδ����Ϣ
			chatMessage.setSendSucc(sendSucc);
			// ���ز����ʱ�����͵�Ⱥ����Ϣ���������ű����ڴ���Ϣ�б��ͷ��������͵���Ϣһ����
			addGroupChatMessageToQueue(chatMessage, sendSucc);

			// ֪ͨ�ϲ�UI
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
				// ������Ϣʧ��+++lqg+++ ������Ӳ��ȴ�ʩ
				CYLog.i(TAG, " group cmd msg failed");
			} else {
				// ��ͨȺ����Ϣʧ�ܣ�����û��Ⱥ����Ϣ
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
	 * ��ȡ���ص�Ⱥ��
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
	 * �����ȡ����Ⱥ����Ϣ������һ��Ⱥ����Ϣ��Ⱥ�Ĺ�����Ϣ
	 */
	public void onEventBackgroundThread(GroupChatMessage groupChatMessage) {
		CYLog.i(TAG, "�洢��ȡ����Ⱥ����Ϣ!");
		checkGroupChatRoomMap();

		String groupId = groupChatMessage.getGroupId();
		String message = groupChatMessage.getMessage();
		CYLog.i(TAG, "groupId=" + groupId + "   message=" + message);

		boolean ret = dealGroupCmdMessage(groupId, message);
		if (ret) {
			return;// ������Ϣ
		}

		// ���洦����ͨ��Ⱥ����Ϣ��Ҫ���˵����͸��Լ�����ͨȺ����Ϣ
		String userAccount = this.getTigaseAccount();
		String password = this.getTigasePassword();
		try {
			// ȡ����Ϣ����
			int index = message.indexOf("_");
			String from = message.substring(0, index);
			String message2 = message.substring(index + 1);
			int index2 = message2.indexOf("_");
			String name = message2.substring(0, index2);
			String messageBody = message2.substring(index2 + 1);

			// ���˵��������˵���ͨȺ����Ϣ
			if (userAccount.equals(from)) {
				return;
			}

			if (messageBody.startsWith(APPConstant.CMD_PREFIX_FILE_SEND)) {
				String url = messageBody.replaceAll(
						APPConstant.CMD_PREFIX_FILE_SEND, "");
				new DownloadFileTask(url, null, null, handler).execute("");
			}

			// ����û�д�����Ϣ
			if (getFriendInfoByAccount(from) == null) {
				// �ڱ�������Ĭ�ϵ���Ϣ
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

			// ���ı���Ϣ��װ�ɰ������ʹ���Ϣ�˺š�����ʱ�䡢id�����͵ȵ�ChatMessage����
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setMessageContent(messageBody);
			chatMessage.setMid(UUID.randomUUID().toString());
			chatMessage.setOwner(groupId);
			chatMessage.setUserAccount(userAccount);
			chatMessage.setSenderAccount(from);
			chatMessage.setRecvAccount(userAccount);
			// ���ô���Ϣ����ΪȺ����Ϣ
			chatMessage.setType(ChatMessage.GROUPCHATMESSAGE);
			chatMessage.setTime(new Date(System.currentTimeMillis()));
			chatMessage.setIsRead(0);// ����Ϊδ����Ϣ
			chatMessage.setSendSucc(true);// ���յ���Ⱥ����Ϣʼ�ճɹ�

			// �����ڴ���Ϣ�б����
			addGroupChatMessageToQueue(chatMessage, true);

			isMessageCome = true;

			// ��Ϣ֪ͨ
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
			// ��Ϣ֪ͨ
			notifyMainUIMsgCome(groupName, messageBody);

			// ֪ͨ�ϲ�UI
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
	 * ����Ⱥ��������Ϣ, �����Ƿ�Ϊ������Ϣ
	 * 
	 * @param groupId
	 * @param msg
	 */
	private boolean dealGroupCmdMessage(String groupId, String msg) {
		try {
			if (!msg.startsWith(APPConstant.CMD_PREFIX)) {
				return false;// ��������Ϣ
			}

			if (msg.startsWith(APPConstant.CMD_PREFIX_GROUPCHAT_ACCEPT_INVITE)) {
				CYLog.i(TAG, "�յ��������ߵĴ�!��������ͬ�����������");

				jobManager.addJobInBackground(new ReceiveInviteeAcceptReplyJob(
						DataCenterManagerService.this, groupId, msg));

				return true;

			} else if (msg
					.startsWith(APPConstant.CMD_PREFIX_GROUPCHAT_DECLINE_INVITE)) {
				CYLog.i(TAG, "�յ��������ߵĴ�!�������߾ܾ�����������");
				return true;

			} else if (msg.startsWith(APPConstant.CMD_PREFIX_GROUPCHAT_KICK)) {
				CYLog.i(TAG, "�յ�����������!");

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
	 * ���Ⱥ����Զ����´ӱ������ݿ⵼��
	 */
	public void checkGroupChatRoomMap() {
		if (groupChatRoomMap == null) {
			if (groupChatDao == null) {
				groupChatDao = new GroupChatDao(this);
			}
			// ��������Ⱥ�ı�
			groupChatRoomMap = groupChatDao.getAllGroupChatRoomEntityMap(this
					.getTigaseAccount());
			if (groupChatRoomMap == null) {
				groupChatRoomMap = new HashMap<String, GroupChatRoomEntity>();
			}
		}
	}

	/**
	 * ���ChatItem���Զ����´ӱ������ݿ⵼��
	 */
	private void checkChatItemList() {
		if (chatItems == null) {
			if (chatItemDao == null) {
				chatItemDao = new ChatItemDao(this);
			}
			// �����ڴ��е�chatItems
			chatItems = chatItemDao.getAllChatItem(this.userSelfContactsEntity
					.getAccountNum());
		}
	}

	/**
	 * ���͵�Ⱥ����Ϣ�汾����Ϣ�б������ݿ�
	 */
	private synchronized void addGroupChatMessageToQueue(
			ChatMessage chatMessage, boolean saveToDatabase) {
		try {
			if (saveToDatabase) {
				// ��Ӵ���Ϣ�����ݿ���
				chatMessageDao.addChatMessage(chatMessage);
			}

			// ����������Ϣ���뵽������Ϣ�б�
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
			chatMessageList.addAll(chatMessageListtemp);// δ����Ϣ˳��Ӧ�����µ��������棬����������棬��Ϊ�ǵ���ȡ��
			unreadChatMessageMap.put(groupId, chatMessageList);// update
			if (saveToDatabase) {
				// ����Ϣѡ�� ����ѯ������Ⱥ����Ŀ���ڣ�������Ϊ����������Ŀ�����򣬽���Ⱥ����Ŀ��ӵ���Ϣѡ��б���
				// ��������chatFragment
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
					// ��������ڣ�����һ���µ�item
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
						groupChatRoomMap.put(groupId, groupChatRoomEntity);// ���������Ϣ
					}

					if (groupChatRoomEntity != null) {
						foundItem.setName(groupChatRoomEntity.getGroupName());
						foundItem.setIcon("");
					} else {
						foundItem.setName(groupId);
						foundItem.setIcon("");// �����ָ�����
					}
					foundItem.setType(ChatItem.GROUPCHATITEM);
					foundItem.setIcon(Integer.toString(20)); // ����һ��Ⱥ����Ϣʱ������������chatitem�Ѿ����ڣ�����ִ�в���������ŵ������޷�����Ⱥͷ�񡣴���������
				}

				foundItem.setLatestMessage(chatMessage.getMessageContent());
				foundItem.setTime(chatMessage.getTime());
				foundItem.setIcon(Integer.toString(20));
				// δ����Ϣ+1
				// (rcvItem) {
				// �����Լ����ͳ�ȥ����Ϣ
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
				// ��������item���ڶ�����ǰ�棬�ֻ���ʾ�������б�Ķ���
				chatItems.add(0, foundItem);
				// ��chatItem���
				chatItemDao.deleteAndSave(foundItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "addGroupChatMessageToQueue " + e.toString());
		}
	}

	/**
	 * �жϵ�½�û��Ƿ�Ϊ����Ա
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
			CYLog.i(TAG, "isAdminOfGroupChatRoom����ִ�г����쳣!");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * �õ������ҵĴ����ߣ���Ⱥ��
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
	 * �жϱ��û��Ƿ��������Ҵ����ߣ���Ⱥ��
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
			CYLog.i(TAG, "isCreaterOfGroupChatRoom����ִ�г����쳣!");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * �жϴ��������Ƿ���Ĭ�ϵİ༶������ �û��Լ�������������id��ʽΪ���û��˺� + "-" +�����ұ�� Ĭ�ϰ༶�����ҵ�id��Ϊ�༶id
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
	 * �Զ����������༶������
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
	 * ɾ����ϵ�˵�������
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

			//��ʱ����ҳ��
			new Thread() {
				public void run() {
					try {
						sleep(500);
						
						// ֪ͨ�ϲ�UI
						Bundle bundle = new Bundle();
						// ��Ϣҳ��ֻҪ����Ϣ��Ҫ���£�������ϵ��ҳ��ֻ���û�ͼ�����ʱ����Ҫ����
						bundle.putString("from", "updateUI");// ֻ��UIͼƬ����
						messageIntent.putExtras(bundle);
						sendBroadcast(messageIntent);// ֪ͨUI����,����chatFragment
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
