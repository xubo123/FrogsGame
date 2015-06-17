package com.hust.schoolmatechat.engine;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

public class ReplaceBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG="ApkDelete";
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		File downLoadApk = new File(Environment.getExternalStorageDirectory(),
				"NewAppSample.apk");
		if(downLoadApk.exists()){
			downLoadApk.delete();
		}
		CYLog.i(TAG, "downLoadApkFile was deleted!");
	}

}
