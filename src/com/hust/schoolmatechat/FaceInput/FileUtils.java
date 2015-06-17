package com.hust.schoolmatechat.FaceInput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

/**
 * 
 ******************************************
 * @author 
 * @�ļ�����	:  FileUtils.java
 * @����ʱ��	: 2013-1-27 ����02:35:09
 * @�ļ�����	: �ļ�������
 ******************************************
 */
public class FileUtils {
	/**
	 * ��ȡ���������ļ�
	 * 
	 * @param context
	 * @return
	 */
	public static List<String> getEmojiFile(Context context) {
		try {
			List<String> list = new ArrayList<String>();
			InputStream in = context.getResources().getAssets().open("emoji");
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"UTF-8"));
			String str = null;
			while ((str = br.readLine()) != null) {
				list.add(str);
			}

			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
