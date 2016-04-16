package com.github.blovemaple.mj.object;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 麻将桌上一个玩家的信息。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class PlayerInfo {
	/**
	 * 玩家。
	 */
	private Player player = null;
	/**
	 * 手中的牌。
	 */
	private Set<Tile> aliveTiles = new HashSet<>();
	/**
	 * 最后摸的牌。
	 */
	private Tile lastDrawedTile = null;
	/**
	 * 已经打出的牌。
	 */
	private List<Tile> discardedTiles = new ArrayList<>();
	/**
	 * 吃碰杠。
	 */
	private List<TileGroup> tileGroups = new ArrayList<>();
	/**
	 * 是否听和。
	 */
	private boolean isTing = false;

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Set<Tile> getAliveTiles() {
		return aliveTiles;
	}

	public void setAliveTiles(Set<Tile> aliveTiles) {
		this.aliveTiles = aliveTiles;
	}

	public Tile getLastDrawedTile() {
		return lastDrawedTile;
	}

	public void setLastDrawedTile(Tile lastDrawedTile) {
		this.lastDrawedTile = lastDrawedTile;
	}

	public List<Tile> getDiscardedTiles() {
		return discardedTiles;
	}

	public void setDiscardedTiles(List<Tile> discardedTiles) {
		this.discardedTiles = discardedTiles;
	}

	public List<TileGroup> getTileGroups() {
		return tileGroups;
	}

	public void setTileGroups(List<TileGroup> tileGroups) {
		this.tileGroups = tileGroups;
	}

	public boolean isTing() {
		return isTing;
	}

	public void setTing(boolean isTing) {
		this.isTing = isTing;
	}

	/**
	 * 清空玩家的牌，回到初始状态。
	 */
	public void clear() {
		aliveTiles.clear();
		lastDrawedTile = null;
		discardedTiles.clear();
		tileGroups.clear();
		isTing = false;
	}

}
