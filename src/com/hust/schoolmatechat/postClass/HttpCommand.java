package com.hust.schoolmatechat.postClass;

import java.util.HashMap;

import com.google.gson.Gson;

public class HttpCommand {

	
    String JsonStr;

	public HttpCommand(String command, Object content) {
		super();
		HashMap map = new HashMap();
		map.put("command", command);
		map.put("content", content);
		Gson gson=new Gson();
		JsonStr=gson.toJson(map);
		
	}


	public String getJsonStr() {
		return JsonStr;
	}

	public void setJsonStr(String jsonStr) {
		JsonStr = jsonStr;
	}

}
