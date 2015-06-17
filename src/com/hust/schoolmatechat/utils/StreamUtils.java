package com.hust.schoolmatechat.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.CYLog;

public class StreamUtils {
	private static final String TAG = "StreamUtils";
	
	public static byte[] getBytes(InputStream is) {
		try {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while((len = is.read(buffer))!=-1){
			bos.write(buffer, 0, len);
		}
		is.close();
		bos.flush();
		byte[] result = bos.toByteArray();
		
		return  result;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.i(TAG, e.toString());
			return null;
		}
	}
	
	public static int stringToAsciiInt(String value)
	{
		int ascii = 0;
		char[] chars = value.toCharArray(); 
		for (int i = 0; i < chars.length; i++) {
			ascii+=(int)chars[i];
		}
		return ascii;
	}
	/**
	 * 手机号、姓名处理
	 * 
	 * @param value 传入的手机号或姓名
	 */
	public static String phoneOrNameManage(String value){
		value = value.replaceAll("(?: |-|_|\\+86)", "");  
		
		return value;
	}
	
	public static Boolean checkBaseInfo(SharedPreferences prefs){
	if(APPBaseInfo.FLAG==null){
		CYLog.d(TAG, APPBaseInfo.URL);	
		APPBaseInfo.MQTTServiceURL="tcp://"+prefs.getString("pushServer", "122.205.9.116")+":"+prefs.getString("pushServerPort", "1883");//MQTT服务器ip与端口号;219.140.177.108
		APPBaseInfo.REGISTER_CODE_SECRET_KEY=prefs.getString("codeSecretKey", "getRegisterCode123");// = "getRegisterCode123";
		APPBaseInfo.SCHOOL_ID_NUMBER=prefs.getString("baseId", "000010") ;//= "000150";
		APPBaseInfo.PUSH_SERVICE_ACCOUNT =prefs.getString("pushServerAccount", "system_hust");//= "system_hust";
		APPBaseInfo.PUSH_SERVICE_PASSWD=prefs.getString("pushServerPassword", "manager_hust");//= "manager_hust";
		APPBaseInfo.TIGASE_SERVER_DOMAIN=prefs.getString("chatServerDomain", "hust");// = "hust";
		APPBaseInfo.TIGASE_SERVER_PORT =Integer.parseInt(prefs.getString("chatServerPort", "5222"));//= 5222;
		APPBaseInfo.SCHOOLNAME= prefs.getString("schoolName", "华中科技大学");//= "窗友公司测试";
		APPBaseInfo.SCHOOLPHONE=prefs.getString("telphone", "02787542502"); ;//= "02787542502";
		APPBaseInfo.TigaseIP=prefs.getString("chatServer", "122.205.9.117");// = "219.140.177.108";
		APPBaseInfo.URL="http://"+ prefs.getString("webServer", "122.205.9.115")+":"+ prefs.getString("webServerPort", "80");// = "http://219.140.177.108:9080";
		APPBaseInfo.URL8088="http://"+ prefs.getString("fileServer", "122.205.9.115")+":"+ prefs.getString("fileServerPort", "8088");//文件服务器;;// = "http://219.140.177.108:8088";//文件服务器
		APPBaseInfo.FLAG = "true";
		CYLog.d(TAG, APPBaseInfo.URL);	
		return false;
	}
	return  true;
	}
}
