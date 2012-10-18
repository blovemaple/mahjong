package blove.mj;

import java.util.List;
import java.util.Map;

import blove.mj.board.GameBoard;
import blove.mj.board.PlayerTiles;
import blove.mj.event.GameEventListener;

/**
 * 游戏桌的玩家视图。游戏的所有事件通过监听器通知。<br/>
 * 此视图对应的玩家离开此游戏桌后，所有已注册的监听器将自动移除，且大部分方法都将抛出{@link PlayerLeavedException}。
 * 
 * @author blovemaple
 */
public interface GameBoardView {

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
	 * @throws PlayerLeavedException
	 */
	void addGameEventListener(GameEventListener listener);

	/**
	 * 移除一个游戏事件监听器。
	 * 
	 * @param listener
	 *            监听器
	 * @throws PlayerLeavedException
	 */
	void removeGameEventListener(GameEventListener listener);

	/**
	 * 返回当前桌上的所有玩家。
	 * 
	 * @return 所有位置到玩家的映射
	 * @throws PlayerLeavedException
	 */
	Map<PlayerLocation, Player> getPlayers();

	/**
	 * 返回当前玩家手中的牌。
	 * 
	 * @return 牌
	 * @throws PlayerLeavedException
	 */
	PlayerTiles getMyTiles();

	/**
	 * 准备好开始一局游戏。
	 * 
	 * @throws PlayerLeavedException
	 * @throws IllegalStateException
	 *             此时已在游戏中，或者玩家已经准备好
	 */
	void readyForGame();

	/**
	 * 打牌。
	 * 
	 * @param tile
	 *            牌
	 * @param readyHand
	 *            是否叫听
	 * @throws PlayerLeavedException
	 * @throws IllegalStateException
	 *             此时不该打牌
	 * @throws IllegalArgumentException
	 *             此牌不在此玩家手中，或指定听牌但出此牌不能叫听，或由于其他原因此牌不能打出
	 */
	void discard(Tile tile, boolean readyHand);

	/**
	 * 吃/碰/杠。
	 * 
	 * @param cpk
	 *            动作
	 * @throws PlayerLeavedException
	 * @throws IllegalStateException
	 *             此时不该吃/碰/杠
	 * @throws IllegalArgumentException
	 *             此时不能进行指定的吃/碰/杠动作
	 */
	void cpk(Cpk cpk);

	/**
	 * 和牌。
	 * 
	 * @throws PlayerLeavedException
	 * @throws IllegalStateException
	 *             此时不能和牌
	 */
	void win();

	/**
	 * 放弃当前的吃/碰/杠/非自摸和机会。
	 * 
	 * @throws PlayerLeavedException
	 * @throws IllegalStateException
	 *             此时没有吃/碰/杠/非自摸和机会
	 */
	void giveUpCpkw();

	/**
	 * 返回此玩家在此桌上的所有游戏结果。
	 * 
	 * @return 结果列表，按顺序排列
	 */
	List<GameResult> getResults();

	/**
	 * 离开此游戏桌。此方法将自动移除所有监听器。
	 * 
	 * @throws PlayerLeavedException
	 */
	void leave();
}
