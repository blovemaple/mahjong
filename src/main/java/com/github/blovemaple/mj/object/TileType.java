package com.github.blovemaple.mj.object;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.object.TileRank.NumberRank;

/**
 * 牌型。每个牌型的牌通常有四张。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class TileType implements Serializable, Comparable<TileType> {
	private static final long serialVersionUID = 1L;

	/**
	 * 字牌的占位大小值。
	 */
	public static final int HONOR_RANK = 0;

	private static final List<TileType> all;
	private static final Map<TileSuit, Map<TileRank<?>, TileType>> map;
	static {
		// 初始化所有牌型
		all = Collections.unmodifiableList( //
				Stream.of(TileSuit.values())
						.flatMap(suit -> suit.getAllRanks().stream().map(rank -> new TileType(suit, rank)))
						.collect(Collectors.toList()));
		map = all.stream().collect( //
				Collectors.groupingBy(TileType::suit, //
						Collectors.groupingBy(TileType::rank, //
								Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0)))));
	}

	/**
	 * 返回所有牌型的列表。
	 */
	public static List<TileType> all() {
		return all;
	}

	/**
	 * 返回指定牌型。
	 */
	public static TileType of(TileSuit suit, TileRank<?> rank) {
		return map.get(suit).get(rank);
	}

	private final TileSuit suit;
	private final TileRank<?> rank;

	private TileType(TileSuit suit, TileRank<?> rank) {
		this.suit = suit;
		this.rank = rank;
	}

	/**
	 * 返回花色。
	 */
	public TileSuit suit() {
		return suit;
	}

	/**
	 * 返回种类。
	 */
	public TileRank<?> rank() {
		return rank;
	}
	
	/**
	 * 返回种类是否是数字。
	 */
	public boolean isNumberRank() {
		return rank.getClass()==NumberRank.class;
	}

	/**
	 * 返回数字种类的数值。
	 * 
	 * @throws UnsupportedOperationException
	 *             种类不是数字
	 */
	public int number() {
		if (!isNumberRank())
			throw new UnsupportedOperationException("No number for a non-number rank.");
		return ((NumberRank) rank).number();
	}

	@Override
	public int compareTo(TileType o) {
		if (this == o)
			return 0;
		int suitRes = this.suit().compareTo(o.suit());
		if (suitRes != 0)
			return suitRes;
		return TileRank.compare(this.rank(), o.rank());
	}

	/**
	 * Just for debug.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (suit.getRankClass() == NumberRank.class)
			return rank.toString() + " " + suit;
		else
			return rank.toString();
	}

}
