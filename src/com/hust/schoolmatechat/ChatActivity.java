package com.hust.schoolmatechat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.schoolmatechat.view.MsgListView.OnRefreshListener;
import com.hust.schoolmatechat.DataCenterManagerService.ChatSendFileTask;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.FaceInput.FaceConversionUtil;
import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.engine.ChatMessage;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.pushedmsgservice.SingleNewsMessage;
import com.hust.schoolmatechat.utils.DateFormatUtils;
import com.hust.schoolmatechat.utils.ImageTools;
import com.hust.schoolmatechat.utils.ImageUtils;
import com.hust.schoolmatechat.utils.StreamUtils;
import com.hust.schoolmatechat.view.MsgListView;

public class ChatActivity extends Activity {
	private static final String TAG = "ChatActivity";
	private MsgListView lvChatmessages = null;
	private TextView tvNickname = null;
	private Context mContext;
	// 下拉刷新
	private static final int Below_minSize = 0;
	private static final int Upon_minSize = 1;
	private static final int ABLE_TO_REFRESH = 2;
	private static final int NO_REFRESH_MORE = 3;
	private static final int ALLMESSAGES = 0;
	int Flag_RefreshUI = 0;// 是否处于刷新状态 Flag_Adapt,
	final int MinSize = 10;// 初始chatAcitivity显示的消息条数
	int IncreaseSize = 10;// 消息显示初始大小
	int Size = MinSize;// 当前listView的消息条数
	// List<ChatMessage> unreadedmessages = new ArrayList<ChatMessage>();
	List<ChatMessage> abletorefresh_messages = new ArrayList<ChatMessage>();
	List<ChatMessage> messagesListTemp = new ArrayList<ChatMessage>();

	// List<ChatMessage> nomorerefresh_messages = new ArrayList<ChatMessage>();

	public int REQUEST_CODE;
	private static final int CHOOSE_PICTURE = 1;
	private static final int TAKE_PICTURE = 2;
	private static final int SEND_FILE = 3;
	private static final int SCALE = 2;// 照片缩小比例
	private static final String TEMP_PIC_FILE = "post_picture.jpg"; // 临时文件的名字

	private static final String IMAGE_UNSPECIFIED = "image/*";
	private File tempPicture;
	private ChatSendFileTask chatSendFileTask;
	private String fileUrl, account, pass, owner;
	private int messageType;
	private int sendtype = 1;
	private boolean isSendingFile = false;// 是否正在发送文件
	private int sendFileProgress = 0;// 发送文件进度条
	private ChatMessage sendingFileMessage;// 正在发送文件时用来显示进度的聊天条目
	private ProgressBar sendingProgressBar;

	private LinearLayout function_choose = null;
	private MyAdapter adapter = null;
	private LayoutInflater inflater = null;
	private LinearLayout mLayoutBottom = null;
	private EditText etMessageBody = null;
	private Button btSend = null;
	private ImageButton btSendNo = null;
	private ChatMessageBroadCastReceiver chatReceiver = null;
	private PushedMsgBroadCastReceiver newsReceiver = null;
	private ActionBar bar = null;
	private ChatItem clickedChatItem;
	private boolean ischat = false;
	private boolean isFront = true;
	private boolean markGroupChatActiviy = false;// 标记是否为群聊要打开的页面
	// List<ChatMessage> messages = new ArrayList<ChatMessage>();
	private int lastMessageTime = 0;
	
	public static Activity instance = null;   //存储当前activity的实例
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

			initChatActivity();
		}
	};

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case 0:
				String obj = (String) msg.obj;
				if (obj == null || obj.equals("") || obj.equals("updateUI")) {
					// 收到不明信息，只做 UI更新操作，
					CYLog.d(TAG, "update ui");

					// List<ChatMessage> temp_messages = abletorefresh_messages;
					abletorefresh_messages = dataCenterManagerService
							.getChatData(owner, messageType, Size);
					// abletorefresh_messages.addAll(temp_messages);
					// nomorerefresh_messages = dataCenterManagerService
					// .getChatData(owner,
					// ALLMESSAGES);
					// messageListMap = dataCenterManagerService
					// .getUnReadMessageListMap();
					// messages =
					// messageListMap.get(owner);
					adapter.notifyDataSetChanged();
					lvChatmessages.setSelection(lvChatmessages.getBottom());
					return;
				}
				if (owner.equals(obj)) {
					CYLog.d(TAG, "Size=" + Size + ",owner.equals(obj)" + obj);
					// messageListMap = dataCenterManagerService
					// .getUnReadMessageListMap();
					// List<ChatMessage> temp_messages = abletorefresh_messages;
					abletorefresh_messages = dataCenterManagerService
							.getChatData(owner, messageType, Size);
					// abletorefresh_messages.addAll(temp_messages); //
					// getChatData有问题，这里只能取到最新的一条，先暂时这么做
					// CYLog.d(TAG, "abletorefresh_messages.Size="
					// + abletorefresh_messages.size());

					// nomorerefresh_messages = dataCenterManagerService
					// .getChatData(owner,
					// ALLMESSAGES);
					// messages =
					// messageListMap.get(owner);
					adapter.notifyDataSetChanged();
					lvChatmessages.setSelection(lvChatmessages.getBottom());
					// if (isFront) {
					// NotificationManager notiManager = (NotificationManager)
					// getSystemService(NOTIFICATION_SERVICE);
					// CYLog.d(TAG,
					// "notiManager.cancel 333 ="
					// + StreamUtils
					// .stringToAsciiInt(getActionBar()
					// .getTitle().toString()));
					// notiManager.cancel(StreamUtils
					// .stringToAsciiInt(getActionBar().getTitle()
					// .toString()));// 通知栏清零
					//
					// dataCenterManagerService.setNumflag(0);
					// }
					clickedChatItem.setUnread(0);
					// dataCenterManagerService//此处clickedChatItem的数据还是旧的，不是自己发送的最新的消息，而且数据中心已经更新了数据库，这里不需要再更新
					// .updateChatItemToDb(clickedChatItem);// 更新点击的消息

					// ++++lqg++++ 既然可以传递obj，就应该直接传递item过来，提供效率，待修改
					List<ChatItem> chatItems = dataCenterManagerService
							.getChatItems();
					if (chatItems != null) {
						for (ChatItem item : chatItems) {
							if (item.getOwner().equals(obj)) {
								item.setUnread(0);
							}
						}
					}

				} else {
					CYLog.d(TAG, "handler else" + obj);
					/*
					 * List<ChatItem> chatItems = app.getChatItems(); int unread
					 * = 0; for (ChatItem item : chatItems) { unread +=
					 * item.getUnread(); } //btReturn.setText("返回(" + unread +
					 * ")"); String barTitle=bar.getTitle().toString();
					 * bar.setTitle
					 * ("("+unread+")"+barTitle.substring(barTitle.indexOf
					 * (")")+1,barTitle.length()));
					 */
				}
				break;

			case 1:
				String channelId = (String) msg.obj;
				if (owner.equals(channelId)) {

					// messageListMap = dataCenterManagerService
					// .getUnReadMessageListMap();
					// messages =
					// messageListMap.get(owner);

					// List<ChatMessage> temp_messages = abletorefresh_messages;
					abletorefresh_messages = dataCenterManagerService
							.getChatData(owner, messageType, Size);
					// abletorefresh_messages.addAll(temp_messages); //
					// getChatData有问题，这里只能取到最新的一条，先暂时这么做
					// CYLog.d(TAG, "abletorefresh_messages.Size="
					// + abletorefresh_messages.size());
					// nomorerefresh_messages = dataCenterManagerService
					// .getChatData(owner,
					// ALLMESSAGES);
					// if (isFront) {
					// NotificationManager notiManager = (NotificationManager)
					// getSystemService(NOTIFICATION_SERVICE);
					// CYLog.d(TAG,
					// "notiManager.cancel 333 ="
					// + StreamUtils
					// .stringToAsciiInt(getActionBar()
					// .getTitle().toString()));
					// notiManager.cancel(StreamUtils
					// .stringToAsciiInt(getActionBar().getTitle()
					// .toString()));// 通知栏清零
					//
					// dataCenterManagerService.setNumflag(0);
					// }
					adapter.notifyDataSetChanged();
					lvChatmessages.setSelection(lvChatmessages.getBottom());
					List<ChatItem> chatItems2 = dataCenterManagerService
							.getChatItems();
					for (ChatItem item : chatItems2) {
						if (item.getOwner().equals(owner)) {
							item.setUnread(0);
						}
					}
				} else {
					/*
					 * List<ChatItem> chatItems = app.getChatItems(); int unread
					 * = 0; for (ChatItem item : chatItems) { unread +=
					 * item.getUnread(); } //btReturn.setText("返回(" + unread +
					 * ")");
					 * 
					 * String barTitle=bar.getTitle().toString();
					 * bar.setTitle("("
					 * +unread+")"+barTitle.substring(barTitle.indexOf
					 * (")")+1,barTitle.length()));
					 */
				}
				break;
			case APPConstant.UPLOAD_STARTED:
				isSendingFile = true;
				if (function_choose != null)
					function_choose.setVisibility(View.GONE);// 暂时只允许同时发送一张图片
				if (btSendNo != null)
					btSendNo.setClickable(false);
				if (abletorefresh_messages == null) {// 加一个临时的聊天项，用来显示文件发送的进度条
					abletorefresh_messages = new ArrayList<ChatMessage>();
				}
				// CYLog.d(TAG,"UPLOAD_STARTED--abletorefresh_messages.size()="+abletorefresh_messages.size());
				List<ChatMessage> temp_messages = abletorefresh_messages;
				abletorefresh_messages.clear();
				abletorefresh_messages.add(sendingFileMessage);
				// messages = messageListMap.get(owner);
				if (dataCenterManagerService != null) {
					abletorefresh_messages.addAll(dataCenterManagerService
							.getChatData(owner, messageType, Size));
				} else {
					abletorefresh_messages.addAll(temp_messages);
				}
				// CYLog.d(TAG,"UPLOAD_STARTED++===sendingFileMessage="+sendingFileMessage.getMessageContent()+"--abletorefresh_messages.size()="+abletorefresh_messages.size());

				if (adapter == null) {
					adapter = new MyAdapter();
				}
				adapter.notifyDataSetChanged();
				if (lvChatmessages != null)
					lvChatmessages.setSelection(lvChatmessages.getBottom());
				break;

			case APPConstant.UPDATE_UPLOAD_PROGRESS:
				int percentage = msg.arg1;
				sendFileProgress = percentage;
				if (adapter == null) {
					adapter = new MyAdapter();
				}
				// CYLog.d(TAG,"UPDATE_UPLOAD_PROGRESS++--sendFileProgress="+sendFileProgress);

				adapter.notifyDataSetChanged();
				break;

			case APPConstant.UPLOAD_FINISHED:
				String url = (String) msg.obj;
				CYLog.d(TAG, "发送返回的url" + url);
				// 发送完了吧临时的进度条item移除，再走正常 的消息发送环回
				for (int i = 0; abletorefresh_messages != null
						&& i < abletorefresh_messages.size(); i++) {
					if (abletorefresh_messages.get(i).getMessageContent()
							.startsWith(APPConstant.FILE_SEND_TMP)) {
						abletorefresh_messages.remove(i);
					}
				}

				sendFileProgress = 101;
				if (adapter == null) {
					adapter = new MyAdapter();
				}
				adapter.notifyDataSetChanged();
				if (lvChatmessages != null)
					lvChatmessages.setSelection(lvChatmessages.getBottom());
				if (url != null && url.startsWith("http://")) {
					String[] path = url.split("all_user_files");
					int start = url.lastIndexOf("/");
					String filename = url.substring(start + 1, url.length());
					String pathname = APPConstant.CHAT_FILE
							+ File.separator
							+ path[1].substring(0,
									path[1].length() - filename.length() - 1);
					File file = new File(pathname);
					// CYLog.i(TAG,filename+"  file.mkdirs();  "
					// +pathname);
					if (!file.exists()) {
						file.mkdirs();
					}
					try {
						boolean flag = new File(fileUrl)
								.renameTo(new File(pathname + File.separator
										+ URLDecoder.decode(filename, "UTF-8")));
						url = url.substring(0, start + 1)
								+ URLDecoder.decode(filename, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// 将临时文件移动到指定文件夹，自己发送的文件不需要下载
					// FileInputStream fi = null;
					// FileOutputStream fo = null;
					// FileChannel in = null;
					// FileChannel out = null;
					// try {
					// fi = new FileInputStream(new File(fileUrl));
					// fo = new FileOutputStream(new File(pathname+
					// File.separator + filename));
					// in = fi.getChannel();// 得到对应的文件通道
					// out = fo.getChannel();// 得到对应的文件通道
					// in.transferTo(0, in.size(), out);//
					// 连接两个通道，并且从in通道读取，然后写入out通道
					// } catch (IOException e) {
					// e.printStackTrace();
					// } finally {
					// try {
					// fi.close();
					// in.close();
					// fo.close();
					// out.close();
					// } catch (IOException e) {
					// e.printStackTrace();
					// }
					// }

					sendChatMessage(APPConstant.CMD_PREFIX_FILE_SEND + url);// 转换成特殊消息发给对方
				} else {
					CYLog.d(TAG, "upload failed");
					sendChatMessage(APPConstant.CMD_PREFIX_FILE_SEND + sendtype);// 文件上传失败的话，没有url，加上文件类型，以便以后区别显示发送失败的图标
				}
				isSendingFile = false;
				if (btSendNo != null)
					btSendNo.setClickable(true);
				if (adapter != null)
					adapter.notifyDataSetChanged();
				break;
			default:
				break;
			}

			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*
		 * 连接数据中心管理服务
		 */
		final Intent dataCenterManagerIntent = new Intent(this,
				DataCenterManagerService.class);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
				Context.BIND_ABOVE_CLIENT);
		instance = this;
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		if (ischat) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.menus, menu);
		}

		if (markGroupChatActiviy) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.group_member_menus, menu);
		}
		return true;
	}

	private void initChatActivity() {
		mContext = this.getApplicationContext();

		bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		clickedChatItem = (ChatItem) getIntent().getSerializableExtra(
				ChatItem.SER_KEY);

		setContentView(R.layout.universalchat_main);

		CYLog.i(TAG, "chatactivity onCreate");

		IntentFilter intentFilter = new IntentFilter(
				"com.schoolmatechat.message");
		chatReceiver = new ChatMessageBroadCastReceiver();
		registerReceiver(chatReceiver, intentFilter);

		IntentFilter newsFilter = new IntentFilter(
				"com.schoolmatechat.newsadded2message");
		newsReceiver = new PushedMsgBroadCastReceiver();
		registerReceiver(newsReceiver, newsFilter);
		function_choose = (LinearLayout) this
				.findViewById(R.id.function_choose);
		mLayoutBottom = (LinearLayout) this
				.findViewById(R.id.layout_chat_bottom);
		if (clickedChatItem == null) {
			List<ChatItem> chatItems = dataCenterManagerService.getChatItems();
			if (chatItems != null) {

				clickedChatItem = chatItems.get(0);
				clickedChatItem.setUnread(0);// 未读消息数为0
				dataCenterManagerService.updateChatItemToDb(clickedChatItem);// 更新点击的消息
			} else {
				CYLog.d(TAG, "++++++clickedChatItem=" + clickedChatItem
						+ ",chatItems=" + chatItems);

				Intent intent = new Intent(ChatActivity.this,
						LogoActivity.class);
				startActivity(intent);
				finish();
				return;
			}
		}
		if (clickedChatItem != null) {
			bar.setTitle(clickedChatItem.getName());
			owner = clickedChatItem.getOwner();
			messageType = clickedChatItem.getType();
		}
		if (clickedChatItem != null && messageType == ChatItem.PRIVATECHATITEM) {// 主界面单聊消息条目
			mLayoutBottom.setVisibility(View.VISIBLE);
			ischat = true;
			// if (owner != null) {
			// if (clickedChatItem.getName() != null) {
			// bar.setTitle(clickedChatItem.getName());
			// }
			// }
		} else if (clickedChatItem != null && messageType == ChatItem.NEWSITEM) {// 主界面新闻消息条目
			mLayoutBottom.setVisibility(View.GONE);
			// bar.setTitle(userName);
		} else if (clickedChatItem != null
				&& messageType == ChatItem.SCHOOLHELPERITEM) {// 主界面校友帮帮忙消息条目
			mLayoutBottom.setVisibility(View.GONE);
			// bar.setTitle(clickedChatItem.getName());
		} else if (clickedChatItem != null
				&& messageType == ChatItem.GROUPCHATITEM) { // 群聊
			mLayoutBottom.setVisibility(View.VISIBLE);
			markGroupChatActiviy = true;
		} else {
			CYLog.d(TAG, "++++++clickedChatItem=" + clickedChatItem
					+ ",messageType=" + messageType);

			Intent intent = new Intent(ChatActivity.this, LogoActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		lvChatmessages = (MsgListView) this
				.findViewById(R.id.listView_chat_messages);

		if (dataCenterManagerService != null) {
			abletorefresh_messages = dataCenterManagerService.getChatData(
					owner, messageType, Size);
			if (abletorefresh_messages != null) {
				messagesListTemp.clear();
				messagesListTemp.addAll(abletorefresh_messages);
			}
			dataCenterManagerService.checkDataFromDB(
					clickedChatItem.getOwner(), dataCenterManagerService
							.getUserSelfContactsEntity().getUserAccount(),
					clickedChatItem.getType());
		}
		// nomorerefresh_messages = dataCenterManagerService.getChatData(
		// owner, ALLMESSAGES);
		// // messages = dataCenterManagerService.getChatData(clickedChatItem
		// .getOwner(), Size);;

		adapter = new MyAdapter();
		if (clickedChatItem == null || owner == null || lvChatmessages == null
				|| adapter == null) {
			CYLog.d(TAG, "======clickedChatItem=" + clickedChatItem + ",owner="
					+ owner + ",lvChatmessages=" + lvChatmessages + ",adapter="
					+ adapter);
			Intent intent = new Intent(ChatActivity.this, LogoActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		lvChatmessages.setAdapter(adapter);
		dataCenterManagerService.setNumflag(0);
		NotificationManager notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// CYLog.d(TAG,
		// " notiManager.cancel 111 ="
		// + StreamUtils.stringToAsciiInt(getActionBar()
		// .getTitle().toString()));
		notiManager.cancel(0);
		lvChatmessages.setonRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				new AsyncTask<Void, Void, Void>() {
					protected Void doInBackground(Void... params) {
						try {
							Thread.sleep(500);
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {

							if (messageType == ChatItem.SCHOOLHELPERITEM) {
								return null;
							}
							// 校友帮帮忙不需要刷新
							// if (messageType == ChatItem.GROUPCHATITEM) {
							// return;
							// }
							Flag_RefreshUI = 1;

							Size += IncreaseSize;
							List<ChatMessage> temp_messages = abletorefresh_messages;
							abletorefresh_messages.clear();
							if (dataCenterManagerService != null) { // 防止异步操作时数据中心可能为空
								abletorefresh_messages = dataCenterManagerService
										.getChatData(owner, messageType, Size);
							} else {
								abletorefresh_messages = temp_messages;
							}// 已经进入聊天室开始刷新
								// Size = abletorefresh_messages.size();
								// CYLog.d(TAG, "ref----  Size=" + Size
							// + ",abletorefresh_messages.size()="
							// + abletorefresh_messages.size());

						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						adapter.notifyDataSetChanged();
						lvChatmessages.onRefreshComplete();
						if (abletorefresh_messages != null
								&& abletorefresh_messages.size() > 0) {
							messagesListTemp.clear();
							messagesListTemp.addAll(abletorefresh_messages);// 缓存一份
						}
					}
				}.execute();
			}
		}, 0);

		account = dataCenterManagerService.getTigaseAccount();
		pass = dataCenterManagerService.getTigasePassword();
		dataCenterManagerService.setIsChatingWithWho(getActionBar().getTitle()
				.toString());
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		etMessageBody = (EditText) this
				.findViewById(R.id.editText_chat_chatcontent);
		btSend = (Button) this.findViewById(R.id.button_chat_send);
		btSendNo = (ImageButton) this.findViewById(R.id.button_chat_sendno);
		// tvNickname = (TextView)
		// this.findViewById(R.id.textView_chat_nickname);

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (lvChatmessages == null) {
					CYLog.e(TAG, "lvChatmessages is null");
					Intent intent = new Intent(ChatActivity.this,
							LogoActivity.class);
					startActivity(intent);
					finish();
					return;
				} else {
					if (abletorefresh_messages == null
							&& dataCenterManagerService != null)
						abletorefresh_messages = dataCenterManagerService
								.getChatData(owner, messageType, Size);
					messagesListTemp.clear();
					messagesListTemp.addAll(abletorefresh_messages);
					lvChatmessages.setSelection(lvChatmessages.getBottom());
				}
			}
		}, 200);
		etMessageBody.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() > 0) {
					btSendNo.setVisibility(View.GONE);
					btSend.setVisibility(View.VISIBLE);

				} else {
					btSendNo.setVisibility(View.VISIBLE);
					btSend.setVisibility(View.GONE);
				}
			}

		});
		adapter.notifyDataSetChanged();
	}

	public void newsOnClick(View view) {
		try {
			// 校友帮帮忙访问限制
			if (messageType == ChatItem.SCHOOLHELPERITEM) {
				String auth = dataCenterManagerService
						.getUserSelfContactsEntity().getAuthenticated();
				if (auth == null || !auth.equals("1")) {
					Toast.makeText(ChatActivity.this, "对不起，请完善您的班级信息认证才可以访问!",
							Toast.LENGTH_SHORT).show();
					return;
				}
			}
			String news = null;

			// 将消息Url传送到NewsExploreActivity并启动它
			news = (String) view.getTag();
			String[] newsData = news.split("\\*");
			Intent intent = new Intent(getApplicationContext(),
					NewsExploreActivitiy.class);
			intent.putExtra("newsUrl", newsData[0]);
			if (newsData.length > 1) {
				intent.putExtra("tittle", newsData[1]);
				intent.putExtra("image", newsData[2]);
			}
			String userName = clickedChatItem.getName();
			// if (messageType == ChatItem.SCHOOLHELPERITEM) {//
			// 如果为校友帮帮忙的消息
			// userName = (String) ((TextView) view
			// .findViewById(R.id.textView_schoolhelper_cname)).getText();
			// }
			intent.putExtra("userName", userName);
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void chatItemOnClick(View view) {

		TextView tvSS = (TextView) view;
		SpannedString content = (SpannedString) tvSS.getText();
		// Toast.makeText(ChatActivity.this,
		// content.toString(),Toast.LENGTH_SHORT).show();
		// 使用Intent
		String text = content.toString();
		if (text.startsWith(APPConstant.CMD_PREFIX_FILE_SEND))
			text = text.split("\t")[0];
		// Toast.makeText(this, text, 100).show();
		Intent intent = new Intent(this, ChatDetailActivity.class);
		intent.putExtra("TEXT", text);
		startActivity(intent);

	}

	public void buttonsOnClick(View view) {
		switch (view.getId()) {
		/*
		 * case R.id.button_chat_return: Intent chatFragment = new
		 * Intent(getApplicationContext(), MainActivity.class);
		 * startActivity(chatFragment); break;
		 */

		case R.id.button_chat_send:
			// 获取发送的消息
			final String messageBody = etMessageBody.getText().toString();
			sendChatMessage(messageBody);
			etMessageBody.setText("");// 情况输入框
			lvChatmessages.setSelection(lvChatmessages.getBottom());
			break;

		case R.id.groupchatimageViewsendfail:
			View gparent = (View) view.getParent();
			TextView tvGM = (TextView) gparent
					.findViewById(R.id.textView_groupchat_message);
			String text = tvGM.getText().toString();
			if (text.startsWith(APPConstant.CMD_PREFIX_FILE_SEND))
				text = text.split("\t")[0];
			createResendDialog(text + "");

			break;

		case R.id.imageViewsendfail:
			View parent = (View) view.getParent();
			final TextView tvSS = (TextView) parent
					.findViewById(R.id.textView_selfsent_message);

			String text1 = tvSS.getText().toString();
			if (text1.startsWith(APPConstant.CMD_PREFIX_FILE_SEND))
				text1 = text1.split("\t")[0];
			// Toast.makeText(this, text1, 100).show();
			createResendDialog(text1 + "");

			break;
		case R.id.button_chat_sendno:
			if (function_choose.getVisibility() == View.VISIBLE) {
				function_choose.setVisibility(View.GONE);
			} else if (function_choose.getVisibility() == View.GONE) {
				function_choose.setVisibility(View.VISIBLE);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0); // 强制隐藏键盘
			}
			break;
		case R.id.send_image:
			// Toast.makeText(ChatActivity.this, "发送图片",
			// Toast.LENGTH_SHORT).show();
			Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
			sendtype = APPConstant.PICTURE;
			sendingFileMessage = getSendingFileMessage(sendtype, messageType,
					account, owner);
			REQUEST_CODE = CHOOSE_PICTURE;
			openAlbumIntent.setDataAndType(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					IMAGE_UNSPECIFIED);
			startActivityForResult(openAlbumIntent, REQUEST_CODE);
			break;
		case R.id.take_photo:
			// Toast.makeText(ChatActivity.this, "拍照发送",
			// Toast.LENGTH_SHORT).show();
			sendtype = APPConstant.PICTURE;
			sendingFileMessage = getSendingFileMessage(sendtype, messageType,
					account, owner);
			Uri imageUri = null;
			String fileName = null;
			Intent openCameraIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);
			REQUEST_CODE = TAKE_PICTURE;
			fileName = TEMP_PIC_FILE;
			imageUri = Uri.fromFile(new File(APPConstant.CHAT_FILE, fileName));
			// 指定照片保存路径（SD卡），TEMP_PIC_FILE为一个临时文件，每次拍照后这个图片都会被替换
			openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(openCameraIntent, REQUEST_CODE);
			break;
		case R.id.send_file:
			// Toast.makeText(ChatActivity.this, "拍照发送",
			// Toast.LENGTH_SHORT).show();
			sendtype = APPConstant.NORMAL_FILE;
			sendingFileMessage = getSendingFileMessage(sendtype, messageType,
					account, owner);
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("file/");
			// intent.setType("*/*");
			REQUEST_CODE = SEND_FILE;
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			try {
				startActivityForResult(
						Intent.createChooser(intent, "请选择一个要发送的文件"),
						REQUEST_CODE);
			} catch (android.content.ActivityNotFoundException ex) {
				// Potentially direct the user to the Market with a Dialog
				Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	private void createResendDialog(final String messageBody) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setItems(new String[] { "重发消息", "取消" },
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (which) {
						case 0:
							// if (dataCenterManagerService != null)
							/*
							 * dataCenterManagerService.sendChatMessage( owner,
							 * tvSS.getText() + "");
							 */
							sendChatMessage(messageBody);

							dialog.dismiss();
							break;

						case 1:
							dialog.dismiss();
							break;

						default:
							break;
						}
					}
				});

		// Set other dialog properties

		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		dialog.getWindow().setGravity(Gravity.BOTTOM);
		dialog.show();
	}

	public void sendChatMessage(String messageBody) {
		if (messageType == ChatItem.PRIVATECHATITEM) {
			if (dataCenterManagerService == null
					|| !dataCenterManagerService.sendChatMessage(owner,
							messageBody)) {
				CYLog.e(TAG, "sendMessage failed");
			}
		} else if (messageType == ChatItem.GROUPCHATITEM) {
			if (dataCenterManagerService == null
					|| !dataCenterManagerService.sendGroupChatMessage(owner,
							messageBody, false)) {
				CYLog.e(TAG, "sendGroupChatMessage failed");
			}
		}
	}

	public void selfOnclick(View view) {
		Intent selfInfo = new Intent(this, AccountActivity.class);
		selfInfo.putExtra("selfInfo", true);
		startActivity(selfInfo);
	}

	public void iconOnclick(View view) {
		Intent fpActivity = new Intent(this, FriendProfileActivity.class);
		fpActivity.putExtra("accountNum", dataCenterManagerService
				.getUserSelfContactsEntity().getUserAccount());
		fpActivity.putExtra("password", dataCenterManagerService
				.getUserSelfContactsEntity().getPassword());

		// fpActivityownercount", owner.substring(0, owner.indexOf("@")));
		fpActivity.putExtra("friendAccount", owner);
		startActivity(fpActivity);
	}

	private class MyAdapter extends BaseAdapter {
		private boolean showTime = true;

		@Override
		public int getCount() {
			// 获取聊天消息列表
			// Map<String, List<ChatMessage>> messageListMap =
			// dataCenterManagerService
			// .getUnReadMessageListMap();
			// CYLog.d(TAG, "messageListMap.size()=" + messageListMap.size());
			// CYLog.d(TAG,
			// "owner=" + owner);
			// this.notifyDataSetChanged();
			if (abletorefresh_messages == null)
				CYLog.d(TAG, "getCount() abletorefresh_messages==null");
			else
				CYLog.i(TAG,
						"getCount() abletorefresh_messages.size() =="
								+ abletorefresh_messages.size()
								+ ",messagesListTemp.size() =="
								+ messagesListTemp.size());

			if (abletorefresh_messages == null
					|| abletorefresh_messages.size() == 0) {
				// CYLog.i(TAG,"getCount() abletorefresh_messages == null|| abletorefresh_messages.size() == 0");
				if (messagesListTemp != null)
					return messagesListTemp.size();
				else
					return 0;
			} else {
				CYLog.i(TAG, "getCount()=" + abletorefresh_messages.size());
				return abletorefresh_messages.size();
			}
			// if (Flag_RefreshUI == 0) {
			// if (unreadedmessages == null
			// || unreadedmessages.size() == 0) {
			// return 0;
			// } else
			// return unreadedmessages.size();
			// } else if (nomorerefresh_messages.size() != 0
			// && (nomorerefresh_messages.get(0).getType() ==
			// ChatMessage.PRIVATECHATMESSAGE || nomorerefresh_messages
			// .get(0).getType() == ChatMessage.GROUPCHATMESSAGE)) {
			// if (Flag_Adapt == ABLE_TO_REFRESH) {
			// return Size;
			// } else
			// return nomorerefresh_messages.size();
			// }
			// }
			// return nomorerefresh_messages.size();
		}

		@Override
		public Object getItem(int position) {
			// 获取聊天消息列表
			// Map<String, List<ChatMessage>> messageListMap =
			// dataCenterManagerService
			// .getUnReadMessageListMap();
			// List<ChatMessage> FromMessages = new ArrayList<ChatMessage>();
			//
			// if (Flag_RefreshUI == 0) {
			// FromMessages = unreadedmessages; // 第一次进入聊天室
			// } else if (Flag_Adapt == ABLE_TO_REFRESH) {
			//
			// FromMessages = abletorefresh_messages;
			// } else
			// FromMessages = nomorerefresh_messages;
			// if (abletorefresh_messages == null)
			// CYLog.i(TAG, "abletorefresh_messages==null");
			// else
			// CYLog.i(TAG, "getItem() position=" + position
			// + ",abletorefresh_messages.size()"
			// + abletorefresh_messages.size());
			if (abletorefresh_messages != null
					&& abletorefresh_messages.size() > position) {
				// CYLog.i(TAG,
				// "getItem() abletorefresh_messages.get(position)="
				// + abletorefresh_messages.get(position)
				// .getMessageContent());
				return abletorefresh_messages.get(position);
			} else {
				if (messagesListTemp != null
						&& messagesListTemp.size() > position) {
					// CYLog.i(TAG, "getItem() messagesListTemp.get(position)="
					// + messagesListTemp.get(position)
					// .getMessageContent());
					return abletorefresh_messages.get(position);
				} else {
					CYLog.i(TAG, "getItem return null");
					return null;
				}
			}

		}

		@Override
		public long getItemId(int position) {
			// CYLog.i(TAG, "getItemId() position=" + position);

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// 获取聊天消息列表
			// Map<String, List<ChatMessage>> messageListMap =
			// dataCenterManagerService
			// .getUnReadMessageListMap();
			// CYLog.d(TAG, "messageListMap.size()=" + messageListMap.size());
			View view = null;
			View view2 = inflater.inflate(
					R.layout.universalchat_privatechatmessage, null);

			SpannableString spannableString = null;
			List<ChatMessage> FromMessages = new ArrayList<ChatMessage>();

			// if (Flag_RefreshUI == 0) {
			// FromMessages = unreadedmessages; // 第一次进入聊天室
			// CYLog.d(TAG,"第一次进入聊天室"+unreadedmessages.size());
			// } else {
			// if (Flag_Adapt == ABLE_TO_REFRESH) {
			FromMessages.clear();
			FromMessages.addAll(abletorefresh_messages);// 可以刷新
			// CYLog.d(TAG, " 可以刷新" + abletorefresh_messages.size());
			if (abletorefresh_messages.size() == 0) {
				FromMessages.clear();
				FromMessages.addAll(messagesListTemp);
			}
			// } else if (Flag_Adapt == NO_REFRESH_MORE) {
			// FromMessages = nomorerefresh_messages;// 消息已经全部显示无法刷新
			// List<ChatMessage> list = null;
			// list = FromMessages;
			// CYLog.d(TAG," 消息已经全部显示无法刷新"+nomorerefresh_messages.size());
			//
			// }
			// }
			List<ChatMessage> ToMessages = new ArrayList<ChatMessage>();
			int j = FromMessages.size();
			for (int i = j - 1; i >= 0; i--) {
				ToMessages.add(FromMessages.get(i));
				// ChatMessage a = FromMessages.get(FromMessages.size() -
				// getCount() +
				// i);//新闻的话反了
				// ToMessages.add(a);
			}
			if (ToMessages == null) {
				CYLog.d(TAG, "ToMessages==null");
				return null;
			}
			if (position < ToMessages.size()) {
				// CYLog.d(TAG, "messages.size()=" + messages.size());
				ChatMessage currentMessage = ToMessages.get(position);
				// CYLog.d(TAG, "position=" + position + ",getMessageContent()"
				// + currentMessage.getMessageContent());// +
				// // ",lastMessageTime="
				// + lastMessageTime + ",currentMessage.getTime()="
				// + DateFormatUtils.date2Int(currentMessage.getTime()));
				if (position == 0) {
					lastMessageTime = DateFormatUtils.date2Int(currentMessage
							.getTime());
					showTime = true;
				} else {
					if (DateFormatUtils.date2Int(currentMessage.getTime())
							- lastMessageTime > 10) {
						showTime = true;
					} else {
						showTime = false;
					}
					lastMessageTime = DateFormatUtils.date2Int(currentMessage
							.getTime());
				}
				CYLog.d(TAG, "currentMessage.getMessageContent()="
						+ currentMessage.getMessageContent() + sendFileProgress);
				switch (currentMessage.getType()) {
				case ChatMessage.PRIVATECHATMESSAGE:
					spannableString = FaceConversionUtil.getInstace()
							.getExpressionString(mContext,
									currentMessage.getMessageContent());
					if (!currentMessage.getSenderAccount().equals(
							dataCenterManagerService
									.getUserSelfContactsEntity()
									.getUserAccount())) {// 不是自己发的消息
						view = inflater
								.inflate(
										R.layout.universalchat_privatechatmessage,
										null);
						TextView tvPcMessage = (TextView) view
								.findViewById(R.id.textView_privatechat_message);
						try {
							tvPcMessage.setText(spannableString);
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}

						TextView tvSsMessageDate = (TextView) view
								.findViewById(R.id.tv_sendtime);
						tvSsMessageDate.setText(DateFormatUtils
								.date2MessageTime(currentMessage.getTime()));
						if (!showTime) {
							tvSsMessageDate.setVisibility(View.GONE);
							showTime = true;
						}
						ImageView ivPC = (ImageView) view
								.findViewById(R.id.iv_privatechaticon);
						ContactsEntity friendContactsEntity = dataCenterManagerService
								.getFriendInfoByAccount(currentMessage
										.getSenderAccount());
						if (friendContactsEntity != null) {
							String icon = friendContactsEntity.getPicture();
							if (icon != null) {
								ImageUtils.setUserHeadIcon(ivPC, icon, handler);
							} else {
								if (currentMessage.getIcon() != null) {
									// 从服务器拿图片
									ImageUtils.setUserHeadIcon(ivPC,
											currentMessage.getIcon(), handler);
								}
							}
						} else {
							if (currentMessage.getIcon() != null) {
								// 从服务器拿图片
								ImageUtils.setUserHeadIcon(ivPC,
										currentMessage.getIcon(), handler);
							}
						}
					} else {
						view = inflater.inflate(
								R.layout.universalchat_selfsentmessage, null);
						TextView tvSsMessage = (TextView) view
								.findViewById(R.id.textView_selfsent_message);
						try {
							tvSsMessage.setText(spannableString);
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}

						// 时间
						TextView tvSsMessageDate = (TextView) view
								.findViewById(R.id.tv_sendtime);
						tvSsMessageDate.setText(""
								+ (DateFormatUtils
										.date2MessageTime(currentMessage
												.getTime())));
						if (!showTime) {
							tvSsMessageDate.setVisibility(View.GONE);
							showTime = true;
						}
						ImageView ivSengFail = (ImageView) view
								.findViewById(R.id.imageViewsendfail);
						// CYLog.d(TAG, "send state:" +
						// currentMessage.isSendSucc());
						if (!currentMessage.isSendSucc())
							ivSengFail.setVisibility(View.VISIBLE);
						ImageView ivSC = (ImageView) view
								.findViewById(R.id.iv_selfsenticon);
						String s = currentMessage.getIcon();
						if (currentMessage.getIcon() != null) {
							ImageUtils.setUserHeadIcon(ivSC,
									dataCenterManagerService
											.getUserSelfContactsEntity()
											.getPicture(), handler);
						}
					}
					// 发文件时显示进度条
					// CYLog.d(TAG, "isSendingFile=" + isSendingFile + "====="
					// + currentMessage.getMessageContent());
					if (isSendingFile
							&& currentMessage.getMessageContent().startsWith(
									APPConstant.FILE_SEND_TMP)) {
						CYLog.d(TAG, "isSendingFile。。。。。");
						ImageView iv = (ImageView) view
								.findViewById(R.id.imageView1);
						ProgressBar sendingProgressBar = (ProgressBar) view
								.findViewById(R.id.progressBar1);
						if (sendFileProgress < 101) {
							CYLog.d(TAG, "sendFileProgress。。。。。="
									+ sendFileProgress);
							if (currentMessage.getMessageContent().equals(
									APPConstant.FILE_SEND_TMP
											+ APPConstant.PICTURE)) {// 发送的是图片，先显示缓存图片
								Bitmap photo = ImageUtils.getThumbnail(
										getBaseContext(), Uri
												.fromFile(new File(
														APPConstant.CHAT_FILE,
														TEMP_PIC_FILE)), 500);
								iv.setImageBitmap(photo);
							} else if (currentMessage.getMessageContent()
									.equals(APPConstant.FILE_SEND_TMP
											+ APPConstant.NORMAL_FILE)) {// 发送的是普通文件，先显示缓存图片
								Bitmap photo = BitmapFactory.decodeResource(
										getBaseContext().getResources(),
										R.drawable.sendfileing);
								iv.setImageBitmap(photo);
							}
							sendingProgressBar.setProgress(sendFileProgress);
							iv.setVisibility(View.VISIBLE);
							sendingProgressBar.setVisibility(View.VISIBLE);
							TextView tvSsMessage = (TextView) view
									.findViewById(R.id.textView_selfsent_message);
							tvSsMessage.setVisibility(View.GONE);
						} else {
							iv.setVisibility(View.GONE);
							sendingProgressBar.setVisibility(View.GONE);
						}
					}
					break;

				case ChatMessage.GROUPCHATMESSAGE:
					String senderAccount = currentMessage.getSenderAccount();
					if (senderAccount.endsWith("@"
							+ APPBaseInfo.TIGASE_SERVER_DOMAIN)) {
						senderAccount = senderAccount.substring(
								0,
								senderAccount.lastIndexOf("@"
										+ APPBaseInfo.TIGASE_SERVER_DOMAIN));
					}

					spannableString = FaceConversionUtil.getInstace()
							.getExpressionString(mContext,
									currentMessage.getMessageContent());
					if (!senderAccount.equals(dataCenterManagerService
							.getUserSelfContactsEntity().getUserAccount())) {// 不是自己发送的
						view = inflater.inflate(
								R.layout.universalchat_groupchatmessage, null);

						TextView tvGpMessage = (TextView) view
								.findViewById(R.id.textView_groupchat_message);
						tvGpMessage.setText(spannableString);
						tvGpMessage
								.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										chatItemOnClick(v); // 按钮按下的处理部分
									}
								});

						TextView tvSsMessageDate = (TextView) view
								.findViewById(R.id.tv_sendtime);
						tvSsMessageDate.setText(DateFormatUtils
								.date2MessageTime(currentMessage.getTime()));
						if (!showTime) {
							tvSsMessageDate.setVisibility(View.GONE);
							showTime = true;
						}
						// 获取联系人信息
						ContactsEntity contactsEntity = dataCenterManagerService
								.getFriendInfoByAccount(senderAccount);

						// 设置姓名和图像
						TextView tvGpNickname = (TextView) view
								.findViewById(R.id.textView_groupchat_nickname);
						ImageView ivSC = (ImageView) view
								.findViewById(R.id.iv_groupchaticon);
						if (contactsEntity != null) {
							tvGpNickname.setText(contactsEntity.getName());
							if (contactsEntity.getPicture() != null) {
								ImageUtils.setUserHeadIcon(ivSC,
										contactsEntity.getPicture(), handler);
							}
						} else {
							tvGpNickname.setText(senderAccount);
						}

						// 发送失败的消息判断
						ImageView ivSengFail = (ImageView) view
								.findViewById(R.id.groupchatimageViewsendfail);
						if (!currentMessage.isSendSucc())
							ivSengFail.setVisibility(View.VISIBLE);
					} else {
						view = inflater.inflate(
								R.layout.universalchat_groupchat_selfmessage,
								null);

						TextView tvGpMessage = (TextView) view
								.findViewById(R.id.textView_groupchat_message);
						tvGpMessage.setText(spannableString);
						tvGpMessage
								.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										chatItemOnClick(v); // 按钮按下的处理部分
									}
								});
						TextView tvSsMessageDate = (TextView) view
								.findViewById(R.id.tv_sendtime);
						tvSsMessageDate.setText(DateFormatUtils
								.date2MessageTime(currentMessage.getTime()));
						if (!showTime) {
							tvSsMessageDate.setVisibility(View.GONE);
							showTime = true;
						}
						// 获取联系人信息
						ContactsEntity contactsEntity = dataCenterManagerService
								.getUserSelfContactsEntity();

						// 设置姓名和图像
						TextView tvGpNickname = (TextView) view
								.findViewById(R.id.textView_groupchat_nickname);
						ImageView ivSC = (ImageView) view
								.findViewById(R.id.iv_groupchaticon);
						if (contactsEntity != null) {
							tvGpNickname.setText(contactsEntity.getName());
							if (contactsEntity.getPicture() != null) {
								ImageUtils.setUserHeadIcon(ivSC,
										contactsEntity.getPicture(), handler);
							}
						} else {
							tvGpNickname.setText(senderAccount);
						}

						// 发送失败的消息判断
						ImageView ivSengFail = (ImageView) view
								.findViewById(R.id.groupchatimageViewsendfail);
						if (!currentMessage.isSendSucc())
							ivSengFail.setVisibility(View.VISIBLE);
					}
					// 显示进度条
					if (isSendingFile
							&& currentMessage.getMessageContent().startsWith(
									APPConstant.FILE_SEND_TMP)) {

						ImageView iv = (ImageView) view
								.findViewById(R.id.groupchatimageView1);
						ProgressBar sendingProgressBar = (ProgressBar) view
								.findViewById(R.id.groupchatprogressBar1);
						if (sendFileProgress < 101) {
//							CYLog.d(TAG, "sendFileProgress。。。。。="
//									+ sendFileProgress);

							if (currentMessage.getMessageContent().equals(
									APPConstant.FILE_SEND_TMP
											+ APPConstant.PICTURE)) {// 发送的是图片，显示缓存图片
								Bitmap photo = ImageUtils.getThumbnail(
										getBaseContext(), Uri
												.fromFile(new File(
														APPConstant.CHAT_FILE,
														TEMP_PIC_FILE)), 500);
								iv.setImageBitmap(photo);
							} else if (currentMessage.getMessageContent()
									.equals(APPConstant.FILE_SEND_TMP
											+ APPConstant.NORMAL_FILE)) {// 发送的是普通文件，先显示缓存图片
								Bitmap photo = BitmapFactory.decodeResource(
										getBaseContext().getResources(),
										R.drawable.sendfileing);
								iv.setImageBitmap(photo);
							}
							sendingProgressBar.setProgress(sendFileProgress);
							iv.setVisibility(View.VISIBLE);
							sendingProgressBar.setVisibility(View.VISIBLE);
							TextView tvGpMessage = (TextView) view
									.findViewById(R.id.textView_groupchat_message);
							tvGpMessage.setVisibility(View.GONE);
						} else {
							iv.setVisibility(View.GONE);
							sendingProgressBar.setVisibility(View.GONE);
						}
					}
					break;

				case ChatMessage.NEWSMESSAGE:
					view = inflater.inflate(
							R.layout.universalchat_newsmessagelist, null);
					TextView tvNewsTime = (TextView) view
							.findViewById(R.id.textView_news_time);
					tvNewsTime.setText(DateFormatUtils
							.date2MessageTime(currentMessage.getTime()));

					LinearLayout llList = (LinearLayout) view
							.findViewById(R.id.linearlayout_newslist);
					int i = 0;
					if (currentMessage.getNewsList() == null) {
						// CYLog.d(TAG, "currentMessage.getNewsList()== null");
						break;
					}
					for (SingleNewsMessage newsMessage : currentMessage
							.getNewsList()) {
						i++;
						RelativeLayout rl = null;
						if (i == 1) {
							rl = (RelativeLayout) inflater
									.inflate(
											R.layout.universalchat_newsmessagelist_breaking,
											null);

						} else {
							rl = (RelativeLayout) inflater
									.inflate(
											R.layout.universalchat_newsmessagelist_normal,
											null);
						}

						TextView newsTitle = (TextView) rl
								.findViewById(R.id.textView_news_title);
						ImageView newsIcon = (ImageView) rl
								.findViewById(R.id.imageView_news_icon);
						// 设置标题
						newsTitle.setText(newsMessage.getTitle());
						CYLog.d(TAG, newsMessage.getTitle());
						// 设置tag
						if (i == 1) {
							rl.setTag(newsMessage.getNewsUrl() + "*"
									+ currentMessage.getMessageContent() + "*"
									+ newsMessage.getIcon());
						} else {
							rl.setTag(newsMessage.getNewsUrl() + "*"
									+ newsMessage.getTitle() + "*"
									+ newsMessage.getIcon());

						}

						// 设置图片
						ImageUtils.setIcon(newsIcon, newsMessage.getIcon(),
								handler);
						// CYLog.d(TAG,
						// "newsMessage.getIcon() = " + newsMessage.getIcon());

						llList.addView(rl);

					}

					break;

				case ChatMessage.SCHOOLHELPER:
					view = inflater.inflate(R.layout.schoolhelper_info, null);
					ImageView cIcon = (ImageView) view
							.findViewById(R.id.iv_schoolhelper_cicon);

					TextView tvCName = (TextView) view
							.findViewById(R.id.textView_schoolhelper_cname);

					// ++++lqg++++ test
					// tvCName.setText(currentMessage.getOwner());
					tvCName.setText(currentMessage.getUserAccount());
					setSchoolHelperIcon(currentMessage.getUserAccount(), cIcon);
					// ImageUtils.setIcon(cIcon, currentMessage.getIcon(),
					// handler);
					view.setTag(currentMessage.getMessageContent()
							+ dataCenterManagerService
									.getUserSelfContactsEntity()
									.getUserAccount());
					// CYLog.d(TAG,currentMessage.getMessageContent()+
					// dataCenterManagerService.getUserSelfContactsEntity()
					// .getId());
					// int iconId = 0;
					// //try {
					// //+++++这里直接用currentMessage.getIcon()的话容易错乱，因为如果/res下面的图片资源有变化的话，每张图片的id是会重新生成的，就跟之前数据库里存的id不一样了
					// iconId = APPConstant.SCHOOL_HELPER_ICON[0];
					// CYLog.d(TAG,"currentMessage.getIcon() = "+currentMessage.getIcon());
					// //} catch (NumberFormatException e) {
					// // TODO: handle exception
					// // e.printStackTrace();
					// // CYLog.e(TAG,"iconId e= "+e);
					// //}
					// if (iconId != 0) {
					// CYLog.d(TAG,"iconId !=0, = "+iconId);
					// cIcon.setImageResource(iconId);
					// }else{
					// CYLog.d(TAG,"iconId = "+iconId);
					// }
					break;

				default:
					break;
				}

				return view;
			} else {
				CYLog.d(TAG, "return view2");

				view2.setVisibility(View.GONE);
				return view2;
			}
		}
	}

	public void setSchoolHelperIcon(String name, ImageView cIcon) {
		for (int i = 0; i < APPConstant.SCHOOL_HELPER_TYPE.length; i++) {
			if (name.equals(APPConstant.SCHOOL_HELPER_TYPE[i])) {
				cIcon.setImageResource(APPConstant.SCHOOL_HELPER_ICON[i]);
			}
		}

	}

	class ChatMessageBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				// lvChatmessages.requestLayout();
				// adapter.notifyDataSetChanged();
				// lvChatmessages.setSelection(lvChatmessages.getBottom());

				Bundle bundle = intent.getExtras();
				android.os.Message msg = new android.os.Message();

				if (bundle != null && bundle.containsKey("from")) {
					String obj = bundle.getString("from");

					// List<ChatItem> chatItems = dataCenterManagerService
					// .getChatItems();
					// for (ChatItem item : chatItems) {
					// if (item.getOwner().equals(obj)) {
					// item.setUnread(0);
					// }
					// }
					CYLog.d(TAG, "ChatMessageBroadCastReceiver");
					msg.obj = obj;
					msg.what = 0;
					handler.sendMessage(msg);
					// NotificationManager notiManager = (NotificationManager)
					// getSystemService(NOTIFICATION_SERVICE);
					// CYLog.d(TAG,
					// "notiManager.cancel ="+StreamUtils.stringToAsciiInt(getActionBar().getTitle().toString()));
					// notiManager.cancel(StreamUtils.stringToAsciiInt(getActionBar().getTitle().toString()));//
					// 通知栏清零
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		CYLog.i(TAG, "chatactivity onresume");
		// findMessagesFromRAM();
		// adapter.notifyDataSetChanged();
		super.onResume();
		// NotificationManager notiManager = (NotificationManager)
		// getSystemService(NOTIFICATION_SERVICE);
		// CYLog.d(TAG,
		// "getActionBar().getTitle()222="+StreamUtils.stringToAsciiInt(getActionBar().getTitle().toString()));
		// notiManager.cancel(StreamUtils.stringToAsciiInt(getActionBar().getTitle().toString()));//
		// 通知栏清零
		isFront = true;
		if (dataCenterManagerService != null) {
			// abletorefresh_messages = dataCenterManagerService.getChatData(
			// owner, messageType, Size);
			// nomorerefresh_messages = dataCenterManagerService.getChatData(
			// owner, ALLMESSAGES);
			// dataCenterManagerService.setNumflag(0);
			dataCenterManagerService.setIsChatingWithWho(getActionBar()
					.getTitle().toString());
			adapter.notifyDataSetChanged();
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		CYLog.i(TAG, "chatactivity onstart");
		isFront = false;
		if (dataCenterManagerService != null) {
			dataCenterManagerService.setIsChatingWithWho("");
		}
		super.onPause();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		CYLog.i(TAG, "chatactivity onstart");
		// lvChatmessages.setSelection(lvChatmessages.getBottom());
		// 获取消息界面传来的chatitem的cid
		/*
		 * Intent parentIntent = getIntent(); int type =
		 * parentIntent.getIntExtra("itemType", 1); CYLog.i(TAG,"type="+type);
		 * cid = parentIntent.getStringExtra("cid"); if (type ==
		 * ChatItem.PRIVATECHATITEM) {
		 * 
		 * 
		 * if (cid != null) { //tvNickname.setText(cid); bar.setTitle(cid);
		 * CYLog.i(TAG,"jid=" + cid); } } else if (type == ChatItem.NEWSITEM) {
		 * Channel rcvChannel = null; List<Channel> channels =
		 * app.getChannels(); for (Channel channel : channels) { if
		 * (channel.getChannelId().equals(cid)) { rcvChannel = channel; } }
		 * //tvNickname.setText(rcvChannel.getcName());
		 * bar.setTitle(rcvChannel.getcName()); }
		 */

		super.onStart();
	}

	class PushedMsgBroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			/*
			 * CYLog.i(TAG,"in newsreceiver"); PushedMessage pushedMessage =
			 * (PushedMessage) intent .getSerializableExtra("PushedMessage");
			 */
			try {
				if (intent != null && intent.hasExtra("channelId")) {
					String channelId = intent.getStringExtra("channelId");
					android.os.Message msg = new android.os.Message();
					msg.obj = channelId;
					msg.what = 1;
					handler.sendMessage(msg);
					// NotificationManager notiManager = (NotificationManager)
					// getSystemService(NOTIFICATION_SERVICE);
					// CYLog.d(TAG,
					// "getActionBar().getTitle()444="+StreamUtils.stringToAsciiInt(getActionBar().getTitle().toString()));
					// notiManager.cancel(StreamUtils.stringToAsciiInt(getActionBar().getTitle().toString()));//
					// 通知栏清零
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		if (chatReceiver != null)
			unregisterReceiver(chatReceiver);
		if (newsReceiver != null)
			unregisterReceiver(newsReceiver);
		// add 取消绑定 ibind
		unbindService(dataCenterManagerIntentConn);
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		/*
		 * cid=null; messages=null;
		 */

		CYLog.i(TAG, "chatactivity onStop");
		super.onStop();
		Flag_RefreshUI = 0;
		IncreaseSize = 10;

		// +++++ 这里可能要重新处理一下这些标志量，待测试修改++++/////

		// if ( clickedChatItem == null
		// || owner == null) {
		// Flag_Adapt = 0;
		// return;
		// }
		// messages = messageListMap.get(owner);
		// if (messages == null || messages.size() < MinSize) {
		// Flag_Adapt = 0;
		// } else {
		// Flag_Adapt = 1;
		// }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.friend_info:
			Intent fpActivity = new Intent(this, FriendProfileActivity.class);
			fpActivity.putExtra("accountNum", dataCenterManagerService
					.getUserSelfContactsEntity().getUserAccount());
			fpActivity.putExtra("password", dataCenterManagerService
					.getUserSelfContactsEntity().getPassword());
			// fpActivity
			// ownerount", owner.substring(0,
			// owner.indexOf("@")));
			fpActivity.putExtra("friendAccount", owner);
			startActivity(fpActivity);
			break;
		case R.id.groupchat_room_info: {
			String roomId = getIntent().getStringExtra("roomId");
			String roomName = getIntent().getStringExtra("roomName");
			Intent intent = new Intent();
			Bundle mBundle = new Bundle();
			mBundle.putString("roomId", roomId);
			mBundle.putString("roomName", roomName);
			mBundle.putString("fromPage", "ChatActivity");
			intent.putExtras(mBundle);
			intent.setClass(mContext, ViewGroupChatDetailedInfoActivity.class);
			startActivity(intent);
		}
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case TAKE_PICTURE:
				// 将保存在本地的图片取出并缩小后显示在界面上
				// Bitmap bitmap =
				// BitmapFactory.decodeFile(EAPPConstant.CHAT_IMAGE
				// + File.separator
				// + TEMP_PIC_FILE);

				Uri imageUri = Uri.fromFile(new File(APPConstant.CHAT_FILE,
						TEMP_PIC_FILE));
				Bitmap bitmap = ImageUtils.getThumbnail(getBaseContext(),
						imageUri, 500);
				// Bitmap newBitmap = ImageTools.zoomBitmap(bitmap,
				// bitmap.getWidth() / SCALE, bitmap.getHeight() / SCALE);
				// 由于Bitmap内存占用较大，这里需要回收内存，否则会报out of memory异常
				// bitmap.recycle();
				// 保存临时文件
				// ImageTools.savePhotoToSDCard(newBitmap,
				// APPConstant.CHAT_IMAGE,
				// TEMP_PIC_FILE);
				ImageTools.savePhotoToSDCard(bitmap, APPConstant.CHAT_FILE,
						TEMP_PIC_FILE);
				fileUrl = APPConstant.CHAT_FILE + File.separator
						+ TEMP_PIC_FILE;
				CYLog.i(TAG, "url------->" + fileUrl);
				// 准备上传图片到服务器
				tempPicture = new File(APPConstant.CHAT_FILE + File.separator
						+ TEMP_PIC_FILE);

				// 启动一个上传任务
				chatSendFileTask = new ChatSendFileTask(sendtype, fileUrl,
						account, pass, handler);
				chatSendFileTask.execute();
				break;
			case SEND_FILE:
				Uri fileUri = data.getData();
				CYLog.d(TAG, "选择的文件的url" + fileUri);
				fileUrl = fileUri.toString();
				if (fileUrl.startsWith("content")) {
					String[] proj = { MediaStore.Images.Media.DATA };

					Cursor actualimagecursor = managedQuery(fileUri, proj,
							null, null, null);

					int actual_image_column_index = actualimagecursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

					actualimagecursor.moveToFirst();

					fileUrl = actualimagecursor
							.getString(actual_image_column_index);

				} else {
					fileUrl = fileUrl.replace("file://", "");
				}
				final int start = fileUrl.lastIndexOf("/");

				String fname = "";
				try {
					fname = URLDecoder.decode(
							fileUrl.substring(start + 1, fileUrl.length()),
							"UTF-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				final String filename = fname;
				Dialog dialog = new AlertDialog.Builder(ChatActivity.this)
						.setTitle("发送文件")
						.setMessage("您确定发送文件  " + fname + " ?")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										String pathname = APPConstant.CHAT_FILE;
										File file = new File(pathname);
										// CYLog.i(TAG,filename+"  file.mkdirs();  "
										// +pathname);
										if (!file.exists()) {
											file.mkdirs();
										}
										FileInputStream fi = null;
										FileOutputStream fo = null;
										FileChannel in = null;
										FileChannel out = null;

										try {
											File newFile = new File(
													APPConstant.CHAT_FILE
															+ File.separator
															+ URLEncoder
																	.encode(filename,
																			"UTF-8"));

											fileUrl = fileUrl.substring(0,
													start + 1) + filename;
											fi = new FileInputStream(new File(
													fileUrl));
											fo = new FileOutputStream(newFile);
											fileUrl = APPConstant.CHAT_FILE
													+ File.separator
													+ URLEncoder.encode(
															filename, "UTF-8");
											in = fi.getChannel();// 得到对应的文件通道
											out = fo.getChannel();// 得到对应的文件通道
											in.transferTo(0, in.size(), out);// 连接两个通道，并且从in通道读取，然后写入out通道
										} catch (IOException e) {
											e.printStackTrace();
										} finally {
											try {
												fi.close();
												in.close();
												fo.close();
												out.close();
											} catch (IOException e) {
												e.printStackTrace();
											}
										}

										CYLog.i(TAG, "选择的文件的url" + fileUrl);
										chatSendFileTask = new ChatSendFileTask(
												sendtype, fileUrl, account,
												pass, handler);
										chatSendFileTask.execute();
									}
								})
						.setNegativeButton("返回",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
									}
								}).create();
				dialog.show();

				break;
			case CHOOSE_PICTURE:
				// ContentResolver resolver = getContentResolver();
				// 照片的原始资源地址
				Uri originalUri = data.getData();
				CYLog.i(TAG, "选择的照片的url" + originalUri);
				try {
					// 使用ContentProvider通过URI获取原始图片
					// Bitmap photo =
					// MediaStore.Images.Media.getBitmap(resolver,
					// originalUri);
					Bitmap photo = ImageUtils.getThumbnail(getBaseContext(),
							originalUri, 500);
					if (photo != null) {
						// 为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
						// Bitmap smallBitmap = ImageTools.zoomBitmap(photo,
						// photo.getWidth() / SCALE, photo.getHeight()
						// / SCALE);
						// 释放原始图片占用的内存，防止out of memory异常发生

						// 保存临时文件
						ImageTools.savePhotoToSDCard(photo,
								APPConstant.CHAT_FILE, TEMP_PIC_FILE);
						photo.recycle();
						// 准备上传图片到服务器
						tempPicture = new File(APPConstant.CHAT_FILE
								+ File.separator + TEMP_PIC_FILE);
						// 添加图片发送代码，图片url 为
						// Environment.getExternalStorageDirectory()+
						// File.separator + TEMP_PIC_FILE

						fileUrl = APPConstant.CHAT_FILE + File.separator
								+ TEMP_PIC_FILE;
						CYLog.i(TAG, "url------->" + fileUrl);
						// 启动一个上传任务
						// 第一个参数代表文件类型 第二个是文件地址 第三个为账号 第四个密码
						chatSendFileTask = new ChatSendFileTask(sendtype,
								fileUrl, account, pass, handler);
						chatSendFileTask.execute();

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			}
		}

	}

	public ChatMessage getSendingFileMessage(int fileType, int messageType,
			String from, String to) {
		ChatMessage rcvMessage = new ChatMessage();
		rcvMessage.setOwner(to);
		rcvMessage.setUserAccount(from);
		rcvMessage.setSenderAccount(from);
		rcvMessage.setRecvAccount(to);
		rcvMessage.setMessageContent(APPConstant.FILE_SEND_TMP + fileType);
		rcvMessage.setMid(UUID.randomUUID().toString());
		rcvMessage.setType(messageType);
		Date mTime = new Date(System.currentTimeMillis());
		rcvMessage.setTime(mTime);
		rcvMessage.setIsRead(0);
		rcvMessage.setSendSucc(true);// 接收的消息始终是成功的
		return rcvMessage;
	}
}