package com.hust.schoolmatechat.provider;

import android.net.Uri;

/**
 * 此类包含了窗友CYProvider中所有的Uri
 * @author 刘宇龙
 *
 */
public class CYContract {
	public static final String AUTHORITY="content://com.hust.schoolmatechat.provider";
	
	public static final Uri BASE_URI=Uri.parse(AUTHORITY);
	
	public static final Uri TABLE_AD_TABLE = Uri.withAppendedPath(BASE_URI, "Ad_Table");
	public static final Uri TABLE_CHANNEL_TABLE = Uri.withAppendedPath(BASE_URI, "Channel_Table");
	public static final Uri TABLE_COMMENT_TABLE = Uri.withAppendedPath(BASE_URI, "Comment_Table");
	public static final Uri TABLE_CONFIGURATION_TABLE = Uri.withAppendedPath(BASE_URI, "Configuration_Table");
	public static final Uri TABLE_DEPARTMENT_TABLE = Uri.withAppendedPath(BASE_URI, "Department_Table");
	public static final Uri TABLE_FRIENDGROUP_TABLE = Uri.withAppendedPath(BASE_URI, "FriendGroup_Table");
	public static final Uri TABLE_GROUPINFO_TABLE = Uri.withAppendedPath(BASE_URI, "GroupInfo");
	public static final Uri TABLE_GROUPMEMBERINFO_TABLE = Uri.withAppendedPath(BASE_URI, "GroupMemberInfo");

	public static final Uri TABLE_MESSAGE_TABLE = Uri.withAppendedPath(BASE_URI, "Message_Table");
	public static final Uri TABLE_NEWS_TABLE = Uri.withAppendedPath(BASE_URI, "News_Table");
	public static final Uri TABLE_SCHOOLFELLOW_TABLE = Uri.withAppendedPath(BASE_URI, "SchoolFellow_Table");
	public static final Uri TABLE_USERPROFILE_TABLE = Uri.withAppendedPath(BASE_URI, "UserProfile_Table");
	public static final Uri TABLE_SINGLENEWSMESSAGE_TABLE = Uri.withAppendedPath(BASE_URI, "SingleNewsMessage_Table");
	public static final Uri TABLE_PUSHEDMESSAGE_TABLE = Uri.withAppendedPath(BASE_URI, "PushedMessage_Table");
	public static final Uri TABLE_CHATMESSAGE_TABLE = Uri.withAppendedPath(BASE_URI, "ChatMessage_Table");
	public static final Uri TABLE_CHECK_TABLE = Uri.withAppendedPath(BASE_URI, "Check_Table");

	public static final Uri TABLE_CHATITEM = Uri.withAppendedPath(BASE_URI, "chatitem");
	public static final Uri TABLE_CHATMESSAGE = Uri.withAppendedPath(BASE_URI, "chatmessage");
	public static final Uri TABLE_SINGLENEWSMESSAGE = Uri.withAppendedPath(BASE_URI, "singlenewsmessage");
	public static final Uri TABLE_STUDENTSINFO = Uri.withAppendedPath(BASE_URI, "StudentsInfo");
	public static final Uri TABLE_USERSELFINFO = Uri.withAppendedPath(BASE_URI, "UserSelfInfo");
	public static final Uri TABLE_STUDENTSSAMPLE = Uri.withAppendedPath(BASE_URI, "StudentsSample");
	
	

}
