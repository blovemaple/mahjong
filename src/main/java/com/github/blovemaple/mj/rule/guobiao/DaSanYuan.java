package com.github.blovemaple.mj.rule.guobiao;

import static com.github.blovemaple.mj.object.TileRank.ZiRank.*;
import static com.github.blovemaple.mj.object.TileSuit.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.rule.AbstractFanType;

/**
 * 大三元。牌里有中、发、白3副刻子。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class DaSanYuan extends AbstractFanType {

	public DaSanYuan() {
		addCacheKey(PlayerInfo::getTileGroups);
	}

	private static final Set<TileType> TILE_TYPES = new HashSet<>();
	static {
		TILE_TYPES.add(TileType.of(ZI, ZHONG));
		TILE_TYPES.add(TileType.of(ZI, FA));
		TILE_TYPES.add(TileType.of(ZI, BAI));
	}

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		Map<TileType, Long> countByType = countByType(playerInfo, realAliveTiles, TILE_TYPES);
		if (countByType.size() < 3)
			return false;
		if (countByType.values().stream().anyMatch(count -> count < 3))
			return false;
		return true;
	}

}
