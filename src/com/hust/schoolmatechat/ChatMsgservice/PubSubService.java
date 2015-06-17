package com.hust.schoolmatechat.ChatMsgservice;

import java.util.List;

import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.Affiliation;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.NodeType;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.SubscribeForm;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

import com.hust.schoolmatechat.engine.CYLog;

public class PubSubService {
	public final static String TAG = "PubSubService";
		
	//根据节点名称得到节点对象
//	public static LeafNode getNode(PubSubManager manager,String nodeId){
//		try {
//			return manager.getNode(nodeId);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		} 
//	}
	
	//创建一个节点
	public static LeafNode createNode(PubSubManager manager,String nodeId){
		ConfigureForm form = new ConfigureForm(FormType.submit);
		form.setNodeType(NodeType.leaf);
		form.setAccessModel(AccessModel.open);
		form.setPublishModel(PublishModel.open);
		form.setPersistentItems(true);
		form.setNotifyRetract(true);
		form.setMaxItems(65535);
		LeafNode roomNode = null;
		
		try {
			roomNode = manager.createNode(nodeId);
			roomNode.sendConfigurationForm(form);
			CYLog.i(TAG, "createNode is called successfully!");
			return roomNode;
		} catch (Exception e) {
			CYLog.i(TAG, "createNode is in exception!");
			e.printStackTrace();
			return null;
		} 
	}
	
	//删除一个节点
	public static boolean deleteNode(PubSubManager manager,String nodeId){
		try {
			manager.deleteNode(nodeId);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
	}
	
	//获取根节点上所有联系人
	public static List<Affiliation> getAffiliationList(PubSubManager manager){
		try {
			return manager.getAffiliations();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	// 获取根节点上所有的订阅者
	public static List<Subscription> getSubscriptionList(PubSubManager manager) {
		try {
			return manager.getSubscriptions();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//订阅节点
//	public static boolean subscribe(PubSubManager manager,String nodeId,String account){
//		LeafNode roomNode = getNode(manager,nodeId);
//		if(roomNode == null)
//		{
//			CYLog.i(TAG, "subscribe-----roomNode is null!");
//			return false;
//		}
//		
//		try {
//			SubscribeForm subscriptionForm = new SubscribeForm(FormType.submit);
//			subscriptionForm.setDeliverOn(true);
//			subscriptionForm.setDigestFrequency(5000);
//			subscriptionForm.setDigestOn(true);
//			subscriptionForm.setIncludeBody(true);
//			roomNode.subscribe(account,subscriptionForm);
			//+++lqg+++ 目前的消息处理采用统一的监听器
//			roomNode.addItemEventListener(new ItemEventListener<Item>(){
//				@Override
//				public void handlePublishedItems(ItemPublishEvent<Item> items) {
//					CYLog.i(TAG, "---------->收到聊天室消息!");
//					CYLog.i(TAG, "Item count:" + items.getItems().size());
//					CYLog.i(TAG, items.toString());
//					
//					PayloadItem temp = (PayloadItem) items.getItems().get(0);
//					String rawMsg = temp.getPayload().toString();
//					String senderId = rawMsg.substring(0, rawMsg.indexOf("/"));
//					String restMsg = rawMsg.substring(rawMsg.indexOf("/"),rawMsg.length());
//					String senderName = restMsg.substring(0, restMsg.indexOf("/"));
//					String actualMsg = restMsg.substring(restMsg.indexOf("/"),restMsg.length());
//					
//					GroupChatMessage mGroupChatMessage = new GroupChatMessage();
//					mGroupChatMessage.setGroupId(items.getNodeId());
//					mGroupChatMessage.setFromId(senderName);
//					mGroupChatMessage.setMessage(actualMsg);
//					EventBus.getDefault().post(mGroupChatMessage);
//				}
//				
//			});
//			return true;
//		} catch (Exception e) {
//			CYLog.i(TAG, "subscribe is in exception!");
//			e.printStackTrace();
//			return false;
//		}
//	}
	
	//发布
//	public static boolean publish(PubSubManager manager,String nodeId,GroupChatMessage msg){
//		String sendMsg = msg.getFromId() + "/" + msg.getFromName()+ "/" + msg.getMessage();
//		SimplePayload payload = new SimplePayload("picture", "pubsub:test:picture", 
//				"<picture xmlns='pubsub:test:picture'><content>" + sendMsg + "</content></picture>");
//		SimplePayload payload = new SimplePayload("picture", "pubsub:test:picture", 
//				"<body>" + sendMsg + "</body>");
//		PayloadItem item = new PayloadItem(null, nodeId, payload);
		
//		LeafNode roomNode = getNode(manager,nodeId);
//		if(roomNode != null){
//			try {
//				roomNode.publish(item);
//				roomNode.send(new Item(sendMsg));
//				roomNode.publish(new Item(sendMsg));
//				return true;
//			} catch (Exception e) {
//				e.printStackTrace();
//				return false;
//			}
//		} else{
//			return false;
//		}
//	}
	
//	//得到节点上存储的n条item
//	public static List<? extends Item> getPersistedMessages(PubSubManager manager,String nodeId,int count){
//		LeafNode roomNode = getNode(manager,nodeId);
//		if(roomNode != null){
//			try {
//				return roomNode.getItems(count);
//			} catch (Exception e) {
//				e.printStackTrace();
//				return null;
//			} 
//		}
//		return null;
//	}
}

