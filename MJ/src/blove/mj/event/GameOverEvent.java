package blove.mj.event;

import blove.mj.GameResult;
import blove.mj.board.GameBoard;

/**
 * 一局结束的事件。
 * 
 * @author 陈通
 */
public class GameOverEvent extends GameEvent {

	private static final long serialVersionUID = 1L;

	private final GameResult result;

	/**
	 * 新建一个实例。
	 * 
	 * @param source
	 *            事件源
	 * @param result
	 *            结果
	 */
	public GameOverEvent(GameBoard source, GameResult result) {
		super(source);
		this.result = result;
	}

	/**
	 * 返回结果。
	 * 
	 * @return result
	 */
	public GameResult getResult() {
		return result;
	}

}
