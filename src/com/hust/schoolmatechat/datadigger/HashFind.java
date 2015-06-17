package com.hust.schoolmatechat.datadigger;

import java.util.HashMap;

public class HashFind {
	HashMap<String,Integer> hash=new HashMap<String, Integer>();
	HashMap<String,Integer> a_Flag=new HashMap<String, Integer>();//判断a数组数字中是否被访问，为0没被访问为1被访问了
    public HashFind() {
		// TODO Auto-generated constructor stub
	}
	public String[] HashSearch(String[] a,String[] b){
		int a_len=a.length;
		int j=0;
		int b_len=b.length;
		String[] Friend =new String[b.length];//存放相同名称的姓名，最坏情况为b。length
		//初始化HashMap
		for(int i=0;i<a_len;i++)
		{	hash.put(a[i], 0);
		    a_Flag.put(a[i], 0);}
		for(int i=0;i<b_len;i++)
			hash.put(b[i], 0);
		for(int i=0;i<a_len;i++)
			hash.put(a[i], 1);
		for(int i=0;i<b_len;i++)
			if(hash.get(b[i])==1&&a_Flag.get(b[i])==0){
				Friend[j]=b[i];
				a_Flag.put(b[i], 1);
				j++;
			}
		String[] Friends=new String[j];
		for(int i=0;i<j;i++){
			Friends[i]=Friend[i];
		}
		//CYLog.i(TAG,Friends.length);
		return Friends;
	}


}
