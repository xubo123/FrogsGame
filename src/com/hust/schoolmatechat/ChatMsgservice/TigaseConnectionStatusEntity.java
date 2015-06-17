package com.hust.schoolmatechat.ChatMsgservice;

import com.hust.schoolmatechat.engine.APPConstant;
public class TigaseConnectionStatusEntity {
	private int connStatus;
	
	//��ʾTigase����������״����ȷ��
	public final static int TIGASE_UNDEFINED = APPConstant.NETWORK_UN;   
	//Tigase��������������
	public final static int TIGASE_CONNECTED = APPConstant.NETWORK_OK;
	//TIgase�������ѶϿ�
	public final static int TIGASE_DISCONNECTED = APPConstant.NETWORK_NO;

	
	public TigaseConnectionStatusEntity(){
		connStatus = TIGASE_UNDEFINED;
	}

	public int getConnStatus() {
		return connStatus;
	}

	public void setConnStatus(int connStatus) {
		this.connStatus = connStatus;
	}
}
