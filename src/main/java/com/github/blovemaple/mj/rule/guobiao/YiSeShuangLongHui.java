package com.github.blovemaple.mj.rule.guobiao;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileRank.NumberRank;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.rule.AbstractFanType;

/**
 * 一色双龙会。由一种花色的2个老少副和一对5为将牌组成。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class YiSeShuangLongHui extends AbstractFanType {

	public YiSeShuangLongHui() {
		addCacheKey(PlayerInfo::getTileGroups);
	}

	private static final Set<Integer> TILE_RANKS = new HashSet<>(Arrays.asList(1, 2, 3, 5, 7, 8, 9));

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		TileSuit suit = null;
		Map<Integer, Integer> countByRank = new HashMap<>();
		for (Tile tile : tiles(playerInfo, realAliveTiles).collect(Collectors.toList())) {
			TileSuit crtSuit = tile.type().suit();
			// 花色相同
			if (suit == null) {
				// 是数字牌
				if (crtSuit.getRankClass() != NumberRank.class)
					return false;
				suit = crtSuit;
			} else if (suit != crtSuit)
				return false;

			int number = ((NumberRank) tile.type().rank()).number();
			// 必须是1 2 3 5 7 8 9中的一个
			if (!TILE_RANKS.contains(number))
				return false;

			Integer count = countByRank.get(number);
			if (count == null) {
				countByRank.put(number, 1);
			} else {
				count++;
				// 每个数字不超过2个
				if (count > 2)
					return false;
				countByRank.put(number, count);
			}

		}
		return true;
	}

}
