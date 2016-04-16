package com.github.blovemaple.mj.object;

import java.util.Set;

/**
 * 牌的单元，即判断和牌时对牌的分组，如顺子、刻子、杠子、将牌等。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class TileUnit {
	private final TileUnitType type;
	private final Set<Tile> tiles;

	public TileUnit(TileUnitType type, Set<Tile> tiles) {
		this.type = type;
		this.tiles = tiles;
	}

	public TileUnitType getType() {
		return type;
	}

	public Set<Tile> getTiles() {
		return tiles;
	}

	@Override
	public String toString() {
		return "TileUnit [type=" + type + ", tiles=" + tiles + "]";
	}

}
