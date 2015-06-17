package com.hust.schoolmatechat.DataCenterManagerService;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.hust.schoolmatechat.DataCenterManagerService.CustomMultipartEntity.ProgressListener;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;

public class ChatSendFileTask extends AsyncTask<String, Integer	, String>{
	private static final String TAG="ChatSendFileTask";
	
	
	private int type=0;
	private String fileUrl;
	private String accountNum;
	private String password;
	
	private Handler handler;
	
	private long totalSize;
	
	
	
	
	
	/**
	 * 
	 * @param type �ϴ��ļ�������,������:ChatSendFile.PICTURE,ChatSendFile.AUDIO,ChatSendFile.VIDEO,ChatSendFile.NORMAL_FILE�е���һ��
	 * @param fileUrl Ҫ�ϴ����ļ�·��
	 * @param accountNum ��ǰ�˺�
	 * @param password ��ǰ�˺ŵ�����
	 * @param handler �����ϴ�UI��handler
	 */
	public ChatSendFileTask(int type, String fileUrl, String accountNum,
			String password, Handler handler) {
		super();
		this.type = type;
		this.fileUrl = fileUrl;
		this.accountNum = accountNum;
		this.password = password;
		this.handler = handler;
	}

	
	
	private static String generateUploadJson(String accountNum,String password,int type) throws JSONException{
		JSONObject object=new JSONObject();
		object.put("command", 1);
		
		/*{
		      "command":USER_PROFILE_UPLOAD_FILE,  �����ֶ�       
		      "content":
		      {
		       "accountNum":"abc123" �����ֶ�
		       "password":"123456" �����ֶ�
		       "type":"1" �����ֶ�
		      }
		}*/
		
		
		JSONObject content=new JSONObject();
		content.put("accountNum", accountNum);
		content.put("password", password);
		content.put("type", type+"");
		
		object.put("content", content);
		
		String jsonStr=object.toString();
		
		CYLog.i(TAG,"upload json: "+jsonStr);
		
		return jsonStr;
		
	}
	
	private static String parseUploadResult(String result) throws JSONException{
		
		
//		{"msg":"�ϴ��ɹ�!","obj":"http://127.0.0.1:8080/all_user_files/20150127/230/1/183312#kdjk.jpg","success":true}
		if(result==null)return null;
		
		JSONObject resultObj=new JSONObject(result);
		
		String url=null;
		
	
			if(resultObj.has("obj"))
				url=(String) resultObj.get("obj");
			
		
		return url;
	}

	
	
	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		try {
			String url =parseUploadResult(result);
			Log.i(TAG, "url: "+url);
			handler.sendMessage(handler.obtainMessage(APPConstant.UPLOAD_FINISHED, url));//��handler֪ͨ�ϴ�������message.objΪ���ص�����url
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		handler.sendEmptyMessage(APPConstant.UPLOAD_STARTED);//��handler��message֪ͨ��ʼ�ϴ�
		
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		handler.sendMessage(handler.obtainMessage(APPConstant.UPDATE_UPLOAD_PROGRESS, values[0], 0));//֪ͨhandler�����ϴ����ȣ�message.arg1�ǵ�ǰ�Ľ��Ȱٷֱ� 
	}

	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		String result=null;
		try{
		String userUrl=APPConstant.getUSERURL();
		
		String jsonStr=generateUploadJson(accountNum, password, type);
		
		File file=new File(fileUrl);
		
		
		
		if(userUrl==null||userUrl.equals("")||jsonStr==null||file==null)return null;
		
		
		
		HttpClient client=new DefaultHttpClient();
		HttpPost post=new HttpPost(userUrl);
		
		
		CustomMultipartEntity entity=new CustomMultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, new ProgressListener() {
			
			@Override
			public void transferred(long num) {
				// TODO Auto-generated method stub
				int percentage= (int)((num/(float)totalSize)*100);
				publishProgress(percentage);
			}
		});
		int start = fileUrl.lastIndexOf("/");
		final String filename = fileUrl.substring(start + 1,
				fileUrl.length());
		
		entity.addPart("fileNameUtf8", new StringBody(filename));
		entity.addPart("upload", new FileBody(file));
		entity.addPart("jsonStr",new StringBody(jsonStr));
		
		totalSize=entity.getContentLength();
		
		post.setEntity(entity);
		
		HttpResponse response=client.execute(post);
		
//		if(response.getStatusLine().getStatusCode()==HttpStatus.SC_ACCEPTED){
		result=EntityUtils.toString(response.getEntity());
//			CYLog.i(TAG,"upload result : "+result);
//		}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
	
	

}
