package com.hust.schoolmatechat.ChatMsgservice;

public class GroupChatRoom {
	private String mName;         //聊天室的名称
	private String mJid;           //聊天室JID
	private int mOccupants;    //聊天室中占有者数量
	private String mDescription;   //聊天室的描述
	private String mSubject;       //聊天室的主题
	
	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getJid() {
		return mJid;
	}

	public void setJid(String jid) {
		this.mJid = jid;
	}

	public int getOccupants() {
		return mOccupants;
	}

	public void setOccupants(int i) {
		this.mOccupants = i;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public String getSubject() {
		return mSubject;
	}

	public void setSubject(String subject) {
		mSubject = subject;
	}
}
