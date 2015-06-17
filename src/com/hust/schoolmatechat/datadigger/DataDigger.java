package com.hust.schoolmatechat.datadigger;

import java.util.HashMap;

import android.content.Context;

import com.hust.schoolmatechat.DataCenterManagerService.DataCenterManagerService;
import com.hust.schoolmatechat.engine.CYLog;

public class DataDigger {
	Context mContext;
	public Contacts aContacts;
	String[] Friend = null;
	private DataCenterManagerService dataCenterManagerService;

	public DataDigger(Context context,
			DataCenterManagerService dataCenterManagerService) {
		mContext = context;
		this.dataCenterManagerService = dataCenterManagerService;
	}

	public String[] DigDataFromContacts() {
		try {
			// 班级名单
			String[] NameList = dataCenterManagerService
					.getUnAuthenticatedContactsNamesArray();

			if (NameList != null) {
				for (int i = 0; i < NameList.length; i++) {
					CYLog.i("NameList", NameList[i]);
				}

				aContacts = new Contacts(mContext);
				HashFind aHashFind = new HashFind();
				String[] Contacts = aContacts.getPhoneContacts();// 通讯录名单
				for (int i = 0; i < Contacts.length; i++) {
					CYLog.i("Contacts", Contacts[i]);
				}

				Friend = aHashFind.HashSearch(NameList, Contacts);// 获取与名单同名的联系信息
			}
			return Friend;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void DigDataFromHistory() {
		DataDigger aDataDigger = new DataDigger(mContext,
				dataCenterManagerService);
		String[] Friend = new String[10];
		//CYLog.i(TAG,Friend.length);
		if (aDataDigger.DigDataFromContacts() != null) {
			Friend = aDataDigger.DigDataFromContacts();
		}
		Contacts aContacts = new Contacts(mContext);
		HashMap<String, String> aHashMap = aContacts.getHistory(Friend);// 得到与数据挖掘的同学信息的通话记录
		//CYLog.i(TAG,Friend.length);
	}
}
