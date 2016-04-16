package com.github.blovemaple.mj.action;

import java.util.Objects;

import com.github.blovemaple.mj.game.GameContext;
import com.github.blovemaple.mj.object.PlayerLocation;

/**
 * 动作类型和玩家位置的组合。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class ActionTypeAndLocation {
	private final ActionType actionType;
	private final PlayerLocation location;

	private final GameContext context;

	public ActionTypeAndLocation(ActionType actionType,
			PlayerLocation location) {
		this(actionType, location, null);
	}

	public ActionTypeAndLocation(ActionType actionType, PlayerLocation location,
			GameContext context) {
		Objects.requireNonNull(actionType);
		this.actionType = actionType;
		this.location = location;
		this.context = context;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public PlayerLocation getLocation() {
		return location;
	}

	public GameContext getContext() {
		return context;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((actionType == null) ? 0 : actionType.hashCode());
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
		if (getClass() != obj.getClass())
			return false;
		ActionTypeAndLocation other = (ActionTypeAndLocation) obj;
		if (actionType == null) {
			if (other.actionType != null)
				return false;
		} else if (!actionType.equals(other.actionType))
			return false;
		if (location != other.location)
			return false;
		return true;
	}

}