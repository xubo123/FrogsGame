package com.hust.schoolmatechat.engine;

import java.io.Serializable;
import java.util.Date;

public class ChatItem implements Serializable {
	public  final static String SER_KEY = "com.hust.schoolmatechat.engine.ChatItem"; 
	/**
	 * 主界面新闻消息条目 owner为通道名称channelName
	 * 主界面群聊消息条目 owner为聊天室名称groupChatRoomName
	 * 主界面单聊消息条目 owner为对方的账号
	 * 主界面校友帮帮忙消息条目 owner为channelId
	 */
	private String owner;//item属主
	private String icon;//图片url,好友或新闻图片url
	private int type=3;//类别 可为NEWSITEM、GROUPCHATITEM、PRIVATECHATITEM
	private String name;//新闻通道名称，对方的姓名，校友帮帮忙时通道名称
	private String friendAccount;//对方的账号，单聊或者群聊时消息的另一方的账号
	private Date time;//消息时间
	private String latestMessage;//最新消息
	private int unread;//未读消息数
	private String userAccount;//始终为自己的账号,登陆app的人,需要入库
	
	public static final int ALLITEM=0;
	/** 主界面新闻消息条目 owner为通道名称channelName */
	public static final int NEWSITEM=1;
	/** 主界面群聊消息条目 owner为聊天室名称groupChatRoomName */
	public static final int GROUPCHATITEM=2;
	/** 主界面单聊消息条目 owner为对方的账号 */
	public static final int PRIVATECHATITEM=3;
	/** 主界面校友帮帮忙消息条目 owner为channelId */
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
