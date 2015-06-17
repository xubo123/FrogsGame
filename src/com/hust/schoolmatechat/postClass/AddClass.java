package com.hust.schoolmatechat.postClass;

public class AddClass {
String[] baseInfoId;
String accountNum;
String password;
String[] classmates;
String phoneNum;
String name;
public AddClass(String[] baseInfoId, String accountNum, String password,
		String[] classmates, String phoneNum, String name) {
	super();
	this.baseInfoId = baseInfoId;
	this.accountNum = accountNum;
	this.password = password;
	this.classmates = classmates;
	this.phoneNum = phoneNum;
	this.name = name;
}
}
