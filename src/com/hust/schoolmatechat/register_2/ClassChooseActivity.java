package com.hust.schoolmatechat.register_2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.dao.DepartmentDao;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.postClass.GetClassmatesById;
import com.hust.schoolmatechat.postClass.HttpCommand;
import com.hust.schoolmatechat.postClass.HttpupLoad_gson;
import com.hust.schoolmatechat.register.GetHandObj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ClassChooseActivity extends Activity {
	ListView classList;
	private GetHandObj getContent;
	private Bundle bundle;
	private ArrayList<String> content;
	private String fullJson;
	private Map<String, String> map;
	private Map<String, String> idmap;
	private DepartmentDao departmentDao;
	private List<String> departmentList;
	private Map<String, String> departmentMap;
	ArrayList<HashMap<String, Object>> listItem;
	SimpleAdapter listItemAdapter;
	View lastView;
	private String fullID;
	HttpupLoad_gson httpupLoad_gson;
	String name,password,phoneNum;

	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 20:
				JSONObject json;
				try {
					json = new JSONObject(httpupLoad_gson.getLoaddata()
							.getStrResult());
					boolean success = json.getBoolean("success");
					if (!success) {
						Toast.makeText(getApplicationContext(), "获取班级数据失败，请重试",
								Toast.LENGTH_SHORT).show();
						break;
					}
					else{
						Intent intent = new Intent(getApplicationContext(),
								SelectClassmatesActivity.class);
						
						
						String classmatesStr=httpupLoad_gson.getLoaddata()
								.getStrResult();
						intent.putExtra("calssmatesStr", classmatesStr);
						intent.putExtra("baseInfo", idmap.get(fullID));
						intent.putExtra("name", name);
						intent.putExtra("password", password);
						intent.putExtra("phoneNum", phoneNum);
						
						startActivity(intent);
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
					break;
				}						
				
				

				break;
				default:
					break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.class_choose);
		classList = (ListView) findViewById(R.id.class_list);

		getContent = new GetHandObj();
		Intent intent = getIntent();
		bundle = intent.getExtras();
		if(bundle!=null){
			name=bundle.getString("name");
			password=bundle.getString("password");
			phoneNum=bundle.getString("phoneNum");
		}
		content = new ArrayList<String>();
		content = (ArrayList<String>) bundle.get("IDList");

		fullJson = (String) bundle.get("fullid");
		try {
			JSONObject sss = new JSONObject(fullJson);
			String idString = sss.getString("obj");
			JSONArray idlist = new JSONArray(idString);
			idmap = new HashMap<String, String>();
			for(int s = 0 ;s<idlist.length();s++){
				String id_16=idlist.getString(s).substring(0,16);
				idmap.put(id_16, idlist.getString(s));
			}
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String fullNameArray[] = null;
		try {
			String fullNameList = (String) bundle.get("fullNameList");
			if (fullNameList != null && !fullNameList.equals("")) {
				fullNameArray = fullNameList.split("_");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		map = new HashMap<String, String>();
		map = getContent.getApartIDmap(fullJson);

		departmentDao = new DepartmentDao(this.getApplicationContext());
		departmentList = new ArrayList<String>();
		departmentMap = new HashMap<String, String>();
		for (int i = 0; i < content.size(); i++) {
			String classBaseId = content.get(i).substring(0, 16);
			String grade = departmentDao.getDepartmentFullName(classBaseId);
			if (grade == null || grade.equals("")) {
				// 存本地
				if (fullNameArray != null && fullNameArray.length > i) {
					grade = fullNameArray[i];
				}

				if (grade != null && !grade.equals("")) {
					departmentDao.addDepartment(classBaseId, grade);
				} else {
					grade = "k,未知的院系,k,未知的班级";
				}
			}
			// 选择的字符串与id之间的反向映射
			String gradeSeg[] = grade.split(",");
			StringBuffer buf = new StringBuffer();
			buf.append(gradeSeg[1]).append(" ").append(gradeSeg[3]);
			departmentMap.put(buf.toString(), content.get(i));
			departmentList.add(buf.toString());
		}
		listItem = new ArrayList<HashMap<String, Object>>();
		listItem.clear();

		if (departmentList != null && departmentList.size() != 0) {
			for (int i = 0; i < departmentList.size(); i++) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("ItemTitle", departmentList.get(i));
				map.put("ItemPicture", R.drawable.checked);
				listItem.add(map);
			}
		}

		listItemAdapter = new SimpleAdapter(this, listItem,// 数据源
				R.layout.class_choose_item,// ListItem的XML实现
				// 动态数组与ImageItem对应的子项
				new String[] { "ItemTitle", "ItemPicture" },
				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
				new int[] { R.id.classname, R.id.class_check });
		classList.setAdapter(listItemAdapter);

		classList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (lastView != null) {
					lastView.findViewById(R.id.class_check).setVisibility(
							View.GONE);
				}
				lastView = view;
				view.findViewById(R.id.class_check).setVisibility(View.VISIBLE);
				String aprtment = departmentList.get(position);
				// String aprtment =
				// parent.getItemAtPosition(position).toString();
				fullID = departmentMap.get(aprtment).substring(0, 16);
				Toast.makeText(getApplicationContext(), fullID,
						Toast.LENGTH_SHORT).show();
				
				GetClassmatesById getClassmatesById=new GetClassmatesById(fullID);
				 HttpCommand httpCommand=new HttpCommand(APPConstant.USER_PROFILE_GET_CLASSMATES_INFO_LIST, 
						 getClassmatesById);
				 String uploadJson=httpCommand.getJsonStr();
				System.out.println("发送的数据"+uploadJson);
				 httpupLoad_gson=new HttpupLoad_gson(APPConstant.getUSERURL(),
						 uploadJson,
						 handler,
						 20,
						 getApplicationContext());
					httpupLoad_gson.execute();
			}
		});
	}
	private Object JSONObject(String fullJson2) {
		// TODO Auto-generated method stub
		return null;
	}
}
	
