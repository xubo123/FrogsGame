/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hust.schoolmatechat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;

/**
 * Provides access to the dictionary database.
 */
public class SearchSuggestionProvider extends ContentProvider {
	private static final String TAG = "SerchSugeestionPrioder";
	public static String AUTHORITY = "com.hust.schoolmatechat.SearchSuggestionProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/suggestion");

	// ·��ƥ��ɹ����
	private static final int SEARCH_SUGGEST = 2;

	// �������ݿ�������
	// final static String filepath = "data/data/com.hust.schoolmatechat/" +
	// APPConstant.getCYDBNAME();
	// final static String pathStr = "data/data/com.hust.schoolmatechat";
	public final static String pathStr = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "chuangyou" + File.separator + "data";
	final static String filepath = pathStr + File.separator
			+ APPConstant.getCYDBNAME();
	
	final static String COLUMN_0 = "SchoolfellowIDs"; // ���ݿ�������
	final static String COLUMN_1 = "Name"; // ���ݿ�������

	private static final UriMatcher sURIMatcher = buildUriMatcher();

	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
				SEARCH_SUGGEST);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*",
				SEARCH_SUGGEST);
		return matcher;
	}

	@Override
	public boolean onCreate() {
		CYLog.i("Provider", "is created!");
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		try {
			CYLog.i("Provider", "query is called!");
			switch (sURIMatcher.match(uri)) {
			case SEARCH_SUGGEST:
				if (selectionArgs == null) {
					CYLog.e(TAG, "selectionArgs must be provided for the Uri: "
							+ uri);
				}
				return getSuggestions(selectionArgs[0]);
			default:
				CYLog.e(TAG, "Unknown Uri: " + uri);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.i(TAG, e.toString());
			return null;
		}
	}

	private Cursor getSuggestions(String query) {
		CYLog.i("Provider", "getSuggestion is called!");
		query = query.toLowerCase();
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		CYLog.i(TAG, "insert");
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		CYLog.i(TAG, "delete");
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		CYLog.i(TAG, "update");
		return 0;
	}

	private static int sqlite_db_conn_size = 10;
	public static void init() {
		File jhPath = new File(filepath);
		// �鿴���ݿ��ļ��Ƿ����
		if (jhPath.exists()) {
			for (int i = 0; i < sqlite_db_conn_size; i++) {
				SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(jhPath,
						null);
				DataCenterManagerService.dbList.add(db);
			}
		} else {
			// �������ȴ����ļ���
			File path = new File(pathStr);
			if (path.mkdir()) {
				CYLog.i(TAG, "�����ɹ�");
			} else {
				CYLog.i(TAG, "����ʧ��");
			}
			try {
				// �õ���Դ
				AssetManager am = SchoolMateChat.getInstance().getAssets();
				// �õ����ݿ��������
				InputStream is = am.open(APPConstant.getCYDBNAME());
				// �������д��SDcard����
				FileOutputStream fos = new FileOutputStream(jhPath);
				// ����byte���� ����1KBдһ��
				byte[] buffer = new byte[1024];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				// ���رվͿ�����
				fos.flush();
				fos.close();
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				CYLog.e(TAG, e);
				return;
			}
			// ���û��������ݿ� �����Ѿ�����д��SD�����ˣ�Ȼ����ִ��һ��������� �Ϳ��Է������ݿ���
			SQLiteDatabase db1 = null;
			SQLiteDatabase db2 = null;
			for (int i = 0; i < sqlite_db_conn_size; i++) {
				SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(jhPath,
						null);
				if (db1 == null) {
					db1 = db;
				} else {
					db2 = db;
					if (db1 == db2 || db1.equals(db2)) {
						System.out.println("equals");
					}
				}
				DataCenterManagerService.dbList.add(db);
			}
		}

	}

	static class SearchSuggestionProviderSingletonHolder {
		static SearchSuggestionProvider instance = new SearchSuggestionProvider();
	}

	public static SearchSuggestionProvider getInstance() {
		return SearchSuggestionProviderSingletonHolder.instance;
	}

	public static MySQLiteDatabase openDatabase(Context context) {
		try {
			synchronized (DataCenterManagerService.dbList) {
				if (DataCenterManagerService.dbList == null) {
					SearchSuggestionProvider.init();
				}
				// ��ȡ�Ӽ������Ƴ���Connection����, �������װΪMyConnection����󷵻�.
				SQLiteDatabase db = DataCenterManagerService.dbList.remove(0);
				if (db == null) {
					File jhPath = new File(filepath);
					db = SQLiteDatabase.openOrCreateDatabase(jhPath,
							null);
					if (db == null) {
						CYLog.e(TAG, "can't create db here--please check error");
						return null;
					}
				}
				return SearchSuggestionProvider.getInstance().MySQLiteDatabase(
						db, DataCenterManagerService.dbList);
			}
		} catch (Exception e) {
			CYLog.e(TAG, e);
			return null;
		}
	}

	private MySQLiteDatabase MySQLiteDatabase(SQLiteDatabase db,
			List<SQLiteDatabase> dbList) {
		return new MySQLiteDatabase(db, dbList);
	}

	public class MySQLiteDatabase {
		private SQLiteDatabase db;
		private List<SQLiteDatabase> dbList;

		public MySQLiteDatabase(SQLiteDatabase db, List<SQLiteDatabase> dbList) {
			this.db = db;
			this.dbList = dbList;
		}

		public SQLiteDatabase openDatabase(Context context) {
			return db;
		}

		public void close() {
			dbList.add(db);
		}
	}
}
