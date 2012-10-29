package blove.mj.rules;

import java.util.Map;
import java.util.Set;

import blove.mj.PointItem;
import blove.mj.TileType;
import blove.mj.board.PlayerTiles;

/**
 * 和牌策略。
 * 
 * @author blovemaple
 */
public interface WinStrategy {
	/**
	 * 计算并返回所有听牌机会。即从指定牌集合中打出哪个牌后可以听牌。
	 * 
	 * @param playerTiles
	 *            玩家的牌
	 * 
	 * @return 打出哪些牌型可以听牌
	 * @throws IllegalArgumentException
	 *             牌的数量不合法
	 */
	Set<TileType> getReadyHandChances(PlayerTiles playerTiles);

	/**
	 * 计算并返回指定牌集合的所有和牌机会。即指定牌集合再得到什么牌可以和牌。
	 * 
	 * @param playerTiles
	 *            玩家的牌
	 * 
	 * @return 得到哪些牌型可以和牌
	 * @throws IllegalArgumentException
	 *             牌的数量不合法
	 */
	Set<TileType> getWinChances(PlayerTiles playerTiles);

	/**
	 * 返回指定听牌玩家的所有和牌及其分数。
	 * 
	 * @param playerTiles
	 *            玩家的牌
	 * @return 所有和牌到其分数的映射
	 * @throws IllegalArgumentException
	 *             牌的数量不合法，或指定牌集合不是听牌状态
	 */
	Map<TileType, Set<PointItem>> getPointsFromReadyHand(
			PlayerTiles playerTiles);

	/**
	 * 返回指定牌集合是否和牌。
	 * 
	 * @param playerTiles
	 *            玩家的牌
	 * 
	 * @return 如果和牌，返回true；否则返回false。
	 * @throws IllegalArgumentException
	 *             牌的数量不合法
	 */
	boolean isWin(PlayerTiles playerTiles);

	/**
	 * 返回赢家的基本得分。
	 * 
	 * @return 基本得分
	 */
	int getBasicPoints();

	/**
	 * 返回指定和牌集合的分数。
	 * 
	 * @param playerTiles
	 *            玩家的牌
	 * @return 分数
	 * @throws IllegalArgumentException
	 *             牌的数量不合法，或指定玩家的牌没有和牌
	 */
	Set<PointItem> getPoints(PlayerTiles playerTiles);

	/**
	 * 返回庄家分数倍数。
	 * 
	 * @return 倍数
	 */
	int getDealerMultiple();

}
