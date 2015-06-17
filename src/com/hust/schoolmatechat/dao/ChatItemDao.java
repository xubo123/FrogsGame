package com.hust.schoolmatechat.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hust.schoolmatechat.SearchSuggestionProvider;
import com.hust.schoolmatechat.SearchSuggestionProvider.MySQLiteDatabase;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.utils.DateFormatUtils;

public class ChatItemDao extends BaseDao {
	private static final String TAG = "ChatItemDao";

	public ChatItemDao(Context context) {
		super(context);
	}

	public void addChatItem(ChatItem chatItem) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { chatItem.getOwner(),
						chatItem.getIcon(), chatItem.getName(), chatItem.getType(),						
						chatItem.getFriendAccount(),
						DateFormatUtils.date2String(chatItem.getTime()),
						chatItem.getLatestMessage(), chatItem.getUnread(),
						chatItem.getUserAccount() };

				db.execSQL(
						"INSERT INTO chatitem(owner,icon,name,cType,friendAccount,cTime,latestMessage,unread,userAccount) VALUES(?,?,?,?,?,?,?,?,?)",
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

	public List<ChatItem> getAllChatItems(String userAccount) {
		List<ChatItem> chatItems = null;
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		try {
			if (db.isOpen()) {
				cursor = db
						.rawQuery(
								"SELECT * FROM chatitem WHERE userAccount=? ORDER BY cTime DESC Limit 0,100",
								new String[] { userAccount });
				chatItems = new ArrayList<ChatItem>();

				while (cursor.moveToNext()) {
					String cid = cursor.getString(cursor.getColumnIndex("owner"));
					String icon = cursor.getString(cursor
							.getColumnIndex("icon"));
					String name = cursor.getString(cursor
							.getColumnIndex("name"));
					int type = cursor.getInt(cursor.getColumnIndex("cType"));
					String friendAccount = cursor.getString(cursor
							.getColumnIndex("friendAccount"));
					String timeStr = cursor.getString(cursor
							.getColumnIndex("cTime"));
					Date time = new Date();
					if (timeStr != null && !timeStr.trim().equals("")) {
						time = DateFormatUtils.string2Date(timeStr);
					}
					String latestMessage = cursor.getString(cursor
							.getColumnIndex("latestMessage"));
					int unread = cursor.getInt(cursor.getColumnIndex("unread"));

					ChatItem chatItem = new ChatItem(cid, icon, type, name, friendAccount,
							time, latestMessage, unread, userAccount);
					chatItems.add(chatItem);

				}
				
			} else {
				CYLog.e(TAG, "eb is not opened!");
			}
		} catch (Exception e) {
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

		return chatItems;

	}

	/*
	 * 更新消息选项卡条目
	 */
	public void updateChatItem(ChatItem chatItem) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { chatItem.getIcon(), chatItem.getName(),
						chatItem.getType(), chatItem.getFriendAccount(),
						DateFormatUtils.date2String(chatItem.getTime()),
						chatItem.getLatestMessage(), chatItem.getUnread(),
						chatItem.getOwner(), chatItem.getUserAccount() };
				
				db.execSQL(
						"UPDATE chatitem SET icon=?,name=?,cType=?,friendAccount=?,cTime=?,latestMessage=?,unread=? WHERE owner=? AND userAccount=?",
						params);
			} else {
				CYLog.e(TAG, "updateChatItem db is not opened!");
			}
		} catch (Exception e) {
			CYLog.e(TAG, e.toString());
		} finally {
			if (db1 != null) {
				db1.close();
			}
		}
	}

	public void deleteAndSave(ChatItem chatItem) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "delete from chatitem where owner=? and userAccount=?";
		String savesql = "INSERT INTO chatitem(owner,icon,name,cType,friendAccount,cTime,latestMessage,unread,userAccount) values(?,?,?,?,?,?,?,?,?)";

		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { chatItem.getOwner(), chatItem.getUserAccount() };//直接删除原有的那条记录
				db.execSQL(sql, params);

				Object[] params2 = new Object[] { chatItem.getOwner(),
						chatItem.getIcon(), chatItem.getName(), chatItem.getType(),
						chatItem.getFriendAccount(),
						DateFormatUtils.date2String(chatItem.getTime()),
						chatItem.getLatestMessage(), chatItem.getUnread(),
						chatItem.getUserAccount() };
				db.execSQL(savesql, params2);
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
	
	//删除ChatItem
	public void deleteChatItem(String owner, String userAccount, int ctype) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "delete from chatitem where owner=? and userAccount=? and cType=?";

		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { owner, userAccount, ctype };//直接删除原有的那条记录
				db.execSQL(sql, params);
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
	 * 登陆app的人不同，该方法需要带上登陆者账号作为参数
	 * @param userAccount
	 * @return
	 */
	public List<ChatItem> getAllChatItem(String userAccount) {
		List<ChatItem> ChatItemList = new ArrayList<ChatItem>();
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "select * from chatitem WHERE userAccount=? order by cTime desc limit 0,100";
		Cursor cursor = null;
		try {
			if (db.isOpen()) {
				cursor = db.rawQuery(sql, new String[] {userAccount});
				while (cursor.moveToNext()) {
					ChatItem chatItem = new ChatItem();
					chatItem.setUserAccount(userAccount);
					chatItem.setOwner(cursor.getString(cursor
							.getColumnIndex("owner")));
					chatItem.setIcon(cursor.getString(cursor
							.getColumnIndex("icon")));
					chatItem.setName(cursor.getString(cursor
							.getColumnIndex("name")));
					chatItem.setLatestMessage(cursor.getString(cursor
							.getColumnIndex("latestMessage")));
					chatItem.setFriendAccount(cursor.getString(cursor
							.getColumnIndex("friendAccount")));
					String cTime = cursor.getString(cursor
							.getColumnIndex("cTime"));
					if (cTime != null && !"".equals(cTime)) {
						chatItem.setTime(simpleDateFormat.parse(cTime));
					}
					chatItem.setType(cursor.getInt(cursor
							.getColumnIndex("cType")));
					chatItem.setUnread(cursor.getInt(cursor
							.getColumnIndex("unread")));
					ChatItemList.add(chatItem);
				}
				
			} else {
				CYLog.e(TAG, "eb is not opened!");
			}
		} catch (Exception e) {
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

		return ChatItemList;
	}

}
