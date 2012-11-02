package blove.mj.event;

import blove.mj.board.GameBoard;

/**
 * 计时的时候时间变化的事件。
 * 
 * @author blovemaple
 */
public class TimeLimitEvent extends GameEvent {
	private static final long serialVersionUID = 1L;

	/**
	 * 表示停止计时的时间值。
	 */
	public static long STOP_TIME_LIMIT = -1;

	private final long timeLimit;

	/**
	 * 新建一个实例。
	 * 
	 * @param source
	 *            源
	 * @param timeLimit
	 *            当前限时。单位：秒。若停止计时则为-1。
	 */
	public TimeLimitEvent(GameBoard source, long timeLimit) {
		super(source);
		this.timeLimit = timeLimit;
	}

	/**
	 * 返回当前限时。单位：秒。
	 * 
	 * @return 限时。若停止计时则返回-1。
	 */
	public long getTimeLimit() {
		return timeLimit;
	}

}
