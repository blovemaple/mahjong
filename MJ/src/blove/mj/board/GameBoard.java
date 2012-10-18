package blove.mj.board;

import java.util.Map;

import blove.mj.GameBoardView;
import blove.mj.Player;
import blove.mj.PlayerLocation;
import blove.mj.rules.TimeLimitStrategy;
import blove.mj.rules.WinStrategy;

/**
 * 游戏桌。
 * 
 * @author blovemaple
 */
public interface GameBoard {

	/**
	 * 返回此桌上目前所有玩家。
	 * 
	 * @return 人数
	 */
	Map<PlayerLocation, Player> getPlayers();

	/**
	 * 返回此桌目前是否正在进行游戏。
	 * 
	 * @return 如果正在进行，返回true；否则返回false。
	 */
	boolean isInGame();

	/**
	 * 新玩家进入。
	 * 
	 * @param name
	 *            玩家名称
	 * @return 该玩家的游戏桌视图
	 * @throws GameBoardFullException
	 *             此游戏桌已满
	 */
	GameBoardView newPlayer(String name) throws GameBoardFullException;

	/**
	 * 返回限时策略。
	 * 
	 * @return 限时策略
	 */
	TimeLimitStrategy getTimeLimitStrategy();

	/**
	 * 返回和牌策略。
	 * 
	 * @return 和牌策略
	 */
	WinStrategy getWinStrategy();

}
