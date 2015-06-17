package com.hust.schoolmatechat.ChatMsgservice;

public class EventbusCMD {
	/**
	 * 0 登录聊天服务器
	 * 1发送聊天服务器花名册roster出去
	 * 2退出聊天服务器，但聊天服务不退出
	 */
    private int cmd;
    
    public static final int DEFAULT_VAL = -1;
    /** 0 登录聊天服务器*/
    public static final int LOGIN_TIGASE = 0;
    /** 1发送聊天服务器花名册roster出去*/
    public static final int SEND_ROSTER = 1;
    /** 2退出聊天服务器，但聊天服务不退出*/
    public static final int QUIT_TIGASE = 2;
	
    public EventbusCMD() {
    	this.cmd = DEFAULT_VAL;
    }
    
	public int getCMD() {
		return cmd;
	}
	
	public void setCMD(int cmd) {
		this.cmd = cmd;
	}
}
