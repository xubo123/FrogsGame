package com.hust.schoolmatechat.ChatMsgservice;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import com.hust.schoolmatechat.engine.CYLog;

public class ChatMsgPacketFilter implements PacketFilter {
	private static final String TAG = "ChatMsgPacketFilter";

	ChatMsgService chatMsgService;

	public ChatMsgPacketFilter(ChatMsgService chatMsgService) {
		this.chatMsgService = chatMsgService;
	}

	@Override
	public boolean accept(Packet arg0) {
		Presence p = (Presence) arg0;
		if (p.getType() == Presence.Type.subscribe) {
//			CYLog.i(TAG, "PacketFilter accept " + p.getFrom() + " "
//					+ p.getType());
			try {
				chatMsgService.sendFriendAddBroadcast(p.getFrom());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (p.getType() == Presence.Type.unsubscribed) {
//			CYLog.i(TAG, "PacketFilter accept " + p.getFrom() + " "
//					+ p.getType());
			try {
				chatMsgService.removeContactsEntity(p.getFrom());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}