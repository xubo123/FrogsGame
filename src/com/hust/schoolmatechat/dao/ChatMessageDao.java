package com.hust.schoolmatechat.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hust.schoolmatechat.SearchSuggestionProvider;
import com.hust.schoolmatechat.SearchSuggestionProvider.MySQLiteDatabase;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.engine.ChatMessage;
import com.hust.schoolmatechat.pushedmsgservice.SingleNewsMessage;
import com.hust.schoolmatechat.utils.DateFormatUtils;

public class ChatMessageDao extends BaseDao {
	private static final String TAG = "ChatMessageDao";
	private SingleNewsMessageDao newsDao = null;

	public ChatMessageDao(Context context) {
		super(context);
		this.newsDao = new SingleNewsMessageDao(context);
	}

	/** 添加一条ChatMessage */
	public void addChatMessage(ChatMessage chatMessage) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { chatMessage.getMid(),
						chatMessage.getIcon(), chatMessage.getType(),
						chatMessage.getUserAccount(),
						chatMessage.getSenderAccount(),
						chatMessage.getRecvAccount(),
						DateFormatUtils.date2String(chatMessage.getTime()),
						chatMessage.getMessageContent(),
						chatMessage.getOwner(), chatMessage.getIsRead() };
				db.execSQL(
						"INSERT INTO chatmessage(mid,icon,mType,userAccount,senderAccount,recvAccount,mTime,messageContent,owner,isRead) VALUES(?,?,?,?,?,?,?,?,?,?)",
						params);

				if (chatMessage.getNewsList() != null
						&& chatMessage.getNewsList().size() > 0) {
					for (SingleNewsMessage newsMessage : chatMessage
							.getNewsList()) {
						// if(!newsDao.isSingleNewsExist(newsMessage.getNid()))
						newsDao.addSingleNewsMessage(newsMessage);
					}
				}
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
	 * 根据属主，登陆用户和消息类型获取未读信息
	 * 
	 * @return
	 */
	public List<ChatMessage> getUnreadChatMessage(String owners, String userAccount,
			int messageType) {
		List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		Cursor cursor1 = null;

		StringBuffer sql1 = new StringBuffer();
		sql1.append("select * from chatmessage where isRead=0")
				.append(" and owner=").append("'"+owners+"'")
				.append(" and mType=").append(messageType)
				.append(" and userAccount=").append(userAccount)
				.append(" order by id desc");

		try {
			if (db.isOpen()) {
				cursor = db.rawQuery(sql1.toString(), new String[] {});
				while (cursor.moveToNext()) {
					ChatMessage chatMessage = getChatMessageFromDBCursor(cursor);
					if (chatMessage != null) {
						chatMessageList.add(chatMessage);
					}
				}

				if (messageType == ChatMessage.NEWSMESSAGE) {
					for (ChatMessage chatMessage : chatMessageList) {
						String sql2 = "select * from singleNewsMessage where PMId='"
								+ chatMessage.getMid() + "'";
						cursor1 = db.rawQuery(sql2, new String[] {});
						List<SingleNewsMessage> singleNewsMessageList = new ArrayList<SingleNewsMessage>();
						while (cursor1.moveToNext()) {
							SingleNewsMessage singleNewsMessage = getSingleNewsFromDBCursor(cursor1);
							if (singleNewsMessage != null) {
								singleNewsMessageList.add(singleNewsMessage);
							}
						}

						chatMessage.setNewsList(singleNewsMessageList);
					}
				}
			} else {
				CYLog.e(TAG, "eb is not opened!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			if (cursor1 != null) {
				cursor1.close();
			}
			if (db1 != null) {
				db1.close();
			}
		}

		return chatMessageList;
	}

	/**
	 * 根据属主、登陆用户和指定的where限定条件inputType查询聊天的未读信息
	 * 
	 * @return
	 */
	public List<ChatMessage> getInputTypeChatMessage(String owner,
			String userAccount, String inputType) {
		List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;

		StringBuffer sql1 = new StringBuffer();
		sql1.append("select * from chatmessage where isRead=0")
				.append(" and owner='").append(owner).append("') ")
				.append("and mType=").append(inputType)
				.append(" and userAccount=").append(userAccount)
				.append(" order by id desc");

		try {
			if (db.isOpen()) {
				cursor = db.rawQuery(sql1.toString(), new String[] {});
				while (cursor.moveToNext()) {
					ChatMessage chatMessage = getChatMessageFromDBCursor(cursor);
					if (chatMessage != null) {
						chatMessageList.add(chatMessage);
					}
				}
			} else {
				CYLog.e(TAG, "eb is not opened!");
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

		return chatMessageList;
	}

	/**
	 * 取已读消息
	 * 
	 * @param messageType
	 * @param start
	 * @return
	 */
	public List<ChatMessage> getReadChatMessage(String userAccount,
			int messageType, int start, int rows) {
		List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
		List<SingleNewsMessage> singleNewsMessageList = new ArrayList<SingleNewsMessage>();
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		Cursor cursor1 = null;

		String sql1 = "select * from chatmessage where isRead=1 and userAccount="
				+ userAccount;
		if (messageType == ChatMessage.NEWSMESSAGE) {
			sql1 += " and mType=1";
		} else {
			sql1 += " and mType!=1";
		}
		sql1 += " order by id desc" + " limit " + rows + " offset " + start;
		String sql2 = "select * from singleNewsMessage where userAccount="
				+ userAccount;

		try {
			if (db.isOpen()) {
				cursor = db.rawQuery(sql1, new String[] {});
				while (cursor.moveToNext()) {
					ChatMessage chatMessage = getChatMessageFromDBCursor(cursor);
					if (chatMessage != null) {
						chatMessageList.add(chatMessage);
					}
				}

				cursor1 = db.rawQuery(sql2, new String[] {});
				while (cursor1.moveToNext()) {
					SingleNewsMessage singleNewsMessage = getSingleNewsFromDBCursor(cursor1);
					if (singleNewsMessage != null) {
						singleNewsMessageList.add(singleNewsMessage);
					}
				}

				for (ChatMessage chatMessage : chatMessageList) {
					List<SingleNewsMessage> newsList = new ArrayList<SingleNewsMessage>();
					for (SingleNewsMessage singleNewsMessage : singleNewsMessageList) {
						if (chatMessage.getMid().equals(
								singleNewsMessage.getPMId())) {
							newsList.add(singleNewsMessage);
						}
					}
					chatMessage.setNewsList(newsList);
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
			if (cursor1 != null) {
				cursor1.close();
			}
			if (db1 != null) {
				db1.close();
			}
		}

		return chatMessageList;
	}

	/**
	 * 取已读消息
	 * 
	 * @param messageType
	 * @param start
	 * @return
	 */
	public List<ChatMessage> getReadChatMessage(String Owner,
			String userAccount, int messageType, int start, int rows) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		List<ChatMessage> chatMessages = null;
		chatMessages = new ArrayList<ChatMessage>();
		try {
			String sql = "";
			if (messageType == ChatItem.SCHOOLHELPERITEM) {
				//sql = "SELECT * FROM chatmessage WHERE isRead=1 and owner=? AND mType=?";
				return getSchoolHelperMessages(ChatMessage.SCHOOLHELPER);
			} else {
				sql = "SELECT * FROM chatmessage WHERE isRead=1 and owner=? AND userAccount=? AND mType=?";
			}
			if (rows != 0) {
				sql += " order by id desc" + " limit " + rows + " offset "
						+ start;
			} else
				sql += " order by id desc";//用mtime排序，会有同一秒入库的，排序就会乱
			if (db.isOpen()) {
				cursor = db.rawQuery(sql, new String[] { Owner,
						userAccount, "" + messageType });
				while (cursor.moveToNext()) {
					ChatMessage chatMessage = getChatMessageFromDBCursor(cursor);
					if (chatMessage != null
							&& messageType == ChatMessage.NEWSMESSAGE) {
						List<SingleNewsMessage> newsList = newsDao
								.getSingleNewsMessagesByPMId(chatMessage
										.getMid());
						if (newsList != null) {
							chatMessage.setNewsList(newsList);
						} else {
							CYLog.d(TAG, "newsList == null");
						}
					}
					if (chatMessage != null) {
						chatMessages.add(chatMessage);
//						CYLog.e(TAG, ",chatMessage="
//								+ chatMessage.getMessageContent());
					}
				}
			} else {
				CYLog.e(TAG, "eb is not opened!");
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
		if (chatMessages == null) {
			CYLog.e(TAG, "chatMessages==null");
		}
		return chatMessages;

	} // end of function

	/**
	 * 从数据库cursor获取到一条SingleNewsMessage
	 * 
	 * @param cursor
	 * @return
	 */
	private SingleNewsMessage getSingleNewsFromDBCursor(Cursor cursor) {
		try {
			SingleNewsMessage singleNewsMessage = new SingleNewsMessage();
			singleNewsMessage
					.setNid(cursor.getInt(cursor.getColumnIndex("nid")));
			singleNewsMessage.setIcon(cursor.getString(cursor
					.getColumnIndex("icon")));
			int breaking = cursor.getInt(cursor.getColumnIndex("isBreaking"));
			if (breaking == 1) {
				singleNewsMessage.setBreaking(true);
			} else {
				singleNewsMessage.setBreaking(false);
			}
			singleNewsMessage.setTitle(cursor.getString(cursor
					.getColumnIndex("title")));
			singleNewsMessage.setSummary(cursor.getString(cursor
					.getColumnIndex("summary")));
			String smTime = cursor.getString(cursor.getColumnIndex("smTime"));
			if (smTime != null && !"".equals(smTime)) {
				singleNewsMessage.setTime(simpleDateFormat.parse(smTime));
			}
			singleNewsMessage.setNewsUrl(cursor.getString(cursor
					.getColumnIndex("newsUrl")));
			singleNewsMessage.setChannelId(cursor.getString(cursor
					.getColumnIndex("channelId")));
			singleNewsMessage.setPMId(cursor.getString(cursor
					.getColumnIndex("PMId")));
			singleNewsMessage.setContent(cursor.getString(cursor
					.getColumnIndex("content")));
			return singleNewsMessage;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 从数据库cursor获取到一条ChatMessage
	 * 
	 * @param cursor
	 * @return
	 */
	private ChatMessage getChatMessageFromDBCursor(Cursor cursor) {
		try {
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setMid(cursor.getString(cursor.getColumnIndex("mid")));
			chatMessage
					.setIcon(cursor.getString(cursor.getColumnIndex("icon")));
			chatMessage.setType(cursor.getInt(cursor.getColumnIndex("mType")));
			chatMessage.setUserAccount(cursor.getString(cursor
					.getColumnIndex("userAccount")));
			chatMessage.setSenderAccount(cursor.getString(cursor
					.getColumnIndex("senderAccount")));
			chatMessage.setRecvAccount(cursor.getString(cursor
					.getColumnIndex("recvAccount")));
			String mTime = cursor.getString(cursor.getColumnIndex("mTime"));
			if (mTime != null && !"".equals(mTime)) {
				chatMessage.setTime(simpleDateFormat.parse(mTime));
			}
			chatMessage.setMessageContent(cursor.getString(cursor
					.getColumnIndex("messageContent")));
			chatMessage.setOwner(cursor.getString(cursor
					.getColumnIndex("owner")));
			chatMessage
					.setIsRead(cursor.getInt(cursor.getColumnIndex("isRead")));
			chatMessage.setSendSucc(true);// 数据库取出的消息都是成功
			return chatMessage;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 批量更新,将信息设为已读
	 */
	public void update(List<ChatMessage> chatMessageList) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql1 = "update chatmessage set isRead=1 where mid=? and userAccount=?";

		try {
			if (db.isOpen()) {
				for (ChatMessage chatMessage : chatMessageList) {
					Object[] params = new Object[] { chatMessage.getMid(),
							chatMessage.getUserAccount() };
					db.execSQL(sql1, params);
				}
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
	 * 根据消息属主和登陆app的用户账号获取信息
	 * 
	 * @param owner
	 * @param userAccount
	 * @return
	 */
	public List<ChatMessage> getChatMessagesByOwner(String owner,
			String userAccount) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		List<ChatMessage> chatMessages = null;
		chatMessages = new ArrayList<ChatMessage>();
		try {
			if (db.isOpen()) {
				cursor = db
						.rawQuery(
								"SELECT * FROM chatmessage WHERE owner=? AND userAccount=? ORDER BY id ASC",
								new String[] { owner, userAccount });
				while (cursor.moveToNext()) {
					ChatMessage chatMessage = getChatMessageFromDBCursor(cursor);
					if (chatMessage != null) {
						List<SingleNewsMessage> newsList = null;
						if (chatMessage.getType() == ChatMessage.NEWSMESSAGE) {
							newsList = newsDao
									.getSingleNewsMessagesByPMId(chatMessage
											.getMid());
							chatMessage.setNewsList(newsList);
						}

						chatMessages.add(chatMessage);
					}
				}
			} else {
				CYLog.e(TAG, "eb is not opened!");
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
		if (chatMessages == null) {
			CYLog.e(TAG, "chatMessages==null");
		}
		return chatMessages;
	}

	/**
	 * 根据消息属主、登陆app的用户账号、信息类型获取所有历史信息，包括已读和未读 限制去前10000条信息内
	 * 
	 * @param owner
	 * @param userAccount
	 * @param messageType
	 * @return
	 */
	public List<ChatMessage> getChatMessages(String owner, String userAccount,
			int messageType) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		List<ChatMessage> chatMessages = null;
		chatMessages = new ArrayList<ChatMessage>();
		try {
			if (db.isOpen()) {
				cursor = db
						.rawQuery(
								"SELECT * FROM chatmessage WHERE owner=? AND userAccount=? AND mType=? ORDER BY id ASC limit 0,10000",
								new String[] { owner, userAccount,
										"" + messageType });
				while (cursor.moveToNext()) {
					ChatMessage chatMessage = getChatMessageFromDBCursor(cursor);
					if (chatMessage != null
							&& messageType == ChatMessage.NEWSMESSAGE) {
						List<SingleNewsMessage> newsList = newsDao
								.getSingleNewsMessagesByPMId(chatMessage
										.getMid());
						if (newsList != null) {
							chatMessage.setNewsList(newsList);
						} else {
							CYLog.d(TAG, "newsList == null");
						}
					}
					if (chatMessage != null) {
						chatMessages.add(chatMessage);
					}
				}
			} else {
				CYLog.e(TAG, "eb is not opened!");
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
		if (chatMessages == null) {
			CYLog.e(TAG, "chatMessages==null");
		}
		return chatMessages;
	}
	
	/**
	 * 删除ChatMessage
	 */
	public void deleteChatMessages(String owner, String userAccount,
			int messageType) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "delete from chatmessage where owner=? and userAccount=? and mType=?";
		
		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { owner, userAccount, messageType };
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
	 * 去校友帮帮忙信息
	 * 
	 * @param owner
	 * @param messageType
	 * @return
	 */
	public List<ChatMessage> getSchoolHelperMessages(int messageType) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		List<ChatMessage> chatMessages = null;
		Cursor cursor = null;
		chatMessages = new ArrayList<ChatMessage>();
		try {
			if (db.isOpen()) {
				cursor = db.rawQuery(
						"SELECT * FROM chatmessage WHERE mType=? order by id desc",
						new String[] { "" + messageType });
				while (cursor.moveToNext()) {
					ChatMessage chatMessage = getChatMessageFromDBCursor(cursor);
					if (chatMessage != null) {
						chatMessages.add(chatMessage);
					}
				}

			} else {
				CYLog.e(TAG, "eb is not opened!");
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
		if (chatMessages == null) {
			CYLog.e(TAG, "chatMessages==null");
		}
		return chatMessages;
	}

	public void checkDataFromDB(String owner, String userAccount,
			int messageType) {
		// TODO Auto-generated method stub
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		final int Size=5;
		String sql = "SELECT COUNT (*) FROM chatmessage WHERE owner=? AND userAccount=? AND mType=?  ";
		int resultCount = 0;
		try {
			if (db.isOpen()) {
				cursor = db.rawQuery(sql, new String[] { owner, userAccount,
						"" + messageType });

				if (cursor.moveToFirst()) {
				 resultCount = cursor.getInt(0);
//					CYLog.i(TAG, "userAccount " + userAccount + " count: "
//							+ resultCount);
				}
			} 
		    if(resultCount>1000){
			String sql1="delete from chatmessage WHERE mid IN (select mid from chatmessage WHERE owner=? AND userAccount=? AND mType=? order by mTime asc limit 0,"+Size+")";
				if (db.isOpen()) {
                db.execSQL(sql1, new String[] { owner, userAccount,
							"" + messageType });
				} else {
					CYLog.e(TAG, "db is not opened!");
				}
		    }
		}
			 catch (Exception e) {
				CYLog.e(TAG, e.toString());
			} finally {
				if (cursor != null) {
					cursor.close();
				}
				if (db1 != null) {
					db1.close();
				}
			}
		}
	}
	

