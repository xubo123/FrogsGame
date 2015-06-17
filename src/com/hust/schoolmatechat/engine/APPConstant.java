package com.hust.schoolmatechat.engine;

import java.io.File;

import android.os.Environment;

import com.hust.schoolmatechat.R;

/**
 * ���Է�����
 * @author Administrator
 *
 */
public class APPConstant {
	private APPConstant() {
	}

	/** ����ģʽ���أ�ֻ�ܴ��ѹ�˾���԰��������Ϊtrue��ѧУ��ֻ�ܲ������ʱʹ�ã�����Ϊfalse */
	public static final boolean DEBUG_MODE = false;
	

/*********************�����Ǹ���ѧУ��Ҫ�޸ĵĲ���**************************/
	

	/** ������ҳ */
	public static final String INDEXURL = "http://www.cy199.cn/";
	
	public static final String BASEINFOURL = "http://121.40.119.186:8760/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action?";
	
	/** ������� */
	public static final String FEEDBACKURL = APPBaseInfo.URL+"/mobile/messageBoard/messageBoardAction!initFeedBack.action?messageBoard.messageType=404&messageBoard.messageUserId=";

	/** �汾����url */
	public static final String UPDATEURL = "/clientRelease/clientReleaseAction!doNotNeedSessionAndSecurity_getNewestVersion.action?checkcode=12345";
	/** app����Ĭ��url */
	public static final String DOWNLOADURL = "/file/20141103/20141103112458_424.apk";
	/** ���������url */
	public static final String USERURL = "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action?";

	/** ����ȡ����url */
	public static final String GETNEWSURL = "/news/newsAction!doNotNeedSessionAndSecurity_getRegularNews.action";

	/** ����ȡƵ��url */
	public static final String GETCHANNELURL = "/newsChannel/newsChannelAction!doNotNeedSessionAndSecurity_getAllChannels.action";
	
	/** У���ھ�url */
	public static final String DATADIGGER = "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action?";

	/** Ĭ���Ķ���Ȥ */
	public static final String[] INTERESTLIST = { "Ҫ��", "�Ƽ�", "����", "��Ѷ" };
	/** Ĭ��Ƶ�� */
	public static final String[] CHANNELLIST = { "ĸУ����", "�ܻ���" };
	/*
	 * �����б�
	 */
	/** �ļ��ϴ� */
	public static final String USER_PROFILE_UPLOAD_FILE = "1";

	/** ����������ȡ���������Ӧ�Ļ�����Ϣ���ݿ�id��š����֤��(ͬ��ͬ����ʱ��)���˺��б� */
	public static final String USER_PROFILE_GET_USER_BASE_INFO_ID_LIST = "2";

	/** �����ֻ��Ż�ȡע���� */
	public static final String USER_PROFILE_GET_REGISTER_CODE = "3";

	/** �ύע����Ϣ */
	public static final String USER_PROFILE_REGISTER = "4";

	/** �û���Ϣ���� */
	public static final String USER_PROFILE_UPDATE_USER_PROFILE = "5";

	/** �û�ͼ���ϴ� */
	public static final String USER_PROFILE_IMAGE_FILE_UPLOAD = "6";

	/** ��̨����֪ͨĳ�û������֤ */
	public static final String USER_PROFILE_USER_AUTHENTICATED = "7";

	/** ��ȡ�༶ͬѧid���������˺��б� */
	public static final String USER_PROFILE_GET_CLASSMATES_INFO_LIST = "8";

	/** �����û� */
	public static final String USER_PROFILE_SEARCH_FOR_USERS = "9";

	/** �����˺ź������ȡ�û��Լ��Ļ�����Ϣ */
	public static final String USER_PROFILE_GET_USER_SELF_PROFILE = "10";

	/** �����˺ź������ȡ�û����ѵĻ�����Ϣ */
	public static final String USER_PROFILE_GET_FRIEND_PROFILE = "11";

	/** �޸�Web��������Ⱥ�� */
	public static final String USER_PROFILE_UPDATE_GROUP_INFO = "12";

	/** �����˺š����롢Ⱥ����ȡȺ��Ա�˺���Ϣ */
	public static final String USER_PROFILE_GET_GROUP_INFO = "13";

	/** �����ھ���°༶ͬѧ�ֻ��ţ�������ע��������� */
	public static final String USER_PROFILE_UPDATE_CLASSMATE_TEL = "14";

	/** �û���֤ */
	public static final String USER_PROFILE_AUTHENTICATED = "15";

	/** �޸����� */
	public static final String USER_PROFILE_CHANGE_PASSWORD = "16";
	
	/** ������ȡȺ��Ǻ��ѳ�Ա����Ϣ  �˺ţ�������ͼƬ��ַ */
	public static final String USER_PROFILE_GET_GROUP_MEMBERS_INFO = "17";
	
	/** ���ݻ���id��ȡ�������� */
	public static final String USER_PROFILE_GET_DEPART_FULL_NAME = "18";

	/** ��ȡ����֧�ֵ�ѧУ */
	public static final String USER_PROFILE_GET_SUPPORTED_SCHOOLS = "19";

	/** ����ѧУ��Ż�ȡѧУ������Ϣ */
	public static final String USER_PROFILE_GET_SCHOOL_CONFIGS = "20";
	
	/** ��ͨ�����ƣ�����ͨ�������Ϣ */
	public static final String GENERAL_CHANNEL_NAME = "GeneralChannelName";

	/** У�Ѱ��æ��ͨ������ */
	public static final String SCHOOL_HELPER_CHANNEL_NAME = "У�Ѱ��æ";
	public static final String[] SCHOOL_HELPER_TYPE = { "��Ƹ��Ϣ", "��Ŀ��Ϣ", "�ʽ���Ϣ",
			"��ְ��Ϣ", "������Ϣ" };
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

	/** �Ķ���Ȥ��ͨ������ */
	public static final String INTEREST_CHANNEL_NAME = "�Ķ���Ȥ";

	/** ���졢���ͷ��������Ӽ���� ��λms */
	public static final int CONNECTION_CHECK_INTERVAL = 10000;

	/** ������������������� */
	public static final int TIGASE_CONNECTION_COUNT_INTERVAL = 6 * 2;// �������������Ͼ�����

	/** ���ͷ��������������� */
	public static final int MQTT_CONNECTION_COUNT_INTERVAL = 6 * 5;// ������������Ͼ�����

	/** δ��¼��������� */
	public static final int LOGIN_NOT_EXCUTE = 0;

	/** ��¼���������ʧ�� */
	public static final int LOGIN_FAILED = 1;

	/** �޷����ӵ���������� */
	public static final int LOGIN_DISCONNECTED = 2;

	/** ��¼��������������쳣 */
	public static final int LOGIN_SYSTEM_ERROR = 3;

	/** �ɹ���¼��������� */
	public static final int LOGIN_TIGASE_SUCC = 4;

	/** ��¼�����������δ���, ���ڵ�¼ */
	public static final int LOGIN_NOT_COMPLETE = 5;
	
	/** ����������½��������� */
	public static final int LOGIN_ADOPT = 5;

	/** �û�����ͼ��URI */
	public static final String PERSON_SELF_PHOTO = "file://mnt/sdcard/chuangyou/picture/photo.png";

	/** �����ļ�URI */
	public static final String CHAT_FILE = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ File.separator
			+ "chuangyou" + File.separator + "file";

	/** �����ļ��������ָ�� */
	public static final int PICTURE = 1;
	public static final int AUDIO = 2;
	public static final int VIDEO = 3;
	public static final int NORMAL_FILE = 4;
	public static final String SPICTURE = "[ͼƬ]";
	public static final String SAUDIO = "[��Ƶ]";
	public static final String SVIDEO = "[��Ƶ]";
	public static final String SNORMAL_FILE = "[�ļ�]";

	/** MainActivity�� Handler Message ��־ */
	public static final int GROUPCHAT_INV = 101;
	public static final int GROUPCHAT_EXIT = 102;
	public static final int FRIEND_ADD = 103;
	public static final int NETWORK_UN = 104;
	public static final int NETWORK_OK = 105;
	public static final int NETWORK_NO = 106;
	
	/** �ϴ������ļ� Handler Message ��־ */
	public static final int UPLOAD_STARTED = 701;
	public static final int UPDATE_UPLOAD_PROGRESS = 702;
	public static final int UPLOAD_FINISHED = 703;
	public static final int DOWNLOAD_STARTED = 704;
	public static final int UPDATE_DOWNLOAD_PROGRESS = 705;
	public static final int DOWNLOAD_FINISHED = 706;
	
	/**
	 * ���е�������Ϣ���������ʽ����
	 * ����ĸ�ʽΪ:ǰ׺ #_#_ + type_ + �Զ������ݵ���ʽ 
	 * typeΪ0����ͼƬ��Ϣ���£�1Ϊ���������룬2Ϊ�������������룬3Ϊ�ܾ����������룬4�����ļ�����
	 */
	public static final String CMD_PREFIX = "#_#_";
	
	/** ������Ϣ���� */
	public static final String CMD_PREFIX_UPDATE_FRIEND_INFO = "#_#_0_";
	/** ��������������ǰ׺ */
	public static final String CMD_PREFIX_GROUPCHAT_INVITE = "#_#_1_";
	/** ������������������ǰ׺ */
	public static final String CMD_PREFIX_GROUPCHAT_ACCEPT_INVITE = "#_#_2_";
	/** �ܾ���������������ǰ׺ */
	public static final String CMD_PREFIX_GROUPCHAT_DECLINE_INVITE = "#_#_3_";
	/** ɾ������������ǰ׺*/
	public static final String CMD_PREFIX_GROUPCHAT_DELETE = "#_#_6_";
	/** ����(��Ⱥ)����ǰ׺ */
	public static final String CMD_PREFIX_GROUPCHAT_KICK = "#_#_5_";
	
	/** ���´����ڵ��ǿ��Ⱥ�ڳ�Ա���Ľڵ� */
	public static final String CMD_PREFIX_FORCE_SUBSCRIBE = "#_#_7_";
	
	/** �Ӻ������� account_name_class */
	public static final String CMD_PREFIX_FRIEND_ADD_REQUEST = "#_#_8_";
	
	/** ͬ��Ӻ��� account_name_class */
	public static final String CMD_PREFIX_FRIEND_ADD_AGREE = "#_#_9_";
	
	/** �ܾ��Ӻ��ѻ�ɾ������ account_name_class */
	public static final String CMD_PREFIX_FRIEND_ADD_DECLINE = "#_#_10_";
	
	/** �����ļ�����ǰ׺ */
	public static final String CMD_PREFIX_FILE_SEND = "#_file_#";
	
	/** �����ļ����ڷ��ͺͽ���ʱ��������ʾ��ǰ׺ */
	public static final String FILE_SEND_TMP = "#_filetmp_#";
	
	/** Ƥ�����������汳����Դ */
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
