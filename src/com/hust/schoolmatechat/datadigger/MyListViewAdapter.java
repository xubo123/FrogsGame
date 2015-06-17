package com.hust.schoolmatechat.datadigger;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.db.Check;
import com.hust.schoolmatechat.db.GetDataFromCheckTable;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;

public class MyListViewAdapter extends BaseAdapter

{
	private static final String TAG = "MyListViewAdapter";
	ViewHolder vholder = new ViewHolder();
	private HashMap<Integer, Integer> lmap;
	private LayoutInflater iflater;
	private String strResult;
	private HttpupLoad GetTask;
	HashMap<String, String> Name_Time;
	String[] Friend;
	Context context1;
	DataDigger aDataDigger;
	HashMap<String, Integer> ifchecked;
	HashMap<String, Integer> ifpassed;
	Handler handler;
	ArrayList<HashMap<String, String>> listData;
	DataCenterManagerService dataCenterManagerService;

	public MyListViewAdapter(Context context,
			ArrayList<HashMap<String, String>> listData,
			HashMap<String, Integer> ifchecked,
			HashMap<String, Integer> ifpassed, String[] Friend,
			DataDigger aDataDigger, HashMap<String, String> Name_Time, DataCenterManagerService dataCenterManagerService) {
		this.context1 = context;
		this.ifchecked = ifchecked;
		this.ifpassed = ifpassed;
		this.Friend = Friend;
		this.aDataDigger = aDataDigger;
		this.listData = listData;// 数据挖掘的记录
		iflater = LayoutInflater.from(context);
		lmap = new HashMap<Integer, Integer>();
		for (int i = 0; i < listData.size(); i++) {
			lmap.put(i, 0);
		}
		this.dataCenterManagerService = dataCenterManagerService;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listData.size();
	}

	@Override
	public HashMap<String, String> getItem(int position) {
		// TODO Auto-generated method stub
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	// 重写View
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		// final ViewHolder vholder = null;
		if (convertView == null) {
			CYLog.d(TAG, "convertView == null");
			convertView = iflater.inflate(R.layout.check_layout, null);
			vholder = new ViewHolder();
			vholder.username = (TextView) convertView
					.findViewById(R.id.ItemTitle);
			vholder.id = (TextView) convertView.findViewById(R.id.ItemText);

			convertView.setTag(vholder);
		} else {
			vholder = (ViewHolder) convertView.getTag();
		}

		final GetDataFromCheckTable check = new Check(context1);//
		if (vholder == null)
			CYLog.d(TAG, "vholder==null");
		vholder.username.setText((String) listData.get(position).get(
				"ItemTitle"));

		vholder.id.setText((String) listData.get(position).get("ItemText"));

		final Button pass = (Button) convertView.findViewById(R.id.button1);
		final Button fail = (Button) convertView.findViewById(R.id.button2);
		final TextView Pass = (TextView) convertView.findViewById(R.id.Pass);
		final TextView Fail = (TextView) convertView.findViewById(R.id.Fail);
		pass.setVisibility(convertView.VISIBLE);
		fail.setVisibility(convertView.VISIBLE);

		if (lmap.get(position) == 1) {
			CYLog.d(TAG, "lmap.get(position)==1");
			Fail.setVisibility(convertView.VISIBLE);
			pass.setVisibility(convertView.GONE);
			pass.setClickable(false);
			fail.setVisibility(convertView.GONE);
			fail.setClickable(false);
		} else if (lmap.get(position) == 2) {
			CYLog.d(TAG, "lmap.get(position)==2");
			Pass.setVisibility(convertView.VISIBLE);
			pass.setVisibility(convertView.GONE);
			pass.setClickable(false);
			fail.setVisibility(convertView.GONE);
			fail.setClickable(false);

		}

		// 未检验
		if (ifchecked.get(Friend[position]) == 0 && lmap.get(position) == 0) {
			Pass.setVisibility(convertView.GONE);
			Fail.setVisibility(convertView.GONE);
			CYLog.d(TAG,
					"ifchecked.get(Friend[position]) == 0"
							+ listData.get(position).get("ItemTitle"));
			final HashMap<String, String> Record = Name_Time;
			fail.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					lmap.put(position, 1);
					ContentValues values = new ContentValues();
					values.put("IfChecked", 1);
					values.put("IfPassed", 0);
					check.updateCheck(values, "Name=?",
							new String[] { Friend[position] });
					Fail.setVisibility(v.VISIBLE);
					pass.setVisibility(v.GONE);
					pass.setClickable(false);
					fail.setVisibility(v.GONE);
					fail.setClickable(false);
					CYLog.d(TAG, "vholder.fail.setOnClickListener");
				}
			});

			pass.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					// TODO Auto-generated method stub
					lmap.put(position, 2);
					
					JSONObject jsonObject = new JSONObject();
					JSONObject jsonObject1 = new JSONObject();
					try {
						jsonObject1.put("accountNum", dataCenterManagerService.getUserSelfContactsEntity().getUserAccount());
						jsonObject1.put("password", dataCenterManagerService.getUserSelfContactsEntity().getPassword());
						String bId[] = dataCenterManagerService.getContactsEntityByName(Friend[position]).getBaseInfoId().split(",");
						jsonObject1.put("Gmid", bId[0].substring(0, 16));
						jsonObject1.put("Name", Friend[position]);
					//	CYLog.i(TAG,Friend[0]);
						// jsonObject1.put("Name","huyi");
						// jsonObject1.put("Gmid",
						// aDataDigger.classMateList.Name_Gmid.get(Friend[position]).substring(0,
						// 7));
						// jsonObject1.put("Gmid", "M2013727");
						HashMap<String, String> a = aDataDigger.aContacts.Name_Tel;
						jsonObject1.put("telId", aDataDigger.aContacts.Name_Tel
								.get(Friend[position]));
						CYLog.d(TAG,Friend[position]+aDataDigger.aContacts.Name_Tel
								.get(Friend[position]));
						// jsonObject1.put("telId","13265883886");
						String Time = null;
						try {
							Time = Record.get(Friend[position]);
						} catch (NullPointerException e) {
							// TODO: handle exception
							e.printStackTrace();
						}

						if (Time != null) {
							jsonObject1.put("useTime",
									Record.get(Friend[position]));
						}
						
						jsonObject.put("command",
								APPConstant.USER_PROFILE_UPDATE_CLASSMATE_TEL);
						jsonObject.put("content", jsonObject1);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					CYLog.d(TAG, jsonObject.toString());
					String uri = APPConstant.getURIDataDigger();
					
					handler = new Handler() {
						@Override
						public void handleMessage(Message msg) {
							// TODO Auto-generated method stub
							switch (msg.what) {
							case 0:
								try {
									JSONObject json= new JSONObject(GetTask.getLoaddata().getStrResult());
									Boolean b = json.getBoolean("success");
									if(b){
										Toast.makeText(context1,"验证成功，已向校友会推荐"+Friend[position]+"的校友名片",
												 Toast.LENGTH_SHORT).show();
										pass.setVisibility(v.GONE);
										pass.setClickable(false);
										fail.setVisibility(v.GONE);
										fail.setClickable(false);
										Pass.setVisibility(v.VISIBLE);
										ContentValues values = new ContentValues();
										values.put("IfChecked", 1);
										values.put("IfPassed", 1);
										check.updateCheck(values, "Name=?",
												new String[] { Friend[position] });
									}else{
										Toast.makeText(context1,"验证失败，"+json.get("msg").toString(),
											 Toast.LENGTH_SHORT).show();
									}
									
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									CYLog.e(TAG,""+e);
									e.printStackTrace();
								} 								 
								 
								break;
							default:
								break;
							}
						}
					};
					GetTask = new HttpupLoad(uri, jsonObject, handler);
					GetTask.execute();
					
				}

			});
		} else {
			CYLog.d(TAG, "已验证" + ifpassed.get(Friend[position]));
			pass.setVisibility(convertView.GONE);
			fail.setVisibility(convertView.GONE);

			if (ifpassed.get(Friend[position]) == 1 || lmap.get(position) == 2) {
				Pass.setVisibility(convertView.VISIBLE);
				Fail.setVisibility(convertView.GONE);
			} else if (ifpassed.get(Friend[position]) == 0
					|| lmap.get(position) == 1) {
				Fail.setVisibility(convertView.VISIBLE);
				Pass.setVisibility(convertView.GONE);
			}
		}

		return convertView;
	}

	class ViewHolder {
		TextView username, id;// , Fail, Pass;
		// Button pass, fail;
	}
}