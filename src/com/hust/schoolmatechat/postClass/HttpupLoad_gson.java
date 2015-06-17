package com.hust.schoolmatechat.postClass;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class HttpupLoad_gson extends AsyncTask<Void, Integer, Boolean> {
	private String httpurl;
	private String jsonobject;
	private String returnContent;
	private uploaddata_gson loaddata;
	private String Result;
	private Handler handler;
	private Message msg;
	private int order;
	private File file;

	Context mContext;

	/**
	 * http异步服务，通过handler处理返回结果信息
	 * @param url
	 * @param json
	 * @param hand
	 * @param i
	 * @param context
	 */
	public HttpupLoad_gson(String url, String json, Handler hand, int i,
			Context context) {
		httpurl = url;
		jsonobject = json;
		handler = hand;
		order = i;
		loaddata = new uploaddata_gson();
		mContext = context;
	}

	public HttpupLoad_gson(String url, String json, Handler hand, int i,
			Context context, File file) {
		httpurl = url;
		jsonobject = json;
		handler = hand;
		order = i;
		loaddata = new uploaddata_gson();
		mContext = context;
		this.file = file;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		Boolean success = false;
		if (order == 0 || order == 3 || order == 5 || order == 6 || order == 7
				|| order == 8 || order == 4 || order == 20 || order==11 || order==12 ||order==21) {
			// 上传个人信息为3 注册为0  认证为 11 修改密码为12
			success = loaddata.connect(httpurl, jsonobject);
		}

		if (order == 1) {
			// 图片上传为1
			try {
				success = loaddata.sendpicture(httpurl, jsonobject, file);
			} catch (Exception e) {
				e.printStackTrace();
				success = false;
			}
		}
//		if (order == 2) {
//			// 照片的获取
//			try {
//				success = loaddata.getPicture(httpurl,
//						jsonobject.getString("accountNum").toString());
//			} catch (JSONException e) {
//				e.printStackTrace();
//				success = false;
//			}
//
//		}
		
		msg = new Message();
		msg.what = order;
		handler.sendMessage(msg);
		return success;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(final Boolean success) {
//		if (!success) {
//			Toast.makeText(mContext, "网络服务失败，请重试", Toast.LENGTH_SHORT).show();
//		}
		super.onPostExecute(success);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	private boolean is = false;

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public String getResult() {
		return Result;
	}

	public void setResult(String result) {
		Result = result;
	}

	public uploaddata_gson getLoaddata() {
		return loaddata;
	}

	public void setLoaddata(uploaddata_gson loaddata) {
		this.loaddata = loaddata;
	}

	public String getReturnContent() {
		return returnContent;
	}

	public void setReturnContent(String returnContent) {
		this.returnContent = returnContent;
	}
}