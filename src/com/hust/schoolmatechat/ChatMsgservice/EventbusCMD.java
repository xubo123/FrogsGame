package com.hust.schoolmatechat.ChatMsgservice;

public class EventbusCMD {
	/**
	 * 0 ��¼���������
	 * 1�������������������roster��ȥ
	 * 2�˳����������������������˳�
	 */
    private int cmd;
    
    public static final int DEFAULT_VAL = -1;
    /** 0 ��¼���������*/
    public static final int LOGIN_TIGASE = 0;
    /** 1�������������������roster��ȥ*/
    public static final int SEND_ROSTER = 1;
    /** 2�˳����������������������˳�*/
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
