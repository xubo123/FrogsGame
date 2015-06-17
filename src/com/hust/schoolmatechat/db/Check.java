package com.hust.schoolmatechat.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hust.schoolmatechat.engine.CYLog;

public class Check implements GetDataFromCheckTable {
	private static final String TAG = "Check";
	private DbOpenHelper helper=null;
	public Check(Context context) {
		// TODO Auto-generated constructor stub
		helper=new DbOpenHelper(context);
	}

	@Override
	public boolean addCheck(ContentValues values) {
		// TODO Auto-generated method stub
		boolean flag=false;
		SQLiteDatabase database=null;
		long id=-1;
		try {
			database=helper.getWritableDatabase();
			id=database.insert("Check_Table", null, values);
			flag=(id!=-1?true:false);

		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			if(database!=null){
			database.close();
			}
			}
		return flag;
	}

	@Override
	public boolean deleteCheck(String whereClause, String[] whereArgs) {
		// TODO Auto-generated method stub
		boolean flag=false;
		SQLiteDatabase database=null;
		int count=0;
		try {
			database=helper.getWritableDatabase();
			count=database.delete("Check_Table", whereClause, whereArgs);
			flag=(count>0?true:false);

		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			if(database!=null){
			database.close();
			}
			}
		return flag;
	}

	@Override
	public boolean updateCheck(ContentValues values, String whereClause,
			String[] whereArgs) {
		// TODO Auto-generated method stub
		boolean flag=false;
		SQLiteDatabase database=null;
		int count=0;
		try {
			database=helper.getWritableDatabase();
			count=database.update("Check_Table", values, whereClause, whereArgs);
			flag=(count>0?true:false);

		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			if(database!=null){
			database.close();
			}
			}
		return flag;
	}

	@Override
	public aCheck viewCheck(String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		 aCheck acheck=new aCheck();
			SQLiteDatabase database=null;
			Cursor cursor=null;
			Map<String, Object> map=new HashMap<String, Object>();
			try {
				database=helper.getWritableDatabase();
				cursor=database.query(true, "Check_Table", null, selection, selectionArgs, null, null, null, null);
		       int cols_len=cursor.getColumnCount();
				while(cursor.moveToNext())
		        {
		        	for(int i=0;i<cols_len;i++)
		        	{
		        		String cols_name=cursor.getColumnName(i);
		        		String cols_values=cursor.getString(cursor.getColumnIndex(cols_name));
		        		if(cols_values==null){
		        			cols_values="";
		        		}
		        		map.put(cols_name, cols_values);
		        	}
		        }

			} catch (Exception e) {
				// TODO: handle exception
			}finally{
				if(database!=null){
				database.close();
				}
				}
//			CYLog.e("===",""+map.get("IfChecked"));
//			CYLog.e("===",""+map.get("IfPassed"));
//			CYLog.e("===",""+map.get("Name"));
//			CYLog.e("===",""+map.get("IfPassed"));
		   CYLog.i(TAG,"View:"+ map.get("Name"));
           acheck.setIfChecked(Integer.parseInt((String)map.get("IfChecked")));
           acheck.setIfPassed(Integer.parseInt( (String)map.get("IfPassed")));
           acheck.setName((String)map.get("Name"));
          

			return acheck;
	}

	@Override
	public List<Map<String, String>> listAdMaps(String selection,
			String[] selectionArgs, String column) {
		// TODO Auto-generated method stub
		 List<Map<String, String>> list=new ArrayList<Map<String,String>>();
			SQLiteDatabase database=null;
			Cursor cursor=null;
			Map<String, String> map=new HashMap<String, String>();
			try {
				database=helper.getReadableDatabase();
				cursor=database.query(true, "Check_Table", null, selection, selectionArgs, null, null, null, null);
		       int cols_len=cursor.getColumnCount();
				while(cursor.moveToNext())
		        {
		        	for(int i=0;i<cols_len;i++)
		        	{
		        		String cols_name=cursor.getColumnName(i);
		        		String cols_values=cursor.getString(cursor.getColumnIndex(cols_name));
		        		if(cols_values==null){
		        			cols_values="";
		        		}
		        		map.put(cols_name, cols_values);
		        	}
		        	list.add(map);
		        }

			} catch (Exception e) {
				// TODO: handle exception
			}finally{
				if(database!=null){
				database.close();
				}
			}
				return list;
	}

}
