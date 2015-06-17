package com.hust.schoolmatechat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService.DataCenterManagerBiner;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.GroupManagementItem;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.utils.ImageUtils;

public class CreateGroupChatActivity extends Activity {
	private final String TAG = "CreateGroupChatActivity";
	//ListView
	private ListView mListView = null;
	//ListView������
	private MyListAdapter mListViewAdapter;
	//����Ⱥ�����ȷ�ϰ�ť
	private Button mConfirmButton;
	//����Ⱥ�������������
	private EditText mEditText;
	
	//�����ϵ���������з���,��������ѽ�Ⱥ����ListView���½�Ⱥ����ʱ��������ʱListView
	private List<GroupManagementItem> mGroupManagementItems=null;     //����ʾGroupManagementItem���б�
	private List<GroupManagementItem> mSourceList=null;              //����GroupManagementItem���б�    
	private Map<String, Boolean> mSourceListItemSelected=null;       //��Ӧ��mSourceListÿһ���Ƿ�ѡ��
	private Handler handler = new Handler();

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
        	
        	initCreateGroupChatActivity();
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
		super.onCreate(savedInstanceState);
		/*
		 * �����������Ĺ������
		 */
		final Intent dataCenterManagerIntent = new Intent(this, DataCenterManagerService.class);
		//startService(dataCenterManagerIntent);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn, Context.BIND_ABOVE_CLIENT); 	
	}
	
	private void initCreateGroupChatActivity() {
		// �����Զ���ActionBar
		ActionBar bar = getActionBar();
		bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_HOME_AS_UP|ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_SHOW_TITLE); 
		LayoutParams customActionbarParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		ViewGroup customActionbar = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.group_chat_actionbar, null);
		mConfirmButton = (Button)customActionbar.findViewById(R.id.group_chat_create_confirm);
		bar.setCustomView(customActionbar, customActionbarParams);
		bar.setTitle("���Ⱥ��");
		
		setContentView(R.layout.activity_group_chat);
		mEditText = (EditText)findViewById(R.id.group_chat_room_name);
		
		//��ʼ��
		mGroupManagementItems = new ArrayList<GroupManagementItem>();
		mSourceList=new ArrayList<GroupManagementItem>();
	    
		initAllGroupInfo();
		initConfirmCreateGroupChatRoom();
		//����������Ϊδѡ��
		CYLog.i(TAG, "mSourceList=");
		mSourceListItemSelected = new HashMap<String, Boolean>();  
		for(GroupManagementItem item:mSourceList){
			String gmid = item.getGmid();
			CYLog.i(TAG, item.getName() + "  " + item.getGmid());
			mSourceListItemSelected.put(gmid, false);
		}
		mListView = (ListView)findViewById(R.id.friends_list);
		mListViewAdapter = new MyListAdapter(this);
		mListView.setAdapter(mListViewAdapter);
		mListView.setOnItemClickListener(new ListView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//��ʼ��CheckBox��صĵ���¼�
				ViewHolderGroupItem groupHolder = null;
				ViewHolderContactItem contactHolder = null;
				switch(mListViewAdapter.getItemViewType(position)){
				case MyListAdapter.type_1:
					break;
				case MyListAdapter.type_2:
					contactHolder = (ViewHolderContactItem)view.getTag();
					contactHolder.checkbox.toggle();                            // ��ÿ�λ�ȡ�����itemʱ�ı�checkbox��״̬   
					GroupManagementItem item = (GroupManagementItem)mListViewAdapter.getItem(position);
					CYLog.i("GroupChatActivity","onItemClick"+item.getName());
					mSourceListItemSelected.put(item.getGmid(), contactHolder.checkbox.isChecked());
					mListViewAdapter.notifyDataSetChanged();
					break;
				}
                mConfirmButton.setText("ȷ��"+"("+getSelectedContact().size()+")");  
                
                //��ʼ����չ����ص���¼�
    			GroupManagementItem currentItem=(GroupManagementItem) mListViewAdapter.getItem(position);
    			if(GroupManagementItem.GROUP==currentItem.getType()){
    				CYLog.i("GroupChatActivity","click Group Item");
    				CYLog.i(TAG, "currentItem.gmid="+currentItem.getGmid());
    				CYLog.i(TAG, "currentItem.type="+currentItem.getType());
    				CYLog.i(TAG, "currentItem.GroupId="+currentItem.getGroupId());
    				CYLog.i(TAG, "currentItem.Name="+currentItem.getName());
    				if(currentItem.isExtended()){
    					currentItem.setExtended(false);
    				}else{
    					currentItem.setExtended(true);
    				}
    				
    				for(GroupManagementItem item:mSourceList){
    					if(item.getGroupId() == null){
    						CYLog.i(TAG, "item.getGroupId() is null!");
    					}
    					if(currentItem.getGroupId() == null){
    						CYLog.i(TAG, "currentItem.getGroupId() is null!");
    					}
    					if(item.getGroupId().equals(currentItem.getGroupId())&&GroupManagementItem.CONTACT==item.getType()){
    						if(item.isVisible()){
    							item.setVisible(false);
    						}else{
    							item.setVisible(true);
    						}
    					}
    				}
    				filterVisibleList(mSourceList,mGroupManagementItems);
    				mListViewAdapter.notifyDataSetChanged();
    			}
    			if(GroupManagementItem.CONTACT==currentItem.getType()){
    			}
            }
		});
		
	}
	
	public void initConfirmCreateGroupChatRoom(){
		mConfirmButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String inputRoomName = mEditText.getText().toString();
				List<Map<String,String>> selectedContacts = getSelectedContact();
				if(inputRoomName.equals("")||selectedContacts.size()==0){
					Toast.makeText(getApplicationContext(), "������д����������������д!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				//����µ�������
				Map<String, Integer> memberMap = new HashMap<String, Integer>();
				for (int i=0; i < selectedContacts.size(); i++) {
					try {
						String account = selectedContacts.get(i).get("gmid").replace("contact_", "");
						memberMap.put(account, 0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				dataCenterManagerService.addGroupChatRoom(inputRoomName, memberMap, false);
				
				CYLog.i("GroupChatActivity","initConfirmCreateGroupChatRoom is called4!");
				Intent intent = new Intent(getApplicationContext(),MainActivity.class);
				startActivity(intent);
			}
			
		});
	}
	
	/**
	 * �����������checkbox������,�����ȫѡ�˷�����������ϵ��
	 */
	private class MyCheckBoxListener implements CheckBox.OnCheckedChangeListener{
		private int mPosition;
		public MyCheckBoxListener(int position){
			mPosition = position;
		}
		@Override
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
			String currentItemGroupId = mGroupManagementItems.get(mPosition).getGroupId();
			mSourceListItemSelected.put(mGroupManagementItems.get(mPosition).getGmid(), isChecked);
			CYLog.i(TAG, "onCheckedChanged-->"+mPosition+""+mGroupManagementItems.get(mPosition).getName() + "  " + mGroupManagementItems.get(mPosition).getGmid());
			
			for(Iterator<GroupManagementItem> iterator=mSourceList.iterator();iterator.hasNext();){
				GroupManagementItem item=iterator.next();
				if(item.getGroupId().equals(currentItemGroupId)&&GroupManagementItem.CONTACT==item.getType()){
					mSourceListItemSelected.put(item.getGmid(), isChecked);
    		    }
			}
			mListViewAdapter.notifyDataSetChanged();
			mConfirmButton.setText("ȷ��"+"("+getSelectedContact().size()+")");  
		}
		
	}
	
	/**
	 * ��ȡ���з�����Ϣ
	 */
	public void initAllGroupInfo() {
		Map<String, List<ContactsEntity>> classMap=dataCenterManagerService.getContactsEntityMap();
		updateSourcelist(classMap);
        filterVisibleList(mSourceList,mGroupManagementItems);
	}
	
	/**
	 * ���˵�����ʾ�ķ�����
	 */
	public void filterVisibleList(List<GroupManagementItem> sourceList,List<GroupManagementItem> targetList){
		targetList.clear();
		targetList.addAll(sourceList);
		for(Iterator<GroupManagementItem> iterator=targetList.iterator();iterator.hasNext();){
			GroupManagementItem item=iterator.next();
			CYLog.i("GroupChatActivity","filterVisibleList1--->item.Type="+item.getName() + "   GroupId=" + item.getGroupId());
			if((!item.isVisible())||(item.getType()==GroupManagementItem.MULTI_CHAT_GROUP)||(item.getType()==GroupManagementItem.MULTI_CHAT_ROOM)
					|| (item.getAuthorized()==0)){
				CYLog.i("GroupChatActivity","filterVisibleList2--->item.Type="+item.getName() + "   GroupId=" + item.getGroupId());
				iterator.remove();
			}
		}
	}
	
	/**
	 * ��mSourceList��ѡ��״̬�б��й��˵õ���ǰ��ʾ��GroupManagementItem��ѡ��״̬�б�
	 * ״̬��Ԫ�����˳��Ӧ�ú�mSourceList������ʵ�˳�򱣳�һ��
	 */
	public HashMap<Integer, Boolean> getVisibleSelectedList(){
		HashMap<Integer, Boolean> map = new HashMap<Integer, Boolean>();
		int pos = 0;
		for(GroupManagementItem item:mSourceList){
			if(item.isVisible()&&((item.getType()==GroupManagementItem.GROUP)||(item.getType()==GroupManagementItem.CONTACT))){
				map.put(pos, mSourceListItemSelected.get(item.getGmid()));
				pos++;
			}
		}
		return map;
	}
	
	/**
	 * �������б�ѡ�е���ϵ�˵�,�����������Ŀ¼��
	 */
	public List<Map<String,String>> getSelectedContact(){
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		for(int i=0; i<mSourceListItemSelected.size(); i++){
			GroupManagementItem item = mSourceList.get(i);
			if(item.getType()==GroupManagementItem.CONTACT&&mSourceListItemSelected.get(item.getGmid())==true){
				Map<String,String> map = new HashMap<String,String>();
				map.put("gmid", item.getGmid());
				map.put("name", item.getName());
				map.put("groupId", item.getGroupId());
				list.add(map);
				CYLog.i("GroupChatActivity","�����������Ⱥ���ҵ�����:"+"id="+item.getGmid()+"    "+"name="+item.getName()+"    "+"groupId="+item.getGroupId());
			}
		}
		return list;
	}
	
	/**
	 * ���·����б�
	 * @param roster
	 * @param classMap
	 * ����GroupManagementItem��gmid����Ϊ���¸�ʽ:
	 * (1)һ���Group:"group"+"_"+����
	 * (2)���������ҵ�Group:"Ⱥ��"
	 * (3)��ϵ��:"contact"+"_"+RosterEntry.getUser()
	 * (4)������:"groupchat_room"+"_"+getJid()
	 * (5)δ��֤��ϵ��:"unauthorized"+"_"+getUserId()
	 */
	public void updateSourcelist(Map<String, List<ContactsEntity>> classmap){
		if (dataCenterManagerService == null) {
			CYLog.e(TAG, "updateSourcelist dataCenterManagerService");
			return;
		}

		if (mSourceList != null) {
			mSourceList.clear();// ÿ�ζ�����װ��
		} else {
			mSourceList = new ArrayList<GroupManagementItem>();
		}
		int item_gmid = 1; // ��������ÿ��GroupManagementItem������,��Ҫȷ��ÿһ�����ж�һ�޶�������
		
		// ��ȡ�ڴ��е���ϵ���б�
		Map<String, List<ContactsEntity>> classMap = dataCenterManagerService
				.getContactsEntityMap();
		if (classMap == null || classMap.size() == 0) {
			// ��ȡ�������ݿ��е���ϵ���б�
			classMap = dataCenterManagerService.getLocalDbContactsEntityMap();
			if (classMap == null || classMap.size() == 0) {
				classMap = null;
			}
		}

		if (classMap != null) {
			for (Entry<String, List<ContactsEntity>> entry : classMap
					.entrySet()) {
				// ������е��鵽��ʾҳ��
				GroupManagementItem groupItem = new GroupManagementItem();
				groupItem.setGmid("group" + "_" + item_gmid);
				groupItem.setType(GroupManagementItem.GROUP);
				groupItem.setName(entry.getKey());
				groupItem.setGroupId(groupItem.getGmid());
				if (mSourceList != null)
					mSourceList.add(groupItem);
				item_gmid++;

				// ���������������е���֤����ϵ�˵���ʾҳ��ǰ��
				List<ContactsEntity> cmrs = entry.getValue();
				for (ContactsEntity cmr : cmrs) {
					GroupManagementItem contactItem = new GroupManagementItem();

					if (cmr.getAuthenticated().equals("1")) {// ��֤
						contactItem.setGmid("contact" + "_"
								+ cmr.getAccountNum());
						contactItem.setAuthorized(1);
					} else {
						continue;
					}

					contactItem.setUserId(cmr.getBaseInfoId());
					contactItem.setType(GroupManagementItem.CONTACT);
					contactItem.setName(cmr.getName());
					// ���ͼƬ��ַ
					contactItem.setIcon(cmr.getPicture());
					contactItem.setSignature(cmr.getSign());
					contactItem.setGroupId(groupItem.getGmid());
					contactItem.setVisible(false);
					mSourceList.add(contactItem);
				}

				// ���������������е�δ��֤����ϵ�˵���ʾҳ�����
				for (ContactsEntity cmr : cmrs) {
					GroupManagementItem contactItem = new GroupManagementItem();

					// �ȷ�1Ҳ��0 ���ݸ�ʽ���󣬴�log��δ��֤����
					if (cmr.getAuthenticated().equals("0")
							|| !cmr.getAuthenticated().equals("1")) {// δ��֤
						contactItem.setGmid("unauthorized" + "_"
								+ cmr.getBaseInfoId());
						contactItem.setAuthorized(0);
					} else {
						continue;
					}

					contactItem.setUserId(cmr.getBaseInfoId());
					contactItem.setType(GroupManagementItem.CONTACT);
					contactItem.setName(cmr.getName());
					contactItem.setSignature(cmr.getSign());
					contactItem.setGroupId(groupItem.getGmid());
					contactItem.setVisible(false);
					mSourceList.add(contactItem);
				}
			}
		} else {
			CYLog.e(TAG, "classMap is null system error no data sql or web");
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			Intent intent = new Intent(this,MainActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Ⱥ��������Ҷ�Ӧ�� ViewHolder
	 */
	public class ViewHolderGroupItem {    
		public ImageView group_icon = null;  //����ͼ��
		public TextView group_name = null;   //����
	    public CheckBox checkbox = null;     //CheckBox
	}

	/**
	 * ��ϵ�˶�Ӧ�� ViewHolder
	 */
	public class ViewHolderContactItem {    
		public ImageView contact_icon = null;              //����ͼ��
		public TextView contact_nickname = null;           //��������
		public ImageView contact_authorized = null;        //������֤ͼ��
		public ImageView contact_flag_unregistered = null; //������֤ͼ��
		public TextView contact_signature = null;          //����ǩ��
	    public CheckBox checkbox = null;                   //CheckBox
	}
	/**
	 * �����Ҷ�Ӧ�� ViewHolder
	 */
	public class ViewHolderChatRoomItem {    
		public ImageView chatroom_icon = null;             //Ⱥ����ͼ��
		public TextView chatroom_name = null;              //Ⱥ��������
		public TextView chatroom_signature = null;          //Ⱥ����ǩ��
	    public CheckBox checkbox = null;                   //CheckBox
	}
	
	/**
	 * �Զ���ListView������
	 */
	public class MyListAdapter extends BaseAdapter {    
        public  HashMap<Integer, Boolean> isSelected;    //������ŵ�ǰ��ʾ��GroupManagementItem��ѡ��״̬
        private Context context = null;    
        private LayoutInflater inflater = null;    
        
        final static int type_default = -1;//Ĭ����ʽ
        final static int type_1 = 0;   //��ϵ�˷�����ʾ��ʽ
        final static int type_2 = 1;   //��ϵ����ʾ��ʽ
    
        public MyListAdapter(Context context) {    
            this.context = context;    
            inflater = LayoutInflater.from(context);    
            init();    
        }    
    
        // ��ʼ�� ��������checkbox��Ϊδѡ��     
        public void init() {    
            isSelected = getVisibleSelectedList();
        }    
        
        //���������ݸ��º�ˢ��isSelected���飬���С���ܻ�仯
        public void notifyDataSetChanged(){
        	isSelected.clear();
        	isSelected.putAll(getVisibleSelectedList());
        	super.notifyDataSetChanged();
        }
    
        @Override    
        public int getCount() {    
            return mGroupManagementItems.size();    
        }    
    
        @Override    
        public Object getItem(int arg0) {    
            return mGroupManagementItems.get(arg0);    
        }    
    
        @Override    
        public long getItemId(int arg0) {    
            return 0;    
        }    
        
        // ÿ��convert view������ô˷�������õ�ǰ����Ҫ��view��ʽ  
        @Override  
        public int getItemViewType(int position) {  
            int type = mGroupManagementItems.get(position).getType();
            if (type == GroupManagementItem.GROUP)
                return type_1;  
            else if(type==GroupManagementItem.CONTACT)
                return type_2;
            else 
            	return type_default;
        }  
      
        @Override  
        public int getViewTypeCount() {  
            return 2;  
        } 
    
        @Override    
        public View getView(int position, View view, ViewGroup arg2) { 
			GroupManagementItem currentItem = mGroupManagementItems.get(position);
			CYLog.i(TAG, "getView-->"+position+"  "+currentItem.getName());
            ViewHolderGroupItem groupHolder = null;
            ViewHolderContactItem contactHolder = null;
            int type = getItemViewType(position);  
            
            if(view==null){
            	switch(type){
    			case type_1:
    	            view = inflater.inflate(R.layout.group_chat_group_list_item, null);
    	            groupHolder = new ViewHolderGroupItem();
    	            groupHolder.group_icon = (ImageView)view.findViewById(R.id.group_chat_group_icon);
    	            groupHolder.group_name = (TextView) view.findViewById(R.id.group_chat_group_name);
    	            groupHolder.checkbox = (CheckBox)view.findViewById(R.id.group_chat_group_checkbox);
    	            groupHolder.checkbox.setOnCheckedChangeListener(new MyCheckBoxListener(position));
    				view.setTag(groupHolder);
    				break;
    			case type_2:
    	            view = inflater.inflate(R.layout.group_chat_contact_list_item, null);
    	            contactHolder = new ViewHolderContactItem();
    	            contactHolder.contact_nickname = (TextView) view.findViewById(R.id.group_chat_contact_nickname);
    	            contactHolder.contact_signature = (TextView) view.findViewById(R.id.group_chat_contact_signature);
    	            contactHolder.contact_authorized = (ImageView) view.findViewById(R.id.group_chat_contact_authorized);
    	            contactHolder.contact_icon = (ImageView) view.findViewById(R.id.group_chat_contact_icon);
    				contactHolder.checkbox = (CheckBox)view.findViewById(R.id.group_chat_contact_checkbox);
    				view.setTag(contactHolder);
    				break;
    			default:
    				break;
            	}
            }else{
            	switch(type){
            	case type_1:
            		groupHolder = (ViewHolderGroupItem) view.getTag();
            		break;
            	case type_2:
            		contactHolder = (ViewHolderContactItem)view.getTag();
            		break;
        		default:
        			break;
            	}
            }
            
            switch (type) {
            case type_1:
	            groupHolder.group_name.setText(currentItem.getName());
	            groupHolder.checkbox.setChecked(isSelected.get(position));  
				if(currentItem.isExtended()){
					groupHolder.group_icon.setImageResource(R.drawable.group_item_arrow_down);
				}else{
					groupHolder.group_icon.setImageDrawable(getResources().getDrawable(R.drawable.group_item_arrow_right));
				}
            	break;
            case type_2:
				contactHolder.contact_nickname.setText(currentItem.getName());
				contactHolder.contact_signature.setText(currentItem.getSignature());
				contactHolder.checkbox.setChecked(isSelected.get(position));
				if(currentItem.getAuthorized()==0){
					contactHolder.contact_authorized.setImageResource(R.drawable.flag_unregistered);
				}else if(currentItem.getAuthorized()==1){
					contactHolder.contact_authorized.setImageResource(R.drawable.flag_registered);
				}
				
				if(currentItem.getIcon() != null) {
					ImageUtils.setUserHeadIcon(contactHolder.contact_icon, currentItem.getIcon(), handler);
				}
            	break;
        	default:
        		break;
            }
            return view;    
        }    
    
    }   

}
