package net.imyeyu.itools;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class ITools {
	
	/**
	 * 队列对象，输入若干对象，逐一遍历，返回第一个不为空的对象
	 * 
	 * @param <T> 对象类型
	 * @param ts  对象数组
	 * @return 第一个不为空的对象
	 */
	@SafeVarargs
	public static <T>T queue(T... ts) {
		for (int i = 0; i < ts.length; i++) {
			if (ts[i] != null) {
				return ts[i];
			}
		}
		return null;
	}
	
	public static String date() {
		return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	}
	
	public static String time() {
		return new SimpleDateFormat("HH:mm:ss").format(new Date());
	}
	
	public static String datetime() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
}