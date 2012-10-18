package blove.mj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import blove.mj.PlayerLocation.Relation;
import blove.mj.TileType.Suit;
import blove.mj.board.PlayerTiles;

/**
 * 吃碰杠类型。
 * 
 * @author 陈通
 */
public enum CpkType {
	/**
	 * 吃
	 */
	CHOW("吃", 2) {

		@Override
		public Set<Cpk> getChances(PlayerTiles tiles, Tile newTile,
				Relation fromRelation) {
			if (fromRelation != Relation.PREVIOUS)
				return Collections.emptySet();
			if (newTile.getType().getSuit().isHonor())
				return Collections.emptySet();

			Set<Cpk> chances = new HashSet<>();
			TileType newTileType = newTile.getType();
			Set<Tile> aliveTiles = tiles.getAliveTiles();

			Set<Tile> prev2Tiles, prev1Tiles, next1Tiles, next2Tiles;
			if (newTileType.getRank() > 2)
				prev2Tiles = getTilesForType(
						aliveTiles,
						TileType.get(newTileType.getSuit(),
								newTileType.getRank() - 2));
			else
				prev2Tiles = Collections.emptySet();
			if (newTileType.getRank() > 1)
				prev1Tiles = getTilesForType(
						aliveTiles,
						TileType.get(newTileType.getSuit(),
								newTileType.getRank() - 1));
			else
				prev1Tiles = Collections.emptySet();
			if (newTileType.getRank() < 9)
				next1Tiles = getTilesForType(
						aliveTiles,
						TileType.get(newTileType.getSuit(),
								newTileType.getRank() + 1));
			else
				next1Tiles = Collections.emptySet();
			if (newTileType.getRank() < 8)
				next2Tiles = getTilesForType(
						aliveTiles,
						TileType.get(newTileType.getSuit(),
								newTileType.getRank() + 2));
			else
				next2Tiles = Collections.emptySet();

			chances.addAll(composeChances(prev2Tiles, prev1Tiles, newTile));
			chances.addAll(composeChances(prev1Tiles, next1Tiles, newTile));
			chances.addAll(composeChances(next1Tiles, next2Tiles, newTile));

			return chances;
		}

		private Set<Cpk> composeChances(Set<Tile> tiles1, Set<Tile> tiles2,
				Tile newTile) {
			if (tiles1.isEmpty() || tiles2.isEmpty())
				return Collections.emptySet();

			Set<Cpk> cpks = new HashSet<>();
			for (Tile tile1 : tiles1)
				for (Tile tile2 : tiles2)
					cpks.add(new Cpk(CHOW, newTile, Relation.PREVIOUS, tile1,
							tile2, newTile));
			return cpks;
		}

		@Override
		boolean isValid(Set<Tile> tiles) {
			if (tiles.size() != 3)
				return false;
			List<Tile> list = new LinkedList<>(tiles);
			Collections.sort(list);
			Suit suit = null;
			int lastRank = 0;
			for (Tile tile : list) {
				if (suit == null) {
					suit = tile.getType().getSuit();
					lastRank = tile.getType().getRank();
				} else {
					if (tile.getType().getSuit() != suit
							|| tile.getType().getRank() != ++lastRank)
						return false;
				}
			}
			return true;
		}

	},
	/**
	 * 碰
	 */
	PONG("碰", 1) {

		@Override
		Set<Cpk> getChances(PlayerTiles tiles, Tile newTile,
				Relation fromRelation) {
			if (tiles.isForDiscarding())
				return Collections.emptySet();

			Set<Cpk> chances = new HashSet<Cpk>();

			Set<Tile> sameTiles = getTilesForType(tiles.getAliveTiles(),
					newTile.getType());
			if (sameTiles.size() < 2)
				return Collections.emptySet();
			else if (sameTiles.size() == 2) {
				sameTiles.add(newTile);
				chances.add(new Cpk(PONG, newTile, fromRelation, sameTiles
						.toArray(new Tile[] {})));
			} else {
				List<Tile> sameTilesList = new ArrayList<>(sameTiles);
				for (int i1 = 0; i1 < sameTilesList.size() - 1; i1++) {
					for (int i2 = i1 + 1; i2 < sameTilesList.size(); i2++) {
						chances.add(new Cpk(PONG, newTile, fromRelation,
								sameTilesList.get(i1), sameTilesList.get(i2),
								newTile));
					}
				}
			}

			return chances;
		}

		@Override
		boolean isValid(Set<Tile> tiles) {
			if (tiles.size() != 3)
				return false;
			return isSameTypeTiles(tiles);
		}
	},
	/**
	 * 明杠
	 */
	EXPOSED_KONG("明杠", 0, true) {
		@Override
		Set<Cpk> getChances(PlayerTiles tiles, Tile newTile,
				Relation fromRelation) {
			if (fromRelation == Relation.SELF) {
				// 自摸明杠
				if (!tiles.isForDiscarding())
					return Collections.emptySet();

				Set<Cpk> chances = null;
				for (Cpk myCpk : tiles.getCpks()) {
					if (myCpk.getType() == CpkType.PONG
							&& myCpk.getForTile().getType()
									.equals(newTile.getType())) {
						List<Tile> kongTiles = new ArrayList<>(4);
						kongTiles.addAll(myCpk.getTiles());
						kongTiles.add(newTile);
						chances = Collections.singleton(new Cpk(EXPOSED_KONG,
								newTile, Relation.SELF, kongTiles
										.toArray(new Tile[] {})));
						break;
					}
				}
				if (chances == null)
					chances = Collections.emptySet();
				return chances;
			} else {
				// 普通明杠
				if (tiles.isForDiscarding())
					return Collections.emptySet();

				Set<Cpk> chances = null;
				Set<Tile> sameTiles = getTilesForType(tiles.getAliveTiles(),
						newTile.getType());
				if (sameTiles.size() == 3) {
					List<Tile> kongTiles = new ArrayList<>(4);
					kongTiles.addAll(sameTiles);
					kongTiles.add(newTile);
					chances = Collections.singleton(new Cpk(EXPOSED_KONG,
							newTile, Relation.SELF, kongTiles
									.toArray(new Tile[] {})));
				} else
					chances = Collections.emptySet();
				return chances;
			}
		}

		@Override
		boolean isValid(Set<Tile> tiles) {
			if (tiles.size() != 4)
				return false;
			return isSameTypeTiles(tiles);
		}
	},
	/**
	 * 暗杠
	 */
	CONCEALED_KONG("暗杠", 0, true) {
		@Override
		Set<Cpk> getChances(PlayerTiles tiles, Tile newTile,
				Relation fromRelation) {
			if (!tiles.isForDiscarding())
				return Collections.emptySet();

			Set<Cpk> chances = new HashSet<Cpk>();
			Map<TileType, Integer> countForType = new HashMap<>();
			for (Tile tile : tiles.getAliveTiles()) {
				TileType type = tile.getType();
				Integer count = countForType.get(type);
				if (count == null)
					countForType.put(type, 1);
				else
					countForType.put(type, count++);
			}

			for (Map.Entry<TileType, Integer> countEntry : countForType
					.entrySet()) {
				TileType type = countEntry.getKey();
				int count = countEntry.getValue();
				if (count == 4) {
					Set<Tile> sameTiles = getTilesForType(
							tiles.getAliveTiles(), type);
					chances.add(new Cpk(CONCEALED_KONG, newTile, fromRelation,
							sameTiles.toArray(new Tile[] {})));
				}
			}

			return chances;
		}

		@Override
		boolean isValid(Set<Tile> tiles) {
			return EXPOSED_KONG.isValid(tiles);
		}
	};

	private final String name;
	private final int priority;
	private final boolean isKong;

	private CpkType(String name, int priority) {
		this(name, priority, false);
	}

	private CpkType(String name, int priority, boolean isKong) {
		this.name = name;
		this.priority = priority;
		this.isKong = isKong;
	}

	/**
	 * 根据指定条件返回机会。
	 * 
	 * @param tiles
	 *            玩家手中的牌
	 * @param newTile
	 *            新得到的牌
	 * @param fromRelation
	 *            得牌来源
	 * @return 机会集合
	 */
	abstract Set<Cpk> getChances(PlayerTiles tiles, Tile newTile,
			Relation fromRelation);

	/**
	 * 判断指定牌组是否合法。
	 * 
	 * @param tiles
	 *            牌组
	 * @return 如果合法，返回true；否则返回false。
	 */
	abstract boolean isValid(Set<Tile> tiles);

	/**
	 * 判断指定牌组是否都是相同牌型的牌。用于判断碰、杠牌组的合法性。
	 * 
	 * @param tiles
	 *            牌组
	 * @return 如果相同，返回true；否则返回false。
	 */
	private static boolean isSameTypeTiles(Collection<Tile> tiles) {
		TileType type = null;
		for (Tile tile : tiles) {
			if (type == null)
				type = tile.getType();
			else {
				if (!tile.getType().equals(type))
					return false;
			}
		}
		return true;
	}

	/**
	 * 返回指定牌集合里指定类型的牌。
	 * 
	 * @param tiles
	 *            牌集合
	 * @param type
	 *            类型
	 * @return 指定类型的牌集合
	 */
	private static Set<Tile> getTilesForType(Set<Tile> tiles, TileType type) {
		Set<Tile> typeTiles = new HashSet<>();
		for (Tile tile : tiles)
			if (tile.getType().equals(type))
				typeTiles.add(tile);
		return typeTiles;
	}

	/**
	 * 返回所有吃/碰/杠机会。
	 * 
	 * @param tiles
	 *            玩家手中的牌
	 * @param forTile
	 *            新得到的牌
	 * @param fromRelation
	 *            得牌来源
	 * @return 机会集合
	 */
	public static Set<Cpk> getAllChances(PlayerTiles tiles, Tile forTile,
			Relation fromRelation) {
		Set<Cpk> chances = new HashSet<>();
		for (CpkType type : CpkType.values())
			chances.addAll(type.getChances(tiles, forTile, fromRelation));
		return chances;
	}

	/**
	 * 返回是否是杠。
	 * 
	 * @return 如果是杠，返回true；否则返回false。
	 */
	public boolean isKong() {
		return isKong;
	}

	int getPriority() {
		return priority;
	}

	@Override
	public String toString() {
		return name;
	}
}