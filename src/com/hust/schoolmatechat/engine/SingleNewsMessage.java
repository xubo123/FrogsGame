package com.hust.schoolmatechat.engine;

import java.io.Serializable;
import java.util.Date;

public class SingleNewsMessage{

	private static final long serialVersionUID = 1L;
	private int nid;// ����id
	private String icon;
	private boolean isBreaking;// �Ƿ����ͷ��
	private String title;// ���ű���
	private String summary;// ����ժҪ
	private Date time;
	private String newsUrl;// ������ַ
	private String channelId;// ����Ƶ��id
	private String PMId;// ����pushedmessage��id
	private String content;

	public SingleNewsMessage(int nid, String icon, boolean isBreaking, String title, String summary, Date time, String newsUrl, String channelId, String pMId,
			String content)
	{
		super();
		this.nid = nid;
		this.icon = icon;
		this.isBreaking = isBreaking;
		this.title = title;
		this.summary = summary;
		this.time = time;
		this.newsUrl = newsUrl;
		this.channelId = channelId;
		PMId = pMId;
		this.content = content;
	}

	public SingleNewsMessage()
	{
		super();
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public String getChannelId()
	{
		return channelId;
	}

	public void setChannelId(String channelId)
	{
		this.channelId = channelId;
	}

	public String getPMId()
	{
		return PMId;
	}

	public void setPMId(String pMId)
	{
		PMId = pMId;
	}

	public String getNewsUrl()
	{
		return newsUrl;
	}

	public void setNewsUrl(String newsUrl)
	{
		this.newsUrl = newsUrl;
	}

	public int getNid()
	{
		return nid;
	}

	public void setNid(int nid)
	{
		this.nid = nid;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public boolean isBreaking()
	{
		return isBreaking;
	}

	public void setBreaking(boolean isBreaking)
	{
		this.isBreaking = isBreaking;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getSummary()
	{
		return summary;
	}

	public void setSummary(String summary)
	{
		this.summary = summary;
	}

	public Date getTime()
	{
		return time;
	}

	public void setTime(Date time)
	{
		this.time = time;
	}

	@Override
	public String toString()
	{
		return "SingleNewsMessage [nid=" + nid + ", icon=" + icon + ", isBreaking=" + isBreaking + ", title=" + title + ", summary=" + summary + ", time="
				+ time + ", newsUrl=" + newsUrl + ", channelId=" + channelId + ", PMId=" + PMId + ", content=" + content + "]";
	}

}
