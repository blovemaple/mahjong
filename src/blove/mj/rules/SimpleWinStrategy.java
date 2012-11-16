package blove.mj.rules;

import java.util.Collections;
import java.util.Set;

import blove.mj.PointItem;
import blove.mj.Tile;
import blove.mj.board.PlayerTiles;

/**
 * 最简单的和牌策略。没有基本得分，和牌得分为10分。
 * 
 * @author blovemaple
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

	@Override
	protected boolean isWin(PlayerTiles playerTiles, Set<Tile> aliveTiles) {
		return BasicWin.match(playerTiles, aliveTiles);
	}

	@Override
	protected Set<PointItem> getPoints(PlayerTiles playerTiles,
			Set<Tile> aliveTiles) {
		if (!isWin(playerTiles, aliveTiles))
			throw new IllegalArgumentException("指定玩家的牌不是和牌");
		return Collections
				.<PointItem> singleton(new SimplePointItem("Win", 10));
	}

}
