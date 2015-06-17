package com.hust.schoolmatechat.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hust.schoolmatechat.SearchSuggestionProvider;
import com.hust.schoolmatechat.SearchSuggestionProvider.MySQLiteDatabase;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.register.OpenDatabase;

public class DepartmentDao extends BaseDao {
	private static final String TAG = "DepartmentDao";

	public DepartmentDao(Context context) {
		super(context);
	}

	/**
	 * 获取机构全名
	 * 
	 * @param baseInfoId
	 * @return
	 */
	public synchronized String getDepartmentFullName(String baseInfoId) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(
					"SELECT * FROM cy_dept WHERE dept_id=?",
					new String[] { baseInfoId });
			String name = null;
			while (cursor.moveToNext()) {
				// String _id = c.getString(c.getColumnIndex("dept_id"));
				name = cursor.getString(cursor.getColumnIndex("full_name"));
				// CYLog.i("db", "_id=>" + _id + ", name=>" + name);
			}
			return name;
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
	}

	/**
	 * 增加一条机构记录到本地
	 * 
	 * @param baseInfoId
	 * @param departmentFullName
	 */
	public synchronized void addDepartment(String baseInfoId, String departmentFullName) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		try {
			if (db.isOpen()) {
				Object[] params = new Object[] { baseInfoId, departmentFullName };
				db.execSQL(
						"INSERT INTO cy_dept(dept_id,full_name)  VALUES(?,?)",
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
	 * 根据机构名获取机构id
	 * 
	 * @param departmentFullName
	 * @return
	 */
	public synchronized String getDepartmentId(String departmentFullName) {
		MySQLiteDatabase db1 = SearchSuggestionProvider.openDatabase(null);
		SQLiteDatabase db = db1.openDatabase(null);
		Cursor cursor = null;
		try {
			if (db.isOpen()) {
				StringBuffer baseInfoBuf = new StringBuffer();
				baseInfoBuf.append("%").append(departmentFullName).append("%");

				String[] params = new String[] { baseInfoBuf.toString() };
				cursor = db.rawQuery(
						"SELECT * FROM cy_dept WHERE full_name LIKE ?", params);
				String ID = null;
				while (cursor.moveToNext()) {
//					String name = cursor.getString(cursor
//							.getColumnIndex("full_name"));
					ID = cursor.getString(cursor.getColumnIndex("dept_id"));
				}

				return ID;
			} else {
				CYLog.e(TAG, "eb is not opened!");
				return null;
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
	}
}
