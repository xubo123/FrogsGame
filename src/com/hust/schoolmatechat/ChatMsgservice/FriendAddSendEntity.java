package com.hust.schoolmatechat.ChatMsgservice;

public class FriendAddSendEntity {
	private String name;
	private String accountNum;//�������㽫�˺��滻Ϊ�û����˺�
	private String className;
	private String cmdType;//������������
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getAccountNum() {
		return accountNum;
	}
	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
	}
	public String getCmdType() {
		return cmdType;
	}
	public void setCmdType(String cmdType) {
		this.cmdType = cmdType;
	}
	
	
}
