package com.hust.schoolmatechat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hust.schoolmatechat.R;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.engine.ChatMessage;

public class DbOpenHelper extends SQLiteOpenHelper {
	private static String name = "WindowsFriendDB";
	private static int version = 1;

	public DbOpenHelper(Context context) {
		super(context, name, null, version);
		// TODO Auto-generated constructor stub
	}

	// ++++lqg++++ 将不再采用??? 使用SearchSuggesstionProvider
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub
		// Ad_Table
		arg0.execSQL("CREATE TABLE IF NOT EXISTS [Ad_Table] ([AdID] NUMBER(10) PRIMARY KEY NOT NULL,[AdPicture] VARCHAR(10), [UrlLink] VARCHAR(200), [LastUpdate] DATETIME);");
		// Channel_Table
		arg0.execSQL("CREATE TABLE IF NOT EXISTS [Channel_Table] ([ChannelID] VARCHAR(20) PRIMARY KEY NOT NULL, [ChannelName] VARCHAR(20), [Icon] VARCHAR(200), [channelRemark] VARCHAR(200));");
		// Comment_Table
		arg0.execSQL("CREATE TABLE IF NOT EXISTS [Comment_Table] ([CommentID] NUMBER(10) PRIMARY KEY NOT NULL, [NewsID] NUMBER(10) CONSTRAINT [NewsID] REFERENCES [News_Table]([NewsID]), [Content] VARCHAR(200), [CommentTime] DATETIME,  [CUserName] VARCHAR(10), [LastUpdate] DATETIME);");
		// Configuration_Table
		arg0.execSQL("CREATE TABLE IF NOT EXISTS [Configuration_Table] ([UID] NUMBER(10) PRIMARY KEY NOT NULL, [NightAntiDisturb] BOOLEAN DEFAULT false, [RemindofMessage] BOOLEAN DEFAULT true,[FlashtoRemind] BOOLEAN DEFAULT false, [ShaketoRemind] BOOLEAN DEFAULT false, [VoicetoRemind] BOOLEAN DEFAULT false, [GroupShaketoRemind] BOOLEAN DEFAULT false, [GroupVoicetoRemind] BOOLEAN DEFAULT false, [KindofVoice] VARCHAR(20), [LastUpdate] DATETIME DEFAULT (2014/08/12),[Theme] VARCHAR(20));");
		// Department_Table
		arg0.execSQL("CREATE TABLE IF NOT EXISTS [Department_Table] ([DID] NUMBER(10) PRIMARY KEY NOT NULL, [DepartmentName] VARCHAR(100), [Remark] VARCHAR(100), [LastUpdate] DATETIME);");
		// FriendGroup_Table
		arg0.execSQL("CREATE TABLE IF NOT EXISTS [FriendGroup_Table] ([FGID] NUMBER(10)  PRIMARY KEY NOT NULL, [Users] VARCHAR(1000) CONSTRAINT [Users] REFERENCES [SchoolFellow_Table]([SID]), [GourpName] VARCHAR(30), [Remark] VARCHAR(100), [LastUpdate] DATETIME);");
		// Group_Table
		arg0.execSQL("CREATE TABLE IF NOT EXISTS [Group_Table] ([GID] NUMBER(10) PRIMARY KEY NOT NULL, [Users] VARCHAR(1000) CONSTRAINT [Users] REFERENCES [SchoolFellow_Table]([SID]), [GroupOwners] VARCHAR(1000) CONSTRAINT [GroupOwners] REFERENCES [SchoolFellow_Table]([SID]), [GourpName] VARCHAR(30), [CreatDate] DATETIME, [Remark] VARCHAR(100), [LastUpdate] DATETIME);");
		// GroupMember_Table
		arg0.execSQL("CREATE TABLE IF NOT EXISTS [GroupMember_Table] ([GroupMemberID] VARCHAR(100) NOT NULL, [GroupMemberName] VARCHAR(20), [GroupID] VARCHAR(20), [OwnerID] VARCHAR(20), PRIMARY KEY([GroupMemberID],[GroupID]));");

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

		// chatitem
		arg0.execSQL("CREATE TABLE IF NOT EXISTS chatitem(id integer primary key AutoIncrement,owner VARCHAR(80),icon VARCHAR(80),cType INT,name VARCHAR(20),friendAccount VARCHAR(80),cTime VARCHAR(80),latestMessage VARCHAR(80),unread INT,userAccount VARCHAR(80))");

		// chatmessage
		arg0.execSQL("CREATE TABLE IF NOT EXISTS chatmessage(id integer primary key AutoIncrement,mid VARCHAR(80),icon VARCHAR(80),mType INT,userAccount VARCHAR(50),senderAccount VARCHAR(50),recvAccount VARCHAR(50),mTime VARCHAR(80),messageContent VARCHAR(200),owner VARCHAR(50),isRead INT)");

		// singlenewsmessage
		arg0.execSQL("CREATE TABLE IF NOT EXISTS singlenewsmessage(nid INT(10) PRIMARY KEY,icon VARCHAR(80),isBreaking INT,title VARCHAR(80),summary VARCHAR(80),smTime VARCHAR(80),newsUrl VARCHAR(80),channelId VARCHAR(80),PMId VARCHAR(80),content VARCHAR(200))");
		CYLog.i("dbOpenHelper", "tables created");
		// StudentsInfo 联系人信息
		arg0.execSQL("CREATE TABLE IF NOT EXISTS StudentsInfo (nid integer primary key AutoIncrement,userAccount VARCHAR(50),className VARCHAR(50),accountNum VARCHAR(50),address CHAR(50),authenticated CHAR(1),baseInfoId CHAR(50),channels CHAR(150),email CHAR(20),id CHAR(10),intrestType CHAR(150),name CHAR(10),phoneNum CHAR(15),picture CHAR(150),sex CHAR(1),sign CHAR(50),hasAllClassmates INT);");
		// UserSelfInfo 使用app登陆的人
		arg0.execSQL("CREATE TABLE IF NOT EXISTS UserSelfInfo (nid integer primary key AutoIncrement,userAccount VARCHAR(50),className VARCHAR(50),accountNum VARCHAR(50),address CHAR(50),authenticated CHAR(1),baseInfoId CHAR(50),channels CHAR(150),email CHAR(20),id CHAR(10),intrestType CHAR(150),name CHAR(10),"
				+ "phoneNum CHAR(15),picture CHAR(150),sex CHAR(1),sign CHAR(50),groupName VARCHAR(500),password CHAR(50),hasAllClassmates INT);");
		// StudentsSample
		arg0.execSQL("CREATE TABLE IF NOT EXISTS [StudentsSample] ([SNum] integer primary key  AutoIncrement,[Name] CHAR(10) NOT NULL UNIQUE);");

		// 插入校友帮帮忙初始数据
		arg0.execSQL("INSERT INTO Channel_Table(ChannelID,ChannelName,channelRemark,Icon) VALUES('校友帮帮忙','校友帮帮忙','找人才、找工作、找项目、找资金，……，校友来帮你','"
				+ R.drawable.schoolhelper_main + "')");

		arg0.execSQL("INSERT INTO chatmessage(mid,mType,userAccount,messageContent,owner,icon) VALUES('帮帮忙-招聘',"
				+ ChatMessage.SCHOOLHELPER
				+ ",'招聘信息','"
				+ APPConstant.getURL()
				+ "/mobile/messageBoard/messageBoardAction!initMessageList.action?messageBoard.messageType=1&messageBoard.messageUserId=','校友帮帮忙','"
				+ R.drawable.schoolhelper_offer + "')");
		arg0.execSQL("INSERT INTO chatmessage(mid,mType,userAccount,messageContent,owner,icon) VALUES('帮帮忙-项目',"
				+ ChatMessage.SCHOOLHELPER
				+ ",'项目信息','"
				+ APPConstant.getURL()
				+ "/mobile/messageBoard/messageBoardAction!initMessageList.action?messageBoard.messageType=4&messageBoard.messageUserId=','校友帮帮忙','"
				+ R.drawable.schoolhelper_project + "')");
		arg0.execSQL("INSERT INTO chatmessage(mid,mType,userAccount,messageContent,owner,icon) VALUES('帮帮忙-资金',"
				+ ChatMessage.SCHOOLHELPER
				+ ",'资金信息','"
				+ APPConstant.getURL()
				+ "/mobile/messageBoard/messageBoardAction!initMessageList.action?messageBoard.messageType=3&messageBoard.messageUserId=','校友帮帮忙','"
				+ R.drawable.schoolhelper_fund + "')");
		arg0.execSQL("INSERT INTO chatmessage(mid,mType,userAccount,messageContent,owner,icon) VALUES('帮帮忙-求职',"
				+ ChatMessage.SCHOOLHELPER
				+ ",'求职信息','"
				+ APPConstant.getURL()
				+ "/mobile/messageBoard/messageBoardAction!initMessageList.action?messageBoard.messageType=2&messageBoard.messageUserId=','校友帮帮忙','"
				+ R.drawable.schoolhelper_jobwanting + "')");
		arg0.execSQL("INSERT INTO chatmessage(mid,mType,userAccount,messageContent,owner,icon) VALUES('帮帮忙-其他',"
				+ ChatMessage.SCHOOLHELPER
				+ ",'其他信息','"
				+ APPConstant.getURL()
				+ "/mobile/messageBoard/messageBoardAction!initMessageList.action?messageBoard.messageType=99&messageBoard.messageUserId=','校友帮帮忙','"
				+ R.drawable.schoolhelper_other + "')");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
