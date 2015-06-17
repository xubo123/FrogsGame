package com.hust.schoolmatechat.pushedmsgservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ChannelList {
	private List<Channel> channelList;

	public ChannelList() {
		this.channelList = new ArrayList<Channel>();
	}
	
	public void add(Channel e) {
		channelList.add(e);
	}
	
	public Channel get(int i) {
		return channelList.get(i);
	}
	public ChannelList(List<Channel> list) {
		this.channelList = list;
	}
	
	public List<Channel> getList() {
		return channelList;
	}

	public void setList(List<Channel> list) {
		this.channelList = list;
	}
	
}
