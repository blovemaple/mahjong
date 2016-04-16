package com.github.blovemaple.mj.object;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.blovemaple.mj.object.TileRank.NumberRank;

/**
 * 牌型。每个牌型的牌通常有四张。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class TileType implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 字牌的占位大小值。
	 */
	public static final int HONOR_RANK = 0;

	private static final Set<TileType> all;
	static {
		// 初始化所有牌型
		all = Collections.unmodifiableSet( //
				Stream.of(TileSuit.values())
						.flatMap(suit -> suit.getAllRanks().stream()
								.map(rank -> new TileType(suit, rank)))
						.collect(Collectors.toSet()));
	}

	/**
	 * 返回所有牌型的集合。
	 */
	public static Set<TileType> all() {
		return all;
	}

	/**
	 * 返回指定牌型。
	 */
	public static TileType of(TileSuit suit, TileRank<?> rank) {
		return all.stream().filter(
				type -> type.getSuit() == suit && type.getRank() == rank)
				.findAny().orElse(null);
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
	public TileSuit getSuit() {
		return suit;
	}

	/**
	 * 返回种类。
	 */
	public TileRank<?> getRank() {
		return rank;
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
