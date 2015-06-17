package com.hust.schoolmatechat;

import java.util.Observable;
import java.util.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.channelselect.ChannelSelectActivity;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.AppEngine;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.utils.ImageUtils;

/**
 * 窗友安卓app框架
 * 
 * 
 * @author luoguangzhen
 */
public class MyselfFragment extends Fragment implements Observer {
	private static final String TAG = "MyselfFragment";
	// public MainActivity mContext;
	public LinearLayout mMyselfLayout;
	ImageView piture;
	int num = 0;

	/** 对接数据中心服务 */
	private DataCenterManagerService dataCenterManagerService;

	public MyselfFragment() {
	}

	// public MyselfFragment(DataCenterManagerService dataCenterManagerService,
	// MainActivity mContext2) {
	// this.dataCenterManagerService = dataCenterManagerService;
	// this.mContext = mContext2;
	// }
	// public static MyselfFragment newInstance(
	// DataCenterManagerService dataCenterManagerService) {
	//
	// MyselfFragment mf = new MyselfFragment();
	// Bundle args = new Bundle();
	// args.putParcelable("dataCenterManagerService",
	// dataCenterManagerService);
	// mf.setArguments(args);
	//
	// return mf;
	//
	// }

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		// if (args != null) {
		// this.dataCenterManagerService = (DataCenterManagerService) args
		// .getParcelable("dataCenterManagerService");
		// }
		if (getActivity() instanceof MainActivity)
			this.dataCenterManagerService = ((MainActivity) getActivity())
					.getDataCenterManagerService();
	}

	@Override
	public void onDestroy() {
		// if (mContext == null) {
		// mContext = (MainActivity) this.getActivity();
		// }
		if (getActivity() instanceof MainActivity)
		((MainActivity) getActivity()).onStopForFragmentDestory();
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.myself_layout, container, false);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// if(mContext==null){
		// mContext=(MainActivity) this.getActivity();
		// }
		if (dataCenterManagerService == null) {
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), LogoActivity.class);
			startActivity(intent);
			this.getActivity().finish();
			return;
		}
		initMyselFragment();
	}

	private void initMyselFragment() {
		mMyselfLayout = (LinearLayout) getActivity().findViewById(
				R.id.myself_linearLayout);
		AppEngine.getInstance(getActivity()).addObserver(this);
		if (mMyselfLayout == null) {
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), LogoActivity.class);
			startActivity(intent);
			this.getActivity().finish();
			return;
		}
		getActivity().findViewById(R.id.person_name_rl).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(getActivity(), AccountActivity.class);
						startActivity(intent);
					}
				});

		getActivity().findViewById(R.id.person_set_rl).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(getActivity(), SettingActivity.class);
						startActivityForResult(intent, 1);
					}
				});
		getActivity().findViewById(R.id.person_album_rl).setVisibility(
				View.GONE);
		getActivity().findViewById(R.id.person_album_rl).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Toast.makeText(getActivity(), "我的相册", Toast.LENGTH_SHORT)
								.show();
					}
				});
		getActivity().findViewById(R.id.person_collect_rl).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent1 = new Intent();
						intent1.setClass(getActivity(), ListViewAcitivity.class);
						startActivity(intent1);
					}
				});
		getActivity().findViewById(R.id.person_news_rl).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent1 = new Intent();
						intent1.setClass(getActivity(),
								ChannelSelectActivity.class);
						startActivity(intent1);
					}
				});
		getActivity().findViewById(R.id.person_class_rl).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						String auth = dataCenterManagerService
								.getUserSelfContactsEntity().getAuthenticated();
						if (auth == null || !auth.equals("1")) {
							TextView name = (TextView) getActivity()
									.findViewById(R.id.person_myname_tv);
							String userName = (String) name.getText()
									.toString();
							CYLog.i(TAG, "input user name : " + userName);
							if (userName != null && !userName.equals("")) {
								intent.putExtra("userName", userName);
								intent.setClass(getActivity(),
										StudyExActivity.class);
								startActivity(intent);
							} else {
								Toast.makeText(getActivity(), "抱歉，系统没有记录您的真实姓名", Toast.LENGTH_SHORT)
										.show();
							}
						} else {
							intent.putExtra("userName",
									dataCenterManagerService
											.getUserSelfContactsEntity()
											.getName());
							intent.setClass(getActivity(),
									StudyExActivity.class);
							startActivity(intent);
						}
					}
				});

	}

	@Override
	public void onResume() {
		super.onResume();
		// if (getActivity() == null) {
		// getActivity() = (MainActivity) this.getActivity();
		// }
		if (dataCenterManagerService == null) {
			CYLog.e(TAG, "dataCenterManagerService == null,restartapp");
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), LogoActivity.class);
			startActivity(intent);
			this.getActivity().finish();
			return;
		}
		if (dataCenterManagerService != null) {
			String accountNum = dataCenterManagerService
					.getUserSelfContactsEntity().getUserAccount();
			piture = (ImageView) getActivity().findViewById(R.id.head);
			Handler handler = new Handler();
			ImageUtils.setUserHeadIcon(piture, dataCenterManagerService
					.getUserSelfContactsEntity().getPicture(), handler);

			TextView name = (TextView) getActivity().findViewById(
					R.id.person_myname_tv);
			name.setText(dataCenterManagerService.getUserSelfContactsEntity()
					.getName());
			TextView num = (TextView) getActivity().findViewById(
					R.id.person_num_tv);
			num.setText(accountNum);
			initMyselFragment();
			// setSkin();
		} else {
			CYLog.e(TAG, "dataCenterManagerService is null");
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// super.onResume();
		super.onActivityResult(requestCode, resultCode, data);
		CYLog.i(TAG, "onActivityResult requestCode = " + requestCode);
		// 用户退出
		if (resultCode == 1) {
			// if (getActivity() == null) {
			// getActivity() = (MainActivity) this.getActivity();
			// }
			getActivity().finish();
		}
	}

	public void onSkinChange(int skinId) {
		AppEngine.getInstance(getActivity()).setTheme("" + skinId);

	}

	public void setSkin() {// 根据选择的皮肤设置背景
		int index = Integer.parseInt(AppEngine.getInstance(getActivity())
				.getTheme());
		mMyselfLayout
				.setBackgroundResource(APPConstant.MYSELFFRAGMENT_BG_COLOR_IDS[index]);
	}

	@Override
	public void update(Observable observable, Object data) {
		setSkin();
	}

	@Override
	public void onStop() {
		super.onStop();
	}
}