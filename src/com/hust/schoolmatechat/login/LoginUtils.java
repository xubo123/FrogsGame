package com.hust.schoolmatechat.login;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Message;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hust.schoolmatechat.ChatMsgservice.GroupChatRoomEntity;
import com.hust.schoolmatechat.engine.APPConstant;
import com.hust.schoolmatechat.engine.CYLog;
import com.hust.schoolmatechat.entity.ContactsEntity;
import com.hust.schoolmatechat.utils.StreamUtils;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class LoginUtils {

	private static Context context = null;
	private static String TAG = "LoginUtils";

	public LoginUtils(Context context) {
		super();
		this.context = context;
	}

	/**
	 * 登陆web服务器认证,返回获取的json格式字符串
	 * 
	 * @param accountNum
	 *            为手机号或者账号
	 * @param password
	 * @return
	 */
	public static String loginOnMainServer(String accountNum, String password) {
		try {
			HttpClient client = new DefaultHttpClient();

			CYLog.i(TAG, "ready to login:" + accountNum);

			String path = APPConstant.getURL()
					+ "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";

			JSONObject jStr = new JSONObject();
			jStr.put("command", APPConstant.USER_PROFILE_GET_USER_SELF_PROFILE);

			JSONObject jContent = new JSONObject();
			jContent.put("accountNum", accountNum);
			jContent.put("password", password);

			jStr.put("content", jContent);

			String paramJson = URLEncoder.encode(jStr.toString());

			HttpGet httpGet = new HttpGet(path + "?jsonStr=" + paramJson);
			HttpResponse response = client.execute(httpGet);

			InputStream in = response.getEntity().getContent();
			byte[] resultBytes = StreamUtils.getBytes(in);
			String resultJson = new String(resultBytes);

			CYLog.i(TAG, jStr+"///"+resultJson);

			return resultJson;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			Toast.makeText(context, "网络异常，登录服务器失败，请稍后重试",
					Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	/*
	 * {"msg":"查询成功!","obj":{"accountNum":"liuxiaohong","address":"湖北 武汉",
	 * "authenticated"
	 * :"0","baseInfoId":"0001500010199501001","channels":"华工科技,华工风采,时代之音"
	 * ,"id":85
	 * ,"intrestType":"新闻,资讯,体育","name":"刘晓红","phoneNum":"13317159192","picture"
	 * :
	 * "http://219.140.177.108:8088/face_image/13.png","sex":"1","sign":"重新出发"},
	 * "success":true}
	 */
	public static boolean isSuccess(String jResult) {
		try {
			JSONTokener jsonTokener = new JSONTokener(jResult);
			JSONObject resultObj = (JSONObject) jsonTokener.nextValue();
			boolean success = resultObj.getBoolean("success");
			return success;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			Toast.makeText(context, "网络异常，无法获取信息，请稍后重试",
					Toast.LENGTH_SHORT).show();
			return false;
		}

	}

	/**
	 * 到web服务器获取好友信息 警告 !!!!!!! 信息中没有设置好友所在分组信息 !!!!!
	 */
	public static ContactsEntity getFriendInfo(String accountNum,
			String password, String friendAccount) {
		try {
			HttpClient client = new DefaultHttpClient();

			CYLog.i(TAG, "ready to get FriendInfo" + friendAccount);

			String path = APPConstant.getURL()
					+ "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";

			JSONObject jStr = new JSONObject();
			jStr.put("command", APPConstant.USER_PROFILE_GET_FRIEND_PROFILE);

			JSONObject jContent = new JSONObject();
			jContent.put("accountNum", accountNum);
			jContent.put("password", password);
			jContent.put("friendAccount", friendAccount);

			jStr.put("content", jContent);

			String paramJson = URLEncoder.encode(jStr.toString());

			HttpGet httpGet = new HttpGet(path + "?jsonStr=" + paramJson);
			HttpResponse response = client.execute(httpGet);

			InputStream in = response.getEntity().getContent();
			byte[] resultBytes = StreamUtils.getBytes(in);
			String resultJson = new String(resultBytes);

			ContactsEntity contactsEntity = jsonToContacsEntity(resultJson,
					accountNum, password);
			if (contactsEntity != null) {
				contactsEntity.setHasAllClassmates(1);// 联系人有详细信息
			}

			return contactsEntity;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			Toast.makeText(context, "网络异常，无法获取信息，请稍后重试",
					Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	/*
	 * 到web服务器获取好友信息 警告 !!!!!!! 信息中没有设置好友所在分组信息 !!!!!
	 */
	public static ContactsEntity getFriendInfoByBaseInfoId(String accountNum,
			String password, String baseInfoId) {
		try {
			HttpClient client = new DefaultHttpClient();

			CYLog.i(TAG, "ready to get baseInfoId" + baseInfoId);

			String path = APPConstant.getURL()
					+ "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";

			JSONObject jStr = new JSONObject();
			jStr.put("command", APPConstant.USER_PROFILE_GET_FRIEND_PROFILE);

			JSONObject jContent = new JSONObject();
			jContent.put("accountNum", accountNum);
			jContent.put("password", password);
			jContent.put("baseInfoId", baseInfoId);

			jStr.put("content", jContent);

			CYLog.i(TAG,path+"\\\\"+jStr.toString());

			String paramJson = URLEncoder.encode(jStr.toString());

			HttpGet httpGet = new HttpGet(path + "?jsonStr=" + paramJson);
			HttpResponse response = client.execute(httpGet);

			InputStream in = response.getEntity().getContent();
			byte[] resultBytes = StreamUtils.getBytes(in);
			String resultJson = new String(resultBytes);

			ContactsEntity contactsEntity = jsonToContacsEntity(resultJson,
					accountNum, password);
			if (contactsEntity != null) {
				contactsEntity.setHasAllClassmates(1);// 联系人有详细信息
			}

			return contactsEntity;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			Toast.makeText(context, "网络异常，无法获取信息，请稍后重试",
					Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	/**
	 * 解析http的json结果 警告 !!!!! 联系人所在的分组没有设置!!!!!!
	 * 
	 * @param resultJson
	 * @param userAccount
	 * @param password
	 * @return
	 */
	public static ContactsEntity jsonToContacsEntity(String resultJson,
			String userAccount, String password) {
		try {
			JSONTokener jsonTokener = new JSONTokener(resultJson);
			JSONObject resultJO = (JSONObject) jsonTokener.nextValue();
			boolean success = resultJO.getBoolean("success");
			if (success) {
				JSONObject jsonObject = resultJO.getJSONObject("obj");
				ContactsEntity contactsEntity = jsonObjectToContactsEntity(jsonObject, userAccount);
				return contactsEntity;	
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			return null;
		}
	}

	/**
	 * 使用JSONObject的旧版本
	 * @param resultObj
	 * @param userAccount
	 * @return
	 */
	public static ContactsEntity jsonObjectToContactsEntity(
			JSONObject resultObj, String userAccount) {
		try {
			ContactsEntity contactsEntity = new ContactsEntity();// 联系人信息入库

			// userAccount和accountNum不一定就是一样的
			contactsEntity.setUserAccount(userAccount);

			// 登陆账号存在大小写不区分问题,web验证时需要使用accountNum而非userAccount
			if (resultObj.has("accountNum")) {
				String accountNum = resultObj.getString("accountNum");
				contactsEntity.setAccountNum(accountNum);
			}

			if (resultObj.has("authenticated")) {
				String authenticated = resultObj.getString("authenticated");
				contactsEntity.setAuthenticated(authenticated);
			}

			if (resultObj.has("address")) {
				String address = resultObj.getString("address");
				contactsEntity.setAddress(address);
			} else {
				contactsEntity.setAddress("");
			}

			if (resultObj.has("baseInfoId")) {
				String baseInfo = resultObj.getString("baseInfoId");// 基础信息id列表
				contactsEntity.setBaseInfoId(baseInfo);
			} else {
				contactsEntity.setBaseInfoId("");
			}

			if (resultObj.has("channels")) {
				String channels = resultObj.getString("channels");
				contactsEntity.setChannels(channels);
			} else {
				contactsEntity.setChannels("");
			}

			if (resultObj.has("email")) {
				String email = resultObj.getString("email");
				contactsEntity.setEmail(email);
			} else {
				contactsEntity.setEmail("");
			}

			if (resultObj.has("groupName")) {// 好友信息没有这个字段
				String groupName = resultObj.getString("groupName");
				contactsEntity.setGroupName(groupName);
				CYLog.i(TAG, "本账号个人信息groupName=" + groupName);
			}

			if (resultObj.has("className")) {// web服务器没有这个字段
				String className = resultObj.getString("className");
				contactsEntity.setClassName(className);
			}

//			if (resultObj.has("id")) {
//				int numId = resultObj.getInt("id");
//				contactsEntity.setId("" + numId);
//			}

			if (resultObj.has("intrestType")) {
				String intrestType = resultObj.getString("intrestType");
				contactsEntity.setIntrestType(intrestType);
			} else {
				contactsEntity.setIntrestType("");
			}

			if (resultObj.has("name")) {
				String myName = resultObj.getString("name");
				contactsEntity.setName(myName);
			} else {
				contactsEntity.setName("");
			}

			if (resultObj.has("phoneNum")) {
				String phoneNum = resultObj.getString("phoneNum");
				contactsEntity.setPhoneNum(phoneNum);
			} else {
				contactsEntity.setPhoneNum("");
			}

			if (resultObj.has("picture")) {
				String picture = resultObj.getString("picture");
				contactsEntity.setPicture(picture);
			} else {
				contactsEntity.setPicture("");
			}

			if (resultObj.has("sex")) {
				String sex = resultObj.getString("sex");
				contactsEntity.setSex(sex);
			} else {
				contactsEntity.setSex("");
			}

			if (resultObj.has("sign")) {
				String sign = resultObj.getString("sign");
				contactsEntity.setSign(sign);
			} else {
				contactsEntity.setSign("");
			}

			return contactsEntity;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * jsonObject结构转换为ContactsEntity
	 */
	public static ContactsEntity jsonObjectToContactsEntity(
			JsonObject resultObj, String userAccount) {
		try {
			ContactsEntity contactsEntity = new ContactsEntity();// 联系人信息入库

			// userAccount和accountNum不一定就是一样的
			contactsEntity.setUserAccount(userAccount);

			// 登陆账号存在大小写不区分问题,web验证时需要使用accountNum而非userAccount
			if (resultObj.has("accountNum")) {
				String accountNum = resultObj.get("accountNum").getAsString();
				contactsEntity.setAccountNum(accountNum);
			}

			if (resultObj.has("authenticated")) {
				String authenticated = resultObj.get("authenticated").getAsString();
				contactsEntity.setAuthenticated(authenticated);
			}

			if (resultObj.has("address")) {
				String address = resultObj.get("address").getAsString();
				contactsEntity.setAddress(address);
			} else {
				contactsEntity.setAddress("");
			}

			if (resultObj.has("baseInfoId")) {
				String baseInfo = resultObj.get("baseInfoId").getAsString();// 基础信息id列表
				contactsEntity.setBaseInfoId(baseInfo);
			} else {
				contactsEntity.setBaseInfoId("");
			}

			if (resultObj.has("channels")) {
				String channels = resultObj.get("channels").getAsString();
				contactsEntity.setChannels(channels);
			} else {
				contactsEntity.setChannels("");
			}

			if (resultObj.has("email")) {
				String email = resultObj.get("email").getAsString();
				contactsEntity.setEmail(email);
			} else {
				contactsEntity.setEmail("");
			}

			if (resultObj.has("groupName")) {// 好友信息没有这个字段
				String groupName = resultObj.get("groupName").getAsString();
				contactsEntity.setGroupName(groupName);
				CYLog.i(TAG, "本账号个人信息groupName=" + groupName);
			}

			if (resultObj.has("className")) {// web服务器没有这个字段
				String className = resultObj.get("className").getAsString();
				contactsEntity.setClassName(className);
			}

//			if (resultObj.has("id")) {
//				int numId = resultObj.getInt("id");
//				contactsEntity.setId("" + numId);
//			}

			if (resultObj.has("intrestType")) {
				String intrestType = resultObj.get("intrestType").getAsString();
				contactsEntity.setIntrestType(intrestType);
			} else {
				contactsEntity.setIntrestType("");
			}

			if (resultObj.has("name")) {
				String myName = resultObj.get("name").getAsString();
				contactsEntity.setName(myName);
			} else {
				contactsEntity.setName("");
			}

			if (resultObj.has("phoneNum")) {
				String phoneNum = resultObj.get("phoneNum").getAsString();
				contactsEntity.setPhoneNum(phoneNum);
			} else {
				contactsEntity.setPhoneNum("");
			}

			if (resultObj.has("picture")) {
				String picture = resultObj.get("picture").getAsString();
				contactsEntity.setPicture(picture);
			} else {
				contactsEntity.setPicture("");
			}

			if (resultObj.has("sex")) {
				String sex = resultObj.get("sex").getAsString();
				contactsEntity.setSex(sex);
			} else {
				contactsEntity.setSex("");
			}

			if (resultObj.has("sign")) {
				String sign = resultObj.get("sign").getAsString();
				contactsEntity.setSign(sign);
			} else {
				contactsEntity.setSign("");
			}

			return contactsEntity;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getClassMates(String classId, String accountNum,
			String password) {
		try {
			HttpClient client = new DefaultHttpClient();

			CYLog.i(TAG, "ready to get classmates" + classId);

			String path = APPConstant.getURL()
					+ "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";

			JSONObject jStr = new JSONObject();
			jStr.put("command",
					APPConstant.USER_PROFILE_GET_CLASSMATES_INFO_LIST);

			JSONObject jContent = new JSONObject();
			jContent.put("accountNum", accountNum);
			jContent.put("password", password);
			jContent.put("classId", classId);

			jStr.put("content", jContent);

			String paramJson = URLEncoder.encode(jStr.toString());

			HttpGet httpGet = new HttpGet(path + "?jsonStr=" + paramJson);
			HttpResponse response = client.execute(httpGet);

			InputStream in = response.getEntity().getContent();
			byte[] resultBytes = StreamUtils.getBytes(in);
			String resultJson = new String(resultBytes);

			CYLog.i(TAG, resultJson);

			return resultJson;
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			Toast.makeText(context, "网络异常，获取信息失败，请稍后重试",
					Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	/**
	 * 解析从web服务器上获取到的班级同学的id、账号，姓名,同时设置联系人的默认分组 若联系人已经认证，则获取其详细信息
	 * 
	 * @param classmatesResult
	 * @return
	 */
	public static List<ContactsEntity> handleClassmatesResult(
			String classmatesResult, String userAccount, String password,
			String className) {
		try {
			JSONTokener jsonTokener = new JSONTokener(classmatesResult);
			JSONObject resultJO = (JSONObject) jsonTokener.nextValue();
			boolean success = resultJO.getBoolean("success");
			if (success) {
				JSONArray resultObj = resultJO.getJSONArray("obj");
				List<ContactsEntity> classmateResults = new ArrayList<ContactsEntity>();
				for (int i = 0; i < resultObj.length(); i++) {
					JSONObject cmJO = resultObj.getJSONObject(i);
					ContactsEntity classmateResult;
					String accountNum = "";
					String userId = "";
					String userName = "";
					try {
						if (cmJO.has("accountNum")) {
							accountNum = cmJO.getString("accountNum");
						}
					} catch (JSONException e) {
						e.printStackTrace();
						CYLog.e(TAG, "user account null");
						accountNum = null;
					}

					if (accountNum == null || accountNum.equals("")) {// 没有认证
						classmateResult = new ContactsEntity();
						classmateResult.setAuthenticated("0");// web上获取的，默认没有认证，让roster来更新
						classmateResult.setAccountNum("");
					} else {
						// 过滤掉登陆用户
						if (accountNum.equals(userAccount)) {
							continue;
						}

						// 若为认证用户则从web服务器获取用户详细信息
						try {
							// web上获取的好友信息没有带分组,需另外设置
							classmateResult = LoginUtils.getFriendInfo(
									userAccount, password, accountNum);
							if (classmateResult != null) {
								classmateResult.setHasAllClassmates(1);
							} else {
								classmateResult = new ContactsEntity();
								classmateResult.setAccountNum(accountNum);
								classmateResult.setHasAllClassmates(0);// 获取详细信息失败
							}
						} catch (Exception e) {
							e.printStackTrace();
							classmateResult = new ContactsEntity();
							classmateResult.setAccountNum(accountNum);
							classmateResult.setHasAllClassmates(0);// 获取详细信息失败
						}

						classmateResult.setAuthenticated("1");// 已经认证过
						classmateResult.setAccountNum(accountNum);
					}

					userId = cmJO.getString("userId");
					userName = cmJO.getString("userName");

					classmateResult.setBaseInfoId(userId);
					classmateResult.setName(userName);
					classmateResult.setUserAccount(userAccount);
					classmateResult.setClassName(className);// 设置默认分组
					classmateResults.add(classmateResult);
				}
				return classmateResults;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			CYLog.e(TAG, e.toString());
			Toast.makeText(context, "网络异常，获取同学信息失败，请稍后重试",
					Toast.LENGTH_SHORT).show();
			return null;
		}

	}

	// +++++++++++++++++++++++++ 群组 ++++++++++++++++++++++++++++++++++++
	/**
	 * jsonObject转换为GroupChatRoomEntity
	 * 使用Ion库版本
	 */
	public static GroupChatRoomEntity jsonObject2GroupChatRoomEntity(
			JsonObject jsonRoom, String accountNum, String password) {
		try {
			String name = null;
			if (jsonRoom.has("groupName")) {
				name = jsonRoom.get("groupName").getAsString();
			}

			String groupId = null;
			if (jsonRoom.has("groupId")) {
				groupId = jsonRoom.get("groupId").getAsString();
			}

			String description = null;
			if (jsonRoom.has("description")) {
				description = jsonRoom.get("description").getAsString();
			}

			String subject = null;
			if (jsonRoom.has("subject")) {
				subject = jsonRoom.get("subject").getAsString();
			}

			String createrAccount = null;
			if (jsonRoom.has("createrAccount")) {
				createrAccount = jsonRoom.get("createrAccount").getAsString();
			}

			String membersAccount = null;
			if (jsonRoom.has("membersAccount")) {
				membersAccount = jsonRoom.get("membersAccount").getAsString();
			}

			String adminsAccount = null;
			if (jsonRoom.has("adminsAccount")) {
				adminsAccount = jsonRoom.get("adminsAccount").getAsString();
			}

			Map<String, Integer> memberMap = new HashMap<String, Integer>();
			if (adminsAccount != null && !adminsAccount.equals("")) {
				String[] ownerAccountArray = adminsAccount.split(",");
				for (int i = 0; i < ownerAccountArray.length; ++i) {
					String t = ownerAccountArray[i];
					if (!t.equals("") && !memberMap.containsKey(t)) {
						memberMap.put(t, 1);// 管理员
					}
				}
			}

			if (membersAccount != null && !membersAccount.equals("")) {
				String[] memberAccountsTmp = membersAccount.split(",");
				for (int i = 0; i < memberAccountsTmp.length; ++i) {
					String t = memberAccountsTmp[i];
					if (!t.equals("") && !memberMap.containsKey(t)) {
						memberMap.put(t, 0);// 普通成员
					}
				}
			}

			GroupChatRoomEntity groupChatRoom = new GroupChatRoomEntity();
			groupChatRoom.setGroupName(name);
			groupChatRoom.setGroupId(groupId);
			groupChatRoom.setSubject(subject);
			groupChatRoom.setDescription(description);

			groupChatRoom.setUserAccount(accountNum);
			groupChatRoom.setPassword(password);
			groupChatRoom.setCreaterAccount(createrAccount);
			groupChatRoom.setOccupantsMap(memberMap);

			return groupChatRoom;
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context, "网络异常，获取信息失败，请稍后重试",
					Toast.LENGTH_SHORT).show();
			return null;
		}
	}
	
	/**
	 * jsonObject转换为GroupChatRoomEntity
	 * 对应httpClient库版本
	 */
//	public static GroupChatRoomEntity jsonObject2GroupChatRoomEntity(
//			JSONObject jsonRoom, String accountNum, String password) {
//		try {
//			String name = null;
//			if (jsonRoom.has("groupName")) {
//				name = jsonRoom.getString("groupName");
//			}
//
//			String groupId = null;
//			if (jsonRoom.has("groupId")) {
//				groupId = jsonRoom.getString("groupId");
//			}
//
//			String description = null;
//			if (jsonRoom.has("description")) {
//				description = jsonRoom.getString("description");
//			}
//
//			String subject = null;
//			if (jsonRoom.has("subject")) {
//				subject = jsonRoom.getString("subject");
//			}
//
//			String createrAccount = null;
//			if (jsonRoom.has("createrAccount")) {
//				createrAccount = jsonRoom.getString("createrAccount");
//			}
//
//			String membersAccount = null;
//			if (jsonRoom.has("membersAccount")) {
//				membersAccount = jsonRoom.getString("membersAccount");
//			}
//
//			String adminsAccount = null;
//			if (jsonRoom.has("adminsAccount")) {
//				adminsAccount = jsonRoom.getString("adminsAccount");
//			}
//
//			Map<String, Integer> memberMap = new HashMap<String, Integer>();
//			if (adminsAccount != null && !adminsAccount.equals("")) {
//				String[] ownerAccountArray = adminsAccount.split(",");
//				for (int i = 0; i < ownerAccountArray.length; ++i) {
//					String t = ownerAccountArray[i];
//					if (!t.equals("") && !memberMap.containsKey(t)) {
//						memberMap.put(t, 1);// 管理员
//					}
//				}
//			}
//
//			if (membersAccount != null && !membersAccount.equals("")) {
//				String[] memberAccountsTmp = membersAccount.split(",");
//				for (int i = 0; i < memberAccountsTmp.length; ++i) {
//					String t = memberAccountsTmp[i];
//					if (!t.equals("") && !memberMap.containsKey(t)) {
//						memberMap.put(t, 0);// 普通成员
//					}
//				}
//			}
//
//			GroupChatRoomEntity groupChatRoom = new GroupChatRoomEntity();
//			groupChatRoom.setGroupName(name);
//			groupChatRoom.setGroupId(groupId);
//			groupChatRoom.setSubject(subject);
//			groupChatRoom.setDescription(description);
//
//			groupChatRoom.setUserAccount(accountNum);
//			groupChatRoom.setPassword(password);
//			groupChatRoom.setCreaterAccount(createrAccount);
//			groupChatRoom.setOccupantsMap(memberMap);
//
//			return groupChatRoom;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

	/**
	 * 群组信息同步到web服务器,群组信息全删全建，member为空时将删除服务器上面的记录，群组id为空时将增加一条记录
	 * 同时修改web服务器的个人信息表
	 */
//	public static boolean syncGroupChatRoomInfoToWeb(
//			GroupChatRoomEntity groupChatRoomEntity, String accountNum,
//			String password) {
//		try {
//			JSONObject jsonStr = new JSONObject();
//			JSONObject jsonContentStr = new JSONObject();
//			jsonContentStr.put("accountNum", accountNum);
//			jsonContentStr.put("password", password);
//
//			// 如果为空则新建一条记录
//			String groupId = groupChatRoomEntity.getGroupId();
//			if (groupId != null && !groupId.equals("")) {
//				jsonContentStr.put("groupId", groupId);
//			}
//			
//			String createrAccount = groupChatRoomEntity.getCreaterAccount();
//			if (createrAccount != null && !createrAccount.equals("")){
//				jsonContentStr.put("createrAccount", createrAccount);
//			}
//			
//			jsonContentStr.put("type", groupChatRoomEntity.getSyncType());
//
//			jsonContentStr.put("groupName", groupChatRoomEntity.getGroupName());
//			jsonContentStr.put("description",
//					groupChatRoomEntity.getDescription());
//			jsonContentStr.put("subject", groupChatRoomEntity.getSubject());
//
//			String adminsAccount = groupChatRoomEntity.getAdministratersStr();
//			if (adminsAccount != null) {
//				jsonContentStr.put("adminsAccount", adminsAccount);
//			}
//
//			String membersAccount = groupChatRoomEntity.getNormalMembersStr();
//			if (membersAccount != null) {
//				jsonContentStr.put("membersAccount", membersAccount);
//			}
//			jsonStr.put("command", APPConstant.USER_PROFILE_UPDATE_GROUP_INFO);
//			jsonStr.put("content", jsonContentStr);
//
//			HttpClient client = new DefaultHttpClient();
//			CYLog.i(TAG, "ready to send GroupChatRoom to web! "
//					+ groupChatRoomEntity.getGroupName());
//
//			String path = APPConstant.getURL()
//					+ "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";
//
//			String paramJson = URLEncoder.encode(jsonStr.toString());
//
//			HttpGet httpGet = new HttpGet(path + "?jsonStr=" + paramJson);
//			HttpResponse response = client.execute(httpGet);
//
//			InputStream in = response.getEntity().getContent();
//			byte[] resultBytes = StreamUtils.getBytes(in);
//			String resultJson = new String(resultBytes);
//
//			CYLog.i(TAG, resultJson);
//
//			// 解析结果字符串
//			JSONObject jsonObject = new JSONObject(resultJson);
//			boolean success = jsonObject.getBoolean("success");
//			if (success) {
//				CYLog.i(TAG, "更新聊天室到web服务器上成功!");
//				return true;
//			} else {
//				CYLog.i(TAG, "更新聊天室到web服务器上失败!");
//				return false;
//			}
//		} catch (Exception e) {
//			CYLog.i(TAG, "更新聊天室到web服务器上失败!");
//			e.printStackTrace();
//			return false;
//		}
//	}

	/**
	 * 将群组信息同步到web服务器的个人信息表
	 * 
	 * @param groupIds
	 *            群id列表代替之前的群名列表
	 * @param accountNum
	 * @param password
	 * @return
	 */
//	public static boolean syncGroupInfoToUserProfile(String groupIds,
//			String accountNum, String password) {
//		try {
//			JSONObject jsonStr = new JSONObject();
//			JSONObject jsonContentStr = new JSONObject();
//			jsonContentStr.put("accountNum", accountNum);
//			jsonContentStr.put("password", password);
//			if (groupIds != null) {
//				jsonContentStr.put("groupName", groupIds);
//			}
//			jsonStr.put("command", APPConstant.USER_PROFILE_UPDATE_USER_PROFILE);
//			jsonStr.put("content", jsonContentStr);
//
//			HttpClient client = new DefaultHttpClient();
//			CYLog.i(TAG, "ready to send GroupChatRoomName to web! " + groupIds);
//
//			String path = APPConstant.getURL()
//					+ "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";
//
//			String paramJson = URLEncoder.encode(jsonStr.toString());
//
//			HttpGet httpGet = new HttpGet(path + "?jsonStr=" + paramJson);
//			HttpResponse response = client.execute(httpGet);
//
//			InputStream in = response.getEntity().getContent();
//			byte[] resultBytes = StreamUtils.getBytes(in);
//			String resultJson = new String(resultBytes);
//
//			CYLog.i(TAG, resultJson);
//
//			// 解析结果字符串
//			JSONObject jsonObject = new JSONObject(resultJson);
//			boolean success = jsonObject.getBoolean("success");
//			if (success) {
//				CYLog.i(TAG, "更新聊天室名字到web服务器上个人信息表成功!");
//				return true;
//			} else {
//				CYLog.i(TAG, "更新聊天室名字到web服务器上个人信息表失败!");
//				return false;
//			}
//		} catch (Exception e) {
//			CYLog.i(TAG, "更新聊天室名字到web服务器上个人信息表失败!");
//			e.printStackTrace();
//			return false;
//		}
//	}
	
	/**
	 * 从服务器获取机构全名
	 * @param baseInfoId
	 * @return String
	 */
	public static String getDepartFullNameFromWeb(String baseInfoId) {
		try {
			JSONObject jsonStr = new JSONObject();
			JSONObject jsonContentStr = new JSONObject();
			jsonContentStr.put("baseInfoId", baseInfoId);
			jsonStr.put("command", APPConstant.USER_PROFILE_GET_DEPART_FULL_NAME);
			jsonStr.put("content", jsonContentStr);

			HttpClient client = new DefaultHttpClient();

			String path = APPConstant.getURL()
					+ "/userProfile/userProfileAction!doNotNeedSessionAndSecurity_userProfileHandler.action";

			String paramJson = URLEncoder.encode(jsonStr.toString());

			HttpGet httpGet = new HttpGet(path + "?jsonStr=" + paramJson);
			HttpResponse response = client.execute(httpGet);

			InputStream in = response.getEntity().getContent();
			byte[] resultBytes = StreamUtils.getBytes(in);
			String resultJson = new String(resultBytes);

			CYLog.i(TAG, resultJson);

			// 解析结果字符串
			JSONObject jsonObject = new JSONObject(resultJson);
//			boolean success = jsonObject.getBoolean("success");
			if (jsonObject != null && jsonObject.has("obj")) {
				CYLog.i(TAG, "获取机构信息成功!");
				return jsonObject.getString("obj");
			} else {
				CYLog.i(TAG, "获取机构信息失败!");
				return null;
			}
		} catch (Exception e) {
			CYLog.i(TAG, "获取机构信息失败!");
			e.printStackTrace();
			Toast.makeText(context, "网络异常，获取机构信息失败，请稍后重试",
					Toast.LENGTH_SHORT).show();
			return null;
		}
	}
}
