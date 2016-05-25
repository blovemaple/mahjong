package com.github.blovemaple.mj.object;

import static com.github.blovemaple.mj.object.TileRank.NumberRank.*;
import static com.github.blovemaple.mj.object.TileSuit.*;
import static com.github.blovemaple.mj.object.TileType.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
		protected boolean isLegalTilesWithCorrectSize(Collection<Tile> tiles) {
			return tiles.stream().map(Tile::type).distinct().count() == 1;
		}

		@Override
		protected List<List<TileType>> getLackedTypesForLessTiles(Collection<Tile> tiles) {
			TileType type = tiles instanceof List ? ((List<Tile>) tiles).get(0).type() : tiles.iterator().next().type();
			return singletonList(singletonList(type));
		}
	},
	/**
	 * 顺子
	 */
	SHUNZI(3) {
		@Override
		protected boolean isLegalTilesWithCorrectSize(Collection<Tile> tiles) {
			// rank类型非NumberRank的，非法
			if (tiles.iterator().next().type().suit().getRankClass() != NumberRank.class)
				return false;

			// 花色有多种的，非法
			if (tiles.stream().map(tile -> tile.type().suit()).distinct().count() > 1)
				return false;

			// rank不连续的，非法
			int[] numbers = tiles.stream().mapToInt(tile -> ((NumberRank) tile.type().rank()).number()).sorted()
					.toArray();
			int crtNumber = 0;
			for (int number : numbers) {
				if (crtNumber == 0 || number == crtNumber + 1)
					crtNumber = number;
				else
					return false;
			}

			return true;
		}

		@Override
		protected List<List<TileType>> getLackedTypesForLessTiles(Collection<Tile> tiles) {
			if (tiles.size() == 1) {
				Tile tile = tiles instanceof List ? ((List<Tile>) tiles).get(0) : tiles.iterator().next();
				// rank类型非NumberRank的，非法
				if (tile.type().suit().getRankClass() != NumberRank.class)
					return emptyList();

				List<List<TileType>> result = new ArrayList<>();
				TileSuit suit = tile.type().suit();
				int number = ((NumberRank) tile.type().rank()).number();
				if (number >= 3)
					result.add(Arrays.asList(of(suit, ofNumber(number - 2)), of(suit, ofNumber(number - 1))));
				if (number >= 2 && number <= 8)
					result.add(Arrays.asList(of(suit, ofNumber(number - 1)), of(suit, ofNumber(number + 1))));
				if (number <= 7)
					result.add(Arrays.asList(of(suit, ofNumber(number + 1)), of(suit, ofNumber(number + 2))));
				return result;

			} else if (tiles.size() == 2) {
				// rank类型非NumberRank的，非法
				if (tiles.iterator().next().type().suit().getRankClass() != NumberRank.class)
					return emptyList();

				// 花色有多种的，非法
				if (tiles.stream().map(tile -> tile.type().suit()).distinct().count() > 1)
					return emptyList();

				List<List<TileType>> result = new ArrayList<>();
				TileSuit suit = tiles.iterator().next().type().suit();
				Iterator<Tile> tileItr = tiles.iterator();
				int number1 = ((NumberRank) tileItr.next().type().rank()).number();
				int number2 = ((NumberRank) tileItr.next().type().rank()).number();
				int distance = Math.abs(number2 - number1);
				// number差距不是1或2的，非法
				if (distance < 1 || distance > 2)
					return emptyList();
				if (number1 > number2) {
					int t = number2;
					number2 = number1;
					number1 = t;
				}
				switch (distance) {
				case 1:
					if (number1 >= 2)
						result.add(singletonList(of(suit, ofNumber(number1 - 1))));
					if (number2 <= 8)
						result.add(singletonList(of(suit, ofNumber(number2 + 1))));
					break;
				case 2:
					result.add(singletonList(of(suit, ofNumber(number1 + 1))));
					break;
				}
				return result;
			}
			throw new IllegalArgumentException(tiles.toString());
		}
	},
	/**
	 * 刻子
	 */
	KEZI(3) {
		@Override
		protected boolean isLegalTilesWithCorrectSize(Collection<Tile> tiles) {
			return tiles.stream().map(Tile::type).distinct().count() == 1;
		}

		@Override
		protected List<List<TileType>> getLackedTypesForLessTiles(Collection<Tile> tiles) {
			if (tiles.size() > 1 && tiles.stream().map(Tile::type).distinct().count() > 1)
				return emptyList();

			TileType type = tiles instanceof List ? ((List<Tile>) tiles).get(0).type() : tiles.iterator().next().type();
			if (tiles.size() == size() - 1)
				return singletonList(singletonList(type));
			return singletonList(nCopies(size() - tiles.size(), type));
		}
	},
	/**
	 * 杠子
	 */
	GANGZI(4) {
		@Override
		protected boolean isLegalTilesWithCorrectSize(Collection<Tile> tiles) {
			return tiles.stream().map(Tile::type).distinct().count() == 1;
		}

		@Override
		protected List<List<TileType>> getLackedTypesForLessTiles(Collection<Tile> tiles) {
			if (tiles.size() > 1 && tiles.stream().map(Tile::type).distinct().count() > 1)
				return emptyList();

			TileType type = tiles instanceof List ? ((List<Tile>) tiles).get(0).type() : tiles.iterator().next().type();
			if (tiles.size() == size() - 1)
				return singletonList(singletonList(type));
			return singletonList(nCopies(size() - tiles.size(), type));
		}
	},
	/**
	 * 花牌单元，通常是补花形成的牌组
	 */
	HUA_UNIT(1) {
		@Override
		protected boolean isLegalTilesWithCorrectSize(Collection<Tile> tiles) {
			return tiles.stream().allMatch(tile -> tile.type().suit() == HUA);
		}

		@Override
		protected List<List<TileType>> getLackedTypesForLessTiles(Collection<Tile> tiles) {
			throw new IllegalArgumentException(tiles.toString());
		}
	};

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(StandardTileUnitType.class.getSimpleName());

	private final int size;

	private StandardTileUnitType(int tileCount) {
		this.size = tileCount;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isLegalTiles(Collection<Tile> tiles) {
		if (tiles.size() != size())
			return false;
		return isLegalTilesWithCorrectSize(tiles);
	}

	protected abstract boolean isLegalTilesWithCorrectSize(Collection<Tile> tiles);

	public List<List<TileType>> getLackedTypesForTiles(Collection<Tile> tiles) {
		if (tiles.size() > size())
			return emptyList();
		if (tiles.size() == size())
			return isLegalTilesWithCorrectSize(tiles) ? singletonList(emptyList()) : emptyList();
		if (tiles.isEmpty())
			throw new IllegalArgumentException("tiles cannot be empty.");
		return getLackedTypesForLessTiles(tiles);
	}

	protected abstract List<List<TileType>> getLackedTypesForLessTiles(Collection<Tile> tiles);
}
