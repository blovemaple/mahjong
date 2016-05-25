package com.github.blovemaple.mj.rule.guobiao;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileRank.NumberRank;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.rule.AbstractFanType;

/**
 * 连七对。由同一种花色顺序相连的7副对子组成。和牌类型应该由七对定义。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class LianQiDui extends AbstractFanType {

	public LianQiDui() {
	}

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		// 没有牌组
		if (!playerInfo.getTileGroups().isEmpty())
			return false;

		TileSuit suit = null;
		Map<Integer, Integer> countByRank = new HashMap<>();
		int min = 10, max = 0;
		for (Tile tile : realAliveTiles) {
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
			if (number < min)
				min = number;
			if (number > max)
				max = number;
			// 最小数字和最大数字差不超过6
			if (max - min > 6)
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
		// 最小数字和最大数字差不低于6
		if (max - min < 6)
			return false;
		// 每个数字不低于2个
		if (countByRank.values().stream().anyMatch(count -> count < 2))
			return false;
		return true;
	}

}
