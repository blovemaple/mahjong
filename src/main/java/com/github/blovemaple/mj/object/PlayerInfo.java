package com.github.blovemaple.mj.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * 麻将桌上一个玩家的信息，包括玩家对象、牌，以及其他信息。
 * 
 * @author blovemaple <blovemaple2010(at)gmail.com>
 */
public class PlayerInfo extends PlayerTiles implements PlayerInfoPlayerView, Cloneable {
	/**
	 * 玩家。
	 */
	private Player player = null;
	/**
	 * 最后摸的牌。
	 */
	private Tile lastDrawedTile = null;
	/**
	 * 已经打出的牌。
	 */
	private List<Tile> discardedTiles = new ArrayList<>();
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

	@Override
	public String getPlayerName() {
		return player != null ? player.getName() : null;
	}

	@Override
	public int getAliveTileSize() {
		return getAliveTiles().size();
	}

	public Tile getLastDrawedTile() {
		return lastDrawedTile;
	}

	public void setLastDrawedTile(Tile lastDrawedTile) {
		this.lastDrawedTile = lastDrawedTile;
	}

	@Override
	public List<Tile> getDiscardedTiles() {
		return Collections.unmodifiableList(discardedTiles);
	}

	public void addDiscardedTiles(Collection<Tile> tiles) {
		discardedTiles.addAll(tiles);
	}

	@Override
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

	public PlayerInfo clone() {
		PlayerInfo c;
		try {
			c = (PlayerInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			// 不可能，因为PlayerInfo已经实现了Cloneable
			throw new RuntimeException(e);
		}
		// deep copy
		c.aliveTiles = new HashSet<>(aliveTiles);
		c.discardedTiles = new ArrayList<>(discardedTiles);
		c.tileGroups = new ArrayList<>(tileGroups);
		return c;
	}

}
