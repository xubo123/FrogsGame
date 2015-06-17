package com.hust.schoolmatechat.datadigger;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.hust.schoolmatechat.engine.CYLog;

public class uploaddata {
	private static final String TAG = "uploaddata";
	private String strResult;

	public String getStrResult() {
		return strResult;
	}

	public void setStrResult(String strResult) {
		this.strResult = strResult;
	}

	public uploaddata() {
		strResult = null;
	}

	public Boolean connect(String url, JSONObject json) {
		Boolean issuccess = null;
		HttpPost httpRequest = new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		NameValuePair pair1 = new BasicNameValuePair("jsonStr", json.toString());
		params.add(pair1);
		try {
			HttpEntity httpentity = new UrlEncodedFormEntity(params, "utf-8");
			httpRequest.setEntity(httpentity);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)

			{
				// 取得返回的字符串
				strResult = EntityUtils.toString(httpResponse.getEntity());
				CYLog.i(TAG,"uploaddata数据:" + strResult);

				issuccess = true;
			}

			else

			{

				CYLog.e(TAG,"-->请求错误!");
				issuccess = false;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return issuccess;

	}

	public String acceptStr() {
		String accept = strResult;
		return accept;
	}
}
