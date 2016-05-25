package com.github.blovemaple.mj.rule;

import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;

/**
 * WinType的共同逻辑。带缓存，默认识别条件由{@link CachedPlayerTileType}定义。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class AbstractWinType extends CachedPlayerTileType implements WinType {
	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> realAliveTiles) {
		return parseWinTileUnits(playerInfo, realAliveTiles != null ? realAliveTiles : playerInfo.getAliveTiles())
				.findAny().isPresent();
	}
}
