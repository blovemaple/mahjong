package blove.mj.event;

import java.util.EventObject;

import blove.mj.board.GameBoard;

/**
 * 游戏事件。
 * 
 * @author blovemaple
 */
public abstract class GameEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	/**
	 * 新建一个实例。
	 * 
	 * @param source
	 *            事件源
	 */
	public GameEvent(GameBoard source) {
		super(source);
	}

}
