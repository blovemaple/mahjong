package com.github.blovemaple.mj.action;

import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerLocation;

/**
 * 尝试执行非法动作时抛出此异常。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class IllegalActionException extends Exception {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private Action action;

	public IllegalActionException(GameContext context, PlayerLocation location, Action action) {
		super(location + action.toString() + " context: " + context);
		this.action = action;
	}

}
