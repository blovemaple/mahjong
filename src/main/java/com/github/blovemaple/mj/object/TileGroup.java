package com.github.blovemaple.mj.object;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.blovemaple.mj.object.PlayerLocation.Relation;

/**
 * 牌组，即玩家的牌中非活牌之外的若干个组，通常是吃、碰、杠等动作形成。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class TileGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private TileGroupType type;
	private Set<Tile> tiles;
	private Relation fromRelation;
	private Tile gotTile;

	/**
	 * 新建一个实例。
	 * 
	 * @param type
	 *            类型
	 * @param gotTile
	 *            得牌
	 * @param fromRelation
	 *            得牌来自于哪个关系的玩家
	 * @param tiles
	 *            牌组中的牌
	 * @throws IllegalArgumentException
	 *             不合法
	 */
	public TileGroup(TileGroupType type, Tile gotTile, Relation fromRelation,
			Set<Tile> tiles) {
		if (!type.isLegalTiles(tiles))
			throw new IllegalArgumentException("Illegal group tiles：[" + type
					+ "]" + Arrays.asList(tiles));

		this.type = type;
		this.gotTile = gotTile;
		this.fromRelation = fromRelation;
		this.tiles = new HashSet<>(tiles);
	}

	/**
	 * 新建一个实例，没有从其他玩家得到的牌。
	 * 
	 * @param type
	 *            类型
	 * @param tiles
	 *            牌组中的牌
	 * @throws IllegalArgumentException
	 *             不合法
	 */
	public TileGroup(TileGroupType type, Set<Tile> tiles) {
		this(type, null, null, tiles);
	}

	/**
	 * 返回类型。
	 * 
	 * @return 类型
	 */
	public TileGroupType getType() {
		return type;
	}

	/**
	 * 返回牌组中所有牌。
	 * 
	 * @return tiles 集合
	 */
	public Set<Tile> getTiles() {
		return tiles;
	}

	/**
	 * 返回得牌来自于哪个关系的玩家。
	 * 
	 * @return 玩家位置
	 */
	public Relation getFromRelation() {
		return fromRelation;
	}

	/**
	 * 返回得牌。
	 * 
	 * @return 得牌
	 */
	public Tile getGotTile() {
		return gotTile;
	}

	public void setType(TileGroupType type) {
		this.type = type;
	}

	public void setTiles(Set<Tile> tiles) {
		this.tiles = tiles;
	}

	public void setFromRelation(Relation fromRelation) {
		this.fromRelation = fromRelation;
	}

	public void setGotTile(Tile gotTile) {
		this.gotTile = gotTile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gotTile == null) ? 0 : gotTile.hashCode());
		result = prime * result
				+ ((fromRelation == null) ? 0 : fromRelation.hashCode());
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
		if (!(obj instanceof TileGroup))
			return false;
		TileGroup other = (TileGroup) obj;
		if (gotTile == null) {
			if (other.gotTile != null)
				return false;
		} else if (!gotTile.equals(other.gotTile))
			return false;
		if (fromRelation == null) {
			if (other.fromRelation != null)
				return false;
		} else if (!fromRelation.equals(other.fromRelation))
			return false;
		if (tiles == null) {
			if (other.tiles != null)
				return false;
		} else if (!tiles.equals(other.tiles))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	/**
	 * Just for debug.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + type + " " + gotTile + " from " + fromRelation
				+ " to compose " + tiles + "]";
	}

}
