package com.hust.schoolmatechat.ChatMsgservice;

import java.util.Collection;

import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import com.hust.schoolmatechat.engine.CYLog;

class TigaseRosterListener implements RosterListener {// roster¼àÌýÆ÷
	private final static String TAG = "TigaseRosterListener";

	private ChatMsgService chatMsgService;

	TigaseRosterListener(ChatMsgService chatMsgService) {
		this.chatMsgService = chatMsgService;
	}

	@Override
	public void presenceChanged(Presence arg0) {
		// CYLog.i(TAG, arg0.getFrom() + " presenceChanged");
	}

	@Override
	public void entriesUpdated(Collection<String> arg0) {
		// CYLog.i(TAG, "rosterListener entriesUpdated");
	}

	@Override
	public void entriesDeleted(Collection<String> arg0) {
		// CYLog.i(TAG, "rosterListener entriesDeleted");
	}

	@Override
	public void entriesAdded(Collection<String> arg0) {
		try {
			chatMsgService.getRoster().reload();
			chatMsgService.setRoster(chatMsgService.getRoster());
			// CYLog.i(TAG, "rosterListener entriesAdded");
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
		}
	}
}