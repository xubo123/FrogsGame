package com.hust.schoolmatechat.pushedmsgservice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

import com.hust.schoolmatechat.ChatMsgservice.ChatMsgService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.dao.ChannelDao;
import com.hust.schoolmatechat.dao.ChatItemDao;
import com.hust.schoolmatechat.dao.ClassmateDao;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.AppEngine;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.utils.StreamUtils;

import de.greenrobot.event.EventBus;

public class PushedMsgService extends Service {
	private static final String TAG = "PushedMsgService";

	// private List<Channel> channels = null;

	// private ChatItemService chatItemService = null;
	private ChannelDao channelDao = null;
	// private List<ChatItem> chatItems = null;
	String httpJsonnews, httpJsonchannel;
	public String clientId = "";
	int connectCount = 0;
//	public static final String TOPIC = APPConstant.GENERAL_CHANNEL_NAME;
//	public static final String user = APPBaseInfo.PUSH_SERVICE_ACCOUNT;
//	public static final String passwd = APPBaseInfo.PUSH_SERVICE_PASSWD;
	private MqttClient mqttClient;
	MqttConnectOptions options;
	TimeBroadcastReceiver receiverTime;
	WakeupBroadcastReceiver receiverW;
	private boolean isMQTTConnect = false;
	MessageThread thread;
	private ContactsEntity userSelfContactsEntity;

	public void onEventBackgroundThread(ContactsEntity userSelfContactsEntity) {
		this.userSelfContactsEntity = userSelfContactsEntity;
		channelDao = new ChannelDao(this);
		List<Channel> channel = channelDao.getChanenelsALL();
		ChannelList channelList = new ChannelList();
		channelList.setList(channel);
		getUserChannelList(channelList);
	}

	public void getUserChannelList(ChannelList channelList) {
		ChannelList cl = new ChannelList();

		String[] userdata = new String[2];

		userdata[0] = userSelfContactsEntity.getChannels();
		userdata[1] = userSelfContactsEntity.getIntrestType();
		CYLog.d(TAG, "===" + userSelfContactsEntity.getChannels());
		String[] channels = userdata[0].split(",");
		if (userdata[0].equals(""))
			channels = APPConstant.CHANNELLIST;

		for (int i = 0; channelList != null && i < channelList.getList().size(); i++) {
			String name = channelList.get(i).getcName();

			if (!name.equals(APPConstant.SCHOOL_HELPER_CHANNEL_NAME)) {
				int k = 0;
				for (k = 0; k < channels.length; k++) {
					if (name.equals(channels[k])) {
						cl.add(channelList.getList().get(i));
						break;
					}
				}
			}
		}
		EventBus.getDefault().post(cl.getList());
	}

	public boolean isUserInterest(SingleNewsMessage mSingleNewsMessage) {
		String userdata = "";
		userdata = userSelfContactsEntity.getIntrestType();
	//	CYLog.d(TAG, "===" + userSelfContactsEntity.getIntrestType());

		String[] myinterest = userdata.split(",");
		if (userdata.equals(""))
			myinterest = APPConstant.INTERESTLIST;

		String[] newstype = mSingleNewsMessage.getContent().split(",");
		for (int i = 0; i < myinterest.length; i++) {
			for (int j = 0; j < newstype.length; j++) {
				if (myinterest[i].equals(newstype[j])) {
					return true;
				}
			}
		}
		return false;
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

	public String getUserAccount() {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		return prefs.getString("USERNAME", "username");
	}

	@Override
	public void onCreate() {
		CYLog.i(TAG, "PushedMsgService created");
		if (thread == null) {
			thread = new MessageThread();
		}
		EventBus.getDefault().register(this);
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		StreamUtils.checkBaseInfo(prefs);
		HTTPGetChannelTask mGetTask = new HTTPGetChannelTask();
		mGetTask.execute();// 主动取频道
		// chatItemService = new ChatItemService(getApplicationContext());
		// channelService = new ChannelService(getApplicationContext());
		// channelDao = new ChannelDao(this);
		// final Intent dataCenterManagerIntent = new Intent(this,
		// DataCenterManagerService.class);
		// bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
		// Context.BIND_ABOVE_CLIENT);

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		CYLog.i(TAG, "PushedMsgService started");
		receiverTime = new TimeBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		registerReceiver(receiverTime, intentFilter);
		receiverW = new WakeupBroadcastReceiver();
		IntentFilter intentFilter1 = new IntentFilter(
				Intent.ACTION_USER_PRESENT);
		registerReceiver(receiverW, intentFilter1);

		getUserSelfContactsEntity();
		connectCount = 0;
		boolean f = startMQTT();
		CYLog.i(TAG, "startMQTT " + f);
		getPushedServerNewsActively();

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
				reStartService();

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
				reStartService();
			}
		}
	}

	public void reStartService() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		StreamUtils.checkBaseInfo(prefs);
		String auto = prefs.getString("AUTO", "no");
		if (auto == null || auto.equals("auto")) {// 自动登陆才能重新启动
			if (!AppEngine.getInstance(getBaseContext()).isPushServiceWorked()) {
				Intent PushService = new Intent(PushedMsgService.this,
						PushedMsgService.class);
				CYLog.i(TAG, "PushService restart");
				startService(PushService);
			}
			if (!AppEngine.getInstance(getBaseContext()).isDataCenterWorked()) {
				Intent dataCenterManagerIntent = new Intent(
						PushedMsgService.this, DataCenterManagerService.class);
				CYLog.i(TAG, "dataCenterManagerService restart");

				startService(dataCenterManagerIntent);
			}
			if (!AppEngine.getInstance(getBaseContext())
					.isChatMsgServiceWorked()) {
				Intent chatIntent = new Intent(PushedMsgService.this,
						ChatMsgService.class);
				CYLog.i(TAG, "ChatMsgService  restart");

				startService(chatIntent);
			}
		}
		
	}

	public boolean startMQTT() {
		setIsMQTTConnect(false);
		try {
			if (mqttClient != null)
				mqttClient.disconnect(0);
			CYLog.d("restartMQTT", "mqttClient.disconnect(0);");
		} catch (MqttException e) {
			e.printStackTrace();
		}
		CYLog.d(TAG, "MQTTService start");
		clientId = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.ANDROID_ID);
		try {
			mqttClient = new MqttClient(APPConstant.getMQTTServiceURL(), clientId, new MemoryPersistence());
			mqttClient.setCallback(new PushCallback(this));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		options = new MqttConnectOptions();
		// 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
		options.setCleanSession(false);
		// 设置连接的用户名
		options.setUserName(APPBaseInfo.PUSH_SERVICE_ACCOUNT);
		// 设置连接的密码
		options.setPassword(APPBaseInfo.PUSH_SERVICE_PASSWD.toCharArray());
		// 设置超时时间 单位为秒
		options.setConnectionTimeout(10);
		// 设置会话心跳时间 单位为秒 服务器会每隔1.5*30秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
		options.setKeepAliveInterval(30);
		// 设置回调
		thread.start();

		/*
		 * GetPushedMessage mGetPushedMessage = new GetPushedMessage(this); //
		 * 根据频道名取新闻，后面有两个参数，第一个表示偏移量，第二个表示取推送新闻的组数，比如0,1表示从最新的开始取，取一组新闻
		 * List<PushedMessage> pushedmessagelist = mGetPushedMessage
		 * .viewPushedMessage(null, null, 0, 1); if (pushedmessagelist.size() ==
		 * 0) { // channelDao.initChannel(); CYLog.i(TAG, "主动取新闻");
		 * HTTPGetNewsTask mGetTask = new HTTPGetNewsTask();
		 * mGetTask.execute();// 主动取新闻，测试用 }
		 */
		return true;
	}

	/***
	 * 提供给数据中心调用
	 */
	public void getPushedServerNewsActively() {
		ChatItemDao chatItemDao = new ChatItemDao(this);
		List<ChatItem> chatItems;
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		chatItems = chatItemDao.getAllChatItem(prefs.getString("USERNAME",
				"username"));
		if (chatItems != null && chatItems.size() < 2) {// 若消息列表小于两条，判断为没有新闻，主动取一次
			HTTPGetNewsTask mGetTask = new HTTPGetNewsTask();
			mGetTask.execute();// 主动取新闻，测试用
		}
	}

	public boolean getIsMQTTConnect() {
		return isMQTTConnect;
	}

	public void setIsMQTTConnect(boolean ismqttconnect) {
		this.isMQTTConnect = ismqttconnect;
	}

	/***
	 * 从服务端获取消息
	 * 
	 * @author luoguangzhen
	 * 
	 */
	public class MessageThread extends Thread {
		// 运行状态

		@Override
		public void run() {
			/*
			 * if (dataCenterManagerService == null) { final Intent
			 * dataCenterManagerIntent = new Intent( PushedMsgService.this,
			 * DataCenterManagerService.class);
			 * bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
			 * Context.BIND_ABOVE_CLIENT); }
			 */
			while (!getIsMQTTConnect()) {

				// Toast.makeText(getApplicationContext(),
				// "push test start", Toast.LENGTH_SHORT).show();

				CYLog.d(TAG, "push connect Thread start");

				try {
					if (options == null) {
						options = new MqttConnectOptions();

						// 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
						options.setCleanSession(false);
						// 设置连接的用户名
						options.setUserName(APPBaseInfo.PUSH_SERVICE_ACCOUNT);
						// 设置连接的密码
						options.setPassword(APPBaseInfo.PUSH_SERVICE_PASSWD.toCharArray());
						// 设置超时时间 单位为秒
						options.setConnectionTimeout(10);
						// 设置会话心跳时间 单位为秒
						// 服务器会每隔1.5*30秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
						options.setKeepAliveInterval(30);
						// 设置回调
					}
					if (mqttClient == null) {
						try {
							mqttClient = new MqttClient(APPConstant.getMQTTServiceURL(), clientId,
									new MemoryPersistence());
							mqttClient.setCallback(new PushCallback(
									PushedMsgService.this));
						} catch (MqttException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (mqttClient != null && options != null) {
						mqttClient.connect(options);
						// Subscribe to all subtopics of homeautomation
						mqttClient.subscribe(APPConstant.GENERAL_CHANNEL_NAME, 1);
						channelDao = new ChannelDao(PushedMsgService.this);
						List<Channel> channel = channelDao.getChanenelsALL();
						ChannelList mChannelList = new ChannelList();
						mChannelList.setList(channel);
						for (int i = 0; i < mChannelList.getList().size(); i++) {
							if (!mChannelList
									.get(i)
									.getChannelId()
									.equals(APPConstant.SCHOOL_HELPER_CHANNEL_NAME)) {
								mqttClient.subscribe(mChannelList.get(i)
										.getChannelId(), 1);
//								CYLog.d(TAG, "mqttClient.subscribe--"
//										+ mChannelList.get(i).getChannelId());
							}
						}

						CYLog.d(TAG, "push connect thread4 success");
						setIsMQTTConnect(true);
						connectCount = 0;
					}
				} catch (Exception e) {
					e.printStackTrace();
					String massage = ("" + e).trim();
//					CYLog.d(TAG, "connectCount=" + connectCount
//							+ "push connect thread error1-" + massage);
					connectCount++;
					if (connectCount > APPConstant.MQTT_CONNECTION_COUNT_INTERVAL) {
						Intent PushService = new Intent(PushedMsgService.this,
								PushedMsgService.class);
						CYLog.i(TAG, "connectCount > 30,PushService restart");
						stopService(PushService);
					}
					if (massage.startsWith("客户机已连接")) {
						setIsMQTTConnect(true);
					} else {
						try {
							mqttClient = null;
							setIsMQTTConnect(false);
							Thread.sleep(APPConstant.CONNECTION_CHECK_INTERVAL);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							CYLog.d(TAG, "push connect thread sleep(10000)-"
									+ e1);
						}
					}
				}

			}
		}
	}

	public class PushCallback implements MqttCallback {
		private static final String TAG = "PushCallback";
		private ContextWrapper context;
		// int flag = 0;
		Channel channel;
		SingleNewsMessage mSingleNewsMessage;

		public PushCallback(ContextWrapper context) {
			this.context = context;
		}

		@Override
		public void connectionLost(Throwable cause) {
			// We should reconnect here
			CYLog.d("PushCallback", "push connectionLost");
			try {
				setIsMQTTConnect(true);
				if (thread != null) {
					thread.interrupt();
					thread = null;
				}
				// 休息10秒
				Thread.sleep(APPConstant.CONNECTION_CHECK_INTERVAL);
				if (thread == null) {
					setIsMQTTConnect(false);
					thread = new MessageThread();
					thread.start();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				CYLog.e(TAG, "PushCallback connectionLost thread sleep(10000)-"
						+ e);
			}

		}

		@Override
		public void messageArrived(MqttTopic topic, MqttMessage message) {
			try {
				String me = messageManage(topic + "",
						new String(message.getPayload()));
			} catch (Exception e) {
				e.printStackTrace();
				CYLog.d(TAG, e.toString());
			}
		}

		// 有信道更新信息到来
		public String messageManage(String topic, String message) {
			try {
			CYLog.d("PushCallback", "messageArrived" + topic);
			if (message.trim().startsWith("Enter")) {
				return "#";
			} else if (APPConstant.GENERAL_CHANNEL_NAME.equals(topic)) {
				CYLog.d("PushCallback", "message" + message);
				try {
					JSONTokener jsonParser = new JSONTokener(message);
					JSONObject js;

					js = (JSONObject) jsonParser.nextValue();

					JSONArray jsonArray = js.getJSONArray("list");
					ChannelList cl = new ChannelList();
					for (int i = 0; i < jsonArray.length(); i++) {
						channel = new Channel();

						String cName = (String) jsonArray.getJSONObject(i).get(
								"channelName");
						channel.setcName(cName);
						String channelID = ""
								+ jsonArray.getJSONObject(i).get("channelId");
						channel.setChannelId(cName);// name设为id
						String channelRemark = (String) jsonArray
								.getJSONObject(i).get("channelRemark");
						channel.setChannelRemark(channelRemark);
						String icon = "";
						if (!cName.equals(APPConstant.INTEREST_CHANNEL_NAME)) {
							icon = (String) jsonArray.getJSONObject(i).get(
									"channelIcon");
							try {
								icon = URLDecoder.decode(icon, "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
						channel.setIcon(icon);
//						CYLog.d("PushCallback", "get channle id : " + channelID);
//						CYLog.d("PushCallback", "get channle icon : " + icon);
//						CYLog.d("PushCallback", "get channle name : " + cName);
						cl.add(channel);

					}
					channelDao = new ChannelDao(PushedMsgService.this);
					channelDao.deleteAndSaveALL(cl.getList());
					getUserChannelList(cl);
					for (int i = 0; i < cl.getList().size(); i++) {
						if (!cl.get(i).getChannelId()
								.equals(APPConstant.SCHOOL_HELPER_CHANNEL_NAME)) {
							try {
								if (mqttClient != null)
									mqttClient.subscribe(cl.get(i)
											.getChannelId(), 1);
							} catch (MqttSecurityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (MqttException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							CYLog.d(TAG,
									"channel update mqttClient.subscribe--"
											+ cl.get(i).getChannelId());
						}
					}// 频道更新，重新监听
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					CYLog.e("PushCallback,messageManage", "" + e);
				}
				return "窗友新消息#频道信息更新";

			} else {
				CYLog.d("PushCallback", "message" + message);
				JSONTokener jsonParser = new JSONTokener(message);
				JSONObject js;
				PushedMessage pushmessage = new PushedMessage();
				try {
					js = (JSONObject) jsonParser.nextValue();
					String mPMId = js.optString("PMId");
					pushmessage.setPMId(mPMId);
					String mcName = js.optString("channelName");
					pushmessage.setcName(mcName);
					String mchannelId = js.optString("channelId");
					pushmessage.setChannelId(mchannelId);
					String micon = js.optString("icon");
					try {
						micon = URLDecoder.decode(micon, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					pushmessage.setIcon(micon);

					DateFormat format = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String time = js.optString("time");
					Date mTime;
					try {
						mTime = format.parse(time);
						pushmessage.setTime(mTime);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					List<SingleNewsMessage> ls = new ArrayList<SingleNewsMessage>();
					JSONArray jsonArrayl = js.getJSONArray("newsList");
					for (int i = 0; i < jsonArrayl.length(); i++) {
						mSingleNewsMessage = new SingleNewsMessage();
						String channelID = (String) jsonArrayl.getJSONObject(i)
								.get("channelId");
						mSingleNewsMessage.setChannelId(channelID);
						boolean isBreaking = jsonArrayl.getJSONObject(i)
								.getBoolean("breaking");
						mSingleNewsMessage.setBreaking(isBreaking);
						String PMId = (String) jsonArrayl.getJSONObject(i).get(
								"PMId");
						mSingleNewsMessage.setPMId(PMId);
						String newsUrl = (String) jsonArrayl.getJSONObject(i)
								.get("newsUrl");
						try {
							newsUrl = URLDecoder.decode(newsUrl, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						mSingleNewsMessage.setNewsUrl(newsUrl);
						int nid = jsonArrayl.getJSONObject(i).getInt("nid");
						mSingleNewsMessage.setNid(nid);
						String title = (String) jsonArrayl.getJSONObject(i)
								.get("title");
						mSingleNewsMessage.setTitle(title);
						String summary = (String) jsonArrayl.getJSONObject(i)
								.get("summary");
						mSingleNewsMessage.setSummary(summary);
						String icon = (String) jsonArrayl.getJSONObject(i).get(
								"icon");
						try {
							icon = URLDecoder.decode(icon, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						mSingleNewsMessage.setIcon(icon);
						time = js.optString("time");
						try {
							mTime = format.parse(time);
							mSingleNewsMessage.setTime(mTime);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							CYLog.e("===time", "" + e);
						}
//						CYLog.d("PushCallback",
//								"mSingleNewsMessage get channle pmid : " + PMId);
//						CYLog.d("PushCallback",
//								"mSingleNewsMessage get channle icon : "
//										+ micon);
//						CYLog.d("PushCallback",
//								"mSingleNewsMessage get channle name : "
//										+ mcName);
//						CYLog.d("PushCallback",
//								"mSingleNewsMessage get channle name : "
//										+ mSingleNewsMessage.getTime());
						String content = (String) jsonArrayl.getJSONObject(i)
								.get("content");
						mSingleNewsMessage.setContent(content);
						if (isUserInterest(mSingleNewsMessage)) {
							ls.add(mSingleNewsMessage);
							CYLog.d(TAG, mSingleNewsMessage.getTitle()
									+ "isUserInterest");
						} else {
							CYLog.d(TAG, mSingleNewsMessage.getTitle()
									+ "is not UserInterest");
						}

					}
					if (ls.size() > 0) {
						pushmessage.setNewsList(ls);
						mSingleNewsMessage = ls.get(0);
						pushmessage.setNewsSummary(mSingleNewsMessage
								.getSummary());
						EventBus.getDefault().post(pushmessage);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					CYLog.e("PushCallback", "" + e);
				}
				return mSingleNewsMessage.getTitle() + "#"
						+ mSingleNewsMessage.getSummary();
			}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}

		@Override
		public void deliveryComplete(MqttDeliveryToken token) {
			// We do not need this because we do not publish
		}

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		CYLog.i(TAG, "PushedMsgService destroyed");
		if (receiverW != null)
			unregisterReceiver(receiverW);
		if (receiverTime != null)
			unregisterReceiver(receiverTime);

		try {
			if (mqttClient != null)
				mqttClient.disconnect(0);
			CYLog.d("onDestroyMQTT", "mqttClient.disconnect(0);");
		} catch (MqttException e) {
			e.printStackTrace();
			CYLog.e(TAG, "mqttClient.disconnect(0) e" + e);
		}
		// Intent PushService = new Intent(PushedMsgService.this,
		// PushedMsgService.class);
		//
		// startService(PushService);
		setIsMQTTConnect(true);

		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
		EventBus.getDefault().unregister(this);
		super.onDestroy();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String auto = prefs.getString("AUTO", "no");
		if (auto == null || auto.equals("auto")// 如果是用户主动退出的话就不需要重启服务了
				|| connectCount > APPConstant.MQTT_CONNECTION_COUNT_INTERVAL) {
			CYLog.i(TAG, "unnormal onDestroy,restart");
			connectCount = 0;
			reStartService();
		}
	}

	String toast = null;

	public class HTTPGetNewsTask extends AsyncTask<Void, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			try {
				// Simulate network access.
				Thread.sleep(200);
			} catch (InterruptedException e) {
				return false;
			}
			HttpClient httpClient = new DefaultHttpClient();
			// 创建一个HttpGet对象
			HttpGet get = new HttpGet(APPBaseInfo.URL + APPConstant.GETNEWSURL);
			try {
				// 发送GET请求
				HttpResponse httpResponse = httpClient.execute(get);
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					// 读取服务器响应
					BufferedReader br = new BufferedReader(
							new InputStreamReader(entity.getContent()));
					String line = null;
					StringBuilder builder = new StringBuilder();
					while ((line = br.readLine()) != null) {
						builder.append(line);

					}
					httpJsonnews = builder.toString();
					return true;

				} else {
					toast = "网络连接错误21，请稍后重试";
				}
			} catch (Exception e) {
				e.printStackTrace();
				toast = "网络连接错误31，请稍后重试" + e;
			}

			return false;

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			if (success) {

				try {
					JSONArray jsonArray = new JSONArray(httpJsonnews);
					for (int i = 0; i < jsonArray.length(); i++) {

						new PushCallback(PushedMsgService.this).messageManage(
								"news", "" + jsonArray.getJSONObject(i));
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			// initDB();
			if (toast != null) {
				Toast.makeText(PushedMsgService.this, toast, 1000).show();
				toast = null;
			}
			super.onPostExecute(success);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}

	public class HTTPGetChannelTask extends AsyncTask<Void, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			try {
				// Simulate network access.
				Thread.sleep(200);
			} catch (InterruptedException e) {
				return false;
			}
			HttpClient httpClient = new DefaultHttpClient();
			// 创建一个HttpGet对象			
			HttpGet get = new HttpGet(APPBaseInfo.URL + APPConstant.GETCHANNELURL);
			CYLog.d(TAG, APPBaseInfo.URL);
			try {
				// 发送GET请求
				HttpResponse httpResponse = httpClient.execute(get);
				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					// 读取服务器响应
					BufferedReader br = new BufferedReader(
							new InputStreamReader(entity.getContent()));
					String line = null;
					StringBuilder builder = new StringBuilder();
					while ((line = br.readLine()) != null) {
						builder.append(line);

					}
					httpJsonchannel = builder.toString();
					return true;

				} else {
					toast = "网络连接错误2，请稍后重试";
				}
			} catch (Exception e) {
				e.printStackTrace();
				toast = "网络连接错误3，请稍后重试" + e;
			}

			return false;

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			if (success) {
				new PushCallback(PushedMsgService.this).messageManage(APPConstant.GENERAL_CHANNEL_NAME,
						"{'list':" + httpJsonchannel + "}");

			}
			// initDB();
			if (toast != null) {
				Toast.makeText(PushedMsgService.this, toast, Toast.LENGTH_SHORT).show();
				toast = null;
			}
			super.onPostExecute(success);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
