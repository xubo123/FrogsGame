package com.hust.schoolmatechat.ChatMsgservice;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.hust.schoolmatechat.LoginActivity;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;

import de.greenrobot.event.EventBus;

class TigaseConnectionListener implements ConnectionListener {
	private static final String TAG = "TigaseConnectionListener";
	
	private ChatMsgService chatMsgService;

	TigaseConnectionListener(ChatMsgService chatMsgService) {
		this.chatMsgService = chatMsgService;
	}
	
	@Override
	public void authenticated(XMPPConnection conn) {
		//CYLog.i(TAG, "XMPPConnection authenticated");
	}

	@Override
	public void connected(XMPPConnection conn) {
		//CYLog.i(TAG, "XMPPConnection connected");
	}

	@Override
	public void connectionClosed() {
		CYLog.i(TAG, "XMPPConnection connectionClosed --->");
	}

	@Override
	public void connectionClosedOnError(Exception e) {
		CYLog.e(TAG, e.getMessage());   
		// ����������粻�������߱��������߼������¼�  
		if (e.getMessage().contains("conflict")) { 
		      // ��������    
		      Toast.makeText(chatMsgService, "��������",
						Toast.LENGTH_SHORT).show();
		      CYLog.e(TAG, chatMsgService.getUserAccount() + "��������");
		      /** 
		         log.e("�������Ӽ���,conn�������ر�");
		         log.e("�������ر��쳣:"+arg0.getMessage());  
		         log.e(con.isConnected());
		     */    
		     // �ر����ӣ������Ǳ��˼����ߣ��������û��Լ������Թر����ӣ����û����µ�¼��һ���ȽϺõ�ѡ��   
//		      chatMsgService.clearConnection();   
		     // �����������ͨ������һ���㲥����ʾ�û��������ߣ������ܼ򵥣��������µ�¼   
		   } else if (e.getMessage().contains("Connection timed out")) {
		      // ���ӳ�ʱ    
		      // �����κβ�������ʵ���Զ�����    
		      CYLog.i(TAG, "���ӳ�ʱ"); 
		      CYLog.e(TAG, chatMsgService.getUserAccount() + "���ӳ�ʱ");
		   }  
		CYLog.e(TAG, "XMPPConnection connectionClosedOnError--->");
		
		chatMsgService.clearConnection(); 
		
		//ϵͳ�Դ���������������Ҫ���·��͵�½����
		//SharedPreferences prefs = PreferenceManager
		//		.getDefaultSharedPreferences(chatMsgService);
		//String auto = prefs.getString("AUTO", "no");
		//if (auto != null && auto.equals("auto")) {
		//	EventbusCMD mEventbusCMD = new EventbusCMD();
		//	mEventbusCMD.setCMD(EventbusCMD.LOGIN_TIGASE);
		//	EventBus.getDefault().post(mEventbusCMD);// �����ݶ���ĵ���ֱ����intָ����棬0Ϊ��½ָ��
		//}
	}

	@Override
	public void reconnectingIn(int seconds) {
		CYLog.i(TAG, "XMPPConnection reconnectingIn after " + seconds + "s");
	}

	@Override
	public void reconnectionFailed(Exception e) {
		CYLog.e(TAG, e.toString());
		CYLog.e(TAG, "XMPPConnection reconnectionFailed");
	}

	@Override
	public void reconnectionSuccessful() {
		CYLog.i(TAG, "XMPPConnection reconnectionSuccessful");
		// �����ݿ��ȡ����ʧ�ܵ���Ϣ�����ǵ�ǰ�û������·���
		// dataCenterManagerService
		// ���ͳɹ���ɾ����Ϣ
	}
}