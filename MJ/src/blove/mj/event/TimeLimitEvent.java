package blove.mj.event;

import blove.mj.board.GameBoard;

public class TimeLimitEvent extends GameEvent {
	private static final long serialVersionUID = 1L;

	private final long timeLimit;

	/**
	 * 新建一个实例。
	 * 
	 * @param source
	 *            源
	 * @param timeLimit
	 *            当前限时。单位：秒。
	 */
	public TimeLimitEvent(GameBoard source, long timeLimit) {
		super(source);
		this.timeLimit = timeLimit;
	}

	/**
	 * 返回当前限时。单位：秒。
	 * 
	 * @return 限时
	 */
	public long getTimeLimit() {
		return timeLimit;
	}

}
