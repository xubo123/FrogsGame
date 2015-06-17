package com.hust.schoolmatechat;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.datadigger.DataDigger;
import com.hust.schoolmatechat.datadigger.MyListViewAdapter;
import com.hust.schoolmatechat.db.Check;
import com.hust.schoolmatechat.db.GetDataFromCheckTable;
import com.hust.schoolmatechat.engine.CYLog;

public class ListViewAcitivity extends Activity {
	Button button1, button2, button3;
	private static final String TAG="ListViewAcitivity";
//	Handler handler;
	String[] checkFriend;
	HashMap<String, Integer> Ifchecked = new HashMap<String, Integer>();
	HashMap<String, Integer> Ifpassed = new HashMap<String, Integer>();

	/** �Խ��������ķ��� */
	private DataCenterManagerService dataCenterManagerService;
	ServiceConnection dataCenterManagerIntentConn = new ServiceConnection() {  
        @Override  
        public void onServiceDisconnected(ComponentName name) {  
        	dataCenterManagerService = null;
        }  
          
        @Override  
        public void onServiceConnected(ComponentName name, IBinder service) {  
            //����һ��MsgService����  
        	dataCenterManagerService = ((DataCenterManagerBiner)service).getService();
        	
        	initListViewActivity();
        }  
    };
    @Override
	public void onDestroy() {
		// add ȡ���� ibind
		unbindService(dataCenterManagerIntentConn);
		super.onDestroy();
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		/*
		 * �����������Ĺ������
		 */
		final Intent dataCenterManagerIntent = new Intent(this, DataCenterManagerService.class);
		//startService(dataCenterManagerIntent);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn, Context.BIND_ABOVE_CLIENT); 
	}
	
	private void initListViewActivity() {
		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		// bar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.back));
		bar.setDisplayShowHomeEnabled(false);
		bar.setTitle("�Ƽ�У����Ƭ");
		setContentView(R.layout.datadigger_listview);
		TextView NoClassmate = (TextView) findViewById(R.id.NoClassmate);
		NoClassmate.setVisibility(NoClassmate.GONE);
		final DataDigger aDataDigger = new DataDigger(getApplicationContext(), dataCenterManagerService);
		String[] Friend = aDataDigger.DigDataFromContacts();
		if (Friend!=null&&Friend.length != 0) {
			GetDataFromCheckTable check = new Check(this);
			ContentValues values = new ContentValues();
			try {
				for (int i = 0; i < Friend.length; i++) {
					values.put("Name", Friend[i]);
					check.addCheck(values);
				}
			} catch (SQLiteConstraintException e) {
				// TODO: handle exception
				e.printStackTrace();
			}

			for (int i = 0; i < Friend.length; i++) {
				Ifchecked.put(Friend[i],
						check.viewCheck("Name=?", new String[] { Friend[i] })
								.getIfChecked());
				Ifpassed.put(Friend[i],
						check.viewCheck("Name=?", new String[] { Friend[i] })
								.getIfPassed());

			}// ����Ƿ񱻼��鼰�Ƿ�ͨ��
			ListView list = (ListView) findViewById(R.id.ListView01);

			// ���ɶ�̬����

			ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
			if (Friend != null) {
				for (int i = 0; i < Friend.length; i++) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("ItemTitle", Friend[i]);
					CYLog.i(TAG,Friend[i]);
					map.put("ItemText", "������ͬѧ��");
					listItem.add(map);
				}
				// ������������Item�Ͷ�̬�������Ӧ��Ԫ��
				// SimpleAdapter listItemAdapter =new SimpleAdapter(this,
				// listItem, R.layout.listview_layout, new
				// String[]{"ItemTitle","ItemText"}, new
				// int[]{R.id.ItemTitle,R.id.ItemText});
				// ��Ӳ���ʾ
				GetDataFromCheckTable check2=new Check(getApplicationContext());
				int j=0;//δ��֤ͬѧ������������
				HashMap<String, String> Name_Time=new HashMap<String, String>();
				for (int i = 0; i < Friend.length; i++){
					checkFriend=new String[Friend.length];
					if(check2.viewCheck("Name=?", new String[]{listItem.get(i).get("ItemTitle")}).getIfChecked()==0){
						CYLog.i(TAG,listItem.get(i).get("ItemTitle"));
						checkFriend[j]=listItem.get(i).get("ItemTitle");
				     }
				}
				Name_Time=aDataDigger.aContacts.getHistory(checkFriend);
				final MyListViewAdapter aDapt = new MyListViewAdapter(
						getApplicationContext(), listItem, Ifchecked, Ifpassed,
						Friend, aDataDigger,Name_Time,this.dataCenterManagerService);
				list.setAdapter(aDapt);
//				bar2.setVisibility(bar2.GONE);
			}

		} else {
			NoClassmate.setVisibility(NoClassmate.VISIBLE);
			NoClassmate.setTextSize(20);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}