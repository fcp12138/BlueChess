package com.fcp.bluechess.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期转换工具
 * @author fcp
 *
 */
public class DateUtils {
	
	/**
	 * 将日期转化成“yyyy-MM-dd”格式
	 * @param date 日期（如：new date）
	 * @return string类型数据(yyyy-MM-dd)
	 */
	public static String DateToYymd(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String formatDate = sdf.format(date);
		return formatDate;
	}

	/**
	 * 将日期转化成“MM-dd”格式
	 * @param date 日期（如：new date）
	 * @return string类型数据(MM-dd)
	 */
	public static String DateToMd(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
		String formatDate = sdf.format(date);
		return formatDate;
	}
	
	/**
	 * 将日期转化成“yyyy年MM月dd日 HH时mm分ss秒”格式
	 * @param date 日期（如：new date）
	 * @return string类型数据(yyyy年MM月dd日 HH时mm分ss秒)
	 */
	public static String DateToyMdHms(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
		String formatDate = sdf.format(date);
		return formatDate;
	}
	
	/**
	 * 获取当前时间的时间戳，返回的是毫秒数。
	 * 推荐此种方法，执行速度快。
	 * @return 毫秒数
	 */
	public static long getCurrentTimeMillis(){
		return System.currentTimeMillis();
	}
	
	/**
	 * 将日期转换成时间戳格式
	 * @param date 日期（如：new date）
	 * @return 毫秒数
	 */
	public static long getDateTimeMillis(Date date){
		return date.getTime();
	}
	
	/**
	 * 将long类型的时间格式转化成数据库的时间戳格式
	 * @param timeLong 毫秒级的时间数据
	 * @return 数据库的时间戳格式(yyyy-MM-dd HH:mm:ss.SSS)
	 */
	public static Timestamp longToTimestamp(long timeLong){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date(timeLong);
		String dataStr = formatter.format(date);
		Timestamp timestamp = Timestamp.valueOf(dataStr);
		
		return timestamp;
	}
	
	/**
	 * 将long类型的时间格式转化成时间格式
	 * @param timeLong 毫秒级的时间数据
	 * @return 格式(yyyy-MM-dd HH:mm:ss)
	 */
	public static String longToTimeFormat(long timeLong){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(timeLong > 0){
			Date date = new Date(timeLong);
			String dataStr = formatter.format(date);
			
			return dataStr;
		}else{
			return "";
		}
	}

	/**
	 * 将long类型的时间格式转化成时间格式
	 * @param timeLong 毫秒级的时间数据
	 * @return 格式(yyyy-MM-dd HH:mm)
	 */
	public static String longToTimeHMFormat(long timeLong){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if(timeLong > 0){
			Date date = new Date(timeLong);
			String dataStr = formatter.format(date);
			return dataStr;
		}else{
			return "";
		}
	}

	/**
	 * 将long类型的时间格式转化成时间格式
	 * @param timeLong 毫秒级的时间数据
	 * @return 格式(MM-dd)
	 */
	public static String longToMdFormat(long timeLong){
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");
		if(timeLong > 0){
			Date date = new Date(timeLong);
			String dataStr = formatter.format(date);
			return dataStr;
		}else{
			return "";
		}
	}

	/**
	 *将YYYY-MM-DD HH:mm转化为long
	 * @return
	 */
	public static long stringYMDHMToLong(String s){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
			Date date=formatter.parse(s);
			return date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获得音频文件名字
	 * @return
	 */
	public static String getCurrentDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		return formatter.format(curDate);
	}
	
}
