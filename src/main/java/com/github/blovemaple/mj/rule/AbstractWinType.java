package com.github.blovemaple.mj.rule;

import java.util.Set;
import java.util.function.Function;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;

/**
 * WinType的共同逻辑。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class AbstractWinType extends CachedPlayerTileType implements WinType {

	public AbstractWinType() {
		super();
	}

	@SafeVarargs
	public AbstractWinType(boolean useAliveTiles, Function<PlayerInfo, ?>... otherValues) {
		super(useAliveTiles, otherValues);
	}
	
	@Override
	public boolean match(PlayerInfo playerInfo, Set<Tile> aliveTiles) {
		return super.match(playerInfo, aliveTiles);
	}

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> aliveTiles) {
		return parseWinTileUnits(playerInfo, aliveTiles != null ? aliveTiles : playerInfo.getAliveTiles()).findAny()
				.isPresent();
	}
}
