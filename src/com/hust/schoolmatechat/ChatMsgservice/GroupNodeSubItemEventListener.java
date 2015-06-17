package com.hust.schoolmatechat.ChatMsgservice;

import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

import com.hust.schoolmatechat.engine.CYLog;

import de.greenrobot.event.EventBus;

public class GroupNodeSubItemEventListener implements ItemEventListener<Item> {
	private static final String TAG = "GroupNodeSubItemEventListener";

	@Override
	public void handlePublishedItems(ItemPublishEvent<Item> items) {
		CYLog.i(TAG, "Item count:" + items.getItems().size());
		CYLog.i(TAG, items.toString());
		Item temp = items.getItems().get(0);
		CYLog.i(TAG, "接收聊天室消息 name : " + temp.getElementName() + " id " + temp.getId());
		
		GroupChatMessage mGroupChatMessage = new GroupChatMessage();
		mGroupChatMessage.setGroupId(items.getNodeId());
		mGroupChatMessage.setMessage(items.toString());
		EventBus.getDefault().post(mGroupChatMessage);
	}

}
