package com.hust.schoolmatechat.ChatMsgservice;


public class GroupChatMessage {
	private String groupId; //Ⱥ��id
	private String fromId; //������id
	private String fromName; //����������
	private String message; //��Ϣ
	
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getFromId() {
		return fromId;
	}
	public void setFromId(String fromId) {
		this.fromId = fromId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void setFromName(String name) {
		this.fromName = name;
	}
	public String getFromName() {
		return fromName;
	}	
}
