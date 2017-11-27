package com.github.blovemaple.mj.local.bazbot;

import java.util.List;
import java.util.Set;

import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.object.TileUnitType;

/**
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
class BazBotTileUnit {
	@SuppressWarnings("unused")
	private boolean completed;
	@SuppressWarnings("unused")
	private TileUnitType unitType;
	@SuppressWarnings("unused")
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

	}

	public static BazBotTileUnit completed(TileUnitType unitType, Set<Tile> tiles, BazBotTileNeighborhood hood) {
		return new BazBotTileUnit(true, unitType, tiles, hood);
	}

	public static BazBotTileUnit uncompleted(TileUnitType unitType, Set<Tile> tiles, BazBotTileNeighborhood hood) {
		return new BazBotTileUnit(false, unitType, tiles, hood);
	}

	private BazBotTileUnit(boolean isCompleted, TileUnitType unitType, Set<Tile> tiles, BazBotTileNeighborhood hood) {
		this.completed = isCompleted;
		this.unitType = unitType;
		this.tiles = tiles;
		this.hood = hood;
	}

	public BazBotTileUnitType type() {
		return type;
	}

	public BazBotTileNeighborhood hood() {
		return hood;
	}

	public boolean conflictWith(BazBotTileUnit other) {
		if (this == other)
			return true;

		// TODO
		return false;
	}

	public List<List<TileType>> forTileTypes(Set<Tile> conflictTiles) {
		// TODO
		return null;
	}

}
