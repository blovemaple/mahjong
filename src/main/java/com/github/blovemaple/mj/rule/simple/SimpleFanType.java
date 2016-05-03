package com.github.blovemaple.mj.rule.simple;

import java.util.Set;

import com.github.blovemaple.mj.object.PlayerInfo;
import com.github.blovemaple.mj.object.Tile;
import com.github.blovemaple.mj.rule.AbstractFanType;

/**
 * 简单番种，和牌即算。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class SimpleFanType extends AbstractFanType {
	public static final String NAME = "SIMPLE";

	public SimpleFanType() {
		super(NAME);
	}

	@Override
	public boolean match(PlayerInfo playerInfo, Set<Tile> aliveTiles) {
		return true;
	}

	@Override
	public boolean matchWithoutCache(PlayerInfo playerInfo, Set<Tile> aliveTiles) {
		return true;
	}

}
