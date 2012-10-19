package blove.mj.bot;

import blove.mj.board.GameBoard;
import blove.mj.board.GameBoardFullException;

/**
 * 机器人用户。
 * 
 * @author blovemaple
 */
public interface BotPlayer {
	/**
	 * 加入一个游戏桌。
	 * 
	 * @param gameBoard
	 *            游戏桌
	 * @throws IllegalStateException
	 *             已经加入了一个游戏桌
	 * @throws GameBoardFullException
	 *             此游戏桌已满
	 */
	public void join(GameBoard gameBoard) throws GameBoardFullException;

	/**
	 * 离开所在的游戏桌。
	 * 
	 * @throws IllegalStateException
	 *             没有加入游戏桌
	 */
	public void leave();
}
