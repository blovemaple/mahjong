package com.github.blovemaple.mj.rule.simple;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.rule.AbstractGameStrategy;
import com.github.blovemaple.mj.rule.FanType;
import com.github.blovemaple.mj.rule.WinType;

/**
 * 简单游戏规则。固定坐庄、没有和牌限制、和牌固定为1番。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class SimpleGameStrategy extends AbstractGameStrategy {

	@Override
	protected PlayerLocation nextZhuangLocation(GameContext context) {
		return PlayerLocation.EAST;
	}

	private static final Set<WinType> WIN_TYPES = Collections
			.singleton(new SimpleWinType());

	@Override
	public Set<WinType> getAllWinTypes() {
		return WIN_TYPES;
	}

	private static final Map<FanType, Integer> FAN_TYPES = Collections
			.singletonMap(new SimpleFanType(), 1);

	@Override
	public Map<? extends FanType, Integer> getAllFanTypes() {
		return FAN_TYPES;
	}

	@Override
	public Map<? extends FanType, Set<? extends FanType>> getAllCoveredFanTypes() {
		return Collections.emptyMap();
	}

}
