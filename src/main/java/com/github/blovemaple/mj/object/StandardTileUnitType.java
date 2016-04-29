package com.github.blovemaple.mj.object;

import static com.github.blovemaple.mj.object.TileSuit.*;

import java.util.Set;
import java.util.logging.Logger;

import com.github.blovemaple.mj.object.TileRank.NumberRank;

/**
 * 一些标准的TileUnitType。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public enum StandardTileUnitType implements TileUnitType {
	/**
	 * 将牌
	 */
	JIANG(2) {
		@Override
		public boolean isLegalTilesWithCorrectSize(Set<Tile> tiles) {
			return tiles.stream().map(Tile::type).distinct().count() == 1;
		}
	},
	/**
	 * 顺子
	 */
	SHUNZI(3) {
		@Override
		public boolean isLegalTilesWithCorrectSize(Set<Tile> tiles) {
			// rank类型非NumberRank的，非法
			if (tiles.iterator().next().type().getSuit()
					.getRankClass() != NumberRank.class)
				return false;

			// 花色有多种的，非法
			if (tiles.stream().map(tile -> tile.type().getSuit()).distinct()
					.count() > 1)
				return false;

			// rank不连续的，非法
			int[] numbers = tiles.stream().mapToInt(
					tile -> ((NumberRank) tile.type().getRank()).getNumber())
					.sorted().toArray();
			int crtNumber = 0;
			for (int number : numbers) {
				if (crtNumber == 0 || number == crtNumber + 1)
					crtNumber = number;
				else
					return false;
			}

			return true;
		}
	},
	/**
	 * 刻子
	 */
	KEZI(3) {
		@Override
		public boolean isLegalTilesWithCorrectSize(Set<Tile> tiles) {
			return tiles.stream().map(Tile::type).distinct().count() == 1;
		}
	},
	/**
	 * 杠子
	 */
	GANGZI(4) {
		@Override
		protected boolean isLegalTilesWithCorrectSize(Set<Tile> tiles) {
			return tiles.stream().map(Tile::type).distinct().count() == 1;
		}
	},
	/**
	 * 花牌单元，通常是补花形成的牌组
	 */
	HUA_UNIT(1) {
		@Override
		protected boolean isLegalTilesWithCorrectSize(Set<Tile> tiles) {
			return tiles.stream()
					.allMatch(tile -> tile.type().getSuit() == HUA);
		}
	};

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(StandardTileUnitType.class.getSimpleName());

	private final int size;

	private StandardTileUnitType(int tileCount) {
		this.size = tileCount;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isLegalTiles(Set<Tile> tiles) {
		if (size() > 0 && tiles.size() != size())
			return false;
		return isLegalTilesWithCorrectSize(tiles);
	}

	protected abstract boolean isLegalTilesWithCorrectSize(Set<Tile> tiles);
}
