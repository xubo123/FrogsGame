package com.hust.schoolmatechat;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.AppEngine;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.utils.DateFormatUtils;
import com.hust.schoolmatechat.utils.ImageUtils;

/**
 * 窗友安卓app框架
 * 
 * 
 * @author luoguangzhen
 */
public class NewsFragment extends Fragment implements Observer {
	private static final String TAG = "NewsFragment";
//	public MainActivity mContext;
	public LinearLayout mLayout;

	private LayoutInflater inflater = null;
	private ListView mListView = null;
	NewsFragmentAdapter mAdapter = null;
	private List<ChatItem> newsItems;
	private Handler handler = new Handler();

	/** 对接数据中心服务 */
	private DataCenterManagerService mDataCenterManagerService;

	public NewsFragment() {
	}

//	public NewsFragment(DataCenterManagerService dataCenterManagerService,
//			MainActivity mContext2) {
//		this.mDataCenterManagerService = dataCenterManagerService;
//		this.mContext = mContext2;
//	}
//	public static NewsFragment newInstance(
//			DataCenterManagerService dataCenterManagerService) {
//
//		NewsFragment nf = new NewsFragment();
//		Bundle args = new Bundle();
//		args.putParcelable("dataCenterManagerService",
//				dataCenterManagerService);
//		nf.setArguments(args);
//
//		return nf;
//
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
//		if (args != null) {
//			this.mDataCenterManagerService = (DataCenterManagerService) args
//					.getParcelable("dataCenterManagerService");
//		}
		if (getActivity() instanceof MainActivity)
			this.mDataCenterManagerService = ((MainActivity) getActivity()).getDataCenterManagerService();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.channelmanagement_main, container,
				false);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		if (mContext == null) {
//			mContext = (MainActivity) this.getActivity();
//		}
		if (mDataCenterManagerService == null) {
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), LogoActivity.class);
			startActivity(intent);
			this.getActivity().finish();
			return;
		}
		initNewsFragment();
	}

	private void initNewsFragment() {
		AppEngine.getInstance(getActivity()).addObserver(this);
		mLayout = (LinearLayout) getActivity()
				.findViewById(R.id.layout_channelm_main);
		// CYLog.i(TAG, "newsFragment onCreate");
		if (mLayout == null) {
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), LogoActivity.class);
			startActivity(intent);
			this.getActivity().finish();
			return;
		}
		mListView = (ListView) mLayout
				.findViewById(R.id.listView_channelm_list);

		inflater = (LayoutInflater) getActivity()
				.getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
		mAdapter = new NewsFragmentAdapter(this, mDataCenterManagerService);

		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new MyItemClickedListener());

		IntentFilter clFilter = new IntentFilter("channelistreceived");
		ChannelListBroadCastReceiver mChannelListBroadCastReceiver = new ChannelListBroadCastReceiver(
				this);
		getActivity().registerReceiver(mChannelListBroadCastReceiver, clFilter);
		if (getActivity() instanceof MainActivity)
		((MainActivity) getActivity()).addBroadcastReceiverToList(mChannelListBroadCastReceiver);

		IntentFilter newsFilter = new IntentFilter(
				"com.schoolmatechat.newsadded2message");
		PushedMessageBroadCastReceiver mPushedMessageBroadCastReceiver = new PushedMessageBroadCastReceiver(this);
		getActivity().registerReceiver(mPushedMessageBroadCastReceiver, newsFilter);
		if (getActivity() instanceof MainActivity)
		((MainActivity) getActivity()).addBroadcastReceiverToList(mPushedMessageBroadCastReceiver);

		updateNewsFramentUI();

	}

	@Override
	public void onResume() {
		super.onResume();
		// findNewsItems();
		if (mAdapter == null) {
			CYLog.e(TAG, "dataCenterManagerService == null,restartapp");
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), LogoActivity.class);
			startActivity(intent);
			this.getActivity().finish();
			return;
		} else {
//			ifgetActivity()xt == null) {
//				mContext = (MainActivity) this.getActivity();
//			}
			if (mDataCenterManagerService != null)
				newsItems = mDataCenterManagerService.getNewsItems();
			updateNewsFramentUI();
		}

		// setSkin();

	}

	public void setSkin() {
		int index = Integer
				.parseInt(AppEngine.getInstance(getActivity()).getTheme());
		mLayout.setBackgroundResource(APPConstant.NEWSFRAGMENT_BG_COLOR_IDS[index]);
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		// setSkin();
	}

	class NewsFragmentAdapter extends BaseAdapter {
		private DataCenterManagerService mDataCenterManagerService;

		NewsFragmentAdapter(NewsFragment mNewsFragment,
				DataCenterManagerService mDataCenterManagerService) {
			this.mDataCenterManagerService = mDataCenterManagerService;

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (mDataCenterManagerService != null) {
//				newsItems = mDataCenterManagerService.getNewsItems();
				if (newsItems == null) {
					CYLog.e(TAG,
							"NewsFragmentAdapter getCount dataCenterManagerService is null");
					return 0;
				}

				return newsItems.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			if (mDataCenterManagerService != null) {
//				newsItems = mDataCenterManagerService.getNewsItems();
				if (newsItems == null) {
					CYLog.e(TAG,
							"NewsFragmentAdapter getItem dataCenterManagerService is null");
					return null;
				}
				return newsItems.get(position);
			} else {
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub

			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (mDataCenterManagerService != null) {
//				newsItems = mDataCenterManagerService.getNewsItems();
				if (newsItems == null) {
					CYLog.e(TAG,
							"NewsFragmentAdapter getView dataCenterManagerService is null");
					return null;
				}
				View view = inflater.inflate(
						R.layout.channelmanagement_channel, null);
				ImageView cIcon = (ImageView) view
						.findViewById(R.id.iv_channelm_cicon);
				ImageView ivRedPoint = (ImageView) view
						.findViewById(R.id.iv_channelm_redpoint);
				TextView tvUnread = (TextView) view
						.findViewById(R.id.tv_channelm_unread);

				TextView tvCName = (TextView) view
						.findViewById(R.id.textView_channelm_cname);
				TextView tvTime = (TextView) view
						.findViewById(R.id.textView_channelm_time);
				TextView tvLatestMessage = (TextView) view
						.findViewById(R.id.textView_channelm_latestmessage);
//				CYLog.d(TAG, "position=" + position + ",newsItems.size()="
//						+ newsItems.size());
				if (position < newsItems.size()) {

					ChatItem currentItem = newsItems.get(position);
					if (currentItem.getUnread() > 0) {
						ivRedPoint.setVisibility(View.VISIBLE);
						tvUnread.setVisibility(View.VISIBLE);
						tvUnread.setText(currentItem.getUnread() + "");
					}

					// ++++lqg++++ 待提供根据账号获取联系人名称后修改
					// CYLog.d(TAG, "news owner :" + currentItem.getOwner());
					tvCName.setText(currentItem.getOwner());
					if (currentItem.getTime() != null)
						tvTime.setText(DateFormatUtils
								.date2ChatItemTime(currentItem.getTime()));
					if (currentItem.getLatestMessage() != null)
						tvLatestMessage.setText(currentItem.getLatestMessage());

					if (currentItem.getType() == ChatItem.SCHOOLHELPERITEM) {
						try {
							int iconId = Integer
									.parseInt(currentItem.getIcon());
							cIcon.setImageResource(iconId);
						} catch (Exception e) {
							e.printStackTrace();
						}

					} else if (currentItem.getType() == ChatItem.NEWSITEM
							&& currentItem.getIcon() != null) {
						ImageUtils.setIcon(cIcon, currentItem.getIcon(),
								handler);
					}
				} else {
					view.setVisibility(View.GONE);
				}
				// CYLog.d(TAG, "icon = " + currentItem.getIcon());

				return view;
			} else {
				return null;
			}
		}

	}

	class ChannelListBroadCastReceiver extends BroadcastReceiver {

		private NewsFragment newsFragment;

		ChannelListBroadCastReceiver(NewsFragment newsFragment) {
			this.newsFragment = newsFragment;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// CYLog.d(TAG, "ChannelListBroadCastReceiver onReceive");
			newsFragment.updateNewsFramentUI();
		}

	}

	class PushedMessageBroadCastReceiver extends BroadcastReceiver {

		private NewsFragment newsFragment;

		PushedMessageBroadCastReceiver(NewsFragment newsFragment) {
			this.newsFragment = newsFragment;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// CYLog.d(TAG, "PushedMessageBroadCastReceiver onReceive");
			newsFragment.updateNewsFramentUI();
		}

	}

	/**
	 * 通知adapter更新数据
	 */
	public void updateNewsFramentUI() {
		if (mAdapter == null && mDataCenterManagerService != null) {
//			if (getActivity() == null) {
//				getActivity() = (MainActivity) this.getActivity();
//			}
			// if(mDataCenterManagerService==null){
			// mDataCenterManagerSegetActivity()ontext.getDataCenterManagerService();
			// }
//			newsItems = mDataCenterManagerService.getNewsItems();
			initNewsFragment();
		}

		try {
			newsItems = mDataCenterManagerService.getNewsItems();
			mAdapter.notifyDataSetChanged();
			// mListView.setSelection(mListView.getBottom());
			// CYLog.d(TAG, "updateNewsFramentUI notifyDataSetChanged");
		} catch (Exception e) {
			e.printStackTrace();
			// CYLog.e(TAG, "updateNewsFramentUI : " + e.toString());
		}
	}

	@Override
	public void onDestroy() {
//		if (mContext == null) {
//			mContext = (MainActivity) this.getActivity();
//		}
		if (getActivity() instanceof MainActivity)
		((MainActivity) getActivity()).onStopForFragmentDestory();
		super.onDestroy();
	}

	class MyItemClickedListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			List<ChatItem> newsItems = mDataCenterManagerService.getNewsItems();
			List<ChatItem> chatItems = mDataCenterManagerService.getChatItems();
			ChatItem clickedItem = newsItems.get(position);
			clickedItem.setUnread(0);
			for (ChatItem item : chatItems) {
				if (clickedItem.getOwner().equals(item.getOwner())) {
					item.setUnread(0);
				}
			}
			mDataCenterManagerService.updateChatItemToDb(clickedItem);

			// 打开Chat页面
			Intent intent = new Intent(getActivity(), ChatActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putSerializable(ChatItem.SER_KEY, clickedItem);
			intent.putExtras(mBundle);
			// CYLog.i(TAG, "chat owner : " + clickedItem.getOwner());
			startActivity(intent);
		}

	}

}
