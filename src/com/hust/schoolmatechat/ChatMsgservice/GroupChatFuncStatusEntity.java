package com.hust.schoolmatechat.ChatMsgservice;

public class GroupChatFuncStatusEntity {
	private int funcStatus;
	private GroupChatRoomEntity groupChatRoomEntity;
	
	//表示还未得到返回结果
	public final static int GROUPCHAT_UNRET = -1;   
	//创建聊天室，只是在tigase服务器上创建节点
	public final static int GROUPCHAT_ADDROOM_SUCCESS = 0;
	public final static int GROUPCHAT_ADDROOM_FAIL = 1;
	//加入聊天室
	public final static int GROUPCHAT_JOINROOM_SUCCESS = 2;
	public final static int GROUPCHAT_JOINROOM_FAIL = 3;
	//退出聊天室
	public final static int GROUPCHAT_UBSUBSSRIBE_SUCCESS = 4;
	public final static int GROUPCHAT_UBSUBSSRIBE_FAIL = 5;
	//删除聊天室
	public final static int GROUPCHAT_DEL_SUCCESS = 6;
	public final static int GROUPCHAT_DEL_FAIL = 7;
	
	//群聊消息发送失败，节点重新创建，给所有成员发强制订阅的命令消息
	public final static int GROUPCHAT_FORCE_SUBSCRIBE = 8;
	
	public GroupChatFuncStatusEntity(){
		funcStatus = GROUPCHAT_UNRET;
	}

	public int getFuncStatus() {
		return funcStatus;
	}

	public void setFuncStatus(int funcStatus) {
		this.funcStatus = funcStatus;
	}

	public GroupChatRoomEntity getGroupChatRoomEntity() {
		return groupChatRoomEntity;
	}

	public void setGroupChatRoomEntity(GroupChatRoomEntity groupChatRoomEntity) {
		this.groupChatRoomEntity = groupChatRoomEntity;
	}
}
