package com.github.blovemaple.mj.rule.guobiao;

import static com.github.blovemaple.mj.object.TileRank.NumberRank.*;
import static com.github.blovemaple.mj.object.TileRank.ZiRank.*;
import static com.github.blovemaple.mj.object.TileSuit.*;

import java.util.HashSet;
import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.rule.AbstractFanType;

/**
 * 绿一色。由条23468及字发组成。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class LvYiSe extends AbstractFanType {

	public LvYiSe() {
		addCacheKey(PlayerInfo::getTileGroups);
	}

	private static final Set<TileType> TILE_TYPES = new HashSet<>();
	static {
		TILE_TYPES.add(TileType.of(TIAO, ER));
		TILE_TYPES.add(TileType.of(TIAO, SAN));
		TILE_TYPES.add(TileType.of(TIAO, SI));
		TILE_TYPES.add(TileType.of(TIAO, LIU));
		TILE_TYPES.add(TileType.of(TIAO, BA));
		TILE_TYPES.add(TileType.of(ZI, FA));
	}

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		return tiles(playerInfo, realAliveTiles).map(Tile::type).allMatch(TILE_TYPES::contains);
	}

}
