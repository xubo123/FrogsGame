package com.hust.schoolmatechat.ChatMsgservice;

public class GroupChatFuncStatusEntity {
	private int funcStatus;
	private GroupChatRoomEntity groupChatRoomEntity;
	
	//��ʾ��δ�õ����ؽ��
	public final static int GROUPCHAT_UNRET = -1;   
	//���������ң�ֻ����tigase�������ϴ����ڵ�
	public final static int GROUPCHAT_ADDROOM_SUCCESS = 0;
	public final static int GROUPCHAT_ADDROOM_FAIL = 1;
	//����������
	public final static int GROUPCHAT_JOINROOM_SUCCESS = 2;
	public final static int GROUPCHAT_JOINROOM_FAIL = 3;
	//�˳�������
	public final static int GROUPCHAT_UBSUBSSRIBE_SUCCESS = 4;
	public final static int GROUPCHAT_UBSUBSSRIBE_FAIL = 5;
	//ɾ��������
	public final static int GROUPCHAT_DEL_SUCCESS = 6;
	public final static int GROUPCHAT_DEL_FAIL = 7;
	
	//Ⱥ����Ϣ����ʧ�ܣ��ڵ����´����������г�Ա��ǿ�ƶ��ĵ�������Ϣ
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
