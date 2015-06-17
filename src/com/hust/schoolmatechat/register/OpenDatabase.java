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
		// �鿴���ݿ��ļ��Ƿ����
		if (jhPath.exists()) {
			// ������ֱ�ӷ��ش򿪵����ݿ�
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
			// �������ȴ����ļ���
			File path = new File(pathStr);
			if (path.mkdir()) {
				CYLog.i(TAG,"�����ɹ�");
			} else {
				CYLog.i(TAG,"����ʧ��");
			}
			;
			try {
				// �õ���Դ
				AssetManager am = context.getAssets();
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
				
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				CYLog.i(TAG,"io�쳣"+e.getMessage());
				return null;
			}
			// ���û��������ݿ� �����Ѿ�����д��SD�����ˣ�Ȼ����ִ��һ��������� �Ϳ��Է������ݿ���
			return openDatabase(context);
		}
	}
}
