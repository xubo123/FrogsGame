package com.hust.schoolmatechat.dao;

import java.text.SimpleDateFormat;

import android.content.Context;

import com.hust.schoolmatechat.db.DbOpenHelper;

public class BaseDao {

	private Context context;
	private DbOpenHelper helper;
	public SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public BaseDao(Context context) {
		super();
		this.context = context;
		this.helper=new DbOpenHelper(context);
		
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public DbOpenHelper getHelper() {
		return helper;
	}

	public void setHelper(DbOpenHelper helper) {
		this.helper = helper;
	}
}
