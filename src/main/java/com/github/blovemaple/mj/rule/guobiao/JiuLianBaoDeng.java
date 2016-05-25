package com.github.blovemaple.mj.rule.guobiao;

import static com.github.blovemaple.mj.object.TileSuit.*;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileRank;
import com.github.blovemaple.mj.object.TileRank.NumberRank;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.rule.AbstractFanType;

/**
 * 九莲宝灯。一种花色牌按1112345678999组成，见同花色和牌。必须是门前清。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class JiuLianBaoDeng extends AbstractFanType {

	public JiuLianBaoDeng() {
	}

	private static final Set<TileSuit> TILE_SUITS = EnumSet.of(WAN, TIAO, BING);

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		// 没有牌组
		if (!playerInfo.getTileGroups().isEmpty())
			return false;

		// 花色相同，并且是数字牌
		if (!isSameSuit(playerInfo, realAliveTiles, TILE_SUITS))
			return false;

		Map<TileRank<?>, Long> countByRank = countByRank(playerInfo, realAliveTiles, null);
		// 九个数字都有
		if (countByRank.size() < 9)
			return false;
		// 一和九必须有3个（这样也能保证其他数字的数量都是对的）
		if (countByRank.get(NumberRank.YI).intValue() != 3)
			return false;
		if (countByRank.get(NumberRank.JIU).intValue() != 3)
			return false;
		return true;
	}

}
