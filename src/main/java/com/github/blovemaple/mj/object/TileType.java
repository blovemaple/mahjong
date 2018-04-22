package com.github.blovemaple.mj.object;

import com.github.blovemaple.mj.object.TileRank.NumberRank;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	private final static int EAST = 0;
	private final static int SOUTH = 1;
	private final static int WEST = 2;
	private final static int NORTH = 3;
	private final static int RED = 4;
	private final static int GREEN = 5;
	private final static int WHITE =6;

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

	/**
	 * For example: of(Stick, 3) -> Stick three.
	 * @param suit
	 * @param index
	 * @return
	 */
	public static TileType of(TileSuit suit, int index){
		if(suit.compareTo(TileSuit.ZI) == 0) return ofNoNumber(index);
		return map.get(suit).get(TileRank.NumberRank.ofNumber(index));
	}

	public static TileType ofNoNumber(int i){
		return map.get(TileSuit.ZI).get(getRank(i));
	}

	private final TileSuit suit;
	private final TileRank<?> rank;

	private TileType(TileSuit suit, TileRank<?> rank) {
		this.suit = suit;
		this.rank = rank;
	}

	private static TileRank getRank(int i){
		switch (i){
			case EAST: return TileRank.ZiRank.DONG_FENG;
			case SOUTH: return TileRank.ZiRank.NAN;
			case WEST: return TileRank.ZiRank.XI;
			case NORTH: return TileRank.ZiRank.BEI;
			case RED: return TileRank.ZiRank.ZHONG;
			case GREEN: return TileRank.ZiRank.FA;
			case WHITE: return TileRank.ZiRank.BAI;
		}
		throw new UnsupportedOperationException("Cannot find non-number tile for "
						+ "current number "+ i);
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
	 * Range 1-9
	 * @throws UnsupportedOperationException
	 *             种类不是数字
	 */
	public int number() {
		if (!isNumberRank())
			throw new UnsupportedOperationException("No number for a non-number rank.");
		return ((NumberRank) rank).number();
	}

	/**
	 * Range 0-6 to represent DONG_FENG, NAN, XI, BEI, ZHONG, FA, BAI.
	 * @return
	 */
	public int notNumberIndex(){
		if(isNumberRank())
			throw new UnsupportedOperationException("No index for a number rank:" +
							this.suit() + this.number());
		String name = rank.name();
		if(name.equalsIgnoreCase("DONG_FENG")){
			return EAST;
		} else if(name.equalsIgnoreCase("NAN")){
			return SOUTH;
		} else if(name.equalsIgnoreCase("XI")){
			return WEST;
		} else if(name.equalsIgnoreCase("BEI")){
			return NORTH;
		} else if(name.equalsIgnoreCase("ZHONG")){
			return RED;
		} else if(name.equalsIgnoreCase("FA")){
			return GREEN;
		} else if(name.equalsIgnoreCase("BAI")){
			return WHITE;
		} else{
			throw new UnsupportedOperationException("Cannont recognize this "
							+ "non-number tile" + this.toString());
		}

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
