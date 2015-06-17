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
	//ListView适配器
	private MyListAdapter mListViewAdapter;
	//创建群聊组的确认按钮
	private Button mConfirmButton;
	//创建群聊组的名字设置
	private EditText mEditText;
	
	//存放联系人下面所有分组,用来填充已建群聊组ListView和新建群聊组时搜索好友时ListView
	private List<GroupManagementItem> mGroupManagementItems=null;     //可显示GroupManagementItem的列表
	private List<GroupManagementItem> mSourceList=null;              //所有GroupManagementItem的列表    
	private Map<String, Boolean> mSourceListItemSelected=null;       //对应于mSourceList每一项是否被选中
	private Handler handler = new Handler();

	/** 对接数据中心服务 */
	private DataCenterManagerService dataCenterManagerService;
	ServiceConnection dataCenterManagerIntentConn = new ServiceConnection() {  
        @Override  
        public void onServiceDisconnected(ComponentName name) {  
        	dataCenterManagerService = null;
        }  
          
        @Override  
        public void onServiceConnected(ComponentName name, IBinder service) {  
            //返回一个MsgService对象  
        	dataCenterManagerService = ((DataCenterManagerBiner)service).getService();   
        	
        	initCreateGroupChatActivity();
        }  
    };
    @Override
	public void onDestroy() {
		// add 取消绑定 ibind
		unbindService(dataCenterManagerIntentConn);
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * 连接数据中心管理服务
		 */
		final Intent dataCenterManagerIntent = new Intent(this, DataCenterManagerService.class);
		//startService(dataCenterManagerIntent);
		bindService(dataCenterManagerIntent, dataCenterManagerIntentConn, Context.BIND_ABOVE_CLIENT); 	
	}
	
	private void initCreateGroupChatActivity() {
		// 设置自定义ActionBar
		ActionBar bar = getActionBar();
		bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM|ActionBar.DISPLAY_HOME_AS_UP|ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_SHOW_TITLE); 
		LayoutParams customActionbarParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		ViewGroup customActionbar = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.group_chat_actionbar, null);
		mConfirmButton = (Button)customActionbar.findViewById(R.id.group_chat_create_confirm);
		bar.setCustomView(customActionbar, customActionbarParams);
		bar.setTitle("添加群组");
		
		setContentView(R.layout.activity_group_chat);
		mEditText = (EditText)findViewById(R.id.group_chat_room_name);
		
		//初始化
		mGroupManagementItems = new ArrayList<GroupManagementItem>();
		mSourceList=new ArrayList<GroupManagementItem>();
	    
		initAllGroupInfo();
		initConfirmCreateGroupChatRoom();
		//设置所有人为未选中
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
				//初始化CheckBox相关的点击事件
				ViewHolderGroupItem groupHolder = null;
				ViewHolderContactItem contactHolder = null;
				switch(mListViewAdapter.getItemViewType(position)){
				case MyListAdapter.type_1:
					break;
				case MyListAdapter.type_2:
					contactHolder = (ViewHolderContactItem)view.getTag();
					contactHolder.checkbox.toggle();                            // 在每次获取点击的item时改变checkbox的状态   
					GroupManagementItem item = (GroupManagementItem)mListViewAdapter.getItem(position);
					CYLog.i("GroupChatActivity","onItemClick"+item.getName());
					mSourceListItemSelected.put(item.getGmid(), contactHolder.checkbox.isChecked());
					mListViewAdapter.notifyDataSetChanged();
					break;
				}
                mConfirmButton.setText("确定"+"("+getSelectedContact().size()+")");  
                
                //初始化组展开相关点击事件
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
					Toast.makeText(getApplicationContext(), "资料填写不完整，请重新填写!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				//添加新的聊天室
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
	 * 分组名后面的checkbox监听器,点击后全选此分组下所有联系人
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
			mConfirmButton.setText("确定"+"("+getSelectedContact().size()+")");  
		}
		
	}
	
	/**
	 * 获取所有分组信息
	 */
	public void initAllGroupInfo() {
		Map<String, List<ContactsEntity>> classMap=dataCenterManagerService.getContactsEntityMap();
		updateSourcelist(classMap);
        filterVisibleList(mSourceList,mGroupManagementItems);
	}
	
	/**
	 * 过滤掉不显示的分组项
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
	 * 从mSourceList的选中状态列表中过滤得到当前显示的GroupManagementItem的选中状态列表
	 * 状态表元素添加顺序应该和mSourceList正序访问的顺序保持一致
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
	 * 返回所有被选中的联系人的,不包括分组的目录项
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
				CYLog.i("GroupChatActivity","正在邀请加入群聊室的人是:"+"id="+item.getGmid()+"    "+"name="+item.getName()+"    "+"groupId="+item.getGroupId());
			}
		}
		return list;
	}
	
	/**
	 * 更新分组列表
	 * @param roster
	 * @param classMap
	 * 所有GroupManagementItem的gmid设置为如下格式:
	 * (1)一般的Group:"group"+"_"+数字
	 * (2)包含聊天室的Group:"群聊"
	 * (3)联系人:"contact"+"_"+RosterEntry.getUser()
	 * (4)聊天室:"groupchat_room"+"_"+getJid()
	 * (5)未认证联系人:"unauthorized"+"_"+getUserId()
	 */
	public void updateSourcelist(Map<String, List<ContactsEntity>> classmap){
		if (dataCenterManagerService == null) {
			CYLog.e(TAG, "updateSourcelist dataCenterManagerService");
			return;
		}

		if (mSourceList != null) {
			mSourceList.clear();// 每次都重新装入
		} else {
			mSourceList = new ArrayList<GroupManagementItem>();
		}
		int item_gmid = 1; // 用来设置每个GroupManagementItem的主键,需要确保每一个都有独一无二的主键
		
		// 获取内存中的联系人列表
		Map<String, List<ContactsEntity>> classMap = dataCenterManagerService
				.getContactsEntityMap();
		if (classMap == null || classMap.size() == 0) {
			// 获取本地数据库中的联系人列表
			classMap = dataCenterManagerService.getLocalDbContactsEntityMap();
			if (classMap == null || classMap.size() == 0) {
				classMap = null;
			}
		}

		if (classMap != null) {
			for (Entry<String, List<ContactsEntity>> entry : classMap
					.entrySet()) {
				// 添加所有的组到显示页面
				GroupManagementItem groupItem = new GroupManagementItem();
				groupItem.setGmid("group" + "_" + item_gmid);
				groupItem.setType(GroupManagementItem.GROUP);
				groupItem.setName(entry.getKey());
				groupItem.setGroupId(groupItem.getGmid());
				if (mSourceList != null)
					mSourceList.add(groupItem);
				item_gmid++;

				// 添加这个组里面所有的认证的联系人到显示页面前面
				List<ContactsEntity> cmrs = entry.getValue();
				for (ContactsEntity cmr : cmrs) {
					GroupManagementItem contactItem = new GroupManagementItem();

					if (cmr.getAuthenticated().equals("1")) {// 认证
						contactItem.setGmid("contact" + "_"
								+ cmr.getAccountNum());
						contactItem.setAuthorized(1);
					} else {
						continue;
					}

					contactItem.setUserId(cmr.getBaseInfoId());
					contactItem.setType(GroupManagementItem.CONTACT);
					contactItem.setName(cmr.getName());
					// 获得图片地址
					contactItem.setIcon(cmr.getPicture());
					contactItem.setSignature(cmr.getSign());
					contactItem.setGroupId(groupItem.getGmid());
					contactItem.setVisible(false);
					mSourceList.add(contactItem);
				}

				// 添加这个组里面所有的未认证的联系人到显示页面后面
				for (ContactsEntity cmr : cmrs) {
					GroupManagementItem contactItem = new GroupManagementItem();

					// 既非1也非0 数据格式错误，打log按未认证处理
					if (cmr.getAuthenticated().equals("0")
							|| !cmr.getAuthenticated().equals("1")) {// 未认证
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
	 * 群组和聊天室对应的 ViewHolder
	 */
	public class ViewHolderGroupItem {    
		public ImageView group_icon = null;  //下拉图标
		public TextView group_name = null;   //组名
	    public CheckBox checkbox = null;     //CheckBox
	}

	/**
	 * 联系人对应的 ViewHolder
	 */
	public class ViewHolderContactItem {    
		public ImageView contact_icon = null;              //好友图标
		public TextView contact_nickname = null;           //好友名字
		public ImageView contact_authorized = null;        //好友认证图标
		public ImageView contact_flag_unregistered = null; //好友认证图标
		public TextView contact_signature = null;          //个性签名
	    public CheckBox checkbox = null;                   //CheckBox
	}
	/**
	 * 聊天室对应的 ViewHolder
	 */
	public class ViewHolderChatRoomItem {    
		public ImageView chatroom_icon = null;             //群聊室图标
		public TextView chatroom_name = null;              //群聊室名字
		public TextView chatroom_signature = null;          //群聊室签名
	    public CheckBox checkbox = null;                   //CheckBox
	}
	
	/**
	 * 自定义ListView适配器
	 */
	public class MyListAdapter extends BaseAdapter {    
        public  HashMap<Integer, Boolean> isSelected;    //用来存放当前显示的GroupManagementItem的选中状态
        private Context context = null;    
        private LayoutInflater inflater = null;    
        
        final static int type_default = -1;//默认样式
        final static int type_1 = 0;   //联系人分组显示样式
        final static int type_2 = 1;   //联系人显示样式
    
        public MyListAdapter(Context context) {    
            this.context = context;    
            inflater = LayoutInflater.from(context);    
            init();    
        }    
    
        // 初始化 设置所有checkbox都为未选择     
        public void init() {    
            isSelected = getVisibleSelectedList();
        }    
        
        //当分组数据更新后，刷新isSelected数组，其大小可能会变化
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
        
        // 每个convert view都会调用此方法，获得当前所需要的view样式  
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
