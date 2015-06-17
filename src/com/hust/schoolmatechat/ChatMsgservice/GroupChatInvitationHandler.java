package com.hust.schoolmatechat.ChatMsgservice;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.WindowManager;

import com.hust.schoolmatechat.ContactsFragment;
import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;

public class GroupChatInvitationHandler extends Handler {
	private static final String TAG = "GroupChatInvitationHandler";
	private DataCenterManagerService dataCenterManagerService;
	private ContactsFragment contactsObserver;

	public GroupChatInvitationHandler(
			DataCenterManagerService dataCenterManagerService) {
		this.dataCenterManagerService = dataCenterManagerService;
	}

	@Override
	public void handleMessage(android.os.Message msg) {
		switch (msg.what) {
		case 0:
			String[] inviter_room = (String[]) msg.obj;
			final String inviter = inviter_room[0];
			final String room = inviter_room[1];
			Dialog dialog = new AlertDialog.Builder(dataCenterManagerService)
					.setTitle("Ⱥ��������")
					.setMessage(inviter + "���������������" + room + ",���Ƿ����?")
					.setPositiveButton("����",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// ����������Ϣ���뱾��GroupMember_Table��,������UI����
//									dataCenterManagerService
//											.pushJoinGroupChatRoom(room,
//													inviter);
								}
							})
					.setNegativeButton("�ܾ�",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									try {
//										dataCenterManagerService
//												.declineJoinChatRoom(room,
//														inviter, null);

									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}).create();
			dialog.getWindow().setType(
					WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			dialog.show();
			break;
		default:
			break;
		}
		super.handleMessage(msg);
	}
}