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
 * @文件名称 : FaceConversionUtil.java
 * @创建时间 : 2013-1-27 下午02:34:09
 * @文件描述 : 表情转换工具
 ****************************************** 
 */
public class FaceConversionUtil {

	/** 每一页表情的个数 */
	private int pageSize = 20;

	private static FaceConversionUtil mFaceConversionUtil;

	/** 保存于内存中的表情HashMap */
	private HashMap<String, String> emojiMap = new HashMap<String, String>();

	/** 保存于内存中的表情集合 */
	private List<ChatEmoji> emojis = new ArrayList<ChatEmoji>();

	/** 表情分页的结果集合 */
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
	 * 得到一个SpanableString对象，通过传入的字符串,并进行正则判断
	 * 
	 * @param context
	 * @param str
	 * @return
	 */
	public SpannableString getExpressionString(Context context, String str) {
		if (str != null && str.startsWith(APPConstant.CMD_PREFIX_FILE_SEND)) {
			return sendFile(context, str);// 发送文件
		} else if (str != null && !str.equals("")) {
			SpannableString spannableString = new SpannableString(str);
			// 正则表达式比配字符串里是否含有表情，如： 我好[开心]啊
			String zhengze = "\\[[^\\]]+\\]";
			// 通过传入的正则表达式来生成一个pattern
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
	 * 添加表情
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
	 * 发送文件
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
						R.drawable.sendfileok);//其他类型暂不处理
				spannableString+="\t"+realname;
			}
		} else {
			if (url.equals(""+APPConstant.PICTURE)) {// 这里可以根据文件类型显示不同的图标标识
				bitmap = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.imageerror);

			} else {
				bitmap = BitmapFactory.decodeResource(context.getResources(),
						R.drawable.sendfileerror);
				spannableString+="\t"+realname+"文件传输失败！";
			}
		}
		ImageSpan imageSpan = new ImageSpan(context, bitmap);
		SpannableString spannable = new SpannableString(spannableString);
		spannable.setSpan(imageSpan, 0, length,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}

	/**
	 * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
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
			// 返回第一个字符的索引的文本匹配整个正则表达式,ture 则继续递归
			if (matcher.start() < start) {
				continue;
			}
			String value = emojiMap.get(key);
			if (TextUtils.isEmpty(value)) {
				continue;
			}
			int resId = context.getResources().getIdentifier(value, "drawable",
					context.getPackageName());
			// 通过上面匹配得到的字符串来生成图片资源id
			// Field field=R.drawable.class.getDeclaredField(value);
			// int resId=Integer.parseInt(field.get(null).toString());
			if (resId != 0) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						context.getResources(), resId);
				// 表情图片不要缩放
				// bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, true);	
				// 通过图片资源id来得到bitmap，用一个ImageSpan来包装
				ImageSpan imageSpan = new ImageSpan(bitmap);
				// 计算该图片名字的长度，也就是要替换的字符串的长度
				int end = matcher.start() + key.length();
				// 将该图片替换字符串中规定的位置中
				spannableString.setSpan(imageSpan, matcher.start(), end,
						Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				if (end < spannableString.length()) {
					// 如果整个字符串还未验证完，则继续。。
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
	 * 解析字符
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
	 * 获取分页数据
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
		// 不这么写，会在viewpager加载中报集合操作异常，我也不知道为什么
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