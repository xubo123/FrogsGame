package com.hust.schoolmatechat.ChatMsgservice;

import com.hust.schoolmatechat.engine.APPConstant;
public class TigaseConnectionStatusEntity {
	private int connStatus;
	
	//表示Tigase服务器连接状况不确定
	public final static int TIGASE_UNDEFINED = APPConstant.NETWORK_UN;   
	//Tigase服务器已连接上
	public final static int TIGASE_CONNECTED = APPConstant.NETWORK_OK;
	//TIgase服务器已断开
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
