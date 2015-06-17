package com.hust.schoolmatechat.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hust.schoolmatechat.SearchSuggestionProvider;
import com.hust.schoolmatechat.SearchSuggestionProvider.MySQLiteDatabase;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.utils.DateFormatUtils;

public class ClassmateDao extends BaseDao {
	private static final String TAG = "ClassmateDao";

	public ClassmateDao(Context context) {
		super(context);
	}

	/**
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
						"INSERT INTO StudentsInfo(userAccount,className,accountNum,address,authenticated,baseInfoId,channels,email,intrestType,name,phoneNum,picture,sex,sign,hasAllClassmates) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
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
	 * 获取所有的联系人,组已经分好 userAccount 使用app登陆的人的账号
	 */
	public Map<String, List<ContactsEntity>> getAllcontactsEntity(
			String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		try {
			// 获取登陆用户的信息
			ContactsEntity selfContactsEntity = getSelfContactsEntity(userAccount);
			if (selfContactsEntity == null) {
				CYLog.d(TAG, "local sqlite db no self user info");
				return null;
			}

			// 获取组信息
			String className = selfContactsEntity.getClassName();
			String classItems[] = className.split(",");
			Set<String> classSet = new HashSet<String>();
			for (int i = 0; i < classItems.length; ++i) {
				if (classItems[i] != null && !classItems[i].equals("")) {
					classSet.add(classItems[i]);
				}
			}

			Map<String, List<ContactsEntity>> allContactsEntity = new HashMap<String, List<ContactsEntity>>();
			if (db.isOpen()) {
				for (String entry : classSet) {// 取出这个组的联系人
					cursor = db
							.rawQuery(
									"SELECT * FROM StudentsInfo WHERE userAccount=? and className=?",
									new String[] { userAccount, entry });
					List<ContactsEntity> contactsEntityList = new ArrayList<ContactsEntity>();
					while (cursor.moveToNext()) {// 获取一个联系人的信息
						ContactsEntity contactsEntity = new ContactsEntity();
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
						// contactsEntity.setId(cursor.getString(cursor
						// .getColumnIndex("id")));
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
						contactsEntityList.add(contactsEntity);
						// CYLog.d(TAG, "get contact from sqlite: account = "
						// + contactsEntity.getAccountNum() + " name = "
						// + contactsEntity.getName());
					}

					if (contactsEntityList.size() > 0) {
						allContactsEntity.put(entry, contactsEntityList);
					}
				}
				
				return allContactsEntity;
			} else {
				CYLog.e(TAG, "db not open");
			}
			return null;
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
	 * 获取所有的联系人 未分组 userAccount 使用app登陆的人的账号
	 * 
	 * @param account
	 * @return
	 */
	public List<ContactsEntity> getAllContactsEntityList(String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		try {
			if (db.isOpen()) {
				cursor = db.rawQuery(
						"SELECT * FROM StudentsInfo WHERE userAccount=?",
						new String[] { userAccount });
				List<ContactsEntity> contactsEntityList = new ArrayList<ContactsEntity>();
				while (cursor.moveToNext()) {// 获取一个联系人的信息
					ContactsEntity contactsEntity = new ContactsEntity();
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
					// contactsEntity.setId(cursor.getString(cursor
					// .getColumnIndex("id")));
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
					contactsEntityList.add(contactsEntity);
				}
				
				return contactsEntityList;
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
	 * 根据登陆app用户的账号userAccount和联系人的基础id更新联系人信息
	 * 
	 * @param contactsEntity
	 * 
	 */
	public void updateContacsEntity(ContactsEntity contactsEntity) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		try {
			if (db.isOpen()) {
				StringBuffer baseInfoBuf = new StringBuffer();
				String baseInfoId = contactsEntity.getBaseInfoId();
				// 基础id修改，id格式不能错误
				if (baseInfoId.length() < 19) {
					return;
				}
				baseInfoBuf.append("%").append(contactsEntity.getBaseInfoId())
						.append("%");

				Object[] params = new Object[] { contactsEntity.getClassName(),
						contactsEntity.getAddress(),
						contactsEntity.getAuthenticated(),
						contactsEntity.getAccountNum(),
						contactsEntity.getChannels(),
						contactsEntity.getEmail(),
						contactsEntity.getIntrestType(),
						contactsEntity.getName(), contactsEntity.getPhoneNum(),
						contactsEntity.getPicture(), contactsEntity.getSex(),
						contactsEntity.getSign(),
						contactsEntity.getHasAllClassmates(),
						contactsEntity.getUserAccount(), baseInfoBuf.toString() };

				db.execSQL(
						"UPDATE StudentsInfo SET className=?,address=?,authenticated=?,accountNum=?,channels=?,email=?,intrestType=?,name=?,phoneNum=?,picture=?,sex=?,sign=?,hasAllClassmates=? WHERE userAccount=? AND baseInfoId LIKE ?",
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
						"UPDATE StudentsInfo SET hasAllClassmates=? WHERE accountNum=? AND userAccount=?",
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
		String sql = "delete from StudentsInfo where userAccount=? and accountNum=?";
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
	 * 根据基础id和姓名删除记录
	 * 
	 * @param userAccount
	 * @param baseInfoIdBak
	 * @param name
	 */
	public void deleteContactsEntityByIdAndName(String userAccount,
			String baseInfoIdBak, String name) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		StringBuffer baseInfoBuf = new StringBuffer();
		baseInfoBuf.append("%").append(baseInfoIdBak).append("%");
		String sql = "delete from StudentsInfo where userAccount=? and baseInfoId LIKE ? and name=?";
		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { userAccount,
						baseInfoBuf.toString(), name };// 直接删除所有的
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
	 * 登陆用户是否拥有某个联系人,判断标准为基础信息id而非用户账号,baseInfoIds内id有可能用逗号隔开了
	 * 
	 * @param contactsEntity
	 * @param userAccount
	 * @param baseInfoIds
	 * @return
	 */
	public boolean isContacsEntityExistedByAccountNum(String userAccount,
			String accountNum) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		String sql = "SELECT COUNT(*) FROM StudentsInfo WHERE userAccount=? and accountNum=?";
		try {
			if (db.isOpen()) {
				String[] params = new String[] { userAccount, accountNum };
				cursor = db.rawQuery(sql, params);

				if (cursor.moveToFirst()) {
					int resultCount = cursor.getInt(0);
					// CYLog.d(TAG, "baseInfoIds " + baseInfoIds +
					// " count: "
					// + resultCount);
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
	 * 登陆用户是否拥有某个联系人,判断标准为基础信息id而非用户账号,baseInfoIds内id有可能用逗号隔开了
	 * 
	 * @param contactsEntity
	 * @param userAccount
	 * @param baseInfoIds
	 * @return
	 */
	public boolean isContacsEntityExisted(String userAccount, String baseInfoIds) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		String sql = "SELECT COUNT(*) FROM StudentsInfo WHERE userAccount=? and baseInfoId LIKE ?";
		try {
			String baseInfoIdArray[] = baseInfoIds.split(",");
			if (db.isOpen()) {
				for (int i = 0; i < baseInfoIdArray.length; ++i) {
					StringBuffer baseInfoBuf = new StringBuffer();
					baseInfoBuf.append("%").append(baseInfoIdArray[i])
							.append("%");
					String[] params = new String[] { userAccount,
							baseInfoBuf.toString() };
					cursor = db.rawQuery(sql, params);

					if (cursor.moveToFirst()) {
						int resultCount = cursor.getInt(0);
						// CYLog.d(TAG, "baseInfoIds " + baseInfoIds +
						// " count: "
						// + resultCount);
						if (resultCount > 0) {
							return true;
						}
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
	 * @param owner
	 * @param userAccount
	 * @return
	 */
	public ContactsEntity getFriendInfoByAccount(String friendAccount,
			String userAccount) {
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
								"SELECT * FROM StudentsInfo WHERE userAccount=? AND accountNum=?",
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
					// contactsEntity.setId(cursor.getString(cursor
					// .getColumnIndex("id")));
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
	 * 根据姓名获取好友基础id
	 * 
	 * @param owner
	 * @param userAccount
	 * @return
	 */
	public ContactsEntity getFriendIDByName(String friendName) {
		if (friendName == null || friendName.equals("")) {
			CYLog.e(TAG, "getFriendIDByName friendName null");
			return null;
		}

		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		try {
			if (db.isOpen()) {
				ContactsEntity contactsEntity = null;
				cursor = db.rawQuery(
						"SELECT * FROM StudentsInfo WHERE name=?",
						new String[] { friendName });

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
					// contactsEntity.setId(cursor.getString(cursor
					// .getColumnIndex("id")));
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
	 * 根据基础信息id获取好友信息
	 * 
	 * @param baseInfoIds
	 * @param userAccount
	 * @return
	 */
	public ContactsEntity getFriendInfoByBaseInfoIds(String baseInfoIds,
			String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		try {
			if (db.isOpen()) {
				ContactsEntity contactsEntity = null;
				StringBuffer baseInfoBuf = new StringBuffer();
				baseInfoBuf.append("%").append(baseInfoIds).append("%");
				cursor = db
						.rawQuery(
								"SELECT * FROM StudentsInfo WHERE userAccount=? AND baseInfoId LIKE ?",
								new String[] { userAccount,
										baseInfoBuf.toString() });

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
					// contactsEntity.setId(cursor.getString(cursor
					// .getColumnIndex("id")));
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
		Cursor cursor = null;
		String sql = "SELECT hasAllClassmates FROM StudentsInfo WHERE accountNum=? AND userAccount=?";
		boolean isExisted = false;
		try {
			if (db.isOpen()) {
				String[] params = new String[] { frindAccount, userAccount };
				cursor = db.rawQuery(sql, params);

				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex("hasAllClassmates");
					if (index >= 0) {
						int resultCount = cursor.getInt(index);
						// CYLog.d(TAG, "frindAccount " + frindAccount
						// + " hasAllClassmates : " + resultCount);
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

	/**
	 * 判断是否为自己的好友
	 * 
	 * @param userAccount
	 * @param frindAccount
	 * @return
	 */
	public boolean isMyFriend(String userAccount, String frindAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		String sql = "SELECT COUNT(*) FROM StudentsInfo WHERE accountNum=? AND userAccount=?";
		boolean isExisted = false;
		try {
			if (db.isOpen()) {
				String[] params = new String[] { frindAccount, userAccount };
				cursor = db.rawQuery(sql, params);

				if (cursor.moveToFirst()) {
					int resultCount = cursor.getInt(0);
					CYLog.d(TAG, "frindAccount " + frindAccount
							+ " is friend count : " + resultCount);
					if (resultCount > 0)
						isExisted = true;
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

	/**
	 * 登陆用户是否拥有某个分组
	 * 
	 * @param className
	 * @param userAccount
	 * @return
	 */
	public boolean isClassExisted(String className, String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		String sql = "SELECT COUNT(*) FROM StudentsInfo WHERE className=? AND userAccount=?";
		boolean isExisted = false;
		try {
			if (db.isOpen()) {
				String[] params = new String[] { className, userAccount };
				cursor = db.rawQuery(sql, params);

				if (cursor.moveToFirst()) {
					int resultCount = cursor.getInt(0);
					CYLog.e(TAG, "class " + className + " count: "
							+ resultCount);
					if (resultCount > 0)
						isExisted = true;
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

	/**
	 * 本地是否有用户的联系人信息
	 * 
	 * @param accountNum
	 * @return
	 */
	public boolean isContacsMapExisted(String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		String sql = "SELECT COUNT(*) FROM StudentsInfo WHERE userAccount=?";
		boolean isExisted = false;
		try {
			if (db.isOpen()) {
				String[] params = new String[] { userAccount };
				cursor = db.rawQuery(sql, params);

				if (cursor.moveToFirst()) {
					int resultCount = cursor.getInt(0);
					CYLog.e(TAG, "contacs number " + resultCount);
					if (resultCount > 0)
						isExisted = true;
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

	/******************** 处理登陆用户信息 **********************/
	/**
	 * 增加个人信息
	 * 
	 * @param contactsEntity
	 */
	public void addSelfContactsEntity(ContactsEntity contactsEntity) {
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
						contactsEntity.getGroupName(),
						contactsEntity.getPassword(),
						contactsEntity.getHasAllClassmates() };

				db.execSQL(
						"INSERT INTO UserSelfInfo(userAccount,className,accountNum,address,authenticated,baseInfoId,channels,email,intrestType,name,phoneNum,picture,sex,sign,groupName,password,hasAllClassmates) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
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
	 * 查找个人信息
	 * 
	 * @param userAccount
	 *            为手机号或者账号
	 */
	public ContactsEntity getSelfContactsEntity(String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		try {
			if (db.isOpen()) {
				cursor = db
						.rawQuery(
								"SELECT * FROM UserSelfInfo WHERE userAccount=? or phoneNum=?",
								new String[] { userAccount, userAccount });
				ContactsEntity contactsEntity = new ContactsEntity();
				while (cursor.moveToNext()) {
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
					// contactsEntity.setId(cursor.getString(cursor
					// .getColumnIndex("id")));
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
					contactsEntity.setGroupName(cursor.getString(cursor
							.getColumnIndex("groupName")));
					contactsEntity.setPassword(cursor.getString(cursor
							.getColumnIndex("password")));
					contactsEntity.setHasAllClassmates(cursor.getInt(cursor
							.getColumnIndex("hasAllClassmates")));
				}

				return contactsEntity;
			} else {
				CYLog.e(TAG, "eb is not opened!");
				return null;
			}
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
	 * 修改登陆用户的个人信息
	 * 
	 * @param contactsEntity
	 */
	public void updateSelfContactsEntity(ContactsEntity contactsEntity) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { contactsEntity.getClassName(),
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
						contactsEntity.getGroupName(),
						contactsEntity.getPassword(),
						contactsEntity.getHasAllClassmates(),
						contactsEntity.getUserAccount(),
						contactsEntity.getPhoneNum() };

				db.execSQL(
						"UPDATE UserSelfInfo SET className=?,accountNum=?,address=?,authenticated=?,baseInfoId=?,channels=?,email=?"
								+ ",intrestType=? ,name=?,phoneNum=?,picture=?,sex=?,sign=?,groupName=?,password=?,hasAllClassmates=? WHERE  userAccount=? or phoneNum=?",
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
	 * 删除登陆用户的信息
	 * 
	 * @param userAccount
	 */
	public boolean deleteSelfContactsEntity(String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "delete from UserSelfInfo where userAccount=? or phoneNum=?";

		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { userAccount, userAccount };// 直接删除所有的
				CYLog.i(TAG, "delete useraccount : " + userAccount);
				db.execSQL(sql, params);
				return true;
			} else {
				CYLog.e(TAG, "eb is not opened!");
				return false;
			}
		} catch (Exception e) {
			CYLog.e(TAG, e.toString());
			return false;
		} finally {
			if (db1 != null) {
				db1.close();
			}
		}
	}

	/**
	 * 更新班级联系人获取情况
	 * 
	 * @param userAccount
	 * @param i
	 */
	public void updateHasAllClassmates(String userAccount, int i) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { i, userAccount };

				db.execSQL(
						"UPDATE UserSelfInfo SET hasAllClassmates=? WHERE  userAccount=?",
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
	 * 本地是否有登陆用户的信息
	 * 
	 * @param userAccount
	 *            登陆用户的账号或者手机号
	 * @return
	 */
	public boolean isSelfContactsEntityExisted(String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		String sql = "SELECT COUNT(*) FROM UserSelfInfo WHERE userAccount=? or phoneNum=?";
		boolean isExisted = false;
		try {
			if (db.isOpen()) {
				String[] params = new String[] { userAccount, userAccount };
				cursor = db.rawQuery(sql, params);

				if (cursor.moveToFirst()) {
					int resultCount = cursor.getInt(0);
					// CYLog.i(TAG, "userAccount " + userAccount + " count: "
					// + resultCount);
					if (resultCount > 0)
						isExisted = true;
				}
				
			} else {
				CYLog.e(TAG, "db is not opened!");
			}
		} catch (Exception e) {
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

	/**
	 * 检查本地获取班级联系人的情况
	 * 
	 * @param userAccount
	 * @return
	 */
	public int hasAllClassmates(String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		try {
			if (db.isOpen()) {
				cursor = db.rawQuery(
						"SELECT * FROM UserSelfInfo WHERE userAccount=?",
						new String[] { userAccount });
				int ret = 0;
				while (cursor.moveToNext()) {// 获取一个联系人的信息
					ret = cursor.getInt(cursor
							.getColumnIndex("hasAllClassmates"));
				}

				return ret;
			} else {
				CYLog.e(TAG, "db not open");
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	/******************************************************/
}