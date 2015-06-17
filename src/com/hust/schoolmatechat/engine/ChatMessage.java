package com.hust.schoolmatechat.engine;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.hust.schoolmatechat.pushedmsgservice.SingleNewsMessage;

public class ChatMessage{
	//++++lqg++++ 字段含义未知???? 罗广镇修改 
	private String mid;//冗余字段，暂时保留		
	private String icon;//图片Url
	private List<SingleNewsMessage> newsList;//新闻List
	
	private boolean sendSucc;//消息是否发送成功，接收的消息无效，不入库
	
	private String userAccount;//此时登陆app用户账号,校友帮帮忙时为"校友帮帮忙"
	private String senderAccount;//单聊或群聊时发送消息的人
	private String recvAccount;//单聊或群聊时接收消息的人
	
	private Date time;//消息产生的时间
	private String messageContent;//消息内容
	
	/** 消息属主，单聊时对应对方的账号，群聊时聊天室名称，新闻时对应通道名称channelId和channelName, 校友帮帮忙*/
	private String owner;
	private int isRead;//是否已读
	
	private int type=3;//类型
	
	public static final int NONNEWSMESSAGE=0;
	public static final int NEWSMESSAGE=1;
	public static final int GROUPCHATMESSAGE=2;
	public static final int PRIVATECHATMESSAGE=3;
	public static final int SCHOOLHELPER=6;
	
	
	
	public ChatMessage() {
		this.mid = "";
		this.icon = "";
		this.userAccount = "";
		this.senderAccount = "";
		this.recvAccount = "";
		this.time = new Date();
		this.messageContent = "";
		this.owner = "";
		sendSucc = true;//默认发送成功,只有发送失败才会重置
	}
	public boolean isSendSucc() {
		return sendSucc;
	}
	public void setSendSucc(boolean sendSucc) {
		this.sendSucc = sendSucc;
	}

	public int getIsRead() {
		return isRead;
	}
	public void setIsRead(int isRead) {
		this.isRead = isRead;
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
	public String getMessageContent() {
		return messageContent;
	}
	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}
	public List<com.hust.schoolmatechat.pushedmsgservice.SingleNewsMessage> getNewsList() {
		return newsList;
	}
	public void setNewsList(List<com.hust.schoolmatechat.pushedmsgservice.SingleNewsMessage> newsList) {
		this.newsList = newsList;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public String getSenderAccount() {
		return senderAccount;
	}

	public void setSenderAccount(String senderAccount) {
		this.senderAccount = senderAccount;
	}

	public String getRecvAccount() {
		return recvAccount;
	}

	public void setRecvAccount(String recvAccount) {
		this.recvAccount = recvAccount;
	}
	//更改
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
}
