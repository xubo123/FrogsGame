package com.hust.schoolmatechat.ChatMsgservice;

public class GroupChatRoom {
	private String mName;         //�����ҵ�����
	private String mJid;           //������JID
	private int mOccupants;    //��������ռ��������
	private String mDescription;   //�����ҵ�����
	private String mSubject;       //�����ҵ�����
	
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
