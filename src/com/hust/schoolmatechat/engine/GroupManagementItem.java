package com.hust.schoolmatechat.engine;


public class GroupManagementItem {
	/*
	 * 用户的个人信息，在此处备份，在联系人map转换为UI显示的时候
	 * 处理好，联系人map的结构不便于查找，直接查询数据库不高效，
	 * 必要的话再去调用
	 */
	private String gmid;//主键,群组的时候为群组id
	private String icon;//图片url
	private int type=0;//类型
	private String name;//名称
	private String signature;//个性签名
	private String groupId;//分组的id
	private boolean visible=true;//是否可见
	private boolean extended=false;
	private int authorized=1;//0 unregistered;1 registered
	private String userId;
	
	public final static int GROUP=0;//分组
	public final static int CONTACT=1;//分组里面的联系人
	public final static int MULTI_CHAT_GROUP = 2;//群聊分组，里面放的是群聊室
	public final static int MULTI_CHAT_ROOM = 3; //群聊室
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getAuthorized() {
		return authorized;
	}
	public void setAuthorized(int authorized) {
		this.authorized = authorized;
	}
	public boolean isExtended() {
		return extended;
	}
	public void setExtended(boolean extended) {
		this.extended = extended;
	}
	public String getGmid() {
		return gmid;
	}
	public void setGmid(String gmid) {
		this.gmid = gmid;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	
	
}
