package com.hust.schoolmatechat.engine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class GetUpdateInfo {
	//从服务器得到最新的版本
	public static String GetUpdateVerJSON(String serverPath){
		StringBuilder newVerJSON=new StringBuilder();
		HttpClient client=new DefaultHttpClient();
		HttpParams httpparams=client.getParams();
		HttpConnectionParams.setConnectionTimeout(httpparams, 3000);
		HttpConnectionParams.setSoTimeout(httpparams, 5000);
		try {
			HttpResponse response=client.execute(new HttpGet(serverPath));
			HttpEntity entity=response.getEntity();
			if(entity!=null){
				BufferedReader reader=new BufferedReader(new InputStreamReader(entity.getContent()),8192);//UTF-8转码
				String line =null;
				while((line=reader.readLine())!=null){
					newVerJSON.append(line+"\n");
				}
				reader.close();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newVerJSON.toString();
		
		
	}
}
