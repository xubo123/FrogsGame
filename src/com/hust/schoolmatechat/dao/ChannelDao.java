package com.hust.schoolmatechat.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.SearchSuggestionProvider;
import com.hust.schoolmatechat.SearchSuggestionProvider.MySQLiteDatabase;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.pushedmsgservice.Channel;

public class ChannelDao extends BaseDao {
	private static final String TAG = "ChannelDao";

	public ChannelDao(Context context) {
		super(context);
	}

	/**
	 * 新增通道信息
	 * 
	 * @param channel
	 */
	public void addChannel(List<Channel> channels) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "INSERT INTO CHANNEL_TABLE(channelId,icon,channelRemark,ChannelName) values(?,?,?,?)";

		try {
			if (db.isOpen()) {
				for (Channel channel : channels) {
					Object[] params = new Object[] { channel.getChannelId(),
							channel.getIcon(), channel.getChannelRemark(),
							channel.getcName() };
					db.execSQL(sql, params);
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
	 * 新增固定通道
	 * 
	 * @param channel
	 */
	public void initChannel() {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String channelID[] = { "母校新闻", "总会快递" };
		String cName[] = { "母校新闻", "总会快递" };
		String channelRemark[] = { "发布母校最新的新闻资讯", "发布校友总会最新动态", };
		String icon[] = {
				APPConstant.getChannelURL1(),						
				APPConstant.getChannelURL2() };
		String sql = "INSERT INTO CHANNEL_TABLE(channelId,icon,channelRemark,ChannelName) values(?,?,?,?)";

		try {
			if (db.isOpen()) {
				for (int i = 0; i < icon.length; i++) {
					Object[] params = new Object[] { channelID[i], icon[i],
							channelRemark[i], cName[i] };
					db.execSQL(sql, params);
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
	 * 更新通道信息
	 * 
	 * @param channel
	 */
	public void updateChannel(List<Channel> channels) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "update channel_table set icon=?,channelRemark=?,ChannelName=? where channelId=?";

		try {
			if (db.isOpen()) {
				for (Channel channel : channels) {
					Object[] params = new Object[] { channel.getIcon(),
							channel.getChannelRemark(), channel.getcName(),
							channel.getChannelId() };
					db.execSQL(sql, params);
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
	 * 先删除全部再新增新数据，存用户订阅的通道
	 * 
	 * @param recvChannels
	 */
	public void deleteAndSave(List<Channel> channels) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "delete from channel_table";
		String savesql = "INSERT INTO CHANNEL_TABLE(channelId,icon,channelRemark,ChannelName) values(?,?,?,?)";

		try {
			if (db.isOpen()) {
				Object[] params = new Object[] {};
				db.execSQL(sql, params);
				db.execSQL("INSERT INTO CHANNEL_TABLE(channelId,ChannelName,channelRemark,icon) VALUES('校友帮帮忙','校友帮帮忙','找人才、找工作、找项目、找资金，……，校友来帮你','"+R.drawable.schoolhelper_main+"')");
				for (Channel channel : channels) {
					Object[] params2 = new Object[] { channel.getChannelId(),
							channel.getIcon(), channel.getChannelRemark(),
							channel.getcName() };
					db.execSQL(savesql, params2);
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
	 * 获取所有订阅通道信息
	 * 
	 * @return
	 */
	public List<Channel> getAllChanenels() {
		List<Channel> channelList = new ArrayList<Channel>();
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "select * from channel_table";
		Cursor cursor = null;

		try {
			if (db.isOpen()) {
				cursor = db.rawQuery(sql, new String[] {});
				while (cursor.moveToNext()) {
					Channel channel = new Channel();
					channel.setChannelId(cursor.getString(cursor
							.getColumnIndex("ChannelID")));
					channel.setChannelRemark(cursor.getString(cursor
							.getColumnIndex("channelRemark")));
					channel.setIcon(cursor.getString(cursor
							.getColumnIndex("Icon")));
					channel.setcName(cursor.getString(cursor
							.getColumnIndex("ChannelName")));
					channelList.add(channel);
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

		return channelList;
	}

	/**
	 * 先删除全部再新增新数据,存所有通道
	 * 
	 * @param recvChannels
	 */
	public void deleteAndSaveALL(List<Channel> channels) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "delete from allchannel_table";
		String savesql = "INSERT INTO ALLCHANNEL_TABLE(channelId,icon,channelRemark,ChannelName) values(?,?,?,?)";

		try {
			if (db.isOpen()) {
				Object[] params = new Object[] {};
				db.execSQL(sql, params);
				for (Channel channel : channels) {
					Object[] params2 = new Object[] { channel.getChannelId(),
							channel.getIcon(), channel.getChannelRemark(),
							channel.getcName() };
					db.execSQL(savesql, params2);
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
	 * 获取所有通道信息
	 * 
	 * @return
	 */
	public List<Channel> getChanenelsALL() {
		List<Channel> channelList = new ArrayList<Channel>();
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		String sql = "select * from allchannel_table";
		Cursor cursor = null;

		try {
			if (db.isOpen()) {
				cursor = db.rawQuery(sql, new String[] {});
				while (cursor.moveToNext()) {
					Channel channel = new Channel();
					channel.setChannelId(cursor.getString(cursor
							.getColumnIndex("ChannelID")));
					channel.setChannelRemark(cursor.getString(cursor
							.getColumnIndex("channelRemark")));
					channel.setIcon(cursor.getString(cursor
							.getColumnIndex("Icon")));
					channel.setcName(cursor.getString(cursor
							.getColumnIndex("ChannelName")));
					channelList.add(channel);
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

		return channelList;
	}
}
