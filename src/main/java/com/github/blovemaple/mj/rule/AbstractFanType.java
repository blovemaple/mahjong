package com.github.blovemaple.mj.rule;

import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroup;
import com.github.blovemaple.mj.object.TileRank;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.object.TileType;

/**
 * FanType的共同逻辑。带缓存，默认识别条件由{@link CachedPlayerTileType}定义。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class AbstractFanType extends CachedPlayerTileType implements FanType {

	protected static Map<TileType, Long> countByType(PlayerInfo playerInfo, Set<Tile> realAliveTiles,
			Collection<TileType> types) {
		if (types != null && types.isEmpty())
			return Collections.emptyMap();

		Stream<Tile> tiles = tiles(playerInfo, realAliveTiles);
		if (types != null)
			tiles = tiles.filter(tile -> types.contains(tile.type()));
		return tiles.collect(groupingBy(Tile::type, counting()));
	}

	protected static Map<TileRank<?>, Long> countByRank(PlayerInfo playerInfo, Set<Tile> realAliveTiles,
			Collection<TileRank<?>> ranks) {
		if (ranks != null && ranks.isEmpty())
			return Collections.emptyMap();

		Stream<Tile> tiles = tiles(playerInfo, realAliveTiles);
		if (ranks != null)
			tiles = tiles.filter(tile -> ranks.contains(tile.type().rank()));
		return tiles.collect(groupingBy(tile -> tile.type().rank(), counting()));
	}

	protected static boolean isSameSuit(PlayerInfo playerInfo, Set<Tile> realAliveTiles, Set<TileSuit> legalSuits) {
		TileSuit suit = realAliveTiles.iterator().next().type().suit();
		if (legalSuits != null && !legalSuits.contains(suit))
			return false;
		return tiles(playerInfo, realAliveTiles).allMatch(tile -> tile.type().suit() == suit);
	}

	protected static Stream<Tile> tiles(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		Stream<Tile> tiles = realAliveTiles.stream();
		if (playerInfo != null)
			for (TileGroup group : playerInfo.getTileGroups())
				tiles = Stream.concat(tiles, group.getTiles().stream());
		return tiles;
	}

}
