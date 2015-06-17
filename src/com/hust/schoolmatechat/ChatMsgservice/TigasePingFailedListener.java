package com.hust.schoolmatechat.ChatMsgservice;

import org.jivesoftware.smackx.ping.PingFailedListener;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.greenrobot.event.EventBus;

public class TigasePingFailedListener implements PingFailedListener{

	private ChatMsgService chatMsgService;
	public TigasePingFailedListener(ChatMsgService chatMsgService) {
		this.chatMsgService = chatMsgService;
	}
	
	@Override
	public void pingFailed() {
		try {
			// ��½״̬Ϊ�Զ���½������������ӶϿ�����½״̬�ǵ�¼�ɹ������µ�¼���������
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(chatMsgService);
			String auto = prefs.getString("AUTO", "no");
			if (auto != null && auto.equals("auto")) {
				EventbusCMD mEventbusCMD = new EventbusCMD();
				mEventbusCMD.setCMD(EventbusCMD.LOGIN_TIGASE);
				EventBus.getDefault().post(mEventbusCMD);// �����ݶ���ĵ���ֱ����intָ����棬0Ϊ��½ָ��
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
