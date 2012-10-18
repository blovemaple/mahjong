package blove.mj.rules;

import java.util.Set;

import blove.mj.PointsItem;
import blove.mj.Tile;
import blove.mj.board.PlayerTiles;

/**
 * 可以判断匹配的得分项目。除了提供判断匹配的接口外，每个项目还具有是否为特殊和牌类型的属性。
 * 
 * @author blovemaple
 */
public interface MatchablePointsItem extends PointsItem {
	/**
	 * 是否为特殊的和牌类型。
	 * 
	 * @return 如果是，返回true；否则返回false。
	 */
	boolean isSpecialWinType();

	/**
	 * 指定玩家的牌是否匹配此项目。
	 * 
	 * @param tiles
	 *            牌
	 * @param aliveTiles
	 *            替代使用的aliveTiles。如果为null，则使用tiles中的aliveTiles。
	 * @return 如果匹配，返回true；否则返回false。
	 */
	boolean match(PlayerTiles tiles, Set<Tile> aliveTiles);

	/**
	 * 返回覆盖的得分项目（此得分项目有效时，哪些项目无效）。
	 * 
	 * @return 覆盖的得分项目集合
	 */
	Set<PointsItem> coverItems();

}
