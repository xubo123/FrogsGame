package com.hust.schoolmatechat.ChatMsgservice;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import com.hust.schoolmatechat.engine.CYLog;

public class ChatMsgPacketListener implements PacketListener {
	private static final String TAG = "ChatMsgPacketListener";
	private ChatMsgService chatMsgService;

	public ChatMsgPacketListener(ChatMsgService chatMsgService) {
		this.chatMsgService = chatMsgService;
	}

	@Override
	public void processPacket(Packet packet) {
		try {
			Presence p = (Presence) packet;
			if (p.getType() == Presence.Type.subscribe) {
				CYLog.i(TAG,"PacketFilter accept " + p.getFrom() + " "
						+ p.getType());
				try {
					chatMsgService.sendFriendAddBroadcast(p.getFrom());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (p.getType() == Presence.Type.unsubscribed) {
				CYLog.i(TAG,"PacketFilter accept " + p.getFrom() + " "
						+ p.getType());
				try {
					chatMsgService.removeContactsEntity(p.getFrom());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}