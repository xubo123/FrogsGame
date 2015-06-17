package com.hust.schoolmatechat.provider;

import com.hust.schoolmatechat.db.DbOpenHelper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * 这个ContentProvider包含的Uri，全部写在CYContract类中
 * 
 * @author 刘宇龙
 *
 */
public class CYProvider extends ContentProvider {

	private DbOpenHelper mHelper = null;
	private SQLiteDatabase db = null;

	private static final int AD_TABLE = 10;
	private static final int CHANNEL_TABLE = 20;
	private static final int COMMENT_TABLE = 30;
	private static final int CONFIGURATION_TABLE = 40;
	private static final int DEPARTMENT_TABLE = 50;
	private static final int FRIENDGROUP_TABLE = 60;
	private static final int GROUP_TABLE = 70;
	private static final int GROUPMEMBER_TABLE = 80;

	private static final int MESSAGE_TABLE = 90;
	private static final int NEWS_TABLE = 100;
	private static final int SCHOOLFELLOW_TABLE = 110;
	private static final int USERPROFILE_TABLE = 120;
	private static final int SINGLENEWSMESSAGE_TABLE = 130;
	private static final int PUSHEDMESSAGE_TABLE = 140;
	private static final int CHATMESSAGE_TABLE = 150;
	private static final int CHECK_TABLE = 160;

	private static final int CHATITEM = 170;
	private static final int CHATMESSAGE = 180;
	private static final int SINGLENEWSMESSAGE = 190;
	private static final int STUDENTSINFO = 200;
	private static final int USERSELFINFO = 210;
	private static final int STUDENTSSAMPLE = 220;

	// Creates a UriMatcher object.
	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sUriMatcher.addURI("com.hust.schoolmatechat.provider", "Ad_Table",
				AD_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider", "Channel_Table",
				CHANNEL_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider", "Comment_Table",
				COMMENT_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider",
				"Configuration_Table", CONFIGURATION_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider",
				"Department_Table", DEPARTMENT_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider",
				"FriendGroup_Table", FRIENDGROUP_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider", "Group_Table",
				GROUP_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider",
				"GroupMember_Table", GROUPMEMBER_TABLE);

		sUriMatcher.addURI("com.hust.schoolmatechat.provider", "Message_Table",
				MESSAGE_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider", "News_Table",
				NEWS_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider",
				"SchoolFellow_Table", SCHOOLFELLOW_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider",
				"UserProfile_Table", USERPROFILE_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider",
				"SingleNewsMessage_Table", SINGLENEWSMESSAGE_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider",
				"PushedMessage_Table", PUSHEDMESSAGE_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider",
				"ChatMessage_Table", CHATMESSAGE_TABLE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider", "Check_Table",
				CHECK_TABLE);

		sUriMatcher.addURI("com.hust.schoolmatechat.provider", "chatitem",
				CHATITEM);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider", "chatmessage",
				CHATMESSAGE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider",
				"singlenewsmessage", SINGLENEWSMESSAGE);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider", "StudentsInfo",
				STUDENTSINFO);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider", "UserSelfInfo",
				USERSELFINFO);
		sUriMatcher.addURI("com.hust.schoolmatechat.provider",
				"StudentsSample", STUDENTSSAMPLE);

	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		db = mHelper.getWritableDatabase();
		String table = uri.getLastPathSegment();

		switch (sUriMatcher.match(uri)) {
		case AD_TABLE:

			break;

		case CHANNEL_TABLE:

			break;

		case COMMENT_TABLE:

			break;

		case CONFIGURATION_TABLE:

			break;

		case DEPARTMENT_TABLE:

			break;

		case FRIENDGROUP_TABLE:

			break;

		case GROUP_TABLE:

			break;

		case GROUPMEMBER_TABLE:

			break;

		case MESSAGE_TABLE:

			break;

		case NEWS_TABLE:

			break;

		case SCHOOLFELLOW_TABLE:

			break;

		case USERPROFILE_TABLE:

			break;

		case SINGLENEWSMESSAGE_TABLE:

			break;

		case PUSHEDMESSAGE_TABLE:

			break;

		case CHATMESSAGE_TABLE:

			break;

		case CHECK_TABLE:

			break;

		case CHATITEM:

			break;

		case CHATMESSAGE:

			break;

		case SINGLENEWSMESSAGE:

			break;

		case STUDENTSINFO:

			break;

		case USERSELFINFO:

			break;

		case STUDENTSSAMPLE:

			break;

		default:
			break;
		}

		return db.delete(table, selection, selectionArgs);

	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub

		switch (sUriMatcher.match(uri)) {
		case AD_TABLE:

			break;

		case CHANNEL_TABLE:

			break;

		case COMMENT_TABLE:

			break;

		case CONFIGURATION_TABLE:

			break;

		case DEPARTMENT_TABLE:

			break;

		case FRIENDGROUP_TABLE:

			break;

		case GROUP_TABLE:

			break;

		case GROUPMEMBER_TABLE:

			break;

		case MESSAGE_TABLE:

			break;

		case NEWS_TABLE:

			break;

		case SCHOOLFELLOW_TABLE:

			break;

		case USERPROFILE_TABLE:

			break;

		case SINGLENEWSMESSAGE_TABLE:

			break;

		case PUSHEDMESSAGE_TABLE:

			break;

		case CHATMESSAGE_TABLE:

			break;

		case CHECK_TABLE:

			break;

		case CHATITEM:

			break;

		case CHATMESSAGE:

			break;

		case SINGLENEWSMESSAGE:

			break;

		case STUDENTSINFO:

			break;

		case USERSELFINFO:

			break;

		case STUDENTSSAMPLE:

			break;

		default:
			break;
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		db = mHelper.getWritableDatabase();

		String table = uri.getLastPathSegment();

		switch (sUriMatcher.match(uri)) {
		case AD_TABLE:

			break;

		case CHANNEL_TABLE:

			break;

		case COMMENT_TABLE:

			break;

		case CONFIGURATION_TABLE:

			break;

		case DEPARTMENT_TABLE:

			break;

		case FRIENDGROUP_TABLE:

			break;

		case GROUP_TABLE:

			break;

		case GROUPMEMBER_TABLE:

			break;

		case MESSAGE_TABLE:

			break;

		case NEWS_TABLE:

			break;

		case SCHOOLFELLOW_TABLE:

			break;

		case USERPROFILE_TABLE:

			break;

		case SINGLENEWSMESSAGE_TABLE:

			break;

		case PUSHEDMESSAGE_TABLE:

			break;

		case CHATMESSAGE_TABLE:

			break;

		case CHECK_TABLE:

			break;

		case CHATITEM:

			break;

		case CHATMESSAGE:

			break;

		case SINGLENEWSMESSAGE:

			break;

		case STUDENTSINFO:

			break;

		case USERSELFINFO:

			break;

		case STUDENTSSAMPLE:

			break;

		default:
			break;
		}

		long locNum = db.insert(table, null, values);
		if (locNum == -1) {
			return null;
		} else {

			return uri.withAppendedPath(uri, locNum + "");
		}
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mHelper = new DbOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		db = mHelper.getReadableDatabase();
		
		String table=uri.getLastPathSegment();
		
		switch (sUriMatcher.match(uri)) {
		case AD_TABLE:

			break;

		case CHANNEL_TABLE:

			break;

		case COMMENT_TABLE:

			break;

		case CONFIGURATION_TABLE:

			break;

		case DEPARTMENT_TABLE:

			break;

		case FRIENDGROUP_TABLE:

			break;

		case GROUP_TABLE:

			break;

		case GROUPMEMBER_TABLE:

			break;

		case MESSAGE_TABLE:

			break;

		case NEWS_TABLE:

			break;

		case SCHOOLFELLOW_TABLE:

			break;

		case USERPROFILE_TABLE:

			break;

		case SINGLENEWSMESSAGE_TABLE:

			break;

		case PUSHEDMESSAGE_TABLE:

			break;

		case CHATMESSAGE_TABLE:

			break;

		case CHECK_TABLE:

			break;

		case CHATITEM:

			break;

		case CHATMESSAGE:

			break;

		case SINGLENEWSMESSAGE:

			break;

		case STUDENTSINFO:

			break;

		case USERSELFINFO:

			break;

		case STUDENTSSAMPLE:

			break;

		default:
			break;
		}
		
		return db.query(table, projection, selection, selectionArgs, null, null, sortOrder);

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		db=mHelper.getWritableDatabase();
		
		String table=uri.getLastPathSegment();

		switch (sUriMatcher.match(uri)) {
		case AD_TABLE:

			break;

		case CHANNEL_TABLE:

			break;

		case COMMENT_TABLE:

			break;

		case CONFIGURATION_TABLE:

			break;

		case DEPARTMENT_TABLE:

			break;

		case FRIENDGROUP_TABLE:

			break;

		case GROUP_TABLE:

			break;

		case GROUPMEMBER_TABLE:

			break;

		case MESSAGE_TABLE:

			break;

		case NEWS_TABLE:

			break;

		case SCHOOLFELLOW_TABLE:

			break;

		case USERPROFILE_TABLE:

			break;

		case SINGLENEWSMESSAGE_TABLE:

			break;

		case PUSHEDMESSAGE_TABLE:

			break;

		case CHATMESSAGE_TABLE:

			break;

		case CHECK_TABLE:

			break;

		case CHATITEM:

			break;

		case CHATMESSAGE:

			break;

		case SINGLENEWSMESSAGE:

			break;

		case STUDENTSINFO:

			break;

		case USERSELFINFO:

			break;

		case STUDENTSSAMPLE:

			break;

		default:
			break;
		}
		
		return db.update(table, values, selection, selectionArgs);
	}

}
