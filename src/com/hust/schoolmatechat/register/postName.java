package com.hust.schoolmatechat.register;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;

public class postName {
	private HttpupLoad GetTask;
	private GetHandObj getContent;
	private ArrayList<String> GetID;
	private static final String TAG = "postName";
	public postName() {
		// TODO Auto-generated constructor stub
	}

	public ArrayList<String> post(String name) {
		JSONObject testarray = new JSONObject();
		JSONObject result = new JSONObject();
		try {
			testarray.put("name", name);
			testarray.put("schoolNum", "000150");
			result.put("command", APPConstant.USER_PROFILE_GET_USER_BASE_INFO_ID_LIST);
			result.put("content", testarray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		CYLog.i(TAG,"----->" + result);
		Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case 0:

					CYLog.i(TAG,"≤‚ ‘result"
							+ GetTask.getLoaddata().getStrResult());
					getContent = new GetHandObj();
					try {
						CYLog.i(TAG,"≤‚ ‘"
								+ getContent.getcutApartID(GetTask.getLoaddata()
										.getStrResult()));
						GetID = getContent.getcutApartID(GetTask.getLoaddata()
								.getStrResult());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				default:
					break;
				}
			}
		};

		return GetID;
	}
}
