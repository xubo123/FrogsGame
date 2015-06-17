package com.hust.schoolmatechat.pushedmsgservice;

import java.io.Serializable;

public class Channel {
	private String channelId;
	private String icon;
	private String channelRemark;
	private String cName;//ÆµµÀÃû³Æ
	
	public String getChannelId() {
		return channelId;
	}
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	public String getChannelRemark() {
		return channelRemark;
	}
	public void setChannelRemark(String channelRemark) {
		this.channelRemark = channelRemark;
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
	
	
	
	
}
