package com.hust.schoolmatechat.register_2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.register.GetHandObj;
import com.hust.schoolmatechat.register.HttpupLoad;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ProvinceChooseActivity extends Activity {
	private ListView list;
    Button search_btn;

	private HttpupLoad GetTask;
	private String TAG = "ProvinceChooseActivity";
	List<String> pList = new ArrayList<String>();
	private GetHandObj getContent;
	List<Map<String, String>> dataList;
	ArrayList<String> sList = new ArrayList<String>();
	ArrayList<String> idList = new ArrayList<String>();

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 19:
				CYLog.i(TAG, "查询结果" + GetTask.getLoaddata().getStrResult());

				dataList = getContent.getSchoolList(GetTask.getLoaddata()
						.getStrResult());
				for (int i = 0; i < dataList.size(); i++) {
					int j = 0;
					for (j = 0; j < pList.size(); j++) {
						if (pList.get(j)
								.equals(dataList.get(i).get("province")))
							break;
					}
					if (j == pList.size()) {
						pList.add(dataList.get(i).get("province"));
					}
				}
				if (pList.size() == 0) {
					pList.add("请选择省份");
				}
				CYLog.i("TAG", "--------->" + pList.get(0));
				ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

				for (int i = 0; i < pList.size(); i++) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("ItemTitle", pList.get(i));
					map.put("ItemImage", R.drawable.ic_launcher);// 图像资源的ID
					listItem.add(map);
				}
				SimpleAdapter listItemAdapter = new SimpleAdapter(
						getApplicationContext(), listItem,// 数据源
						R.layout.province_choose_item,// ListItem的XML实现
						// 动态数组与ImageItem对应的子项
						new String[] { "ItemTitle", "ItemImage" },
						// ImageItem的XML文件里面的一个ImageView,两个TextView ID
						new int[] { R.id.provincename, R.id.image });
				list.setAdapter(listItemAdapter);
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
		setContentView(R.layout.province_choose);
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		getContent = new GetHandObj();
		JSONObject json_2 = new JSONObject();
		try {
			json_2.put("command",
					APPConstant.USER_PROFILE_GET_SUPPORTED_SCHOOLS);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CYLog.i(TAG, "---------->" + json_2);

		GetTask = new HttpupLoad(APPConstant.BASEINFOURL, json_2, handler, 19,
				getApplicationContext());
		GetTask.execute();
		
		search_btn=(Button) findViewById(R.id.search_btn);
		search_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				search_btn.setVisibility(View.GONE);
			
				
			}
		});
		list = (ListView) findViewById(R.id.province_list);

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				sList.clear();
				idList.clear();

				for (int i = 0; i < dataList.size(); i++) {
					if (dataList.get(i).get("province")
							.equals(pList.get(position))) {
						sList.add(dataList.get(i).get("schoolName"));
						idList.add(dataList.get(i).get("baseId"));
					}
				}
                Intent intent0=getIntent();
                Bundle bundle=intent0.getExtras();
				Intent intent = new Intent();
				try {
		               String from=bundle.getString("Activity");
						intent.putExtra("Activity", from);
				} catch (Exception e) {
					// TODO: handle exception
				}
 
				intent.putExtra("schoolList", sList);
				intent.putExtra("idList", idList);
				intent.setClass(getApplicationContext(),
						SchoolChooseActivity.class);
				startActivity(intent);
			}
		});
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}
		return true;
	}
}
