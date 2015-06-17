package com.hust.schoolmatechat;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hust.schoolmatechat.SearchSuggestionProvider.MySQLiteDatabase;
import com.hust.schoolmatechat.dao.ChatItemDao;
import com.hust.schoolmatechat.dao.ChatMessageDao;
import com.hust.schoolmatechat.dao.ClassmateDao;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatItem;
import com.hust.schoolmatechat.engine.ChatMessage;
import com.hust.schoolmatechat.engine.CrashHandler;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

import java.util.Date;
import java.util.UUID;

public class SchoolMateChat extends Application {
	private static final String TAG = "SchoolMateChat";
	
	private static SchoolMateChat instance;  
	  
    private JobManager jobManager;
    public static SchoolMateChat getInstance() {  
        return instance;  
    }  
    
	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		
        configureJobManager();
		instance = this;
	}
	
	/**
	 * 检查数据表都是否创建以及初始数据是否都在
	 */
	public boolean checkExistLocalDBTables() {
		MySQLiteDatabase myarg0 = null;
		try {
			SearchSuggestionProvider.init();
			myarg0 = SearchSuggestionProvider.openDatabase(instance);
			SQLiteDatabase arg0 = myarg0.openDatabase(instance);
			// Ad_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [Ad_Table] ([AdID] NUMBER(10) PRIMARY KEY NOT NULL,[AdPicture] VARCHAR(10), [UrlLink] VARCHAR(200), [LastUpdate] DATETIME);");
			// Channel_Table用户订阅的通道
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [Channel_Table] ([ChannelID] VARCHAR(20) PRIMARY KEY NOT NULL, [ChannelName] VARCHAR(20), [Icon] VARCHAR(200), [channelRemark] VARCHAR(200));");
			// 所有通道
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [ALLChannel_Table] ([ChannelID] VARCHAR(20) PRIMARY KEY NOT NULL, [ChannelName] VARCHAR(20), [Icon] VARCHAR(200), [channelRemark] VARCHAR(200));");
			// Comment_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [Comment_Table] ([CommentID] NUMBER(10) PRIMARY KEY NOT NULL, [NewsID] NUMBER(10) CONSTRAINT [NewsID] REFERENCES [News_Table]([NewsID]), [Content] VARCHAR(200), [CommentTime] DATETIME,  [CUserName] VARCHAR(10), [LastUpdate] DATETIME);");
			// Configuration_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [Configuration_Table] ([UID] NUMBER(10) PRIMARY KEY NOT NULL, [NightAntiDisturb] BOOLEAN DEFAULT false, [RemindofMessage] BOOLEAN DEFAULT true,[FlashtoRemind] BOOLEAN DEFAULT false, [ShaketoRemind] BOOLEAN DEFAULT false, [VoicetoRemind] BOOLEAN DEFAULT false, [GroupShaketoRemind] BOOLEAN DEFAULT false, [GroupVoicetoRemind] BOOLEAN DEFAULT false, [KindofVoice] VARCHAR(20), [LastUpdate] DATETIME DEFAULT (2014/08/12),[Theme] VARCHAR(20));");
			// Department_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [Department_Table] ([DID] NUMBER(10) PRIMARY KEY NOT NULL, [DepartmentName] VARCHAR(100), [Remark] VARCHAR(100), [LastUpdate] DATETIME);");
			// FriendGroup_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [FriendGroup_Table] ([FGID] NUMBER(10)  PRIMARY KEY NOT NULL, [Users] VARCHAR(1000) CONSTRAINT [Users] REFERENCES [SchoolFellow_Table]([SID]), [GourpName] VARCHAR(30), [Remark] VARCHAR(100), [LastUpdate] DATETIME);");
			
			// GroupInfo 群组信息
			arg0.execSQL("CREATE TABLE IF NOT EXISTS GroupInfo(nid integer primary key AutoIncrement,groupId VARCHAR(21),userAccount CHAR(10),groupName VARCHAR(80),createrAccount CHAR(10),description VARCHAR(200),subject VARCHAR(100),adminsAccount VARCHAR(1000),membersAccount VARCHAR(2000));");
			// GroupMemberInfo 群成员，非好友信息
			arg0.execSQL("CREATE TABLE IF NOT EXISTS GroupMemberInfo(nid integer primary key AutoIncrement,userAccount VARCHAR(50),className VARCHAR(50),accountNum VARCHAR(50),address CHAR(50),authenticated CHAR(1),baseInfoId CHAR(50),channels CHAR(150),email CHAR(20),intrestType CHAR(150),name CHAR(10),phoneNum CHAR(15),picture CHAR(150),sex CHAR(1),sign CHAR(50),hasAllClassmates INT);");
						
			// Message_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [Message_Table] ([MessageID] NUMBER(10)  PRIMARY KEY NOT NULL, [UserID] NUMBER(10) CONSTRAINT [UserID] REFERENCES [UserProfile_Table]([UID]), [RecieveDate] DATETIME, [Content] VARCHAR(1000), [IsRead] BOOLEAN, [LastUpdate] DATETIME);");
			// News_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [News_Table] ([ChannelID] NUMBER NOT NULL CONSTRAINT [ChannelID] REFERENCES [Channel_Table]([ChannelID]), [NewsDate] DATETIME, [NewsID] NUMBER(10) PRIMARY KEY NOT NULL, [Title] VARCHAR(50), [Abstract] VARCHAR(400), [Url] VARCHAR(200), [IsRead] BOOLEAN, [Img] VARCHAR(100), [Type] NUMBER(10), [LastUpdate] DATETIME);");
			// SchoolFellow_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [SchoolFellow_Table] ([SID] NUMBER(10)  PRIMARY KEY NOT NULL, [Name] VARCHAR(30), [Number] VARCHAR(30), [DID] VARCHAR(30) CONSTRAINT [DID] REFERENCES [Department_Table]([DID]), [Major] VARCHAR(50), [PostTitle] INTEGER, [BirthDate] DATETIME, [NativePlace] VARCHAR(50), [Indentity] VARCHAR(20), [EnterDate] DATE, [LeaveDate] DATE, [Remark] VARCHAR(100), [LastUpdate] DATETIME);");
			// UserProfile_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [UserProfile_Table] ([Picture] CHAR(300), [UID] CHAR(20) NOT NULL, [Password] CHAR(20), [Name] CHAR(20), [PhoneNum] CHAR(20), [Sex] CHAR(10), [Address] CHAR(100), [Sign] CHAR(200), [IntrestType] CHAR(100), [Channels] CHAR(100), [Email] CHAR(50), [IdNumber] CHAR(20),  [BaseInfoId] CHAR(20) DEFAULT 0001500010199501001, CONSTRAINT [sqlite_autoindex_UserProfile_Table_1] PRIMARY KEY ([UID]));");
			// SingleNewsMessage_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [SingleNewsMessage_Table] ([Nid] VARCHAR(80) PRIMARY KEY NOT NULL, [Icon] VARCHAR(200), [ISbreaking] BOOLEAN DEFAULT false, [Title] VARCHAR(200), [Summary] VARCHAR[1000], [Time] DateTime, [NewsUrl] VARCHAR[200], [ChannelID] VARCHAR(80), [PMId] VARCHAR[80]);");
			// PushedMessage_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [PushedMessage_Table] ([PMId] VARCHAR(80) PRIMARY KEY NOT NULL, [Icon] VARCHAR(200), [ISunread] BOOLEAN DEFAULT true, [CName] VARCHAR[20], [ChannelID] VARCHAR(80), [Time] DateTime, [NewsSummary] VARCHAR(500));");
			// ChatMessage_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [ChatMessage_Table] ([id] integer primary key AutoIncrement, [Mid] VARCHAR(80), [Icon] VARCHAR(200), [Type]  INTEGER, [NickName] VARCHAR(20), [Time] DateTime, [MessageContent] VARCHAR(200));");
			// Check_Table
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [Check_Table] ( [Name] CHAR(20) PRIMARY KEY NOT NULL, [IfChecked] NUMBER DEFAULT 0, [IfPassed] NUMBER DEFAULT 0);");

			// cy_dept
			arg0.execSQL("CREATE TABLE IF NOT EXISTS cy_dept(dept_id VARCHAR(30) primary key,full_name VARCHAR(200))");

			// chatitem
			arg0.execSQL("CREATE TABLE IF NOT EXISTS chatitem(id integer primary key AutoIncrement,owner VARCHAR(80),icon VARCHAR(80),cType INT,name VARCHAR(20),friendAccount VARCHAR(80),cTime VARCHAR(80),latestMessage VARCHAR(80),unread INT,userAccount VARCHAR(80))");

			// chatmessage
			arg0.execSQL("CREATE TABLE IF NOT EXISTS chatmessage(id integer primary key AutoIncrement,mid VARCHAR(80),icon VARCHAR(80),mType INT,userAccount VARCHAR(50),senderAccount VARCHAR(50),recvAccount VARCHAR(50),mTime VARCHAR(80),messageContent VARCHAR(200),owner VARCHAR(50),isRead INT)");

			// singlenewsmessage
			arg0.execSQL("CREATE TABLE IF NOT EXISTS singlenewsmessage(nid INT(10) PRIMARY KEY,icon VARCHAR(80),isBreaking INT,title VARCHAR(80),summary VARCHAR(80),smTime VARCHAR(80),newsUrl VARCHAR(80),channelId VARCHAR(80),PMId VARCHAR(80),content VARCHAR(200))");
			CYLog.i("dbOpenHelper", "tables created");
			// StudentsInfo 联系人信息
			arg0.execSQL("CREATE TABLE IF NOT EXISTS StudentsInfo (nid integer primary key AutoIncrement,userAccount VARCHAR(50),className VARCHAR(50),accountNum VARCHAR(50),address CHAR(50),authenticated CHAR(1),baseInfoId CHAR(50),channels CHAR(150),email CHAR(20),intrestType CHAR(150),name CHAR(10),phoneNum CHAR(15),picture CHAR(150),sex CHAR(1),sign CHAR(50),hasAllClassmates INT);");
			// UserSelfInfo 使用app登陆的人
			arg0.execSQL("CREATE TABLE IF NOT EXISTS UserSelfInfo (nid integer primary key AutoIncrement,userAccount VARCHAR(50),className VARCHAR(50),accountNum VARCHAR(50),address CHAR(50),authenticated CHAR(1),baseInfoId CHAR(50),channels CHAR(150),email CHAR(20),intrestType CHAR(150),name CHAR(10),"
					+ "phoneNum CHAR(15),picture CHAR(150),sex CHAR(1),sign CHAR(50),groupName VARCHAR(500),password CHAR(50),hasAllClassmates INT);");
			// StudentsSample
			arg0.execSQL("CREATE TABLE IF NOT EXISTS [StudentsSample] ([SNum] integer primary key  AutoIncrement,[Name] CHAR(10) NOT NULL UNIQUE);");

			String sql = "SELECT COUNT(*) FROM Channel_Table WHERE ChannelName='"
					+ APPConstant.SCHOOL_HELPER_CHANNEL_NAME + "'";
			if (!isRecordExisted(arg0, sql)) {
				// 插入校友帮帮忙初始数据
				arg0.execSQL("INSERT INTO Channel_Table(ChannelID,ChannelName,channelRemark,Icon) VALUES('"
						+ APPConstant.SCHOOL_HELPER_CHANNEL_NAME
						+ "','"
						+ APPConstant.SCHOOL_HELPER_CHANNEL_NAME
						+ "','找人才、找工作、找项目、找资金，……，校友来帮你','"
						+ R.drawable.schoolhelper_main + "')");

				arg0.execSQL("INSERT INTO chatmessage(mid,mType,userAccount,messageContent,owner,icon) VALUES('帮帮忙-招聘',"
						+ ChatMessage.SCHOOLHELPER
						+ ",'"
						+ APPConstant.SCHOOL_HELPER_TYPE[0]
						+ "','"
						+ APPConstant.getURL()
						+ APPConstant.SCHOOL_HELPER_URL[0]
						+ "','"
						+ APPConstant.SCHOOL_HELPER_CHANNEL_NAME
						+ "','"
						+ APPConstant.SCHOOL_HELPER_ICON[0] + "')");
				arg0.execSQL("INSERT INTO chatmessage(mid,mType,userAccount,messageContent,owner,icon) VALUES('帮帮忙-项目',"
						+ ChatMessage.SCHOOLHELPER
						+ ",'"
						+ APPConstant.SCHOOL_HELPER_TYPE[1]
						+ "','"
						+ APPConstant.getURL()
						+ APPConstant.SCHOOL_HELPER_URL[1]
						+ "','"
						+ APPConstant.SCHOOL_HELPER_CHANNEL_NAME
						+ "','"
						+ APPConstant.SCHOOL_HELPER_ICON[1] + "')");
				arg0.execSQL("INSERT INTO chatmessage(mid,mType,userAccount,messageContent,owner,icon) VALUES('帮帮忙-资金',"
						+ ChatMessage.SCHOOLHELPER
						+ ",'"
						+ APPConstant.SCHOOL_HELPER_TYPE[2]
						+ "','"
						+ APPConstant.getURL()
						+ APPConstant.SCHOOL_HELPER_URL[2]
						+ "','"
						+ APPConstant.SCHOOL_HELPER_CHANNEL_NAME
						+ "','"
						+ APPConstant.SCHOOL_HELPER_ICON[2] + "')");
				arg0.execSQL("INSERT INTO chatmessage(mid,mType,userAccount,messageContent,owner,icon) VALUES('帮帮忙-求职',"
						+ ChatMessage.SCHOOLHELPER
						+ ",'"
						+ APPConstant.SCHOOL_HELPER_TYPE[3]
						+ "','"
						+ APPConstant.getURL()
						+ APPConstant.SCHOOL_HELPER_URL[3]
						+ "','"
						+ APPConstant.SCHOOL_HELPER_CHANNEL_NAME
						+ "','"
						+ APPConstant.SCHOOL_HELPER_ICON[3] + "')");
				arg0.execSQL("INSERT INTO chatmessage(mid,mType,userAccount,messageContent,owner,icon) VALUES('帮帮忙-其他',"
						+ ChatMessage.SCHOOLHELPER
						+ ",'"
						+ APPConstant.SCHOOL_HELPER_TYPE[4]
						+ "','"
						+ APPConstant.getURL()
						+ APPConstant.SCHOOL_HELPER_URL[4]
						+ "','"
						+ APPConstant.SCHOOL_HELPER_CHANNEL_NAME
						+ "','"
						+ APPConstant.SCHOOL_HELPER_ICON[4] + "')");
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, "checkExistLocalDBTables failed : " + e.toString());
			return false;
		} finally {
			if (myarg0 != null) {
				myarg0.close();
			}
		}
	}

	/**
	 * sql语句指定的记录在arg0里是否存在
	 * 
	 * @param arg0
	 * @param sql
	 * @return
	 */
	private boolean isRecordExisted(SQLiteDatabase arg0, String sql) {
		Cursor cursor = null;
		try {
			boolean isExisted = false;// 默认有此用户
			String[] params = new String[] {};
			cursor = arg0.rawQuery(sql, params);
			if (cursor.moveToFirst()) {
				int resultCount = cursor.getInt(0);
				if (resultCount > 0)
					isExisted = true;
			}
			
			return isExisted;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	/**
	* 配置后台任务管理器
	*/
    private void configureJobManager() {
        Configuration configuration = new Configuration.Builder(this)
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";

                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .build();
        jobManager = new JobManager(this, configuration);
    }

    public JobManager getJobManager() {
        return jobManager;
    }
}
