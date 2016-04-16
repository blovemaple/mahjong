package com.github.blovemaple.mj.rule.simple;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.rule.TimeLimitStrategy;

/**
 * 简单的限时策略，采用固定限时。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class SimpleTimeLimitStrategy implements TimeLimitStrategy {
	private final int limit;

	/**
	 * 新建一个实例。
	 * 
	 * @param discardLimit
	 *            打牌限时
	 * @param cpkLimit
	 *            其他操作限时
	 * @param timeUnit
	 *            时间单位
	 */
	public SimpleTimeLimitStrategy(int limit, TimeUnit timeUnit) {
		this.limit = (int) TimeUnit.SECONDS.convert(limit, timeUnit);
	}

	@Override
	public Integer getLimit(GameContext context,
			Map<PlayerLocation, Set<ActionType>> choises) {
		return limit;
	}

}
