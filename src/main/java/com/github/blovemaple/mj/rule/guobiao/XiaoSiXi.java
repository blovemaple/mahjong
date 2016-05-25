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
 * 小四喜。包含3副风刻和风牌为将牌。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class XiaoSiXi extends AbstractFanType {

	public XiaoSiXi() {
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
		boolean found2 = false;
		for (Long count : countByType.values()) {
			if (count == 2) {
				if (found2)
					return false;
				found2 = true;
			}
		}
		return found2;
	}

}
