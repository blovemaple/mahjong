package blove.mj.board.local;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 游戏进行时的计时器。每倒数一秒触发一次动作。
 * 
 * @author 陈通
 */
class Timer {
	private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(
			2);
	private Future<?> taskFuture;

	private long remainSecs;
	private boolean timeout = false;

	private class TimerTask implements Runnable {

		final TimerAction action;

		public TimerTask(TimerAction action) {
			this.action = action;
		}

		@Override
		public synchronized void run() {
			remainSecs--;
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
		}

	}

	/**
	 * 开始计时。
	 * 
	 * @param time
	 *            时间
	 * @param unit
	 *            时间单位
	 * @param action
	 *            动作
	 * @throws IllegalArgumentException
	 *             time<=0
	 * @throws IllegalStateException
	 *             当前正在计时
	 */
	public void start(long time, TimeUnit unit, TimerAction action) {
		if (time <= 0)
			throw new IllegalArgumentException("时间必须是正数");
		if (taskFuture != null && !taskFuture.isDone())
			throw new IllegalStateException("当前正在计时");

		timeout = false;
		taskFuture = executor.scheduleAtFixedRate(new TimerTask(action), 1, 1,
				TimeUnit.SECONDS);
	}

	/**
	 * 停止计时。
	 * 
	 * @throws IllegalStateException
	 *             当前没有计时
	 */
	public void stop() {
		taskFuture.cancel(true);
	}

	/**
	 * 返回当前（如果正在计时）或上次（如果已停止计时）计时有没有超时。
	 * 
	 * @return 如果超时，返回true；否则返回false。
	 */
	public boolean hasTimeout() {
		return timeout;
	}

	/**
	 * 计时器计时动作。
	 * 
	 * @author 陈通
	 */
	abstract static class TimerAction {

		/**
		 * 每倒数一秒执行一次。
		 * 
		 * @param remainSecs
		 *            剩余的秒数
		 */
		public abstract void countRun(long remainSecs);

		/**
		 * 超时后执行。
		 */
		public abstract void timeoutRun();
	}

}
