package com.github.blovemaple.mj.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 一个玩家的牌。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class PlayerTiles {
	/**
	 * 手中的牌。
	 */
	protected Set<Tile> aliveTiles = new HashSet<>();
	/**
	 * 吃碰杠。
	 */
	protected List<TileGroup> tileGroups = new ArrayList<>();

	public PlayerTiles() {
		super();
	}

	public Set<Tile> getAliveTiles() {
		return aliveTiles;
	}

	public void setAliveTiles(Set<Tile> aliveTiles) {
		this.aliveTiles = aliveTiles;
	}

	public List<TileGroup> getTileGroups() {
		return tileGroups;
	}

	public void setTileGroups(List<TileGroup> tileGroups) {
		this.tileGroups = tileGroups;
	}

}