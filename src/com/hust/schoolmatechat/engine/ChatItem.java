package com.hust.schoolmatechat.engine;

import java.io.Serializable;
import java.util.Date;

public class ChatItem implements Serializable {
	public  final static String SER_KEY = "com.hust.schoolmatechat.engine.ChatItem"; 
	/**
	 * ������������Ϣ��Ŀ ownerΪͨ������channelName
	 * ������Ⱥ����Ϣ��Ŀ ownerΪ����������groupChatRoomName
	 * �����浥����Ϣ��Ŀ ownerΪ�Է����˺�
	 * ������У�Ѱ��æ��Ϣ��Ŀ ownerΪchannelId
	 */
	private String owner;//item����
	private String icon;//ͼƬurl,���ѻ�����ͼƬurl
	private int type=3;//��� ��ΪNEWSITEM��GROUPCHATITEM��PRIVATECHATITEM
	private String name;//����ͨ�����ƣ��Է���������У�Ѱ��æʱͨ������
	private String friendAccount;//�Է����˺ţ����Ļ���Ⱥ��ʱ��Ϣ����һ�����˺�
	private Date time;//��Ϣʱ��
	private String latestMessage;//������Ϣ
	private int unread;//δ����Ϣ��
	private String userAccount;//ʼ��Ϊ�Լ����˺�,��½app����,��Ҫ���
	
	public static final int ALLITEM=0;
	/** ������������Ϣ��Ŀ ownerΪͨ������channelName */
	public static final int NEWSITEM=1;
	/** ������Ⱥ����Ϣ��Ŀ ownerΪ����������groupChatRoomName */
	public static final int GROUPCHATITEM=2;
	/** �����浥����Ϣ��Ŀ ownerΪ�Է����˺� */
	public static final int PRIVATECHATITEM=3;
	/** ������У�Ѱ��æ��Ϣ��Ŀ ownerΪchannelId */
	public static final int SCHOOLHELPERITEM=4;
	
	
	public ChatItem() {
		this.owner = "";
		this.icon = "";
		this.name = "";
		this.friendAccount = "";
		this.time = new Date();
		this.latestMessage = "";
		this.userAccount = "";
	}
	
	public ChatItem(String owner, String icon, int type, String name,
			String friendAccount, Date time, String latestMessage, int unread, String userAccount) {
		super();
		this.owner = owner;
		this.icon = icon;
		this.type = type;
		this.name = name;
		this.friendAccount = friendAccount;
		this.time = time;
		this.latestMessage = latestMessage;
		this.unread = unread;
		this.userAccount = userAccount;
	}
	public int getUnread() {
		return unread;
	}
	public void setUnread(int unread) {
		this.unread = unread;
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
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getLatestMessage() {
		return latestMessage;
	}
	public void setLatestMessage(String latestMessage) {
		this.latestMessage = latestMessage;
	}
	public String getFriendAccount() {
		return friendAccount;
	}
	public void setFriendAccount(String friendAccount) {
		this.friendAccount = friendAccount;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
}
