package com.hust.schoolmatechat.ChatMsgservice;

import org.jivesoftware.smackx.ping.PingManager;

import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;

/*
 * ��������������ӣ�����¼��ά������
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
			// �����������ĵ������жϣ����Ͽ����߳�ҲҪ�˳�
			while (chatMsgService.isTigaseServiceContinue()) {
				CYLog.i(TAG,"chatMsgService.isTigaseServiceContinue()="+chatMsgService.isTigaseServiceContinue());
//				try {
//					if (pingManager == null) {
//						pingManager = PingManager.getInstanceFor(chatMsgService
//							.getConnection());
//					}
//
//				    //����ping��
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

				// �����������
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
					// �Ƿ���˰�������
					if (!chatMsgService
							.isHasAddConnectionPacketAndFilterListener()) {
						chatMsgService
								.setHasAddConnectionPacketAndFilterListener(chatMsgService
										.addConnectionPacketAndFilterListener(chatMsgService
												.getConnection()));
					}
					// �Ƿ���Լ������˰༶Ⱥ����
//					if (!chatMsgService.isHasAddToClassChatRoom()) {
//						chatMsgService.setHasAddToClassChatRoom(chatMsgService
//								.addSelfToClassChatRoom());
//					}
					// ��¼���������
					if (!chatMsgService.isHasLogined()) {
						if (!chatMsgService.TryLoginOnTigase(
								chatMsgService.getUserAccount(),
								chatMsgService.getPassword())) {
							// ��¼ʧ��,�˺�������������߷�����������
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
//			// �������������ibind�Ͽ�����
			chatMsgService.setTigaseServiceContinue(false);
			chatMsgService.clearLastConnection();
			// System.exit(0);// ������������
		} catch (Exception e) {
			chatMsgService.setHasLogined(false);
			e.printStackTrace();
			CYLog.i(TAG, "tigaseConnectionMaintenance system error");
			// ����ibind setCYLoginState(APPConstant.LOGIN_SYSTEM_ERROR);//?
			chatMsgService
					.setChatServerLoginState(APPConstant.LOGIN_SYSTEM_ERROR);
		}
	}
}