package com.github.blovemaple.mj.rule;

import java.util.Map;
import java.util.Set;

import com.github.blovemaple.mj.action.ActionType;
import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerLocation;

/**
 * 限时策略。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
@FunctionalInterface
public interface TimeLimitStrategy {
	/**
	 * 不限时。
	 */
	public static final TimeLimitStrategy NO_LIMIT = (context, choises) -> null;

	/**
	 * 根据上下文返回限时。
	 * 
	 * @return 限时（单位：秒），若不限制则返回null。
	 */
	Integer getLimit(GameContext context,
			Map<PlayerLocation, Set<ActionType>> choises);
}
