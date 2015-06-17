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
import com.hust.schoolmatechat.pushedmsgservice.SingleNewsMessage;
import com.hust.schoolmatechat.utils.DateFormatUtils;

public class SingleNewsMessageDao extends BaseDao {
	private static final String TAG = "SingleNewsMessageDao";

	public SingleNewsMessageDao(Context context) {
		super(context);
	}

	public void addSingleNewsMessage(SingleNewsMessage newsMessage) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);

		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { null, newsMessage.getIcon(),
						newsMessage.isBreaking() ? 1 : 0,
						newsMessage.getTitle(), newsMessage.getSummary(),
						DateFormatUtils.date2String(newsMessage.getTime()),
						newsMessage.getNewsUrl(), newsMessage.getChannelId(),
						newsMessage.getPMId(), newsMessage.getContent() };
				db.execSQL(
						"INSERT INTO singlenewsmessage(nid,icon,isBreaking,title,summary,smTime,newsUrl,channelId,PMId,content) VALUES(?,?,?,?,?,?,?,?,?,?)",
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

	public List<SingleNewsMessage> getSingleNewsMessagesByPMId(String PMId) {
		List<SingleNewsMessage> newsMessages = null;
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;

		try {
			if (db.isOpen()) {
				cursor = db.rawQuery(
						"SELECT * FROM singlenewsmessage WHERE PMId=?",
						new String[] { PMId });
				newsMessages = new ArrayList<SingleNewsMessage>();

				while (cursor.moveToNext()) {
					int nid = cursor.getInt(cursor.getColumnIndex("nid"));
					String icon = cursor.getString(cursor
							.getColumnIndex("icon"));
					boolean isBreaking = cursor.getInt(cursor
							.getColumnIndex("isBreaking")) == 1 ? true : false;
					String title = cursor.getString(cursor
							.getColumnIndex("title"));
					String summary = cursor.getString(cursor
							.getColumnIndex("summary"));
					String timeStr = cursor.getString(cursor
							.getColumnIndex("smTime"));
					if (timeStr != null && !timeStr.trim().equals(""))
						;
					Date time = DateFormatUtils.string2Date(timeStr);
					String newsUrl = cursor.getString(cursor
							.getColumnIndex("newsUrl"));
					String channelId = cursor.getString(cursor
							.getColumnIndex("channelId"));
					String content = cursor.getString(cursor
							.getColumnIndex("content"));
					
					SingleNewsMessage newsMessage = new SingleNewsMessage(nid,
							icon, isBreaking, title, summary, time, newsUrl,
							channelId, PMId, content);
					newsMessages.add(newsMessage);

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

		return newsMessages;
	}

	public boolean isSingleNewsExist(int nid) {
		boolean flag = false;
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;

		try {
			if (db.isOpen()) {
				cursor = db.rawQuery(
						"SELECT title FROM singlenewsmessage WHERE nid=?",
						new String[] { nid + "" });

				if (cursor.moveToFirst()) {
					flag = true;
				}
			} else {
				CYLog.e(TAG, "eb is not opened!");
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

		return flag;
	}

}
