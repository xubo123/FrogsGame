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
	 * ����һ����ϵ��
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
	 * ��ȡ���е���ϵ��,���Ѿ��ֺ� userAccount ʹ��app��½���˵��˺�
	 */
	public Map<String, List<ContactsEntity>> getAllcontactsEntity(
			String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		try {
			// ��ȡ��½�û�����Ϣ
			ContactsEntity selfContactsEntity = getSelfContactsEntity(userAccount);
			if (selfContactsEntity == null) {
				CYLog.d(TAG, "local sqlite db no self user info");
				return null;
			}

			// ��ȡ����Ϣ
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
				for (String entry : classSet) {// ȡ����������ϵ��
					cursor = db
							.rawQuery(
									"SELECT * FROM StudentsInfo WHERE userAccount=? and className=?",
									new String[] { userAccount, entry });
					List<ContactsEntity> contactsEntityList = new ArrayList<ContactsEntity>();
					while (cursor.moveToNext()) {// ��ȡһ����ϵ�˵���Ϣ
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
	 * ��ȡ���е���ϵ�� δ���� userAccount ʹ��app��½���˵��˺�
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
				while (cursor.moveToNext()) {// ��ȡһ����ϵ�˵���Ϣ
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
	 * ���ݵ�½app�û����˺�userAccount����ϵ�˵Ļ���id������ϵ����Ϣ
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
				// ����id�޸ģ�id��ʽ���ܴ���
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
	 * ������ϵ����Ϣ��ȡ���
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
	 * ɾ����½�û�����ϵ��
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
				Object[] params = new Object[] { userAccount, accountNum };// ֱ��ɾ�����е�
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
	 * ���ݻ���id������ɾ����¼
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
						baseInfoBuf.toString(), name };// ֱ��ɾ�����е�
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
	 * ��½�û��Ƿ�ӵ��ĳ����ϵ��,�жϱ�׼Ϊ������Ϣid�����û��˺�,baseInfoIds��id�п����ö��Ÿ�����
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
	 * ��½�û��Ƿ�ӵ��ĳ����ϵ��,�жϱ�׼Ϊ������Ϣid�����û��˺�,baseInfoIds��id�п����ö��Ÿ�����
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
	 * �����˺Ż�ȡ������Ϣ��ע��û����֤�İ༶ͬѧ���˺�Ϊnull����ʱ��ȡ��������˵���Ϣ��Ҫͨ��������Ϣid����ȡ
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

				while (cursor.moveToNext()) {// ��ȡһ����ϵ�˵���Ϣ
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
	 * ����������ȡ���ѻ���id
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

				while (cursor.moveToNext()) {// ��ȡһ����ϵ�˵���Ϣ
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
	 * ���ݻ�����Ϣid��ȡ������Ϣ
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

				while (cursor.moveToNext()) {// ��ȡһ����ϵ�˵���Ϣ
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
	 * ��ϵ����Ϣ�Ƿ�����
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
	 * �ж��Ƿ�Ϊ�Լ��ĺ���
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
	 * ��½�û��Ƿ�ӵ��ĳ������
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
	 * �����Ƿ����û�����ϵ����Ϣ
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

	/******************** �����½�û���Ϣ **********************/
	/**
	 * ���Ӹ�����Ϣ
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
	 * ���Ҹ�����Ϣ
	 * 
	 * @param userAccount
	 *            Ϊ�ֻ��Ż����˺�
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
	 * �޸ĵ�½�û��ĸ�����Ϣ
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
	 * ɾ����½�û�����Ϣ
	 * 
	 * @param userAccount
	 */
	public boolean deleteSelfContactsEntity(String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "delete from UserSelfInfo where userAccount=? or phoneNum=?";

		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { userAccount, userAccount };// ֱ��ɾ�����е�
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
	 * ���°༶��ϵ�˻�ȡ���
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
	 * �����Ƿ��е�½�û�����Ϣ
	 * 
	 * @param userAccount
	 *            ��½�û����˺Ż����ֻ���
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
	 * ��鱾�ػ�ȡ�༶��ϵ�˵����
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
				while (cursor.moveToNext()) {// ��ȡһ����ϵ�˵���Ϣ
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