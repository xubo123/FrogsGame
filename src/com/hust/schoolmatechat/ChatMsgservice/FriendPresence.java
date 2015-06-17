package com.hust.schoolmatechat.ChatMsgservice;

import org.jivesoftware.smack.packet.Presence.Type;

public class FriendPresence {

	private Type subscribed;
	private String friendAccount;
	public Type getSubscribed() {
		return subscribed;
	}
	public void setSubscribed(Type subscribed) {
		this.subscribed = subscribed;
	}
	public String getFriendAccount() {
		return friendAccount;
	}
	public void setFriendAccount(String friendAccount) {
		this.friendAccount = friendAccount;
	}
	
}
