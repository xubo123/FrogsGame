package com.hust.schoolmatechat.pushedmsgservice;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class PushedMessage {
	
	private String PMId;//������Ϣid
	private String icon;
	private String cName;//Ƶ������
	private String channelId;//����Ƶ��Id
	private String newsSummary;//ͷ�����ŵ�ժҪ
	private String isUnRead;//ͷ�����ŵ�ժҪ
	private Date time;
	private List<SingleNewsMessage> newsList;//������ŵ�list
	public String getPMId() {
		return PMId;
	}
	public void setPMId(String pMId) {
		PMId = pMId;
	}
	public String getisUnRead() {
		return isUnRead;
	}
	public void setisUnRead(String misUnRead) {
		isUnRead = misUnRead;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getcName() {
		return cName;
	}
	public void setcName(String cName) {
		this.cName = cName;
	}
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getNewsSummary() {
		return newsSummary;
	}
	public void setNewsSummary(String newsSummary) {
		this.newsSummary = newsSummary;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public List<SingleNewsMessage> getNewsList() {
		return newsList;
	}
	public void setNewsList(List<SingleNewsMessage> newsList) {
		this.newsList = newsList;
	}
	
	

}
