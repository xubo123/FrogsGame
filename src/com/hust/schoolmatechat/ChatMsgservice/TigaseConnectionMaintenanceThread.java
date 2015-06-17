package com.hust.schoolmatechat.ChatMsgservice;

import org.jivesoftware.smackx.ping.PingManager;

import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;

/*
 * 与聊天服务器连接，并登录，维护连接
 */
public class TigaseConnectionMaintenanceThread extends Thread {
	private static final String TAG = "TigaseConnectionMaintenanceThread";

	private ChatMsgService chatMsgService;

	TigaseConnectionMaintenanceThread(ChatMsgService chatMsgService) {
		this.chatMsgService = chatMsgService;
	}

	@Override
	public void run() {
		try {
			int count = 0;
			PingManager pingManager = null;
			// 加入数据中心的连接判断，若断开，线程也要退出
			while (chatMsgService.isTigaseServiceContinue()) {
				CYLog.i(TAG,"chatMsgService.isTigaseServiceContinue()="+chatMsgService.isTigaseServiceContinue());
//				try {
//					if (pingManager == null) {
//						pingManager = PingManager.getInstanceFor(chatMsgService
//							.getConnection());
//					}
//
//				    //发送ping包
//					if (pingManager == null || !pingManager.pingMyServer()) {
//						CYLog.d(TAG, "pingManager.pingMyServer failed");
//						chatMsgService.clearLastConnection();
//						if (!chatMsgService.TryConnectToTigase()) {
//							chatMsgService
//									.setChatServerLoginState(APPConstant.LOGIN_DISCONNECTED);
//							CYLog.e(TAG, "try connect to tigase failed");
//						}
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}

				// 检查网络连接
				if (chatMsgService.getConnection() == null
						|| !chatMsgService.getConnection().isConnected()) {
					CYLog.i(TAG,"chatMsgService.getConnection()="+chatMsgService.getConnection());
					chatMsgService.clearLastConnection();
					if (!chatMsgService.TryConnectToTigase()) {
						chatMsgService
								.setChatServerLoginState(APPConstant.LOGIN_DISCONNECTED);
						CYLog.e(TAG, "try connect to tigase failed");
					}
				} else {
					// 是否加了包过滤器
					if (!chatMsgService
							.isHasAddConnectionPacketAndFilterListener()) {
						chatMsgService
								.setHasAddConnectionPacketAndFilterListener(chatMsgService
										.addConnectionPacketAndFilterListener(chatMsgService
												.getConnection()));
					}
					// 是否把自己加入了班级群聊室
//					if (!chatMsgService.isHasAddToClassChatRoom()) {
//						chatMsgService.setHasAddToClassChatRoom(chatMsgService
//								.addSelfToClassChatRoom());
//					}
					// 登录聊天服务器
					if (!chatMsgService.isHasLogined()) {
						if (!chatMsgService.TryLoginOnTigase(
								chatMsgService.getUserAccount(),
								chatMsgService.getPassword())) {
							// 登录失败,账号密码有问题或者服务器有问题
							chatMsgService
									.setChatServerLoginState(APPConstant.LOGIN_FAILED);
							CYLog.d(TAG, "try login on tigase failed");
							// clearLastConnection();
							// return;
						} else {
							chatMsgService.setHasLogined(true);
							chatMsgService
									.setChatServerLoginState(APPConstant.LOGIN_TIGASE_SUCC);
							CYLog.d(TAG, "login on tigase successful");
						}
					}
					++count;
					if (count % 3 == 0 && !chatMsgService.sendOnlinePresence(chatMsgService
							.getUserSelfContactsEntity().getSign())) {
						chatMsgService.clearLastConnection();
					}
				}
//				CYLog.i(TAG, "connect to tigase normal");
				sleep(APPConstant.CONNECTION_CHECK_INTERVAL);
			}
//			boolean r1 = chatMsgService.isTigaseServiceContinue();
//			boolean r2 = chatMsgService.getDataCenterManagerService() == null;
//			CYLog.i(TAG, "TigaseConnectionMaintenanceThread quit------------------" +
//			        "chatMsgService.isTigaseServiceContinue() = " +
//					 r1 +
//					" chatMsgService.getDataCenterManagerService() == null : " +
//					r2);
//			// 如果是数据中心ibind断开导致
			chatMsgService.setTigaseServiceContinue(false);
			chatMsgService.clearLastConnection();
			// System.exit(0);// 结束整个进程
		} catch (Exception e) {
			chatMsgService.setHasLogined(false);
			e.printStackTrace();
			CYLog.i(TAG, "tigaseConnectionMaintenance system error");
			// 调用ibind setCYLoginState(APPConstant.LOGIN_SYSTEM_ERROR);//?
			chatMsgService
					.setChatServerLoginState(APPConstant.LOGIN_SYSTEM_ERROR);
		}
	}
}