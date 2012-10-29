package blove.mj.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import blove.mj.PointItem;
import blove.mj.Tile;
import blove.mj.TileType;
import blove.mj.board.PlayerTiles;

/**
 * 简化的和牌策略接口。
 * 
 * @author blovemaple
 */
public abstract class AbstractWinStrategy implements WinStrategy {

	@Override
	public Set<TileType> getReadyHandChances(PlayerTiles playerTiles) {
		if (!playerTiles.isForDiscarding())
			throw new IllegalArgumentException("牌的数量不合法。");

		Set<TileType> chances = new HashSet<>();
		Set<Tile> aliveTiles = new HashSet<>(playerTiles.getAliveTiles());
		for (Tile discardTile : playerTiles.getAliveTiles()) {
			aliveTiles.remove(discardTile);
			if (!getWinChances(playerTiles, aliveTiles).isEmpty())
				chances.add(discardTile.getType());
			aliveTiles.add(discardTile);
		}

		return chances;
	}

	@Override
	public Set<TileType> getWinChances(PlayerTiles playerTiles) {
		return getWinChances(playerTiles, null);
	}

	@Override
	public Map<TileType, Set<PointItem>> getPointsFromReadyHand(
			PlayerTiles playerTiles) {
		Set<TileType> winChances = getWinChances(playerTiles);
		Set<Tile> aliveTiles = new HashSet<>(playerTiles.getAliveTiles());
		Map<TileType, Set<PointItem>> points = new HashMap<>();
		for (TileType winTileType : winChances) {
			Tile winTile = findAdditionalTileFromType(aliveTiles, winTileType);
			if (winTile == null)
				throw new RuntimeException();// 不可能出现

			aliveTiles.add(winTile);
			points.put(winTileType, getPoints(playerTiles, aliveTiles));
			aliveTiles.remove(winTile);
		}

		return points;
	}

	@Override
	public boolean isWin(PlayerTiles playerTiles) {
		return isWin(playerTiles, null);
	}

	@Override
	public Set<PointItem> getPoints(PlayerTiles playerTiles) {
		return getPoints(playerTiles, null);
	}

	/**
	 * 计算并返回指定牌集合的所有和牌机会。即指定牌集合再得到什么牌可以和牌。
	 * 
	 * @param playerTiles
	 *            玩家的牌
	 * @param aliveTiles
	 *            替代使用的aliveTiles。如果为null，则使用playerTiles中的aliveTiles。
	 * 
	 * @return 得到哪些牌型可以和牌
	 * @throws IllegalArgumentException
	 *             牌的数量不合法
	 */
	private Set<TileType> getWinChances(PlayerTiles playerTiles,
			Set<Tile> aliveTiles) {
		if ((aliveTiles != null && PlayerTiles.isForDiscarding(aliveTiles))
				|| playerTiles.isForDiscarding())
			throw new IllegalArgumentException("牌的数量不合法。");

		Set<TileType> chances = new HashSet<>();
		Set<Tile> winAttemptTiles = new HashSet<>(
				aliveTiles != null ? aliveTiles : playerTiles.getAliveTiles());

		Set<TileType> attemptedTypes = new HashSet<>();
		for (Tile tile : Tile.getAllTiles()) {
			TileType type = tile.getType();
			if (attemptedTypes.contains(type))
				continue;

			winAttemptTiles.add(tile);
			if (isWin(playerTiles, winAttemptTiles))
				chances.add(type);
			winAttemptTiles.remove(tile);
			attemptedTypes.add(type);
		}

		return chances;
	}

	private Tile findAdditionalTileFromType(Set<Tile> aliveTiles, TileType type) {
		Set<Tile> winTiles = Tile.getTilesForType(type);
		for (Tile tile : winTiles) {
			if (!aliveTiles.contains(tile)) {
				return tile;
			}
		}
		return null;
	}

	/**
	 * 判断指定玩家的牌是否和牌。
	 * 
	 * @param playerTiles
	 *            玩家的牌
	 * @param aliveTiles
	 *            替代使用的aliveTiles。如果为null，则使用playerTiles中的aliveTiles。
	 * @return 如果和牌，返回true；否则返回false。
	 * @throws IllegalArgumentException
	 *             牌的数量不合法
	 */
	protected abstract boolean isWin(PlayerTiles playerTiles,
			Set<Tile> aliveTiles);

	/**
	 * 返回指定和牌集合的分数。
	 * 
	 * @param playerTiles
	 *            玩家的牌
	 * @param aliveTiles
	 *            替代使用的aliveTiles。如果为null，则使用playerTiles中的aliveTiles。
	 * @return 得分
	 * @throws IllegalArgumentException
	 *             牌的数量不合法，或指定玩家的牌不是和牌
	 */
	protected abstract Set<PointItem> getPoints(PlayerTiles playerTiles,
			Set<Tile> aliveTiles);

}
