package com.hust.schoolmatechat.ChatMsgservice;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GroupChatRoomEntity {
	private String groupName; // 聊天室的名称
	private String groupId; // 聊天室mGroupId,全局唯一
	private String createrAccount; // 聊天室创建者,实际上聊天室有三种用户：创建者，即群主
								   // ;管理员;普通用户
	private Map<String, Integer> occupantsMap;  // 聊天室成员，值为1则为管理员，0则代码普通成员。Web服务器上
												// ownerAccount代表管理员，memberAccount代表所有成员
												//createAccount也包含在管理员中
	private String description; // 聊天室的描述
	private String subject; // 聊天室的主题
	
	//下面的字段用于在tigase上进行群聊操作
	private Map<String, Integer> targetOccupantsMap; //群聊操作操作的对象
    private int functionType;           //聊天室操作类型，有添加、申请加入、删除
	public final static int FUNCTYPE_ADD = 0;          //创建聊天室，给定mName,mOccupants(要邀请的人)
	public final static int FUNCTYPE_JOIN = 1;         //加入聊天室 ,给定mName,mOccupants(要加入的人)
	public final static int FUNCTYPE_DEL = 2;          //删除聊天室，给定mName
//	public final static int FUNCTION_KICK = 3;         //从聊天室中踢人，给定mName,mOccupants(要踢的人)
	public final static int FUNCTION_CONSULT = 4;      //查询聊天室详细信息，给定mName
	public final static int FUNCTION_UBSUBSSRIBE = 5;      //查询聊天室详细信息，给定mName
	
//	type字段说明
//	0 创建群组 
//	1 删除群组
//	2 添加普通成员 membersAccount内要添加的普通成员
//	3 删除普通成员 membersAccount内为要删除的普通成员
//	4 添加管理员  adminsAccount内为要添加的管理员
//	5 删除管理员 adminsAccount内要删除的管理员
//	6 普通成员提升为管理员  membersAccount内为要提升的普通成员
//	7 管理员降级为普通成员  adminsAccount内为要要降级的管理员
//	8 修改群自有信息，群名，描述，主题
	//下面的字段用于在web服务器上进行群聊操作
	private int syncType;
	public final static int SYNCTYPE_ADD_ROOM = 0;
	public final static int SYNCTYPE_DEL_ROOM = 1;
	public final static int SYNCTYPE_ADD_NORMAL_MEMBER = 2;
	public final static int SYNCTYPE_DEL_NORMAL_MEMBER = 3;
	public final static int SYNCTYPE_ADD_ADMIN = 4;
	public final static int SYNCTYPE_DEL_ADMIN = 5;
	public final static int SYNCTYPE_ELEVATE_PERMISSION = 6;  //将普通成员提升为管理员
	public final static int SYNCTYPE_DEGRADE_PERMISSION = 7;  //将管理员降级为普通成员
	public final static int SYNCTYPE_UPDATE_INFO = 8;  //包括群名、描述、主题 

	// 用于数据同步到web失败时重新同步
	private String userAccount; // 登陆用户
	private String password; // 登陆用户的密码

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

	// 将管理员账号组合为字符串返回
	public String getAdministratersStr() {
		try {
			if (occupantsMap.size() == 0) {
				return "";
			}

			StringBuffer administratersBuf = new StringBuffer(); // 管理员
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

	// 将普通用户账号组合为字符串返回
	public String getNormalMembersStr() {
		try {
			if (occupantsMap.size() == 0) {
				return "";
			}

			StringBuffer membersBuf = new StringBuffer(); // 管理员
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
	 * 取所有成员
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

	// 将给定账号添加为管理员
	public void addAdministrater(String userId) {
		occupantsMap.put(userId, 1);
	}

	// 将给定账号添加为普通成员
	public void addNormalMember(String userId) {
		occupantsMap.put(userId, 0);
	}

	// 删除聊天室成员，操作者的权限必须大于被操作者的权限
	// 普通成员权限为0，管理员权限为1，即只有管理员能删人，而且只能删除普通成员
	// 以后可能添加群主，他的权限可设为2，即群主可以删除所有成员
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
