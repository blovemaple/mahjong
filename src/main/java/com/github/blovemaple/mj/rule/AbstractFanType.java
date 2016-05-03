package com.github.blovemaple.mj.rule;

import java.util.Set;
import java.util.function.Function;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;

/**
 * FanType的共同逻辑。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public abstract class AbstractFanType extends CachedPlayerTileType implements FanType {

	private final String name;

	public AbstractFanType(String name) {
		super();
		this.name = name;
	}

	@SafeVarargs
	public AbstractFanType(String name, boolean useAliveTiles, Function<PlayerInfo, ?>... otherValues) {
		super(useAliveTiles, otherValues);
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public abstract boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> aliveTiles);

}
