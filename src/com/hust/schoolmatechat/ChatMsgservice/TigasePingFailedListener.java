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
			// 登陆状态为自动登陆，聊天服务连接断开，登陆状态非登录成功则重新登录聊天服务器
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(chatMsgService);
			String auto = prefs.getString("AUTO", "no");
			if (auto != null && auto.equals("auto")) {
				EventbusCMD mEventbusCMD = new EventbusCMD();
				mEventbusCMD.setCMD(EventbusCMD.LOGIN_TIGASE);
				EventBus.getDefault().post(mEventbusCMD);// 不传递对象的调用直接用int指令代替，0为登陆指令
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
