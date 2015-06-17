package com.hust.schoolmatechat.db;

import java.util.List;
import java.util.Map;

import android.content.ContentValues;

public interface GetDataFromCheckTable {
	public boolean addCheck(ContentValues values);
	public boolean deleteCheck(String whereClause, String[] whereArgs);
	public boolean updateCheck(ContentValues values,String whereClause,String[] whereArgs);
    public aCheck viewCheck(String selection,String[] selectionArgs);
    public List<Map<String, String>> listAdMaps(String selection,String[] selectionArgs,String column);
}
