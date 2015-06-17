package com.hust.schoolmatechat.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatUtils {
	
	public static String date2String(Date date){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String str=null;
		if(date!=null){
			str=sdf.format(date);
		}
		return str;
		
	}
	
	public static int date2Int(Date date){
		SimpleDateFormat sdf=new SimpleDateFormat("MMddHHmm");
		int out=0;
		if(date!=null){
			out=Integer.parseInt(sdf.format(date));
		}
		return out;
		
	}
	public static String date2ChatItemTime(Date date){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		String str=null;
		String timeOut="";
			
		if(date!=null){
			str=sdf.format(date);
		}
		String []timeInput = str.split("-");
		String []timeNow = sdf.format(new Date()).split("-");
		if(Integer.parseInt(timeNow[0])-Integer.parseInt(timeInput[0])==1){
			timeOut+="去年"+Integer.parseInt(timeInput[1])+"月"+Integer.parseInt(timeInput[2])+"日";
		}else if(Integer.parseInt(timeNow[0])-Integer.parseInt(timeInput[0])>1){
			timeOut+=Integer.parseInt(timeInput[0])+"年"+Integer.parseInt(timeInput[1])+"月"+Integer.parseInt(timeInput[2])+"日";
		}else{
			if((timeNow[1]+timeNow[2]).equals((timeInput[1]+timeInput[2]))){
				if(Integer.parseInt(timeInput[3])>=18){
					timeOut="晚上 ";
				}else if(Integer.parseInt(timeInput[3])>=12){
					timeOut="下午 ";
				}else if(Integer.parseInt(timeInput[3])>=6){
					timeOut="上午 ";
				}else {
					timeOut="凌晨 ";
				}
				timeOut+=Integer.parseInt(timeInput[3])+":"+timeInput[4];
			}else{
				timeOut=Integer.parseInt(timeInput[1])+"月"+Integer.parseInt(timeInput[2])+"日";
			}
		}
		return timeOut;
		
	}
	public static String date2MessageTime(Date date){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		String str=null;
		String timeOut="";
			
		if(date!=null){
			str=sdf.format(date);
		}
		String []timeInput = str.split("-");
		String []timeNow = sdf.format(new Date()).split("-");
		if(Integer.parseInt(timeNow[0])-Integer.parseInt(timeInput[0])==1){
			timeOut+="去年"+Integer.parseInt(timeInput[1])+"月"+Integer.parseInt(timeInput[2])+"日  "+Integer.parseInt(timeInput[3])+":"+timeInput[4];
		}else if(Integer.parseInt(timeNow[0])-Integer.parseInt(timeInput[0])>1){
			timeOut+=Integer.parseInt(timeInput[0])+"年"+Integer.parseInt(timeInput[1])+"月"+Integer.parseInt(timeInput[2])+"日  "+Integer.parseInt(timeInput[3])+":"+timeInput[4];
		}else{
			if((timeNow[1]+timeNow[2]).equals((timeInput[1]+timeInput[2]))){
				if(Integer.parseInt(timeInput[3])>=18){
					timeOut="晚上 ";
				}else if(Integer.parseInt(timeInput[3])>=12){
					timeOut="下午 ";
				}else if(Integer.parseInt(timeInput[3])>=6){
					timeOut="上午 ";
				}else {
					timeOut="凌晨 ";
				}
				timeOut+=Integer.parseInt(timeInput[3])+":"+timeInput[4];
			} else if(Integer.parseInt(timeNow[2])-Integer.parseInt(timeInput[2])==1){
				timeOut="昨天";
				if(Integer.parseInt(timeInput[3])>=18){
					timeOut+="晚上 ";
				}else if(Integer.parseInt(timeInput[3])>=12){
					timeOut+="下午 ";
				}else if(Integer.parseInt(timeInput[3])>=6){
					timeOut+="上午 ";
				}else {
					timeOut+="凌晨 ";
				}
				timeOut+=Integer.parseInt(timeInput[3])+":"+timeInput[4];
			}else{
				timeOut=Integer.parseInt(timeInput[1])+"月"+Integer.parseInt(timeInput[2])+"日  "+Integer.parseInt(timeInput[3])+":"+timeInput[4];
			}
		}
		return timeOut;
		
	}
	public static Date string2Date(String str){
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(str==null||str.equals(""))
			return null;
		try {
			
			return sdf.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
 
}
