package com.hust.schoolmatechat.postClass;

import com.hust.schoolmatechat.engine.APPBaseInfo;
import com.hust.schoolmatechat.engine.APPConstant;

public class SecretKey {
	//ͨ���ֻ��Ż�ȡ��֤��
	String phoneNum;
	String secretKey = APPBaseInfo.REGISTER_CODE_SECRET_KEY;

	public SecretKey(String phoneNum, String secretKey) {
		super();
		this.phoneNum = phoneNum;
		this.secretKey = secretKey;

	}

}
