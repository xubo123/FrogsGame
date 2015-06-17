package com.hust.schoolmatechat.engine;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.hust.schoolmatechat.pushedmsgservice.SingleNewsMessage;

public class ChatMessage{
	//++++lqg++++ �ֶκ���δ֪???? �޹����޸� 
	private String mid;//�����ֶΣ���ʱ����		
	private String icon;//ͼƬUrl
	private List<SingleNewsMessage> newsList;//����List
	
	private boolean sendSucc;//��Ϣ�Ƿ��ͳɹ������յ���Ϣ��Ч�������
	
	private String userAccount;//��ʱ��½app�û��˺�,У�Ѱ��æʱΪ"У�Ѱ��æ"
	private String senderAccount;//���Ļ�Ⱥ��ʱ������Ϣ����
	private String recvAccount;//���Ļ�Ⱥ��ʱ������Ϣ����
	
	private Date time;//��Ϣ������ʱ��
	private String messageContent;//��Ϣ����
	
	/** ��Ϣ����������ʱ��Ӧ�Է����˺ţ�Ⱥ��ʱ���������ƣ�����ʱ��Ӧͨ������channelId��channelName, У�Ѱ��æ*/
	private String owner;
	private int isRead;//�Ƿ��Ѷ�
	
	private int type=3;//����
	
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
		sendSucc = true;//Ĭ�Ϸ��ͳɹ�,ֻ�з���ʧ�ܲŻ�����
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
	//����
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
