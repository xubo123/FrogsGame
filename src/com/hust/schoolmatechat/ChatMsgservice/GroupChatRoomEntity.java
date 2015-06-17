package com.hust.schoolmatechat.ChatMsgservice;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GroupChatRoomEntity {
	private String groupName; // �����ҵ�����
	private String groupId; // ������mGroupId,ȫ��Ψһ
	private String createrAccount; // �����Ҵ�����,ʵ�����������������û��������ߣ���Ⱥ��
								   // ;����Ա;��ͨ�û�
	private Map<String, Integer> occupantsMap;  // �����ҳ�Ա��ֵΪ1��Ϊ����Ա��0�������ͨ��Ա��Web��������
												// ownerAccount�������Ա��memberAccount�������г�Ա
												//createAccountҲ�����ڹ���Ա��
	private String description; // �����ҵ�����
	private String subject; // �����ҵ�����
	
	//������ֶ�������tigase�Ͻ���Ⱥ�Ĳ���
	private Map<String, Integer> targetOccupantsMap; //Ⱥ�Ĳ��������Ķ���
    private int functionType;           //�����Ҳ������ͣ�����ӡ�������롢ɾ��
	public final static int FUNCTYPE_ADD = 0;          //���������ң�����mName,mOccupants(Ҫ�������)
	public final static int FUNCTYPE_JOIN = 1;         //���������� ,����mName,mOccupants(Ҫ�������)
	public final static int FUNCTYPE_DEL = 2;          //ɾ�������ң�����mName
//	public final static int FUNCTION_KICK = 3;         //�������������ˣ�����mName,mOccupants(Ҫ�ߵ���)
	public final static int FUNCTION_CONSULT = 4;      //��ѯ��������ϸ��Ϣ������mName
	public final static int FUNCTION_UBSUBSSRIBE = 5;      //��ѯ��������ϸ��Ϣ������mName
	
//	type�ֶ�˵��
//	0 ����Ⱥ�� 
//	1 ɾ��Ⱥ��
//	2 �����ͨ��Ա membersAccount��Ҫ��ӵ���ͨ��Ա
//	3 ɾ����ͨ��Ա membersAccount��ΪҪɾ������ͨ��Ա
//	4 ��ӹ���Ա  adminsAccount��ΪҪ��ӵĹ���Ա
//	5 ɾ������Ա adminsAccount��Ҫɾ���Ĺ���Ա
//	6 ��ͨ��Ա����Ϊ����Ա  membersAccount��ΪҪ��������ͨ��Ա
//	7 ����Ա����Ϊ��ͨ��Ա  adminsAccount��ΪҪҪ�����Ĺ���Ա
//	8 �޸�Ⱥ������Ϣ��Ⱥ��������������
	//������ֶ�������web�������Ͻ���Ⱥ�Ĳ���
	private int syncType;
	public final static int SYNCTYPE_ADD_ROOM = 0;
	public final static int SYNCTYPE_DEL_ROOM = 1;
	public final static int SYNCTYPE_ADD_NORMAL_MEMBER = 2;
	public final static int SYNCTYPE_DEL_NORMAL_MEMBER = 3;
	public final static int SYNCTYPE_ADD_ADMIN = 4;
	public final static int SYNCTYPE_DEL_ADMIN = 5;
	public final static int SYNCTYPE_ELEVATE_PERMISSION = 6;  //����ͨ��Ա����Ϊ����Ա
	public final static int SYNCTYPE_DEGRADE_PERMISSION = 7;  //������Ա����Ϊ��ͨ��Ա
	public final static int SYNCTYPE_UPDATE_INFO = 8;  //����Ⱥ�������������� 

	// ��������ͬ����webʧ��ʱ����ͬ��
	private String userAccount; // ��½�û�
	private String password; // ��½�û�������

	public GroupChatRoomEntity() {
		groupName = "";
		groupId = "";
		createrAccount = "";
		occupantsMap = null;
		description = "";
		subject = "";
	}
	
	public GroupChatRoomEntity(GroupChatRoomEntity room){
		groupName = room.getGroupName();
		groupId = room.getGroupId();
		createrAccount = room.getCreaterAccount();
		occupantsMap = new HashMap<String,Integer>();
		occupantsMap.putAll(room.getOccupantsMap());
		description = room.getDescription();
		subject = room.getSubject();
		userAccount = room.getUserAccount();
		password = room.getPassword();
	}

	// ������Ա�˺����Ϊ�ַ�������
	public String getAdministratersStr() {
		try {
			if (occupantsMap.size() == 0) {
				return "";
			}

			StringBuffer administratersBuf = new StringBuffer(); // ����Ա
			for (String key : occupantsMap.keySet()) {
				if (occupantsMap.get(key) == 1) {
					administratersBuf.append(",").append(key);
				}
			}
			String administraters = administratersBuf.toString();
			if (administraters == null || administraters.equals("")) {
				return "";
			}
			return administraters.substring(1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// ����ͨ�û��˺����Ϊ�ַ�������
	public String getNormalMembersStr() {
		try {
			if (occupantsMap.size() == 0) {
				return "";
			}

			StringBuffer membersBuf = new StringBuffer(); // ����Ա
			for (String key : occupantsMap.keySet()) {
				if (occupantsMap.get(key) == 0) {
					membersBuf.append(",").append(key);
				}
			}
			String members = membersBuf.toString();
			if (members == null || members.equals("")) {
				return "";
			}
			return members.substring(1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ȡ���г�Ա
	 * @return
	 */
	public String getAllMembersStr() {
		try {
			if (occupantsMap.size() == 0) {
				return "";
			}

			StringBuffer membersBuf = new StringBuffer();
			for (String key : occupantsMap.keySet()) {
				membersBuf.append(",").append(key);
			}
			String members = membersBuf.substring(1).toString();
			return members;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// �������˺����Ϊ����Ա
	public void addAdministrater(String userId) {
		occupantsMap.put(userId, 1);
	}

	// �������˺����Ϊ��ͨ��Ա
	public void addNormalMember(String userId) {
		occupantsMap.put(userId, 0);
	}

	// ɾ�������ҳ�Ա�������ߵ�Ȩ�ޱ�����ڱ������ߵ�Ȩ��
	// ��ͨ��ԱȨ��Ϊ0������ԱȨ��Ϊ1����ֻ�й���Ա��ɾ�ˣ�����ֻ��ɾ����ͨ��Ա
	// �Ժ�������Ⱥ��������Ȩ�޿���Ϊ2����Ⱥ������ɾ�����г�Ա
	public void deleteOccupant(String operator, String userId) {
		if (occupantsMap.keySet().contains(userId)
				&& occupantsMap.keySet().contains(operator)
				&& (occupantsMap.get(operator) > occupantsMap.get(userId)))
			occupantsMap.remove(userId);
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getCreaterAccount() {
		return createrAccount;
	}

	public void setCreaterAccount(String createrAccount) {
		this.createrAccount = createrAccount;
	}

	public int getOccupantsNum() {
		return occupantsMap.size();
	}

	public Map<String, Integer> getOccupantsMap() {
		return occupantsMap;
	}

	public void setOccupantsMap(Map<String, Integer> occupantsMap) {
		this.occupantsMap = occupantsMap;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Map<String, Integer> getTargetOccupantsMap() {
		return this.targetOccupantsMap;
	}

	public void setTargetOccupantsMap(Map<String, Integer> targetOccupantsMap) {
		this.targetOccupantsMap = targetOccupantsMap;
	}

	public int getFunctionType() {
		return functionType;
	}

	public void setFunctionType(int functionType) {
		this.functionType = functionType;
	}
	
	public int getSyncType() {
		return syncType;
	}

	public void setSyncType(int syncType) {
		this.syncType = syncType;
	}
	
}
