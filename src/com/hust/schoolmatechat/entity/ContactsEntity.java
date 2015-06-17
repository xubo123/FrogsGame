package com.hust.schoolmatechat.entity;

import java.io.Serializable;

/*
 * 联系人
 */
public class ContactsEntity{
	private String userAccount;//登陆app的人的账号
	/** 对于登陆app的人为该用户所拥有的所有分组名称列表，逗号隔开;对于联系人来说为其所在的分组名称*/
	private String className;
	private String accountNum;//联系人账号
	private String address;//地址
	private String authenticated;//'0'没有认证，'1'已经认证
	private String baseInfoId;//联系人基础信息id，当为自己的信息时，为id列表，逗号分开
	private String channels;//用户收听的新闻频道
	private String email;//邮箱
//	private String id;//在服务器上数据库中的id
	private String intrestType;//兴趣类型
	private String name;//姓名
	private String phoneNum;//手机号
	private String picture;//图像地址
	private String sex;//性别
	private String sign;//个性签名
	
	//对于登陆app的用户时，0从web上获取班级同学信息失败，1获取成功
	//对于联系人信息，表示个人信息是否从web服务器上拉取完整，认证过的用户才可能拉取完整，否则为0
	private int hasAllClassmates;
	
	private String groupName;//对于登陆app的用户时，其所在聊天群信息，逗号隔开对入库出库有用
	private String password;//对于登陆app的用户时，密码字段对入库出库有用
	
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
	//修改调用者 完后解注
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
