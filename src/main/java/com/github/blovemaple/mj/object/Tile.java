package com.github.blovemaple.mj.object;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 麻将牌。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class Tile implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final List<Tile> all;
	static {
		// 初始化所有牌
		List<Tile> allTiles = TileType.all().stream()
				.flatMap(type -> IntStream
						.range(0, type.suit().getTileCountByType())
						.mapToObj(id -> new Tile(type, id)))
				.collect(Collectors.toList());
		all = Collections.unmodifiableList(allTiles);
	}

	/**
	 * 返回所有144张牌的列表。
	 */
	public static List<Tile> all() {
		return all;
	}

	/**
	 * 返回指定牌型的所有牌的集合。
	 */
	public static Set<Tile> allOfType(TileType type) {
		return all.stream().filter(tile -> tile.type() == type)
				.collect(Collectors.toSet());
	}

	/**
	 * 返回指定牌。
	 */
	public static Tile of(TileType type, int id) {
		return all.stream()
				.filter(tile -> tile.type() == type && tile.id() == id)
				.findAny().orElse(null);
	}

	private final TileType type;
	private final int id;// 每个牌型从0到3。

	private Tile(TileType type, int id) {
		this.type = type;
		this.id = id;
	}

	/**
	 * 返回牌型。
	 */
	public TileType type() {
		return type;
	}

	/**
	 * 返回ID，同一牌型从0开始，通常是[0,3]。
	 */
	public int id() {
		return id;
	}

	/**
	 * Just for debug.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(type.isNumberRank()) {
			return "" + type.suit() + type.number();
		}else {
			if(type.suit().equals(TileSuit.HUA)){
				return ""+type;
			}
			return "" + type.suit() + type.notNumberIndex();
		}
//		return "[" + type + ", " + id + "]";
	}

}
