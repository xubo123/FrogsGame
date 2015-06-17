package com.hust.schoolmatechat.engine;


public class GroupManagementItem {
	/*
	 * �û��ĸ�����Ϣ���ڴ˴����ݣ�����ϵ��mapת��ΪUI��ʾ��ʱ��
	 * ����ã���ϵ��map�Ľṹ�����ڲ��ң�ֱ�Ӳ�ѯ���ݿⲻ��Ч��
	 * ��Ҫ�Ļ���ȥ����
	 */
	private String gmid;//����,Ⱥ���ʱ��ΪȺ��id
	private String icon;//ͼƬurl
	private int type=0;//����
	private String name;//����
	private String signature;//����ǩ��
	private String groupId;//�����id
	private boolean visible=true;//�Ƿ�ɼ�
	private boolean extended=false;
	private int authorized=1;//0 unregistered;1 registered
	private String userId;
	
	public final static int GROUP=0;//����
	public final static int CONTACT=1;//�����������ϵ��
	public final static int MULTI_CHAT_GROUP = 2;//Ⱥ�ķ��飬����ŵ���Ⱥ����
	public final static int MULTI_CHAT_ROOM = 3; //Ⱥ����
	
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
