package com.hust.schoolmatechat.DataCenterManagerService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;

public class DownloadFileTask extends AsyncTask<String, Integer, String> {

	private static final String TAG = "DownloadFileTask";
	private String url;
	private String specifiedPath;
	private String specifiedName;

	private Handler handler;

	/**
	 * 
	 * @param url
	 *            ����url
	 * @param specifiedPath
	 *            ������ָ���Ĵ��·����Ϊnull���Զ���url��ȡ
	 * @param specifiedName
	 *            ������ָ�����ļ�����Ϊnull���Զ���url��ȡ
	 * @param handler
	 *            ����Ϊ��
	 */

	public DownloadFileTask(String url, String specifiedPath,
			String specifiedName, Handler handler) {
		super();
		this.url = url;
		this.specifiedPath = specifiedPath;
		this.specifiedName = specifiedName;
		this.handler = handler;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		handler.sendEmptyMessage(APPConstant.DOWNLOAD_STARTED);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		handler.sendMessage(handler.obtainMessage(
				APPConstant.UPDATE_DOWNLOAD_PROGRESS, values[0], 0));
	}

	@Override
	protected String doInBackground(String... params) {
		if (url == null)
			return null;
		// TODO Auto-generated method stub
		try {
			HttpClient client = new DefaultHttpClient();
//			String fileUrl =URLEncoder.encode(url, "UTF-8");
//			int start = url.lastIndexOf("/");
//			String filename = url.substring(start + 1, url.length());
//			fileUrl=url.substring(0, start+1)+URLEncoder.encode(filename,  "UTF-8");
			HttpGet get = new HttpGet(url);
			CYLog.d("DownloadFileTask", url);
			HttpResponse response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream in = response.getEntity().getContent();

				long totalSize = response.getEntity().getContentLength();
				int transferred = 0;
				int percentage = 0;

				File path = null;
				if (specifiedPath != null) {// ���������ָ���˴洢·������ʹ�õ�����ָ����·��
					String filePath = specifiedPath;
					path = new File(filePath);
				} else {// �����url��ȡ·��
					String subPath = getSubPathFromUrl(url);
					path = new File(subPath);
					CYLog.i(TAG,"download path:" + path);
				}

				if (!path.exists())
					path.mkdirs();// ���·���������򴴽�·��

				String fileName = specifiedName;
				if (fileName == null)
					fileName = getFileName(url);

				File downloadFile = new File(path, fileName);
				if (!downloadFile.exists()) {
					FileOutputStream fout = new FileOutputStream(downloadFile);
					int len = 0;
					byte[] buffer = new byte[1024];
					while ((len = in.read(buffer)) > 0) {
						fout.write(buffer, 0, len);
						transferred += len;
						percentage = (int) ((transferred / (float) totalSize) * 100);
						publishProgress(percentage);
					}

					fout.flush();
					in.close();
					fout.close();
				}
				return downloadFile.toString();
			}else{
				CYLog.e("DownloadFileTask", "DownloadFileTask fail,response.getStatusLine().getStatusCode()="+response.getStatusLine().getStatusCode()+response.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e("DownloadFileTask", "DownloadFileTask fail Exception"+e);
		}

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);

		handler.sendMessage(handler.obtainMessage(
				APPConstant.DOWNLOAD_FINISHED, result));
	}

	public String getSubPathFromUrl(String url) {
		if (url == null)
			return null;
		String[] path = url.split("all_user_files");
		int start = url.lastIndexOf("/");
		String filename = url.substring(start + 1, url.length());
		String pathname = APPConstant.CHAT_FILE
				+ File.separator
				+ path[1]
						.substring(0, path[1].length() - filename.length() - 1);

		//
		// int thirdIndex=0;
		// for(int i=0;i<3;i++){
		//
		// thirdIndex=url.indexOf("/");
		// url=url.substring(thirdIndex+1);
		// }
		// String subPath=url.substring(0, url.lastIndexOf("/"));
		// CYLog.i(TAG,"subPath:"+subPath);
		// all_user_files/20150202/249/1
		return pathname;
	}

	public String getFileName(String url) {
		if (url == null)
			return null;

		String name = null;
		name = url.substring(url.lastIndexOf("/") + 1);
		// CYLog.i(TAG,"file name:"+name);

		return name;
	}

}
