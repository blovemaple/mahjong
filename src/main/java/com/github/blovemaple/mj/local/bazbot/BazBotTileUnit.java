package com.github.blovemaple.mj.local.bazbot;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.github.blovemaple.mj.object.StandardTileUnitType;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
class BazBotTileUnit {
	@SuppressWarnings("unused")
	private boolean completed;
	@SuppressWarnings("unused")
	private StandardTileUnitType unitType;
	private Set<Tile> tiles;

	private BazBotTileUnitType type;
	private BazBotTileNeighborhood hood;

	enum BazBotTileUnitType {
		/**
		 * 完整将牌。
		 */
		COMPLETE_JIANG(true),
		/**
		 * 完整顺刻。
		 */
		COMPLETE_SHUNKE(false),
		/**
		 * 缺一张的不完整顺刻。
		 */
		UNCOMPLETE_SHUNKE_FOR_ONE(false),
		/**
		 * 缺两张的不完整顺刻。
		 */
		UNCOMPLETE_SHUNKE_FOR_TWO(false),
		/**
		 * 不完整将牌。
		 */
		UNCOMPLETE_JIANG(true);

		private final boolean isJiang;

		private BazBotTileUnitType(boolean isJiang) {
			this.isJiang = isJiang;
		}

		public boolean isJiang() {
			return isJiang;
		}

		public static BazBotTileUnitType of(boolean completed, StandardTileUnitType unitType, Set<Tile> tiles) {
			switch (unitType) {
			case GANGZI:
			case HUA_UNIT:
				throw new RuntimeException("Unsupported StandardTileUnitType: " + unitType);
			case JIANG:
				return completed ? COMPLETE_JIANG : UNCOMPLETE_JIANG;
			case KEZI:
			case SHUNZI:
				return completed ? COMPLETE_SHUNKE
						: unitType.size() - tiles.size() == 1 ? UNCOMPLETE_SHUNKE_FOR_ONE : UNCOMPLETE_SHUNKE_FOR_TWO;
			default:
				throw new RuntimeException("Unrecognized StandardTileUnitType: " + unitType);
			}
		}

	}

	public static BazBotTileUnit completed(StandardTileUnitType unitType, Set<Tile> tiles,
			BazBotTileNeighborhood hood) {
		return new BazBotTileUnit(true, unitType, tiles, hood);
	}

	public static BazBotTileUnit uncompleted(StandardTileUnitType unitType, Set<Tile> tiles,
			BazBotTileNeighborhood hood) {
		return new BazBotTileUnit(false, unitType, tiles, hood);
	}

	private BazBotTileUnit(boolean isCompleted, StandardTileUnitType unitType, Set<Tile> tiles,
			BazBotTileNeighborhood hood) {
		this.completed = isCompleted;
		this.unitType = unitType;
		this.tiles = tiles;
		this.type = BazBotTileUnitType.of(isCompleted, unitType, tiles);
		this.hood = hood;
	}

	public Set<Tile> tiles() {
		return tiles;
	}

	public BazBotTileUnitType type() {
		return type;
	}

	public BazBotTileNeighborhood hood() {
		return hood;
	}

	public boolean conflictWith(BazBotTileUnit other) {
		if (tiles.isEmpty() || other.tiles.isEmpty())
			return false;
		if (this == other)
			return !tiles.isEmpty();
		return !disjoint(tiles, other.tiles);
	}

	public boolean conflictWith(Collection<Tile> tiles) {
		if (this.tiles.isEmpty() || tiles.isEmpty())
			return false;
		return !disjoint(this.tiles, tiles);
	}

	public List<List<TileType>> forTileTypes(Set<Tile> conflictTiles) {
		if (completed)
			return List.of(List.of());
		else {
			Set<TileType> conflictTileTypes = conflictTiles.stream().map(Tile::type).collect(toSet());
			return unitType.getLackedTypesForTiles(this.tiles).stream()
					.filter(tileTypes -> disjoint(tileTypes, conflictTileTypes)).collect(toList());
		}
	}

}
