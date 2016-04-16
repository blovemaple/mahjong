package com.github.blovemaple.mj.game.rule.simple;

import com.github.blovemaple.mj.game.rule.FanType;
import com.github.blovemaple.mj.object.PlayerInfo;

/**
 * 简单番种，和牌即算。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class SimpleFanType implements FanType {
	public static final String NAME = "SIMPLE";

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public boolean match(PlayerInfo playerInfo) {
		return true;
	}

}
