package com.hust.schoolmatechat.entity;

import java.io.Serializable;

/*
 * ��ϵ��
 */
public class ContactsEntity{
	private String userAccount;//��½app���˵��˺�
	/** ���ڵ�½app����Ϊ���û���ӵ�е����з��������б����Ÿ���;������ϵ����˵Ϊ�����ڵķ�������*/
	private String className;
	private String accountNum;//��ϵ���˺�
	private String address;//��ַ
	private String authenticated;//'0'û����֤��'1'�Ѿ���֤
	private String baseInfoId;//��ϵ�˻�����Ϣid����Ϊ�Լ�����Ϣʱ��Ϊid�б����ŷֿ�
	private String channels;//�û�����������Ƶ��
	private String email;//����
//	private String id;//�ڷ����������ݿ��е�id
	private String intrestType;//��Ȥ����
	private String name;//����
	private String phoneNum;//�ֻ���
	private String picture;//ͼ���ַ
	private String sex;//�Ա�
	private String sign;//����ǩ��
	
	//���ڵ�½app���û�ʱ��0��web�ϻ�ȡ�༶ͬѧ��Ϣʧ�ܣ�1��ȡ�ɹ�
	//������ϵ����Ϣ����ʾ������Ϣ�Ƿ��web����������ȡ��������֤�����û��ſ�����ȡ����������Ϊ0
	private int hasAllClassmates;
	
	private String groupName;//���ڵ�½app���û�ʱ������������Ⱥ��Ϣ�����Ÿ���������������
	private String password;//���ڵ�½app���û�ʱ�������ֶζ�����������
	
	public ContactsEntity() {
		this.userAccount = "";
		this.className = "";
		this.accountNum = "";
		this.address = "";
		this.authenticated = "";
		this.baseInfoId = "";
		this.channels = "";
		this.email = "";
//		this.id = "";
		this.intrestType = "";
		this.name = "";
		this.phoneNum = "";
		this.picture = "";
		this.sex = "";
		this.sign = "";
		this.groupName = "";
		this.password = "";
	}
	public String getUserAccount() {
		return userAccount;
	}
	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	//�޸ĵ����� ����ע
	public String getAccountNum() {
		return accountNum;
	}
	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getAuthenticated() {
		return authenticated;
	}
	public void setAuthenticated(String authenticated) {
		this.authenticated = authenticated;
	}
	public String getBaseInfoId() {
		return baseInfoId;
	}
	public void setBaseInfoId(String baseInfoId) {
		this.baseInfoId = baseInfoId;
	}
	public String getChannels() {
		return channels;
	}
	public void setChannels(String channels) {
		this.channels = channels;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
//	public String getId() {
//		return id;
//	}
//	public void setId(String id) {
//		this.id = id;
//	}
	public String getIntrestType() {
		return intrestType;
	}
	public void setIntrestType(String intrestType) {
		this.intrestType = intrestType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getHasAllClassmates() {
		return hasAllClassmates;
	}
	public void setHasAllClassmates(int hasAllClassmates) {
		this.hasAllClassmates = hasAllClassmates;
	}
}
