package com.hust.schoolmatechat.channelselect;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.dao.ChannelDao;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.pushedmsgservice.Channel;
import com.hust.schoolmatechat.pushedmsgservice.ChannelList;
import com.hust.schoolmatechat.register.GetHandObj;
import com.hust.schoolmatechat.register.HttpupLoad;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Ƶ������
 * 
 * @Author RA
 * @Blog http://blog.csdn.net/vipzjyno1
 */
public class ChannelSelectActivity extends Activity implements
		OnItemClickListener {
	String TAG = "ChannelSelectActivity";
	/** �û�Ƶ����GRIDVIEW */
	private DragGrid userChannelGridView;
	/** ����Ƶ����GRIDVIEW */
	private OtherGridView otherChannelGridView;
	/** �û�Ƶ����Ӧ���������������϶� */
	DragAdapter userChannelAdapter;
	/** ����Ƶ����Ӧ�������� */
	OtherAdapter otherChannelAdapter;
	/** ����Ƶ���б� */
	ArrayList<String> otherChannelList = new ArrayList<String>();
	/** �û�Ƶ���б� */
	ArrayList<String> userChannelList = new ArrayList<String>();
	/** �û���Ȥ��GRIDVIEW */
	private DragGrid userInterestGridView;
	/** ������Ȥ��GRIDVIEW */
	private OtherGridView otherInterestGridView;
	/** �û���Ȥ��Ӧ���������������϶� */
	DragAdapter userInterestAdapter;
	/** ������Ȥ��Ӧ�������� */
	OtherAdapter otherInterestAdapter;
	/** ������Ȥ�б� */
	ArrayList<String> otherInterestList = new ArrayList<String>();
	/** �û���Ȥ�б� */
	ArrayList<String> userInterestList = new ArrayList<String>();
	/** �Ƿ����ƶ�����������Ƕ���������Ž��е����ݸ��棬�����������Ϊ�˱������̫Ƶ����ɵ����ݴ��ҡ� */
	boolean isMove = false;
	boolean isChange = false;
	ContactsEntity mContactsEntity;

	private GetHandObj getContent;
	private HttpupLoad GetTask;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 3:
				try {
					CYLog.i(TAG, "�޸Ľ��" + GetTask.getLoaddata().getStrResult());
					if (getContent.getIfsuccess(GetTask.getLoaddata()
							.getStrResult())) {
						if(isChange)
						Toast.makeText(ChannelSelectActivity.this, "�޸ĳɹ�",
								Toast.LENGTH_SHORT).show();
					} else {
						if(isChange)
						Toast.makeText(ChannelSelectActivity.this,
								"��������ͬ�������ѱ��汾��", Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	};
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*
		 * �����������Ĺ������
		 */
		final Intent dataCenterManagerIntent = new Intent(this,
				DataCenterManagerService.class);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn,
				Context.BIND_ABOVE_CLIENT);
		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// add ȡ���� ibind
		saveChannel();
		unbindService(dataCenterManagerIntentConn);
		super.onDestroy();
	}

	protected void initChatActivity() {
		// TODO Auto-generated method stub
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setDisplayShowHomeEnabled(false);
		setContentView(R.layout.activity_channel_select);

		CYLog.d(TAG, "initChatActivity");
		getUserData();
		initView();
		initData();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/** ��ʼ������ */
	private void getUserData() {
		CYLog.d(TAG, "getUserData");
		String[] userdata = new String[2];
		ContactsEntity mContactsEntity = dataCenterManagerService
				.getUserSelfContactsEntity();
		CYLog.d(TAG,
				"mContactsEntitygetIntrestType="
						+ mContactsEntity.getIntrestType());
		userdata[0] = mContactsEntity.getChannels();
		userdata[1] = mContactsEntity.getIntrestType();
		CYLog.d(TAG, "===" + mContactsEntity.getChannels());
		userChannelList = new ArrayList<String>();
		userInterestList = new ArrayList<String>();
		String[] channels = userdata[0].split(",");
		if (userdata[0].equals(""))
			channels = APPConstant.CHANNELLIST;
		if (channels != null)
			for (int i = 0; i < channels.length; i++) {
				userChannelList.add(channels[i]);
			}
		String[] interest = userdata[1].split(",");
		if (userdata[1].equals(""))
			interest = APPConstant.INTERESTLIST;
		if (interest != null)
			for (int i = 0; i < interest.length; i++) {
				userInterestList.add(interest[i]);
			}
		ChannelDao channelDao = new ChannelDao(this);
		List<Channel> channel = channelDao.getChanenelsALL();
		ChannelList channelList = new ChannelList();
		channelList.setList(channel);
		for (int i = 0; channelList != null && i < channelList.getList().size(); i++) {
			String name = channelList.get(i).getcName();

			if (name.equals(APPConstant.INTEREST_CHANNEL_NAME)) {
				CYLog.d(TAG, "marks=" + channelList.get(i).getChannelRemark());
				String[] marks = channelList.get(i).getChannelRemark()
						.split(",");
				for (int j = 0; marks != null && j < marks.length; j++) {
					int k = 0;
					for (k = 0; k < interest.length; k++) {
						if (marks[j].equals(interest[k])) {
							break;
						}
					}
					if (k == interest.length)
						otherInterestList.add(marks[j]);
				}
			} else if (!name.equals(APPConstant.SCHOOL_HELPER_CHANNEL_NAME)) {
				int k = 0;
				for (k = 0; k < channels.length; k++) {
					if (name.equals(channels[k])) {
						break;
					}
				}
				if (k == channels.length)
					otherChannelList.add(name);
			}
		}
	}

	/** ��ʼ������ */
	private void initData() {
		userChannelAdapter = new DragAdapter(this, userChannelList);
		userChannelGridView.setAdapter(userChannelAdapter);
		otherChannelAdapter = new OtherAdapter(this, otherChannelList);
		otherChannelGridView.setAdapter(this.otherChannelAdapter);
		// ����GRIDVIEW��ITEM�ĵ������
		otherChannelGridView.setOnItemClickListener(this);
		userChannelGridView.setOnItemClickListener(this);

		userInterestAdapter = new DragAdapter(this, userInterestList);
		userInterestGridView.setAdapter(userInterestAdapter);
		otherInterestAdapter = new OtherAdapter(this, otherInterestList);
		otherInterestGridView.setAdapter(this.otherInterestAdapter);
		// ����GRIDVIEW��ITEM�ĵ������
		otherInterestGridView.setOnItemClickListener(this);
		userInterestGridView.setOnItemClickListener(this);
	}

	/** ��ʼ������ */
	private void initView() {
		userChannelGridView = (DragGrid) findViewById(R.id.userGridView);
		otherChannelGridView = (OtherGridView) findViewById(R.id.otherGridView);
		userInterestGridView = (DragGrid) findViewById(R.id.userGridView_1);
		otherInterestGridView = (OtherGridView) findViewById(R.id.otherGridView_1);
	}

	/** GRIDVIEW��Ӧ��ITEM��������ӿ� */
	@Override
	public void onItemClick(AdapterView<?> parent, final View view,
			final int position, long id) {
		isChange = true;
		// ��������ʱ��֮ǰ������û��������ô���õ���¼���Ч
		if (isMove) {
			return;
		}
		switch (parent.getId()) {
		case R.id.userGridView:
			// positionΪ 0��1 �Ĳ����Խ����κβ���
			if (position != 0 && position != 1) {
				final ImageView moveImageView = getView(view);
				if (moveImageView != null) {
					TextView newTextView = (TextView) view
							.findViewById(R.id.text_item);
					final int[] startLocation = new int[2];
					newTextView.getLocationInWindow(startLocation);
					final String channel = ((DragAdapter) parent.getAdapter())
							.getItem(position);// ��ȡ�����Ƶ������
					otherChannelAdapter.setVisible(false);
					// ��ӵ����һ��
					otherChannelAdapter.addItem(channel);
					new Handler().postDelayed(new Runnable() {
						public void run() {
							try {
								int[] endLocation = new int[2];
								// ��ȡ�յ������
								otherChannelGridView.getChildAt(
										otherChannelGridView
												.getLastVisiblePosition())
										.getLocationInWindow(endLocation);
								MoveAnim(moveImageView, startLocation,
										endLocation, channel,
										userChannelGridView, 0);
								userChannelAdapter.setRemove(position);
							} catch (Exception localException) {
							}
						}
					}, 50L);
				}
			}
			break;
		case R.id.otherGridView:
			final ImageView moveImageView = getView(view);
			if (moveImageView != null) {
				TextView newTextView = (TextView) view
						.findViewById(R.id.text_item);
				final int[] startLocation = new int[2];
				newTextView.getLocationInWindow(startLocation);
				final String channel = ((OtherAdapter) parent.getAdapter())
						.getItem(position);
				userChannelAdapter.setVisible(false);
				// ��ӵ����һ��
				userChannelAdapter.addItem(channel);
				new Handler().postDelayed(new Runnable() {
					public void run() {
						try {
							int[] endLocation = new int[2];
							// ��ȡ�յ������
							userChannelGridView.getChildAt(
									userChannelGridView
											.getLastVisiblePosition())
									.getLocationInWindow(endLocation);
							MoveAnim(moveImageView, startLocation, endLocation,
									channel, otherChannelGridView, 0);
							otherChannelAdapter.setRemove(position);
						} catch (Exception localException) {
						}
					}
				}, 50L);
			}
			break;
		case R.id.userGridView_1:
			// positionΪ 0��1 �Ĳ����Խ����κβ���
			if (position != 0 && position != 1) {
				final ImageView moveImageView1 = getView(view);
				if (moveImageView1 != null) {
					TextView newTextView = (TextView) view
							.findViewById(R.id.text_item);
					final int[] startLocation = new int[2];
					newTextView.getLocationInWindow(startLocation);
					final String channel = ((DragAdapter) parent.getAdapter())
							.getItem(position);// ��ȡ�����Ƶ������
					otherInterestAdapter.setVisible(false);
					// ��ӵ����һ��
					otherInterestAdapter.addItem(channel);
					new Handler().postDelayed(new Runnable() {
						public void run() {
							try {
								int[] endLocation = new int[2];
								// ��ȡ�յ������
								otherInterestGridView.getChildAt(
										otherInterestGridView
												.getLastVisiblePosition())
										.getLocationInWindow(endLocation);
								MoveAnim(moveImageView1, startLocation,
										endLocation, channel,
										userInterestGridView, 1);
								userInterestAdapter.setRemove(position);
							} catch (Exception localException) {
							}
						}
					}, 50L);
				}
			}
			break;
		case R.id.otherGridView_1:
			final ImageView moveImageView1 = getView(view);
			if (moveImageView1 != null) {
				TextView newTextView = (TextView) view
						.findViewById(R.id.text_item);
				final int[] startLocation = new int[2];
				newTextView.getLocationInWindow(startLocation);
				final String channel = ((OtherAdapter) parent.getAdapter())
						.getItem(position);
				userInterestAdapter.setVisible(false);
				// ��ӵ����һ��
				userInterestAdapter.addItem(channel);
				new Handler().postDelayed(new Runnable() {
					public void run() {
						try {
							int[] endLocation = new int[2];
							// ��ȡ�յ������
							userInterestGridView.getChildAt(
									userInterestGridView
											.getLastVisiblePosition())
									.getLocationInWindow(endLocation);
							MoveAnim(moveImageView1, startLocation,
									endLocation, channel,
									otherInterestGridView, 1);
							otherInterestAdapter.setRemove(position);
						} catch (Exception localException) {
						}
					}
				}, 50L);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * ���ITEM�ƶ�����
	 * 
	 * @param moveView
	 * @param startLocation
	 * @param endLocation
	 * @param moveChannel
	 * @param clickGridView
	 */
	private void MoveAnim(View moveView, int[] startLocation,
			int[] endLocation, final String moveChannel,
			final GridView clickGridView, final int type) {
		int[] initLocation = new int[2];
		// ��ȡ���ݹ�����VIEW������
		moveView.getLocationInWindow(initLocation);
		// �õ�Ҫ�ƶ���VIEW,�������Ӧ��������
		final ViewGroup moveViewGroup = getMoveViewGroup();
		final View mMoveView = getMoveView(moveViewGroup, moveView,
				initLocation);
		// �����ƶ�����
		TranslateAnimation moveAnimation = new TranslateAnimation(
				startLocation[0], endLocation[0], startLocation[1],
				endLocation[1]);
		moveAnimation.setDuration(300L);// ����ʱ��
		// ��������
		AnimationSet moveAnimationSet = new AnimationSet(true);
		moveAnimationSet.setFillAfter(false);// ����Ч��ִ����Ϻ�View���󲻱�������ֹ��λ��
		moveAnimationSet.addAnimation(moveAnimation);
		mMoveView.startAnimation(moveAnimationSet);
		moveAnimationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				isMove = true;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				moveViewGroup.removeView(mMoveView);
				// instanceof �����ж�2��ʵ���ǲ���һ�����жϵ������DragGrid����OtherGridView
				if (clickGridView instanceof DragGrid) {
					if (type == 0) {
						otherChannelAdapter.setVisible(true);
						otherChannelAdapter.notifyDataSetChanged();
						userChannelAdapter.remove();
					} else if (type == 1) {
						otherInterestAdapter.setVisible(true);
						otherInterestAdapter.notifyDataSetChanged();
						userInterestAdapter.remove();
					}
				} else {
					if (type == 0) {
						userChannelAdapter.setVisible(true);
						userChannelAdapter.notifyDataSetChanged();
						otherChannelAdapter.remove();
					} else if (type == 1) {
						userInterestAdapter.setVisible(true);
						userInterestAdapter.notifyDataSetChanged();
						otherInterestAdapter.remove();
					}
				}
				isMove = false;
			}
		});
	}

	/**
	 * ��ȡ�ƶ���VIEW�������ӦViewGroup��������
	 * 
	 * @param viewGroup
	 * @param view
	 * @param initLocation
	 * @return
	 */
	private View getMoveView(ViewGroup viewGroup, View view, int[] initLocation) {
		int x = initLocation[0];
		int y = initLocation[1];
		viewGroup.addView(view);
		LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mLayoutParams.leftMargin = x;
		mLayoutParams.topMargin = y;
		view.setLayoutParams(mLayoutParams);
		return view;
	}

	/**
	 * �����ƶ���ITEM��Ӧ��ViewGroup��������
	 */
	private ViewGroup getMoveViewGroup() {
		ViewGroup moveViewGroup = (ViewGroup) getWindow().getDecorView();
		LinearLayout moveLinearLayout = new LinearLayout(this);
		moveLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		moveViewGroup.addView(moveLinearLayout);
		return moveLinearLayout;
	}

	/**
	 * ��ȡ�����Item�Ķ�ӦView��
	 * 
	 * @param view
	 * @return
	 */
	private ImageView getView(View view) {
		view.destroyDrawingCache();
		view.setDrawingCacheEnabled(true);
		Bitmap cache = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);
		ImageView iv = new ImageView(this);
		iv.setImageBitmap(cache);
		return iv;
	}

	/** �˳�ʱ�򱣴�ѡ������ݿ������ */
	private void saveChannel() {

		List<String> userChannelList = userChannelAdapter.getChannnelLst();
		String mychannels = "";
		for (int i = 0; i < userChannelList.size(); i++) {
			mychannels += userChannelList.get(i);
			if (i != userChannelList.size() - 1)
				mychannels += ",";
		}
		List<String> userInterestList = userInterestAdapter.getChannnelLst();
		String myinterests = "";
		for (int i = 0; i < userInterestList.size(); i++) {
			myinterests += userInterestList.get(i);
			if (i != userInterestList.size() - 1)
				myinterests += ",";
		}

		getContent = new GetHandObj();

		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("accountNum", dataCenterManagerService
					.getUserSelfContactsEntity().getUserAccount());

			jsonObject.put("password", dataCenterManagerService
					.getUserSelfContactsEntity().getPassword());
			JSONArray intrestType = arraytoJSon(myinterests.split(","));
			JSONArray channel = arraytoJSon(mychannels.split(","));
			jsonObject.put("channels", channel);
			jsonObject.put("intrestType", intrestType);
			JSONObject json_1 = jsonObject;
			JSONObject json_2 = new JSONObject();
			json_2.put("content", json_1);
			json_2.put("command", APPConstant.USER_PROFILE_UPDATE_USER_PROFILE);
			CYLog.i(TAG,"---------->" + json_2);

			GetTask = new HttpupLoad(APPConstant.getUSERURL(), json_2, handler,
					3, getApplicationContext());
			GetTask.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		dataCenterManagerService.updateChannelInterest(mychannels, myinterests);
		CYLog.d(TAG, "saveChannel=" + mychannels + myinterests);
	}

	@Override
	public void onBackPressed() {
		saveChannel();
		super.onBackPressed();
	}

	public JSONArray arraytoJSon(String[] list) {
		try {
			JSONArray jsonarray = new JSONArray();

			for (int i = 0; i < list.length; i++) {
				jsonarray.put(i, list[i]);
			}
			return jsonarray;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.i(TAG, e.toString());
			return null;
		}
	}
}
