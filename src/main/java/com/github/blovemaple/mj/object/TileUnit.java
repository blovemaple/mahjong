package com.github.blovemaple.mj.object;

import static com.github.blovemaple.mj.object.TileUnit.TileUnitSource.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 牌的单元，即判断和牌时对牌的分组，如顺子、刻子、杠子、将牌等。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class TileUnit {
	private final TileUnitType type;
	private final Set<Tile> tiles;
	private List<TileType> tileTypes; // 排序过的
	private final TileUnitSource source;
	private final Tile gotTile;

	public enum TileUnitSource {
		SELF_OR_WIN, GOT_IN_GAME
	}

	public static TileUnit selfOrWin(TileUnitType type, Collection<Tile> tiles) {
		return new TileUnit(type, tiles, SELF_OR_WIN, null);
	}

	public static TileUnit gotInGame(TileUnitType type, Collection<Tile> tiles, Tile gotTile) {
		return new TileUnit(type, tiles, GOT_IN_GAME, gotTile);
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

	public List<TileType> getTileTypes() {
		if (tileTypes == null)
			tileTypes = tiles.stream().map(Tile::type).sorted().collect(Collectors.toList());
		return tileTypes;
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
