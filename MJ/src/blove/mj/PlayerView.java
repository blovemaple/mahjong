package blove.mj;

import java.util.List;

import blove.mj.board.GameBoard;
import blove.mj.board.PlayerTiles;
import blove.mj.event.GameEventListener;

/**
 * 游戏桌的玩家视图。游戏的所有事件通过监听器通知。<br/>
 * 此视图对应的玩家离开此游戏桌后，所有已注册的监听器将自动移除，且大部分方法都将抛出{@link PlayerLeavedException}。
 * 
 * @author blovemaple
 */
public interface PlayerView {

	/**
	 * 返回游戏桌。
	 * 
	 * @return 游戏桌
	 * @throws PlayerLeavedException
	 */
	GameBoard getBoard();

	/**
	 * 返回玩家
	 * 
	 * @return 玩家
	 */
	Player getPlayer();

	/**
	 * 返回当前玩家的位置。
	 * 
	 * @return 位置
	 */
	PlayerLocation getMyLocation();

	/**
	 * 添加一个游戏事件监听器。
	 * 
	 * @param listener
	 *            监听器
	 */
	void addGameEventListener(GameEventListener listener);

	/**
	 * 移除一个游戏事件监听器。
	 * 
	 * @param listener
	 *            监听器
	 */
	void removeGameEventListener(GameEventListener listener);

	/**
	 * 返回当前玩家手中的牌。
	 * 
	 * @return 牌
	 * @throws PlayerLeavedException
	 */
	PlayerTiles getMyTiles();

	/**
	 * 返回此玩家在此桌上的所有游戏结果。
	 * 
	 * @return 结果列表，按顺序排列
	 */
	List<GameResult> getResults();

	/**
	 * 准备好开始一局游戏。
	 * 
	 * @throws PlayerLeavedException
	 * @throws IllegalStateException
	 *             此时已在游戏中
	 */
	void readyForGame();

	/**
	 * 离开此游戏桌。
	 * 
	 * @throws PlayerLeavedException
	 */
	void leave();
}
