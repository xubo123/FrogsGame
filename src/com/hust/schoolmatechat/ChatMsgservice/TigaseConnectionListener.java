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
		// 这里就是网络不正常或者被挤掉断线激发的事件  
		if (e.getMessage().contains("conflict")) { 
		      // 被挤掉线    
		      Toast.makeText(chatMsgService, "被挤下线",
						Toast.LENGTH_SHORT).show();
		      CYLog.e(TAG, chatMsgService.getUserAccount() + "被挤下线");
		      /** 
		         log.e("来自连接监听,conn非正常关闭");
		         log.e("非正常关闭异常:"+arg0.getMessage());  
		         log.e(con.isConnected());
		     */    
		     // 关闭连接，由于是被人挤下线，可能是用户自己，所以关闭连接，让用户重新登录是一个比较好的选择   
//		      chatMsgService.clearConnection();   
		     // 接下来你可以通过发送一个广播，提示用户被挤下线，重连很简单，就是重新登录   
		   } else if (e.getMessage().contains("Connection timed out")) {
		      // 连接超时    
		      // 不做任何操作，会实现自动重连    
		      CYLog.i(TAG, "连接超时"); 
		      CYLog.e(TAG, chatMsgService.getUserAccount() + "连接超时");
		   }  
		CYLog.e(TAG, "XMPPConnection connectionClosedOnError--->");
		
		chatMsgService.clearConnection(); 
		
		//系统自带的重连机制则需要重新发送登陆命令
		//SharedPreferences prefs = PreferenceManager
		//		.getDefaultSharedPreferences(chatMsgService);
		//String auto = prefs.getString("AUTO", "no");
		//if (auto != null && auto.equals("auto")) {
		//	EventbusCMD mEventbusCMD = new EventbusCMD();
		//	mEventbusCMD.setCMD(EventbusCMD.LOGIN_TIGASE);
		//	EventBus.getDefault().post(mEventbusCMD);// 不传递对象的调用直接用int指令代替，0为登陆指令
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
		// 从数据库获取发送失败的消息，若是当前用户则重新发送
		// dataCenterManagerService
		// 发送成功则删除消息
	}
}