package blove.mj.local;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 游戏进行时的计时器。每倒数一秒触发一次动作。
 * 
 * @author blovemaple
 */
class Timer {
	private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(
			2);
	private Future<?> taskFuture;
	private TimerAction action;

	private long remainSecs;
	private boolean timeout = false;

	private class TimerTask implements Runnable {

		public TimerTask(TimerAction action) {
			Timer.this.action = action;
		}

		@Override
		public void run() {// XXX - 总是从减少一秒开始计时
			synchronized (Timer.this) {
				executor.execute(new Runnable() {

					@Override
					public void run() {
						action.countRun(remainSecs);
					}
				});
				if (remainSecs <= 0) {
					timeout = true;
					taskFuture.cancel(true);
					executor.execute(new Runnable() {

						@Override
						public void run() {
							action.timeoutRun();
						}
					});
				}

				// for next
				remainSecs--;
			}
		}

	}

	/**
	 * 开始计时。
	 * 
	 * @param time
	 *            时间。如果小于0则不计时。
	 * @param unit
	 *            时间单位
	 * @param action
	 *            动作
	 * @throws IllegalStateException
	 *             当前正在计时
	 */
	public synchronized void start(long time, TimeUnit unit, TimerAction action) {
		if (taskFuture != null && !taskFuture.isDone())
			throw new IllegalStateException("当前正在计时");
		if (time < 0)
			return;

		timeout = false;
		remainSecs = TimeUnit.SECONDS.convert(time, unit);
		taskFuture = executor.scheduleAtFixedRate(new TimerTask(action), 0, 1,
				TimeUnit.SECONDS);
	}

	/**
	 * 停止计时并执行{@link TimerAction#stopRun()}。如果当前没有计时，则不执行任何操作。
	 */
	public synchronized void stop() {
		if (taskFuture != null && !taskFuture.isDone()) {
			taskFuture.cancel(true);
			action.stopRun();
		}
	}

	/**
	 * 返回当前（如果正在计时）或上次（如果已停止计时）计时有没有超时。
	 * 
	 * @return 如果超时，返回true；否则返回false。
	 */
	public synchronized boolean hasTimeout() {
		return timeout;
	}

	/**
	 * 计时器计时动作。
	 * 
	 * @author blovemaple
	 */
	interface TimerAction {

		/**
		 * 每倒数一秒执行一次。
		 * 
		 * @param remainSecs
		 *            剩余的秒数
		 */
		void countRun(long remainSecs);

		/**
		 * 超时后执行。
		 */
		void timeoutRun();

		/**
		 * 中止计时后执行。
		 */
		void stopRun();
	}

}
