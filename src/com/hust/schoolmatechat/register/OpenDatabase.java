package com.hust.schoolmatechat.register;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;


public class OpenDatabase {
//	final static String filepath = "data/data/com.hust.schoolmatechat/" + APPConstant.getCYDBNAME();
//	final static String pathStr = "data/data/com.hust.schoolmatechat";
	final static String pathStr = Environment.getExternalStorageDirectory().getAbsolutePath() + 
			File.separator + "chuangyou" + File.separator + "data";
	final static String filepath = pathStr + File.separator + APPConstant.getCYDBNAME();
	private static final String TAG = "OpenDatabase";
	public OpenDatabase() {
		// TODO Auto-generated constructor stub
	}
	public SQLiteDatabase openDatabase(Context context) {
//		CYLog.i(TAG,"filePath:" + filepath);
		File jhPath = new File(filepath);
		// 查看数据库文件是否存在
		if (jhPath.exists()) {
			// 存在则直接返回打开的数据库
			SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(jhPath, null);
			
//			Cursor c = db.rawQuery("SELECT * FROM cy_dept WHERE dept_id = " + "'000150'", null);  
//	        while (c.moveToNext()) {  
//	            String _id = c.getString(c.getColumnIndex("dept_id"));  
//	            String name = c.getString(c.getColumnIndex("full_name"));  
//	            
//	            CYLog.i("db", "_id=>" + _id + ", name=>" + name);  
//	        }  
//	        c.close();  
	        
			return db;
		} else {
			// 不存在先创建文件夹
			File path = new File(pathStr);
			if (path.mkdir()) {
				CYLog.i(TAG,"创建成功");
			} else {
				CYLog.i(TAG,"创建失败");
			}
			;
			try {
				// 得到资源
				AssetManager am = context.getAssets();
				// 得到数据库的输入流
				InputStream is = am.open(APPConstant.getCYDBNAME());
				// 用输出流写到SDcard上面
				FileOutputStream fos = new FileOutputStream(jhPath);
				// 创建byte数组 用于1KB写一次
				byte[] buffer = new byte[1024];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				// 最后关闭就可以了
				fos.flush();
				fos.close();
				is.close();
				
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				CYLog.i(TAG,"io异常"+e.getMessage());
				return null;
			}
			// 如果没有这个数据库 我们已经把他写到SD卡上了，然后在执行一次这个方法 就可以返回数据库了
			return openDatabase(context);
		}
	}
}
