package com.wind.Paint_MyLuence.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期Util
 * 
 * @author zhouyanjun
 * @version 1.0 2014-8-26
 */
public class DateUtil {
	public final static SimpleDateFormat TimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 将格式为yyyy-mm-dd HH:mm:ss字符串时间转换成时间对象
	 * 
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public synchronized static Date parseTime(String date) throws ParseException {
		if (date == null || date.isEmpty()) return null;
		Date d = null;
		return TimeFormat.parse(date);
	}

	/**
	 * 将时间对象转换为yyyy-mm-dd HH:mm:ss格式用的字符串时间
	 * 
	 * @param date
	 * @return
	 */
	public synchronized static String timeFormat(Date date) {
		if (date == null) return null;
		return TimeFormat.format(date);
	}
}
