package net.imyeyu.itools.service;

import java.util.Timer;
import java.util.TimerTask;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * <br>整数迭代线程
 * <br>默认值: 范围 [0, Long.MAX_VALUE], 步进 1, 间隔 1000 毫秒
 * 
 * @author 夜雨
 */
public class NumberService extends Service<Long> {
	
	private long i = 0, end = Long.MAX_VALUE;
	private int step = 1, interval = 1000;
	
	private Timer timer;
	private boolean isPause = false;
	
	public NumberService() {}
	
	public NumberService(long start, long end, int step, int interval) {
		this.i = start;
		this.end = end;
		this.step = step;
		this.interval = interval;
	}
	
	protected Task<Long> createTask() {
		return new Task<Long>() {
			protected Long call() throws Exception {
				updateMessage(ServiceEvent.START);
				timer = new Timer();
				timer.scheduleAtFixedRate(new TimerTask() {
					public void run() {
						if (!isPause) {
							updateValue(i);
							i += step;
							if (i == end) {
								updateMessage(ServiceEvent.FINISH);
								shutdown();
							}
						}
					}
				}, 0, interval);
				return null;
			}
		};
	}
	
	public void pause() {
		this.isPause = true;
	}
	
	public void proceed() {
		this.isPause = false;
	}

	public void shutdown() {
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
	}
	
	public void setStart(long start) {
		this.i = start;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
}