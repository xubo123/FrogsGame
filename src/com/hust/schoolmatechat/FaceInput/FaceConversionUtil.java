package com.hust.schoolmatechat.FaceInput;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.engine.ChatMessage;
import com.hust.schoolmatechat.utils.ImageUtils;

/**
 * 
 ****************************************** 
 * @author 
 * @�ļ����� : FaceConversionUtil.java
 * @����ʱ�� : 2013-1-27 ����02:34:09
 * @�ļ����� : ����ת������
 ****************************************** 
 */
public class FaceConversionUtil {

	/** ÿһҳ����ĸ��� */
	private int pageSize = 20;

	private static FaceConversionUtil mFaceConversionUtil;

	/** �������ڴ��еı���HashMap */
	private HashMap<String, String> emojiMap = new HashMap<String, String>();

	/** �������ڴ��еı��鼯�� */
	private List<ChatEmoji> emojis = new ArrayList<ChatEmoji>();

	/** �����ҳ�Ľ������ */
	public List<List<ChatEmoji>> emojiLists = new ArrayList<List<ChatEmoji>>();

	private FaceConversionUtil() {

	}

	public static FaceConversionUtil getInstace() {
		if (mFaceConversionUtil == null) {
			mFaceConversionUtil = new FaceConversionUtil();
		}
		return mFaceConversionUtil;
	}

	/**
	 * �õ�һ��SpanableString����ͨ��������ַ���,�����������ж�
	 * 
	 * @param context
	 * @param str
	 * @return
	 */
	public SpannableString getExpressionString(Context context, String str) {
		if (str != null && str.startsWith(APPConstant.CMD_PREFIX_FILE_SEND)) {
			return sendFile(context, str);// �����ļ�
		} else if (str != null && !str.equals("")) {
			SpannableString spannableString = new SpannableString(str);
			// ������ʽ�����ַ������Ƿ��б��飬�磺 �Һ�[����]��
			String zhengze = "\\[[^\\]]+\\]";
			// ͨ�������������ʽ������һ��pattern
			Pattern sinaPatten = Pattern.compile(zhengze,
					Pattern.CASE_INSENSITIVE);
			try {
				dealExpression(context, spannableString, sinaPatten, 0);
			} catch (Exception e) {
				Log.e("dealExpression", e.getMessage());
			}
			return spannableString;
		} else {
			return null;
		}
	}

	/**
	 * ��ӱ���
	 * 
	 * @param context
	 * @param imgId
	 * @param spannableString
	 * @return
	 */
	public SpannableString addFace(Context context, int imgId,
			String spannableString) {
		if (TextUtils.isEmpty(spannableString)) {
			return null;
		}
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				imgId);
		bitmap = Bitmap.createScaledBitmap(bitmap, 35, 35, true);
		ImageSpan imageSpan = new ImageSpan(context, bitmap);
		SpannableString spannable = new SpannableString(spannableString);
		spannable.setSpan(imageSpan, 0, spannableString.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}

	/**
	 * �����ļ�
	 * @param context
	 * @param spannableString
	 * @return
	 */
	public SpannableString sendFile(Context context, String spannableString) {
//		Log.e("====",spannableString);
		String url = "";
		String realname="";
		int length = spannableString.length();
		if (TextUtils.isEmpty(spannableString)) {
			return null;
		} else {
			url = spannableString.replaceAll(APPConstant.CMD_PREFIX_FILE_SEND, "");
		}
		Bitmap bitmap;
		if (url.startsWith("http://")) {
			String[] path = url.split("all_user_files");
			int start = url.lastIndexOf("/") ;
			String filetype = url.substring(start -1, start);
			
			String filename = url.substring(start + 1, url.length());
			int nstart = filename.indexOf("_") ;
			realname = filename.substring(nstart + 1, filename.length());
			String pathname = APPConstant.CHAT_FILE
					+ File.separator
					+ path[1].substring(0, path[1].length() - filename.length()
							- 1);
			if(filetype.equals(""+APPConstant.PICTURE)){
				
			File file = new File(pathname, filename);
			if (file.exists()) {
				WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	 
				int width = wm.getDefaultDisplay().getWidth();
				bitmap = ImageUtils.getThumbnail(context, Uri.fromFile(file),
						width/3);
			} else {
				bitmap = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.imagesending);
				Intent downloadFileIntent = new Intent("com.schoolmatechat.downloadFile");
				downloadFileIntent.putExtra("URL", url);
				context.sendBroadcast(downloadFileIntent);
			}
			}else if(filetype.equals(""+APPConstant.NORMAL_FILE)){
				bitmap = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.sendfileok);
				spannableString+="\t"+realname;
			}else{
				bitmap = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.sendfileok);//���������ݲ�����
				spannableString+="\t"+realname;
			}
		} else {
			if (url.equals(""+APPConstant.PICTURE)) {// ������Ը����ļ�������ʾ��ͬ��ͼ���ʶ
				bitmap = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.imageerror);

			} else {
				bitmap = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.sendfileerror);
				spannableString+="\t"+realname+"�ļ�����ʧ�ܣ�";
			}
		}
		ImageSpan imageSpan = new ImageSpan(context, bitmap);
		SpannableString spannable = new SpannableString(spannableString);
		spannable.setSpan(imageSpan, 0, length,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}

	/**
	 * ��spanableString���������жϣ��������Ҫ�����Ա���ͼƬ����
	 * 
	 * @param context
	 * @param spannableString
	 * @param patten
	 * @param start
	 * @throws Exception
	 */
	private void dealExpression(Context context,
			SpannableString spannableString, Pattern patten, int start)
			throws Exception {
		Matcher matcher = patten.matcher(spannableString);
		while (matcher.find()) {
			String key = matcher.group();
			// ���ص�һ���ַ����������ı�ƥ������������ʽ,ture ������ݹ�
			if (matcher.start() < start) {
				continue;
			}
			String value = emojiMap.get(key);
			if (TextUtils.isEmpty(value)) {
				continue;
			}
			int resId = context.getResources().getIdentifier(value, "drawable",
					context.getPackageName());
			// ͨ������ƥ��õ����ַ���������ͼƬ��Դid
			// Field field=R.drawable.class.getDeclaredField(value);
			// int resId=Integer.parseInt(field.get(null).toString());
			if (resId != 0) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						context.getResources(), resId);
				// ����ͼƬ��Ҫ����
				// bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, true);	
				// ͨ��ͼƬ��Դid���õ�bitmap����һ��ImageSpan����װ
				ImageSpan imageSpan = new ImageSpan(bitmap);
				// �����ͼƬ���ֵĳ��ȣ�Ҳ����Ҫ�滻���ַ����ĳ���
				int end = matcher.start() + key.length();
				// ����ͼƬ�滻�ַ����й涨��λ����
				spannableString.setSpan(imageSpan, matcher.start(), end,
						Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				if (end < spannableString.length()) {
					// ��������ַ�����δ��֤�꣬���������
					dealExpression(context, spannableString, patten, end);
				}
				break;
			}
		}
	}

	public void getFileText(Context context) {
		ParseData(FileUtils.getEmojiFile(context), context);
	}

	/**
	 * �����ַ�
	 * 
	 * @param data
	 */
	private void ParseData(List<String> data, Context context) {
		if (data == null) {
			return;
		}
		ChatEmoji emojEentry;
		try {
			for (String str : data) {
				String[] text = str.split(",");
				String fileName = text[0]
						.substring(0, text[0].lastIndexOf("."));
				emojiMap.put(text[1], fileName);
				int resID = context.getResources().getIdentifier(fileName,
						"drawable", context.getPackageName());

				if (resID != 0) {
					emojEentry = new ChatEmoji();
					emojEentry.setId(resID);
					emojEentry.setCharacter(text[1]);
					emojEentry.setFaceName(fileName);
					emojis.add(emojEentry);
				}
			}
			int pageCount = (int) Math.ceil(emojis.size() / 20 + 0.1);

			for (int i = 0; i < pageCount; i++) {
				emojiLists.add(getData(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ȡ��ҳ����
	 * 
	 * @param page
	 * @return
	 */
	private List<ChatEmoji> getData(int page) {
		int startIndex = page * pageSize;
		int endIndex = startIndex + pageSize;

		if (endIndex > emojis.size()) {
			endIndex = emojis.size();
		}
		// ����ôд������viewpager�����б����ϲ����쳣����Ҳ��֪��Ϊʲô
		List<ChatEmoji> list = new ArrayList<ChatEmoji>();
		list.addAll(emojis.subList(startIndex, endIndex));
		if (list.size() < pageSize) {
			for (int i = list.size(); i < pageSize; i++) {
				ChatEmoji object = new ChatEmoji();
				list.add(object);
			}
		}
		if (list.size() == pageSize) {
			ChatEmoji object = new ChatEmoji();
			object.setId(R.drawable.face_del_icon);
			list.add(object);
		}
		return list;
	}
}