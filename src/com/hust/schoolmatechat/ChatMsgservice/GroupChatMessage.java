package com.hust.schoolmatechat.ChatMsgservice;


public class GroupChatMessage {
	private String groupId; //群组id
	private String fromId; //发言人id
	private String fromName; //发言人姓名
	private String message; //信息
	
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
