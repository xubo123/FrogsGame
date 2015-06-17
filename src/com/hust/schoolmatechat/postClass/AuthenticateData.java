package com.hust.schoolmatechat.postClass;

import org.json.JSONArray;

public class AuthenticateData {
String password;
String name;
String phoneNum;
String[] classmates;
String[] baseInfoId;
public AuthenticateData(String password, String name, String phoneNum,
		String[] classmates, String[] baseInfoId) {
	super();
	this.password = password;
	this.name = name;
	this.phoneNum = phoneNum;
	this.classmates = classmates;
	this.baseInfoId = baseInfoId;
}

}
