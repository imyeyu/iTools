package net.imyeyu.itools.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * <br>精确的多任务服务，指定某一时刻执行，并每隔某一时段再执行
 * <br>通常用于: 每日任务，每时任务等
 * <br>执行间隔需要大于等于 10 分钟，否则更推荐其他计时器，默认间隔 1 天
 * 
 * @author 夜雨
 */
public class ExactService extends Service<Long> {

	public static final long MINUTE = 60000;
	public static final long HORSE = MINUTE * 60;
	public static final long DAY = HORSE * 24;
	public static final long WEEK = DAY * 7;
	
	private int h, m, s;
	private long i = 0, interval = DAY;
	private Timer timer;

	public ExactService(int h, int m, int s) {
		this.h = h;
		this.m = m;
		this.s = s;
	}

	protected Task<Long> createTask() {
		return new Task<Long>() {
			protected Long call() throws Exception {
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, h);
				calendar.set(Calendar.MINUTE, m);
				calendar.set(Calendar.SECOND, s);
				// 第一次执行的时间
				Date date = calendar.getTime();
				// 如果小于当前的时间
				if (date.before(new Date())) {
					Calendar startDT = Calendar.getInstance();
					startDT.setTime(date);
					startDT.add(Calendar.DATE, 1);
					date = startDT.getTime();
				}
				timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					public void run() {
						updateValue(++i);
					}
				}, date, interval);
				return null;
			}
		};
	}
	
 	public void shutdown() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
	}
 	
 	public void setInterval(long interval) {
 		if (interval < 10 * MINUTE) {
			throw new IllegalArgumentException("最小执行间隔不应小于 10 分钟，如果需要频率更高的计时器，请使用其他 Service");
		} else {
			this.interval = interval;
		}
 	}
}