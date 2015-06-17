package com.hust.schoolmatechat.postClass;

public class RegisterData {
    // 注册的数据整理
	String name;
	String phoneNum;
	String checkCode;
	String password;

	public RegisterData(String name, String phoneNum, String checkCode,
			String password) {
		super();
		this.name = name;
		this.phoneNum = phoneNum;
		this.checkCode = checkCode;
		this.password = password;
	}

}
