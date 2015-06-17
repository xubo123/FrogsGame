package com.hust.schoolmatechat.datadigger;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.hust.schoolmatechat.utils.StreamUtils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;

public class Contacts {
    Context mContext;
    public HashMap<String, String> Name_Tel=new HashMap<String, String>();
    private static final String[] PHONES_PROJECTION=new String[]{
    	Phone.DISPLAY_NAME,Phone.NUMBER,Photo.PHOTO_ID,Phone.CONTACT_ID};
    //��ϵ����ʾ����
    private static final int PHONES_DISPLAY_NAME_INDEX=0;
    //�绰����
    private static final int PHONES_NUMBER_INDEX=1;
    //ͷ��ID
    private static final int PHONES_PHOTO_ID_INDEX=2;
    //��ϵ�˵�ID
    private static final int PHONES_CONTACT_ID_INDEX=3;
    //��ϵ������
    private ArrayList<String> mContactsName=new ArrayList<String>();
    //��ϵ�˵绰
    private ArrayList<String> mContactsNumber=new ArrayList<String>();
    //��ϵ��ͷ��
    private ArrayList<Bitmap> mContactsPhoto=new ArrayList<Bitmap>();
    
	public Contacts(Context context) {
		// TODO Auto-generated constructor stub
		mContext=context;
	}
	//��ȡ�ֻ�ͨѶ¼�е�����
    public String[] getPhoneContacts(){
    	String[] str1 = null;
    	String[] str2 = null;
    	ContentResolver resolver=mContext.getContentResolver();
    	// ��ȡSims����ϵ��  
    	Uri uri2 = Uri.parse("content://icc/adn"); 
//    	Cursor phoneCursor=resolver.query(uri2, PHONES_PROJECTION, null, null, null);
    	Cursor contentCursor=resolver.query(Phone.CONTENT_URI, PHONES_PROJECTION, null, null, null);
    	
//    	if(phoneCursor!=null){
//    		while (phoneCursor.moveToNext()){
//    			//�õ��ֻ�����
//    			String phoneNumber=phoneCursor.getString(PHONES_NUMBER_INDEX);
//    		    //���ֻ�����Ϊ�յĻ���Ϊ���ֶ� ������ǰѭ��
//    			if(TextUtils.isEmpty(phoneNumber))
//    				continue;
//    			//�õ���ϵ������
//    			String contactName=phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
//    			//�õ���ϵ��ID
//    			Long contactid=phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);
//    			//�õ���ϵ��ͷ��ID
//    			Long photoid=phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);
//    			//�õ���ϵ��ͷ��
//    			Bitmap contactPhoto=null;
//    			if(photoid>0){
//    				Uri uri=ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactid);
//    				InputStream input=ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
//    				contactPhoto=BitmapFactory.decodeStream(input);
//    			}
//    			else {
////    				contactPhoto=BitmapFactory.decodeResource(getResources(), R.drawable.abc_ab_bottom_solid_dark_holo);//����Ĭ��ͷ��
//    				
//    			}
//    			mContactsName.add(contactName);
//    			CYLog.i(TAG,contactName);
//    			mContactsNumber.add(phoneNumber);
//    			mContactsPhoto.add(contactPhoto);
//    		}
//    		phoneCursor.close();}
    	   	if(contentCursor!=null){
        		while (contentCursor.moveToNext()){
        			//�õ��ֻ�����
        			String phoneNumber=contentCursor.getString(PHONES_NUMBER_INDEX);
        		    //���ֻ�����Ϊ�յĻ���Ϊ���ֶ� ������ǰѭ��
        			if(TextUtils.isEmpty(phoneNumber))
        				continue;
        			//�õ���ϵ������
        			String contactName=contentCursor.getString(PHONES_DISPLAY_NAME_INDEX);
        			//�õ���ϵ��ID
        			Long contactid=contentCursor.getLong(PHONES_CONTACT_ID_INDEX);
        			//�õ���ϵ��ͷ��ID
        			Long photoid=contentCursor.getLong(PHONES_PHOTO_ID_INDEX);
        			//�õ���ϵ��ͷ��
        			Bitmap contactPhoto=null;
        			if(photoid>0){
        				Uri uri=ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactid);
        				InputStream input=ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
        				contactPhoto=BitmapFactory.decodeStream(input);
        			}
        			else {
//        				contactPhoto=BitmapFactory.decodeResource(getResources(), R.drawable.abc_ab_bottom_solid_dark_holo);//����Ĭ��ͷ��
        				
        			}
        			mContactsName.add(StreamUtils.phoneOrNameManage(contactName));
        			//CYLog.i(TAG,contactName);
        			mContactsNumber.add(StreamUtils.phoneOrNameManage(phoneNumber));
        			mContactsPhoto.add(contactPhoto);
        		}
        		contentCursor.close();}
    		str1 = (String[])mContactsName.toArray(new String[mContactsName.size()]);//��ArrayListת��ΪString����
    		str2 = (String[])mContactsNumber.toArray(new String[mContactsNumber.size()]);
    		for(int i=0;i<str1.length;i++){
    			Name_Tel.put(str1[i], str2[i]);
    		    //CYLog.i(TAG,str2[i]);
    	}
		return str1;
    }
public HashMap<String, String> getHistory(String[] Friend){
    ContentResolver cr = mContext.getContentResolver();
    HashMap<String, String> aName_Time=null;
	HashMap<String, String> Name_Time=null;
    final Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.NUMBER,CallLog.Calls.CACHED_NAME,CallLog.Calls.TYPE, CallLog.Calls.DATE}, null, null,CallLog.Calls.DEFAULT_SORT_ORDER);
	if(cursor.getCount()!=0){
	aName_Time=new HashMap<String, String>();
	String[] str1 = new String[cursor.getCount()];//�绰����
	String[] str2 = new String[cursor.getCount()];//����
    Date date;
    String[] time= new String[cursor.getCount()];
    for (int i = 0; i < cursor.getCount(); i++) {  
    	int n=cursor.getCount();
        cursor.moveToPosition(i);
        str1[i] = cursor.getString(0); //�绰����
        str2[i] = cursor.getString(1);//ͨ����¼�е�����
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        date = new Date(Long.parseLong(cursor.getString(3)));
        time[i] = sfd.format(date);//ͨ��ʱ��
        aName_Time.put(str2[i], time[i]);
       }
   
   HashFind aHashFind=new HashFind();
   String[] History=null;
   History=aHashFind.HashSearch(Friend,str2);
   //CYLog.i(TAG,aHashFind.HashSearch(Friend,str2));
   if(History!=null){
   Name_Time=new HashMap<String, String>();
   for(int i=0;i<History.length;i++){
   Name_Time.put(History[i], aName_Time.get(History[i]));
   }
   return Name_Time;}
   else return null; }
	return null;
   
}
}
