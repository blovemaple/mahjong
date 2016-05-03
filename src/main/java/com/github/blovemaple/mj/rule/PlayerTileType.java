package com.github.blovemaple.mj.rule;

import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;

/**
 * 玩家牌型，表示玩家的牌的特征，如和牌牌型和番种。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public interface PlayerTileType {
	public boolean match(PlayerInfo playerInfo, Set<Tile> aliveTiles);
}
