package com.hust.schoolmatechat.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hust.schoolmatechat.SearchSuggestionProvider;
import com.hust.schoolmatechat.ChatMsgservice.GroupChatRoomEntity;
import com.hust.schoolmatechat.SearchSuggestionProvider.MySQLiteDatabase;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.entity.ContactsEntity;

public class GroupChatDao extends BaseDao {
	private static final String TAG = "GroupChatDao";

	public GroupChatDao(Context context) {
		super(context);
	}

	/**
	 * 增加一个群组表项
	 * 
	 * @param values
	 */
	public boolean addGroupChatEntity(GroupChatRoomEntity groupChatRoomEntity) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		try {
			String groupId = groupChatRoomEntity.getGroupId();
			String userAccount = groupChatRoomEntity.getUserAccount();
			String groupName = groupChatRoomEntity.getGroupName();
			String createrAccount = groupChatRoomEntity.getCreaterAccount();
			String description = groupChatRoomEntity.getDescription();
			String subject = groupChatRoomEntity.getSubject();
			String adminsAccount = groupChatRoomEntity.getAdministratersStr();
			String membersAccount = groupChatRoomEntity.getNormalMembersStr();

			if (db.isOpen()) {
				db.execSQL(
						"INSERT INTO GroupInfo(groupId,userAccount,groupName,createrAccount,description,subject,adminsAccount,membersAccount) VALUES(?,?,?,?,?,?,?,?)",
						new Object[] { groupId, userAccount, groupName,
								createrAccount, description, subject,
								adminsAccount, membersAccount });
			} else {
				CYLog.e(TAG, "db is not opened!");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return false;
		} finally {
			if (db1 != null) {
				db1.close();
			}
		}
		return true;
	}

	/**
	 * 根据用户ID,聊天室ID,删除一个群聊室表项
	 */
	public boolean deleteGroupChatEntity(String userAccount, String groupId) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		try {
			if (db.isOpen()) {
				db.execSQL(
						"delete from GroupInfo where userAccount=? and groupId=?",
						new Object[] { userAccount, groupId });
			}
		} catch (Exception e) {
			CYLog.e(TAG, "db is not opened!");
			return false;
		} finally {
			if (db1 != null) {
				db1.close();
			}
		}
		return true;
	}

	/**
	 * 更新群组表
	 */
	public boolean updateGroupChatEntity(GroupChatRoomEntity groupChatRoomEntity) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);

		try {
			String groupId = groupChatRoomEntity.getGroupId();
			String userAccount = groupChatRoomEntity.getUserAccount();
			String groupName = groupChatRoomEntity.getGroupName();
			// String createrAccount = groupChatRoomEntity.getCreaterAccount();
			String description = groupChatRoomEntity.getDescription();
			String subject = groupChatRoomEntity.getSubject();
			String adminsAccount = groupChatRoomEntity.getAdministratersStr();
			String membersAccount = groupChatRoomEntity.getNormalMembersStr();

			if (db.isOpen()) {
				db.execSQL(
						"UPDATE GroupInfo SET groupName=?,description=?,subject=?,adminsAccount=?,membersAccount=? where groupId=? and userAccount=?",
						new Object[] { groupName, description, subject,
								adminsAccount, membersAccount, groupId,
								userAccount });
			} else {
				CYLog.e(TAG, "db is not opened!");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return false;
		} finally {
			if (db1 != null) {
				db1.close();
			}
		}
		return true;
	}

	/**
	 * 根据群组id获取本地群组信息，不带有password信息，若同步处理需要自行设定
	 * 
	 * @param userAccount
	 * @param groupId
	 * @return
	 */
	public GroupChatRoomEntity getGroupChatRoomByGroupId(String userAccount,
			String groupId) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;

		try {
			if (db.isOpen()) {
				cursor = db
						.rawQuery(
								"SELECT * FROM GroupInfo WHERE userAccount=? and groupId=?",
								new String[] { userAccount, groupId });

				while (cursor.moveToNext()) {
					GroupChatRoomEntity groupChatRoomEntity = dbCursorToGroupChatRoomEntity(cursor);
					return groupChatRoomEntity;
				}
				return null;
			} else {
				CYLog.e(TAG, "db is not opened!");
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			
			if (db1 != null) {
				db1.close();
			}
		}
	}

	/**
	 * 取一条群组数据记录
	 * 
	 * @param cursor
	 * @return
	 */
	private GroupChatRoomEntity dbCursorToGroupChatRoomEntity(Cursor cursor) {
		try {
			GroupChatRoomEntity groupChatRoomEntity = new GroupChatRoomEntity();
			String groupId = cursor.getString(cursor.getColumnIndex("groupId"));
			String userAccount = cursor.getString(cursor
					.getColumnIndex("userAccount"));
			String groupName = cursor.getString(cursor
					.getColumnIndex("groupName"));
			String createrAccount = cursor.getString(cursor
					.getColumnIndex("createrAccount"));
			String description = cursor.getString(cursor
					.getColumnIndex("description"));
			String subject = cursor.getString(cursor.getColumnIndex("subject"));
			String adminsAccount = cursor.getString(cursor
					.getColumnIndex("adminsAccount"));
			String membersAccount = cursor.getString(cursor
					.getColumnIndex("membersAccount"));
			groupChatRoomEntity.setGroupId(groupId);
			groupChatRoomEntity.setUserAccount(userAccount);
			groupChatRoomEntity.setGroupName(groupName);
			groupChatRoomEntity.setCreaterAccount(createrAccount);
			groupChatRoomEntity.setDescription(description);
			groupChatRoomEntity.setSubject(subject);

			Map<String, Integer> memberMap = new HashMap<String, Integer>();
			if (adminsAccount != null && !adminsAccount.equals("")) {
				String[] ownerAccountArray = adminsAccount.split(",");
				for (int i = 0; i < ownerAccountArray.length; ++i) {
					String t = ownerAccountArray[i];
					if (!t.equals("") && !memberMap.containsKey(t)) {
						memberMap.put(t, 1);// 管理员
					}
				}
			}
			memberMap.put(createrAccount, 1);

			if (membersAccount != null && !membersAccount.equals("")) {
				String[] memberAccountsTmp = membersAccount.split(",");
				for (int i = 0; i < memberAccountsTmp.length; ++i) {
					String t = memberAccountsTmp[i];
					if (!t.equals("") && !memberMap.containsKey(t)) {
						memberMap.put(t, 0);// 普通成员
					}
				}
			}

			groupChatRoomEntity.setOccupantsMap(memberMap);
			return groupChatRoomEntity;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 取群组记录列表，群组id为压缩字符串，逗号作为分隔符
	 * 
	 * @param userAccount
	 * @param groupIds
	 * @return
	 */
	public List<GroupChatRoomEntity> getGroupChatRoomEntityList(
			String userAccount, String groupIds) {
		try {
			if (groupIds == null || groupIds.equals("")) {
				return null;
			}

			String groupIdArray[] = groupIds.split(",");
			Set<String> groupIdSet = new HashSet<String>();
			List<GroupChatRoomEntity> groupChatRoomEntityList = new ArrayList<GroupChatRoomEntity>();
			for (int i = 0; i < groupIdArray.length; ++i) {
				if (!groupIdSet.contains(groupIdArray[i])) {
					groupIdSet.add(groupIdArray[i]);
					GroupChatRoomEntity groupChatRoomEntityT = this
							.getGroupChatRoomByGroupId(userAccount,
									groupIdArray[i]);
					groupChatRoomEntityList.add(groupChatRoomEntityT);
				}
			}
			if (groupChatRoomEntityList.size() > 0) {
				return groupChatRoomEntityList;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 获取用户在本地的群组记录数目
	 * @param userAccount
	 * @return
	 */
	public Map<String, GroupChatRoomEntity> getAllGroupChatRoomEntityMap(String userAccount) {
		try {
			List<GroupChatRoomEntity> groupChatRoomEntityList = 
					this.getAllGroupChatRoomEntityList(userAccount);
			int len = groupChatRoomEntityList.size();
			Map<String, GroupChatRoomEntity> groupChatRoomEntityMap = new HashMap<String, GroupChatRoomEntity>();
			for (int i = 0; i < len; ++i) {
				GroupChatRoomEntity groupChatRoomEntity = groupChatRoomEntityList.get(i);
				groupChatRoomEntityMap.put(groupChatRoomEntity.getGroupId(), groupChatRoomEntity);
			}
			
			if (len > 0) {
				return groupChatRoomEntityMap;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}
	}
	
	/**
	 * 获取用户在本地的群组记录数目
	 * @param userAccount
	 * @return
	 */
	public List<GroupChatRoomEntity> getAllGroupChatRoomEntityList(String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		String sql = "SELECT * FROM GroupInfo WHERE userAccount=?";
		try {
			List<GroupChatRoomEntity> groupChatRoomEntityList = new ArrayList<GroupChatRoomEntity>();
			if (db.isOpen()) {
				String[] params = new String[] { userAccount };
				cursor = db.rawQuery(sql, params);

				while (cursor.moveToNext()) {
					GroupChatRoomEntity groupChatRoomEntity = dbCursorToGroupChatRoomEntity(cursor);
					groupChatRoomEntityList.add(groupChatRoomEntity);
				}
			
				return groupChatRoomEntityList;
			} else {
				CYLog.e(TAG, "db is not opened!");
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db1 != null) {
				db1.close();
			}
		}
	}
	
	/**
	 * 获取用户在本地的群组记录数目
	 * @param userAccount
	 * @return
	 */
	public int getGroupChatRoomEntityCount(String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "SELECT COUNT(*) FROM GroupInfo WHERE userAccount=?";
		Cursor cursor = null;
		try {
			if (db.isOpen()) {
				String[] params = new String[] { userAccount };
				cursor = db.rawQuery(sql, params);

				if (cursor.moveToFirst()) {
					int resultCount = cursor.getInt(0);
//					CYLog.d(TAG, "groupId " + groupId + " count: "
//							+ resultCount);
					return resultCount;
				}
			} else {
				CYLog.e(TAG, "db is not opened!");
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return 0;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db1 != null) {
				db1.close();
			}
		}
	}
	
	/**
	 * 判断本地是否有群表记录
	 * @param userAccount
	 * @param groupId
	 * @return
	 */
	public boolean isGroupChatRoomEntityExisted(String userAccount, String groupId) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "SELECT COUNT(*) FROM GroupInfo WHERE userAccount=? and groupId=?";
		Cursor cursor = null;
		try {
			if (db.isOpen()) {
				String[] params = new String[] { userAccount, groupId };
				cursor = db.rawQuery(sql, params);

				if (cursor.moveToFirst()) {
					int resultCount = cursor.getInt(0);
//					CYLog.d(TAG, "groupId " + groupId + " count: "
//							+ resultCount);
					if (resultCount > 0) {
						return true;
					}
				}
			} else {
				CYLog.e(TAG, "db is not opened!");
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return false;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db1 != null) {
				db1.close();
			}
		}
	}

	//+++++++++++++++++++++++++++++++++++++ 群成员表 ++++++++++++++++++++++++++++++
	/**
	 * !!!!!注意!!!!检查是否为自己的好友，若是好友，则不允许入群联系人表，退群，也要将此人从群成员表中删除
	 * 增加一个联系人
	 * 
	 * @param contactsEntity
	 */
	public void addContactsEntity(ContactsEntity contactsEntity) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		try {
			if (db.isOpen()) {
				Object[] params = new Object[] {
						contactsEntity.getUserAccount(),
						contactsEntity.getClassName(),
						contactsEntity.getAccountNum(),
						contactsEntity.getAddress(),
						contactsEntity.getAuthenticated(),
						contactsEntity.getBaseInfoId(),
						contactsEntity.getChannels(),
						contactsEntity.getEmail(),
						contactsEntity.getIntrestType(),
						contactsEntity.getName(), contactsEntity.getPhoneNum(),
						contactsEntity.getPicture(), contactsEntity.getSex(),
						contactsEntity.getSign(),
						contactsEntity.getHasAllClassmates() };

				db.execSQL(
						"INSERT INTO GroupMemberInfo(userAccount,className,accountNum,address,authenticated,baseInfoId,channels,email,intrestType,name,phoneNum,picture,sex,sign,hasAllClassmates) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
						params);
			} else {
				CYLog.e(TAG, "eb is not opened!");
			}
		} catch (Exception e) {
			CYLog.e(TAG, e.toString());
		} finally {
			if (db1 != null) {
				db1.close();
			}
		}
	}

	/**
	 * 根据账号列表压缩字段获取联系人列表  userAccount 使用app登陆的人的账号
	 * 
	 * @param account
	 * accountNum 
	 * @return
	 */
	public List<ContactsEntity> getContactsEntity(String userAccount, String accountNums) {
		try {
			if (accountNums == null || accountNums.equals("")) {
				return null;
			}
			
			String accountNumArray[] = accountNums.split(",");
			Set<String> accountNumSet = new HashSet<String>();
			List<ContactsEntity> contactsEntityList = new ArrayList<ContactsEntity>();
			for (int i = 0; i < accountNumArray.length; ++i) {
				if (!accountNumSet.contains(accountNumArray[i])) {
					accountNumSet.add(accountNumArray[i]);
					ContactsEntity contactsEntity = getFriendInfoByAccount(userAccount, 
							accountNumArray[i]);
					if (contactsEntity != null) {
						contactsEntityList.add(contactsEntity);
					}
				}
			}
			
			if (contactsEntityList.size() > 0) {
				return contactsEntityList;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据登陆app用户的账号userAccount和联系人的账号更新联系人信息
	 * 
	 * @param contactsEntity
	 * 
	 */
	public void updateContacsEntity(ContactsEntity contactsEntity) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { contactsEntity.getClassName(),
						contactsEntity.getAddress(),
						contactsEntity.getBaseInfoId(),
						contactsEntity.getAuthenticated(),
						contactsEntity.getChannels(),
						contactsEntity.getEmail(),
						contactsEntity.getIntrestType(),
						contactsEntity.getName(), contactsEntity.getPhoneNum(),
						contactsEntity.getPicture(), contactsEntity.getSex(),
						contactsEntity.getSign(),
						contactsEntity.getHasAllClassmates(),
						contactsEntity.getUserAccount(),
						contactsEntity.getAccountNum() };

				db.execSQL(
						"UPDATE GroupMemberInfo SET className=?,address=?,baseInfoId=?,authenticated=?,channels=?,email=?,intrestType=?,name=?,phoneNum=?,picture=?,sex=?,sign=?,hasAllClassmates=? WHERE userAccount=? AND accountNum=?",
						params);
			} else {
				CYLog.e(TAG, "eb is not opened!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
		} finally {
			if (db1 != null) {
				db1.close();
			}
		}
	}

	/**
	 * 更新联系人信息获取情况
	 * 
	 * @param userAccount
	 *            accountNum
	 * @param i
	 */
	public void updateHasAllClassmates(String userAccount, String accountNum,
			int i) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { i, accountNum, userAccount };

				db.execSQL(
						"UPDATE GroupMemberInfo SET hasAllClassmates=? WHERE accountNum=? AND userAccount=?",
						params);
			} else {
				CYLog.e(TAG, "updateChatItem db is not opened!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
		} finally {
			if (db1 != null) {
				db1.close();
			}
		}
	}

	/**
	 * 删除登陆用户的联系人
	 * 
	 * @param userAccount
	 * @param accountNum
	 */
	public void deleteContactsEntity(String userAccount, String accountNum) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "delete from GroupMemberInfo where userAccount=? and accountNum=?";
		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { userAccount, accountNum };// 直接删除所有的
				db.execSQL(sql, params);
			} else {
				CYLog.e(TAG, "eb is not opened!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
		} finally {
			if (db1 != null) {
				db1.close();
			}
		}
	}

	/**
	 * 登陆用户是否拥有某个联系人
	 * 
	 * @param contactsEntity
	 * @param userAccount
	 * @param accountNum
	 * @return
	 */
	public boolean isContactsEntityExisted(String userAccount, String accountNum) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "SELECT COUNT(*) FROM GroupMemberInfo WHERE userAccount=? and accountNum=?";
		Cursor cursor = null;
		try {
			if (db.isOpen()) {
				String[] params = new String[] { userAccount, accountNum };
				cursor = db.rawQuery(sql, params);

				if (cursor.moveToFirst()) {
					int resultCount = cursor.getInt(0);
//					CYLog.d(TAG, "accountNum " + accountNum + " count: "
//							+ resultCount);
					if (resultCount > 0) {
						return true;
					}
				}
			} else {
				CYLog.e(TAG, "db is not opened!");
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return false;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db1 != null) {
				db1.close();
			}
		}
	}

	/**
	 * 根据账号获取好友信息，注意没有认证的班级同学的账号为null，此时获取不到这个人的信息，要通过基础信息id来获取
	 * 
	 * @param friendAccount
	 * @param userAccount
	 * @return
	 */
	public ContactsEntity getFriendInfoByAccount(String userAccount, String friendAccount
			) {
		if (friendAccount == null || friendAccount.equals("")) {
			CYLog.e(TAG, "getFriendInfoByAccount account null");
			return null;
		}

		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		try {
			if (db.isOpen()) {
				ContactsEntity contactsEntity = null;
				cursor = db
						.rawQuery(
								"SELECT * FROM GroupMemberInfo WHERE userAccount=? AND accountNum=?",
								new String[] { userAccount, friendAccount });

				while (cursor.moveToNext()) {// 获取一个联系人的信息
					contactsEntity = new ContactsEntity();
					contactsEntity.setUserAccount(cursor.getString(cursor
							.getColumnIndex("userAccount")));
					contactsEntity.setClassName(cursor.getString(cursor
							.getColumnIndex("className")));
					contactsEntity.setAccountNum(cursor.getString(cursor
							.getColumnIndex("accountNum")));
					contactsEntity.setAddress(cursor.getString(cursor
							.getColumnIndex("address")));
					contactsEntity.setAuthenticated(cursor.getString(cursor
							.getColumnIndex("authenticated")));
					contactsEntity.setBaseInfoId(cursor.getString(cursor
							.getColumnIndex("baseInfoId")));
					contactsEntity.setChannels(cursor.getString(cursor
							.getColumnIndex("channels")));
					contactsEntity.setEmail(cursor.getString(cursor
							.getColumnIndex("email")));
//					contactsEntity.setId(cursor.getString(cursor
//							.getColumnIndex("id")));
					contactsEntity.setIntrestType(cursor.getString(cursor
							.getColumnIndex("intrestType")));
					contactsEntity.setName(cursor.getString(cursor
							.getColumnIndex("name")));
					contactsEntity.setPhoneNum(cursor.getString(cursor
							.getColumnIndex("phoneNum")));
					contactsEntity.setPicture(cursor.getString(cursor
							.getColumnIndex("picture")));
					contactsEntity.setSex(cursor.getString(cursor
							.getColumnIndex("sex")));
					contactsEntity.setSign(cursor.getString(cursor
							.getColumnIndex("sign")));
					contactsEntity.setHasAllClassmates(cursor.getInt(cursor
							.getColumnIndex("hasAllClassmates")));
					break;
				}

				return contactsEntity;
			} else {
				CYLog.e(TAG, "db not open");
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db1 != null) {
				db1.close();
			}
		}
	}

	/**
	 * 联系人信息是否完整
	 * 
	 * @param userAccount
	 * @param frindAccount
	 * @return
	 */
	public boolean isContacsEntityInfoComplete(String userAccount,
			String frindAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "SELECT hasAllClassmates FROM GroupMemberInfo WHERE accountNum=? AND userAccount=?";
		Cursor cursor = null;
		boolean isExisted = false;
		try {
			if (db.isOpen()) {
				String[] params = new String[] { frindAccount, userAccount };
				cursor = db.rawQuery(sql, params);

				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex("hasAllClassmates");
					if (index >= 0) {
						int resultCount = cursor.getInt(index);
//						CYLog.d(TAG, "frindAccount " + frindAccount
//								+ " hasAllClassmates : " + resultCount);
						if (resultCount == 1)
							isExisted = true;
					}
				}
			} else {
				CYLog.e(TAG, "db is not opened!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (db1 != null) {
				db1.close();
			}
		}

		return isExisted;
	}
}
