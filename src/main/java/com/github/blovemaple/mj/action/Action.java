package com.github.blovemaple.mj.action;

import com.github.blovemaple.mj.object.Tile;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * 动作。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class Action {
	/**
	 * 动作类型。
	 */
	private ActionType type;
	/**
	 * 与动作相关的牌。
	 */
	private Set<Tile> tiles;

	/**
	 * 新建一个实例，没有相关的牌。
	 */
	public Action(ActionType type) {
		this(type, (Set<Tile>) null);
	}

	/**
	 * 新建一个实例，相关的牌有且只有一个。
	 */
	public Action(ActionType type, Tile tile) {
		this(type, Collections.singleton(tile));
	}

	/**
	 * 新建一个实例。
	 */
	public Action(ActionType type, Set<Tile> tiles) {
		Objects.requireNonNull(type);
		this.type = type;
		this.tiles = tiles == null ? Collections.emptySet() : tiles;
	}

	public ActionType getType() {
		return type;
	}

	public Set<Tile> getTiles() {
		return tiles;
	}

	public Tile getTile() {
		if (tiles.size() > 1)
			throw new IllegalStateException(
					"Tile count is more than 1: " + tiles.size() + tiles.toString());
		if (tiles.isEmpty())
			return null;
		return tiles.iterator().next();
	}

	public void setType(ActionType type) {
		this.type = type;
	}

	public void setTiles(Set<Tile> tiles) {
		this.tiles = tiles;
	}

	public void setTile(Tile tile) {
		this.tiles = Collections.singleton(tile);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tiles == null) ? 0 : tiles.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Action))
			return false;
		Action other = (Action) obj;
		if (tiles == null) {
			if (other.tiles != null)
				return false;
		} else if (!tiles.equals(other.tiles))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + type + ", " + tiles + "]";
	}

}
