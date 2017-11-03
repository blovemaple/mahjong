package com.github.blovemaple.mj.object;

import static com.github.blovemaple.mj.object.TileUnit.TileUnitSource.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * 牌的单元，即判断和牌时对牌的分组，如顺子、刻子、杠子、将牌等。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class TileUnit {
	private final TileUnitType type;
	private final Set<Tile> tiles;
	private TileType firstTileType; // 最小的一个牌型
	private final TileUnitSource source;
	private final Tile gotTile;

	public enum TileUnitSource {
		SELF, GOT
	}

	public static TileUnit self(TileUnitType type, Collection<Tile> tiles) {
		return new TileUnit(type, tiles, SELF, null);
	}

	public static TileUnit got(TileUnitType type, Collection<Tile> tiles, Tile gotTile) {
		return new TileUnit(type, tiles, GOT, gotTile);
	}

	public TileUnit(TileUnitType type, Collection<Tile> tiles, TileUnitSource source, Tile gotTile) {
		this.type = type;
		this.tiles = tiles instanceof Set ? (Set<Tile>) tiles : new HashSet<>(tiles);
		this.source = source;
		this.gotTile = gotTile;
	}

	public TileUnitType getType() {
		return type;
	}

	public Set<Tile> getTiles() {
		return tiles;
	}

	public TileType getFirstTileType() {
		if (firstTileType == null)
			firstTileType = tiles.stream().map(Tile::type).min(Comparator.naturalOrder()).orElse(null);
		return firstTileType;
	}

	public TileUnitSource getSource() {
		return source;
	}

	public Tile getGotTile() {
		return gotTile;
	}

	@Override
	public String toString() {
		return "TileUnit [type=" + type + ", tiles=" + tiles + "]";
	}

}
