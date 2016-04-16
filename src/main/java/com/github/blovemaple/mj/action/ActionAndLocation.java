package com.github.blovemaple.mj.action;

import java.util.Objects;

import com.github.blovemaple.mj.object.PlayerLocation;

/**
 * 动作和玩家位置的组合。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class ActionAndLocation {
	private final Action action;
	private final PlayerLocation location;

	public ActionAndLocation(Action action, PlayerLocation location) {
		Objects.requireNonNull(action);
		this.action = action;
		this.location = location;
	}

	public Action getAction() {
		return action;
	}

	public PlayerLocation getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return location + action.toString();
	}

}