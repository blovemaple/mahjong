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

	public ActionType getActionType() {
		return action.getType();
	}

	public PlayerLocation getLocation() {
		return location;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ActionAndLocation))
			return false;
		ActionAndLocation other = (ActionAndLocation) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (location != other.location)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return location + action.toString();
	}

}