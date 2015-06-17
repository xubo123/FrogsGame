package com.hust.schoolmatechat.engine;

import java.io.File;

import android.os.Environment;

import com.hust.schoolmatechat.R;

/**
 * 测试服务器
 * @author Administrator
 *
 */
public class APPConstant {
	private APPConstant() {
	}

	/** 调试模式开关，只能窗友公司测试版可以设置为true，学校的只能部署调试时使用，否则为false */
	public static final boolean DEBUG_MODE = false;
	

/*********************以上是更换学校需要修改的参数**************************/
	

	/** 窗友主页 */
	public static final String INDEXURL = "http://www.cy199.cn/";
	
	public static final String BASEINFOURL = "http://121.40.119.186:8760/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action?";
	
	/** 意见反馈 */
	public static final String FEEDBACKURL = APPBaseInfo.URL+"/mobile/messageBoard/messageBoardAction!initFeedBack.action?messageBoard.messageType=404&messageBoard.messageUserId=";

	/** 版本更新url */
	public static final String UPDATEURL = "/clientRelease/clientReleaseAction!doNotNeedSessionAndSecurity_getNewestVersion.action?checkcode=12345";
	/** app下载默认url */
	public static final String DOWNLOADURL = "/file/20141103/20141103112458_424.apk";
	/** 请求服务器url */
	public static final String USERURL = "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action?";

	/** 主动取新闻url */
	public static final String GETNEWSURL = "/news/newsAction!doNotNeedSessionAndSecurity_getRegularNews.action";

	/** 主动取频道url */
	public static final String GETCHANNELURL = "/newsChannel/newsChannelAction!doNotNeedSessionAndSecurity_getAllChannels.action";
	
	/** 校友挖掘url */
	public static final String DATADIGGER = "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action?";

	/** 默认阅读兴趣 */
	public static final String[] INTERESTLIST = { "要闻", "推荐", "新闻", "资讯" };
	/** 默认频道 */
	public static final String[] CHANNELLIST = { "母校新闻", "总会快递" };
	/*
	 * 命令列表
	 */
	/** 文件上传 */
	public static final String USER_PROFILE_UPLOAD_FILE = "1";

	/** 根据姓名获取与此姓名对应的基础信息数据库id编号、身份证号(同班同名的时候)、账号列表 */
	public static final String USER_PROFILE_GET_USER_BASE_INFO_ID_LIST = "2";

	/** 根据手机号获取注册码 */
	public static final String USER_PROFILE_GET_REGISTER_CODE = "3";

	/** 提交注册信息 */
	public static final String USER_PROFILE_REGISTER = "4";

	/** 用户信息更改 */
	public static final String USER_PROFILE_UPDATE_USER_PROFILE = "5";

	/** 用户图像上传 */
	public static final String USER_PROFILE_IMAGE_FILE_UPLOAD = "6";

	/** 后台用于通知某用户获得认证 */
	public static final String USER_PROFILE_USER_AUTHENTICATED = "7";

	/** 获取班级同学id、姓名、账号列表 */
	public static final String USER_PROFILE_GET_CLASSMATES_INFO_LIST = "8";

	/** 搜索用户 */
	public static final String USER_PROFILE_SEARCH_FOR_USERS = "9";

	/** 根据账号和密码获取用户自己的基本信息 */
	public static final String USER_PROFILE_GET_USER_SELF_PROFILE = "10";

	/** 根据账号和密码获取用户好友的基本信息 */
	public static final String USER_PROFILE_GET_FRIEND_PROFILE = "11";

	/** 修改Web服务器上群表 */
	public static final String USER_PROFILE_UPDATE_GROUP_INFO = "12";

	/** 根据账号、密码、群名获取群成员账号信息 */
	public static final String USER_PROFILE_GET_GROUP_INFO = "13";

	/** 数据挖掘更新班级同学手机号，并发送注册邀请短信 */
	public static final String USER_PROFILE_UPDATE_CLASSMATE_TEL = "14";

	/** 用户认证 */
	public static final String USER_PROFILE_AUTHENTICATED = "15";

	/** 修改密码 */
	public static final String USER_PROFILE_CHANGE_PASSWORD = "16";
	
	/** 批量获取群组非好友成员的信息  账号，姓名，图片地址 */
	public static final String USER_PROFILE_GET_GROUP_MEMBERS_INFO = "17";
	
	/** 根据机构id获取机构名称 */
	public static final String USER_PROFILE_GET_DEPART_FULL_NAME = "18";

	/** 获取窗友支持的学校 */
	public static final String USER_PROFILE_GET_SUPPORTED_SCHOOLS = "19";

	/** 根据学校编号获取学校配置信息 */
	public static final String USER_PROFILE_GET_SCHOOL_CONFIGS = "20";
	
	/** 主通道名称，监听通道变更信息 */
	public static final String GENERAL_CHANNEL_NAME = "GeneralChannelName";

	/** 校友帮帮忙，通道名称 */
	public static final String SCHOOL_HELPER_CHANNEL_NAME = "校友帮帮忙";
	public static final String[] SCHOOL_HELPER_TYPE = { "招聘信息", "项目信息", "资金信息",
			"求职信息", "其他信息" };
	public static final String[] SCHOOL_HELPER_URL = {
			"/mobile/messageBoard/messageBoardAction!initMessageList.action?messageBoard.messageType=1&messageBoard.messageUserId=",
			"/mobile/messageBoard/messageBoardAction!initMessageList.action?messageBoard.messageType=4&messageBoard.messageUserId=",
			"/mobile/messageBoard/messageBoardAction!initMessageList.action?messageBoard.messageType=3&messageBoard.messageUserId=",
			"/mobile/messageBoard/messageBoardAction!initMessageList.action?messageBoard.messageType=2&messageBoard.messageUserId=",
			"/mobile/messageBoard/messageBoardAction!initMessageList.action?messageBoard.messageType=99&messageBoard.messageUserId=" };
	public static final int[] SCHOOL_HELPER_ICON = {
			R.drawable.schoolhelper_offer, R.drawable.schoolhelper_project,
			R.drawable.schoolhelper_fund, R.drawable.schoolhelper_jobwanting,
			R.drawable.schoolhelper_other };

	/** 阅读兴趣，通道名称 */
	public static final String INTEREST_CHANNEL_NAME = "阅读兴趣";

	/** 聊天、推送服务器连接检查间隔 单位ms */
	public static final int CONNECTION_CHECK_INTERVAL = 10000;

	/** 聊天服务器重连最大计数 */
	public static final int TIGASE_CONNECTION_COUNT_INTERVAL = 6 * 2;// 两分钟重连不上就重启

	/** 推送服务器重连最大计数 */
	public static final int MQTT_CONNECTION_COUNT_INTERVAL = 6 * 5;// 五分钟重连不上就重启

	/** 未登录聊天服务器 */
	public static final int LOGIN_NOT_EXCUTE = 0;

	/** 登录聊天服务器失败 */
	public static final int LOGIN_FAILED = 1;

	/** 无法连接到聊天服务器 */
	public static final int LOGIN_DISCONNECTED = 2;

	/** 登录聊天服务器出现异常 */
	public static final int LOGIN_SYSTEM_ERROR = 3;

	/** 成功登录聊天服务器 */
	public static final int LOGIN_TIGASE_SUCC = 4;

	/** 登录聊天服务器还未完成, 正在登录 */
	public static final int LOGIN_NOT_COMPLETE = 5;
	
	/** 主动放弃登陆聊天服务器 */
	public static final int LOGIN_ADOPT = 5;

	/** 用户个人图像URI */
	public static final String PERSON_SELF_PHOTO = "file://mnt/sdcard/chuangyou/picture/photo.png";

	/** 聊天文件URI */
	public static final String CHAT_FILE = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "chuangyou" + File.separator + "file";

	/** 聊天文件发送相关指令 */
	public static final int PICTURE = 1;
	public static final int AUDIO = 2;
	public static final int VIDEO = 3;
	public static final int NORMAL_FILE = 4;
	public static final String SPICTURE = "[图片]";
	public static final String SAUDIO = "[音频]";
	public static final String SVIDEO = "[视频]";
	public static final String SNORMAL_FILE = "[文件]";

	/** MainActivity中 Handler Message 标志 */
	public static final int GROUPCHAT_INV = 101;
	public static final int GROUPCHAT_EXIT = 102;
	public static final int FRIEND_ADD = 103;
	public static final int NETWORK_UN = 104;
	public static final int NETWORK_OK = 105;
	public static final int NETWORK_NO = 106;
	
	/** 上传下载文件 Handler Message 标志 */
	public static final int UPLOAD_STARTED = 701;
	public static final int UPDATE_UPLOAD_PROGRESS = 702;
	public static final int UPLOAD_FINISHED = 703;
	public static final int DOWNLOAD_STARTED = 704;
	public static final int UPDATE_DOWNLOAD_PROGRESS = 705;
	public static final int DOWNLOAD_FINISHED = 706;
	
	/**
	 * 所有的特殊消息以命令的形式发送
	 * 命令的格式为:前缀 #_#_ + type_ + 自定义数据的形式 
	 * type为0好友图片信息更新，1为聊天室邀请，2为接受聊天室邀请，3为拒绝聊天室邀请，4聊天文件发送
	 */
	public static final String CMD_PREFIX = "#_#_";
	
	/** 好友信息更新 */
	public static final String CMD_PREFIX_UPDATE_FRIEND_INFO = "#_#_0_";
	/** 聊天室邀请命令前缀 */
	public static final String CMD_PREFIX_GROUPCHAT_INVITE = "#_#_1_";
	/** 接受聊天室邀请命令前缀 */
	public static final String CMD_PREFIX_GROUPCHAT_ACCEPT_INVITE = "#_#_2_";
	/** 拒绝聊天室邀请命令前缀 */
	public static final String CMD_PREFIX_GROUPCHAT_DECLINE_INVITE = "#_#_3_";
	/** 删除聊天室命令前缀*/
	public static final String CMD_PREFIX_GROUPCHAT_DELETE = "#_#_6_";
	/** 踢人(退群)命令前缀 */
	public static final String CMD_PREFIX_GROUPCHAT_KICK = "#_#_5_";
	
	/** 重新创建节点后，强制群内成员订阅节点 */
	public static final String CMD_PREFIX_FORCE_SUBSCRIBE = "#_#_7_";
	
	/** 加好友请求 account_name_class */
	public static final String CMD_PREFIX_FRIEND_ADD_REQUEST = "#_#_8_";
	
	/** 同意加好友 account_name_class */
	public static final String CMD_PREFIX_FRIEND_ADD_AGREE = "#_#_9_";
	
	/** 拒绝加好友或删除好友 account_name_class */
	public static final String CMD_PREFIX_FRIEND_ADD_DECLINE = "#_#_10_";
	
	/** 聊天文件发送前缀 */
	public static final String CMD_PREFIX_FILE_SEND = "#_file_#";
	
	/** 聊天文件正在发送和接收时进度条显示的前缀 */
	public static final String FILE_SEND_TMP = "#_filetmp_#";
	
	/** 皮肤，各个界面背景资源 */
	//
	public static final int[] MYSELFFRAGMENT_BG_COLOR_IDS = {
			R.color.skin_background_color, R.color.skin_background_color,
			R.color.skin_background_color };
	public static final int[] NEWSFRAGMENT_BG_COLOR_IDS = {
			R.color.skin_background_color, R.color.skin_background_color,
			R.color.skin_background_color };
	public static final int[] CONTACTSFRAGMENT_BG_COLOR_IDS = {
			R.color.skin_background_color, R.color.skin_background_color,
			R.color.skin_background_color };
	public static final int[] CHATFRAGMENT_BG_COLOR_IDS = {
			R.color.skin_background_color, R.color.skin_background_color,
			R.color.skin_background_color };
//
//	public static String getIP() {//tig
//			return APPBaseInfo.TigaseIP;
//	}

	public static String getUPDATEURL() {
		return APPBaseInfo.URL + UPDATEURL;
	}

//	public static String getNEWSURL() {
//		return APPBaseInfo.URL + GETNEWSURL;
//	}

//	public static String getCHANNELURL() {
//		return APPBaseInfo.URL + GETCHANNELURL;
//	}

	public static String getUSERURL() {
		return APPBaseInfo.URL + USERURL;
	}

	public static String getMQTTServiceURL() {
		return APPBaseInfo.MQTTServiceURL;
	}

	public static String getDOWNLOADURL() {
		return APPBaseInfo.URL + DOWNLOADURL;
	}

	public static String getURL() {
		return APPBaseInfo.URL;
	}

	public static String getURL8088() {
		return APPBaseInfo.URL8088;
	}

	public static String getChannelURL1() {
		return APPBaseInfo.URL8088 + "/image/20141112/20141112075946_39.gif";
	}

	public static String getChannelURL2() {
		return APPBaseInfo.URL8088 + "/image/20141112/20141112080435_373.jpg";
	}

	public static String getCYDBNAME() {
		return "cy_android.db";
	}

	public static String getURIDataDigger() {
		return APPBaseInfo.URL+DATADIGGER;
	}
}
