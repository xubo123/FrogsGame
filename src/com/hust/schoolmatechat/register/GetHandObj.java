package com.hust.schoolmatechat.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hust.schoolmatechat.engine.CYLog;

public class GetHandObj {
	private static final String TAG = "GetHandObj";

	public GetHandObj() {
		// TODO Auto-generated constructor stub
	}

	public ArrayList<String> getcutApartID(String objString) {
		try {
			JSONObject sss = new JSONObject(objString);
			String idString = sss.getString("obj");
			JSONArray idlist = new JSONArray(idString);
			ArrayList<String> IDlist = new ArrayList<String>();
			ArrayList<String> AparymentList = new ArrayList<String>();
			for (int i = 0; i < idlist.length(); i++) {
				IDlist.add((String) idlist.get(i));
				AparymentList.add(IDlist.get(i).substring(0, 16));

			}

			return AparymentList;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}

	}
	
	public List<Map<String, String>> getSchoolList(String objString){
		try {
			List<Map<String, String>> dataList = new ArrayList<Map<String, String>>();
			JSONObject sss = new JSONObject(objString);
			String schoolString = sss.getString("obj");
			JSONArray schoollist = new JSONArray(schoolString);
			

			for (int i = 0; i < schoollist.length(); i++) {
				Map<String, String> dqMap=new HashMap<String, String>();
				   dqMap.put("baseId", schoollist.getJSONObject(i).getString("baseId"));
				   dqMap.put("city", schoollist.getJSONObject(i).getString("city"));
				   dqMap.put("province",schoollist.getJSONObject(i).getString("province"));
				   dqMap.put("schoolName", schoollist.getJSONObject(i).getString("schoolName"));
				   dataList.add(dqMap);
			}
			return dataList;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}
	}
	public String getMessage(String objString) {
		try {
			JSONObject sss = new JSONObject(objString);
			String idString = sss.getString("msg");

			return idString;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}
	}

	public Boolean getIfsuccess(String objString) {
		try {
			JSONObject sss = new JSONObject(objString);
			boolean idString = sss.getBoolean("success");

			return idString;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return false;
		}

	}

	public ArrayList<String> getfullApartID(String objString) {
		try {
			JSONObject sss = new JSONObject(objString);
			String idString = sss.getString("obj");
			JSONArray idlist = new JSONArray(idString);
			ArrayList<String> IDlist = new ArrayList<String>();
			ArrayList<String> AparymentList = new ArrayList<String>();
			for (int i = 0; i < idlist.length(); i++) {
				IDlist.add((String) idlist.get(i));
				AparymentList.add(IDlist.get(i));

			}

			return AparymentList;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}

	}

	public Map<String, String> getApartIDmap(String objString) {
		try {
			JSONObject sss = new JSONObject(objString);
			Map<String, String> map = new HashMap<String, String>();
			String idString = sss.getString("obj");
			JSONArray idlist = new JSONArray(idString);
			ArrayList<String> IDlist = new ArrayList<String>();
			ArrayList<String> AparymentList = new ArrayList<String>();
			for (int i = 0; i < idlist.length(); i++) {
				IDlist.add((String) idlist.get(i));
				AparymentList.add(IDlist.get(i).substring(0, 16));
				map.put(AparymentList.get(i), IDlist.get(i));
			}

			return map;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}
	}

}
