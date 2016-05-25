package com.github.blovemaple.mj.rule.guobiao;

import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.object.TileRank.NumberRank;
import com.github.blovemaple.mj.rule.AbstractFanType;

/**
 * 清幺九。由序数为1、9的刻子和将牌组成。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class QingYaoJiu extends AbstractFanType {

	public QingYaoJiu() {
		addCacheKey(PlayerInfo::getTileGroups);
	}

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		return tiles(playerInfo, realAliveTiles).map(tile -> tile.type().rank()).allMatch(rank -> {
			try {
				int number = ((NumberRank) rank).number();
				return number == 1 || number == 9;
			} catch (ClassCastException e) {
				return false;
			}
		});
	}

}
