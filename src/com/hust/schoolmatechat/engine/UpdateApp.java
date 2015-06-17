package com.hust.schoolmatechat.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

public class UpdateApp {
	/** Called when the activity is first created. */
	boolean flag = false;
	private static final String TAG = "Update";
	private ProgressDialog pBar;
	private String downPath = APPConstant.getDOWNLOADURL();
	private String requestPath = APPConstant.getUPDATEURL();
	private String appName = "Schoolmatechat.apk";
	private String appVersion;
	private int newVerCode = 0;
	private String newVerName = "1.0";
	private Context mContext;

	public UpdateApp(Context context) {
		this.mContext = context;
	}

	public boolean doUpdateApp() {

		// Looper.prepare();

		if (isNetworkAvailable(mContext) == false) {
			// Toast.makeText(mContext, "不可联网", Toast.LENGTH_SHORT).show();
			// Looper.loop();
		} else {
			checkToUpdate();
			// Toast.makeText(mContext, "可联网", Toast.LENGTH_SHORT).show();
		}
		return flag;
	}

	// check the Network is available
	public static boolean isNetworkAvailable(Context context) {
		// TODO Auto-generated method stub
		try {

			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();
			return (netWorkInfo != null && netWorkInfo.isAvailable());// 检测网络是否可用
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e("isNetworkAvailable", "isNetworkAvailable" + e);
			return false;
		}
	}

	// check new version and update
	public void checkToUpdate() {
		try {
			// TODO Auto-generated method stub
			if (getServerVersion()) {
				CYLog.i(TAG,"getServersion OK!");
				String currentName = CurrentVersion.getVerName(mContext);
				if (appVersion.compareTo(currentName) > 0) {// Current Version
															// is
															// old
															// 弹出更新对话框
					CYLog.i(TAG,"be ready to show");
					flag = true;

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.i(TAG, e.toString());
		}
	}

	// show Update Dialog
	public void showUpdateDialog() {
		// TODO Auto-generated method stub
		CYLog.i(TAG,"showDialog");
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本");
		sb.append(CurrentVersion.getVerName(mContext));
		// sb.append("VerCode:");
		// sb.append(CurrentVersion.getVerCode(mContext));
		sb.append("\n");
		sb.append("发现新版本:");
		// sb.append(newVerName);
		// sb.append("NewVerCode:");
		sb.append(appVersion);
		sb.append("\n");
		sb.append("是否更新");
		Dialog dialog = new AlertDialog.Builder(mContext)
				.setTitle("软件更新")
				.setMessage(sb.toString())
				.setPositiveButton("更新", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						showProgressBar();// 更新当前版本
					}
				})
				.setNegativeButton("暂不更新",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
							}
						}).create();
		dialog.show();
	}

	public void showProgressBar() {
		// TODO Auto-generated method stub
		pBar = new ProgressDialog(mContext);
		pBar.setTitle("正在下载");
		pBar.setMessage("请稍后");
		pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		downAppFile(downPath);
	}

	// Get ServerVersion from GetUpdateInfo.getUpdateVerJSON
	public boolean getServerVersion() {
		// TODO Auto-generated method stub
		try {
			String newVerJSON = GetUpdateInfo.GetUpdateVerJSON(requestPath);
			CYLog.i(TAG,newVerJSON);
			newVerJSON = URLDecoder.decode(newVerJSON, "utf-8");
			JSONObject jsonobject = new JSONObject(newVerJSON);
			CYLog.i(TAG,newVerJSON);
			if (jsonobject.length() > 0) {
				JSONObject obj = jsonobject;
				try {
					appVersion = obj.getString("version");
					downPath = obj.getString("url");
					downPath = URLDecoder.decode(downPath, "UTF-8");
					CYLog.i(TAG,appVersion + "==" + downPath);
				} catch (Exception e) {
					CYLog.e(TAG, e.getMessage());
					newVerCode = -1;
					newVerName = "";
					return false;
				}
			}
		} catch (Exception e) {
			CYLog.e(TAG, e.getMessage());
			return false;
		}
		return true;
	}

	public void downAppFile(final String url) {
		pBar.show();

		new Thread() {
			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					long length = entity.getContentLength();
					Log.isLoggable("DownTag", (int) length);
					InputStream is = entity.getContent();
					FileOutputStream fileOutputStream = null;
					if (is == null) {
						CYLog.e(TAG, "isStream is null");
						return;
					}
					File file = new File(
							Environment.getExternalStorageDirectory(), appName);
					fileOutputStream = new FileOutputStream(file);
					byte[] buf = new byte[1024];
					int ch = -1;
					do {
						ch = is.read(buf);
						if (ch <= 0)
							break;
						fileOutputStream.write(buf, 0, ch);// 从0到ch写入到输出流
					} while (true);
					is.close();
					fileOutputStream.close();
					haveDownLoad();
				} catch (ClientProtocolException e) {
					CYLog.e("Download", e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					CYLog.e("Download", e.getMessage());
					e.printStackTrace();
				}
			}
		}.start();

	}

	// cancel progressBar and start new App
	public void haveDownLoad() {
		// TODO Auto-generated method stub
		Looper.prepare();
		pBar.cancel();

		// 弹出警告框，提示是否安装新的版本
		Dialog installDialog = new AlertDialog.Builder(mContext)
				.setTitle("下载完成").setMessage("是否安装新的应用")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						installNewApk();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				}).create();
		installDialog.show();
		Looper.loop();

	}

	// 本地数据库操作相关
	// final static String filepath = "data/data/com.hust.schoolmatechat/" +
	// APPConstant.getCYDBNAME();
	// final static String pathStr = "data/data/com.hust.schoolmatechat";
	final static String pathStr = Environment.getExternalStorageDirectory()
			.getAbsolutePath()
			+ File.separator
			+ "chuangyou"
			+ File.separator
			+ "data";
	final static String filepath = pathStr + File.separator
			+ APPConstant.getCYDBNAME();

	// 安装新的应用
	public void installNewApk() {
		try {
			// 删除之前的数据库文件
			File jhPath = new File(filepath);
			// 查看数据库文件是否存在
			if (jhPath.exists()) {
				jhPath.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(Environment
				.getExternalStorageDirectory(), appName)),
				"application/vnd.android.package-archive");
		mContext.startActivity(intent);
	}

}