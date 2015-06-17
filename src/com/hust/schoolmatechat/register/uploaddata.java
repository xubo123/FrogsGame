package com.hust.schoolmatechat.register;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.hust.schoolmatechat.engine.CYLog;

public class uploaddata {
	private final static String TAG = "uploaddata";
	private String strResult = "";

	public String getStrResult() {
		return strResult;
	}

	public void setStrResult(String strResult) {
		this.strResult = strResult;
	}

	public uploaddata() {
		strResult = "";
	}

	public Boolean getPicture(String pictureUrl, String accountNum) {
		boolean it = false;
		HttpClient hc = new DefaultHttpClient();
		if (!pictureUrl.equals("")) {
			HttpGet hg = new HttpGet(pictureUrl);
			final Bitmap bm;
			HttpResponse hr;
			try {
				hr = hc.execute(hg);
				if (hr.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					it = true;
					strResult = EntityUtils.toString(hr.getEntity());
					bm = BitmapFactory
							.decodeStream(hr.getEntity().getContent());
					savepicture(bm, accountNum);
				}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return it;
	}

	/**
	 * 上传图片文件
	 * 
	 * @param url
	 * @param jsonStr
	 * @param file
	 * @return
	 * @throws JSONException
	 */
	public Boolean sendpicture(String url, JSONObject jsonStr, File file)
			throws JSONException {
		Boolean is = false;
		ContentBody cbFile = new FileBody(file);
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		// String jsonStr = new
		// String("{\"command\":\"6\",\"content\":{\"accountNum\":\"liming\",\"password\":\"lqg\"}}");
		HttpEntity req = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addPart("upload", cbFile)
				.addTextBody("jsonStr", jsonStr.toString()).build();

		httppost.setEntity(req);
		CYLog.i(TAG,"executing request " + httppost.getRequestLine());

		HttpResponse httpResponse;
		try {
			httpResponse = httpclient.execute(httppost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得返回的字符串
				strResult = EntityUtils.toString(httpResponse.getEntity());
				if (!strResult.equals("")) {
					is = true;
				}
				CYLog.i(TAG,"uploaddata数据" + strResult);
				// Toast.makeText(TestMainActivity.this, strResult,
				// Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;

	}

	public Boolean connect(String url, JSONObject json) {
		Boolean issuccess = false;
		HttpPost httpRequest = new HttpPost(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		NameValuePair pair1 = new BasicNameValuePair("jsonStr", json.toString());
		params.add(pair1);
		
		try {
			HttpEntity httpentity = new UrlEncodedFormEntity(params, "utf-8");
			httpRequest.setEntity(httpentity);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 取得返回的字符串
				strResult = EntityUtils.toString(httpResponse.getEntity());
				CYLog.i(TAG, "uploaddata数据" + strResult);
				issuccess = true;
			} else {
				CYLog.e(TAG,"-->请求错误!");
				issuccess = false;
			}
		} catch (Exception e) {
			CYLog.e(TAG,"-->请求错误!"+e);
			e.printStackTrace();
		}
		return issuccess;

	}

	public String acceptStr() {
		String accept = strResult;
		return accept;
	}

	public void savepicture(Bitmap mBitmap, String accountNum) {
		String PictureUrl = Environment.getExternalStorageDirectory().getAbsolutePath() + 
				File.separator + "chuangyou" + File.separator + "picture/" + accountNum + ".png";
		File f = new File(PictureUrl);

		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// DebugMessage.put("在保存图片时出错："+e.toString());
			CYLog.e(TAG,"在保存图片时出错：" + e.toString());
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
