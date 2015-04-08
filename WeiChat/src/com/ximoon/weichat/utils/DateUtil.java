package com.ximoon.weichat.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

	private static SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

	public static SimpleDateFormat getSimpleDateFormat() {
		return sdf;
	}

	public static String getCurrentTime() {
		return sdf.format(new Date());
	}

}
