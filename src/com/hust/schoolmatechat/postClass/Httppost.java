package com.hust.schoolmatechat.postClass;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.hust.schoolmatechat.engine.CYLog;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class Httppost extends AsyncTask<Void, Integer, Boolean> {
	String strResult = "";
	private Message msg;
	String url;
	private Handler handler;
	String Str;
	int order;
	String TAG = "Httppost";
	
	public Httppost(String url, Handler handler, String str, int order) {
		super();
		this.url = url;
		this.handler = handler;
		Str = str;
		this.order = order;
	}

	public String getStrResult() {
		return strResult;
	}

	public void setStrResult(String strResult) {
		this.strResult = strResult;
	}





	@Override
	protected Boolean doInBackground(Void... params) {
		Boolean success = false;
		success = connect(url, Str);
		msg = new Message();
		msg.what = order;
		handler.sendMessage(msg);
		return success;
	}

	public Boolean connect(String url, String json) {
		Boolean issuccess = false;
		HttpPost httpRequest = new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		NameValuePair pair1 = new BasicNameValuePair("jsonStr", json);
		params.add(pair1);

		try {
			HttpEntity httpentity = new UrlEncodedFormEntity(params, "utf-8");
			httpRequest.setEntity(httpentity);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得返回的字符串
				strResult = EntityUtils.toString(httpResponse.getEntity());
				// CYLog.i(TAG, "uploaddata数据" + strResult);
				CYLog.i(TAG,"返回的数据------>" + strResult);
				issuccess = true;
			} else {
				CYLog.i(TAG,"-->请求错误!");
				issuccess = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return issuccess;

	}

}
