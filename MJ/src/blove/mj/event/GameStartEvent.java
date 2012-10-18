package blove.mj.event;

import blove.mj.board.GameBoard;

/**
 * 游戏开始的事件。
 * 
 * @author blovemaple
 */
public class GameStartEvent extends GameEvent {

	private static final long serialVersionUID = 1L;

	public GameStartEvent(GameBoard source) {
		super(source);
	}

}
