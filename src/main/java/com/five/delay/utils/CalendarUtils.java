package com.five.delay.utils;

import java.util.Calendar;

public class CalendarUtils {
	
	/**
	 * 获得当前时间second秒后的时间戳 
	 **/
	public static long getCurrentTimeInMillis(int second){
		Calendar cal = Calendar.getInstance();
		if(second>0){
			cal.add(Calendar.SECOND, second);
		}
        return cal.getTimeInMillis();
	}
	
	public static String getCurrentTimeByStr(int second){
		Calendar cal = Calendar.getInstance();
		if(second>0){
			cal.add(Calendar.SECOND, second);
		}
		String time = cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND);
		return time;
	}
}
