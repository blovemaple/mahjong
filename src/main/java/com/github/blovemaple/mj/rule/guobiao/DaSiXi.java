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
 * 大四喜。由4副风刻(杠)加一对将牌组成的牌型。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class DaSiXi extends AbstractFanType {

	public DaSiXi() {
		addCacheKey(PlayerInfo::getTileGroups);
	}

	private static final Set<TileType> TILE_TYPES = new HashSet<>();
	static {
		TILE_TYPES.add(TileType.of(ZI, DONG_FENG));
		TILE_TYPES.add(TileType.of(ZI, NAN));
		TILE_TYPES.add(TileType.of(ZI, XI));
		TILE_TYPES.add(TileType.of(ZI, BEI));
	}

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		Map<TileType, Long> countByType = countByType(playerInfo, realAliveTiles, TILE_TYPES);
		if (countByType.size() < 4)
			return false;
		if (countByType.values().stream().anyMatch(count -> count < 3))
			return false;
		return true;
	}

}
