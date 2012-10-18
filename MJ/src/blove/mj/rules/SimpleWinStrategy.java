package blove.mj.rules;

import java.util.Collections;
import java.util.Set;

import blove.mj.PointsItem;
import blove.mj.Tile;
import blove.mj.board.PlayerTiles;

/**
 * 最简单的和牌策略。没有基本得分，和牌得分为10分。
 * 
 * @author 陈通
 */
public class SimpleWinStrategy extends AbstractWinStrategy {

	@Override
	public int getBasicPoints() {
		return 0;
	}

	@Override
	public int getDealerMultiple() {
		return 2;
	}

	protected boolean isWin(PlayerTiles playerTiles, Set<Tile> aliveTiles) {
		// TODO
		return false;
	}

	protected Set<PointsItem> getPoints(PlayerTiles playerTiles,
			Set<Tile> aliveTiles) {
		if (!isWin(playerTiles, aliveTiles))
			throw new IllegalArgumentException("指定玩家的牌不是和牌");

		return Collections.<PointsItem> singleton(new PointsItem() {

			@Override
			public int getPoints() {
				return 10;
			}

			@Override
			public String getName() {
				return "Win";
			}
		});
	}

}
