package com.hust.schoolmatechat.entity;

import java.io.Serializable;

public class GroupInfoEntity {
	/*
	 * groupName Ⱥ����
	 * ownerAccounts Ⱥ����Ա�˺��б����Ÿ���
	 * memberAccounts ��Ա�˺��б�,���Ÿ���
	 */
    private String groupName;
    private String ownerAccounts;
    private String memberAccounts;
   
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getOwnerAccounts() {
		return ownerAccounts;
	}
	public void setOwnerAccounts(String ownerAccounts) {
		this.ownerAccounts = ownerAccounts;
	}
	public String getMemberAccounts() {
		return memberAccounts;
	}
	public void setMemberAccounts(String memberAccounts) {
		this.memberAccounts = memberAccounts;
	}
	
}
