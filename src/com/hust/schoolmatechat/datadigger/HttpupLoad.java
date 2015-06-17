package com.hust.schoolmatechat.datadigger;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class HttpupLoad extends AsyncTask<Void, Integer, Boolean> {
	private String httpurl;
	private JSONObject jsonobject;
	private String returnContent;
	private uploaddata loaddata;
	private String Result;
	private Handler handler;
	private Message msg ;
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
	public uploaddata getLoaddata() {
		return loaddata;
	}
	public void setLoaddata(uploaddata loaddata) {
		this.loaddata = loaddata;
	}
	public String getReturnContent() {
		return returnContent;
	}
	public void setReturnContent(String returnContent) {
		this.returnContent = returnContent;
	}
	
	public  HttpupLoad(String url,JSONObject json,Handler hand) {
		// TODO Auto-generated constructor stub
		httpurl=url;
		jsonobject=json;
		handler = hand;
		loaddata=new uploaddata();
	}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			Boolean is= loaddata.connect(httpurl,jsonobject);
			Result = loaddata.getStrResult();
			 msg = new Message();  
	            msg.what = 0;  
	            handler.sendMessage(msg);
	            return is;
			
			
	    }

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(final Boolean success) {

			
			super.onPostExecute(success);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	
	}