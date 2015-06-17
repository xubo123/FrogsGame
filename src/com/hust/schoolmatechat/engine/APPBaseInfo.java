package com.hust.schoolmatechat.engine;

public class APPBaseInfo {
	/**  MQTT 服务器  */
	public static String MQTTServiceURL = "tcp://219.140.177.108:1883";//MQTT服务器ip与端口号
	/** tigase 服务器 */
	public static String TigaseIP = "219.140.177.108";
	/** web 服务器 */
	public static  String URL = "http://219.140.177.108:9080";
	public static  String URL8088 = "http://219.140.177.108:8088";//文件服务器

	/** 获取手机注册码秘钥 */
	public static String REGISTER_CODE_SECRET_KEY = "getRegisterCode123";
	/** 基础数据对学校的编号 */
	public static Object SCHOOL_ID_NUMBER = "000150";
	/** 推送服务的帐号 */
	public static String PUSH_SERVICE_ACCOUNT = "system_hust";
	/** 推送服务的密码 */
	public static String PUSH_SERVICE_PASSWD= "manager_hust";
	/** 聊天服务器domain */
	public static String TIGASE_SERVER_DOMAIN = "hust";
	/** 聊天服务器端口 */
	public static int TIGASE_SERVER_PORT = 5222;
	/** 所属学校 */
	public static String SCHOOLNAME = "窗友";
	/** 校友会联系电话 */
	public static String SCHOOLPHONE = "02787542502";
	public static String FLAG = null;
	
}
