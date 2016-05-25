package com.github.blovemaple.mj.rule.guobiao;

import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileSuit;
import com.github.blovemaple.mj.rule.AbstractFanType;

/**
 * 字一色。由字牌的刻子和将牌组成。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class ZiYiSe extends AbstractFanType {

	public ZiYiSe() {
		addCacheKey(PlayerInfo::getTileGroups);
	}

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		return tiles(playerInfo, realAliveTiles).map(tile -> tile.type().suit()).allMatch(suit -> suit == TileSuit.ZI);
	}

}
