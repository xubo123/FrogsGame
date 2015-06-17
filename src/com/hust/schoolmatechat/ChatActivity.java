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
	// ����ˢ��
	private static final int Below_minSize = 0;
	private static final int Upon_minSize = 1;
	private static final int ABLE_TO_REFRESH = 2;
	private static final int NO_REFRESH_MORE = 3;
	private static final int ALLMESSAGES = 0;
	int Flag_RefreshUI = 0;// �Ƿ���ˢ��״̬ Flag_Adapt,
	final int MinSize = 10;// ��ʼchatAcitivity��ʾ����Ϣ����
	int IncreaseSize = 10;// ��Ϣ��ʾ��ʼ��С
	int Size = MinSize;// ��ǰlistView����Ϣ����
	// List<ChatMessage> unreadedmessages = new ArrayList<ChatMessage>();
	List<ChatMessage> abletorefresh_messages = new ArrayList<ChatMessage>();
	List<ChatMessage> messagesListTemp = new ArrayList<ChatMessage>();

	// List<ChatMessage> nomorerefresh_messages = new ArrayList<ChatMessage>();

	public int REQUEST_CODE;
	private static final int CHOOSE_PICTURE = 1;
	private static final int TAKE_PICTURE = 2;
	private static final int SEND_FILE = 3;
	private static final int SCALE = 2;// ��Ƭ��С����
	private static final String TEMP_PIC_FILE = "post_picture.jpg"; // ��ʱ�ļ�������

	private static final String IMAGE_UNSPECIFIED = "image/*";
	private File tempPicture;
	private ChatSendFileTask chatSendFileTask;
	private String fileUrl, account, pass, owner;
	private int messageType;
	private int sendtype = 1;
	private boolean isSendingFile = false;// �Ƿ����ڷ����ļ�
	private int sendFileProgress = 0;// �����ļ�������
	private ChatMessage sendingFileMessage;// ���ڷ����ļ�ʱ������ʾ���ȵ�������Ŀ
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
	private boolean markGroupChatActiviy = false;// ����Ƿ�ΪȺ��Ҫ�򿪵�ҳ��
	// List<ChatMessage> messages = new ArrayList<ChatMessage>();
	private int lastMessageTime = 0;
	
	public static Activity instance = null;   //�洢��ǰactivity��ʵ��
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
					// �յ�������Ϣ��ֻ�� UI���²�����
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
					// getChatData�����⣬����ֻ��ȡ�����µ�һ��������ʱ��ô��
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
					// .toString()));// ֪ͨ������
					//
					// dataCenterManagerService.setNumflag(0);
					// }
					clickedChatItem.setUnread(0);
					// dataCenterManagerService//�˴�clickedChatItem�����ݻ��Ǿɵģ������Լ����͵����µ���Ϣ���������������Ѿ����������ݿ⣬���ﲻ��Ҫ�ٸ���
					// .updateChatItemToDb(clickedChatItem);// ���µ������Ϣ

					// ++++lqg++++ ��Ȼ���Դ���obj����Ӧ��ֱ�Ӵ���item�������ṩЧ�ʣ����޸�
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
					 * item.getUnread(); } //btReturn.setText("����(" + unread +
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
					// getChatData�����⣬����ֻ��ȡ�����µ�һ��������ʱ��ô��
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
					// .toString()));// ֪ͨ������
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
					 * item.getUnread(); } //btReturn.setText("����(" + unread +
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
					function_choose.setVisibility(View.GONE);// ��ʱֻ����ͬʱ����һ��ͼƬ
				if (btSendNo != null)
					btSendNo.setClickable(false);
				if (abletorefresh_messages == null) {// ��һ����ʱ�������������ʾ�ļ����͵Ľ�����
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
				CYLog.d(TAG, "���ͷ��ص�url" + url);
				// �������˰���ʱ�Ľ�����item�Ƴ����������� ����Ϣ���ͻ���
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

					// ����ʱ�ļ��ƶ���ָ���ļ��У��Լ����͵��ļ�����Ҫ����
					// FileInputStream fi = null;
					// FileOutputStream fo = null;
					// FileChannel in = null;
					// FileChannel out = null;
					// try {
					// fi = new FileInputStream(new File(fileUrl));
					// fo = new FileOutputStream(new File(pathname+
					// File.separator + filename));
					// in = fi.getChannel();// �õ���Ӧ���ļ�ͨ��
					// out = fo.getChannel();// �õ���Ӧ���ļ�ͨ��
					// in.transferTo(0, in.size(), out);//
					// ��������ͨ�������Ҵ�inͨ����ȡ��Ȼ��д��outͨ��
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

					sendChatMessage(APPConstant.CMD_PREFIX_FILE_SEND + url);// ת����������Ϣ�����Է�
				} else {
					CYLog.d(TAG, "upload failed");
					sendChatMessage(APPConstant.CMD_PREFIX_FILE_SEND + sendtype);// �ļ��ϴ�ʧ�ܵĻ���û��url�������ļ����ͣ��Ա��Ժ�������ʾ����ʧ�ܵ�ͼ��
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
		 * �����������Ĺ������
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
				clickedChatItem.setUnread(0);// δ����Ϣ��Ϊ0
				dataCenterManagerService.updateChatItemToDb(clickedChatItem);// ���µ������Ϣ
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
		if (clickedChatItem != null && messageType == ChatItem.PRIVATECHATITEM) {// �����浥����Ϣ��Ŀ
			mLayoutBottom.setVisibility(View.VISIBLE);
			ischat = true;
			// if (owner != null) {
			// if (clickedChatItem.getName() != null) {
			// bar.setTitle(clickedChatItem.getName());
			// }
			// }
		} else if (clickedChatItem != null && messageType == ChatItem.NEWSITEM) {// ������������Ϣ��Ŀ
			mLayoutBottom.setVisibility(View.GONE);
			// bar.setTitle(userName);
		} else if (clickedChatItem != null
				&& messageType == ChatItem.SCHOOLHELPERITEM) {// ������У�Ѱ��æ��Ϣ��Ŀ
			mLayoutBottom.setVisibility(View.GONE);
			// bar.setTitle(clickedChatItem.getName());
		} else if (clickedChatItem != null
				&& messageType == ChatItem.GROUPCHATITEM) { // Ⱥ��
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
							// У�Ѱ��æ����Ҫˢ��
							// if (messageType == ChatItem.GROUPCHATITEM) {
							// return;
							// }
							Flag_RefreshUI = 1;

							Size += IncreaseSize;
							List<ChatMessage> temp_messages = abletorefresh_messages;
							abletorefresh_messages.clear();
							if (dataCenterManagerService != null) { // ��ֹ�첽����ʱ�������Ŀ���Ϊ��
								abletorefresh_messages = dataCenterManagerService
										.getChatData(owner, messageType, Size);
							} else {
								abletorefresh_messages = temp_messages;
							}// �Ѿ����������ҿ�ʼˢ��
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
							messagesListTemp.addAll(abletorefresh_messages);// ����һ��
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
			// У�Ѱ��æ��������
			if (messageType == ChatItem.SCHOOLHELPERITEM) {
				String auth = dataCenterManagerService
						.getUserSelfContactsEntity().getAuthenticated();
				if (auth == null || !auth.equals("1")) {
					Toast.makeText(ChatActivity.this, "�Բ������������İ༶��Ϣ��֤�ſ��Է���!",
							Toast.LENGTH_SHORT).show();
					return;
				}
			}
			String news = null;

			// ����ϢUrl���͵�NewsExploreActivity��������
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
			// ���ΪУ�Ѱ��æ����Ϣ
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
		// ʹ��Intent
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
			// ��ȡ���͵���Ϣ
			final String messageBody = etMessageBody.getText().toString();
			sendChatMessage(messageBody);
			etMessageBody.setText("");// ��������
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
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0); // ǿ�����ؼ���
			}
			break;
		case R.id.send_image:
			// Toast.makeText(ChatActivity.this, "����ͼƬ",
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
			// Toast.makeText(ChatActivity.this, "���շ���",
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
			// ָ����Ƭ����·����SD������TEMP_PIC_FILEΪһ����ʱ�ļ���ÿ�����պ����ͼƬ���ᱻ�滻
			openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(openCameraIntent, REQUEST_CODE);
			break;
		case R.id.send_file:
			// Toast.makeText(ChatActivity.this, "���շ���",
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
						Intent.createChooser(intent, "��ѡ��һ��Ҫ���͵��ļ�"),
						REQUEST_CODE);
			} catch (android.content.ActivityNotFoundException ex) {
				// Potentially direct the user to the Market with a Dialog
				Toast.makeText(this, "�밲װ�ļ�������", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	private void createResendDialog(final String messageBody) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// Add the buttons
		builder.setItems(new String[] { "�ط���Ϣ", "ȡ��" },
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
			// ��ȡ������Ϣ�б�
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
			// ��ȡ������Ϣ�б�
			// Map<String, List<ChatMessage>> messageListMap =
			// dataCenterManagerService
			// .getUnReadMessageListMap();
			// List<ChatMessage> FromMessages = new ArrayList<ChatMessage>();
			//
			// if (Flag_RefreshUI == 0) {
			// FromMessages = unreadedmessages; // ��һ�ν���������
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

			// ��ȡ������Ϣ�б�
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
			// FromMessages = unreadedmessages; // ��һ�ν���������
			// CYLog.d(TAG,"��һ�ν���������"+unreadedmessages.size());
			// } else {
			// if (Flag_Adapt == ABLE_TO_REFRESH) {
			FromMessages.clear();
			FromMessages.addAll(abletorefresh_messages);// ����ˢ��
			// CYLog.d(TAG, " ����ˢ��" + abletorefresh_messages.size());
			if (abletorefresh_messages.size() == 0) {
				FromMessages.clear();
				FromMessages.addAll(messagesListTemp);
			}
			// } else if (Flag_Adapt == NO_REFRESH_MORE) {
			// FromMessages = nomorerefresh_messages;// ��Ϣ�Ѿ�ȫ����ʾ�޷�ˢ��
			// List<ChatMessage> list = null;
			// list = FromMessages;
			// CYLog.d(TAG," ��Ϣ�Ѿ�ȫ����ʾ�޷�ˢ��"+nomorerefresh_messages.size());
			//
			// }
			// }
			List<ChatMessage> ToMessages = new ArrayList<ChatMessage>();
			int j = FromMessages.size();
			for (int i = j - 1; i >= 0; i--) {
				ToMessages.add(FromMessages.get(i));
				// ChatMessage a = FromMessages.get(FromMessages.size() -
				// getCount() +
				// i);//���ŵĻ�����
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
									.getUserAccount())) {// �����Լ�������Ϣ
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
									// �ӷ�������ͼƬ
									ImageUtils.setUserHeadIcon(ivPC,
											currentMessage.getIcon(), handler);
								}
							}
						} else {
							if (currentMessage.getIcon() != null) {
								// �ӷ�������ͼƬ
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

						// ʱ��
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
					// ���ļ�ʱ��ʾ������
					// CYLog.d(TAG, "isSendingFile=" + isSendingFile + "====="
					// + currentMessage.getMessageContent());
					if (isSendingFile
							&& currentMessage.getMessageContent().startsWith(
									APPConstant.FILE_SEND_TMP)) {
						CYLog.d(TAG, "isSendingFile����������");
						ImageView iv = (ImageView) view
								.findViewById(R.id.imageView1);
						ProgressBar sendingProgressBar = (ProgressBar) view
								.findViewById(R.id.progressBar1);
						if (sendFileProgress < 101) {
							CYLog.d(TAG, "sendFileProgress����������="
									+ sendFileProgress);
							if (currentMessage.getMessageContent().equals(
									APPConstant.FILE_SEND_TMP
											+ APPConstant.PICTURE)) {// ���͵���ͼƬ������ʾ����ͼƬ
								Bitmap photo = ImageUtils.getThumbnail(
										getBaseContext(), Uri
												.fromFile(new File(
														APPConstant.CHAT_FILE,
														TEMP_PIC_FILE)), 500);
								iv.setImageBitmap(photo);
							} else if (currentMessage.getMessageContent()
									.equals(APPConstant.FILE_SEND_TMP
											+ APPConstant.NORMAL_FILE)) {// ���͵�����ͨ�ļ�������ʾ����ͼƬ
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
							.getUserSelfContactsEntity().getUserAccount())) {// �����Լ����͵�
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
										chatItemOnClick(v); // ��ť���µĴ�����
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
						// ��ȡ��ϵ����Ϣ
						ContactsEntity contactsEntity = dataCenterManagerService
								.getFriendInfoByAccount(senderAccount);

						// ����������ͼ��
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

						// ����ʧ�ܵ���Ϣ�ж�
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
										chatItemOnClick(v); // ��ť���µĴ�����
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
						// ��ȡ��ϵ����Ϣ
						ContactsEntity contactsEntity = dataCenterManagerService
								.getUserSelfContactsEntity();

						// ����������ͼ��
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

						// ����ʧ�ܵ���Ϣ�ж�
						ImageView ivSengFail = (ImageView) view
								.findViewById(R.id.groupchatimageViewsendfail);
						if (!currentMessage.isSendSucc())
							ivSengFail.setVisibility(View.VISIBLE);
					}
					// ��ʾ������
					if (isSendingFile
							&& currentMessage.getMessageContent().startsWith(
									APPConstant.FILE_SEND_TMP)) {

						ImageView iv = (ImageView) view
								.findViewById(R.id.groupchatimageView1);
						ProgressBar sendingProgressBar = (ProgressBar) view
								.findViewById(R.id.groupchatprogressBar1);
						if (sendFileProgress < 101) {
//							CYLog.d(TAG, "sendFileProgress����������="
//									+ sendFileProgress);

							if (currentMessage.getMessageContent().equals(
									APPConstant.FILE_SEND_TMP
											+ APPConstant.PICTURE)) {// ���͵���ͼƬ����ʾ����ͼƬ
								Bitmap photo = ImageUtils.getThumbnail(
										getBaseContext(), Uri
												.fromFile(new File(
														APPConstant.CHAT_FILE,
														TEMP_PIC_FILE)), 500);
								iv.setImageBitmap(photo);
							} else if (currentMessage.getMessageContent()
									.equals(APPConstant.FILE_SEND_TMP
											+ APPConstant.NORMAL_FILE)) {// ���͵�����ͨ�ļ�������ʾ����ͼƬ
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
						// ���ñ���
						newsTitle.setText(newsMessage.getTitle());
						CYLog.d(TAG, newsMessage.getTitle());
						// ����tag
						if (i == 1) {
							rl.setTag(newsMessage.getNewsUrl() + "*"
									+ currentMessage.getMessageContent() + "*"
									+ newsMessage.getIcon());
						} else {
							rl.setTag(newsMessage.getNewsUrl() + "*"
									+ newsMessage.getTitle() + "*"
									+ newsMessage.getIcon());

						}

						// ����ͼƬ
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
					// //+++++����ֱ����currentMessage.getIcon()�Ļ����״��ң���Ϊ���/res�����ͼƬ��Դ�б仯�Ļ���ÿ��ͼƬ��id�ǻ��������ɵģ��͸�֮ǰ���ݿ�����id��һ����
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
					// ֪ͨ������
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
		// ֪ͨ������
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
		// ��ȡ��Ϣ���洫����chatitem��cid
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
					// ֪ͨ������
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
		// add ȡ���� ibind
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

		// +++++ �������Ҫ���´���һ����Щ��־�����������޸�++++/////

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
				// �������ڱ��ص�ͼƬȡ������С����ʾ�ڽ�����
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
				// ����Bitmap�ڴ�ռ�ýϴ�������Ҫ�����ڴ棬����ᱨout of memory�쳣
				// bitmap.recycle();
				// ������ʱ�ļ�
				// ImageTools.savePhotoToSDCard(newBitmap,
				// APPConstant.CHAT_IMAGE,
				// TEMP_PIC_FILE);
				ImageTools.savePhotoToSDCard(bitmap, APPConstant.CHAT_FILE,
						TEMP_PIC_FILE);
				fileUrl = APPConstant.CHAT_FILE + File.separator
						+ TEMP_PIC_FILE;
				CYLog.i(TAG, "url------->" + fileUrl);
				// ׼���ϴ�ͼƬ��������
				tempPicture = new File(APPConstant.CHAT_FILE + File.separator
						+ TEMP_PIC_FILE);

				// ����һ���ϴ�����
				chatSendFileTask = new ChatSendFileTask(sendtype, fileUrl,
						account, pass, handler);
				chatSendFileTask.execute();
				break;
			case SEND_FILE:
				Uri fileUri = data.getData();
				CYLog.d(TAG, "ѡ����ļ���url" + fileUri);
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
						.setTitle("�����ļ�")
						.setMessage("��ȷ�������ļ�  " + fname + " ?")
						.setPositiveButton("ȷ��",
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
											in = fi.getChannel();// �õ���Ӧ���ļ�ͨ��
											out = fo.getChannel();// �õ���Ӧ���ļ�ͨ��
											in.transferTo(0, in.size(), out);// ��������ͨ�������Ҵ�inͨ����ȡ��Ȼ��д��outͨ��
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

										CYLog.i(TAG, "ѡ����ļ���url" + fileUrl);
										chatSendFileTask = new ChatSendFileTask(
												sendtype, fileUrl, account,
												pass, handler);
										chatSendFileTask.execute();
									}
								})
						.setNegativeButton("����",
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
				// ��Ƭ��ԭʼ��Դ��ַ
				Uri originalUri = data.getData();
				CYLog.i(TAG, "ѡ�����Ƭ��url" + originalUri);
				try {
					// ʹ��ContentProviderͨ��URI��ȡԭʼͼƬ
					// Bitmap photo =
					// MediaStore.Images.Media.getBitmap(resolver,
					// originalUri);
					Bitmap photo = ImageUtils.getThumbnail(getBaseContext(),
							originalUri, 500);
					if (photo != null) {
						// Ϊ��ֹԭʼͼƬ�������ڴ��������������Сԭͼ��ʾ��Ȼ���ͷ�ԭʼBitmapռ�õ��ڴ�
						// Bitmap smallBitmap = ImageTools.zoomBitmap(photo,
						// photo.getWidth() / SCALE, photo.getHeight()
						// / SCALE);
						// �ͷ�ԭʼͼƬռ�õ��ڴ棬��ֹout of memory�쳣����

						// ������ʱ�ļ�
						ImageTools.savePhotoToSDCard(photo,
								APPConstant.CHAT_FILE, TEMP_PIC_FILE);
						photo.recycle();
						// ׼���ϴ�ͼƬ��������
						tempPicture = new File(APPConstant.CHAT_FILE
								+ File.separator + TEMP_PIC_FILE);
						// ���ͼƬ���ʹ��룬ͼƬurl Ϊ
						// Environment.getExternalStorageDirectory()+
						// File.separator + TEMP_PIC_FILE

						fileUrl = APPConstant.CHAT_FILE + File.separator
								+ TEMP_PIC_FILE;
						CYLog.i(TAG, "url------->" + fileUrl);
						// ����һ���ϴ�����
						// ��һ�����������ļ����� �ڶ������ļ���ַ ������Ϊ�˺� ���ĸ�����
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
		rcvMessage.setSendSucc(true);// ���յ���Ϣʼ���ǳɹ���
		return rcvMessage;
	}
}