package com.github.blovemaple.mj.action;

import java.util.Set;

import com.github.blovemaple.mj.object.PlayerLocation;
import com.github.blovemaple.mj.object.Tile;

/**
 * 
 * 玩家做出的动作。可以附加若干牌。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class PlayerAction extends Action {

	private PlayerLocation location;
	private Set<Tile> tiles;

	/**
	 * 新建一个没有附加牌的实例。
	 */
	public PlayerAction(PlayerLocation location, ActionType type) {
		this(location, type, Set.of());
	}

	/**
	 * 新建有且只有一个附加牌的实例。
	 */
	public PlayerAction(PlayerLocation location, ActionType type, Tile tile) {
		this(location, type, Set.of(tile));
	}

	/**
	 * 新建有若干附加牌的实例。
	 */
	public PlayerAction(PlayerLocation location, ActionType type, Set<Tile> tiles) {
		super(type);
		this.location = location;
		this.tiles = tiles;
	}

	/**
	 * 返回附加牌。
	 */
	public Set<Tile> getTiles() {
		return tiles;
	}

	/**
	 * 返回唯一的附加牌。
	 */
	public Tile getTile() {
		Set<Tile> tiles = getTiles();
		if (tiles.isEmpty())
			return null;
		if (tiles.size() > 1)
			throw new IllegalStateException("Tile count is more than 1: " + tiles.size());
		return tiles.iterator().next();
	}

	/**
	 * 返回玩家位置。
	 */
	public PlayerLocation getLocation() {
		return location;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((tiles == null) ? 0 : tiles.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof PlayerAction))
			return false;
		PlayerAction other = (PlayerAction) obj;
		if (location != other.location)
			return false;
		if (tiles == null) {
			if (other.tiles != null)
				return false;
		} else if (!tiles.equals(other.tiles))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + getLocation() + ", " + getType() + ", " + tiles + "]";
	}

}
