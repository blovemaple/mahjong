package blove.mj.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import blove.mj.PointsResult;
import blove.mj.TileType;

/**
 * 听牌。
 * 
 * @author 陈通
 */
public class ReadyHand {
	private final TileType discardTile;
	private final Map<TileType, PointsResult> winForTilesToPoints;

	/**
	 * 新建一个实例。
	 * 
	 * @param discardTile
	 *            上听时出牌
	 * @param winForTilesToScores
	 *            听牌到若和此牌得分的映射。
	 */
	public ReadyHand(TileType discardTile,
			Map<TileType, PointsResult> winForTilesToScores) {
		this.discardTile = discardTile;
		this.winForTilesToPoints = Collections.unmodifiableMap(new HashMap<>(
				winForTilesToScores));
	}

	/**
	 * 返回上听时出牌。
	 * 
	 * @return 上听时出牌
	 */
	public TileType getDiscardTile() {
		return discardTile;
	}

	/**
	 * 返回听牌集合。
	 * 
	 * @return 集合
	 */
	public Set<TileType> getWinForTiles() {
		return winForTilesToPoints.keySet();
	}

	/**
	 * 返回若和指定牌型时的得分情况。
	 * 
	 * @param tile
	 *            牌型
	 * @return 得分情况
	 * @throws IllegalArgumentException
	 *             指定牌型非上听牌型
	 */
	public PointsResult getPointItemsIfWin(TileType tile) {
		PointsResult points = winForTilesToPoints.get(tile);
		if (points != null)
			return points;
		else
			throw new IllegalArgumentException("非上听牌型：" + tile);
	}

}
