package com.hust.schoolmatechat.ChatMsgservice;

import java.io.Serializable;

public class ChatMessageSendEntity {
	private static final long serialVersionUID = 1L;
	private String receiver;
	private String message;
	private String messageType;
	
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
}
