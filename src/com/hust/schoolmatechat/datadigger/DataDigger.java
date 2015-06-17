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
			// �༶����
			String[] NameList = dataCenterManagerService
					.getUnAuthenticatedContactsNamesArray();

			if (NameList != null) {
				for (int i = 0; i < NameList.length; i++) {
					CYLog.i("NameList", NameList[i]);
				}

				aContacts = new Contacts(mContext);
				HashFind aHashFind = new HashFind();
				String[] Contacts = aContacts.getPhoneContacts();// ͨѶ¼����
				for (int i = 0; i < Contacts.length; i++) {
					CYLog.i("Contacts", Contacts[i]);
				}

				Friend = aHashFind.HashSearch(NameList, Contacts);// ��ȡ������ͬ������ϵ��Ϣ
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
		HashMap<String, String> aHashMap = aContacts.getHistory(Friend);// �õ��������ھ��ͬѧ��Ϣ��ͨ����¼
		//CYLog.i(TAG,Friend.length);
	}
}
