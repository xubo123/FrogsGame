package com.hust.schoolmatechat.ChatMsgservice;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.Packet;

import android.os.Message;

/**
 * 群聊室监听器
 */
public class MultiChatMessageListener implements PacketListener {
	private static final String TAG = "MultiChatMessageListener";
	private String groupChatRoomName;
	private List<GroupChatMessage> groupChatMessageList;
	private boolean sendStart;

	MultiChatMessageListener(String groupChatRoomName) {
		this.groupChatRoomName = groupChatRoomName;
		groupChatMessageList = new ArrayList<GroupChatMessage>();
		sendStart = false;//初始化为不发送
	}

	@Override
	public void processPacket(Packet packet)  throws NotConnectedException {
//		Message message = (Message) packet;
////		String from = StringUtils.parseName(message.getFrom());
////		String to = StringUtils.parseName(message.getTo());
//		GroupChatMessage mGroupChatMessage = new GroupChatMessage();
//		mGroupChatMessage.setGroupChatRoomName(groupChatRoomName);
//		mGroupChatMessage.setMessage(message.getBody());
//		
//		if (sendStart) {
//			EventBus.getDefault().post(mGroupChatMessage);
//		} else {
//			if (groupChatMessageList == null) {
//				groupChatMessageList = new ArrayList<GroupChatMessage>();
//			}
//			groupChatMessageList.add(mGroupChatMessage);
//		}
	}

	public List<GroupChatMessage> getGroupChatMessageList() {
		return groupChatMessageList;
	}

	public void setGroupChatMessageList(List<GroupChatMessage> groupChatMessageList) {
		this.groupChatMessageList = groupChatMessageList;
	}

	public void setSendStart(boolean sendStart) {
		this.sendStart = sendStart;
	}
}