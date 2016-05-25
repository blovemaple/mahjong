package com.github.blovemaple.mj.rule.guobiao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.rule.AbstractFanType;

/**
 * 一色四同顺。包含一种花色序数相同的4副顺子。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class YiSeSiTongShun extends AbstractFanType {

	public YiSeSiTongShun() {
		addCacheKey(PlayerInfo::getTileGroups);
	}

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		Map<TileType, Long> countByType = countByType(playerInfo, realAliveTiles, null);
		if (countByType.size() != 4)
			return false;

		List<TileType> typeOf4 = new ArrayList<>();
		boolean found2 = false;
		for (Map.Entry<TileType, Long> entry : countByType.entrySet()) {
			switch (entry.getValue().intValue()) {
			case 2:
				if (found2)
					return false;
				found2 = true;
				break;
			case 4:
				typeOf4.add(entry.getKey());
				break;
			default:
				return false;
			}
		}
		
		//TODO

		return true;
	}

}
