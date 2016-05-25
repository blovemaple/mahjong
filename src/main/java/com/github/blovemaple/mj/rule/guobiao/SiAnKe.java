package com.github.blovemaple.mj.rule.guobiao;

import java.util.Map;
import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileGroupType;
import com.github.blovemaple.mj.object.TileType;
import com.github.blovemaple.mj.rule.AbstractFanType;

/**
 * 四暗刻。包含四个暗刻。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class SiAnKe extends AbstractFanType {

	public SiAnKe() {
		addCacheKey(PlayerInfo::getTileGroups);
	}

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		// 牌组中只有暗杠
		if (playerInfo.getTileGroups().stream().anyMatch(group -> group.getType() != TileGroupType.ANGANG_GROUP))
			return false;

		// 手牌按type统计，有一个2和若干个3
		Map<TileType, Long> countByType = countByType(null, realAliveTiles, null);
		boolean found2 = false;
		for (Long count : countByType.values()) {
			if (count == 2) {
				if (found2)
					return false;
				found2 = true;
			} else if (count != 3)
				return false;
		}
		return found2;
	}

}
