package com.hust.schoolmatechat.login;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

public class CheckNetworkState {
	private static Context context;
	private static CheckNetworkState instance = new CheckNetworkState();

	private CheckNetworkState() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static CheckNetworkState getInstance(Context context) {
		instance.context = context;
		return instance;
	}

	public boolean getNetworkState() {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
					.getState();
			State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
					.getState();
			if (cm.getActiveNetworkInfo() != null) {
				if (cm.getActiveNetworkInfo().isAvailable()) {
					return true;
				}
			}
			if (mobileState == State.CONNECTED || wifiState == State.CONNECTED) {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}


		return false;

	}

}
